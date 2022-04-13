/**
 *
 */
package com.metrixware.log4j;

import org.apache.logging.log4j.Level;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.Bundle;

import com.metrixware.eclipse.Activator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Logging {

    private static final String LOGGING_LEVEL_PREFERENCE = "loggingLevel";
    private static final Logging INSTANCE = new Logging();

    private ILog eclipseLog;
    private Level currentLogLevel;

    private Logging() {
    }

    /**
     * @return instance of logger support.
     */
    public static Logging getInstance() {
        return INSTANCE;
    }
    /**
     * @return current log level.
     */
    public Level getCurrentLogLevel() {
        return currentLogLevel;
    }

    /**
     * @return eclipse logger.
     */
    public ILog getEclipseLog() {
        return eclipseLog;
    }

    /**
     * @param level new log level.
     */
    public void setCurrentLogLevel(final Level level) {
        currentLogLevel = level;
        Activator.getInstance().getPreferences().put(LOGGING_LEVEL_PREFERENCE, level.name());
    }

    public void initialize(final Bundle bundle) {
        final IEclipsePreferences preferences = Activator.getInstance().getPreferences();

        currentLogLevel = Level.valueOf(preferences.get(
                LOGGING_LEVEL_PREFERENCE, Level.ERROR.name()));
        eclipseLog = Platform.getLog(bundle);
    }
}
