/*
 * Title: GlobalConfigure.java
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
 *
 * Description:
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
package com.metrixware.tn5250.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import com.metrixware.eclipse.Messages;

/**
 * Utility class for referencing global settings and functions of which at most
 * one instance can exist per VM.
 * <p>
 * Use GlobalConfigure.instance() to access this instance.
 */
public class PropertiesFileConfigure extends ConfigureFactory {
    private static final String SESSION = "session";

    /**
     * A handle to the the Global Properties
     */
    private Map<String, String> settings = new ConcurrentHashMap<>();

    private Map<String, Map<String, String>> registry = new ConcurrentHashMap<>();

    private final TN5250jLogger log = TN5250jLogFactory.getLogger(PropertiesFileConfigure.class);

    private final IFile file;

    /**
     * The constructor is made protected to allow overriding.
     */
    public PropertiesFileConfigure(final IFile file) {
        super();
        this.file = file;
    }

    /**
     * Reload the environment settings.
     */
    @Override
    public void reloadSettings() {
        settings.clear();

        if (log.isInfoEnabled()) {
            log.info("reloading settings");
        }

        final Map<String, String> loaded = loadSettings();

        cutProperties(loaded, SESSION);
        cutProperties(loaded, MACROS);
        cutProperties(loaded, KEYMAP);
        //put reminder to settings.
        settings.putAll(loaded);

        if (log.isInfoEnabled()) {
            log.info("Done (reloading settings).");
        }
    }

    /**
     * Loads the emulator setting from the setting(s) file
     */
    private Map<String, String> loadSettings() {
        if (file.exists()) {
            try {
                final InputStream in = file.getContents(true);
                try {
                    return loadProperties(in);
                } finally {
                    in.close();
                }
            } catch (CoreException | IOException e) {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.ErrorTitleFailedToLoadSessings, e.getMessage());
            }
        }
        return new HashMap<>();
    }

    /**
     * Save the settings for the global configuration
     */
    @Override
    public void saveSettings() {
        final Map<String, String> rawSettings = new HashMap<>();
        rawSettings.putAll(settings);

        putProperties(SESSION, rawSettings);
        putProperties(MACROS, rawSettings);
        putProperties(KEYMAP, rawSettings);

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            storeProperties(rawSettings, "----------------- tn5250j Settings --------------", out);

            final InputStream source = new ByteArrayInputStream(out.toByteArray());
            if (!file.exists()) {
                file.create(source, true, null);
            } else {
                file.setContents(source, IResource.KEEP_HISTORY, null);
            }
        } catch (final IOException | CoreException e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),
                    Messages.ErrorTitleFailedToSaveSessings, e.getMessage());
        }
    }

    private void putProperties(final String key, final Map<String, String> rawSettings) {
        if (registry.containsKey(key)) {
            final String prefix = key + ".";

            final Map<String, String> data = registry.get(key);
            for (final Map.Entry<String, String> e : data.entrySet()) {
                rawSettings.put(prefix + e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Save the settings in the registry using the key passed with a header
     * in the output.
     *
     * @param regKey
     * @param header
     */
    @Override
    public void saveSettings(final String regKey, final String fileName, final String header) {
        if (registry.containsKey(regKey)) {
            saveSettings();
        }
    }

    private void cutProperties(final Map<String, String> rawSettings, final String regKey) {

        final int offset = regKey.length() + 1;
        final Map<String, String> props = new ConcurrentHashMap<>();

        final Iterator<Map.Entry<String, String>> iter = rawSettings.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, String> e = iter.next();
            final String key = e.getKey();

            if (key.startsWith(regKey)) {
                props.put(key.substring(offset), e.getValue());
            }
        }

        registry.put(regKey, props);
    }

    /**
     * Returns the properties associated with a given registry key.
     *
     * @param regKey
     * @return
     */
    @Override
    public Map<String, String> getProperties(final String regKey) {
        return registry.get(regKey);
    }

    @Override
    public Map<String, String> getProperties(final String regKey, final String fileName,
                                    final boolean createFile, final String header,
                                    final boolean reloadIfLoaded) {
        if (!registry.containsKey(regKey) || reloadIfLoaded) {
            reloadSettings();
        }

        return registry.get(regKey);
    }

    /**
     * Returns the setting from the given key of the global properties.
     *
     * @param key
     * @return
     */
    @Override
    public String getProperty(final String key) {
        return settings.get(key);
    }
}
