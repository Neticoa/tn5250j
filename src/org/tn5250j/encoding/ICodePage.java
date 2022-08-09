package org.tn5250j.encoding;

public interface ICodePage {

    /**
     * Convert a single byte (or maybe more bytes which representing one character) to a Unicode character.
     *
     * @param index single or may be more bytes character.
     * @return java character.
     */
    char ebcdic2uni(byte index);

    /**
     * @param b byte.
     * @return character array or null if unable to convert supplied bytes to character array.
     */
    default char[] charsForNextByte(final byte b) {
        return new char[] {ebcdic2uni(b)};
    }

    /**
     * Convert a Unicode character in it's byte representation.
     * Therefore, only 8bit codepages are supported.
     *
     * @param index java character.
     * @return byte character.
     */
    default byte[] char2bytes(final char index) {
        return new byte[] {uni2ebcdic(index)};
    }

    byte uni2ebcdic(char index);
}
