package org.pgdoc;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class DocumentStore {

    @NonNull
    private final Connection connection;

    public void updateDocuments(
        Iterable<Document> updatedDocuments,
        Iterable<Document> checkedDocuments)
        throws SQLException {

        PreparedStatement statement = this.connection.prepareCall("{call update_documents(?)}");

        List<DocumentUpdate> documentUpdates = new ArrayList<>();

        for (Document document: updatedDocuments) {
            documentUpdates.add(
                new DocumentUpdate(document.getId(), document.getBody(), document.getVersion(), false));
        }

        for (Document document: checkedDocuments) {
            documentUpdates.add(
                new DocumentUpdate(document.getId(), null, document.getVersion(), true));
        }

        Array array = connection.createArrayOf("document_update", documentUpdates.toArray(new DocumentUpdate[0]));
        statement.setObject(1, array);

        statement.executeUpdate();
    }
}
