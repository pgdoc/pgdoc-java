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
import org.pgdoc.DocumentStore;
import org.pgdoc.DocumentStoreException;
import org.pgdoc.UpdateConflictException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * The <code>EntityStore</code> class is used to retrieve and modify documents represented as
 * <code>JsonEntity</code> objects.
 */
public class EntityStore {

    /**
     * Gets the underlying <code>DocumentStore</code> object used to retrieve and modify documents.
     */
    @Getter
    private final DocumentStore documentStore;

    public EntityStore(@NonNull DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    /**
     * Updates atomically the body of multiple documents represented as <code>JsonEntity</code> objects.
     *
     * @param updatedDocuments the documents being updated
     * @param checkedDocuments the documents whose versions are checked, but which are not updated
     */
    public void updateEntities(Iterable<JsonEntity<?>> updatedDocuments, Iterable<JsonEntity<?>> checkedDocuments)
        throws DocumentStoreException, UpdateConflictException {

        this.documentStore.updateDocuments(
            StreamSupport.stream(updatedDocuments.spliterator(), false)
                .map(JsonEntity::toDocument)
                ::iterator,
            StreamSupport.stream(checkedDocuments.spliterator(), false)
                .map(JsonEntity::toDocument)
                ::iterator);
    }

    /**
     * Updates atomically the body of multiple documents represented as <code>JsonEntity</code> objects.
     *
     * @param documents the documents being updated
     */
    public void updateEntities(JsonEntity<?>... documents)
        throws DocumentStoreException, UpdateConflictException {

        this.updateEntities(Arrays.asList(documents), List.of());
    }

    /**
     * Retrieves a document given its ID, represented as a <code>JsonEntity</code> object.
     *
     * @param type     the type used to deserialize the JSON body of the document
     * @param entityId the ID of the document to retrieve
     * @return the document whose ID was provided
     */
    public <T> JsonEntity<T> getEntity(Class<T> type, EntityId entityId)
        throws DocumentStoreException {

        return JsonEntity.fromDocument(type, this.documentStore.getDocument(entityId.getValue()));
    }
}
