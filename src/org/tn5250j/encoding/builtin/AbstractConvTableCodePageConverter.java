/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009,2021
 * Company:
 *
 * @author: nitram509
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.encoding.builtin;

import static org.tn5250j.framework.tn5250.ByteExplainer.SHIFT_IN;
import static org.tn5250j.framework.tn5250.ByteExplainer.SHIFT_OUT;
import static org.tn5250j.framework.tn5250.ByteExplainer.isDataUnicode;
import static org.tn5250j.framework.tn5250.ByteExplainer.isShiftIn;
import static org.tn5250j.framework.tn5250.ByteExplainer.isShiftOut;

import java.io.UnsupportedEncodingException;

import org.tn5250j.tools.GUIGraphicsUtils;

import com.ibm.as400.access.ConvTable;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.text.Font;

/**
 * @author nitram509
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 */
public abstract class AbstractConvTableCodePageConverter implements ICodepageConverter {

    private boolean doubleByteActive;
    private boolean secondByteNeeded;
    private byte lastByte;
    private ConvTable convTable;

    public AbstractConvTableCodePageConverter(final String encoding) {
        try {
            convTable = ConvTable.getTable(encoding);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
    }

    /* (non-Javadoc)
     * @see org.tn5250j.encoding.ICodePage#char2bytes(char)
     */
    @Override
    public byte[] char2bytes(final char index) {
        if (isDataUnicode(index)) {
            return convTable.stringToByteArray(new String(new char[] {index}));
        } else {
            return char2bytes(index);
        }
    }

    @Override
    public byte uni2ebcdic(final char index) {
        return (byte) index;
    }

    @Override
    public char ebcdic2uni(final int index) {
        if (isShiftIn(index)) {
            doubleByteActive = true;
            secondByteNeeded = false;
            return 0;
        }
        if (isShiftOut(index)) {
            doubleByteActive = false;
            secondByteNeeded = false;
            return 0;
        }
        if (isDoubleByteActive()) {
            if (!secondByteNeeded()) {
                lastByte = (byte) index;
                secondByteNeeded = true;
                return 0;
            } else {
                secondByteNeeded = false;
                return convTable.byteArrayToString(new byte[]{SHIFT_IN, lastByte, (byte) index, SHIFT_OUT}, 0, 4).charAt(0);
            }
        }

        return convTable.byteArrayToString(new byte[]{(byte) index}, 0, 1).charAt(0);
    }

    @Override
    public boolean isDoubleByteActive() {
        return doubleByteActive;
    }

    @Override
    public boolean secondByteNeeded() {
        return secondByteNeeded;
    }

    @Override
    public Dimension2D getMaxCharBounds(final Font font) {
        final double minX = GUIGraphicsUtils.getCharBounds(font, '\u006a').getMinX();

        final Bounds ch8000 = GUIGraphicsUtils.getCharBounds(font, '\u8000');
        final double maxX = ch8000.getMaxX() + 2; // uncomment this if you want to increase the char size
        //final double maxX = ch8000.getMaxX();
        final double minY = ch8000.getMinY();
        final double maxY = ch8000.getMaxY() + 6; // uncomment this if you want to increase the char size
        // increasing the maxX and the maxY can reduce the annoying the padding issue, but you must 
        // recalculate the font in AbstractGuiGraphicBuffer (resizeScreenArea).
        //final double maxY = ch8000.getMaxY();


        return new Dimension2D(maxX - minX, maxY - minY);
    }
}
