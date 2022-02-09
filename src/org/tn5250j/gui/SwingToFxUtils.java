/**
 *
 */
package org.tn5250j.gui;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.text.Font;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SwingToFxUtils {
    public static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    static {
        //init JavaFX
        initFx();
    }

    public static synchronized void initFx() {
        if (INITIALIZED.get()) {
            return;
        }
        //this line initializes JavaFX
        new javafx.embed.swing.JFXPanel();

        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        Platform.setImplicitExit(false);
        INITIALIZED.set(true);
    }

    public static java.awt.Font toAwtFont(final Font font) {
        return new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int) Math.round(font.getSize()));
    }

    public static Font fromAwtFont(final java.awt.Font font) {
        return new Font(font.getName(), font.getSize());
    }
}
