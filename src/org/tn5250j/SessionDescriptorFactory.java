/**
 *
 */
package org.tn5250j;

import java.util.Properties;

import com.metrixware.tn5250.connection.CodePage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SessionDescriptorFactory {
    private static final String SESSION_CONNECT_PROGRAM = "SESSION_CONNECT_PROGRAM";
    private static final String SESSION_CONNECT_MENU = "SESSION_CONNECT_MENU";
    private static final String SESSION_CONNECT_LIBRARY = "SESSION_CONNECT_LIBRARY";
    private static final String SESSION_CONNECT_PASSWORD = "SESSION_CONNECT_PASSWORD";
    private static final String SESSION_CONNECT_USER = "SESSION_CONNECT_USER";

    public static SessionDescriptor create(final String sessionName, final SessionConfig config, final Properties sesProps) {
        final SessionDescriptor d = new SessionDescriptor();

        d.setSessionName(sessionName);
        d.setHeartBeat(sesProps.containsKey(TN5250jConstants.SESSION_HEART_BEAT));
        d.setHostNameAsTermName(sesProps.getProperty(TN5250jConstants.SESSION_TERM_NAME_SYSTEM) != null);

        if (sesProps.get(TN5250jConstants.SSL_TYPE) != null) {
            final String sslType = (String) sesProps.get(TN5250jConstants.SSL_TYPE);
            try {
                d.setSslType(SslType.valueOf(sslType));
            } catch (final Exception e) {
            }
        }
        d.setEnhanced(sesProps.containsKey(TN5250jConstants.SESSION_TN_ENHANCED));
        d.setHost(sesProps.getProperty(TN5250jConstants.SESSION_HOST));
        d.setTerminal(calculateCompatibleTerminal(sesProps));

        final String proxyHost = sesProps.getProperty(TN5250jConstants.SESSION_PROXY_HOST);
        if (proxyHost != null && sesProps.containsKey(TN5250jConstants.SESSION_HOST_PORT)) {
            try {
                d.setProxy(new SessionProxy(proxyHost,
                        Integer.parseInt(sesProps.getProperty(TN5250jConstants.SESSION_HOST_PORT))));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (sesProps.containsKey(TN5250jConstants.SESSION_CODE_PAGE)) {
            d.setCodePage(findCodePage(sesProps.getProperty(TN5250jConstants.SESSION_CODE_PAGE)));
        }

        if (sesProps.containsKey(TN5250jConstants.SESSION_DEVICE_NAME)) {
            d.setDeviceName(sesProps.getProperty(TN5250jConstants.SESSION_DEVICE_NAME));
        }

        if (System.getProperties().containsKey(SESSION_CONNECT_USER)) {
            final ConnectUser user = new ConnectUser();
            d.setConnectUser(user);

            user.setUser(System.getProperties().getProperty(SESSION_CONNECT_USER));
            if (System.getProperties().containsKey(SESSION_CONNECT_PASSWORD)) {
                user.setPassword(System.getProperties().getProperty(SESSION_CONNECT_PASSWORD));
            }
            if (System.getProperties().containsKey(SESSION_CONNECT_LIBRARY)) {
                user.setLibrary(System.getProperties().getProperty(SESSION_CONNECT_LIBRARY));
            }
            if (System.getProperties().containsKey(SESSION_CONNECT_MENU)) {
                user.setInitialMenu(System.getProperties().getProperty(SESSION_CONNECT_MENU));
            }
            if (System.getProperties().containsKey(SESSION_CONNECT_PROGRAM)) {
                user.setProgram(System.getProperties().getProperty(SESSION_CONNECT_PROGRAM));
            }
        }

        return d;
    }

    /**
     * @param codePageProperty code page property.
     * @return code page.
     */
    private static CodePage findCodePage(final String codePageProperty) {
        for (final CodePage cp : CodePage.values()) {
            if (cp.getEncoding().equals(codePageProperty)) {
                return cp;
            }
        }
        return CodePage.DEFAULT;
    }

    /**
     * Attention!!! New version not supports the custom terminal type definition therefore
     * the compatiblee terminal type will detected and set.
     *
     * @param sesProps session properties.
     * @return compatible terminal.
     */
    private static Terminal calculateCompatibleTerminal(final Properties sesProps) {
        Terminal terminal = Terminal.IBM_3278_2;

        if (sesProps.containsKey(TN5250jConstants.SESSION_SCREEN_SIZE) && sesProps.getProperty(
                TN5250jConstants.SESSION_SCREEN_SIZE).equals(TN5250jConstants.SCREEN_SIZE_27X132_STR)) {
            terminal = Terminal.IBM_3278_5;
        }

        return terminal;
    }
}
