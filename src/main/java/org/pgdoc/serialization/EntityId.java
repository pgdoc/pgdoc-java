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

import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * The <code>EntityId</code> class encapsulates a <code>UUID</code> object whose first 32 bits are used to represent
 * an entity type.
 */
public class EntityId {

    /**
     * Gets the <code>UUID</code> representation of this <code>EntityId</code> object.
     */
    @Getter
    private final UUID value;

    /**
     * Gets the type of the entity represented by this <code>EntityId</code> object.
     */
    @Getter
    private final int type;

    public EntityId(@NonNull UUID id) {
        this.value = id;
        this.type = (int) (id.getMostSignificantBits() >> 32);
    }

    /**
     * Generates a random <code>EntityId</code> value with the specified entity type.
     */
    public static EntityId newRandom(int type) {
        byte[] bytes = new byte[16];

        // Generate a random new version
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putInt(type);

        bb.position(0);
        long high = bb.getLong();
        long low = bb.getLong();

        return new EntityId(new UUID(high, low));
    }

    /**
     * Parses an <code>EntityId</code> object from a string value.
     */
    public static EntityId fromString(@NonNull String uuid) {
        return new EntityId(UUID.fromString(uuid));
    }

    /**
     * Returns the entity type associated with a class. The target class must be annotated with the
     * {@link JsonEntityType} annotation.
     */
    public static int getEntityType(@NonNull Class<?> type) {
        JsonEntityType annotation = type.getAnnotation(JsonEntityType.class);

        if (annotation == null) {
            throw new IllegalArgumentException(
                String.format("The type %s does not have a JsonEntityType annotation.", type.getName()));
        } else {
            return annotation.typeId();
        }
    }

    /**
     * Returns a copy of this <code>EntityId</code> object with a different entity type.
     */
    public EntityId withType(int type) {
        byte[] bytes = new byte[16];

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putLong(this.value.getMostSignificantBits());
        bb.putLong(this.value.getLeastSignificantBits());
        bb.position(0);
        bb.putInt(type);
        bb.position(0);

        long high = bb.getLong();
        long low = bb.getLong();

        return new EntityId(new UUID(high, low));
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EntityId)) {
            return false;
        } else {
            return this.value.equals(((EntityId) other).value);
        }
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
