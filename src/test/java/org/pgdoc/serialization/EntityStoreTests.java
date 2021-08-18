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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EntityStoreTests {

    private EntityStore store;

    private static final EntityId id = new EntityId(UUID.randomUUID());

    @BeforeEach
    void setup() throws SQLException {
        String connectionString = System.getProperty("db_connection_url");
        Properties props = new Properties();
        props.setProperty("password", System.getProperty("db_connection_password"));

        Connection connection = DriverManager.getConnection(connectionString, props);
        this.store = new SqlEntityStore(connection);

        @Cleanup PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE document;");
        statement.executeUpdate();
    }

    @Test
    void updateEntities_create() throws Exception {
        JsonEntity<TestJsonEntity> entity = new JsonEntity<>(
            id,
            new TestJsonEntity("initial"),
            0);

        this.store.updateEntities(entity);

        JsonEntity<TestJsonEntity> result = this.store.getEntity(TestJsonEntity.class, id);

        assertEquals(id, result.getId());
        assertEquals("initial", result.getEntity().getValue());
        assertEquals(1, result.getVersion());
    }

    @Test
    void updateEntities_update() throws Exception {
        JsonEntity<TestJsonEntity> initialEntity = new JsonEntity<>(
            id,
            new TestJsonEntity("initial"),
            0);

        JsonEntity<TestJsonEntity> updatedEntity = new JsonEntity<>(
            id,
            new TestJsonEntity("updated"),
            1);

        this.store.updateEntities(initialEntity);
        this.store.updateEntities(updatedEntity);

        JsonEntity<TestJsonEntity> result = this.store.getEntity(TestJsonEntity.class, id);

        assertEquals(id, result.getId());
        assertEquals("updated", result.getEntity().getValue());
        assertEquals(2, result.getVersion());
    }

    @Test
    void updateEntities_check() throws Exception {
        JsonEntity<TestJsonEntity> initialEntity = new JsonEntity<>(
            id,
            new TestJsonEntity("initial"),
            0);
        JsonEntity<TestJsonEntity> checkEntity = new JsonEntity<>(
            id,
            null,
            1);

        this.store.updateEntities(initialEntity);
        this.store.updateEntities(List.of(), List.of(checkEntity));

        JsonEntity<TestJsonEntity> result = this.store.getEntity(TestJsonEntity.class, id);

        assertEquals(id, result.getId());
        assertEquals("initial", result.getEntity().getValue());
        assertEquals(1, result.getVersion());
    }

    @Test
    void updateEntities_multiple() throws Exception {
        JsonEntity<TestJsonEntity> entity1 = JsonEntity.create(new TestJsonEntity("initial1"));
        JsonEntity<IntJsonEntity> entity2 = JsonEntity.create(new IntJsonEntity(1000));

        this.store.updateEntities(entity1, entity2);

        JsonEntity<TestJsonEntity> result1 = this.store.getEntity(TestJsonEntity.class, entity1.getId());
        JsonEntity<IntJsonEntity> result2 = this.store.getEntity(IntJsonEntity.class, entity2.getId());

        assertEquals(entity1.getId(), result1.getId());
        assertEquals("initial1", result1.getEntity().getValue());
        assertEquals(1, result1.getVersion());
        assertEquals(entity2.getId(), result2.getId());
        assertEquals(1000, result2.getEntity().getIntValue());
        assertEquals(1, result2.getVersion());
    }

    @Test
    void getEntity_noEntity() throws Exception {
        JsonEntity<TestJsonEntity> result = this.store.getEntity(TestJsonEntity.class, id);

        assertEquals(id, result.getId());
        assertNull(result.getEntity());
        assertEquals(0, result.getVersion());
    }

    private class SqlEntityStore extends SqlDocumentStore implements EntityStore {
        public SqlEntityStore(Connection connection) {
            super(connection);
        }
    }

    @AllArgsConstructor
    @JsonEntityType(typeId = 5)
    private class TestJsonEntity {
        @Getter
        private final String value;
    }

    @AllArgsConstructor
    @JsonEntityType(typeId = 6)
    private class IntJsonEntity {
        @Getter
        private final int intValue;
    }
}
