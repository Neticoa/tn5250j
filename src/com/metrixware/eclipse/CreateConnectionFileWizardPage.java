/**
 *
 */
package com.metrixware.eclipse;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.tn5250j.SslType;
import org.tn5250j.TN5250jConstants;

import com.metrixware.tn5250.config.EclipseSessionConfig;
import com.metrixware.tn5250.config.PropertiesFileConfigure;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CreateConnectionFileWizardPage extends AbstractConnectionWizardPage {
    private static final String EXTENSION = ".5250";

    private IContainer folder;
    private IFile file;

    private Composite parent;
    private Text fileName;

    protected CreateConnectionFileWizardPage(final IContainer folder) {
        super("createConnectionFile");
        this.folder = folder;
    }

    @Override
    protected void addComponents() {
        fileName = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardFileName), createText());
        setSizeInSumbols(fileName, 12);

        updateEnablement();
    }

    @Override
    protected void updateEnablement() {
        file = null;
        final boolean valid = isNotEmpty(fileName);

        if (valid) {
            try {
                file = folder.getFile(Path.fromPortableString(possibleAddExtensioin(fileName.getText().trim())));
            } catch (final Exception e) {
            }
        }

        setPageComplete(file != null);
    }

    private String possibleAddExtensioin(final String str) {
        return str.endsWith(EXTENSION) ? str : str + EXTENSION;
    }

    @SuppressWarnings("deprecation")
    public void createConnectionFile(final ConnectionBean bean) throws IOException {
        final PropertiesFileConfigure cfgFactory = new PropertiesFileConfigure(file);

        final EclipseSessionConfig cfg = new EclipseSessionConfig(bean.getName(), cfgFactory);
        cfg.loadDefaults();
        putConnectionInfo(bean, cfg.getProperties());
        cfg.saveSessionProps();
    }

    /**
     * @param bean bean.
     * @param sesProps target properties.
     */
    private void putConnectionInfo(final ConnectionBean bean, final Map<String, String> sesProps) {
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
}
