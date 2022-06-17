/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j;

import java.util.List;
import java.util.Map;

import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.tn5250.Screen5250Facade;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.keyboard.actions.EmulatorAction;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public interface SessionGui {
    /**
     * @return alloc device name.
     */
    String getAllocDeviceName();
    /**
     * @return session name.
     */
    String getSessionName();
    /**
     * @return host name.
     */
    String getHostName();
    /**
     * @return session.
     */
    Session5250 getSession();
    /**
     * @return true if is visible.
     */
    boolean isVisible();
    /**
     * closes session.
     */
    void closeDown();
    /**
     * @return true if is enabled.
     */
    boolean isEnabled();
    /**
     * @param l session listener.
     */
    void addSessionListener(SessionListener l);
    /**
     * @param l session listener.
     */
    void removeSessionListener(SessionListener l);
    /**
     * @param l session jump listener.
     */
    void addSessionJumpListener(SessionJumpListener l);
    /**
     * @param l session jump listener
     */
    void removeSessionJumpListener(SessionJumpListener l);
    /**
     * Requests resizing.
     */
    void resizeMe();
    /**
     * @return true if is connected
     */
    boolean isVtConnected();
    void actionAttributes();
    boolean confirmCloseSession(boolean b);
    void actionCopy();
    void toggleDebug();
    tnvt getVT();
    void sendScreenEMail();
    Screen5250Facade getScreen();
    void toggleHotSpots();
    void startNewSession();
    void toggleConnection();
    void nextSession();
    void prevSession();
    void printMe();
    void crossHair();
    void setMacroRunning(boolean b);
    void getFocusForMe();
    void startDuplicateSession();
    void executeMacro(String lastKeyStroke);
    void actionSpool();
    void doKeyBoundArea(String lastKeyStroke);
    Dimension2D getDrawingSize();
    RubberBand getRubberband();
    void setDefaultCursor();
    void setWaitCursor();
    void requestFocus();
    boolean isSessionRecording();
    void stopRecordingMe();
    int getPosFromView(double x, double y);
    Rectangle2D getBoundingArea();
    boolean isMacroRunning();
    void setStopMacroRequested();
    void startRecordingMe();
    List<Double> sumThem(boolean which);
    void connect();
    Point2D translateStart(Point2D start);
    Point2D translateEnd(Point2D end);
    Window getWindow();

    KeyboardHandler getKeyHandler();
    void addEmulatorActionListener(EmulatorActionListener listener);
    void addKeyAction(KeyCodeCombination ks, EmulatorAction emulatorAction);
    void clearKeyActions();
    EmulatorAction getKeyAction(KeyEvent e);
    Map<KeyCodeCombination, EmulatorAction> getKeyActions();
}
