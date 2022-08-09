package org.tn5250j.framework.tn5250;

public class ByteExplainer {

    public static final byte SHIFT_IN = 0x0e;
    public static final byte SHIFT_OUT = 0x0f;

    public static boolean isDataEBCDIC(final byte aByte) {
        final int justByte = aByte & 0xff;
        return !isShiftOut(aByte) || !isShiftIn(aByte) || (justByte >= 64 && justByte < 255);
    }

    public static boolean isShiftIn(final byte aByte) {
        return (aByte & 0xff) == SHIFT_IN;
    }

    public static boolean isShiftOut(final byte aByte) {
        return (aByte & 0xff) == SHIFT_OUT;
    }

    public static boolean isAttribute(final byte byte0) {
        final int byte1 = byte0 & 0xff;
        return (byte1 & 0xe0) == 0x20;
    }

    /**
     * Test if the unicode character is a displayable character.
     * The first 32 characters are non displayable characters
     * This is normally the inverse of isDataEBCDIC (That's why there is a check on 255 -&gt; 0xFFFF)
     *
     * @param data data
     * @return true|false
     */
    public static boolean isDataUnicode(final char data) {
        return (((data < 0) || (data >= 32)) && (data != 0xFFFF));
    }
}
