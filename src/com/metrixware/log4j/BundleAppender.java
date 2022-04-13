package com.metrixware.log4j;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.SyslogLayout;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.metrixware.eclipse.Activator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Plugin(name = "BundleAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class BundleAppender extends AbstractAppender {

    public BundleAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
        super(name, filter, layout == null ? SyslogLayout.newBuilder().build() : layout, true, new Property[0]);
    }

    @PluginFactory
    public static BundleAppender createAppender(@PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") final Layout<? extends Serializable> layout) {
        return new BundleAppender(name, filter, layout);
    }

    /* (non-Javadoc)
     * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
     */
    @Override
    public void append(final LogEvent event) {
        if (getEclipseLog() == null) {
            return;
        }

        final Level level = event.getLevel();
        if (!accept(level)) {
            return;
        }

        final String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        final Throwable throwable = event.getThrown();

        if (level == Level.FATAL) {
            logFatal(message, throwable);
        } else if (level == Level.ERROR) {
            logError(message, throwable);
        } else if (level == Level.INFO) {
            logInfo(message, throwable);
        } else if (level == Level.DEBUG) {
            logDebug(message, throwable);
        } else if (level == Level.WARN) {
            logWarn(message, throwable);
        } else {
            logWarn("Unexpected log message level " + level, null);
        }
    }

    private ILog getEclipseLog() {
        return Logging.getInstance().getEclipseLog();
    }

    private boolean accept(final Level level) {
        final Level currentLevel = Logging.getInstance().getCurrentLogLevel();
        return currentLevel.intLevel() >= level.intLevel();
    }

    private void logDebug(final String message, final Throwable throwable) {
        logInfo(message, throwable);
    }

    private void logInfo(final String message, final Throwable throwable) {
        getEclipseLog().log(createStatus(IStatus.INFO, message, throwable));
    }

    private void logWarn(final String message, final Throwable throwable) {
        getEclipseLog().log(createStatus(IStatus.WARNING, message, throwable));
    }

    private void logError(final String message, final Throwable throwable) {
        getEclipseLog().log(createStatus(IStatus.ERROR, message, throwable));
    }

    private void logFatal(final String message, final Throwable throwable) {
        logError(message, throwable);
    }

    /**
     * @param code status code.
     * @param message message.
     * @param throwable throwable
     * @return status.
     */
    private IStatus createStatus(final int code, final String message, final Throwable throwable) {
        return new Status(code, Activator.BUNDLE_SYMBOLIC_NAME, message, throwable);
    }
}
