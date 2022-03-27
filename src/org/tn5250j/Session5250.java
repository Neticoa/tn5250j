/*
 * @(#)Session5250.java
 * Copyright:    Copyright (c) 2001
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.Screen5250Facade;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.FxProxyBuilder;
import org.tn5250j.gui.SystemRequestDialog;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ScanListener;
import org.tn5250j.tools.AsyncServices;

/**
 * A host session
 */
public class Session5250  {

    private int sessionType;
    protected SessionDescriptor sesProps;
    private final AbstractSessionConfig sesConfig;
    private tnvt vt;
    private final Screen5250Facade screen;
    private SessionGui guiComponent;

    private final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    private boolean scan; // = false;
    private final List<ScanListener> scanListeners = new CopyOnWriteArrayList<>();

    public Session5250(final SessionDescriptor props,
                       final AbstractSessionConfig config) {

        sesConfig = config;
        sesProps = props;

        screen = FxProxyBuilder.buildProxy(new Screen5250(), Screen5250Facade.class);
    }

    public AbstractSessionConfig getConfiguration() {
        return sesConfig;
    }

    public SessionManager getSessionManager() {
        return SessionManager.instance();
    }

    public boolean isConnected() {
        if (vt == null) {
            return false;
        }
        return vt.isConnected();
    }

    /**
     * @return true when SSL is used and socket is connected.
     * @see {@link tnvt#isSslSocket()}
     */
    public boolean isSslSocket() {
        if (this.vt != null) {
            return this.vt.isSslSocket();
        } else {
            return false;
        }
    }

    /**
     * @return true when SSL is configured but not necessary in use
     * @see {@link #isSslSocket()}
     */
    public boolean isSslConfigured() {
        return sesProps.getSslType() != SslType.None;
    }

    public boolean isSendKeepAlive() {
        return this.sesProps.isHeartBeat();
    }

    /**
     * @return true if configured, that the host name should be
     */
    public boolean isUseSystemName() {
        return sesProps.isHostNameAsTermName();
    }

    public SessionDescriptor getConnectionProperties() {
        return sesProps;
    }

    public void setGUI(final SessionGui gui) {
        guiComponent = gui;
    }

    public SessionGui getGUI() {
        return guiComponent;
    }

    public String getSessionName() {
        return sesProps.getSessionName();
    }

    public String getAllocDeviceName() {
        if (vt != null) {
            return vt.getAllocatedDeviceName();
        }
        return null;
    }

    public int getSessionType() {

        return sessionType;

    }

    public String getHostName() {
        return vt.getHostName();
    }

    public Screen5250Facade getScreen() {

        return screen;

    }

    public void signalBell() {
        UiUtils.beep();
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.SessionInterface#displaySystemRequest()
     */
    public String showSystemRequest() {
        final SystemRequestDialog sysreqdlg = new SystemRequestDialog();
        return sysreqdlg.show();
    }

    public void connect() {

        final tnvt vt = new tnvt(this, screen, sesProps.isEnhanced(), sesProps.getTerminal().getColumns() == 132);
        setVT(vt);

        if (sesProps.getProxy() != null) {
            vt.setProxy(sesProps.getProxy().getHost(), Integer.toString(sesProps.getProxy().getPort()));
        }
        if (sesProps.getSslType() != SslType.None) {
            vt.setSSLType(sesProps.getSslType().getType());
        }

        vt.setCodePage(sesProps.getCodePage().getEncoding());
        if (sesProps.getDeviceName() != null) {
            vt.setDeviceName(sesProps.getDeviceName());
        }

        // lets set this puppy up to connect within its own thread
        // now lets set it to connect within its own daemon thread
        //    this seems to work better and is more responsive than using
        //    invokelater
        AsyncServices.runTask(() -> vt.connect(sesProps.getHost(), sesProps.getPort()));
    }

    public void disconnect() {
        vt.disconnect();
    }

    // WVL - LDC : TR.000300 : Callback scenario from 5250
    protected void setVT(final tnvt v) {
        vt = v;
        screen.setVT(vt);
        if (vt != null)
            vt.setScanningEnabled(this.scan);
    }

    public tnvt getVT() {
        return vt;
    }

    // WVL - LDC : TR.000300 : Callback scenario from 5250

    /**
     * Enables or disables scanning.
     *
     * @param scan enables scanning when true; disables otherwise.
     * @see tnvt#setCommandScanning(boolean);
     * @see tnvt#isCommandScanning();
     * @see tnvt#scan();
     * @see tnvt#parseCommand();
     * @see scanned(String,String)
     */
    public void setScanningEnabled(final boolean scan) {
        this.scan = scan;

        if (this.vt != null)
            this.vt.setScanningEnabled(scan);
    }

    // WVL - LDC : TR.000300 : Callback scenario from 5250

    /**
     * Checks whether scanning is enabled.
     *
     * @return true if command scanning is enabled; false otherwise.
     * @see tnvt#setCommandScanning(boolean);
     * @see tnvt#isCommandScanning();
     * @see tnvt#scan();
     * @see tnvt#parseCommand();
     * @see scanned(String,String)
     */
    public boolean isScanningEnabled() {
        if (this.vt != null)
            return this.vt.isScanningEnabled();

        return this.scan;
    }

    // WVL - LDC : TR.000300 : Callback scenario from 5250

    /**
     * This is the callback method for the TNVT when sensing the action cmd
     * screen pattern (!# at position 0,0).
     * <p>
     * This is a thread safe method and will be called
     * from the TNVT read thread!
     *
     * @param command   discovered in the 5250 stream.
     * @param remainder are all the other characters on the screen.
     * @see tnvt#setCommandScanning(boolean);
     * @see tnvt#isCommandScanning();
     * @see tnvt#scan();
     * @see tnvt#parseCommand();
     * @see scanned(String,String)
     */
    public final void fireScanned(final String command, final String remainder) {
        for (final ScanListener listener : this.scanListeners) {
            listener.scanned(command, remainder);
        }
    }

    /**
     * @param listener
     */
    public final void addScanListener(final ScanListener listener) {
        scanListeners.add(listener);
    }

    /**
     * @param listener
     */
    public final void removeScanListener(final ScanListener listener) {
        scanListeners.remove(listener);
    }

    /**
     * Notify all registered listeners of the onSessionChanged event.
     *
     * @param state The state change property object.
     */
    public final void fireSessionChanged(final int state) {
        for (final SessionListener listener : this.sessionListeners) {
            final SessionChangeEvent sce = new SessionChangeEvent(this);
            sce.setState(state);
            listener.onSessionChanged(sce);
        }
    }

    /**
     * Add a SessionListener to the listener list.
     *
     * @param listener The SessionListener to be added
     */
    public final void addSessionListener(final SessionListener listener) {
        sessionListeners.add(listener);
    }

    /**
     * Remove a SessionListener from the listener list.
     *
     * @param listener The SessionListener to be removed
     */
    public final void removeSessionListener(final SessionListener listener) {
        sessionListeners.remove(listener);
    }
}
