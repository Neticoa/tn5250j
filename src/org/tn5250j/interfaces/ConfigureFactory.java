/*
 * @(#)ConfigureFactory.java
 * @author  Kenneth J. Pouncey
 * Modified by LDC Luc
 *
 * Copyright:    Copyright (c) 2001, 2002, 2003
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.tn5250j.GlobalConfigure;

/**
 * An interface defining objects that can create Configure
 * instances.
 */
public abstract class ConfigureFactory {

    static final public String SESSIONS = "sessions";
    static final public String MACROS = "macros";
    static final public String KEYMAP = "keymap";
    private static ConfigureFactory factory;

    /**
     * @return An instance of the Configure.
     */
    public static ConfigureFactory getInstance() {
        if (factory == null) {
            setFactory(new GlobalConfigure());
        }
        return factory;
    }

    protected static final void setFactory(final ConfigureFactory f) {
        factory = f;
    }


    /**
     * Returns the setting from the given key of the global properties or the
     * default passed if the property does not exist.
     *
     * @param key
     * @param def
     * @return
     */
    public String getProperty(final String key, final String def) {
        final String value = getProperty(key);
        return value == null ? def : value;
    }

    public Map<String, String> getProperties(final String regKey, final String fileName) {
        return getProperties(regKey, fileName, false, "");
    }

    /**
     * Save the setting in the registry using the key passed in with no header
     * output.
     *
     * @param regKey
     */
    public void saveSettings(final String regKey) {
        saveSettings(regKey, "");
    }

    /**
     * Save the settings in the registry using the key passed with a header
     * in the output.
     *
     * @param regKey
     * @param header
     */
    public void saveSettings(final String regKey, final String header) {
        saveSettings(regKey, regKey, header);
    }

    public static Map<String, String> loadProperties(final InputStream in) throws IOException {
        final Map<String, String> result = new ConcurrentHashMap<>();
        loadProperties(in, result);
        return result;
    }

    /**
     * @param in input stream.
     * @param result target properties map.
     * @throws IOException
     */
    public static void loadProperties(final InputStream in, final Map<String, String> result)
            throws IOException {
        final Properties props = new Properties();
        props.load(in);

        for (final Map.Entry<?, ?> e : props.entrySet()) {
            result.put((String) e.getKey(), (String) e.getValue());
        }
    }

    /**
     * @param map map to save.
     * @param header property header.
     * @param out output stream.
     * @throws IOException
     */
    public static void storeProperties(final Map<String, String> map,
            final String header, final OutputStream out) throws IOException {
        final Properties props = new Properties();
        props.putAll(map);

        props.store(out, header);
        out.flush();
    }

    abstract public void saveSettings(String regKey, String fileName, String header);

    abstract public void reloadSettings();

    abstract public void saveSettings();

    abstract public String getProperty(String regKey);

    abstract public Map<String, String> getProperties(String regKey);

    abstract public Map<String, String> getProperties(String regKey, String fileName,
                                             boolean createFile, String header);
}
