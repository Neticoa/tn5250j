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
package org.tn5250j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.interfaces.ConfigureFactory;

/**
 * Utility class for referencing global settings and functions of which at most
 * one instance can exist per VM.
 * <p>
 * Use GlobalConfigure.instance() to access this instance.
 */
public class GlobalConfigure extends ConfigureFactory {

    private static final Logger log = LoggerFactory.getLogger(GlobalConfigure.class);
    public static final String TN5250J_FOLDER = ".tn5250j";

    /**
     * A handle to the the Global Properties
     */
    static private Map<String, String> settings = new ConcurrentHashMap<>();

    static private Map<String, Map<String, String>> registry = new ConcurrentHashMap<>();
    static private Map<String, String>  headers = new ConcurrentHashMap<>();  //LUC GORRENS

    static final private String settingsFile = "tn5250jstartup.cfg";

    /**
     * The constructor is made protected to allow overriding.
     */
    public GlobalConfigure() {
        verifiySettingsFolder();
        loadSettings();
        loadSessions();
        loadMacros();
        loadKeyStrokes();
    }

    /**
     * check if folder %USERPROFILE%/.tn5250j exists
     * and create if necessary
     */
    private void verifiySettingsFolder() {
        final String settingsfolder = System.getProperty("user.home") + File.separator + TN5250J_FOLDER;
        final File f = new File(settingsfolder);
        if (!f.exists()) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("Settings folder '" + settingsfolder + "' doesn't exist. Will created now.");
                }
                f.mkdir();
            } catch (final Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Couldn't create settings folder '" + settingsfolder + "'", e);
                }
            }
        }
    }

    /**
     * Load the sessions properties
     */
    private void loadSessions() {

        setProperties(SESSIONS, SESSIONS, "------ Sessions --------", true);
    }

    /**
     * Load the macros
     */
    private void loadMacros() {

        setProperties(MACROS, MACROS, "------ Macros --------", true);

    }

    private void loadKeyStrokes() {

        setProperties(KEYMAP, KEYMAP,
                "------ Key Map key=keycode,isShiftDown,isControlDown,isAltDown,isAltGrDown --------",
                true);

    }

    /**
     * Reload the environment settings.
     */
    @Override
    public void reloadSettings() {
        if (log.isInfoEnabled()) {
            log.info("reloading settings");
        }
        loadSettings();
        loadSessions();
        loadMacros();
        loadKeyStrokes();
        if (log.isInfoEnabled()) {
            log.info("Done (reloading settings).");
        }
    }

    /**
     * Loads the emulator setting from the setting(s) file
     */
    private void loadSettings() {

        FileInputStream in = null;
        FileInputStream again = null;
        settings = new ConcurrentHashMap<>();

        // here we will check for a system property is provided first.
        if (System.getProperties().containsKey("emulator.settingsDirectory")) {
            settings.put("emulator.settingsDirectory",
                    System.getProperty("emulator.settingsDirectory") +
                            File.separator);
            checkDirs();
        } else {
            settings.put("emulator.settingsDirectory",
                    System.getProperty("user.home") + File.separator +
                            TN5250J_FOLDER + File.separator);
            try {
                in = new FileInputStream(settingsFile);
                try {
                    settings = loadProperties(in);
                } finally {
                    in.close();
                }
            } catch (final FileNotFoundException fnfe) {
                try {
                    again = new FileInputStream(settingsDirectory() + settingsFile);
                    try {
                        settings = loadProperties(again);
                    } finally {
                        again.close();
                    }
                } catch (final FileNotFoundException fnfea) {
                    log.info(" Information Message: "
                            + fnfea.getMessage() + ".  The file " + settingsFile
                            + " will be created for first time use.");
                    saveSettings();
                } catch (final IOException ioea) {
                    log.warn("IO Exception accessing File "
                            + settingsFile + " for the following reason : "
                            + ioea.getMessage());
                } catch (final SecurityException sea) {
                    log.warn("Security Exception for file "
                            + settingsFile + "  This file can not be "
                            + "accessed because : " + sea.getMessage());
                }
            } catch (final IOException ioe) {
                log.warn("IO Exception accessing File "
                        + settingsFile + " for the following reason : "
                        + ioe.getMessage());
            } catch (final SecurityException se) {
                log.warn("Security Exception for file "
                        + settingsFile + "  This file can not be "
                        + "accessed because : " + se.getMessage());
            }
        }
    }

    private void checkDirs() {
        // we now check to see if the settings directory is a directory.  If not then we create it
        final File sd = new File(settings.get("emulator.settingsDirectory"));
        if (!sd.isDirectory())
            sd.mkdirs();
    }

    /**
     * Save the settings for the global configuration
     */
    @Override
    public void saveSettings() {

        try {
            final FileOutputStream out = new FileOutputStream(settingsDirectory() + settingsFile);
            try {
                storeProperties(settings, "----------------- tn5250j Global Settings --------------", out);
            } finally {
                out.close();
            }
        } catch (final FileNotFoundException fnfe) {
        } catch (final IOException ioe) {
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
            try {
                final FileOutputStream out = new FileOutputStream(
                        settingsDirectory() + fileName);
                try {
                    storeProperties(registry.get(regKey), header, out);
                } finally {
                    out.close();
                }
            } catch (final FileNotFoundException fnfe) {
                log.warn("File not found : writing file "
                        + fileName + ".  Description of error is "
                        + fnfe.getMessage());
            } catch (final IOException ioe) {
                log.warn("IO Exception : writing file "
                        + fileName + ".  Description of error is "
                        + ioe.getMessage());
            } catch (final SecurityException se) {
                log.warn("Security Exception : writing file "
                        + fileName + ".  Description of error is "
                        + se.getMessage());
            }

        }

    }

    /**
     * Set the properties for the given registry key.
     *
     * @param regKey
     * @param fileName
     * @param header
     * @param createFile
     */
    private void setProperties(final String regKey, final String fileName, final String header,
                              final boolean createFile) {

        Map<String, String> props = new ConcurrentHashMap<>();
        headers.put(regKey, header);

        try {
            final InputStream in = new FileInputStream(settingsDirectory()
                    + fileName);
            try {
                props = loadProperties(in);
            } finally {
                in.close();
            }

        } catch (final FileNotFoundException fnfe) {

            if (createFile) {
                log.info(" Information Message: " + fnfe.getMessage()
                        + ".  The file " + fileName + " will"
                        + " be created for first time use.");

                saveSettings(regKey, header);

            } else {

                log.info(" Information Message: " + fnfe.getMessage()
                        + ".");

            }
        } catch (final IOException ioe) {
            log.warn("IO Exception accessing File " + fileName +
                    " for the following reason : "
                    + ioe.getMessage());
        } catch (final SecurityException se) {
            log.warn("Security Exception for file " + fileName
                    + ".  This file can not be accessed because : "
                    + se.getMessage());
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

            Map<String, String> props = new ConcurrentHashMap<>();
            headers.put(regKey, header);

            try {
                final InputStream in = new FileInputStream(settingsDirectory()
                        + fileName);
                try {
                    props = loadProperties(in);
                } finally {
                    in.close();
                }

            } catch (final FileNotFoundException fnfe) {

                if (createFile) {
                    log.info(" Information Message: " + fnfe.getMessage()
                            + ".  The file " + fileName + " will"
                            + " be created for first time use.");

                    registry.put(regKey, props);

                    saveSettings(regKey, header);

                    return props;

                } else {

                    log.info(" Information Message: " + fnfe.getMessage()
                            + ".");

                }
            } catch (final IOException ioe) {
                log.warn("IO Exception accessing File " + fileName +
                        " for the following reason : "
                        + ioe.getMessage());
            } catch (final SecurityException se) {
                log.warn("Security Exception for file " + fileName
                        + ".  This file can not be accessed because : "
                        + se.getMessage());
            }

            registry.put(regKey, props);

            return props;
        } else {
            return registry.get(regKey);
        }
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

    /**
     * Private helper to return the settings directory
     *
     * @return
     */
    private String settingsDirectory() {
        //System.out.println(settings.getProperty("emulator.settingsDirectory"));
        return settings.get("emulator.settingsDirectory");
    }
}
