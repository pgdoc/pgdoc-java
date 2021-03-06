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

import java.util.UUID;

/**
 * Thrown when an attempt to modify a document is made using the wrong base version.
 */
public class UpdateConflictException extends RuntimeException {

    /**
     * Gets the ID of the document that caused a conflict.
     */
    @Getter
    private final UUID id;

    /**
     * Gets the version of the document that caused a conflict.
     */
    @Getter
    private final long version;

    public UpdateConflictException(UUID id, long version) {
        super(String.format("The object '%s' has been modified.", id));
        this.id = id;
        this.version = version;
    }
}
