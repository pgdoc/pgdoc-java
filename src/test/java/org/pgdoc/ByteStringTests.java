package org.pgdoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ByteStringTests {

    @Test
    public void constructor_success() {

        byte[] sourceArray = new byte[] { 18, -103, -128, 127, 0 };
        ByteString result = new ByteString(sourceArray);
        sourceArray[4] = 1;

        assertNotEquals(sourceArray, result.array());
        assertArrayEquals(new byte[] { 18, -103, -128, 127, 0 }, result.array());
    }

    @Test
    public void constructor_null() {

        assertThrows(NullPointerException.class,
            () -> new ByteString(null));
    }

    @Test
    public void parse_success() {
        ByteString result = ByteString.parse("12b2FE460035789ACd");

        assertArrayEquals(new byte[] { 18, -78, -2, 70, 0, 53, 120, -102, -51 }, result.array());
    }

    @Test
    public void parse_invalidLength() {
        assertThrows(NumberFormatException.class,
            () -> ByteString.parse("12b2ff460"));
    }

    @Test
    public void parse_invalidCharacter() {
        assertThrows(NumberFormatException.class,
            () -> ByteString.parse("1G"));

        assertThrows(NumberFormatException.class,
            () -> ByteString.parse("1/"));
    }

    @Test
    public void parse_null() {
        assertThrows(NullPointerException.class,
            () -> ByteString.parse(null));
    }

    @Test
    public void array_success() {

        byte[] sourceArray = new byte[] { 18, -103, -128, 127, 0 };
        ByteString result = new ByteString(sourceArray);

        assertArrayEquals(new byte[] { 18, -103, -128, 127, 0 }, result.array());
    }

    @Test
    public void toString_success() {

        ByteString byteString = new ByteString(new byte[] { 18, -78, -2, 70, 0, 53, 120, -102, -51 });
        String result = byteString.toString();

        assertEquals("12b2fe460035789acd", result);
    }

    @Test
    public void equals_success() {

        assertTrue(ByteString.parse("abcd").equals(ByteString.parse("abcd")));
        assertFalse(ByteString.parse("abcd").equals(ByteString.parse("abce")));
        assertFalse(ByteString.parse("abcd").equals(ByteString.parse("abcdef")));
        assertFalse(ByteString.parse("abcd").equals(ByteString.parse("ab")));
        assertFalse(ByteString.parse("abcd").equals(null));
        assertFalse(ByteString.parse("abcd").equals(4));
    }

    @Test
    public void getHashCode_success() {

        ByteString value1 = ByteString.parse("000001");
        ByteString value2 = ByteString.parse("000002");
        ByteString value3 = ByteString.parse("000001");

        assertEquals(value1.hashCode(), value3.hashCode());
        assertNotEquals(value1.hashCode(), value2.hashCode());
    }
}
