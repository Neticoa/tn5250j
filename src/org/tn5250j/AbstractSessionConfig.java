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

import static java.lang.Float.parseFloat;
import static org.tn5250j.keyboard.KeyMnemonic.CLEAR;
import static org.tn5250j.keyboard.KeyMnemonic.ENTER;
import static org.tn5250j.keyboard.KeyMnemonic.PAGE_DOWN;
import static org.tn5250j.keyboard.KeyMnemonic.PAGE_UP;
import static org.tn5250j.keyboard.KeyMnemonic.PF1;
import static org.tn5250j.keyboard.KeyMnemonic.PF10;
import static org.tn5250j.keyboard.KeyMnemonic.PF11;
import static org.tn5250j.keyboard.KeyMnemonic.PF12;
import static org.tn5250j.keyboard.KeyMnemonic.PF13;
import static org.tn5250j.keyboard.KeyMnemonic.PF14;
import static org.tn5250j.keyboard.KeyMnemonic.PF15;
import static org.tn5250j.keyboard.KeyMnemonic.PF16;
import static org.tn5250j.keyboard.KeyMnemonic.PF17;
import static org.tn5250j.keyboard.KeyMnemonic.PF18;
import static org.tn5250j.keyboard.KeyMnemonic.PF19;
import static org.tn5250j.keyboard.KeyMnemonic.PF2;
import static org.tn5250j.keyboard.KeyMnemonic.PF20;
import static org.tn5250j.keyboard.KeyMnemonic.PF21;
import static org.tn5250j.keyboard.KeyMnemonic.PF22;
import static org.tn5250j.keyboard.KeyMnemonic.PF23;
import static org.tn5250j.keyboard.KeyMnemonic.PF24;
import static org.tn5250j.keyboard.KeyMnemonic.PF3;
import static org.tn5250j.keyboard.KeyMnemonic.PF4;
import static org.tn5250j.keyboard.KeyMnemonic.PF5;
import static org.tn5250j.keyboard.KeyMnemonic.PF6;
import static org.tn5250j.keyboard.KeyMnemonic.PF7;
import static org.tn5250j.keyboard.KeyMnemonic.PF8;
import static org.tn5250j.keyboard.KeyMnemonic.PF9;
import static org.tn5250j.keyboard.KeyMnemonic.SYSREQ;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicSerializer;
import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * A host session configuration object
 */
public abstract class AbstractSessionConfig {

    public static final float KEYPAD_FONT_SIZE_DEFAULT_VALUE = 12.0f;
    public static final String CONFIG_KEYPAD_FONT_SIZE = "keypadFontSize";
    public static final String CONFIG_KEYPAD_ENABLED = "keypad";
    public static final String CONFIG_KEYPAD_MNEMONICS = "keypadMnemonics";
    public static final String YES = "Yes";
    public static final String NO = "No";

    private final SessionConfiguration sessionConfiguration = new SessionConfiguration();
    private final KeyMnemonicSerializer keyMnemonicSerializer = new KeyMnemonicSerializer();

    private final String sessionName;
    protected Map<String, String> sesProps = new ConcurrentHashMap<>();

    private final List<SessionConfigListener> sessionCfglisteners = new CopyOnWriteArrayList<>();
    private boolean modified;

    public AbstractSessionConfig(final String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public final void firePropertyChange(final Object source, final String propertyName, final Object oldValue, final Object newValue) {

        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }

        final SessionConfigEvent sce = new SessionConfigEvent(source, propertyName, oldValue, newValue);
        for (final SessionConfigListener target : this.sessionCfglisteners) {
            target.onConfigChanged(sce);
        }
    }

    /**
     * @return
     * @deprecated see {@link SessionConfiguration}
     */
    @Deprecated
    public Map<String, String> getProperties() {
        return sesProps;
    }

    public void setModified(final boolean v) {
        modified = v;
    }

    public boolean isModified() {
        return modified;
    }

    public abstract void saveSessionPropsOnClose();

    public abstract void saveSessionProps();

    protected abstract void loadConfigurationResource();

    protected void loadDefaults() throws IOException {
        final Properties colorSchemaDefaults = loadPropertiesFromResource("tn5250jSchemas.properties");
        final String prefix = colorSchemaDefaults.getProperty("schemaDefault");
        sesProps.put("colorRed", colorSchemaDefaults.getProperty(prefix + ".colorRed"));
        sesProps.put("colorTurq", colorSchemaDefaults.getProperty(prefix + ".colorTurq"));
        sesProps.put("colorCursor", colorSchemaDefaults.getProperty(prefix + ".colorCursor"));
        sesProps.put("colorGUIField", colorSchemaDefaults.getProperty(prefix + ".colorGUIField"));
        sesProps.put("colorWhite", colorSchemaDefaults.getProperty(prefix + ".colorWhite"));
        sesProps.put("colorYellow", colorSchemaDefaults.getProperty(prefix + ".colorYellow"));
        sesProps.put("colorGreen", colorSchemaDefaults.getProperty(prefix + ".colorGreen"));
        sesProps.put("colorPink", colorSchemaDefaults.getProperty(prefix + ".colorPink"));
        sesProps.put("colorBlue", colorSchemaDefaults.getProperty(prefix + ".colorBlue"));
        sesProps.put("colorSep", colorSchemaDefaults.getProperty(prefix + ".colorSep"));
        sesProps.put("colorHexAttr", colorSchemaDefaults.getProperty(prefix + ".colorHexAttr"));
        sesProps.put("font", GUIGraphicsUtils.getDefaultFont());
    }

