/*
 * Copyright 2016 Flavien Charlon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pgdoc;

import com.impossibl.postgres.jdbc.PGSQLSimpleException;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The <code>SqlDocumentStore</code> class is an implementation of the <code>DocumentStore</code> interface that
 * relies on PosgreSQL for persistence.
 */
public class SqlDocumentStore implements DocumentStore {

    private static final String serializationFailureSqlState = "40001";
    private static final String deadlockDetectedSqlState = "40P01";

    /**
     * Gets the underlying database connection.
     */
    @Getter
    private final Connection connection;

    public SqlDocumentStore(@NonNull Connection connection) {
        this.connection = connection;
    }

    @Override
    public void updateDocuments(Iterable<Document> updatedDocuments, Iterable<Document> checkedDocuments)
        throws UpdateConflictException {

        List<DocumentUpdate> documentUpdates = new ArrayList();

        for (Document document : updatedDocuments) {
            documentUpdates.add(
                new DocumentUpdate(document.getId(), document.getBody(), document.getVersion(), false));
        }

        for (Document document : checkedDocuments) {
            documentUpdates.add(
                new DocumentUpdate(document.getId(), null, document.getVersion(), true));
        }

        try {
            @Cleanup PreparedStatement statement = this.connection.prepareCall("{call update_documents(?)}");

            statement.setObject(
                1,
                connection.createArrayOf("document_update", documentUpdates.toArray(new DocumentUpdate[0])));

            statement.executeUpdate();

        } catch (PGSQLSimpleException exception) {
            if (exception.getSQLState().equals(serializationFailureSqlState) ||
                exception.getSQLState().equals(deadlockDetectedSqlState)) {

                throw new UpdateConflictException(documentUpdates.get(0).getId(), documentUpdates.get(0).getVersion());

            } else if (exception.getMessage().equals("check_violation")) {
                DocumentUpdate conflict = documentUpdates.stream()
                    .filter(update -> update.getId().equals(UUID.fromString(exception.getDetail())))
                    .findFirst()
                    .get();

                throw new UpdateConflictException(conflict.getId(), conflict.getVersion());

            } else {
                throw new DocumentStoreException(exception.getMessage(), exception);
            }

        } catch (SQLException sqlException) {
            throw new DocumentStoreException(sqlException.getMessage(), sqlException);
        }
    }

    @Override
    public List<Document> getDocuments(Iterable<UUID> ids) {
        ArrayList<UUID> idList = new ArrayList<>();
        ids.forEach(idList::add);

        if (idList.size() == 0) {
            return List.of();
        }

        Map<UUID, Document> resultMap = new HashMap<>();
        try {
            @Cleanup PreparedStatement statement = this.connection.prepareStatement(
                "SELECT id, body, version FROM get_documents(?)");

            statement.setObject(1, connection.createArrayOf("uuid", idList.toArray(new UUID[0])));

            @Cleanup ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID id = resultSet.getObject("id", java.util.UUID.class);
                resultMap.put(id, new Document(id, resultSet.getString("body"), resultSet.getLong("version")));
            }

        } catch (SQLException sqlException) {
            throw new DocumentStoreException(sqlException.getMessage(), sqlException);
        }

        // Reorder the documents and add placeholders for the documents that were not found
        ArrayList<Document> result = new ArrayList<>(idList.size());
        for (UUID id : idList) {
            Document document = resultMap.getOrDefault(id, null);
            if (document == null) {
                result.add(new Document(id, null, 0));
            } else {
                result.add(document);
            }
        }

        return Collections.unmodifiableList(result);
    }
}
