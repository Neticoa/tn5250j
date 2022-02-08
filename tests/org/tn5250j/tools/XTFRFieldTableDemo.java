/**
 *
 */
package org.tn5250j.tools;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class XTFRFieldTableDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();

        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {

        final XTFRFieldTable table = new XTFRFieldTable(null) {
            @Override
            protected void itemChanged(final int index, final boolean value) {
                System.out.println("Item " + index + " changed to " + value);
            }
        };
        table.addItem("first");
        table.addItem("второй");
        table.addItem("третий");

        final BorderPane parent = new BorderPane(table);
        final Scene scene = new Scene(parent);
        stage.setScene(scene);

        stage.show();
    }
}
