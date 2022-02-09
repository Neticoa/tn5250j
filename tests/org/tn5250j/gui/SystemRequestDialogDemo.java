/**
 *
 */
package org.tn5250j.gui;

import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemRequestDialogDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();

        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SystemRequestDialog dialog = new SystemRequestDialog();
        System.out.println("Result: " + dialog.show());
    }
}
