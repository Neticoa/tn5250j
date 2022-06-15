/**
 *
 */
package org.tn5250j;

import static org.tn5250j.AbstractSessionConfig.CONFIG_KEYPAD_ENABLED;
import static org.tn5250j.AbstractSessionConfig.CONFIG_KEYPAD_FONT_SIZE;
import static org.tn5250j.AbstractSessionConfig.CONFIG_KEYPAD_MNEMONICS;
import static org.tn5250j.AbstractSessionConfig.YES;

import java.util.Set;

import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.keyboard.KeyMnemonicSerializer;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class SessionGuiFactory {
    public static SessionPanel createGui(final Session5250 session) {
        final MutableUiConfiguration uiCfg = createMutableConfiguration(session.getConfiguration());

        final SessionPanel ui = new SessionPanel(session, uiCfg);
        session.getConfiguration().addSessionConfigListener(e -> handleConfigChanged(ui, e, uiCfg));

        return ui;
    }

    private static void handleConfigChanged(final SessionPanel ui,
            final SessionConfigEvent e, final MutableUiConfiguration oldValue) {
        final MutableUiConfiguration value = oldValue.clone();
        handleConfigChanged(value, e);

        if (!oldValue.equals(value)) {
            ui.updateUiConfiguration(value);
        }
    }
    private static final String CONFIG_MOUSE_WHEEL = "mouseWheel";
    private static final String CONFIG_DOUBLE_CLICK = "doubleClick";

    public static MutableUiConfiguration createMutableConfiguration(final AbstractSessionConfig sessionConfig) {
        final MutableUiConfiguration cfg = new MutableUiConfiguration();

        final Set<String> keys = sessionConfig.getPropertyKeys();
        for (final String key : keys) {
            final Object value = sessionConfig.getProperty(key);
            handleConfigChanged(cfg, key, value);
        }

        return cfg;
    }
    /**
     * @param cfg UI configuration.
     * @param e session configuration changed event.
     */
    public static void handleConfigChanged(final MutableUiConfiguration cfg, final SessionConfigEvent e) {
        final String configName = e.getPropertyName();
        final Object configValue = e.getNewValue();

        handleConfigChanged(cfg, configName, configValue);
    }
    /**
     * @param cfg UI configuration.
     * @param name property name.
     * @param value property value.
     */
    private static void handleConfigChanged(final MutableUiConfiguration cfg, final String name, final Object value) {
        if (CONFIG_KEYPAD_ENABLED.equals(name)) {
            cfg.setKeyPanelVisible(YES.equals(value));
        } else if (CONFIG_KEYPAD_MNEMONICS.equals(name)) {
            cfg.setKeyMnemonics(new KeyMnemonicSerializer().deserialize((String) value));
        } else if (CONFIG_KEYPAD_FONT_SIZE.equals(name)) {
            cfg.setFontSize(Float.parseFloat((String) value));
        } else if (CONFIG_DOUBLE_CLICK.equals(name)) {
            cfg.setDoubleClick(YES.equals(value));
        } else if (CONFIG_MOUSE_WHEEL.equals(name)) {
            cfg.setScrollable(YES.equals(value));
        }
    }
}
