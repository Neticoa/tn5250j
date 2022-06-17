/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.gui;

import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class ActionDelegateDialogPane<T> extends DialogPane {
    protected Dialog<T> dialog;
    private Consumer<ActionDelegateDialogPane<T>> okListener;

    public ActionDelegateDialogPane(final Dialog<T> dialog, final Consumer<ActionDelegateDialogPane<T>> consumer) {
        this();
        setDialog(dialog);
        setOkListener(consumer);
    }
    protected ActionDelegateDialogPane() {
        super();

        getButtonTypes().add(ButtonType.OK);
        lookupButton(ButtonType.OK).addEventHandler(ActionEvent.ACTION, e -> okPressed());
    }

    public void setOkListener(final Consumer<ActionDelegateDialogPane<T>> okListener) {
        this.okListener = okListener;
    }

    protected void okPressed() {
        if (okListener != null) {
            try {
                okListener.accept(this);
            } catch (final Throwable exc) {
                exc.printStackTrace();
                finish(null);
            }
        }
    }

    protected void setDialog(final Dialog<T> dialog) {
        this.dialog = dialog;
    }

    @Override
    protected Node createButton(final ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            //add button without listeners
            final Button button = new Button(buttonType.getText());
            final ButtonData buttonData = buttonType.getButtonData();
            ButtonBar.setButtonData(button, buttonData);
            button.setDefaultButton(buttonType != null && buttonData.isDefaultButton());
            return button;
        } else {
            return super.createButton(buttonType);
        }
    }

    public void finish(final T result) {
        dialog.setResult(result);
        dialog.close();
    }
}
