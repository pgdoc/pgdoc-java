package org.pgdoc;

import com.impossibl.postgres.jdbc.PGSQLSimpleException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.sql.*;
import java.util.*;

@AllArgsConstructor
public class SQLDocumentStore implements DocumentStore {

    private static final String serializationFailureSqlState = "40001";
    private static final String deadlockDetectedSqlState = "40P01";

    @NonNull
    @Getter
    private final Connection connection;

    @Override
    public void updateDocuments(
        Iterable<Document> updatedDocuments,
        Iterable<Document> checkedDocuments) {

        List<DocumentUpdate> documentUpdates = new ArrayList<>();

        for (Document document : updatedDocuments) {
            documentUpdates.add(
                new DocumentUpdate(document.getId(), document.getBody(), document.getVersion(), false));
        }

        for (Document document : checkedDocuments) {
            documentUpdates.add(
                new DocumentUpdate(document.getId(), null, document.getVersion(), true));
        }

        try {
            PreparedStatement statement = this.connection.prepareCall("{call update_documents(?)}");

            statement.setObject(
                1,
                connection.createArrayOf("document_update", documentUpdates.toArray(new DocumentUpdate[0])));

            statement.executeUpdate();
        } catch (PGSQLSimpleException exception) {

            if (exception.getSQLState().equals(serializationFailureSqlState)
                || exception.getSQLState().equals(deadlockDetectedSqlState)) {

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

            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM get_documents(?)");

            statement.setObject(1, connection.createArrayOf("uuid", idList.toArray(new UUID[0])));

            ResultSet resultSet = statement.executeQuery();

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
