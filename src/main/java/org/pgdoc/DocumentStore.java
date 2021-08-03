package org.pgdoc;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class DocumentStore {

    @NonNull
    private final Connection connection;

    public ByteString updateDocuments(
        Iterable<Document> updatedDocuments,
        Iterable<Document> checkedDocuments)
        throws SQLException {

        CallableStatement statement = this.connection.prepareCall("{?= call update_documents(?, ?)}");

        List<Struct> documentUpdates = new ArrayList<Struct>();

        for (Document document : updatedDocuments) {
            documentUpdates.add(
                connection.createStruct(
                    "document_update",
                    new Object[]{document.getId(),
                        document.getBody(),
                        document.getVersion().array(),
                        false
                    }));
        }

        for (Document document : checkedDocuments) {
            documentUpdates.add(
                connection.createStruct(
                    "document_update",
                    new Object[]{
                        document.getId(),
                        null,
                        document.getVersion().array(),
                        true
                    }));
        }

        // Generate a random new version
        SecureRandom random = new SecureRandom();
        byte[] newVersion = new byte[16];
        random.nextBytes(newVersion);

        statement.setArray(0, connection.createArrayOf("document_updates", documentUpdates.toArray()));
        statement.setBytes(1, newVersion);

        statement.execute();

        return new ByteString(newVersion);
    }
}
