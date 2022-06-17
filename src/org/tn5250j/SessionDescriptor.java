/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class SessionDescriptor {

    private String sessionName;
    private boolean heartBeat;
    private boolean hostNameAsTermName;
    private Tls sslType = Tls.None;
    private boolean enhanced;
    private Terminal terminal;
    private String host;
    private int port = 23;
    private SessionProxy proxy;
    private CodePage codePage = CodePage.DEFAULT;
    private String deviceName;
    private ConnectUser connectUser;

    public SessionDescriptor() {
        super();
    }

    /**
     * @return session name.
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * @param name session name.
     */
    public void setSessionName(final String name) {
        this.sessionName = name;
    }

    /**
     * @param heartBeat if true hearbeat should be used.
     */
    public void setHeartBeat(final boolean heartBeat) {
        this.heartBeat = heartBeat;
    }

    /**
     * @return whether or not heartbeat should be used.
     */
    public boolean isHeartBeat() {
        return heartBeat;
    }

    /**
     * @param b should use host name as terminal name.
     */
    public void setHostNameAsTermName(final boolean b) {
        this.hostNameAsTermName = b;
    }

    /**
     * @return whether or not should use host name as terminal name.
     */
    public boolean isHostNameAsTermName() {
        return hostNameAsTermName;
    }

    /**
     * @param sslType the SSL type.
     */
    public void setSslType(final Tls sslType) {
        this.sslType = sslType;
    }

    /**
     * @return the SSL type.
     */
    public Tls getSslType() {
        return sslType;
    }

    /**
     * @param enhanced if true the session is enhanced.
     */
    public void setEnhanced(final boolean enhanced) {
        this.enhanced = enhanced;
    }

    /**
     * @return whether or not the session is enhanced.
     */
    public boolean isEnhanced() {
        return enhanced;
    }

    /**
     * @return host.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host.
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * @return terminal type.
     */
    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * @param terminal terminal type.
     */
    public void setTerminal(final Terminal terminal) {
        this.terminal = terminal;
    }

    /**
     * @return port.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port port.
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @return proxy settings.
     */
    public SessionProxy getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy.
     */
    public void setProxy(final SessionProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * @return code page.
     */
    public CodePage getCodePage() {
        return codePage;
    }

    /**
     * @param page code page.
     */
    public void setCodePage(final CodePage page) {
        this.codePage = page;
    }

    /**
     * @return device name.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @param deviceName device name.
     */
    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @param user session connect user.
     */
    public void setConnectUser(final ConnectUser user) {
        connectUser = user;
    }

    /**
     * @return session connect user.
     */
    public ConnectUser getConnectUser() {
        return connectUser;
    }
}
