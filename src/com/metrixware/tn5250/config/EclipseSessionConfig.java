/**
 *
 */
package com.metrixware.tn5250.config;

import org.tn5250j.AbstractSessionConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EclipseSessionConfig extends AbstractSessionConfig {
    private final PropertiesFileConfigure configurer;

    public EclipseSessionConfig(final String sessionName, final PropertiesFileConfigure configurer) {
        super(sessionName);
        this.configurer = configurer;
    }

    @Override
    public void saveSessionPropsOnClose() {
        //Not save on close
    }

    @Override
    public void saveSessionProps() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void loadConfigurationResource() {
        configurer.reloadSettings();
//        this.sesProps = configurer.get
    }

}
