/**
 *
 */
package com.metrixware.eclipse;

import org.eclipse.osgi.util.NLS;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Messages extends NLS {
    public static String CreateConnectionWizardTimeOutLabel;
    public static String CreateConnectionWizardSessionLabel;
    public static String CreateConnectionWizardCodePageLabel;
    public static String CreateConnectionWizardTerminalLabel;
    public static String CreateConnectionWizardSslTypeLabel;
    public static String CreateConnectionWizardPortLabel;
    public static String CreateConnectionWizardHostLabel;
    public static String CreateConnectionWizardIdleOutLabel;
    public static String CreateConnectionWizardFileName;
    public static String CreateConnectionWizardDeviceLabel;

    public static String PreferencesPageTitle;
    public static String PreferencesPageLogLevelLabel;
    public static String PreferencesPageLogLevelFatal;
    public static String PreferencesPageLogLevelError;
    public static String PreferencesPageLogLevelInfo;
    public static String PreferencesPageLogLevelDebug;
    public static String PreferencesPageLogLevelWarn;
    public static String PreferencesPageLogLevelAll;

    public static String FailedToCreateConnection;
    public static String NotValidParentSelected;
    public static String NewConnectionWizard;

    //errors
    public static String ErrorTitleFailedToLoadSessings;
    public static String ErrorTitleFailedToSaveSessings;
    public static String ErrorTitleFailedToCreateConnection;
    public static String ErrorTitleFailedToInitializeFx;

    static {
        // initialize resource bundle
        NLS.initializeMessages("messages", Messages.class); //$NON-NLS-1$
    }

    private Messages() {
    }
}
