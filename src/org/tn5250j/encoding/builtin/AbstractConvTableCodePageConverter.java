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
import static org.tn5250j.framework.tn5250.ByteExplainer.isShiftIn;
import static org.tn5250j.framework.tn5250.ByteExplainer.isShiftOut;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.ibm.as400.access.ConvTable;

/**
 * @author nitram509
 */
public abstract class AbstractConvTableCodePageConverter implements ICodepageConverter {

    private final AtomicBoolean doubleByteActive = new AtomicBoolean(false);
    private final AtomicBoolean secondByteNeeded = new AtomicBoolean(false);
    private final AtomicInteger lastByte = new AtomicInteger(0);
    private final ConvTable convTable;

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

    @Override
    public byte uni2ebcdic(final char index) {
        return 0;
    }

    @Override
    public char ebcdic2uni(final int index) {
        if (isShiftIn(index)) {
            doubleByteActive.set(true);
            secondByteNeeded.set(false);
            return 0;
        }
        if (isShiftOut(index)) {
            doubleByteActive.set(false);
            secondByteNeeded.set(false);
            return 0;
        }
        if (isDoubleByteActive()) {
            if (!secondByteNeeded()) {
                lastByte.set(index);
                secondByteNeeded.set(true);
                return 0;
            } else {
                secondByteNeeded.set(false);
                return convTable.byteArrayToString(new byte[]{SHIFT_IN, lastByte.byteValue(), (byte) (index & 0xff), SHIFT_OUT}, 0, 4).charAt(0);
            }
        }
        final char ch = convTable.byteArrayToString(new byte[]{(byte) (index & 0xff)}, 0, 1).charAt(0);
        System.out.print(ch);
        return ch;
    }

    @Override
    public boolean isDoubleByteActive() {
        return doubleByteActive.get();
    }

    @Override
    public boolean secondByteNeeded() {
        return secondByteNeeded.get();
    }
}
