/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.gui;

import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class GenericTn5250Frame {

    protected boolean packFrame = false;
    protected final Stage stage = new Stage();

    public GenericTn5250Frame() {
        super();
        stage.getIcons().addAll(GUIGraphicsUtils.getApplicationIcons());
    }

    public void centerStage() {
        if (packFrame) {
            stage.sizeToScene();
        }

        final Rectangle2D bounds = Screen.getPrimary().getBounds();

        final double w = Math.min(stage.getWidth(), bounds.getWidth());
        final double h = Math.min(stage.getHeight(), bounds.getHeight());
        stage.setWidth(w);
        stage.setHeight(h);

        stage.setX((bounds.getWidth() - w) / 2);
        stage.setY((bounds.getHeight() - h) / 2);
    }

    /**
     * @return width of stage
     */
    public double getWidth() {
        return stage.getWidth();
    }

    /**
     * @return height of stage
     */
    public double getHeight() {
        return stage.getHeight();
    }

    /**
     * @param width width
     * @param height height
     */
    public void setSize(final double width, final double height) {
        stage.setWidth(width);
        stage.setHeight(height);
    }

    /**
     * @param x x coordinate.
     * @param y y coordinate.
     */
    public void setLocation(final double x, final double y) {
        stage.setX(x);
        stage.setY(y);
    }

    /**
     * @return x coordinate.
     */
    public double getX() {
        return stage.getX();
    }

    /**
     * @return y coordinate.
     */
    public double getY() {
        return stage.getY();
    }

    /**
     * @return true if the scene is visible.
     */
    public boolean isVisible() {
        return stage != null && stage.isShowing();
    }

    /**
     * @param visible visibility state of frame.
     */
    public void setVisible(final boolean visible) {
        if (visible) {
            stage.show();
        } else {
            stage.hide();
        }
    }

    /**
     * @param cursor cursor to set.
     */
    public void setCursor(final Cursor cursor) {
        stage.getScene().getRoot().setCursor(cursor);
    }

    /**
     * @return window.
     */
    public Window getWindow() {
        return stage;
    }

    /**
     * closes the stage
     */
    public void dispose() {
        stage.close();
    }
}
