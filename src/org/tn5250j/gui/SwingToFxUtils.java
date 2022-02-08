/**
 *
 */
package org.tn5250j.gui;

import static org.tn5250j.gui.UiUtils.toRgb;

import javax.swing.JFrame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SwingToFxUtils {
    public static final JFrame SHARED_FRAME = new JFrame();
    private static JFXPanel PANEL;

    static {
        //init JavaFX
        initFx();
    }

    public static synchronized void initFx() {
        if (PANEL != null) {
            return;
        }

        PANEL = new JFXPanel();
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        Platform.setImplicitExit(false);
    }

    public static JFXPanel createSwingPanel(final Parent node) {
        final JFXPanel fxPane = new JFXPanel();
        final Scene scene = new Scene(node);
        fxPane.setScene(scene);
        return fxPane;
    }

    public static java.awt.Color toAwtColor(final Color colorBg) {
        return new java.awt.Color(toRgb(colorBg));
    }

    public static java.awt.Font toAwtFont(final Font font) {
        return new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int) Math.round(font.getSize()));
    }

    public static Font fromAwtFont(final java.awt.Font font) {
        return new Font(font.getName(), font.getSize());
    }
}
