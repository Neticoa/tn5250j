/**
 *
 */
package com.metrixware.eclipse.preferences;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.metrixware.eclipse.Activator;
import com.metrixware.eclipse.Messages;
import com.metrixware.eclipse.PluginUtils;
import com.metrixware.log4j.Logging;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GlobalPreferences extends PreferencePage implements IWorkbenchPreferencePage {

    private Combo logLevelCombo;
    private final List<Level> logLevels = new LinkedList<>();

    /**
     * Default constructor.
     */
    public GlobalPreferences() {
        super(Messages.PreferencesPageTitle, PluginUtils.createImageDescriptor("tn5250j-16x16.png")); //$NON-NLS-1$

        //create ordered log level list
        logLevels.add(Level.FATAL);
        logLevels.add(Level.ERROR);
        logLevels.add(Level.INFO);
        logLevels.add(Level.DEBUG);
        logLevels.add(Level.WARN);
        logLevels.add(Level.ALL);

        //log levels are comparable by int log level. It is ok
        Collections.sort(logLevels);
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite control = new Composite(parent, SWT.NONE);
        control.setLayout(new GridLayout(2, false));

        //Log level
        final Label label = new Label(control, SWT.NONE);
        label.setText(Messages.PreferencesPageLogLevelLabel);

        this.logLevelCombo = new Combo(control, SWT.NONE | SWT.READ_ONLY);
        for (final Level level : logLevels) {
            logLevelCombo.add(getLogLevelLabel(level));
        }
        resetLogLevel();

        PluginUtils.addLabelAndLayout(control, label, logLevelCombo);

        return control;
    }

    private static String getLogLevelLabel(final Level level) {
        if (level == Level.FATAL) {
            return Messages.PreferencesPageLogLevelFatal;
        }
        if (level == Level.ERROR) {
            return Messages.PreferencesPageLogLevelError;
        }
        if (level == Level.INFO) {
            return Messages.PreferencesPageLogLevelInfo;
        }
        if (level == Level.DEBUG) {
            return Messages.PreferencesPageLogLevelDebug;
        }
        if (level == Level.WARN) {
            return Messages.PreferencesPageLogLevelWarn;
        }
        if (level == Level.ALL) {
            return Messages.PreferencesPageLogLevelAll;
        }

        throw new RuntimeException("Unexpected log level: " + level.name());
    }

    private void resetLogLevel() {
        logLevelCombo.select(logLevels.indexOf(Logging.getInstance().getCurrentLogLevel()));
    }

    @Override
    protected void performApply() {
        super.performApply();

        final Level level = logLevels.get(logLevelCombo.getSelectionIndex());
        Logging.getInstance().setCurrentLogLevel(level);
        try {
            Activator.getInstance().getPreferences().flush();
        } catch (final BackingStoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean performCancel() {
        resetLogLevel();
        return super.performCancel();
    }
}
