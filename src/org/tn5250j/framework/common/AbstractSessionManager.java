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

import java.util.Map;

import org.tn5250j.AbstractSessionConfig;
import org.tn5250j.Session5250;
import org.tn5250j.SessionDescriptor;
import org.tn5250j.SessionDescriptorFactory;
import org.tn5250j.SessionGui;


/**
 * The SessionManager is the central repository for access to all sessions.
 * The SessionManager contains a list of all Session objects available.
 */
public abstract class AbstractSessionManager {

    private final Sessions sessions = new Sessions();

    /**
     * The constructor is made protected to allow overriding.
     */
    protected AbstractSessionManager() {
        super();
    }

    public Sessions getSessions() {
        return sessions;
    }

    public void closeSession(final SessionGui sesspanel) {
        sesspanel.closeDown();
        sessions.removeSession(sesspanel.getSession());
    }

    /**
     * @param sessionName session name.
     * @param sesProps session properties.
     * @param useConfig configuration.
     * @return session.
     */
    public Session5250 createSession(final String sessionName, final Map<String, String> sesProps, final AbstractSessionConfig useConfig) {
        final SessionDescriptor sessionDescriptor = SessionDescriptorFactory.create(sessionName, useConfig, sesProps);
        final Session5250 newSession = new Session5250(sessionDescriptor, useConfig);
        sessions.addSession(newSession);
        return newSession;
    }
}
