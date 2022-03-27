package org.tn5250j.sessionsettings;
/*
 * Title: PrinterAttributesPanel
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

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.AbstractSessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

class PrinterAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane defaultPrinterPanel;
    @FXML
    CheckBox defaultPrinter;

    @FXML
    TitledBorderedPane printerScreenPageParametersPanel;
    @FXML
    Button setPortAttributes;
    @FXML
    Button setLandAttributes;
    @FXML
    ComboBox<String> fonts;

    private PrinterAttributesHelper helper;

    PrinterAttributesController(final AbstractSessionConfig config) {
        super(config, "Printer");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // define ppPanel panel
        defaultPrinterPanel.setTitle(LangTool.getString("sa.print"));
        defaultPrinter.setText(LangTool.getString("sa.defaultPrinter"));
        defaultPrinter.setSelected("Yes".equals(getStringProperty("defaultPrinter")));

        helper = new PrinterAttributesHelper(changes);

        // define page panel
        printerScreenPageParametersPanel.setTitle(LangTool.getString("sa.pageParameters"));

        setPortAttributes.setText(LangTool.getString("sa.columns24"));
        setPortAttributes.setOnAction(e -> getPortraitAttributes());

        setLandAttributes.setText(LangTool.getString("sa.columns132"));
        setLandAttributes.setOnAction(e -> getLandscapeAttributes());

        for (final String fontName: Font.getFontNames()) {
            if (fontName.indexOf('.') < 0) {
                fonts.getItems().add(fontName);
            }
        }

        if (hasProperty("print.font")) {
            fonts.setValue(getStringProperty("print.font"));
        }
    }

    private void getPortraitAttributes() {
        helper.setPappyPort(getPrintAttibutes(helper.getPappyPort()));
    }

    private void getLandscapeAttributes() {
        helper.setPappyLand(getPrintAttibutes(helper.getPappyLand()));
    }

    private PageLayout getPrintAttibutes(final PageLayout layout) {
        final PrinterJob job = PrinterJob.createPrinterJob();
        job.getJobSettings().setPageLayout(layout);

        if (job.showPageSetupDialog(view.getScene().getWindow())) {
            return job.getJobSettings().getPageLayout();
        }

        return layout;
    }

    @Override
    public void applyAttributes() {

        if (defaultPrinter.isSelected()) {
            fireStringPropertyChanged("defaultPrinter", "Yes");
        } else {
            fireStringPropertyChanged("defaultPrinter", "No");
        }

        final PageLayout pappyPort = helper.getPappyPort();
        // portrait parameters
        Rectangle2D imageableArea = getImageableArea(pappyPort);

        fireDoublePropertyChanged("print.portWidth", pappyPort.getPaper().getWidth());
        fireDoublePropertyChanged("print.portImageWidth", imageableArea.getWidth());
        fireDoublePropertyChanged("print.portHeight", pappyPort.getPaper().getHeight());
        fireDoublePropertyChanged("print.portImageHeight", imageableArea.getHeight());
        fireDoublePropertyChanged("print.portImage.X", imageableArea.getMinX());
        fireDoublePropertyChanged("print.portImage.Y", imageableArea.getMinY());

        // landscape parameters
        imageableArea = getImageableArea(pappyPort);
        final PageLayout pappyLand = helper.getPappyLand();

        fireDoublePropertyChanged("print.landWidth", pappyLand.getPaper().getWidth());
        fireDoublePropertyChanged("print.landImageWidth", imageableArea.getWidth());
        fireDoublePropertyChanged("print.landHeight", pappyLand.getPaper().getHeight());
        fireDoublePropertyChanged("print.landImageHeight", imageableArea.getHeight());
        fireDoublePropertyChanged("print.landImage.X", imageableArea.getMinX());
        fireDoublePropertyChanged("print.landImage.Y", imageableArea.getMinY());

        if (fonts.getValue() != null) {
            fireStringPropertyChanged("print.font", fonts.getValue());
        }
    }

    private Rectangle2D getImageableArea(final PageLayout layout) {
        final double w = layout.getPaper().getWidth();
        final double h = layout.getPaper().getHeight();

        return new Rectangle2D(
                layout.getLeftMargin(),
                layout.getTopMargin(),
                w - layout.getLeftMargin() - layout.getRightMargin(),
                h - layout.getTopMargin() - layout.getBottomMargin());
    }

    private void fireDoublePropertyChanged(final String name, final double value) {
        changes.firePropertyChange(this, name, getStringProperty(name), value);
        setProperty("print.portWidth", Double.toString(value));
    }

    @Override
    public Region getView() {
        return view;
    }
}
