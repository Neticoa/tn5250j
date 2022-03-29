/*
 * @(#)Log4jLogger.java
 * @author  Kenneth J. Pouncey
 *
 * Copyright:    Copyright (c) 2001, 2002, 2003
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
package org.tn5250j.tools.logging;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the TN5250jLogger to provide log4j logger instances.
 */
public final class Log4jLogger implements TN5250jLogger {

    private Logger log = null;

    /*
     * Package level access only
     */
    Log4jLogger() {
    }

    public void initialize(final String clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void debug(String message, Throwable throwable) {
        log.debug(message, throwable);
    }

    public void info(String message) {
        log.info(message);
    }

    public void info(String message, Throwable throwable) {
        log.info(message, throwable);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    public void error(String message) {
        log.error(message);
    }

    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }


    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

	@Override
	public void setLevel(int newLevel) {
		// does nothing cause slf4J does not provide an API to set the log level
	}

}
