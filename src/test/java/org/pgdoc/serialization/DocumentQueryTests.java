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

package org.pgdoc.serialization;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pgdoc.SqlDocumentStore;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DocumentQueryTests {

    private SqlDocumentStore documentStore;
    private EntityStore store;

    private static final EntityId id = new EntityId(UUID.randomUUID());

    @BeforeEach
    void setup() throws SQLException {
        String connectionString = System.getProperty("db_connection_url");
        Properties props = new Properties();
        props.setProperty("password", System.getProperty("db_connection_password"));

        this.documentStore = new SqlDocumentStore(DriverManager.getConnection(connectionString, props));
        this.store = new EntityStore(documentStore);

        @Cleanup PreparedStatement statement =
            documentStore.getConnection().prepareStatement("TRUNCATE TABLE document;");
        statement.executeUpdate();
    }

    @Test
    void execute_success() throws Exception {
        JsonEntity<TestJsonEntity> correctEntity = JsonEntity.create(new TestJsonEntity("correct"));
        JsonEntity<WrongTypeJsonEntity> incorrectEntity = JsonEntity.create(new WrongTypeJsonEntity("incorrect"));

        this.store.updateEntities(correctEntity, incorrectEntity);

        List<JsonEntity<TestJsonEntity>> result = DocumentQuery.execute(
            TestJsonEntity.class,
            this.documentStore.getConnection().prepareStatement(
                "SELECT id, body, version FROM document WHERE get_document_type(id) = 5"));

        assertEquals(1, result.size());
        assertEquals(correctEntity.getId(), result.get(0).getId());
        assertEquals("correct", result.get(0).getEntity().getValue());
        assertEquals(1, result.get(0).getVersion());
    }

    @Test
    void execute_deletedDocument() throws Exception {
        JsonEntity<TestJsonEntity> initialEntity = JsonEntity.create(new TestJsonEntity("correct"));
        JsonEntity<TestJsonEntity> deletedEntity = new JsonEntity(initialEntity.getId(), null, 1);

        this.store.updateEntities(initialEntity);
        this.store.updateEntities(deletedEntity);

        List<JsonEntity<TestJsonEntity>> result = DocumentQuery.execute(
            TestJsonEntity.class,
            this.documentStore.getConnection().prepareStatement(
                "SELECT id, body, version FROM document WHERE get_document_type(id) = 5"));

        assertEquals(1, result.size());
        assertEquals(initialEntity.getId(), result.get(0).getId());
        assertNull(result.get(0).getEntity());
        assertEquals(2, result.get(0).getVersion());
    }

    @AllArgsConstructor
    @JsonEntityType(typeId = 5)
    private class TestJsonEntity {
        @Getter
        private final String value;
    }

    @AllArgsConstructor
    @JsonEntityType(typeId = 6)
    private class WrongTypeJsonEntity {
        @Getter
        private final String value;
    }
}
