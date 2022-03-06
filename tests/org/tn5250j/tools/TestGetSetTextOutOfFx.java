/**
 *
 */
package org.tn5250j.tools;

import org.tn5250j.gui.SwingToFxUtils;

import javafx.scene.control.TextField;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TestGetSetTextOutOfFx {
    public static void main(final String[] args) {
        LangTool.init();
        SwingToFxUtils.initFx();

        final TextField hostFile = new TextField();
        hostFile.setText("str");
        System.out.println(hostFile.getText());
    }
}
