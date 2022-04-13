package com.metrixware.eclipse.wizard;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.metrixware.eclipse.ConnectionBean;
import com.metrixware.eclipse.Messages;
import com.metrixware.eclipse.PluginUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NewConnectionWizard extends BasicNewResourceWizard {
    public static final String WIZARD_ID = "com.metrixware.tn5250.newConnectionWizard"; //$NON-NLS-1$

    private CreateConnectionFileWizardPage createConnectionPage;
    private ConfigureConnectionWizardPage configureConnectionPage;

    private List<String> errors = new LinkedList<>();
    private IContainer folder;

    public NewConnectionWizard() {
        super();
    }

    @Override
    public void addPages() {
        if (errors.isEmpty()) {
            createConnectionPage = new CreateConnectionFileWizardPage(folder);
            configureConnectionPage = new ConfigureConnectionWizardPage();
            addPage(createConnectionPage);
            addPage(configureConnectionPage);
        } else {
            addPage(new ErrorPage(errors));
        }
    }

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        final IContainer folder = getFolder(selection);
        this.folder = folder;

        super.init(workbench, selection);
        setWindowTitle(Messages.NewConnectionWizard);
    }

    /**
     * @param selection current selection.
     * @return target parent folder
     */
    private IContainer getFolder(final IStructuredSelection selection) {
        IContainer parent = null;

        final Iterator<?> it = selection.iterator();
        if (it.hasNext()) {
            final Object object = it.next();
            IResource selectedResource = Adapters.adapt(object, IResource.class);
            if (selectedResource != null) {
                if (selectedResource.getType() == IResource.FILE) {
                    selectedResource = selectedResource.getParent();
                }
                if (selectedResource.isAccessible() && selectedResource instanceof IContainer) {
                    parent = (IContainer) selectedResource;
                }
            }
        }

        if (parent == null) {
            errors.add(Messages.NotValidParentSelected);
        }

        return parent;
    }

    @Override
    protected void initializeDefaultPageImageDescriptor() {
        setDefaultPageImageDescriptor(PluginUtils.createImageDescriptor("tn5250j-wizard-48x48.png")); //$NON-NLS-1$
    }

    @Override
    public boolean performFinish() {
        final ConnectionBean bean = configureConnectionPage.getConnection();
        try {
            createConnectionPage.createConnectionFile(bean);
            return true;
        } catch (final IOException e) {
            MessageDialog.openError(getShell(),
                    Messages.ErrorTitleFailedToCreateConnection, e.getMessage());
        }
        return false;
    }
}
