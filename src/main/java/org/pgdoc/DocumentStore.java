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

/**
 * The <code>DocumentStore</code> interface is used to retrieve and modify documents.
 */
public interface DocumentStore {

    /**
     * Updates atomically the body of multiple documents.
     *
     * @param updatedDocuments the documents being updated
     * @param checkedDocuments the documents whose versions are checked, but which are not updated
     */
    void updateDocuments(Iterable<Document> updatedDocuments, Iterable<Document> checkedDocuments)
        throws UpdateConflictException;

    /**
     * Retrieves multiple documents given their IDs.
     *
     * @param ids the IDs of the documents to retrieve
     * @return a list of documents whose IDs were provided
     */
    List<Document> getDocuments(Iterable<UUID> ids);

    /**
     * Updates atomically the body of multiple documents.
     *
     * @param documents the documents being updated
     */
    default void updateDocuments(Document... documents)
        throws UpdateConflictException {

        this.updateDocuments(Arrays.asList(documents), List.of());
    }

    /**
     * Retrieves a document given its ID.
     *
     * @param id the ID of the document to retrieve
     * @return the document whose ID was provided
     */
    default Document getDocument(@NonNull UUID id) {
        return this.getDocuments(List.of(id)).get(0);
    }
}
