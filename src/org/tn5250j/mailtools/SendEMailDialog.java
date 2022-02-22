/**
 * Title: SendEMailDialog.java
 * <p>
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
 * u
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.mailtools;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.tn5250j.SessionConfig;
import org.tn5250j.SessionGui;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.tn5250.Screen5250Facade;
import org.tn5250j.gui.ActionDelegateDialogPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.AsyncServices;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.encoder.EncodeComponent;

import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Send E-Mail dialog
 */
public class SendEMailDialog {

    ComboBox<String> toAddress;
    TextField subject;
    TextArea bodyText;
    TextField attachmentName;
    SessionConfig config;
    SessionGui session;
    String fileName;
    RadioButton text;
    RadioButton graphic;
    RadioButton normal;
    RadioButton screenshot;
    Button browse;
    boolean sendScreen;
    SendEMail sendEMail;
    private final Window parent;

    /**
     * Constructor to send the screen information
     * @param session
     * @param sendScreen
     */
    public SendEMailDialog(final SessionGui session) {
        this(session, true);
    }

    /**
     * Constructor to send the screen information
     * @param session
     */
    public SendEMailDialog(final SessionGui session, final boolean sendScreen) {
        super();

        this.parent = session.getWindow();
        if (!isEMailAvailable()) {
            UiUtils.showError(LangTool.getString("messages.noEmailAPI"), "Error");
        } else {

            this.session = session;
            this.sendScreen = sendScreen;

            final GridPane content = setupMailPanel("tn5250j.txt");

            ButtonType result = ButtonType.NEXT;
            while (result == ButtonType.NEXT) {
                result = showDialog(content, LangTool.getString("em.title"), this::sendEmail1);
                if (result == ButtonType.NEXT) {
                    configureSMTP(parent);
                }
            }
        }
    }

    private void sendEmail1(final ActionDelegateDialogPane<ButtonType> dialog) {
        final Screen5250Facade screen = session.getScreen();

        sendEMail = new SendEMail();
        sendEMail.setConfigFile("SMTPProperties.cfg");
        sendEMail.setTo(toAddress.getValue());
        sendEMail.setSubject(subject.getText());
        if (bodyText.getText().length() > 0)
            sendEMail.setMessage(bodyText.getText());

        if (attachmentName.getText().length() > 0)
            if (!normal.isSelected())
                sendEMail.setAttachmentName(attachmentName.getText());
            else
                sendEMail.setAttachmentName(fileName);

        if (text.isSelected()) {
            sendEMail.setAttachment(getScreenTextContent(screen));
        } else if (graphic.isSelected()) {

            final File dir = new File(System.getProperty("user.dir"));

            //  setup the temp file name
            final String tempFile = "tn5250jTemp";

            try {
                // create the temporary file
                final File f = File.createTempFile(tempFile, ".png", dir);
                // set it to delete on exit
                f.deleteOnExit();

                EncodeComponent.encode(
                        EncodeComponent.PNG,
                        session,
                        f);
                sendEMail.setFileName(f.getName());
            } catch (final Exception ex) {
                System.out.println(ex.getMessage());
            }

        } else if (attachmentName.getText().length() > 0) {
            final File f = new File(attachmentName.getText());
            sendEMail.setFileName(f.toString());
        }

        // send the information
        launchSendService(dialog);
    }
    private void sendEmail2(final ActionDelegateDialogPane<ButtonType> dialog) {
        sendEMail = new SendEMail();

        sendEMail.setConfigFile("SMTPProperties.cfg");
        sendEMail.setTo(toAddress.getValue());
        sendEMail.setSubject(subject.getText());
        if (bodyText.getText().length() > 0)
            sendEMail.setMessage(bodyText.getText());

        if (attachmentName.getText().length() > 0)
            sendEMail.setAttachmentName(attachmentName.getText());

        if (fileName != null && fileName.length() > 0)
            sendEMail.setFileName(fileName);

        // send the information
        launchSendService(dialog);
    }

