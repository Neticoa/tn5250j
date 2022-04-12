package com.metrixware.tn5250.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.tn5250j.Session5250;
import org.tn5250j.SessionGuiFactory;
import org.tn5250j.SessionPanel;
import org.tn5250j.framework.common.AbstractSessionManager;

import com.metrixware.eclipse.Activator;
import com.metrixware.tn5250.config.SessionConfig;

import javafx.application.Platform;
import javafx.scene.Scene;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("restriction")
public class SessionManager extends AbstractSessionManager {
    private Map<SessionConfig, SessionPanel> uis = new ConcurrentHashMap<>();
    private boolean isFxInitialized;

    /**
     * Stops all sessions.
     */
    public void stop() {
        getSessions().dispose();
    }

    /**
     * @param config session configuration.
     * @param container container.
     * @return created canvas component.
     */
    public Composite addSessionComponent(final SessionConfig config, final Composite container) {
        final Composite canvas = createCanvas(container);

        if (!isFxInitialized) {
            Platform.setImplicitExit(false);
            isFxInitialized = true;
        }

        @SuppressWarnings("deprecation")
        final Session5250 session = Activator.getInstance().getSessionManager().createSession(config.getSessionName(),
                config.getProperties(), config);
        uis.put(config, SessionGuiFactory.createGui(session));

        setScene(canvas, new Scene(uis.get(config)));
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        return canvas;
    }

    /**
     * @param config session configuration.
     */
    public void connectSession(final SessionConfig config) {
        final SessionPanel ui = uis.get(config);
        if (ui != null && !ui.isVtConnected()) {
            ui.connect();
        }
    }

    /**
     * @param config session configuration.
     */
    public void closeSession(final SessionConfig config) {
        final SessionPanel ui = uis.get(config);
        if (ui != null) {
            ui.closeDown();
            uis.remove(config);
        }
    }

    /**
     * @param canvas canvas component.
     */
    public void setFocusToFxComponent(final Composite canvas) {
        try {
            final Scene scene = (Scene) canvas.getClass().getMethod("getScene").invoke(canvas);
            scene.getRoot().requestFocus();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to set focus to scene", e);
        }
    }

    /**
     * @param canvas canvas component.
     * @param scene scene to set.
     */
    private void setScene(final Composite canvas, final Scene scene) {
        try {
            canvas.getClass().getMethod("setScene", Scene.class).invoke(canvas, scene);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to set scene to canvas", e);
        }
    }

    /**
     * @param container container of canvas.
     * @return
     */
    private Composite createCanvas(final Composite container) {
        try {
            final Class<?> clazz = Activator.getInstance().getJfxSwtLoader().loadClass(
                    "javafx.embed.swt.FXCanvas");
            final Class<?>[] params = {Composite.class, int.class};
            return (Composite) clazz.getConstructor(params).newInstance(container, SWT.NONE);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create JavaFX canvas");
        }
    }
}
