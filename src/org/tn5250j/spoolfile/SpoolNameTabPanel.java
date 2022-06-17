/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.spoolfile;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class SpoolNameTabPanel extends GridPane implements QueueFilterInterface {
    private final RadioButton all;
    private final RadioButton select;
    private final TextField spoolName;

    public SpoolNameTabPanel() {
        setHgap(5);
        setVgap(5);
        setStyle("-fx-padding: 0.5em 0 0 0;");

        all = new RadioButton("All");
        all.setSelected(true);

        select = new RadioButton("Spool Name");
        select.setSelected(false);

        spoolName = new TextField();
        spoolName.setPrefColumnCount(15);
        spoolName.textProperty().addListener((src, old, value) -> textChanged(spoolName));

        final ToggleGroup bg = new ToggleGroup();
        bg.getToggles().add(all);
        bg.getToggles().add(select);

        getChildren().add(all);

        getChildren().add(select);
        getChildren().add(spoolName);

        setGridConstrains(all, 0, 0, 2);
        setGridConstrains(select, 1, 0, 1);
        setGridConstrains(spoolName, 1, 1, 1);
    }

    private void setGridConstrains(final Node node, final int row, final int column, final int colSpan) {
        GridPane.setColumnIndex(node, column);
        GridPane.setRowIndex(node, row);
        GridPane.setColumnSpan(node, colSpan);
        GridPane.setHalignment(node, HPos.LEFT);
    }

    private void textChanged(final TextField textField) {
        final String text = textField.getText();
        if (text != null && !text.isEmpty()) {
            select.setSelected(true);
        }
    }

    /**
     * Reset to default value(s)
     */
    @Override
    public void reset() {

//      spoolName.setEnabled(false);
        spoolName.setText("");
        all.setSelected(true);

    }

    public String getSpoolName() {
        if (all.isSelected())
            return "";
        else
            return spoolName.getText().trim();
    }

    public void setSpoolName(final String filter) {

        spoolName.setText(filter);
    }
}
