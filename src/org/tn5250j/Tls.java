/**
 *
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Tls {
    None("NONE"),
    V1_0("v1.0"),
    V1_1("v1.1"),
    V1_2("v1.2");

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
