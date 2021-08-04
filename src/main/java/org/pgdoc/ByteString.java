package org.pgdoc;

import lombok.NonNull;

import java.util.Arrays;

public class ByteString {
    private final byte[] data;

    public static final ByteString EMPTY = new ByteString(new byte[0]);

    public ByteString(@NonNull final byte[] data) {
        // Make a copy to avoid outside changes
        this.data = Arrays.copyOf(data, data.length);
    }

    public static ByteString parse(@NonNull String hexValue)
    {
        if (hexValue.length() % 2 == 1)
            throw new NumberFormatException("The hexValue parameter must have an even number of digits.");

        byte[] result = new byte[hexValue.length() >> 1];

        for (int i = 0; i < result.length; ++i) {
            result[i] =
                (byte) ((getHexValue(hexValue.charAt(i << 1)) << 4) + (getHexValue(hexValue.charAt((i << 1) + 1))));
        }

        return new ByteString(result);
    }

    private static int getHexValue(char hex)
    {
        int value = (int)hex;
        if (value >= '0' && value <= '9')
            return value - '0';
        else if (value >= 'a' && value <= 'f')
            return value - 'a' + 10;
        else if (value >= 'A' && value <= 'F')
            return value - 'A' + 10;
        else
            throw new NumberFormatException(String.format("The character '%s' is not a hexadecimal digit.", hex));
    }

    public byte[] array() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString()
    {
        final String hexValues = "0123456789abcdef";

        StringBuilder hex = new StringBuilder(data.length << 1);

        for (byte datum : data) {
            hex.append(hexValues.charAt(Byte.toUnsignedInt(datum) >> 4));
            hex.append(hexValues.charAt(Byte.toUnsignedInt(datum) & 0xF));
        }

        return hex.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteString)) {
            return false;
        }

        ByteString otherByteString = (ByteString) other;

        if (this.data.length != otherByteString.data.length) {
            return false;
        }

        for (int i = 0; i < this.data.length; i++) {
            if (this.data[i] != otherByteString.data[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 113327;
        for (byte datum : data) {
            result = (result * 486187739) ^ Byte.toUnsignedInt(datum);
        }

        return result;
    }
}
