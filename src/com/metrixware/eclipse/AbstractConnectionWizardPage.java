/**
 *
 */
package com.metrixware.eclipse;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractConnectionWizardPage extends WizardPage {

    protected Composite parent;

    public AbstractConnectionWizardPage(final String name) {
        super(name);
    }

    @Override
    public void createControl(final Composite container) {
        final Composite control = new Composite(container, SWT.BORDER);

        this.parent = new Composite(control, SWT.NONE);
        parent.setLayout(new GridLayout(2, false));

        addComponents();

        control.setLayout(new GridLayout(1, false));
        parent.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
        setControl(control);
    }

    protected Text createText() {
        final Text text = new Text(parent, SWT.BORDER);
        text.addListener(SWT.Modify, e -> updateEnablement());
        return text;
    }

    protected boolean containsIntValue(final Text textField) {
        if (!isNotEmpty(textField)) {
            return false;
        }

        try {
            Integer.parseInt(textField.getText());
            return true;
        } catch (final NumberFormatException e) {
        }
        return false;
    }

    protected boolean isAnySelected(final Combo combo) {
        final String text = combo.getText();
        return text != null && !text.isEmpty();
    }

    protected boolean isNotEmpty(final Text text) {
        return text.getText() != null && !text.getText().isEmpty();
    }

    protected Label createLabel(final String text) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    protected <T extends Control> T addLabelAndLayout(final Composite parent, final Label label, final T child) {
        final GridData firstRowData = new GridData();
        firstRowData.horizontalAlignment = SWT.LEFT;
        label.setLayoutData(firstRowData);

        final GridData secondRowData = new GridData();
        secondRowData.horizontalAlignment = SWT.LEFT;
        child.setLayoutData(secondRowData);

        return child;
    }

    protected void setSizeInSumbols(final Text text, final int numChars) {
        ((GridData) text.getLayoutData()).widthHint = UiUtils.getWidthForNumCharacterrs(text, numChars);
    }

    protected abstract void updateEnablement();

    protected abstract void addComponents();
}
