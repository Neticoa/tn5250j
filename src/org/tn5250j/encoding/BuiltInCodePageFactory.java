/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2012
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
package org.tn5250j.encoding;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.encoding.builtin.CCSID1025;
import org.tn5250j.encoding.builtin.CCSID1026;
import org.tn5250j.encoding.builtin.CCSID1112;
import org.tn5250j.encoding.builtin.CCSID1140;
import org.tn5250j.encoding.builtin.CCSID1141;
import org.tn5250j.encoding.builtin.CCSID1147;
import org.tn5250j.encoding.builtin.CCSID1148;
import org.tn5250j.encoding.builtin.CCSID1399;
import org.tn5250j.encoding.builtin.CCSID273;
import org.tn5250j.encoding.builtin.CCSID277;
import org.tn5250j.encoding.builtin.CCSID278;
import org.tn5250j.encoding.builtin.CCSID280;
import org.tn5250j.encoding.builtin.CCSID284;
import org.tn5250j.encoding.builtin.CCSID285;
import org.tn5250j.encoding.builtin.CCSID297;
import org.tn5250j.encoding.builtin.CCSID37;
import org.tn5250j.encoding.builtin.CCSID424;
import org.tn5250j.encoding.builtin.CCSID500;
import org.tn5250j.encoding.builtin.CCSID870;
import org.tn5250j.encoding.builtin.CCSID871;
import org.tn5250j.encoding.builtin.CCSID875;
import org.tn5250j.encoding.builtin.CCSID930;
import org.tn5250j.encoding.builtin.ICodepageConverter;

/**
 * Methods for built-in code page support.
 */
/* package */ class BuiltInCodePageFactory {

    private static BuiltInCodePageFactory singleton;

    private final List<Class<?>> clazzes = new ArrayList<Class<?>>();
    private final static Logger log = LoggerFactory.getLogger(BuiltInCodePageFactory.class);

    private BuiltInCodePageFactory() {
        register();
    }

    public static synchronized final BuiltInCodePageFactory getInstance() {
        if (singleton == null) {
            singleton = new BuiltInCodePageFactory();
        }
        return singleton;
    }

    private void register() {
        clazzes.add(CCSID37.class);
        clazzes.add(CCSID273.class);
        clazzes.add(CCSID277.class);
        clazzes.add(CCSID278.class);
        clazzes.add(CCSID280.class);
        clazzes.add(CCSID284.class);
        clazzes.add(CCSID285.class);
        clazzes.add(CCSID297.class);
        clazzes.add(CCSID424.class);
        clazzes.add(CCSID500.class);
        clazzes.add(CCSID870.class);
        clazzes.add(CCSID871.class);
        clazzes.add(CCSID875.class);
        clazzes.add(CCSID930.class);
        clazzes.add(CCSID1025.class);
        clazzes.add(CCSID1026.class);
        clazzes.add(CCSID1112.class);
        clazzes.add(CCSID1140.class);
        clazzes.add(CCSID1141.class);
        clazzes.add(CCSID1147.class);
        clazzes.add(CCSID1148.class);
        clazzes.add(CCSID1399.class);
    }

    /**
     * @return unsorted list of available code pages
     */
    public String[] getAvailableCodePages() {
        final HashSet<String> cpset = new HashSet<String>();
        for (final Class<?> clazz : clazzes) {
            final ICodepageConverter converter = getConverterFromClassName(clazz);
            if (converter != null) {
                cpset.add(converter.getName());
            }
        }
        return cpset.toArray(new String[cpset.size()]);
    }

    /**
     * @param encoding
     * @return an {@link ICodePage} object OR null, of not found
     */
    public ICodePage getCodePage(final String encoding) {
        for (final Class<?> clazz : clazzes) {
            final ICodepageConverter converter = getConverterFromClassName(clazz);
            if (converter != null && converter.getName().equals(encoding)) {
                return converter;
            }
        }
        return null;
    }

    /**
     * Lazy loading converters takes time,
     * but doesn't happen so often and saves memory.
     *
     * @param clazz {@link ICodepageConverter}
     * @return
     */
    private ICodepageConverter getConverterFromClassName(final Class<?> clazz) {
        try {
            final Constructor<?> constructor = clazz.getConstructor(new Class[0]);
            final ICodepageConverter converter = (ICodepageConverter) constructor.newInstance();
            converter.init();
            return converter;
        } catch (final Exception e) {
            log.error("Couldn't load code page converter class:" + clazz.getCanonicalName(), e);
            return null;
        }
    }

}
