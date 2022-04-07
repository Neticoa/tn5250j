package com.metrixware.eclipse.editor;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.metrixware.eclipse.Activator;
import com.metrixware.tn5250.config.PropertiesFileConfigure;
import com.metrixware.tn5250.config.SessionConfig;
import com.metrixware.tn5250.session.SessionManager;

public class Tn5250Screen extends EditorPart {

    private SessionConfig config;
    private Composite canvas;
    private final AtomicBoolean partOpened = new AtomicBoolean();

    public Tn5250Screen() {
        super();
        Activator.initializeFx();
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        init(site, (IFileEditorInput) input);
    }

    private void init(final IEditorSite site, final IFileEditorInput input) throws PartInitException {
        final PropertiesFileConfigure configurator = new PropertiesFileConfigure(input.getFile());
        configurator.reloadSettings();

        this.config = new SessionConfig(configurator);

        setInput(input);
        setSite(site);
        setPartName(input.getName());
        setTitleToolTip(input.getToolTipText());
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(final Composite container) {
        final GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);

        this.canvas = getSessionManager().addSessionComponent(config, container);
        getSite().getPage().addPartListener(listener);
    }

    private SessionManager getSessionManager() {
        return Activator.getInstance().getSessionManager();
    }

    @Override
    public void setFocus() {
        canvas.forceFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        try {
            getSite().getPage().removePartListener(listener);
        } finally {
            super.dispose();
        }
    }

    void openSession() {
        getSessionManager().connectSession(config);
    }

    void closeSession() {
        getSessionManager().closeSession(config);
    }

    private boolean isMe(final IWorkbenchPartReference partRef) {
        return partRef.getPart(false) == this;
    }

    IPartListener2 listener = new IPartListener2() {
        @Override
        public void partVisible(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partOpened(final IWorkbenchPartReference partRef) {
            if (!isMe(partRef) || partOpened.getAndSet(true)) {
                return;
            }

            openSession();
            setFocus();
        }

        @Override
        public void partInputChanged(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partHidden(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partDeactivated(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partClosed(final IWorkbenchPartReference partRef) {
            if (!isMe(partRef)) {
                return;
            }
            closeSession();
        }

        @Override
        public void partBroughtToTop(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partActivated(final IWorkbenchPartReference partRef) {
        }
    };
}
