/**
 *
 */
package org.tn5250j;

import java.awt.Font;

import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.tools.LangTool;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PrinterThreadSwingDemo {
    public static void main(final String[] args) {
        LangTool.init();
        SwingToFxUtils.initFx();

        final SessionGuiAdapterSwing gui = new SessionGuiAdapterSwing();
        final PrinterThreadSwing printer = new PrinterThreadSwing(
                gui.getScreen(), new Font("default", 15, Font.PLAIN), 10, 3, gui);
        printer.start();
    }
}
