package org.pgdoc;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SQLDocumentStoreTests {

    private SQLDocumentStore store;

    private static final UUID[] ids = IntStream.rangeClosed(0, 10)
        .mapToObj(i -> new UUID(i, 255))
        .toArray(UUID[]::new);

    @BeforeEach
    void setup() throws SQLException {
        String connectionString = "jdbc:pgsql://localhost:5432/pgdoc";
        Properties props = new Properties();
        props.setProperty("user", "pgdoc");
        props.setProperty("password", "");

        Connection connection = DriverManager.getConnection(connectionString, props);
        this.store = new SQLDocumentStore(connection);

        PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE document;");
        statement.executeUpdate();
    }

    //region Constructor

    @Test
    public void new_nullArgument() {
        assertThrows(
            NullPointerException.class,
            () -> new SQLDocumentStore(null));
    }

    //endregion

    //region updateDocuments

    @Test
    public void updateDocuments_exception() {

        DocumentStoreException exception = assertThrows(
            DocumentStoreException.class,
            () -> updateDocument("{\"abc\":}", 0));

        assertEquals("22P02", ((SQLException)exception.getCause()).getSQLState());
    }

    @ParameterizedTest
    @MethodSource("updateDocuments_oneArgument")
    public void updateDocuments_emptyToValue(String to) {

        updateDocument(to, 0);

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], to, 1);
    }

    @ParameterizedTest
    @MethodSource("updateDocuments_twoArguments")
    public void updateDocuments_valueToValue(String from, String to) {

        updateDocument(from, 0);
        updateDocument(to, 1);

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], to, 2);
    }

    @Test
    public void updateDocuments_emptyToCheck() {

        checkDocument(0);

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], null, 0);
    }

    @ParameterizedTest
    @MethodSource("updateDocuments_oneArgument")
    public void updateDocuments_valueToCheck(String from) {

        updateDocument(from, 0);
        checkDocument(1);

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], from, 1);
    }

    @ParameterizedTest
    @MethodSource("updateDocuments_oneArgument")
    public void updateDocuments_checkToValue(String to) {

        checkDocument(0);
        updateDocument(to, 0);

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], to, 1);
    }

    @Test
    public void updateDocuments_checkToCheck() {

        checkDocument(0);
        checkDocument(0);

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], null, 0);
    }

    private static Stream<Arguments> updateDocuments_twoArguments() {
        return Stream.of(
            Arguments.of("{\"abc\":\"def\"}", "{\"ghi\":\"jkl\"}"),
            Arguments.of(null, "{\"ghi\":\"jkl\"}"),
            Arguments.of("{\"abc\":\"def\"}", null),
            Arguments.of(null, null));
    }

    private static Stream<String> updateDocuments_oneArgument() {
        return Stream.of(
            "{\"abc\":\"def\"}",
            null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateDocuments_conflictDocumentDoesNotExist(boolean checkOnly) {

        UpdateConflictException exception = assertThrows(
            UpdateConflictException.class,
            checkOnly
                ? () -> checkDocument(10)
                : () -> updateDocument("{\"abc\":\"def\"}", 10));

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], null, 0);
        assertEquals(ids[0], exception.getId());
        assertEquals(10, exception.getVersion());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateDocuments_conflictWrongVersion(boolean checkOnly) {

        updateDocument("{\"abc\":\"def\"}", 0);

        UpdateConflictException exception = assertThrows(
            UpdateConflictException.class,
            checkOnly
                ? () -> checkDocument(10)
                : () -> updateDocument("{\"abc\":\"def\"}", 10));

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], "{\"abc\":\"def\"}", 1);
        assertEquals(ids[0], exception.getId());
        assertEquals(10, exception.getVersion());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateDocuments_conflictDocumentAlreadyExists(boolean checkOnly) {

        updateDocument("{\"abc\":\"def\"}", 0);

        UpdateConflictException exception = assertThrows(
            UpdateConflictException.class,
            checkOnly
                ? () -> checkDocument(0)
                : () -> updateDocument("{\"abc\":\"def\"}", 0));

        Document document = store.getDocument(ids[0]);

        assertDocument(document, ids[0], "{\"abc\":\"def\"}", 1);
        assertEquals(ids[0], exception.getId());
        assertEquals(0, exception.getVersion());
    }

    @Test
    public void updateDocuments_multipleDocumentsSuccess() {

        store.updateDocument(ids[0], "{\"abc\":\"def\"}", 0);
        store.updateDocument(ids[1], "{\"ghi\":\"jkl\"}", 0);

        store.updateDocuments(
            List.of(
                new Document(ids[0], "{\"v\":\"1\"}", 1),
                new Document(ids[2], "{\"v\":\"2\"}", 0)
            ),
            List.of(
                new Document(ids[1], "{\"v\":\"3\"}", 1),
                new Document(ids[3], "{\"v\":\"4\"}", 0)
            )
        );

        Document document1 = store.getDocument(ids[0]);
        Document document2 = store.getDocument(ids[1]);
        Document document3 = store.getDocument(ids[2]);
        Document document4 = store.getDocument(ids[3]);

        assertDocument(document1, ids[0], "{\"v\":\"1\"}", 2);
        assertDocument(document2, ids[1], "{\"ghi\":\"jkl\"}", 1);
        assertDocument(document3, ids[2], "{\"v\":\"2\"}", 1);
        assertDocument(document4, ids[3], null, 0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateDocuments_multipleDocumentsConflict(boolean checkOnly) {

        store.updateDocument(ids[0], "{\"abc\":\"def\"}", 0);

        UpdateConflictException exception = assertThrows(
            UpdateConflictException.class,
            () -> {
                if (checkOnly) {
                    store.updateDocuments(
                        List.of(new Document(ids[0], "{\"ghi\":\"jkl\"}", 1)),
                        List.of(new Document(ids[1], "{\"mno\":\"pqr\"}", 10))
                    );
                } else {
                    store.updateDocuments(
                        new Document(ids[0], "{\"ghi\":\"jkl\"}", 1),
                        new Document(ids[1], "{\"mno\":\"pqr\"}", 10));
                }
            }
        );

        Document document1 = store.getDocument(ids[0]);
        Document document2 = store.getDocument(ids[1]);

        assertDocument(document1, ids[0], "{\"abc\":\"def\"}", 1);
        assertDocument(document2, ids[1], null, 0);
        assertEquals(ids[1], exception.getId());
        assertEquals(10, exception.getVersion());
    }

    //endregion

    //region GetDocuments

    @Test
    public void getDocuments_singleDocument() {

        updateDocument("{\"abc\":\"def\"}", 0);

        List<Document> documents = store.getDocuments(List.of(ids[0]));

        assertEquals(1, documents.size());
        assertDocument(documents.get(0), ids[0], "{\"abc\":\"def\"}", 1);
    }

    @Test
    public void getDocuments_multipleDocuments() {

        store.updateDocument(ids[0], "{\"abc\":\"def\"}", 0);
        store.updateDocument(ids[1], "{\"ghi\":\"jkl\"}", 0);

        List<Document> documents = store.getDocuments(List.of(ids[0], ids[2], ids[0], ids[1]));

        assertEquals(4, documents.size());
        assertDocument(documents.get(0), ids[0], "{\"abc\":\"def\"}", 1);
        assertDocument(documents.get(1), ids[2], null, 0);
        assertDocument(documents.get(2), ids[0], "{\"abc\":\"def\"}", 1);
        assertDocument(documents.get(3), ids[1], "{\"ghi\":\"jkl\"}", 1);
    }

    @Test
    public void getDocuments_noDocument() {

        List<Document> documents = store.getDocuments(List.of(ids[0]));

        assertEquals(1, documents.size());
        assertDocument(documents.get(0), ids[0], null, 0);
    }

    //endregion

    //region Helper Methods

    private void updateDocument(String body, long version) {
        store.updateDocument(ids[0], body, version);
    }

    private void checkDocument(long version) {
        store.updateDocuments(
            List.of(),
            List.of(new Document(ids[0], "{\"ignored\":\"ignored\"}", version)));
    }

    private static void assertDocument(Document document, UUID id, String body, long version) {
        assertEquals(id, document.getId());

        if (body == null) {
            assertNull(document.getBody());
        } else {
            assertNotNull(document.getBody());

            Gson gson = new Gson();

            // Normalize and compare JSON
            String expectedJson = gson.toJson(gson.fromJson(body, Object.class));
            String actualJson = gson.toJson(gson.fromJson(document.getBody(), Object.class));
            assertEquals(expectedJson, actualJson);
        }

        assertEquals(version, document.getVersion());
    }

    //endregion
}
