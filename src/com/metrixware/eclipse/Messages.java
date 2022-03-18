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
    public static String CreateConnectionWizardTlsLabel;
    public static String CreateConnectionWizardPortLabel;
    public static String CreateConnectionWizardHostLabel;
    public static String CreateConnectionWizardIdleOutLabel;
    public static String CreateConnectionWizardFileName;

    public static String FailedToCreateConnection;
    public static String OnlyJavaProjectCanBeUsed;
    public static String NotValidParentSelected;
    public static String NewConnectionWizard;
    public static String DeviceLabel;

    static {
        // initialize resource bundle
        NLS.initializeMessages("messages", Messages.class); //$NON-NLS-1$
    }

    private Messages() {
    }
}
