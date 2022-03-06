/*
 * @(#)DelimitedOutputFilter.java
 * Copyright:    Copyright (c) 2001, 2002, 2003
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.tools.filters;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.tools.LangTool;

import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class DelimitedOutputFilter implements OutputFilterInterface {

    PrintStream fout = null;
    static String delimiter = ",";
    static String stringQualifier = "\"";
    StringBuffer sb = new StringBuffer();

    // create instance of file for output
    @Override
    public void createFileInstance(final String fileName) throws
            FileNotFoundException {
        fout = new PrintStream(new FileOutputStream(fileName));
    }

    /**
     * Write the html header of the output file
     */
    @Override
    public void parseFields(final byte[] cByte, final List<FileFieldDef> ffd, final StringBuffer rb) {

        FileFieldDef f;

        // write out the html record information for each field that is selected

        for (int x = 0; x < ffd.size(); x++) {
            f = ffd.get(x);
            if (f.isWriteField()) {


                switch (f.getFieldType()) {

                    case 'P':
                    case 'S':
                        rb.append(f.parseData(cByte).trim() + delimiter);
                        break;
                    default:
                        rb.append(stringQualifier + f.parseData(cByte).trim() + stringQualifier + delimiter);
                        break;

                }
            }
        }

        fout.println(rb);

    }

    /**
     * Write the html header of the output file
     */
    @Override
    public void writeHeader(final String fileName, final String host,
                            final List<FileFieldDef> ffd, final char decChar) {

        FileFieldDef f;
        final StringBuffer sb = new StringBuffer();
        //  loop through each of the fields and write out the field name for
        //    each selected field
        for (int x = 0; x < ffd.size(); x++) {
            f = ffd.get(x);
            if (f.isWriteField()) {
                sb.append(f.getFieldName() + delimiter);
            }
        }

        fout.println(sb.toString().toCharArray());
    }


    /**
     * write the footer of the html output
     */
    @Override
    public void writeFooter(final List<FileFieldDef> ffd) {

        fout.flush();
        fout.close();

    }

    @Override
    public boolean isCustomizable() {
        return true;
    }

    @Override
    public void setCustomProperties() {

        new DelimitedDialog();
    }

    class DelimitedDialog {

        public DelimitedDialog() {

            final TitledBorderedPane content = new TitledBorderedPane();
            content.setTitle(LangTool.getString("delm.labelOptions"));

            final GridPane opts = new GridPane();
            opts.setHgap(5);
            opts.setVgap(5);

            content.setContent(opts);

            final Label fdl = new Label(LangTool.getString("delm.labelField"));

            // setup the field delimiter list
            final ComboBox<String> fd = new ComboBox<>();
            fd.getItems().add(",");
            fd.getItems().add(";");
            fd.getItems().add(":");
            fd.getItems().add("|");
            fd.getItems().add(LangTool.getString("delm.labelTab"));
            fd.getItems().add(LangTool.getString("delm.labelSpace"));
            fd.getItems().add(LangTool.getString("delm.labelNone"));

            if (delimiter.length() > 0)
                if (delimiter.equals("\t"))
                    fd.getSelectionModel().select(4);
                else if (delimiter.equals(" "))
                    fd.getSelectionModel().select(5);
                else {
                    if (!delimiter.equals(",") && !delimiter.equals(";") &&
                            !delimiter.equals(":") && !delimiter.equals("|"))
                        fd.getItems().add(delimiter);

                    fd.getSelectionModel().select(delimiter);
                }
            else
                fd.getSelectionModel().select(6);

            fd.setEditable(true);

            // setup the string qualifier list
            final Label tdl = new Label(LangTool.getString("delm.labelText"));
            final ComboBox<String> td = new ComboBox<>();
            td.getItems().add("\"");
            td.getItems().add("'");
            td.getItems().add(LangTool.getString("delm.labelNone"));

            if (stringQualifier.length() > 0) {
                if (!stringQualifier.equals("'") && !stringQualifier.equals("\""))
                    td.getItems().add(stringQualifier);
                td.getSelectionModel().select(stringQualifier);
            } else
                td.getSelectionModel().select(2);

            td.setEditable(true);

            opts.getChildren().add(setConstraints(fdl, 0, 0));
            opts.getChildren().add(setConstraints(fd, 0, 1));
            opts.getChildren().add(setConstraints(tdl, 1, 0));
            opts.getChildren().add(setConstraints(td, 1, 1));

            final Alert alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle(LangTool.getString("delm.title"));
            alert.getDialogPane().setHeaderText("");
            alert.getDialogPane().setContent(content);

            final ButtonType result = alert.showAndWait().orElse(null);
            if (result == ButtonType.OK) {
                delimiter = fd.getValue();
                if (delimiter.equals(LangTool.getString("delm.labelSpace")))
                    delimiter = " ";
                if (delimiter.equals(LangTool.getString("delm.labelTab")))
                    delimiter = "\t";
                if (delimiter.equals(LangTool.getString("delm.labelNone")))
                    delimiter = "";
                stringQualifier = td.getValue();
                if (stringQualifier.equals(LangTool.getString("delm.labelNone")))
                    stringQualifier = "";
            }
        }
    }

    static Region setConstraints(final Region component, final int row, final int column) {
        GridPane.setRowIndex(component, row);
        GridPane.setColumnIndex(component, column);
        GridPane.setHalignment(component, HPos.LEFT);
        return component;
    }
}
