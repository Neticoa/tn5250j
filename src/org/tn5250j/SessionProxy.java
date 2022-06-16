/**
 *
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class SessionProxy {

    private String host;
    private int port;

    /**
     * Default constructor.
     */
    public SessionProxy() {
        super();
    }
    /**
     * @param host proxy host.
     * @param port proxy port.
     */
    public SessionProxy(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
