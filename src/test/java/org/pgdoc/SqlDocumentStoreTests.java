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

import com.google.gson.Gson;
import lombok.Cleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SqlDocumentStoreTests {

    private Connection connection;
    private SqlDocumentStore store;

    private static final UUID[] ids = IntStream.rangeClosed(0, 10)
        .mapToObj(i -> new UUID(i, 255))
        .toArray(UUID[]::new);

    @BeforeEach
    void setup() throws SQLException {
        String connectionString = System.getProperty("db_connection_url");
        Properties props = new Properties();
        props.setProperty("password", System.getProperty("db_connection_password"));

        this.connection = DriverManager.getConnection(connectionString, props);
        this.store = new SqlDocumentStore(this.connection);

        @Cleanup PreparedStatement statement = store.getConnection().prepareStatement("TRUNCATE TABLE document;");
        statement.executeUpdate();
    }

    //region Constructor

    @Test
    public void new_nullArgument() {
        assertThrows(
            NullPointerException.class,
            () -> new SqlDocumentStore(null));
    }

    //endregion

    //region getConnection

    @Test
    public void getConnection_success() {
        assertEquals(this.connection, this.store.getConnection());
    }

    //endregion

    //region updateDocuments

    @Test
    public void updateDocuments_exception() {
        DocumentStoreException exception = assertThrows(
            DocumentStoreException.class,
            () -> updateDocument("{\"abc\":}", 0));

        assertEquals("22P02", ((SQLException) exception.getCause()).getSQLState());
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
        updateDocument(ids[0], "{\"abc\":\"def\"}", 0);
        updateDocument(ids[1], "{\"ghi\":\"jkl\"}", 0);

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
        updateDocument(ids[0], "{\"abc\":\"def\"}", 0);

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
        updateDocument(ids[0], "{\"abc\":\"def\"}", 0);
        updateDocument(ids[1], "{\"ghi\":\"jkl\"}", 0);

        List<Document> documents = store.getDocuments(List.of(ids[0], ids[2], ids[0], ids[1]));

        assertEquals(4, documents.size());
        assertDocument(documents.get(0), ids[0], "{\"abc\":\"def\"}", 1);
        assertDocument(documents.get(1), ids[2], null, 0);
        assertDocument(documents.get(2), ids[0], "{\"abc\":\"def\"}", 1);
        assertDocument(documents.get(3), ids[1], "{\"ghi\":\"jkl\"}", 1);
    }

    @Test
    public void getDocuments_noDocument() {
        List<Document> documents = store.getDocuments(List.of());

        assertEquals(0, documents.size());
    }

    //endregion

    //region Helper Methods

    private void updateDocument(String body, long version) {
        updateDocument(ids[0], body, version);
    }

    private void updateDocument(UUID id, String body, long version) {
        store.updateDocuments(new Document(id, body, version));
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
