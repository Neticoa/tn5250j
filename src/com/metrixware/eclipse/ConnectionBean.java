/**
 *
 */
package com.metrixware.eclipse;

import org.tn5250j.Terminal;
import org.tn5250j.TlsVersion;

import com.metrixware.tn5250.connection.CodePage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ConnectionBean {

    private String host;
    private int port;
    private TlsVersion tls;
    private Terminal terminal;
    private CodePage codePage;
    private String name;
    private String device;
    private int timeOut;
    private int idleOut;

    /**
     * @param host connection host.
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * @return connection host.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param port port.
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @return port.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param tls TLS version.
     */
    public void setTls(final TlsVersion tls) {
        this.tls = tls;
    }

    /**
     * @return TLS version.
     */
    public TlsVersion getTls() {
        return tls;
    }

    /**
     * @param terminal terminal type.
     */
    public void setTerminal(final Terminal terminal) {
        this.terminal = terminal;
    }

    /**
     * @return terminal type.
     */
    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * @param codePage code page.
     */
    public void setCodePage(final CodePage codePage) {
        this.codePage = codePage;
    }

    /**
     * @return code page.
     */
    public CodePage getCodePage() {
        return codePage;
    }

    /**
     * @param name session name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return session name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param device device name.
     */
    public void setDevice(final String device) {
        this.device = device;
    }

    /**
     * @return device name.
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param timeOut time out.
     */
    public void setTimeOut(final int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * @return time out.
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * @param timeOut idle out.
     */
    public void setIdleOut(final int timeOut) {
        this.idleOut = timeOut;
    }

    /**
     * @return idle out
     */
    public int getIdleOut() {
        return idleOut;
    }
}
