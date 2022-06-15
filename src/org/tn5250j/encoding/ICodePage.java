package org.tn5250j.encoding;

public interface ICodePage {

    /**
     * Convert a single byte (or maybe more bytes which representing one character) to a Unicode character.
     *
     * @param index single or may be more bytes character.
     * @return java character.
     */
    public abstract char ebcdic2uni(int index);

    /**
     * Convert a Unicode character in it's byte representation.
     * Therefore, only 8bit codepages are supported.
     *
     * @param index java character.
     * @return byte character.
     */
    public abstract byte uni2ebcdic(char index);

    boolean isDoubleByteActive();

    boolean secondByteNeeded();
}
