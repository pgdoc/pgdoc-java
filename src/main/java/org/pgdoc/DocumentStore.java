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

package org.pgdoc;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface DocumentStore {

    /**
     * Updates atomically the body of several objects.
     * @param updatedDocuments The documents being updated.
     * @param checkedDocuments The documents of which the versions are checked, but which are not updated.
     */
    void updateDocuments(
        Iterable<Document> updatedDocuments,
        Iterable<Document> checkedDocuments)
        throws DocumentStoreException, UpdateConflictException;

    /**
     * Gets a list of documents given their IDs.
     * @param ids The IDs of the documents to retrieve.
     * @return The list of documents whose IDs were provided.
     */
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
