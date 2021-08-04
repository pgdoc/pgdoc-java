package org.pgdoc;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface DocumentStore {

    void updateDocuments(
        Iterable<Document> updatedDocuments,
        Iterable<Document> checkedDocuments)
        throws DocumentStoreException, UpdateConflictException;

    List<Document> getDocuments(
        Iterable<UUID> ids)
        throws DocumentStoreException;

    default void updateDocuments(Document... documents) {
        this.updateDocuments(Arrays.asList(documents), List.of());
    }

    default void updateDocument(@NonNull UUID id, String body, long version) {
        this.updateDocuments(new Document(id, body, version));
    }

    default Document getDocument(@NonNull UUID id) {
        return this.getDocuments(List.of(id)).get(0);
    }
}
