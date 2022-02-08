/**
 *
 */
package org.tn5250j.tools.filters;

import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DelimitedOutputFilterDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final DelimitedOutputFilter filter = new DelimitedOutputFilter();
        filter.setCustomProperties();
    }
}
