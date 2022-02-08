/**
 *
 */
package org.tn5250j.tools;

import org.tn5250j.SessionGuiAdapter;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class XTFRFileDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();

        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SessionGuiAdapter gui = new SessionGuiAdapter();
        final XTFRFile dialog = new XTFRFile(gui.getVT(), gui);
        dialog.setVisible(true);
    }
}
