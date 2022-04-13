/**
 *
 */
package com.metrixware.eclipse.wizard;

import static com.metrixware.eclipse.PluginUtils.addLabelAndLayout;

import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.metrixware.eclipse.ConnectionBean;
import com.metrixware.eclipse.Messages;
import com.metrixware.tn5250.config.PropertiesFileConfigure;
import com.metrixware.tn5250.config.SessionConfig;

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

    public void createConnectionFile(final ConnectionBean bean) throws IOException {
        final PropertiesFileConfigure cfgFactory = new PropertiesFileConfigure(file);

        final SessionConfig cfg = new SessionConfig(cfgFactory);
        cfg.loadDefaults();
        cfg.setConnectionInfo(bean);
        cfg.saveSessionProps();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (visible) {
            fileName.setFocus();
        }
    }
}
