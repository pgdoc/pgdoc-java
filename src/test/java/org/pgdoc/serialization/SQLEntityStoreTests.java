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

public class SQLEntityStoreTests {

    private SQLEntityStore store;

    private static final EntityId id = new EntityId(UUID.randomUUID());

    @BeforeEach
    void setup() throws SQLException {
        String connectionString = "jdbc:pgsql://localhost:5432/pgdoc";
        Properties props = new Properties();
        props.setProperty("user", "pgdoc");
        props.setProperty("password", "");

        Connection connection = DriverManager.getConnection(connectionString, props);
        this.store = new SQLEntityStore(connection);

        PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE document;");
        statement.executeUpdate();
    }

    @Test
    void modify_create() {
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
    void modify_update() {
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
    void getAllEntitiesOfType_success() {
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
