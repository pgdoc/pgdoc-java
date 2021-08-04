package org.pgdoc;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

public class Document {

    /**
     * Gets the unique identifier of the document.
     */
    @Getter
    private final UUID id;

    /**
     * Gets the JSON body of the document as a string, or null if the document does not exist.
     */
    @Getter
    private final String body;

    /**
     * Gets the current version of the document.
     */
    @Getter
    private final long version;

    public Document(
        @NonNull UUID id,
        String body,
        long version)
    {
        this.id = id;
        this.body = body;
        this.version = version;
    }
}
