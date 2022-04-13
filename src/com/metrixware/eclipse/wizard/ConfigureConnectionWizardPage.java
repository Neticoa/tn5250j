/**
 *
 */
package com.metrixware.eclipse.wizard;

import static com.metrixware.eclipse.PluginUtils.addLabelAndLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.tn5250j.SslType;
import org.tn5250j.Terminal;

import com.metrixware.eclipse.ConnectionBean;
import com.metrixware.eclipse.Messages;
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
    private Combo sslType;
    //TERMINAL: Combo
    private Combo terminal;
    //CODEPAGE: Combobox
    private Combo codePage;
    //SESSION: Text Field
    private Text session;
    //DEVICE: Text Field
    private Text device;
    //TIMEOUT:Text Field
    private Text timeOut;
    //IDLEOUT: Text Field
    private Text idleOut;

    private Control lastFocused;

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
        port.setText("23");
        setSizeInSumbols(port, 5);

        //TSL: Combo
        sslType = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardSslTypeLabel), createCombo());
        pupulateSslType();

        //TERMINAL: Combo
        terminal = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardTerminalLabel), createCombo());
        populateTerminal();

        //CODEPAGE: Combobox
        codePage = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardCodePageLabel), createCombo());
        populateCodePage();

        //SESSION: Text Field
        session = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardSessionLabel), createText());

        //DEVICE: Text Field
        device = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardDeviceLabel), createText());
        setSizeInSumbols(device, 12);

        //TIMEOUT:Text Field
        timeOut = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardTimeOutLabel), createText());
        initTimeOut();

        //IDLEOUT: Text Field
        idleOut = addLabelAndLayout(parent, createLabel(Messages.CreateConnectionWizardIdleOutLabel), createText());
        initIdleOut();

        updateEnablement();
        listenComponentsFocus();
    }

    private void listenComponentsFocus() {
        lastFocused = host;

        final FocusListener listener = new FocusListener() {
            @Override
            public void focusLost(final FocusEvent e) {
                lastFocused = (Control) e.widget;
            }

            @Override
            public void focusGained(final FocusEvent e) {
            }
        };

        host.addFocusListener(listener);
        port.addFocusListener(listener);
        sslType.addFocusListener(listener);
        terminal.addFocusListener(listener);
        codePage.addFocusListener(listener);
        session.addFocusListener(listener);
        device.addFocusListener(listener);
        timeOut.addFocusListener(listener);
        idleOut.addFocusListener(listener);
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

        terminal.select(Terminal.IBM_5292_2.ordinal());
    }

    private void pupulateSslType() {
        for (final SslType t : SslType.values()) {
            sslType.add(t.getType());
        }

        sslType.select(SslType.None.ordinal());
    }

    private void populateCodePage() {
        for (final CodePage cp : CodePage.values()) {
            codePage.add(cp.name());
        }

        codePage.select(CodePage.FRANCE.ordinal());
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
                && isAnySelected(sslType)
                && isAnySelected(terminal)
                && isAnySelected(codePage)
                && isNotEmpty(session)
                && isNotEmpty(device)
                && containsIntValue(timeOut)
                && containsIntValue(idleOut);
        setPageComplete(isOk);
    }

    public ConnectionBean getConnection() {
        final ConnectionBean con = new ConnectionBean();
        con.setHost(host.getText());
        con.setPort(Integer.parseInt(port.getText()));
        con.setSslType(SslType.values()[sslType.getSelectionIndex()]);
        con.setTerminal(Terminal.values()[terminal.getSelectionIndex()]);
        con.setCodePage(CodePage.values()[codePage.getSelectionIndex()]);
        con.setName(session.getText());
        con.setDevice(device.getText());
        con.setTimeOut(Integer.parseInt(timeOut.getText()));
        con.setIdleOut(Integer.parseInt(idleOut.getText()));
        return con;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (visible) {
            lastFocused.setFocus();
        }
    }
}
