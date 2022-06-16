/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 *
 * @author: duncanc
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

import org.tn5250j.tools.LangTool;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Small dialog asking the user to confirm the close tab request
 *
 * @author duncanc
 */
public class ConfirmTabCloseDialog {

    private static final String CANCEL = LangTool.getString("ss.optCancel");
    private static final String CLOSE = LangTool.getString("key.labelClose");

    private Alert dialog;

    public ConfirmTabCloseDialog() {
        super();

        dialog = new Alert(AlertType.CONFIRMATION, "Are you sure you want to close this tab?",
                ButtonType.OK, ButtonType.CANCEL);
        dialog.setHeaderText("");
        dialog.setTitle(LangTool.getString("sa.confirmTabClose"));

        UiUtils.changeButtonText(dialog.getDialogPane(), ButtonType.OK, CLOSE);
        UiUtils.changeButtonText(dialog.getDialogPane(), ButtonType.CANCEL, CANCEL);
    }

    /**
     * Shows the dialog and returns the true if the close was confirmed
     * or false if the operation was canceled.
     *
     * @return true if OK button pressed, false otherwise.
     */
    public boolean show() {
        return dialog.showAndWait().orElse(null) == ButtonType.OK;
    }
}
