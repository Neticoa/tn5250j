/**
 *
 */
package org.tn5250j.mailtools;

import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.tools.LangTool;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SendEMailDialogSwingDemo {
    public static void main(final String[] args) {
        LangTool.init();
        SwingToFxUtils.initFx();

//        final JDialog dialog = new SMTPConfigSwing(null, "Demo", false);
//        dialog.setVisible(true);
    }
}
