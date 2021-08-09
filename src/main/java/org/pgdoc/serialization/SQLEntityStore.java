package org.pgdoc.serialization;

import org.pgdoc.Document;
import org.pgdoc.DocumentStoreException;
import org.pgdoc.SQLDocumentStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SQLEntityStore extends SQLDocumentStore {

    public SQLEntityStore(Connection connection) {
        super(connection);
    }

    public void modify(JsonEntity<?>... entities) {
        List<Document> documents =
            Arrays.stream(entities)
                .map(JsonEntity::toDocument)
                .collect(Collectors.toList());

        this.updateDocuments(documents, List.of());
    }

    public <T> List<JsonEntity<T>> getAllEntitiesOfType(Class<T> type) {
        try {
            PreparedStatement statement = this.getConnection().prepareStatement(
                "SELECT id, body, version FROM document WHERE get_document_type(id) = (?)");

            statement.setInt(1, EntityId.getEntityType(type));

            return this.executeDocumentQuery(type, statement);
        } catch (SQLException sqlException) {
            throw new DocumentStoreException(sqlException.getMessage(), sqlException);
        }
    }

    public <T> JsonEntity<T> getEntity(Class<T> type, EntityId entityId) {
        Document document = this.getDocuments(List.of(entityId.getValue())).get(0);

        return JsonEntity.fromDocument(type, document);
    }

    protected <T> List<JsonEntity<T>> executeDocumentQuery(Class<T> type, PreparedStatement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery();

        ArrayList<JsonEntity<T>> result = new ArrayList<>();

        while (resultSet.next()) {
            Document document = new Document(
                resultSet.getObject("id", java.util.UUID.class),
                resultSet.getString("body"),
                resultSet.getLong("version")
            );

            result.add(JsonEntity.fromDocument(type, document));
        }

        return Collections.unmodifiableList(result);
    }
}
