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

import com.ibm.as400.access.ConvTable;

/**
 * @author nitram509
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 */
public abstract class AbstractConvTableCodePageConverter implements ICodepageConverter {

    private boolean doubleByteActive;
    private byte[] buff;
    private final ConvTable convTable;
    private byte previousByte;

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
    public char[] charsForNextByte(final byte index) {
        if (isShiftIn(index)) {
            doubleByteActive = true;
            buff = null;
            return null;
        }
        if (isShiftOut(index)) {
            doubleByteActive = false;
            buff = null;
            return null;
        }
        if (doubleByteActive) {
            char[] result = null;

            if (buff == null) {
                buff = new byte[]{SHIFT_IN, 0, 0, SHIFT_OUT};
            } else {
                buff[1] = previousByte;
                buff[2] = index;
                result = (convTable.byteArrayToString(buff, 0, buff.length) + " ").toCharArray();
                buff = null;
            }

            previousByte = index;
            return result;
        }

        return convTable.byteArrayToString(new byte[]{index}, 0, 1).toCharArray();
    }

    @Override
    public char ebcdic2uni(final byte index) {
        return convTable.byteArrayToString(new byte[]{index}, 0, 1).charAt(0);
    }
}
