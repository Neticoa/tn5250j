/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
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
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
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
     * @return input or null if the operation was canceled
     */
    public String show() {
        final ButtonType result = dialog.showAndWait().orElse(null);
        if (result == ButtonType.OK) {
            return text.getText();
        }
        return null;
    }
}
