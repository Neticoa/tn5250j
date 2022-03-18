/**
 *
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum TlsVersion {
    V1_0("v1.0"),
    V1_1("v1.1"),
    V1_2("v1.2");

    private String name;

    /**
     * @param name protocol name
     */
    TlsVersion(final String name) {
        this.name = name;
    }

    /**
     * @return protocol name
     */
    public String getName() {
        return name;
    }
}
