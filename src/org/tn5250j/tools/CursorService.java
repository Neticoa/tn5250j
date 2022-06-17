/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.tools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class CursorService {
    private final CopyOnWriteArrayList<WeakReference<Runnable>> cursors = new CopyOnWriteArrayList<>();
    private static final CursorService instance = new CursorService();

    private final Timeline timer = new Timeline(new KeyFrame(
        Duration.millis(500l),
        this::processTimeAction
    ));

    private CursorService() {
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    public static CursorService getInstance() {
        return instance;
    }

    public void addCursor(final Runnable cursor) {
        cursors.add(new WeakReference<>(cursor));
    }

    public void removeCursor(final Runnable cursor) {
        for (final WeakReference<Runnable> ref : cursors) {
            if (ref.get() == cursor) {
                cursors.remove(ref);
                break;
            }
        }
    }

    private void processTimeAction(final ActionEvent e) {
        final List<WeakReference<Runnable>> cursors = new ArrayList<>(this.cursors);
        for (final WeakReference<Runnable> ref : cursors) {
            final Runnable cursor = ref.get();
            if (cursor == null) {
                this.cursors.remove(ref);
            } else {
                //next blink
                cursor.run();
            }
        }
    }

    public void stop() {
        timer.stop();
    }
}
