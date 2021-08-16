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

public class EntityId {

    @Getter
    private final UUID value;

    @Getter
    private final int type;

    public EntityId(@NonNull UUID id) {
        this.value = id;
        this.type = (int) (id.getMostSignificantBits() >> 32);
    }

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

    public static EntityId parse(@NonNull String uuid) {
        return new EntityId(UUID.fromString(uuid));
    }

    public static int getEntityType(@NonNull Class<?> type) {
        JsonEntityType annotation = type.getAnnotation(JsonEntityType.class);

        if (annotation == null) {
            throw new IllegalArgumentException(
                String.format("The type %s does not have a JsonEntityType annotation.", type.getName()));
        } else {
            return annotation.typeId();
        }
    }

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
