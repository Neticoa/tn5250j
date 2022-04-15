/**
 *
 */
package org.tn5250j;

/**
 * IBM code pages source https://www.ibm.com/docs/en/cics-tg-zos/9.2?topic=reference-code-pages
 * https://en.wikipedia.org/wiki/Code_page
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum CodePage {
    ARABIC("Cp864"),
    AUSTRIA("Cp273"),
    BELARUS("Cp1131"),
    BELGIUM("Cp500"),
    BOSNIA_HERZEGOVINA("Cp1025"),
    BRAZIL("Cp037"),
    BRAZIL_EURO("Cp1140"),
    BULGARIA("Cp1025"),
    CANADA("Cp037"),
    CROATIA("Cp1025"),
    CZECH("Cp895"),
    DEFAULT("Cp351"),
    DENMARK("Cp277"),
    ESTONIA("Cp1122"),
    FINLAND("Cp278"),
    FRANCE("Cp297"),
    GERMANY("Cp273"),
    GREECE("Cp869"),
    HUNGARY("Cp254"),
    ICELAND("Cp871"),
    ISRAEL_NEW("Cp862"),
    ITALY("Cp280"),
    JAPAN_ENGLISH("Cp939"),
    JAPAN_KATAKANA("Cp930"),
    KOREA("Cp949"),
    LATIN_AMERICA("Cp284"),
    LATVIA("Cp921"),
    LITHUANIA("Cp921"),
    MACEDONIA("Cp1025"),
    MULTILINGUAL("Cp870"),
    MULTILINGUAL_EURO("Cp1153"),
    //MULTILINGUAL_ISO_EURO("Cp"), not found
    NETHERLANDS("Cp037"),
    NORWAY("Cp277"),
    POLAND("Cp252"),
    PORTUGAL("Cp037"),
    CHINA("Big5"),
    ROMANIA("Cp035"),
    RUSSIA("Cp866"),
    SERBIA_MONTEGRO("Cp1025"),
    SLOVAKIA("Cp032"),
    SLOVENIA("Cp1025"),
    SPAIN("Cp284"),
    SWEDEN("Cp278"),
    THAI("Cp874"),
    TURKEY("Cp857"),
    UKRAINE("Cp1123"),
    UNITED_KINGDOM("Cp285"),
    US("Cp437"),
    SWISS("Cp500");

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
}
