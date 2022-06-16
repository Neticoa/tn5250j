/**
 *
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public enum Tls {
    None("NONE"),
    V1_0("TLSv1.0"),
    V1_1("TLSv1.1"),
    V1_2("TLSv1.2");

    private String type;

    /**
     * @param displayName SSL type display name.
     */
    Tls(final String displayName) {
        this.type = displayName;
    }

    /**
     * @return SSL type display name.
     */
    public String getType() {
        return type;
    }
}