    private Properties loadPropertiesFromResource(final String resourceName) throws IOException {
        final Properties properties = new Properties();
        final URL url = getClass().getClassLoader().getResource(resourceName);
        if (url != null) {
            final InputStream in = url.openStream();
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        }
        return properties;
    }

    public boolean isPropertyExists(final String prop) {
        return sesProps.containsKey(prop);
    }

    /**
     * @return
     * @deprecated see {@link SessionConfiguration}
     */
    @Deprecated
    public String getStringProperty(final String prop) {

        if (sesProps.containsKey(prop)) {
            return sesProps.get(prop);
        }
        return "";

    }

    /**
     * @return
     * @deprecated see {@link SessionConfiguration}
     */
    @Deprecated
    public int getIntegerProperty(final String prop) {

        if (sesProps.containsKey(prop)) {
            try {
                return Integer.parseInt(sesProps.get(prop));
            } catch (final NumberFormatException ne) {
                return 0;
            }
        }
        return 0;

    }

    /**
     * @return
     * @deprecated see {@link SessionConfiguration}
     */
    @Deprecated
    public Color getColorProperty(final String prop) {

        if (sesProps.containsKey(prop)) {
            return UiUtils.rgb(getIntegerProperty(prop));
        }
        return null;

    }

    public Rectangle2D getRectangleProperty(final String key) {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        if (sesProps.containsKey(key)) {
            final String rect = sesProps.get(key);
            final StringTokenizer stringtokenizer = new StringTokenizer(rect, ",");
            if (stringtokenizer.hasMoreTokens())
                x = Integer.parseInt(stringtokenizer.nextToken());
            if (stringtokenizer.hasMoreTokens())
                y = Integer.parseInt(stringtokenizer.nextToken());
            if (stringtokenizer.hasMoreTokens())
                width = Integer.parseInt(stringtokenizer.nextToken());
            if (stringtokenizer.hasMoreTokens())
                height = Integer.parseInt(stringtokenizer.nextToken());
        }

        return new Rectangle2D(x, y, width, height);
    }

    public void setRectangleProperty(final String key, final Rectangle2D rect) {

        final String rectStr = round(rect.getMinX()) + "," +
                round(rect.getMinY()) + "," +
                round(rect.getWidth()) + "," +
                round(rect.getHeight());
        sesProps.put(key, rectStr);
    }

    private int round(final double value) {
        return (int) Math.ceil(value);
    }

    /**
     * @return
     * @deprecated see {@link SessionConfiguration}
     */
    @Deprecated
    public float getFloatProperty(final String prop) {
        return getFloatProperty(prop, 0.0f);
    }

    /**
     * @return
     * @deprecated see {@link SessionConfiguration}
     */
    @Deprecated
    public float getFloatProperty(final String propertyName, final float defaultValue) {
        if (sesProps.containsKey(propertyName)) {
            return parseFloat(sesProps.get(propertyName));
        }
        return defaultValue;
    }

    /**
     * @param key property key.
     * @return property value.
     */
    public Object getProperty(final String key) {
        return sesProps.get(key);
    }

    /**
     * @return property keys.
     */
    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<>();
        for (final Object key : sesProps.keySet()) {
            keys.add((String) key);
        }
        return keys;
    }

    public Object setProperty(final String key, final String value) {
        return sesProps.put(key, value);
    }

    public Object removeProperty(final String key) {
        return sesProps.remove(key);
    }

    /**
     * Add a SessionConfigListener to the listener list.
     *
     * @param listener The SessionListener to be added
     */
    public final void addSessionConfigListener(final SessionConfigListener listener) {
        sessionCfglisteners.add(listener);
    }

    /**
     * Remove a SessionListener from the listener list.
     *
     * @param listener The SessionListener to be removed
     */
    public final void removeSessionConfigListener(final SessionConfigListener listener) {
        sessionCfglisteners.remove(listener);
    }

    public SessionConfiguration getConfig() {
        return sessionConfiguration;
    }

    public void setKeypadMnemonicsAndFireChangeEvent(final KeyMnemonic[] keyMnemonics) {
        final String newValue = keyMnemonicSerializer.serialize(keyMnemonics);
        firePropertyChange(this, CONFIG_KEYPAD_MNEMONICS, getStringProperty(CONFIG_KEYPAD_MNEMONICS), newValue);
        setProperty(CONFIG_KEYPAD_MNEMONICS, newValue);
    }

    /**
     * This is the new intended way to access configuration.
     * Only Getters are allowed here!
     * <p>
     * TODO: refactor all former usages which access properties directly
     */
    public class SessionConfiguration {
        public float getKeypadFontSize() {
            return getFloatProperty(CONFIG_KEYPAD_FONT_SIZE, KEYPAD_FONT_SIZE_DEFAULT_VALUE);
        }

        public boolean isKeypadEnabled() {
            return YES.equals(getStringProperty(CONFIG_KEYPAD_ENABLED));
        }

        public KeyMnemonic[] getKeypadMnemonics() {
            final String s = getStringProperty(CONFIG_KEYPAD_MNEMONICS);
            final KeyMnemonic[] result = keyMnemonicSerializer.deserialize(s);
            if (result.length == 0) {
                return getDefaultKeypadMnemonics();
            }
            return result;
        }

        public KeyMnemonic[] getDefaultKeypadMnemonics() {
            return new KeyMnemonic[]{
                    PF1, PF2, PF3, PF4, PF5, PF6, PF7, PF8, PF9, PF10, PF11, PF12, ENTER, PAGE_UP, CLEAR,
                    PF13, PF14, PF15, PF16, PF17, PF18, PF19, PF20, PF21, PF22, PF23, PF24, SYSREQ, PAGE_DOWN
            };
        }

    }
}
