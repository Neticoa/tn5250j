/**
 * Title: KeyMapper
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.1
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
package org.tn5250j.keyboard;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.tn5250j.event.KeyChangeListener;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.tools.LangTool;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;

public class KeyMapper {

    private static HashMap<KeyStroker, String> mappedKeys;
    private static KeyStroker workStroke;
    private static String lastKeyMnemonic;
    private static Vector<KeyChangeListener> listeners;

    private static final Modifier[] POSSIBLE_MODIFIERS = {
        KeyCombination.SHIFT_DOWN, KeyCombination.SHIFT_ANY,
        KeyCombination.CONTROL_DOWN, KeyCombination.CONTROL_ANY,
        KeyCombination.ALT_DOWN, KeyCombination.ALT_ANY,
        KeyCombination.META_DOWN, KeyCombination.META_ANY,
        KeyCombination.SHORTCUT_DOWN, KeyCombination.SHORTCUT_ANY
    };

    public static void init() {

        if (mappedKeys != null)
            return;

        mappedKeys = new HashMap<KeyStroker, String>(60);
        workStroke = new KeyStroker(0, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD);

        final Properties keys = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.KEYMAP);

        if (!containsProperties(keys)) {
            // Key <-> Keycode , isShiftDown , isControlDown , isAlternateDown, location

            mappedKeys.put(new KeyStroker(10, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[enter]");
            mappedKeys.put(new KeyStroker(10, false, false, false, false, KeyStroker.KEY_LOCATION_NUMPAD), "[enter].alt2");

            mappedKeys.put(new KeyStroker(8, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[backspace]");
            mappedKeys.put(new KeyStroker(9, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[tab]");
            mappedKeys.put(new KeyStroker(9, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[backtab]");
            mappedKeys.put(new KeyStroker(127, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[delete]");
            mappedKeys.put(new KeyStroker(155, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[insert]");
            mappedKeys.put(new KeyStroker(19, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[clear]");

            mappedKeys.put(new KeyStroker(17, false, true, false, false, KeyStroker.KEY_LOCATION_LEFT), "[reset]");

            mappedKeys.put(new KeyStroker(27, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[sysreq]");

            mappedKeys.put(new KeyStroker(35, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[eof]");
            mappedKeys.put(new KeyStroker(36, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[home]");
            mappedKeys.put(new KeyStroker(39, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[right]");
            mappedKeys.put(new KeyStroker(39, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[nextword]");
            mappedKeys.put(new KeyStroker(37, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[left]");
            mappedKeys.put(new KeyStroker(37, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[prevword]");
            mappedKeys.put(new KeyStroker(38, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[up]");
            mappedKeys.put(new KeyStroker(40, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[down]");
            mappedKeys.put(new KeyStroker(34, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pgdown]");
            mappedKeys.put(new KeyStroker(33, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pgup]");

            mappedKeys.put(new KeyStroker(96, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad0]");
            mappedKeys.put(new KeyStroker(97, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad1]");
            mappedKeys.put(new KeyStroker(98, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad2]");
            mappedKeys.put(new KeyStroker(99, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad3]");
            mappedKeys.put(new KeyStroker(100, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad4]");
            mappedKeys.put(new KeyStroker(101, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad5]");
            mappedKeys.put(new KeyStroker(102, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad6]");
            mappedKeys.put(new KeyStroker(103, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad7]");
            mappedKeys.put(new KeyStroker(104, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad8]");
            mappedKeys.put(new KeyStroker(105, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad9]");

            mappedKeys.put(new KeyStroker(109, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[field-]");
            mappedKeys.put(new KeyStroker(107, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[field+]");
            mappedKeys.put(new KeyStroker(112, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf1]");
            mappedKeys.put(new KeyStroker(113, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf2]");
            mappedKeys.put(new KeyStroker(114, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf3]");
            mappedKeys.put(new KeyStroker(115, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf4]");
            mappedKeys.put(new KeyStroker(116, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf5]");
            mappedKeys.put(new KeyStroker(117, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf6]");
            mappedKeys.put(new KeyStroker(118, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf7]");
            mappedKeys.put(new KeyStroker(119, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf8]");
            mappedKeys.put(new KeyStroker(120, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf9]");
            mappedKeys.put(new KeyStroker(121, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf10]");
            mappedKeys.put(new KeyStroker(122, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf11]");
            mappedKeys.put(new KeyStroker(123, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf12]");
            mappedKeys.put(new KeyStroker(112, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf13]");
            mappedKeys.put(new KeyStroker(113, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf14]");
            mappedKeys.put(new KeyStroker(114, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf15]");
            mappedKeys.put(new KeyStroker(115, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf16]");
            mappedKeys.put(new KeyStroker(116, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf17]");
            mappedKeys.put(new KeyStroker(117, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf18]");
            mappedKeys.put(new KeyStroker(118, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf19]");
            mappedKeys.put(new KeyStroker(119, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf20]");
            mappedKeys.put(new KeyStroker(120, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf21]");
            mappedKeys.put(new KeyStroker(121, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf22]");
            mappedKeys.put(new KeyStroker(122, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf23]");
            mappedKeys.put(new KeyStroker(123, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf24]");
            mappedKeys.put(new KeyStroker(112, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[help]");

            mappedKeys.put(new KeyStroker(72, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[hostprint]");

            mappedKeys.put(new KeyStroker(67, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[copy]");

            mappedKeys.put(new KeyStroker(86, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[paste]");

            mappedKeys.put(new KeyStroker(39, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markright]");
            mappedKeys.put(new KeyStroker(37, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markleft]");
            mappedKeys.put(new KeyStroker(38, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markup]");
            mappedKeys.put(new KeyStroker(40, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markdown]");

            mappedKeys.put(new KeyStroker(155, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[dupfield]");
            mappedKeys.put(new KeyStroker(17, true, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[newline]");
            mappedKeys.put(new KeyStroker(34, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[jumpnext]");
            mappedKeys.put(new KeyStroker(33, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[jumpprev]");

            saveKeyMap();
        } else {

            setKeyMap(keys);

        }

    }

    private static boolean containsProperties(final Properties keystrokes) {

        if (keystrokes != null && keystrokes.size() > 0)
            return true;
        else
            return false;
    }

    private static void parseKeyStrokes(final Properties keystrokes) {

        String theStringList = "";
        String theKey = "";
        final Enumeration<?> ke = keystrokes.propertyNames();
        while (ke.hasMoreElements()) {
            theKey = (String) ke.nextElement();

            if (OptionAccessFactory.getInstance().isRestrictedOption(theKey)) {
                continue;
            }

            theStringList = keystrokes.getProperty(theKey);
            int kc = 0;
            boolean is = false;
            boolean ic = false;
            boolean ia = false;
            boolean iag = false;
            int location = KeyStroker.KEY_LOCATION_STANDARD;

            final StringTokenizer tokenizer = new StringTokenizer(theStringList, ",");

            // first is the keycode
            kc = Integer.parseInt(tokenizer.nextToken());
            // isShiftDown
            if (tokenizer.nextToken().equals("true"))
                is = true;
            else
                is = false;
            // isControlDown
            if (tokenizer.nextToken().equals("true"))
                ic = true;
            else
                ic = false;
            // isAltDown
            if (tokenizer.nextToken().equals("true"))
                ia = true;
            else
                ia = false;

            // isAltDown Gr
            if (tokenizer.hasMoreTokens()) {
                if (tokenizer.nextToken().equals("true"))
                    iag = true;
                else
                    iag = false;

                if (tokenizer.hasMoreTokens()) {
                    location = Integer.parseInt(tokenizer.nextToken());
                }
            }

            mappedKeys.put(new KeyStroker(kc, is, ic, ia, iag, location), theKey);

        }

    }

    protected static void setKeyMap(final Properties keystrokes) {

        parseKeyStrokes(keystrokes);

    }

    public final static boolean isEqualLast(final KeyEvent ke) {
        return workStroke.equals(ke);
    }

    public final static void saveKeyMap() {

        final Properties map = ConfigureFactory.getInstance().getProperties(ConfigureFactory.KEYMAP);

        map.clear();

        // save off the keystrokes in the keymap
        final Collection<String> v = mappedKeys.values();
        final Set<KeyStroker> o = mappedKeys.keySet();
        final Iterator<KeyStroker> k = o.iterator();
        final Iterator<String> i = v.iterator();
        while (k.hasNext()) {
            final KeyStroker ks = k.next();
            map.put(i.next(), ks.toString());
        }

        ConfigureFactory.getInstance().saveSettings(ConfigureFactory.KEYMAP,
                "------ Key Map key=keycode,isShiftDown,isControlDown,isAltDown,isAltGrDown,location --------");
    }

    public final static String getKeyStrokeText(final KeyEvent ke) {
        return getKeyStrokeText(ke, false);
    }

    public final static String getKeyStrokeText(final KeyEvent ke, final boolean isAltGr) {
        if (!workStroke.equals(ke, isAltGr)) {
            workStroke.setAttributes(ke, isAltGr);
            lastKeyMnemonic = mappedKeys.get(workStroke);
        }

        if (lastKeyMnemonic != null &&
                lastKeyMnemonic.endsWith(KeyStroker.altSuffix)) {

            lastKeyMnemonic = lastKeyMnemonic.substring(0,
                    lastKeyMnemonic.indexOf(KeyStroker.altSuffix));
        }

        return lastKeyMnemonic;

    }

    public final static String getKeyStrokeMnemonic(final KeyEvent ke) {
        return getKeyStrokeMnemonic(ke, false);
    }

    public final static String getKeyStrokeMnemonic(final KeyEvent ke, final boolean isAltGr) {

        workStroke.setAttributes(ke, isAltGr);
        String keyMnemonic = mappedKeys.get(workStroke);

        if (keyMnemonic != null &&
                keyMnemonic.endsWith(KeyStroker.altSuffix)) {

            keyMnemonic = keyMnemonic.substring(0,
                    keyMnemonic.indexOf(KeyStroker.altSuffix));
        }

        return keyMnemonic;

    }

    public final static int getKeyStrokeCode() {
        return workStroke.hashCode();
    }

    public final static String getKeyStrokeDesc(final String which) {

        final Collection<String> v = mappedKeys.values();
        final Set<KeyStroker> o = mappedKeys.keySet();
        final Iterator<KeyStroker> k = o.iterator();
        final Iterator<String> i = v.iterator();
        while (k.hasNext()) {
            final KeyStroker ks = k.next();
            final String keyVal = i.next();
            if (keyVal.equals(which))
                return ks.getKeyStrokeDesc();
        }

        return LangTool.getString("key.dead");
    }

    public final static KeyStroker getKeyStroker(final String which) {

        final Collection<String> v = mappedKeys.values();
        final Set<KeyStroker> o = mappedKeys.keySet();
        final Iterator<KeyStroker> k = o.iterator();
        final Iterator<String> i = v.iterator();
        while (k.hasNext()) {
            final KeyStroker ks = k.next();
            final String keyVal = i.next();
            if (keyVal.equals(which))
                return ks;
        }

        return null;
    }

    public final static boolean isKeyStrokeDefined(final String which) {

        final Collection<String> v = mappedKeys.values();
        final Set<KeyStroker> o = mappedKeys.keySet();
        final Iterator<KeyStroker> k = o.iterator();
        final Iterator<String> i = v.iterator();
        while (k.hasNext()) {
            k.next();
            final String keyVal = i.next();
            if (keyVal.equals(which))
                return true;
        }

        return false;
    }

    public final static boolean isKeyStrokeDefined(final KeyEvent ke) {
        return isKeyStrokeDefined(ke, false);
    }

    public final static boolean isKeyStrokeDefined(final KeyEvent ke, final boolean isAltGr) {

        workStroke.setAttributes(ke, isAltGr);
        return (null != mappedKeys.get(workStroke));

    }

    public final static KeyCodeCombination getKeyStroke(final String which) {
        for (final Map.Entry<KeyStroker, String> e : mappedKeys.entrySet()) {
            final KeyStroker ks = e.getKey();
            final String keyVal = e.getValue();
            if (keyVal.equals(which)) {
                final List<Modifier> modifiers = new LinkedList<>();

                if (!isModifier(ks.getKeyCode())) {
                    try {
                        return new KeyCodeCombination(ks.getKeyCode(), modifiers.toArray(new Modifier[modifiers.size()]));
                    } catch (final RuntimeException exc) {
                        System.out.println("Key code: " + ks.getKeyCode());
                        throw exc;
                    }
                } else {
                    System.err.println("Key code " + ks.getKeyCode() + " is a modifier and can't be registered");
                }
            }
        }

        return null;
    }

    private static boolean isModifier(final KeyCode keyCode) {
        final String name = keyCode.getName();
        for (final Modifier modifier: POSSIBLE_MODIFIERS) {
            if (modifier.toString().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public final static void removeKeyStroke(final String which) {
        for (final Map.Entry<KeyStroker, String> e : mappedKeys.entrySet()) {
            final KeyStroker ks = e.getKey();
            final String keyVal = e.getValue();
            if (keyVal.equals(which)) {
                mappedKeys.remove(ks);
                return;
            }
        }
    }

    public final static void setKeyStroke(final String which, final KeyEvent ke) {

        if (ke == null)
            return;

        // if we got here it was a dead key and we need to add it.
        mappedKeys.put(new KeyStroker(ke), which);
    }

    public final static void setKeyStroke(final String which, final KeyEvent ke, final boolean isAltGr) {

        if (ke == null)
            return;

        // if we got here it was a dead key and we need to add it.
        mappedKeys.put(new KeyStroker(ke, isAltGr), which);

    }

    public final static HashMap<KeyStroker, String> getKeyMap() {
        return mappedKeys;
    }

    /**
     * Add a KeyChangeListener to the listener list.
     *
     * @param listener  The KeyChangedListener to be added
     */
    public static synchronized void addKeyChangeListener(final KeyChangeListener listener) {

        if (listeners == null) {
            listeners = new java.util.Vector<KeyChangeListener>(3);
        }
        listeners.addElement(listener);

    }

    /**
     * Remove a Key Change Listener from the listener list.
     *
     * @param listener  The KeyChangeListener to be removed
     */
    public synchronized void removeKeyChangeListener(final KeyChangeListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);

    }

    /**
     * Notify all registered listeners of the Key Change Event.
     *
     */
    public static void fireKeyChangeEvent() {

        if (listeners != null) {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                final KeyChangeListener target =
                        listeners.elementAt(i);
                target.onKeyChanged();
            }
        }
    }

}
