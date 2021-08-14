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

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentTests {

    private static final String uuid = "f81428a9-0bd9-4d75-95bf-976225f24cf1";

    @Test
    public void new_success() {

        Document document1 = new Document(UUID.fromString(uuid), "{'abc':'def'}", 2);
        Document document2 = new Document(UUID.fromString(uuid), null, 2);

        assertEquals(UUID.fromString(uuid), document1.getId());
        assertEquals("{'abc':'def'}", document1.getBody());
        assertEquals(2, document1.getVersion());
        assertEquals(UUID.fromString(uuid), document2.getId());
        assertNull(document2.getBody());
        assertEquals(2, document2.getVersion());
    }

    @Test
    public void new_nullId() {

        assertThrows(
            NullPointerException.class,
            () -> new Document(null, "{'abc':'def'}", 2));
    }
}
