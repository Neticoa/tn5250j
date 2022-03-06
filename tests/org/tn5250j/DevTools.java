/**
 *
 */
package org.tn5250j;

import java.util.Properties;

import org.tn5250j.gui.ControllerWithView;
import org.tn5250j.gui.UiUtils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DevTools {
    private static final String LOCALHOST = "127.0.0.1";

    public static Session5250 createSession() {
        final SessionConfig config = createSessionConfig();
        config.setProperty("font", "Lucida Sans Typewriter Regular"); // example config

        return new Session5250(new Properties(), LOCALHOST, LOCALHOST, config);
    }

    public static SessionConfig createSessionConfig() {
        return new SessionConfig(LOCALHOST, LOCALHOST);
    }

    public static ButtonType showInDialog(final ControllerWithView controller, final String template) {
        final Dialog<ButtonType> dialog = createDialog(controller, template);
        dialog.setTitle("Demo");
        return dialog.showAndWait().orElse(null);
    }

    public static Dialog<ButtonType> createDialog(final Object controller,
            final String template) {
        final Parent parent = UiUtils.loadTempalte(controller, template);

        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(parent);
        return dialog;
    }

    public static Stage createClosableFxFrame(final String title, final Parent node) {
        final Stage frame = new Stage();
        frame.setTitle(title);
        frame.setScene(new Scene(node));
        return frame;
    }
}
