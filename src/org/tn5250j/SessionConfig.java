/*
 * @(#)SessionConfig.java
 * Copyright:    Copyright (c) 2001
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * A host session configuration object
 */
public class SessionConfig extends AbstractSessionConfig {

    private String configurationResource;
    private boolean usingDefaults;
    private final String sessionName;

    public SessionConfig(final String configurationResource, final String sessionName) {
        this.sessionName = sessionName;
        this.configurationResource = configurationResource;
        loadConfigurationResource();
    }

    public String getConfigurationResource() {

        if (configurationResource == null || configurationResource.trim().isEmpty()) {
            configurationResource = "TN5250JDefaults.props";
            usingDefaults = true;
        }

        return configurationResource;

    }

    @Override
    public void saveSessionProps() {
        if (usingDefaults) {
            ConfigureFactory.getInstance().saveSettings("dfltSessionProps",
                    getConfigurationResource(),
                    "");
        } else {
            try {
                final FileOutputStream out = new FileOutputStream(settingsDirectory() + getConfigurationResource());
                try {
                    // save off the width and height to be restored later
                    ConfigureFactory.storeProperties(sesProps, "------ Defaults --------", out);
                } finally {
                    out.close();
                }
            } catch (final FileNotFoundException ignore) {
                // ignore
            } catch (final IOException ignore) {
                // ignore
            }
        }
    }

    private void loadConfigurationResource() {
        if (configurationResource == null || configurationResource.trim().isEmpty()) {
            configurationResource = "TN5250JDefaults.props";
            usingDefaults = true;
            loadDefaults();
        } else {
            try {
                final FileInputStream in = new FileInputStream(settingsDirectory() + getConfigurationResource());
                try {
                    ConfigureFactory.loadProperties(in, sesProps);
                } finally {
                    in.close();
                }

                if (sesProps.size() == 0)
                    loadDefaults();
            } catch (final IOException ioe) {
                System.out.println("Information Message: Properties file is being "
                        + "created for first time use:  File name "
                        + getConfigurationResource());
                loadDefaults();
            } catch (final SecurityException se) {
                System.out.println(se.getMessage());
            }
        }
    }

    @Override
    public void saveSessionPropsOnClose() {
        if (isModified()) {

            final Object[] args = {getConfigurationResource()};
            final String message = MessageFormat.format(
                    LangTool.getString("messages.saveSettings"),
                    args);

            final Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText(message);
            alert.getButtonTypes().add(ButtonType.APPLY);
            alert.getButtonTypes().add(ButtonType.CANCEL);

            alert.showAndWait();
            final ButtonType result = alert.getResult();

            if (result == ButtonType.APPLY) {
                saveSessionProps();
            }

            setModified(false);
        }
    }

    private String settingsDirectory() {
        return ConfigureFactory.getInstance().getProperty("emulator.settingsDirectory");
    }

    @Override
    protected void loadDefaults() {
        final ConfigureFactory configureFactory = ConfigureFactory.getInstance();
        try {
            sesProps.putAll(configureFactory
                    .getProperties("dfltSessionProps", getConfigurationResource(), true, "Default Settings"));
            if (sesProps.size() == 0) {
                sesProps.putAll(loadPropertiesFromResource(getConfigurationResource()));

                super.loadDefaults();
                configureFactory.saveSettings("dfltSessionProps", getConfigurationResource(), "");
            }
        } catch (final IOException ioe) {
            System.out.println("Information Message: Properties file is being "
                    + "created for first time use:  File name "
                    + getConfigurationResource());
        } catch (final SecurityException se) {
            System.out.println(se.getMessage());
        }
    }

    private Map<String, String> loadPropertiesFromResource(final String resourceName) throws IOException {
        final InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (in != null) {
            try {
                return ConfigureFactory.loadProperties(in);
            } finally {
                in.close();
            }
        }

        return new HashMap<>();
    }

    /* (non-Javadoc)
     * @see org.tn5250j.AbstractSessionConfig#getSessionName()
     */
    @Override
    public String getSessionName() {
        return sessionName;
    }
}
