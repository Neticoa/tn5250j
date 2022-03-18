/**
 *
 */
package com.metrixware.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.tn5250j.Terminal;
import org.tn5250j.TlsVersion;

import com.metrixware.tn5250.connection.CodePage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ConfigureConnectionWizardPage extends AbstractConnectionWizardPage {
    //HOST: Text
    private Text host;
    //PORT: Text
    private Text port;
    //TSL: Combo
    private Combo tls;
    //TERMINAL: Combo
    private Combo terminal;
    //CODEPAGE: Cmobobox
    private Combo codePage;
    //SESSION: Text Field
    private Text session;
    //DEVICE: Text Field
    private Text device;
    //TIMEOUT:Text Field
    private Text timeOut;
    //IDLEOUT: Text Field
    private Text idleOut;

    public ConfigureConnectionWizardPage() {
        super("configureConnection");
    }

    @Override
    protected void addComponents() {
        //HOST: Text
        host = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardHostLabel), createText());
        setSizeInSumbols(host, 12);

        //PORT: Text
        port = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardPortLabel), createText());
        setSizeInSumbols(port, 5);

        //TSL: Combo
        tls = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardTlsLabel), createCombo());
        pupulateTls();

        //TERMINAL: Combo
        terminal = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardTerminalLabel), createCombo());
        populateTerminal();

        //CODEPAGE: Cmobobox
        codePage = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardCodePageLabel), createCombo());
        populateCodePage();

        //SESSION: Text Field
        session = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardSessionLabel), createText());

        //DEVICE: Text Field
        device = addLabelAndLayout(parent, createLabel(Messages.DeviceLabel), createText());
        setSizeInSumbols(device, 12);

        //TIMEOUT:Text Field
        timeOut = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardTimeOutLabel), createText());
        initTimeOut();

        //IDLEOUT: Text Field
        idleOut = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardIdleOutLabel), createText());
        initIdleOut();

        updateEnablement();
    }

    @Override
    public void createControl(final Composite container) {
        final Composite control = new Composite(container, SWT.BORDER);

        this.parent = new Composite(control, SWT.NONE);
        parent.setLayout(new GridLayout(2, false));

    }

    private void initIdleOut() {
        idleOut.setText("0");
        setSizeInSumbols(idleOut, 5);
    }

    private void initTimeOut() {
        timeOut.setText("0");
        setSizeInSumbols(timeOut, 5);
    }

    private void populateTerminal() {
        for (final Terminal term : Terminal.values()) {
            terminal.add(term.getDescription());
        }

        terminal.select(0);
    }

    private void pupulateTls() {
        for (final TlsVersion t : TlsVersion.values()) {
            tls.add(t.getName());
        }

        tls.select(TlsVersion.V1_0.ordinal());
    }

    private void populateCodePage() {
        for (final CodePage cp : CodePage.values()) {
            codePage.add(cp.name());
        }

        codePage.select(CodePage.DEFAULT.ordinal());
    }

    private Combo createCombo() {
        final Combo combo = new Combo(parent, SWT.NONE | SWT.READ_ONLY);
        combo.addListener(SWT.Selection, e -> updateEnablement());
        return combo;
    }

    @Override
    protected void updateEnablement() {
        final boolean isOk =
                isNotEmpty(host)
                && containsIntValue(port)
                && isAnySelected(tls)
                && isAnySelected(terminal)
                && isAnySelected(codePage)
                && isNotEmpty(session)
                && isNotEmpty(device)
                && containsIntValue(timeOut)
                && containsIntValue(idleOut);
        setPageComplete(isOk);
    }
}
