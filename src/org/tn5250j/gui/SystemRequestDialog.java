/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 *
 * @author: master_jaf
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Small dialog asking the user to enter a value for doing a system request.
 *
 * @author master_jaf
 */
public class SystemRequestDialog {
    private static final String CANCEL = "Cancel";
    private static final String SYS_REQ = "SysReq";

    private Alert dialog;
    private TextField text;

    public SystemRequestDialog() {
        super();

        final BorderPane srp = new BorderPane();

        final Label jl = new Label("Enter alternate job");
        jl.setStyle("-fx-padding: 0 0 0.5em 0;");
        text = new TextField();
        srp.setTop(jl);
        srp.setCenter(text);

        dialog = new Alert(AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL);
        dialog.setTitle("System Request");
        dialog.setHeaderText("");

        UiUtils.changeButtonText(dialog.getDialogPane(), ButtonType.OK, SYS_REQ);
        UiUtils.changeButtonText(dialog.getDialogPane(), ButtonType.CANCEL, CANCEL);

        dialog.getDialogPane().setContent(srp);

        // add the listener that will set the focus to the desired option
        dialog.setOnShown(e -> text.requestFocus());
    }

    /**
     * Shows the dialog and returns the given input
     * or null if the operation was canceled.
     *
     * @return
     */
    public String show() {
        final ButtonType result = dialog.showAndWait().orElse(null);
        if (result == ButtonType.OK) {
            return text.getText();
        }
        return null;
    }
}
