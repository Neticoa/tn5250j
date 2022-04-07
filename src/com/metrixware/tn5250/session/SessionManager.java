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
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
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
        final FXCanvas canvas = new FXCanvas(container, SWT.NONE) {
            /* (non-Javadoc)
             * @see org.eclipse.swt.widgets.Control#forceFocus()
             */
            @Override
            public boolean forceFocus() {
                final boolean forceFocusResult = super.forceFocus();
                if (forceFocusResult) {
                    getScene().getRoot().requestFocus();
                }
                return forceFocusResult;
            }
        };

        if (!isFxInitialized) {
            Platform.setImplicitExit(false);
            isFxInitialized = true;
        }

        final Session5250 session = Activator.getInstance().getSessionManager().createSession(config.getSessionName(),
                config.getProperties(), config);
        uis.put(config, SessionGuiFactory.createGui(session));

        canvas.setScene(new Scene(uis.get(config)));
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
}
