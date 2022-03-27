/**
 *
 */
package com.metrixware.eclipse.wizard;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.metrixware.eclipse.Messages;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ErrorPage extends WizardPage {

    public ErrorPage(final List<String> errors) {
        super("errorPage");

        setTitle(Messages.FailedToCreateConnection);
        setMessage(String.join("\n", errors), ERROR);
        setPageComplete(false);
    }

    @Override
    public void createControl(final Composite parent) {
        // Add empty composite
        setControl(new Composite(parent, SWT.NONE));
    }
}
