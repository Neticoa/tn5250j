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
package org.tn5250j.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

import org.tn5250j.SessionConfig;
import org.tn5250j.SessionGui;
import org.tn5250j.event.FTPStatusEvent;
import org.tn5250j.event.FTPStatusListener;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.ActionDelegateDialogPane;
import org.tn5250j.gui.FxProxyBuilder;
import org.tn5250j.gui.GenericTn5250Frame;
import org.tn5250j.gui.NotClosableDialogPane;
import org.tn5250j.gui.TN5250jFileFilterBuilder;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.sql.AS400Xtfr;
import org.tn5250j.sql.SqlWizard;
import org.tn5250j.tools.filters.XTFRFileFilterBuilder;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class XTFRFile extends GenericTn5250Frame {

    private FTP5250Prot ftpProtocol;
    private AS400Xtfr axtfr;

    private TextField user;
    private PasswordField password;
    private TextField systemName;
    private TextField hostFile;
    private TextField localFile;
    private RadioButton allFields;
    private RadioButton selectedFields;
    private ComboBox<String> decimalSeparator;
    private ComboBox<String> fileFormat;
    private CheckBox useQuery;
    private Button queryWizard;
    private TextArea queryStatement;
    private Button customize;
    private Button xtfrButton;

    private RadioButton intDesc;
    private RadioButton txtDesc;

    private BorderPane as400QueryP;
    private GridPane as400p;

    boolean fieldsSelected;
    boolean emailIt;

    tnvt vt;
    XTFRFileFilterBuilder htmlFilter;
    XTFRFileFilterBuilder KSpreadFilter;
    XTFRFileFilterBuilder OOFilter;
    XTFRFileFilterBuilder ExcelFilter;
    XTFRFileFilterBuilder DelimitedFilter;
    XTFRFileFilterBuilder FixedWidthFilter;
    //   XTFRFileFilter ExcelWorkbookFilter;

    // default file filter used.
    XTFRFileFilterBuilder fileFilter;

    ProgressBar progressBar;
    TextArea taskOutput;
    Label fieldsLabel;
    Label textDescLabel;
    Label label;
    Label note;
    ProgressOptionPane monitor;
    XTFRFileFilterBuilder filter;
    SessionGui session;
    private final Window parent;

    static String messageProgress;

    public XTFRFile(final tnvt pvt, final SessionGui session) {
        this(pvt, session, null);
    }

    public XTFRFile(final tnvt pvt, final SessionGui session, final Properties XTFRProps) {
        this.session = session;
        this.parent = session.getWindow();
        this.vt = pvt;

        final BorderPane contentPane = new BorderPane();

        final Scene scene = new Scene(contentPane);
        scene.getStylesheets().add("/application.css");

        stage.setTitle(LangTool.getString("xtfr.title"));
        stage.setScene(scene);

        contentPane.setCursor(Cursor.WAIT);
        initFileFilters();
        contentPane.setCenter(initXTFRInfo(XTFRProps));

        stage.setOnHiding(e -> {
            if (ftpProtocol != null && ftpProtocol.isConnected()) {
                ftpProtocol.disconnect();
            }
        });

        final FTPStatusListener statusListener = FxProxyBuilder.buildProxy(new FTPStatusListener() {
            @Override
            public void statusReceived(final FTPStatusEvent e) {
                statusReceivedImpl(e);
            }
            @Override
            public void fileInfoReceived(final FTPStatusEvent e) {
                fileInfoReceivedImpl(e);
            }
            @Override
            public void commandStatusReceived(final FTPStatusEvent e) {
                commandStatusReceivedImpl(e);
            }
        }, FTPStatusListener.class);


        ftpProtocol = new FTP5250Prot(vt);
        ftpProtocol.addFTPStatusListener(statusListener );
        axtfr = new AS400Xtfr(vt);
        axtfr.addFTPStatusListener(statusListener);
        createProgressMonitor();

        messageProgress = LangTool.getString("xtfr.messageProgress");
        contentPane.setCursor(Cursor.DEFAULT);

        // now show the world what we can do
        setVisible(true);
    }

    private void initFileFilters() {
        htmlFilter =
                new XTFRFileFilterBuilder(
                        new String[]{"html", "htm"},
                        "Hyper Text Markup Language");
        htmlFilter.setOutputFilterName(
                "org.tn5250j.tools.filters.HTMLOutputFilter");
        KSpreadFilter = new XTFRFileFilterBuilder("ksp", "KSpread KDE Spreadsheet");
        KSpreadFilter.setOutputFilterName(
                "org.tn5250j.tools.filters.KSpreadOutputFilter");
        OOFilter = new XTFRFileFilterBuilder("sxc", "OpenOffice");
        OOFilter.setOutputFilterName(
                "org.tn5250j.tools.filters.OpenOfficeOutputFilter");
        ExcelFilter = new XTFRFileFilterBuilder("xls", "Excel");
        ExcelFilter.setOutputFilterName(
                "org.tn5250j.tools.filters.ExcelOutputFilter");
        DelimitedFilter =
                new XTFRFileFilterBuilder(new String[]{"csv", "tab"}, "Delimited");
        DelimitedFilter.setOutputFilterName(
                "org.tn5250j.tools.filters.DelimitedOutputFilter");
        FixedWidthFilter = new XTFRFileFilterBuilder("txt", "Fixed Width");
        FixedWidthFilter.setOutputFilterName(
                "org.tn5250j.tools.filters.FixedWidthOutputFilter");
        //      ExcelWorkbookFilter = new XTFRFileFilter("xls", "Excel 95 97 XP 2000");
        //      ExcelWorkbookFilter.setOutputFilterName("org.tn5250j.tools.filters.ExcelWorkbookOutputFilter");
    }

    private void statusReceivedImpl(final FTPStatusEvent statusevent) {

        if (monitor.isCanceled()) {
            ftpProtocol.setAborted();
        } else {
            final int prog = statusevent.getCurrentRecord();
            final int len = statusevent.getFileLength();
            final Runnable udp = () -> {
                if (prog >= len) {
                    progressBar.setProgress(len);
                    label.setText(LangTool.getString("xtfr.labelComplete"));
                    note.setText(getTransferredNote(len));
                    monitor.setDone();
                    if (emailIt)
                        emailMe();

                } else {
                    progressBar.setProgress((double) prog / len);
                    note.setText(getProgressNote(prog, len));
                }
            };
            Platform.runLater(udp);
        }
    }

    private String getProgressNote(final int prog, final int len) {

        final Object[] args = {Integer.toString(prog), Integer.toString(len)};

        try {
            return MessageFormat.format(messageProgress, args);
        } catch (final Exception exc) {
            System.out.println(" getProgressNote: " + exc.getMessage());
            return "Record " + prog + " of " + len;
        }
    }

    private void emailMe() {
        new SendEMailDialog(session, localFile.getText());
    }

    private String getTransferredNote(final int len) {

        final Object[] args = {Integer.toString(len)};

        try {
            return MessageFormat.format(
                    LangTool.getString("xtfr.messageTransferred"),
                    args);
        } catch (final Exception exc) {
            System.out.println(" getTransferredNote: " + exc.getMessage());
            return len + " records transferred!";
        }
    }

    private void commandStatusReceivedImpl(final FTPStatusEvent e) {
        final String message = e.getMessage() + '\n';
        Platform.runLater(() -> taskOutput.setText(taskOutput.getText() + message));
    }

    private void fileInfoReceivedImpl(final FTPStatusEvent e) {

        hostFile.setText(ftpProtocol.getFullFileName(hostFile.getText()));

        if (allFields.isSelected()) {
            doTransfer();
        } else {
            selectFields();
        }
    }

    private void actionPerformed(final ActionEvent e) {
        final String command = (String) ((Node) e.getSource()).getUserData();

        // process the save transfer information button
        if (command.equals("SAVE")) {
            saveXTFRInfo();
        } else if (command.equals("LOAD")) {
            // process the save transfer information button
            loadXTFRInfo();
        } else if (command.equals("XTFR") || command.equals("EMAIL")) {
            saveXTFRFields();

            if (command.equals("EMAIL"))
                emailIt = true;
            else
                emailIt = false;

            initializeMonitor();
            monitor.showDialog();

            if (useQuery.isSelected()) {

                axtfr.login(user.getText(), password.getText());
                // this will execute in it's own thread and will send a
                //    fileInfoReceived(FTPStatusEvent statusevent) event when
                //    finished without an error.
                axtfr.setDecimalChar(getDecimalChar());
                axtfr.connect(systemName.getText());

            } else {
                if (ftpProtocol != null && ftpProtocol.connect(systemName.getText(), 21)) {

                    if (ftpProtocol.login(user.getText(), password.getText())) {
                        // this will execute in it's own thread and will send a
                        //    fileInfoReceived(FTPStatusEvent statusevent) event when
                        //    finished without an error.
                        ftpProtocol.setDecimalChar(getDecimalChar());
                        ftpProtocol.getFileInfo(hostFile.getText(), intDesc.isSelected());
                    }
                } else {
                    disconnect();
                }
            }
        } else if (command.equals("BROWSEPC")) {
            getPCFile();
        } else if (command.equals("CUSTOMIZE")) {
            filter.getOutputFilterInstance().setCustomProperties();
        }
    }

    private char getDecimalChar() {
        final String ds = decimalSeparator.getValue();
        return ds.charAt(1);
    }

    private void initializeMonitor() {
        progressBar.setProgress(0);
        label.setText(LangTool.getString("xtfr.labelInProgress"));
        note.setText(LangTool.getString("xtfr.labelFileInfo"));
    }

    private void disconnect() {
        if (ftpProtocol != null) {
            ftpProtocol.disconnect();
            ftpProtocol = null;
        }
    }

    private void doTransfer() {
        fileFilter = getFilterByDescription();

        if (useQuery.isSelected()) {

            axtfr.setOutputFilter(fileFilter.getOutputFilterInstance());
            axtfr.getFile(
                    hostFile.getText(),
                    fileFilter.setExtension(localFile.getText()),
                    queryStatement.getText().trim(),
                    intDesc.isSelected());

        } else {
            ftpProtocol.setOutputFilter(fileFilter.getOutputFilterInstance());

            ftpProtocol.getFile(
                    hostFile.getText(),
                    fileFilter.setExtension(localFile.getText()));
        }
    }

    private XTFRFileFilterBuilder getFilterByDescription() {

        final String desc = fileFormat.getValue();

        if (KSpreadFilter.getDescription().equals(desc))
            return KSpreadFilter;
        if (OOFilter.getDescription().equals(desc))
            return OOFilter;
        if (ExcelFilter.getDescription().equals(desc))
            return ExcelFilter;
        if (DelimitedFilter.getDescription().equals(desc))
            return DelimitedFilter;
        if (FixedWidthFilter.getDescription().equals(desc))
            return FixedWidthFilter;
        //      if (ExcelWorkbookFilter.isExtensionInList(localFile.getText()))
        //         return ExcelWorkbookFilter;

        return htmlFilter;
    }

    private void createProgressMonitor() {
        progressBar = new ProgressBar();
        progressBar.setProgress(0);

        taskOutput = new TextArea();
        taskOutput.setPrefColumnCount(20);
        taskOutput.setPrefRowCount(6);
        taskOutput.setStyle("-fx-padding: 5px;");
        taskOutput.setEditable(false);

        final BorderPane panel = new BorderPane();

        note = new Label();
        note.setTextFill(Color.BLUE);
        label = new Label();
        label.setTextFill(Color.BLUE);

        panel.setTop(label);
        panel.setCenter(note);
        panel.setBottom(progressBar);

        final BorderPane contentPane = new BorderPane();
        contentPane.setTop(panel);
        contentPane.setCenter(taskOutput);

        monitor = new ProgressOptionPane(contentPane);
    }

    private void startWizard() {

        try {
            final SqlWizard wizard = new SqlWizard(
                    systemName.getText().trim(), user.getText(), password.getText());
            wizard.setQueryTextArea(queryStatement);
            wizard.show();
        } catch (final NoClassDefFoundError ncdfe) {
            UiUtils.showError(LangTool.getString("messages.noAS400Toolbox"), "Error");
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Get the local file from a file chooser
     */
    private void getPCFile() {

        final FileChooser pcFileChooser = new FileChooser();
        pcFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // set the file filters for the file chooser
        filter = getFilterByDescription();

        pcFileChooser.setSelectedExtensionFilter(filter.buildFilter());

        final File file = pcFileChooser.showSaveDialog(parent);

        // check to see if something was actually chosen
        if (file != null) {
            localFile.setText(filter.setExtension(file));
        }
    }

    /**
     * Creates the dialog components for prompting the user for the information
     * of the transfer
     */
    private BorderPane initXTFRInfo(final Properties XTFRProps) {

        // create some reusable borders and layouts
        // main panel
        final BorderPane mp = new BorderPane();

        // system panel
        final VBox sp = new VBox();
        sp.setSpacing(5);
        sp.setStyle("-fx-padding: 0.5em 0.5em 0.5em 0.5em;");

        // host panel for as400
        final TitledBorderedPane as400pTitle = new TitledBorderedPane();
        as400pTitle.setTitle(LangTool.getString("xtfr.labelAS400"));

        as400p = new GridPane();
        as400p.setMaxWidth(Double.POSITIVE_INFINITY);
        as400pTitle.setContent(as400p);

        final Label snpLabel = new Label(LangTool.getString("xtfr.labelSystemName"));
        as400p.getChildren().add(addSimpleConstraints(snpLabel, 0, 0, insets(5, 10, 5, 5)));

        systemName = new TextField(vt.getHostName());
        GridPane.setColumnSpan(systemName, 2);
        systemName.setPrefColumnCount(30);
        as400p.getChildren().add(addWideComponentConstraints(systemName, 0, 1, insets(5, 5, 5, 10), 2));

        final Label hfnpLabel = new Label(LangTool.getString("xtfr.labelHostFile"));
        as400p.getChildren().add(addSimpleConstraints(hfnpLabel, 1, 0, insets(5, 10, 5, 5)));

        hostFile = new TextField();
        GridPane.setColumnSpan(hostFile, 2);
        as400p.getChildren().add(addWideComponentConstraints(hostFile, 1, 1, insets(5, 5, 5, 10), 2));

        final Label idpLabel = new Label(LangTool.getString("xtfr.labelUserId"));
        as400p.getChildren().add(addSimpleConstraints(idpLabel, 2, 0, insets(5, 10, 5, 5)));

        user = new TextField();
        user.setPrefColumnCount(5);
        as400p.getChildren().add(addWideComponentConstraints(user, 2, 1, insets(5, 5, 5, 5), 1));

        // password panel
        final Label pwpLabel = new Label(LangTool.getString("xtfr.labelPassword"));
        as400p.getChildren().add(addSimpleConstraints(pwpLabel, 3, 0, insets(5, 10, 5, 5)));

        password = new PasswordField();
        password.setPrefColumnCount(5);
        password.addEventHandler(KeyEvent.KEY_TYPED, this::txtONKeyPressed);
        as400p.getChildren().add(addWideComponentConstraints(password, 3, 1, insets(5, 5, 5, 5), 1));

        // Query Wizard
        useQuery = new CheckBox(LangTool.getString("xtfr.labelUseQuery"));
        useQuery.selectedProperty().addListener((src, old, value) -> useQueryStateChanged());;
        as400p.getChildren().add(addSimpleConstraints(useQuery, 4, 0, insets(5, 10, 5, 5)));

        //query button
        queryWizard = new Button(LangTool.getString("xtfr.labelQueryWizard"));
        queryWizard.setOnAction(e -> startWizard());
        queryWizard.setDisable(true);
        as400p.getChildren().add(addWideComponentConstraints(queryWizard, 4, 1, insets(0, 5, 0, 5), 1));

        // Field Selection panel
        fieldsLabel = new Label(LangTool.getString("xtfr.labelFields"));
        as400p.getChildren().add(addSimpleConstraints(fieldsLabel, 5, 0, insets(5, 10, 5, 5)));

        allFields = new RadioButton(LangTool.getString("xtfr.labelAllFields"));
        allFields.setSelected(true);
        as400p.getChildren().add(addSimpleConstraints(allFields, 5, 1, insets(0, 5, 0, 5)));

        selectedFields = new RadioButton(LangTool.getString("xtfr.labelSelectedFields"));
        as400p.getChildren().add(addSimpleConstraints(selectedFields, 5, 2, insets(0, 5, 0, 10)));

        final ToggleGroup fieldGroup = new ToggleGroup();
        fieldGroup.getToggles().add(allFields);
        fieldGroup.getToggles().add(selectedFields);

        // Field Text Description panel
        textDescLabel = new Label(LangTool.getString("xtfr.labelTxtDesc"));
        as400p.getChildren().add(addSimpleConstraints(textDescLabel, 6, 0, insets(5, 10, 5, 5)));

        txtDesc = new RadioButton(LangTool.getString("xtfr.labelTxtDescFull"));
        txtDesc.setSelected(true);
        as400p.getChildren().add(addSimpleConstraints(txtDesc, 6, 1, insets(0, 5, 5, 5)));

        intDesc = new RadioButton(LangTool.getString("xtfr.labelTxtDescInt"));
        as400p.getChildren().add(addSimpleConstraints(intDesc, 6, 2, insets(0, 5, 5, 10)));

        final ToggleGroup txtDescGroup = new ToggleGroup();
        txtDescGroup.getToggles().add(txtDesc);
        txtDescGroup.getToggles().add(intDesc);

        // pc panel for pc information
        final TitledBorderedPane pcpTitle = new TitledBorderedPane();
        pcpTitle.setTitle(LangTool.getString("xtfr.labelpc"));

        final GridPane pcp = new GridPane();
        pcp.setVgap(5);
        pcp.setMaxWidth(Double.POSITIVE_INFINITY);
        pcpTitle.setContent(pcp);

        final Label pffLabel = new Label(LangTool.getString("xtfr.labelFileFormat"));
        pcp.getChildren().add(addSimpleConstraints(pffLabel, 0, 0, insets(5, 10, 5, 5)));

        fileFormat = new ComboBox<>();
        fileFormat.getItems().add(htmlFilter.getDescription());
        fileFormat.getItems().add(OOFilter.getDescription());
        fileFormat.getItems().add(ExcelFilter.getDescription());
        fileFormat.getItems().add(KSpreadFilter.getDescription());
        fileFormat.getItems().add(DelimitedFilter.getDescription());
        fileFormat.getItems().add(FixedWidthFilter.getDescription());
        fileFormat.getSelectionModel().selectedItemProperty().addListener((src, old, value) -> {
            filter = getFilterByDescription();
            customize.setDisable(!filter.getOutputFilterInstance().isCustomizable());
        });
        pcp.getChildren().add(addWideComponentConstraints(fileFormat, 0, 1, insets(0, 5, 0, 5), 2));

        customize = new Button(LangTool.getString("xtfr.labelCustomize"));
        customize.setUserData("CUSTOMIZE");
        customize.setOnAction(this::actionPerformed);
        customize.setMaxWidth(Double.POSITIVE_INFINITY);
        GridPane.setFillWidth(customize, true);
        pcp.getChildren().add(addSimpleConstraints(customize, 0, 3, insets(0, 5, 0, 10)));

        // now make sure we set the customizable button enabled or not
        // depending on the filter.
        fileFormat.getSelectionModel().selectFirst();

        final Label pcpLabel = new Label(LangTool.getString("xtfr.labelPCFile"));
        pcp.getChildren().add(addSimpleConstraints(pcpLabel, 1, 0, insets(5, 10, 5, 5)));

        localFile = new TextField();
        pcp.getChildren().add(addWideComponentConstraints(localFile, 1, 1, insets(0, 5, 0, 5), 2));

        final Button browsePC = new Button(LangTool.getString("xtfr.labelPCBrowse"));
        browsePC.setUserData("BROWSEPC");
        browsePC.setOnAction(this::actionPerformed);
        browsePC.setMaxWidth(Double.POSITIVE_INFINITY);
        GridPane.setFillWidth(browsePC, true);
        pcp.getChildren().add(addSimpleConstraints(browsePC, 1, 3, insets(0, 5, 0, 10)));

        // decimal separator
        pcp.getChildren().add(addSimpleConstraints(new Label(LangTool.getString("xtfr.labelDecimal")),
                2, 0, insets(5, 10, 10, 5)));

        decimalSeparator = new ComboBox<>();
        decimalSeparator.getItems().add(LangTool.getString("xtfr.period"));
        decimalSeparator.getItems().add(LangTool.getString("xtfr.comma"));

        // obtain the decimal separator for the machine locale
        final DecimalFormat formatter =
                (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());

        if (formatter.getDecimalFormatSymbols().getDecimalSeparator() == '.')
            decimalSeparator.getSelectionModel().select(0);
        else
            decimalSeparator.getSelectionModel().select(1);

        pcp.getChildren().add(addWideComponentConstraints(decimalSeparator, 2, 1, insets(0, 5, 5, 5), 2));

        sp.getChildren().add(as400pTitle);
        sp.getChildren().add(pcpTitle);

        // options panel
        final HBox op = new HBox();
        op.setSpacing(5);
        op.setAlignment(Pos.CENTER);
        op.setStyle("-fx-padding: 0.5em 0.5em 0.5em 0.5em;");

        xtfrButton = new Button(LangTool.getString("xtfr.labelXTFR"));
        xtfrButton.setOnAction(this::actionPerformed);
        xtfrButton.setUserData("XTFR");
        op.getChildren().add(xtfrButton);

        final Button emailButton =
                new Button(LangTool.getString("xtfr.labelXTFREmail"));
        emailButton.setOnAction(this::actionPerformed);
        emailButton.setUserData("EMAIL");
        op.getChildren().add(emailButton);

        // add transfer save information button
        final Button saveButton = new Button(LangTool.getString("xtfr.labelXTFRSave"));
        saveButton.setOnAction(this::actionPerformed);
        saveButton.setUserData("SAVE");
        op.getChildren().add(saveButton);

        // add transfer load information button
        final Button loadButton = new Button(LangTool.getString("xtfr.labelXTFRLoad"));
        loadButton.setOnAction(this::actionPerformed);
        loadButton.setUserData("LOAD");
        op.getChildren().add(loadButton);

        mp.setCenter(sp);
        mp.setBottom(op);

        //This QueryPanel will added when Use Query selected
        as400QueryP = new BorderPane();
        GridPane.setRowSpan(as400QueryP, 3);
        GridPane.setColumnSpan(as400QueryP, 3);
        addSimpleConstraints(as400QueryP, 5, 0, insets(5, 10, 10, 10));

        queryStatement = new TextArea();
        queryStatement.setPrefRowCount(2);
        queryStatement.setWrapText(true);
        as400QueryP.setCenter(queryStatement);

        initXTFRFields(XTFRProps);

        return mp;
    }

    private static Node addWideComponentConstraints(final Region node, final int row,
            final int column, final Insets insets, final int width) {
        addSimpleConstraints(node, row, column, insets);
        node.setMaxWidth(Double.POSITIVE_INFINITY);
        GridPane.setColumnSpan(node, width);
        GridPane.setFillWidth(node, true);
        return node;
    }

    private static Node addSimpleConstraints(final Region node, final int row, final int column, final Insets insets) {
        GridPane.setRowIndex(node, row);
        GridPane.setColumnIndex(node, column);
        GridPane.setMargin(node, insets);
        GridPane.setHalignment(node, HPos.LEFT);
        return node;
    }

    private static Insets insets(final double top, final double left, final double bottom, final double right) {
        return new Insets(top, right, bottom, left);
    }

    private void txtONKeyPressed(final KeyEvent evt) {
        if (evt.getCode() == KeyCode.ENTER && evt.getEventType() == KeyEvent.KEY_PRESSED) {
            xtfrButton.fire();
        }
    }

    private void initXTFRFields(Properties props) {

        if (props == null) {
            final SessionConfig config = session.getSession().getConfiguration();
            props = config.getProperties();
        }

        if (props.containsKey("xtfr.fileName"))
            hostFile.setText(props.getProperty("xtfr.fileName"));

        if (props.containsKey("xtfr.user"))
            user.setText(props.getProperty("xtfr.user"));

        if (props.containsKey("xtfr.useQuery")) {
            if (props.getProperty("xtfr.useQuery").equals("true"))
                useQuery.setSelected(true);
            else
                useQuery.setSelected(false);
        }

        if (props.containsKey("xtfr.queryStatement")) {
            queryStatement.setText(props.getProperty("xtfr.queryStatement"));
        }

        if (props.containsKey("xtfr.allFields")) {
            if (props.getProperty("xtfr.allFields").equals("true"))
                allFields.setSelected(true);
            else
                allFields.setSelected(false);
        }

        if (props.containsKey("xtfr.txtDesc")) {
            if (props.getProperty("xtfr.txtDesc").equals("true"))
                txtDesc.setSelected(true);
            else
                txtDesc.setSelected(false);
        }

        if (props.containsKey("xtfr.intDesc")) {
            if (props.getProperty("xtfr.intDesc").equals("true")) {
                intDesc.setSelected(true);
            } else {
                intDesc.setSelected(false);
            }
        }

        if (props.containsKey("xtfr.fileFormat"))
            fileFormat.getSelectionModel().select(props.getProperty("xtfr.fileFormat"));

        if (props.containsKey("xtfr.localFile"))
            localFile.setText(props.getProperty("xtfr.localFile"));

        if (props.containsKey("xtfr.decimalSeparator"))
            decimalSeparator.getSelectionModel().select(props.getProperty("xtfr.decimalSeparator"));

    }

    private void saveXTFRFields() {

        final SessionConfig config = session.getSession().getConfiguration();
        final Properties props = config.getProperties();

        saveXTFRFields(props);

        config.setModified(true);

    }

    private void saveXTFRFields(final Properties props) {

        if (hostFile.getText().trim().length() > 0)
            props.setProperty("xtfr.fileName", hostFile.getText().trim());
        else
            props.remove("xtfr.fileName");

        if (user.getText().trim().length() > 0)
            props.setProperty("xtfr.user", user.getText().trim());
        else
            props.remove("xtfr.user");

        if (useQuery.isSelected())
            props.setProperty("xtfr.useQuery", "true");
        else
            props.remove("xtfr.useQuery");

        if (queryStatement.getText().trim().length() > 0)
            props.setProperty(
                    "xtfr.queryStatement",
                    queryStatement.getText().trim());
        else
            props.remove("xtfr.queryStatement");

        if (allFields.isSelected())
            props.setProperty("xtfr.allFields", "true");
        else
            props.remove("xtfr.allFields");

        // TODO: save Fielddesc state as one propertyvalue (xtfr.fieldDesc=txt|int)
        if (txtDesc.isSelected())
            props.setProperty("xtfr.txtDesc", "true");
        else
            props.remove("xtfr.txtDesc");
        if (intDesc.isSelected())
            props.setProperty("xtfr.intDesc", "true");
        else
            props.remove("xtfr.intDesc");

        props.setProperty(
                "xtfr.fileFormat",
                fileFormat.getValue());

        if (localFile.getText().trim().length() > 0)
            props.setProperty("xtfr.localFile", localFile.getText().trim());
        else
            props.remove("xtfr.localFile");

        props.setProperty("xtfr.decimalSeparator", decimalSeparator.getValue());
    }

    private void saveXTFRInfo() {

        final Properties xtfrProps = new Properties();
        xtfrProps.setProperty("xtfr.destination", "FROM");
        this.saveXTFRFields(xtfrProps);
        final FileChooser pcFileChooser = new FileChooser();
        pcFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // set the file filters for the file chooser
        final TN5250jFileFilterBuilder filter = new TN5250jFileFilterBuilder("dtf", "Transfer from AS/400");

        pcFileChooser.setSelectedExtensionFilter(filter.buildFilter());

        File file = pcFileChooser.showSaveDialog(parent);

        // check to see if something was actually chosen
        if (file != null) {
            file = new File(filter.setExtension(file));

            try {
                final FileOutputStream out = new FileOutputStream(file);
                // save off the width and height to be restored later
                xtfrProps.store(out, "------ Transfer Details --------");

                out.flush();
                out.close();
            } catch (final FileNotFoundException fnfe) {
            } catch (final IOException ioe) {
            }
        }
    }

    private void loadXTFRInfo() {

        final Properties xtfrProps = new Properties();
//      xtfrProps.setProperty("xtfr.destination","FROM");
//      this.saveXTFRFields(xtfrProps);
        final FileChooser pcFileChooser = new FileChooser();
        pcFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // set the file filters for the file chooser
        final TN5250jFileFilterBuilder filter = new TN5250jFileFilterBuilder("dtf", "Transfer from AS/400");

        pcFileChooser.setSelectedExtensionFilter(filter.buildFilter());

        final File file = pcFileChooser.showOpenDialog(parent);

        // check to see if something was actually chosen
        if (file != null) {

            try {
                final FileInputStream in = new FileInputStream(file);
                // save off the width and height to be restored later
                xtfrProps.load(in);

                in.close();
            } catch (final FileNotFoundException fnfe) {
            } catch (final IOException ioe) {
            }
        }

        if (xtfrProps.containsKey("xtfr.destination") &&
                (xtfrProps.get("xtfr.destination").equals("FROM"))) {

            this.initXTFRFields(xtfrProps);
        }
    }

    /** Listens to the use query check boxe */
    private void useQueryStateChanged() {
        if (useQuery.isSelected()) {
            queryWizard.setDisable(false);
            as400p.getChildren().remove(fieldsLabel);
            as400p.getChildren().remove(allFields);
            as400p.getChildren().remove(selectedFields);
            as400p.getChildren().remove(textDescLabel);
            as400p.getChildren().remove(txtDesc);
            as400p.getChildren().remove(intDesc);

            as400p.getChildren().add(as400QueryP);
        } else {
            queryWizard.setDisable(true);
            as400p.getChildren().remove(as400QueryP);

            as400p.getChildren().add(fieldsLabel);
            allFields.setSelected(true);

            as400p.getChildren().add(allFields);
            as400p.getChildren().add(selectedFields);
            as400p.getChildren().add(textDescLabel);
            txtDesc.setSelected(true);

            as400p.getChildren().add(txtDesc);
            as400p.getChildren().add(intDesc);
        }
    }

    private void selectFields() {
        //Create table to hold field data
        final XTFRFieldTable table = new XTFRFieldTable(ftpProtocol);
        final BorderPane content = new BorderPane();
        content.setCenter(table);

        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(LangTool.getString("xtfr.titleFieldSelection"));
        final NotClosableDialogPane dialogPane = new NotClosableDialogPane();
        dialog.setDialogPane(dialogPane);

        final ButtonType selectAll = ButtonType.NEXT;
        final ButtonType selectNone = ButtonType.PREVIOUS;
        final ButtonType done = ButtonType.OK;
        dialogPane.getButtonTypes().addAll(selectAll, selectNone, done);

        final Button selectAllButton = (Button) dialogPane.lookupButton(selectAll);
        selectAllButton.setText(LangTool.getString("xtfr.tableSelectAll"));
        selectAllButton.setOnAction(e -> table.selectAll());

        final Button deselectAllButton = (Button) dialogPane.lookupButton(selectNone);
        deselectAllButton.setText(LangTool.getString("xtfr.tableSelectNone"));
        deselectAllButton.setOnAction(e -> table.deselectAll());

        final Button doneButton = (Button) dialogPane.lookupButton(done);
        doneButton.setText(LangTool.getString("xtfr.tableDone"));
        doneButton.setDefaultButton(true);
        doneButton.setOnAction(e -> {
            try {
                fieldsSelected = ftpProtocol.isFieldsSelected();
                if (ftpProtocol.isFieldsSelected())
                    doTransfer();
            } finally {
                dialog.hide();
            }
        });

        dialogPane.setContent(content);
        dialog.showAndWait();
    }

    /**
     * Create a option pane to show status of the transfer
     */
    private class ProgressOptionPane extends ActionDelegateDialogPane<ButtonType> {

        ProgressOptionPane(final BorderPane content) {
            super();
            setButtonText("Cancel");
            setCursor(Cursor.WAIT);
            setContent(content);
        }

        @Override
        protected void okPressed() {
            if (ftpProtocol != null) {
                ftpProtocol.setAborted();
            }
            if (dialog != null) {
                dialog.hide();
                dialog = null;
            }
        }

        private void setButtonText(final String text) {
            ((Button) lookupButton(ButtonType.OK)).setText(text);
        }

        void showDialog() {
            if (dialog != null) {
                return;
            }

            setButtonText("Cancel");

            dialog = new Dialog<>();
            dialog.setTitle(LangTool.getString("xtfr.progressTitle"));
            dialog.setDialogPane(this);
            dialog.setResizable(true);
            dialog.show();
        }

        public void setDone() {
            setButtonText(LangTool.getString("xtfr.tableDone"));
            setCursor(Cursor.DEFAULT);
        }

        /**
         * Returns true if the user hits the Cancel button in the progress dialog.
         *
         * @return whether or not dialog was cancelled
         */
        public boolean isCanceled() {
            return (dialog.getResult() != null);
        }
    }
}
