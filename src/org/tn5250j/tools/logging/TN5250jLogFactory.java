/*
 * @(#)TN5250jLogFactory.java
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

import static java.lang.Integer.parseInt;
import static org.tn5250j.interfaces.ConfigureFactory.SESSIONS;
import static org.tn5250j.tools.logging.TN5250jLogger.INFO;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.tn5250j.interfaces.ConfigureFactory;

/**
 * An interface defining objects that can create Configure
 * instances.
 * <p>
 * The model for the HashMap implementation of loggers came from the POI project
 * thanks to Nicola Ken Barozzi (nicolaken at apache.org) for the reference.
 */
public final class TN5250jLogFactory {

    // map of TN5250jLogger instances, with classes as keys
    private static final Map<String, TN5250jLogger> _loggers = new HashMap<String, TN5250jLogger>();
    private static boolean log4j;
    private static String customLogger;
    private static int level = INFO;

    /*
     * Here we try to do a little more work up front.
     */
    static {
        try {
            initOrResetLogger();
        } catch (final Exception ignore) {
            // ignore
        }
    }

    static void initOrResetLogger() {
        final Map<String, String> props = ConfigureFactory.getInstance().getProperties(SESSIONS);
        level = parseInt(props.getOrDefault("emul.logLevel", Integer.toString(INFO)));

        customLogger = System.getProperty(TN5250jLogFactory.class.getName());
        if (customLogger == null) {
            try {
                Class.forName("org.apache.log4j.Logger");
                log4j = true;
            } catch (final Exception ignore) {
                // ignore
            }
        }
    }

    /**
     * Set package access only so we have to use getLogger() to return a logger object.
     */
    TN5250jLogFactory() {

    }

    /**
     * @return An instance of the TN5250jLogger.
     */
    public static TN5250jLogger getLogger(final Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * @return An instance of the TN5250jLogger.
     */
    public static TN5250jLogger getLogger(final String clazzName) {
        TN5250jLogger logger = null;

        if (_loggers.containsKey(clazzName)) {
            logger = _loggers.get(clazzName);
        } else {

            if (customLogger != null) {
                try {

                    final Class<?> classObject = Class.forName(customLogger);
                    final Object object = classObject.newInstance();
                    if (object instanceof TN5250jLogger) {
                        logger = (TN5250jLogger) object;
                    }
                } catch (final Exception ex) {
                    // ignore
                }
            } else {
                if (log4j) {
                    logger = new Log4jLogger();
                } else {
                    // take the default logger.
                    logger = new ConsoleLogger();
                }
                logger.initialize(clazzName);
                logger.setLevel(level);
                _loggers.put(clazzName, logger);
            }
        }
        return logger;
    }

    public static boolean isLog4j() {
        return log4j;
    }

    public static void setLogLevels(final int newLevel) {
        if (level != newLevel) {
            level = newLevel;
            final Set<String> loggerSet = _loggers.keySet();
            final Iterator<String> loggerIterator = loggerSet.iterator();
            while (loggerIterator.hasNext()) {
                final TN5250jLogger logger = _loggers.get(loggerIterator.next());
                logger.setLevel(newLevel);
            }
        }
    }

}
