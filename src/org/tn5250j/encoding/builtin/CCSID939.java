/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.encoding.builtin;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public final class CCSID939 extends AbstractConvTableCodePageConverter {

    public final static String NAME = "939";
    public final static String DESCR = "Japanese Latin";

    public CCSID939() {
        super("Cp939");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCR;
    }

    public String getEncoding() {
        return NAME;
    }
}
