/**
 *
 */
package com.metrixware.tn5250.config;

import java.io.IOException;

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
}
