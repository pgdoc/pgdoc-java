package org.pgdoc.serialization;

import lombok.Getter;

import java.util.UUID;

public class EntityId {

    @Getter
    private final UUID value;

    @Getter
    private final int type;

    public EntityId(UUID id) {
        this.value = id;
        this.type = (int)(id.getMostSignificantBits() >> 32);
    }
}
