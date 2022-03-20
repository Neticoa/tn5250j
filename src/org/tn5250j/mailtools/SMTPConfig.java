/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j.mailtools;

import java.util.Map;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class SMTPConfig extends DialogPane {
    Label labelHost = new Label();
    TextField fieldHost = new TextField();
    Label labelPort = new Label();
    TextField fieldPort = new TextField();
    Label labelDefault = new Label();
    Label labelName = new Label();
    TextField fieldName = new TextField();
    Label labelFrom = new Label();
    TextField fieldFrom = new TextField();
    Label labelFileName = new Label();
    TextField fieldFileName = new TextField();
    Map<String, String> SMTPProperties;

    //   String fileName;
    private final String title;
    private final boolean modal;

    private static final String smtpFileName = "SMTPProperties.cfg";

    public SMTPConfig(final String title, final boolean modal) {
        this.title = title;
        this.modal = modal;

        getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button optDone = (Button) lookupButton(ButtonType.OK);
        final Button optCancel = (Button) lookupButton(ButtonType.CANCEL);

        labelHost.setText(LangTool.getString("em.labelHost"));
        fieldHost.setPrefColumnCount(20);
        labelPort.setText(LangTool.getString("em.labelPort"));
        fieldPort.setPrefColumnCount(3);;
        labelDefault.setText(LangTool.getString("em.labelDefault"));
        labelName.setText(LangTool.getString("em.labelName"));
        fieldName.setPrefColumnCount(20);;
        labelFrom.setText(LangTool.getString("em.labelFrom"));
        fieldFrom.setPrefColumnCount(20);

        optDone.setText(LangTool.getString("em.optDone"));
        optDone.setOnAction(e -> handleDone());

        optCancel.setText(LangTool.getString("em.optCancelLabel"));

        labelFileName.setText(LangTool.getString("em.labelFileName"));
        fieldFileName.setText("tn5250j.txt");
        fieldFileName.setPrefColumnCount(20);

        final GridPane configPanel = new GridPane();
        setContent(configPanel);

        configPanel.getChildren().add(addSimpleConstraints(labelHost, 0, 0, insets(10, 10, 5, 5)));
        configPanel.getChildren().add(addTextFieldConstraints(fieldHost, 0, 1, insets(10, 5, 5, 10)));

        configPanel.getChildren().add(addSimpleConstraints(labelPort, 1, 0, insets(15, 10, 5, 5)));
        configPanel.getChildren().add(addSimpleConstraints(fieldPort, 1, 1, insets(5, 5, 5, 5)));
        configPanel.getChildren().add(addSimpleConstraints(labelDefault, 1, 2, insets(5, 15, 5, 10)));

        configPanel.getChildren().add(addSimpleConstraints(labelName, 2, 0, insets(5, 10, 5, 5)));
        configPanel.getChildren().add(addTextFieldConstraints(fieldName, 2, 1, insets(5, 5, 5, 10)));

        configPanel.getChildren().add(addSimpleConstraints(labelFrom, 3, 0, insets(5, 10, 5, 5)));
        configPanel.getChildren().add(addTextFieldConstraints(fieldFrom, 3, 1, insets(5, 5, 5, 10)));

        configPanel.getChildren().add(addSimpleConstraints(labelFileName, 4, 0, insets(5, 10, 5, 5)));
        configPanel.getChildren().add(addTextFieldConstraints(fieldFileName, 4, 1, insets(5, 5, 0, 10)));

        try {
            if (loadConfig(null)) {
                setProperties();
            }
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
    }

    private static Node addTextFieldConstraints(final TextField node, final int row, final int column, final Insets insets) {
        addSimpleConstraints(node, row, column, insets);
        node.setMaxHeight(Double.POSITIVE_INFINITY);
        GridPane.setColumnSpan(node, 2);
        GridPane.setHgrow(node, Priority.ALWAYS);
        return node;
    }

    private static Node addSimpleConstraints(final Node node, final int row, final int column, final Insets insets) {
        GridPane.setRowIndex(node, row);
        GridPane.setColumnIndex(node, column);
        GridPane.setMargin(node, insets);
        GridPane.setHalignment(node, HPos.LEFT);
        return node;
    }

    private static Insets insets(final double top, final double left, final double bottom, final double right) {
        return new Insets(top, right, bottom, left);
    }

    public SMTPConfig() {
        this("", false);
    }

    public void show() {
        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(this);

        if (title != null) {
            dialog.setTitle(title);
        } else {
            dialog.setTitle(LangTool.getString("em.configTitle"));
        }

        if (modal) {
            dialog.showAndWait();
        } else {
            dialog.show();
        }
    }

    private void setProperties() {

        //   mail.smtp.host=            Fill in the host name or ip address of your SMTP
        //                              mail server.
        //
        //   mail.smtp.port=            Fill in the port to use to connect
        //
        //   mail.smtp.from=            This is the e-mail address from.  For example I would
        //                              place kjpou@hotmail.com here as follows:
        //
        //                              mail.smtp.from=kjpou@hotmail.com

        fieldHost.setText(SMTPProperties.get("mail.smtp.host"));
        fieldPort.setText(SMTPProperties.get("mail.smtp.port"));
        fieldFrom.setText(SMTPProperties.get("mail.smtp.from"));
        fieldName.setText(SMTPProperties.get("mail.smtp.realname"));

        // file name
        fieldFileName.setText(SMTPProperties.get("fileName"));

    }

    /**
     * <p>Loads the given configuration file.
     *
     * @param name Configuration file name
     * @return true if the configuration file was loaded
     */
    private boolean loadConfig(final String name) throws Exception {

        SMTPProperties =
                ConfigureFactory.getInstance().getProperties("smtp", smtpFileName);

        if (SMTPProperties.size() > 0)
            return true;
        else
            return false;
    }

    private void handleDone() {

        SMTPProperties.put("mail.smtp.host", fieldHost.getText());
        SMTPProperties.put("mail.smtp.port", fieldPort.getText());
        SMTPProperties.put("mail.smtp.from", fieldFrom.getText());
        SMTPProperties.put("mail.smtp.realname", fieldName.getText());

        // file name
        SMTPProperties.put("fileName", fieldFileName.getText());

        ConfigureFactory.getInstance().saveSettings(
                "smtp",
                smtpFileName,
                "------ SMTP Defaults --------");
    }
}
