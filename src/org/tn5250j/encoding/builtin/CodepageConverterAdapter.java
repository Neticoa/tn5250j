/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 *
 * @author: master_jaf
 * <p>
 * Description:
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

import java.util.Arrays;

/**
 * Adapter class for converters using 8bit codepages.
 *
 * @author master_jaf
 */
public abstract class CodepageConverterAdapter implements ICodepageConverter {

    private char[] codepage = null;
    private int[] reverse_codepage = null;

    @Override
    public void init() {
        codepage = getCodePage();

        int size = 0;
        for (final char c : codepage) {
            size = Math.max(size, c);
        }
        assert (size + 1) < 1024 * 1024; // some kind of maximum size limiter.
        reverse_codepage = new int[size + 1];
        Arrays.fill(reverse_codepage, '?');
        for (int i = 0; i < codepage.length; i++) {
            reverse_codepage[codepage[i]] = i;
        }
    }

    /* (non-Javadoc)
     * @see org.tn5250j.cp.ICodepageConverter#uni2ebcdic(char)
     */
    @Override
    public byte uni2ebcdic(final char index) {
        assert index < reverse_codepage.length;
        return (byte) reverse_codepage[index];
    }

    /* (non-Javadoc)
     * @see org.tn5250j.cp.ICodepageConverter#ebcdic2uni(int)
     */
    @Override
    public char ebcdic2uni(int index) {
        index = index & 0xFF;
        assert index < 256;
        return codepage[index];
    }

    /**
     * @return The oringal 8bit codepage.
     */
    protected abstract char[] getCodePage();

    @Override
    public boolean isDoubleByteActive() {
        return false;
    }

    @Override
    public boolean secondByteNeeded() {
        return false;
    }
}
