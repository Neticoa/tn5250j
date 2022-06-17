/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.tools;

import java.util.Arrays;

import org.tn5250j.tools.XTFRFieldTable.Bean;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
class XTFRFieldTable extends TableView<Bean> {

    private final double CHECK_BOX_WIDTH = new CheckBox().getPrefWidth();
    private final FTP5250Prot ftpProtocol;
    private boolean isDisabledItemListening;

    public XTFRFieldTable(final FTP5250Prot ftpProtocol) {
        this.ftpProtocol = ftpProtocol;

        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final TableColumn<Bean, Boolean> colA = createDefaultSessionColumn("xtfr.tableColA");
        final TableColumn<Bean, String> colB = createTableColumn("xtfr.tableColB");

        setEditable(true);
        getColumns().addAll(Arrays.asList(colA, colB));
    }

    private TableColumn<Bean, String> createTableColumn(
            final String title) {
        final TableColumn<Bean, String> col = new TableColumn<>(LangTool.getString(title));
        col.setEditable(false);
        col.setCellValueFactory(b -> b.getValue().name);
        return col;
    }

    private TableColumn<Bean, Boolean> createDefaultSessionColumn(
            final String title) {
        final TableColumn<Bean, Boolean> col = new TableColumn<>(LangTool.getString(title));
        col.setEditable(true);
        col.setPrefWidth(CHECK_BOX_WIDTH);
        col.setCellValueFactory(new PropertyValueFactory<>("value"));
        col.setCellFactory(c -> createTableCell());
        return col;
    }

    private CheckBoxTableCell<Bean, Boolean> createTableCell() {
        final Callback<Integer, ObservableValue<Boolean>> callback = e -> {
            final Bean model = XTFRFieldTable.this.getItems().get(e);
            return model == null ? null : model.value;
        };
        return new CheckBoxTableCell<>(callback);
    }

    public void addItem(final String name) {
        addItem(name, false);
    }

    public void addItem(final String name, final boolean value) {
        final Bean bean = new Bean();
        bean.name.setValue(name);
        bean.value.setValue(value);

        bean.value.addListener((src, old, v) -> itemChangedImpl(bean));
        getItems().add(bean);
    }

    private void itemChangedImpl(final Bean bean) {
        if (!isDisabledItemListening) {
            itemChanged(getItems().indexOf(bean), bean.value.getValue());
        }
    }

    protected void itemChanged(final int index, final boolean value) {
        ftpProtocol.setFieldSelected(index, value);
    }

    public void selectAll() {
        isDisabledItemListening = true;
        try {
            for (final Bean item : getItems()) {
                item.value.set(true);
            }
        } finally {
            isDisabledItemListening = false;
        }

        ftpProtocol.selectAll();
    }

    public void deselectAll() {
        isDisabledItemListening = true;
        try {
            for (final Bean item : getItems()) {
                item.value.set(false);
            }
        } finally {
            isDisabledItemListening = false;
        }

        ftpProtocol.selectNone();
    }

    static class Bean {
        public final SimpleStringProperty name = new SimpleStringProperty(this, "name");
        public final SimpleBooleanProperty value = new SimpleBooleanProperty(this, "value");
    }
}
