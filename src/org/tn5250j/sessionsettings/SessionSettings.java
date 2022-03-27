package org.tn5250j.sessionsettings;
/*
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

import java.io.IOException;

import org.tn5250j.AbstractSessionConfig;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SessionSettings extends DialogPane {
    private BorderPane jpm = new BorderPane();

    private final AbstractSessionConfig changes;

    private TreeView<AbstractAttributesController> tree = new TreeView<>();
    private final Stage parent;

    public SessionSettings(final Stage parent, final AbstractSessionConfig config) {
        super();
        this.parent = parent;
        getButtonTypes().addAll(ButtonType.YES, ButtonType.APPLY, ButtonType.CANCEL);

        parent.getScene().getRoot().setCursor(Cursor.WAIT);

        changes = config;

        jbInit();
        parent.getScene().getRoot().setCursor(Cursor.DEFAULT);
    }

    /**
     * Component initialization
     */
    private void jbInit() {

        // define default
        final StackPane jp = new StackPane();

        //Create the nodes.
        tree.setShowRoot(false);
        tree.setRoot(new TreeItem<AbstractAttributesController>(null));
        tree.getRoot().setExpanded(true);
        createNodes(jp);

        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.getSelectionModel().selectedItemProperty().addListener((src, old, value) -> treeSelectionChanged(value));

        // define tree selection panel
        final BorderPane jsp = new BorderPane(tree);
        UiUtils.setBackground(jsp, Color.WHITE);

        jpm.setLeft(jsp);
        jpm.setRight(jp);

        setContent(jpm);
        tree.getSelectionModel().selectFirst();
    }

    private void treeSelectionChanged(final TreeItem<AbstractAttributesController> item) {
        item.getValue().getView().toFront();
    }

    private void createNodes(final StackPane top) {
        createNode(top, loadFromTemplate(new ColorAttributesController(changes), "/fxml/ColorAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new DisplayAttributesController(changes), "/fxml/DisplayAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new CursorAttributesController(changes), "/fxml/CursorAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new FontAttributesController(changes), "/fxml/FontAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new TabAttributesController(changes), "/fxml/TabAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new SignoffAttributesController(changes), "/fxml/SignoffAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new OnConnectAttributesController(changes), "/fxml/OnConnectAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new MouseAttributesController(changes), "/fxml/MouseAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new HotspotAttributesController(changes), "/fxml/HotspotAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new KeypadAttributesController(changes), "/fxml/KeypadAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new PrinterAttributesController(changes), "/fxml/PrinterAttributesPane.fxml"));
        createNode(top, loadFromTemplate(new ErrorResetAttributesController(changes), "/fxml/ErrorResetAttributesPane.fxml"));
    }

    private AbstractAttributesController loadFromTemplate(final AbstractAttributesController controller, final String tpl) {
        try {
            final FXMLLoader loader = UiUtils.createLoader(tpl);
            loader.setControllerFactory(cls -> controller);
            loader.load();
            return controller;
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load template: " + tpl, e);
        }
    }

    private void createNode(final StackPane top, final AbstractAttributesController controller) {
        final Region view = controller.getView();
        top.getChildren().add(view);

        final TreeItem<AbstractAttributesController> item = new TreeItem<>(controller);
        tree.getRoot().getChildren().add(item);
    }

    @Override
    protected Node createButton(final ButtonType buttonType) {
        Button button = (Button) super.createButton(buttonType);

        if (buttonType == ButtonType.APPLY) {
            //change returned button for remove closers
            button = new Button();
            button.setText(LangTool.getString("sa.optApply"));
        } else if (buttonType == ButtonType.CANCEL) {
            button.setText(LangTool.getString("sa.optCancel"));
        } else if (buttonType == ButtonType.YES) {
            button.setText(LangTool.getString("sa.optSave"));
        }

        return button;
    }

    public void showIt() {
        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(LangTool.getString("sa.title"));
        dialog.initOwner(parent);

        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.resultProperty().addListener((src, old, value) -> {
            SessionSettings.this.setCursor(Cursor.WAIT);
            try {
                doOptionStuff(value);
            } finally {
                SessionSettings.this.setCursor(Cursor.DEFAULT);
            }
        });

        dialog.setDialogPane(this);
        dialog.setResizable(true); //FIXME possible better to comment it
        Platform.runLater(dialog::show);
    }

    private void doOptionStuff(final ButtonType result) {
        if (result == ButtonType.APPLY) {
            applyAttributes();
        } else if (result == ButtonType.OK) {
            if (changes.isModified()) {
                changes.setModified(false);
            }
            changes.saveSessionProps();
        }
    }

    private void applyAttributes() {
        final ObservableList<TreeItem<AbstractAttributesController>> children = tree.getRoot().getChildren();
        for (final TreeItem<AbstractAttributesController> item : children) {
            item.getValue().applyAttributes();
        }

        changes.setModified(true);
    }
}
