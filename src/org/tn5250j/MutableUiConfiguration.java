/**
 *
 */
package org.tn5250j;

import org.tn5250j.keyboard.KeyMnemonic;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MutableUiConfiguration implements Cloneable {

    private boolean scrollable;
    private boolean doubleClick;
    private boolean keyPanelVisible;
    private KeyMnemonic[] keyMnemonics = {};
    private float fontSize;

    public MutableUiConfiguration() {
        super();
    }

    /**
     * @param scrollable if true the UI should be scrollable.
     */
    public void setScrollable(final boolean scrollable) {
        this.scrollable = scrollable;
    }

    /**
     * @return whether or not UI should be scrollable.
     */
    public boolean isScrollable() {
        return scrollable;
    }

    /**
     * @param value if true, should use double click as VK ENTER
     */
    public void setDoubleClick(final boolean value) {
        this.doubleClick = value;
    }

    /**
     * @return whether or not should use double click as VK ENTER.
     */
    public boolean isDoubleClick() {
        return doubleClick;
    }

    /**
     * @return whether or not key panel should be visible
     */
    public boolean isKeyPanelVisible() {
        return keyPanelVisible;
    }

    /**
     * @param keyPanelVisible if true then key panel should be visible.
     */
    public void setKeyPanelVisible(final boolean keyPanelVisible) {
        this.keyPanelVisible = keyPanelVisible;
    }

    /**
     * @param mnemonics key mnemonics.
     */
    public void setKeyMnemonics(final KeyMnemonic[] mnemonics) {
        this.keyMnemonics = mnemonics;
    }

    /**
     * @return key mnemonics.
     */
    public KeyMnemonic[] getKeyMnemonics() {
        return keyMnemonics;
    }

    /**
     * @param size font size.
     */
    public void setFontSize(final float size) {
        this.fontSize = size;
    }

    /**
     * @return font size.
     */
    public float getFontSize() {
        return fontSize;
    }

    @Override
    public MutableUiConfiguration clone() {
        MutableUiConfiguration clone;
        try {
            //perform default clone.
            clone = (MutableUiConfiguration) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        //now the cloned configuration has some reference to mnemonics as given
        //should create copy
        final KeyMnemonic[] mnemonics = clone.keyMnemonics;
        clone.keyMnemonics = new KeyMnemonic[mnemonics.length];
        System.arraycopy(mnemonics, 0, clone.keyMnemonics, 0, clone.keyMnemonics.length);

        return clone;
    }
}
