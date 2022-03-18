/**
 *
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum SslType {
    None("NONE"),
    SSLv2("SSLv2"),
    SSLv3("SSLv3"),
    TLS("TLS");

    private String type;

    /**
     * @param displayName SSL type display name.
     */
    SslType(final String displayName) {
        this.type = displayName;
    }

    /**
     * @return SSL type display name.
     */
    public String getType() {
        return type;
    }
}
