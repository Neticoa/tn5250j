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

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SessionManager extends AbstractSessionManager {
    private Map<SessionConfig, SessionPanel> uis = new ConcurrentHashMap<>();

    /**
     * Stops all sessions.
     */
    public void stop() {
        getSessions().dispose();
    }

    /**
     * @param config session configuration.
     * @param container container.
     */
    public void addSessionComponent(final SessionConfig config, final Composite container) {
        final FXCanvas canvas = new FXCanvas(container, SWT.NONE);

        final Session5250 session = Activator.getInstance().getSessionManager().createSession(config.getSessionName(),
                config.getProperties(), config);
        uis.put(config, SessionGuiFactory.createGui(session));

        canvas.setScene(new Scene(uis.get(config)));
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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
        }
    }
}
