/**
 *
 */
package org.tn5250j.encoding.builtin;

import com.ibm.as400.access.ConversionMaps;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("unchecked")
public final class CCSID1399 extends AbstractConvTableCodePageConverter {

    public final static String NAME = "1399";
    public final static String DESCR = "JAPAN MIX EBCDIC";

    static {
        ConversionMaps.encodingCcsid_.put("Cp1399", "1399");
    }

    public CCSID1399() {
        super("Cp1399");
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
