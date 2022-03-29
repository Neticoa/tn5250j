/*
 * @(#)TN5250jLogger.java
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

/**
 * An interface defining generic loggers.
 */
public interface TN5250jLogger {

    // debug levels - The levels work from lower to higher. The lower levels
    // will be activated by turning on a higher level
    public static final int DEBUG = 1;    // most verbose
    public static final int INFO = 2;
    public static final int WARN = 4;  // medium verbose, should be choosen for deployment
    public static final int ERROR = 8;
    public static final int FATAL = 16;
    public static final int OFF = 32;  // most silence

    /**
     * @param clazz
     */
    abstract public void initialize(final String clazz);

    /**
     * @param message
     */
    abstract public void debug(String message);

    /**
     * @param message
     * @param throwable
     */
    abstract public void debug(String message, Throwable throwable);

    abstract public void info(String message);

    /**
     * @param message
     * @param throwable
     */
    abstract public void info(String message, Throwable throwable);

    /**
     * @param message
     */
    abstract public void warn(String message);

    /**
     * @param message
     * @param throwable
     */
    abstract public void warn(String message, Throwable throwable);

    /**
     * @param message
     */
    abstract public void error(String message);

    /**
     * @param message
     * @param throwable
     */
    abstract public void error(String message, Throwable throwable);

    /**
     * @return
     */
    abstract public boolean isDebugEnabled();

    /**
     * @return
     */
    abstract public boolean isInfoEnabled();

    /**
     * @return
     */
    abstract public boolean isWarnEnabled();

    /**
     * @return
     */
    abstract public boolean isErrorEnabled();


    /**
     * Sets a new log level.
     *
     * @param newLevel
     * @throws IllegalArgumentException If the new level is not allowed
     */
    abstract public void setLevel(int newLevel);

}
