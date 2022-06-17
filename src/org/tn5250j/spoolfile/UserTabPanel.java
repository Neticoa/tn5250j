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
public class UserTabPanel extends GridPane implements QueueFilterInterface {

    private final RadioButton all;
    private final RadioButton select;
    private final TextField user;

    public UserTabPanel() {
        setHgap(5);
        setVgap(5);
        setStyle("-fx-padding: 0.5em 0 0 0;");

        all = new RadioButton("All");
        all.setSelected(false);

        select = new RadioButton("User");
        select.setSelected(true);

        user = new TextField("*CURRENT");
        user.setPrefColumnCount(15);
        user.textProperty().addListener((src, old, value) -> textChanged(user));

        final ToggleGroup bg = new ToggleGroup();
        bg.getToggles().add(all);
        bg.getToggles().add(select);

        getChildren().add(all);

        getChildren().add(select);
        getChildren().add(user);

        setGridConstrains(all, 0, 0, 2);
        setGridConstrains(select, 1, 0, 1);
        setGridConstrains(user, 1, 1, 1);
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
        user.setText("*CURRENT");
        select.setSelected(true);

    }

    public String getUser() {
        if (all.isSelected())
            return "*ALL";
        else
            return user.getText().trim();
    }

    public void setUser(final String filter) {

        user.setText(filter);
    }
}
