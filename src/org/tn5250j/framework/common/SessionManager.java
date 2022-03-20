/*
 * @(#)SessionManager.java
 * Copyright:    Copyright (c) 2001 - 2004
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
package org.tn5250j.framework.common;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tn5250j.Session5250;
import org.tn5250j.SessionConfig;
import org.tn5250j.SessionDescriptor;
import org.tn5250j.SessionDescriptorFactory;
import org.tn5250j.SessionGui;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;


/**
 * The SessionManager is the central repository for access to all sessions.
 * The SessionManager contains a list of all Session objects available.
 */
public final class SessionManager {

    static private final Sessions sessions = new Sessions();
    static private final List<SessionConfig> configs = new CopyOnWriteArrayList<>();

    private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());
    /**
     * A handle to the unique SessionManager class
     */
    static private final SessionManager _instance = new SessionManager();

    /**
     * The constructor is made protected to allow overriding.
     */
    private SessionManager() {
        log.info("New session Manager initialized");
    }

    /**
     * @return The unique instance of this class.
     */
    static public SessionManager instance() {
        return _instance;
    }

    public Sessions getSessions() {
        return sessions;
    }

    public void closeSession(final SessionGui sesspanel) {

        sesspanel.closeDown();
        //TODO save
        sessions.removeSession(sesspanel.getSession());

    }

    public synchronized Session5250 openSession(final Properties sesProps, String configurationResource
            , final String sessionName) {

        if (sessionName == null)
            sesProps.put(TN5250jConstants.SESSION_TERM_NAME, sesProps.getProperty(TN5250jConstants.SESSION_HOST));
        else
            sesProps.put(TN5250jConstants.SESSION_TERM_NAME, sessionName);

        if (configurationResource == null) configurationResource = "";

        sesProps.put(TN5250jConstants.SESSION_CONFIG_RESOURCE, configurationResource);

        SessionConfig useConfig = null;
        for (final SessionConfig conf : configs) {
            if (conf.getSessionName().equals(sessionName)) {
                useConfig = conf;
            }
        }

        if (useConfig == null) {

            useConfig = new SessionConfig(configurationResource, sessionName);
            configs.add(useConfig);
        }

        final SessionDescriptor sessionDescriptor = SessionDescriptorFactory.create(sessionName, useConfig, sesProps);
        final Session5250 newSession = new Session5250(sessionDescriptor, useConfig);
        sessions.addSession(newSession);
        return newSession;
    }
}
