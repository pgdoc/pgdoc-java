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
        this.type = (int)(id.getMostSignificantBits() >> 32);
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
        return annotation.typeId();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EntityId)) {
            return false;
        }
        else {
            return this.value.equals(((EntityId)other).value);
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
