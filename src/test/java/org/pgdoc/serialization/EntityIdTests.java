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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EntityIdTests {

    private final String guid = "d31b6b50-fda9-11e8-b568-0800200c9a66";

    @ParameterizedTest
    @ValueSource(ints = {
        0,
        1,
        -1,
        Integer.MIN_VALUE,
        Integer.MAX_VALUE,
    })
    public void newRandom_success(int type) {

        EntityId entityId = EntityId.newRandom(type);
        assertEquals(type, entityId.getType());
    }

    @Test
    public void newRandom_Random() {

        EntityId entityId1 = EntityId.newRandom(1);
        EntityId entityId2 = EntityId.newRandom(1);

        assertNotEquals(entityId1.getValue(), entityId2.getValue());
    }

    @Test
    public void toString_success()
    {
        EntityId value = EntityId.parse(guid);

        assertEquals(guid, value.toString());
    }

    @Test
    public void equals_success()
    {
        EntityId value1 = EntityId.newRandom(1);
        EntityId value2 = EntityId.newRandom(1);
        EntityId value3 = new EntityId(
            new UUID(
                value1.getValue().getMostSignificantBits(),
                value1.getValue().getLeastSignificantBits()));

        assertTrue(value1.equals(value1));
        assertTrue(value1.equals(value3));
        assertFalse(value1.equals(value2));
        assertFalse(value1.equals("string"));
        assertFalse(value1.equals(null));
    }

    @Test
    public void hashCode_success()
    {
        EntityId value1 = EntityId.newRandom(1);
        EntityId value2 = EntityId.newRandom(1);
        EntityId value3 = new EntityId(
            new UUID(
                value1.getValue().getMostSignificantBits(),
                value1.getValue().getLeastSignificantBits()));

        assertEquals(value1.hashCode(), value3.hashCode());
        assertNotEquals(value1.hashCode(), value2.hashCode());
    }
}
