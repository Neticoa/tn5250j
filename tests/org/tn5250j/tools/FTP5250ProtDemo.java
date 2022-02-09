/**
 *
 */
package org.tn5250j.tools;

import java.util.LinkedList;
import java.util.List;

import org.tn5250j.gui.SwingToFxUtils;

import javafx.application.Platform;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FTP5250ProtDemo  extends FTP5250Prot {
    private FTP5250ProtDemo() {
        super(null);
    }

    public static void main(final String[] args) {
        LangTool.init();
        SwingToFxUtils.initFx();

        final List<String> messages = new LinkedList<>();
        messages.add("Message-1");
        messages.add("Message-2");
        messages.add("Message-3");
        messages.add("Message-4");

        Platform.runLater(() -> showNullFieldsWarning(messages));
    }
}
