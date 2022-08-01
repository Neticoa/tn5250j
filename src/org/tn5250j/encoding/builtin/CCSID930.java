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

/**
 * @author nitram509
 */
public final class CCSID930 extends AbstractConvTableCodePageConverter {

    public final static String NAME = "930";
    public final static String DESCR = "Japan Katakana (extended range), DBCS";

    public CCSID930() {
        super("Cp930");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCR;
    }

    public String getEncoding() {
        return NAME;
    }
}
