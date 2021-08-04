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
