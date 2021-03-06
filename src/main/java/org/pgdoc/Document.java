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

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * The <code>Document</code> class represents a document comprised of a unique ID, a JSON body and a version number.
 */
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

    public Document(@NonNull UUID id, String body, long version) {
        this.id = id;
        this.body = body;
        this.version = version;
    }
}
