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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.Session5250;
import org.tn5250j.SessionConfig;
import org.tn5250j.TN5250jConstants;


/**
 * The SessionManager is the central repository for access to all sessions.
 * The SessionManager contains a list of all Session objects available.
 */
public final class SessionManager extends AbstractSessionManager {

    static private final List<SessionConfig> configs = new CopyOnWriteArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
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


    public synchronized Session5250 openSession(final Map<String, String> sesProps, final String configurationResource
            , final String sessionName) {
        final SessionConfig useConfig = createConfiguration(sessionName, sesProps, configurationResource);
        return createSession(sessionName, sesProps, useConfig);
    }

    /**
     * @param sessionName
     * @param sesProps
     * @param configurationResource
     * @return
     */
    private SessionConfig createConfiguration(final String sessionName, final Map<String, String> sesProps,
            String configurationResource) {
        if (sessionName == null)
            sesProps.put(TN5250jConstants.SESSION_TERM_NAME, sesProps.get(TN5250jConstants.SESSION_HOST));
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
        return useConfig;
    }
}
