/**
 *
 */
package com.metrixware.tn5250.config;

import java.io.IOException;

import org.tn5250j.AbstractSessionConfig;
import org.tn5250j.SslType;
import org.tn5250j.TN5250jConstants;

import com.metrixware.eclipse.ConnectionBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SessionConfig extends AbstractSessionConfig {

    private final PropertiesFileConfigure configurer;

    public SessionConfig(final PropertiesFileConfigure configurer) {
        super();
        this.configurer = configurer;
        this.sesProps = configurer.getProperties(PropertiesFileConfigure.SESSION);
    }

    @Override
    public void saveSessionPropsOnClose() {
        //Not save on close
    }

    @Override
    public void loadDefaults() throws IOException {
        super.loadDefaults();
    }

    @Override
    public void saveSessionProps() {
        configurer.saveSettings();
    }

    @Override
    public void loadConfigurationResource() {
        configurer.reloadSettings();
    }

    /**
     * @param bean connection bean.
     */
    public void setConnectionInfo(final ConnectionBean bean) {
        sesProps.put(TN5250jConstants.SESSION_TERM_NAME, bean.getName());
        sesProps.put(TN5250jConstants.SESSION_HOST, bean.getHost());
//        sesProps.put(TN5250jConstants.SESSION_TN_ENHANCED, bean.isEnhanced());
        sesProps.put(TN5250jConstants.SESSION_HOST_PORT, Integer.toString(bean.getPort()));
        sesProps.put(TN5250jConstants.SESSION_CODE_PAGE, bean.getCodePage().getEncoding());

        if (bean.getTerminal().getColumns() == 132) {
            sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE, TN5250jConstants.SCREEN_SIZE_27X132_STR);
        } else {
            sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE, TN5250jConstants.SCREEN_SIZE_24X80_STR);
        }

        // TODO proxy
//        if (isSpecified("-usp", args)) {
//
//            // socks proxy host argument
//            if (isSpecified("-sph", args)) {
//                sesProps.put(TN5250jConstants.SESSION_PROXY_HOST, getParm("-sph", args));
//            }
//
//            // socks proxy port argument
//            if (isSpecified("-spp", args))
//                sesProps.put(TN5250jConstants.SESSION_PROXY_PORT, getParm("-spp", args));
//        }

        // are we to use a ssl and if we are what type
        if (bean.getSslType() != SslType.None) {
            sesProps.put(TN5250jConstants.SSL_TYPE, bean.getSslType().getType());
        }

        // check if device name is specified
        sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME, bean.getDevice());
        //TODO may be need to configure by Wizard
        sesProps.put(TN5250jConstants.SESSION_HEART_BEAT, "1");
    }

    /* (non-Javadoc)
     * @see org.tn5250j.AbstractSessionConfig#getSessionName()
     */
    @Override
    public String getSessionName() {
        return getTerminalName();
    }
    public String getTerminalName() {
        return sesProps.get(TN5250jConstants.SESSION_TERM_NAME);
    }
}
