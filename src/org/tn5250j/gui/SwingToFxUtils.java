/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.gui;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.application.Platform;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class SwingToFxUtils {
    public static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    static {
        //init JavaFX
        initFx();
    }

    public static synchronized void initFx() {
        if (INITIALIZED.get()) {
            return;
        }
        //this line initializes JavaFX
        new javafx.embed.swing.JFXPanel();

        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        Platform.setImplicitExit(false);
        INITIALIZED.set(true);
    }
}
