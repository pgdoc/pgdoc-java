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
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlEntityStoreTests {

    private SqlEntityStore store;

    private static final EntityId id = new EntityId(UUID.randomUUID());

    @BeforeEach
    void setup() throws SQLException {
        String connectionString = System.getProperty("db_connection_url");
        Properties props = new Properties();
        props.setProperty("password", System.getProperty("db_connection_password"));

        Connection connection = DriverManager.getConnection(connectionString, props);
        this.store = new SqlEntityStore(connection);

        PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE document;");
        statement.executeUpdate();
    }

    @Test
    void modify_create() throws Exception {
        JsonEntity<TestJsonEntity> entity = new JsonEntity<>(
            id,
            new TestJsonEntity("initial"),
            0);

        this.store.modify(entity);

        JsonEntity<TestJsonEntity> result = this.store.getEntity(TestJsonEntity.class, id);

        assertEquals(id, result.getId());
        assertEquals("initial", result.getEntity().getValue());
        assertEquals(1, result.getVersion());
    }

    @Test
    void modify_update() throws Exception {
        JsonEntity<TestJsonEntity> initialEntity = new JsonEntity<>(
            id,
            new TestJsonEntity("initial"),
            0);

        JsonEntity<TestJsonEntity> updatedEntity = new JsonEntity<>(
            id,
            new TestJsonEntity("updated"),
            1);

        this.store.modify(initialEntity);
        this.store.modify(updatedEntity);

        JsonEntity<TestJsonEntity> result = this.store.getEntity(TestJsonEntity.class, id);

        assertEquals(id, result.getId());
        assertEquals("updated", result.getEntity().getValue());
        assertEquals(2, result.getVersion());
    }

    @Test
    void getAllEntitiesOfType_success() throws Exception {
        JsonEntity<TestJsonEntity> entity = JsonEntity.create(new TestJsonEntity("initial"));

        this.store.modify(entity);

        List<JsonEntity<TestJsonEntity>> result = this.store.getAllEntitiesOfType(TestJsonEntity.class);

        assertEquals(1, result.size());
        assertEquals(entity.getId(), result.get(0).getId());
        assertEquals("initial", result.get(0).getEntity().getValue());
        assertEquals(1, result.get(0).getVersion());
    }

    @AllArgsConstructor
    @JsonEntityType(typeId = 5)
    private class TestJsonEntity {
        @Getter
        private final String value;
    }
}
