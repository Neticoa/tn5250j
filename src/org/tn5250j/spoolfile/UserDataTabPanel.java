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
public class UserDataTabPanel extends GridPane implements QueueFilterInterface {

    RadioButton all;
    RadioButton select;
    TextField userData;

    public UserDataTabPanel() {
        setHgap(5);
        setVgap(5);
        setStyle("-fx-padding: 0.5em 0 0 0;");

        all = new RadioButton("All");
        all.setSelected(false);

        select = new RadioButton("User");
        select.setSelected(true);

        userData = new TextField();
        userData.setPrefColumnCount(15);
//      userData.setEnabled(false);
        userData.textProperty().addListener((src, old, value) -> textChanged(userData));

        final ToggleGroup bg = new ToggleGroup();
        bg.getToggles().add(all);
        bg.getToggles().add(select);

        getChildren().add(all);

        getChildren().add(select);
        getChildren().add(userData);

        setGridConstrains(all, 0, 0, 2);
        setGridConstrains(select, 1, 0, 1);
        setGridConstrains(userData, 1, 1, 1);
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

//      userData.setEnabled(false);
        userData.setText("");
        all.setSelected(true);

    }

    @Override
    public String getUserData() {
        if (all.isSelected())
            return "";
        else
            return userData.getText().trim();
    }

    public void setUserData(final String filter) {

        userData.setText(filter);
    }
}
