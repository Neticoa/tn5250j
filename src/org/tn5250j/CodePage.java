/**
 *
 */
package org.tn5250j;

/**
 * IBM code pages source https://www.ibm.com/docs/en/cics-tg-zos/9.2?topic=reference-code-pages
 * https://en.wikipedia.org/wiki/Code_page
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */

public enum CodePage {
    ARABIC("CP864"),
    AUSTRIA("CP1141"),
    BELARUS("CP1025"),
    BELGIUM("CP1140"),
    BOSNIA_HERZEGOVINA("CP870"),
    BRAZIL("CP1140"),
    BULGARIA("CP1025"),
    CANADA("CP1140"),
    CROATIA("CP870"),
    CZECH("CP870"),
    DEFAULT("CP1147"),
    DENMARK("CP277"),
    ESTONIA("CP1142"),
    FINLAND("CP1143"),
    FRANCE("CP1147"),
    GERMANY("CP1141"),
    GREECE("CP875"),
    HUNGARY("CP870"),
    ICELAND("CP1149"),
    ISRAEL_NEW("CP862"),
    ITALY("CP1144"),
    JAPAN_ENGLISH("CP939"),
    JAPAN_KATAKANA("CP930"),
    KOREA("CP949"),
    LATIN_AMERICA("CP1145"),
    LATVIA("CP921"),
    LITHUANIA("CP921"),
    MACEDONIA("CP1025"),
    MULTILINGUAL("CP1148"),
    NETHERLANDS("CP1140"),
    NORWAY("CP277"),
    POLAND("CP870"),
    PORTUGAL("CP1140"),
    CHINA("Big5"),
    ROMANIA("CP870"),
    RUSSIA("CP1025"),
    SERBIA_MONTEGRO("CP1025"),
    SLOVAKIA("CP870"),
    SLOVENIA("CP870"),
    SPAIN("CP1145"),
    SWEDEN("CP278"),
    THAI("CP874"),
    TURKEY("CP857"),
    UKRAINE("CP1123"),
    UNITED_KINGDOM("CP1146"),
    US("CP1140"),
    SWISS("CP500");

    private final String encoding;

    CodePage(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return encoding
     */
    public String getEncoding() {
        return encoding;
    }
    
    @Override
    public String toString() {
    	return this.name() + "-" + encoding;
    }
}