    /**
     * @param dialog
     */
    private void launchSendService(final ActionDelegateDialogPane<ButtonType> dialog) {
        AsyncServices.startTask(new Task<Void>() {
            @Override
            protected final Void call() throws Exception {
                runSendEmail();
                return null;
            }
            @Override
            protected void scheduled() {
                dialog.lookupButton(ButtonType.OK).setDisable(true);
            }
            @Override
            protected void succeeded() {
                try {
                    afterSendEmail();
                } finally {
                    dialog.finish(ButtonType.OK);
                }
            }
            @Override
            protected void failed() {
                dialog.finish(ButtonType.CANCEL);
            }
        });
    }

    private static String getScreenTextContent(final Screen5250Facade screen) {

        char[] screenTxt;
        char[] screenExtendedAttr;
        char[] screenAttrPlace;

        final int len = screen.getScreenLength();
        screenTxt = new char[len];
        screenExtendedAttr = new char[len];
        screenAttrPlace = new char[len];
        screen.GetScreen(screenTxt, len, TN5250jConstants.PLANE_TEXT);
        screen.GetScreen(screenExtendedAttr, len, TN5250jConstants.PLANE_EXTENDED);
        screen.GetScreen(screenAttrPlace, len, TN5250jConstants.PLANE_IS_ATTR_PLACE);

        final StringBuffer sb = new StringBuffer();
//      char[] s = screen.getScreenAsChars();
        final int c = screen.getColumns();
        final int l = screen.getRows() * c;

        int col = 0;
        for (int x = 0; x < l; x++, col++) {

            // only draw printable characters (in this case >= ' ')
            if (screenTxt[x] >= ' ' && ((screenExtendedAttr[x] & TN5250jConstants.EXTENDED_5250_NON_DSP) == 0)) {

                if (
                        (screenExtendedAttr[x] & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0 &&
                                screenAttrPlace[x] != 1) {
                    sb.append('_');
                } else {
                    sb.append(screenTxt[x]);

                }

            } else {

                if (
                        (screenExtendedAttr[x] & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0 &&
                                screenAttrPlace[x] != 1) {
                    sb.append('_');
                } else {
                    sb.append(' ');
                }
            }

            if (col == c) {
                sb.append('\n');
                col = 0;
            }
        }
        return sb.toString();
    }

    private static ButtonType showDialog(final GridPane content, final String title,
            final Consumer<ActionDelegateDialogPane<ButtonType>> okButtonHandler) {
        final Dialog<ButtonType> dialog = new Dialog<>();

        final ActionDelegateDialogPane<ButtonType> dialogPane = new ActionDelegateDialogPane<>(dialog, okButtonHandler);
        dialog.setDialogPane(dialogPane);
        ((Stage) dialogPane.getScene().getWindow()).getIcons().addAll(GUIGraphicsUtils.getApplicationIcons());

        // setup the dialog options
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.NEXT);

        UiUtils.changeButtonText(dialogPane, ButtonType.OK, LangTool.getString("em.optSendLabel"));
        UiUtils.changeButtonText(dialogPane, ButtonType.CANCEL, LangTool.getString("em.optCancelLabel"));

        final File smtp = new File("SMTPProperties.cfg");

        if (smtp.exists()) {
            UiUtils.changeButtonText(dialogPane, ButtonType.NEXT, LangTool.getString("em.optEditLabel"));
        } else {
            UiUtils.changeButtonText(dialogPane, ButtonType.NEXT, LangTool.getString("em.optConfigureLabel"));
        }

        dialog.getDialogPane().setContent(content);
        dialog.setTitle(title);

        final ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        //dialog.close();
        return result;
    }

    /**
     * Constructor to send a file
     * @param session
     */
    public SendEMailDialog(final SessionGui session, final String fileName) {
        this.parent = session.getWindow();

        if (!isEMailAvailable()) {
            UiUtils.showError(LangTool.getString("messages.noEmailAPI"), "Error");
        } else {

            this.session = session;

            final GridPane content = setupMailPanel(fileName);

            ButtonType result = ButtonType.NEXT;
            while (result == ButtonType.NEXT) {
                result = showDialog(content, LangTool.getString("em.titleFileTransfer"), this::sendEmail2);

                if (result == ButtonType.NEXT) {
                    configureSMTP(parent);
                }
            }
        }
    }

    public void setSendEMail(final SendEMail sem) {
        sendEMail = sem;
    }

    private void runSendEmail() {

//		if (parent == null)
//			parent = new JFrame();

        try {
            if (sendEMail.send()) {
                sendEMail.release();
                sendEMail = null;
            }

//		} catch (IOException ioe) {
//			System.out.println(ioe.getMessage());
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void afterSendEmail() {
        UiUtils.showInfo(LangTool.getString("em.confirmationMessage")
                + " " + toAddress.getValue(),
                LangTool.getString("em.titleConfirmation"));

        if (session != null) {
            config.setProperty(
                    "emailTo",
                    getToTokens(
                            config.getStringProperty("emailTo"),
                            toAddress));
            config.saveSessionProps();
            setToCombo(config.getStringProperty("emailTo"), toAddress);
        }
    }

    /**
     * Configure the SMTP server information
     *
     * @param parent
     */
    private void configureSMTP(final Window parent) {
        new SMTPConfig(parent, null, true).show();
    }

    /**
     * Create the main e-mail panel for display
     *
     * @param fileName
     * @return
     */
    private GridPane setupMailPanel(final String fileName) {
        final GridPane semp = new GridPane();
        semp.getStylesheets().add("/application.css");

        semp.getStyleClass().add("etched-border");

        text = new RadioButton(LangTool.getString("em.text"));
        graphic = new RadioButton(LangTool.getString("em.graphic"));
        normal = new RadioButton(LangTool.getString("em.normalmail"));
        screenshot = new RadioButton(LangTool.getString("em.screenshot"));

        // Group the radio buttons.
        final ToggleGroup tGroup = new ToggleGroup();
        tGroup.getToggles().add(text);
        tGroup.getToggles().add(graphic);

        final ToggleGroup mGroup = new ToggleGroup();
        mGroup.getToggles().add(normal);
        mGroup.getToggles().add(screenshot);

        text.setSelected(false);
        text.setDisable(true);
        graphic.setDisable(true);
        normal.setSelected(true);

        final Label screenDump = new Label(LangTool.getString("em.screendump"));
        final Label tol = new Label(LangTool.getString("em.to"));
        final Label subl = new Label(LangTool.getString("em.subject"));
        final Label bodyl = new Label(LangTool.getString("em.body"));
        final Label fnl = new Label(LangTool.getString("em.fileName"));
        final Label tom = new Label(LangTool.getString("em.typeofmail"));

        browse = new Button(LangTool.getString("em.choosefile"));
        browse.setOnAction(e -> browse_actionPerformed());

        toAddress = new ComboBox<String>();
        toAddress.setMaxWidth(Double.POSITIVE_INFINITY);
        toAddress.setEditable(true);

        subject = new TextField();
        subject.setPrefColumnCount(30);

        bodyText = new TextArea();
        bodyText.setPrefRowCount(6);
        bodyText.setPrefColumnCount(30);

        attachmentName = new TextField(fileName);
        attachmentName.setPrefColumnCount(30);

        if (fileName != null && fileName.length() > 0)
            attachmentName.setText(fileName);
        else
            attachmentName.setText("");

        text.selectedProperty().addListener((src, old, value) -> setAttachmentName());
        normal.selectedProperty().addListener((src, old, value) -> setTypeOfMail());

        if (sendScreen) {
            screenshot.setSelected(true);
        } else {
            normal.setSelected(true);
        }

        config = null;

        if (session != null) {
            config = session.getSession().getConfiguration();

            if (config.isPropertyExists("emailTo")) {
                setToCombo(config.getStringProperty("emailTo"), toAddress);
            }
        }

        //mail type row
        semp.getChildren().add(addSimpleConstraints(tom, 0, 0, insets(5, 10, 5, 5)));
        semp.getChildren().add(addSimpleConstraints(normal, 0, 1, insets(5, 15, 5, 5)));
        semp.getChildren().add(addSimpleConstraints(screenshot, 0, 2, insets(5, 45, 5, 10)));

        //screen dump row
        semp.getChildren().add(addSimpleConstraints(screenDump, 1, 0, insets(0, 10, 5, 5)));
        semp.getChildren().add(addSimpleConstraints(text, 1, 1, insets(0, 15, 5, 5)));
        semp.getChildren().add(addSimpleConstraints(graphic, 1, 2, insets(0, 45, 5, 10)));

        //to row
        semp.getChildren().add(addSimpleConstraints(tol, 2, 0, insets(5, 10, 5, 5)));
        semp.getChildren().add(addTwoColumnsConstraints(toAddress, 2, 1, insets(5, 5, 5, 10)));

        //subject row
        semp.getChildren().add(addSimpleConstraints(subl, 3, 0, insets(5, 10, 5, 5)));
        semp.getChildren().add(addTwoColumnsConstraints(subject, 3, 1, insets(5, 5, 5, 10)));

        //message row
        semp.getChildren().add(addSimpleConstraints(bodyl, 4, 0, insets(5, 10, 5, 5)));

        addTwoColumnsConstraints(bodyText, 4, 1, insets(5, 5, 5, 10));
        GridPane.setRowSpan(bodyText, 3);
        semp.getChildren().add(bodyText);

        semp.getChildren().add(addSimpleConstraints(fnl, 7, 0, insets(5, 10, 5, 5)));
        semp.getChildren().add(addTwoColumnsConstraints(attachmentName, 7, 1, insets(5, 5, 5, 10)));

        semp.getChildren().add(addTwoColumnsConstraints(browse, 8, 1, insets(5, 10, 5, 5)));

        return semp;

    }

    private static Node addTwoColumnsConstraints(final Node node, final int row, final int column, final Insets insets) {
        addSimpleConstraints(node, row, column, insets);
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

    private void browse_actionPerformed() {
        final FileChooser pcFileChooser = new FileChooser();
        pcFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        final File file = pcFileChooser.showOpenDialog(parent);

        // check to see if something was actually chosen
        if (file != null) {
            fileName = file.getName();
            attachmentName.setText(file.toString());
        }
    }

    private void setAttachmentName() {

        if (text.isSelected()) {
            attachmentName.setText("tn5250j.txt");

        } else if (normal.isSelected()) {
            attachmentName.setText("tn5250j.png");
        } else {
            attachmentName.setText("tn5250j.png");
        }
    }

    private void setTypeOfMail() {

        if (normal.isSelected()) {
            text.setDisable(true);
            graphic.setDisable(true);
            attachmentName.setText("");
            browse.setDisable(false);
        } else {
            text.setDisable(false);
            graphic.setDisable(false);
            text.setSelected(true);
            setAttachmentName();
            browse.setDisable(true);
        }
    }

    /**
     * Set the combo box items to the string token from to.
     * The separator is a '|' character.
     *
     * @param to
     * @param boxen
     */
    private void setToCombo(final String to, final ComboBox<String> boxen) {
        final String[] tos = to.split(Pattern.quote("|"));

        boxen.getItems().clear();
        boxen.getItems().addAll(tos);
    }

    /**
     * Creates string of tokens from the combobox items.
     * The separator is a '|' character.  It does not save duplicate items.
     *
     * @param to
     * @param boxen
     * @return
     */
    private String getToTokens(final String to, final ComboBox<String> boxen) {

        final List<String> allSelected = new LinkedList<>(boxen.getItems());
        final String selected = boxen.getValue();
        //move selected to front
        if (selected != null) {
            allSelected.remove(selected);
            allSelected.add(0, selected);
        }

        return String.join("|", allSelected);
    }

    /**
     * Checks to make sure that the e-mail api's are available
     *
     * @return whether or not the e-mail api's are available or not.
     */
    private boolean isEMailAvailable() {

        try {
            Class.forName("javax.mail.Message");
            return true;
        } catch (final Exception ex) {
            System.out.println(" not there " + ex.getMessage());
            return false;
        }

    }
}
