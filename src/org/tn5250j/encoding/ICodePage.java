package org.tn5250j.encoding;

import javafx.geometry.Dimension2D;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public interface ICodePage {

    /**
     * Convert a single byte (or maybe more bytes which representing one character) to a Unicode character.
     *
     * @param index single or may be more bytes character.
     * @return java character.
     */
    char ebcdic2uni(int index);

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

    boolean isDoubleByteActive();

    boolean secondByteNeeded();

    default Dimension2D getMaxCharBounds(final Font font) {
        final Text text = new Text("W");
        text.setFont(font);
        final double w = text.getBoundsInLocal().getWidth();

        text.setText("Wg");
        final double h = text.getBoundsInLocal().getHeight();
        return new Dimension2D(w, h);
    }
}
