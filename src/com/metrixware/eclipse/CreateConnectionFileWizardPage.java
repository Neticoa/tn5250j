/**
 *
 */
package com.metrixware.eclipse;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CreateConnectionFileWizardPage extends AbstractConnectionWizardPage {

    private IResource folder;
    private Composite parent;
    private Text fileName;

    protected CreateConnectionFileWizardPage(final IResource folder) {
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
        final boolean valid = isNotEmpty(fileName);
        if (valid) {
            setPageComplete(true);
        }
    }

    public void createConnectionFile() throws IOException {

    }
}
