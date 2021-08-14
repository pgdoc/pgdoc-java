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

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pgdoc.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JsonEntityTests {

    private static final UUID guid = UUID.fromString("f81428a9-0bd9-4d75-95bf-976225f24cf1");
    private static final long version = 10;
    private static final LocalDateTime date =
        LocalDateTime.of(2009, 1, 3, 20, 30, 0);

    @Test
    public void fromDocument_success() {

        TestJsonEntity testObject = new TestJsonEntity();
        testObject.setStringValue("value");
        testObject.setInt64Value(12345);
        testObject.setBoolValue(true);
        testObject.setDateValue(date);
        testObject.setInstantValue(date.toInstant(ZoneOffset.UTC));

        JsonEntity<TestJsonEntity> entity = new JsonEntity<>(new EntityId(guid), testObject, version);

        Document document = entity.toDocument();
        JsonEntity<TestJsonEntity> result = JsonEntity.fromDocument(TestJsonEntity.class, document);

        assertEquals(guid, document.getId());
        assertEquals(version, document.getVersion());
        assertEquals("{\"string_value\":\"value\",\"int64_value\":12345,\"bool_value\":true,\"date_value\":\"2009-01-03T20:30:00\",\"instant_value\":\"2009-01-03T20:30:00Z\"}", document.getBody());

        assertEquals(guid, result.getId().getValue());
        assertEquals(version, result.getVersion());
        assertEquals("value", result.getEntity().getStringValue());
        assertEquals(12345, result.getEntity().getInt64Value());
        assertEquals(true, result.getEntity().isBoolValue());
        assertEquals(date, result.getEntity().getDateValue());
        assertEquals(date.toInstant(ZoneOffset.UTC), result.getEntity().getInstantValue());
    }

    @Test
    public void fromDocument_nullDocument() {

        JsonEntity<TestJsonEntity> entity = new JsonEntity<>(new EntityId(guid), null, version);

        Document document = entity.toDocument();
        JsonEntity<TestJsonEntity> result = JsonEntity.fromDocument(TestJsonEntity.class, document);

        assertEquals(guid, document.getId());
        assertEquals(version, document.getVersion());
        assertNull(document.getBody());

        assertEquals(guid, result.getId().getValue());
        assertEquals(version, result.getVersion());
        assertNull(result.getEntity());
    }

    @Test
    public void fromDocument_nullValues() {

        TestJsonEntity testObject = new TestJsonEntity();

        JsonEntity<TestJsonEntity> entity = new JsonEntity<>(new EntityId(guid), testObject, version);

        Document document = entity.toDocument();
        JsonEntity<TestJsonEntity> result = JsonEntity.fromDocument(TestJsonEntity.class, document);

        assertEquals(guid, document.getId());
        assertEquals(version, document.getVersion());
        assertEquals("{\"int64_value\":0,\"bool_value\":false}", document.getBody());

        assertEquals(guid, result.getId().getValue());
        assertEquals(version, result.getVersion());
        assertEquals(null, result.getEntity().getStringValue());
        assertEquals(0, result.getEntity().getInt64Value());
        assertEquals(false, result.getEntity().isBoolValue());
        assertEquals(null, result.getEntity().getDateValue());
        assertEquals(null, result.getEntity().getInstantValue());
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, Long.MAX_VALUE, 0, 1, -1 })
    public void fromDocument_serializeInt64(long value) {

        TestJsonEntity testObject = new TestJsonEntity();
        testObject.setInt64Value(value);

        JsonEntity<TestJsonEntity> entity = new JsonEntity<>(new EntityId(guid), testObject, version);

        Document document = entity.toDocument();
        JsonEntity<TestJsonEntity> result = JsonEntity.fromDocument(TestJsonEntity.class, document);

        assertEquals(value, result.getEntity().getInt64Value());
    }

    @Test
    public void create_success() {

        TestJsonEntity testObject = new TestJsonEntity();
        testObject.setInt64Value(100);

        JsonEntity<TestJsonEntity> result = JsonEntity.create(testObject);

        assertEquals(5, result.getId().getType());
        assertEquals(0, result.getVersion());
        assertEquals(100, result.getEntity().getInt64Value());
    }

    @Test
    public void modify_success() {

        TestJsonEntity initialObject = new TestJsonEntity();
        initialObject.setInt64Value(100);

        TestJsonEntity newObject = new TestJsonEntity();
        newObject.setInt64Value(200);

        JsonEntity<TestJsonEntity> initialValue = new JsonEntity<>(new EntityId(guid), initialObject, version);
        JsonEntity<TestJsonEntity> result = initialValue.modify(newObject);

        assertEquals(new EntityId(guid), result.getId());
        assertEquals(version, result.getVersion());
        assertEquals(200, result.getEntity().getInt64Value());
    }

    @NoArgsConstructor
    @JsonEntityType(typeId = 5)
    private class TestJsonEntity {

        @Getter
        @Setter
        @SerializedName(value = "string_value")
        private String stringValue;

        @Getter
        @Setter
        @SerializedName(value = "int64_value")
        private long int64Value;

        @Getter
        @Setter
        @SerializedName(value = "bool_value")
        private boolean boolValue;

        @Getter
        @Setter
        @SerializedName(value = "date_value")
        private LocalDateTime dateValue;

        @Getter
        @Setter
        @SerializedName(value = "instant_value")
        private Instant instantValue;
    }
}
