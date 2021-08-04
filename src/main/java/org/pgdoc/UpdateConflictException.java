package org.pgdoc;

import lombok.Getter;

import java.util.UUID;

public class UpdateConflictException extends RuntimeException {

    @Getter
    private final UUID id;

    @Getter
    private final long version;

    public UpdateConflictException(UUID id, long version)
    {
        super(String.format("The object '%s' has been modified.", id));
        this.id = id;
        this.version = version;
    }
}
