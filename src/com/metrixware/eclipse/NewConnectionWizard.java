package com.metrixware.eclipse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NewConnectionWizard extends BasicNewResourceWizard {
    private static final String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature";
    public static final String WIZARD_ID = "com.metrixware.tn5250.newConnectionWizard"; //$NON-NLS-1$

    private CreateConnectionFileWizardPage createConnectionPage;
    private ConfigureConnectionWizardPage configureConnectionPage;

    private List<String> errors = new LinkedList<>();
    private IResource folder;

    public NewConnectionWizard() {
        super();
    }

    @Override
    public void addPages() {
        if (errors.isEmpty()) {
            createConnectionPage = new CreateConnectionFileWizardPage(folder);
            configureConnectionPage = new ConfigureConnectionWizardPage();
            addPage(configureConnectionPage);
            addPage(createConnectionPage);
        } else {
            addPage(new ErrorPage(errors));
        }
    }

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        final IResource folder = getFolder(selection);
        checkIsJavaProject(folder);
        this.folder = folder;

        super.init(workbench, selection);
        setWindowTitle(Messages.NewConnectionWizard);
    }

    /**
     * @param selection current selection.
     * @return target parent folder
     */
    private IResource getFolder(final IStructuredSelection selection) {
        IResource parent = null;

        final Iterator<?> it = selection.iterator();
        if (it.hasNext()) {
            final Object object = it.next();
            IResource selectedResource = Adapters.adapt(object, IResource.class);
            if (selectedResource != null) {
                if (selectedResource.getType() == IResource.FILE) {
                    selectedResource = selectedResource.getParent();
                }
                if (selectedResource.isAccessible()) {
                    parent = selectedResource;
                }
            }
        }

        if (parent == null) {
            errors.add(Messages.NotValidParentSelected);
        }

        return parent;
    }

    private void checkIsJavaProject(final IResource parent) {
        try {
            final IProjectDescription description = parent.getProject().getDescription();
            for (final String nature : description.getNatureIds()) {
                if (JAVA_NATURE_ID.equals(nature)) {
                    return;
                }
            }
        } catch (final CoreException e) {
            errors.add(e.getMessage());
        }

        errors.add(Messages.OnlyJavaProjectCanBeUsed);
    }

    @Override
    protected void initializeDefaultPageImageDescriptor() {
        final ImageDescriptor desc = ResourceLocator.imageDescriptorFromBundle(
                "tn5250j", "/images/tn5250j-wizard-48x48.png").orElse(null);//$NON-NLS-1$ //$NON-NLS-2$
        setDefaultPageImageDescriptor(desc);
    }

    @Override
    public boolean performFinish() {
        //TODO get connection definition from forst page
        //and create folder by second page.
        return true;
    }
}
