package org.pgdoc;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class DocumentStoreTests {

    @Test
    public void updateDocuments_emptyToValue() throws SQLException {

        String connectionString = "jdbc:pgsql://localhost:5432/pgdoc";
        Properties props = new Properties();
        props.setProperty("user", "pgdoc");
        props.setProperty("password", "");

        Connection connection = DriverManager.getConnection(connectionString, props);

        DocumentStore store = new DocumentStore(connection);
        store.updateDocuments(
            List.of(
                new Document(UUID.randomUUID(), "{}", 0)
            ),
            List.of());
    }
}
