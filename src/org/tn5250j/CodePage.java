/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j;

/**
 * IBM code pages source https://www.ibm.com/docs/en/cics-tg-zos/9.2?topic=reference-code-pages
 * https://en.wikipedia.org/wiki/Code_page
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public enum CodePage {
    ARABIC("864", "CP864"),
    AUSTRIA("1141", "CP1141"),
    BELARUS("1025", "CP1025"),
    BELGIUM("1140", "CP1140"),
    BOSNIA_HERZEGOVINA("870", "CP870"),
    BRAZIL("1140", "CP1140"),
    BULGARIA("1025", "CP1025"),
    CANADA("1140", "CP1140"),
    CROATIA("870", "CP870"),
    CZECH("870", "CP870"),
    DEFAULT("1147", "CP1147"),
    DENMARK("277", "CP277"),
    ESTONIA("1142", "CP1142"),
    FINLAND("1143", "CP1143"),
    FRANCE("1147", "CP1147"),
    GERMANY("1141", "CP1141"),
    GREECE("875", "CP875"),
    HUNGARY("870", "CP870"),
    ICELAND("1149", "CP1149"),
    ISRAEL_NEW("862", "CP862"),
    ITALY("1144", "CP1144"),
    JAPAN_ENGLISH("939", "CP939"),
    JAPAN_KATAKANA("930", "CP930"),
    JAPAN_JIS2004("1399", "CP1399"),
    KOREA("949", "CP949"),
    LATIN_AMERICA("1145", "CP1145"),
    LATVIA("921", "CP921"),
    LITHUANIA("921", "CP921"),
    MACEDONIA("1025", "CP1025"),
    MULTILINGUAL("1148", "CP1148"),
    NETHERLANDS("1140", "CP1140"),
    NORWAY("277", "CP277"),
    POLAND("870", "CP870"),
    PORTUGAL("1140", "CP1140"),
    CHINA("Big5", "Big5"),
    ROMANIA("870", "CP870"),
    RUSSIA("1025", "CP1025"),
    SERBIA_MONTEGRO("1025", "CP1025"),
    SLOVAKIA("870", "CP870"),
    SLOVENIA("870", "CP870"),
    SPAIN("1145", "CP1145"),
    SWEDEN("278", "CP278"),
    THAI("874", "CP874"),
    TURKEY("857", "CP857"),
    UKRAINE("1123", "CP1123"),
    UNITED_KINGDOM("1146", "CP1146"),
    US("1140", "CP1140"),
    SWISS("500", "CP500");

    private final String encoding;
    private final String codePage;

    CodePage(final String codePage, final String encoding) {
        this.encoding = encoding;
        this.codePage = codePage;
    }

    /**
     * @return encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @return code page.
     */
    public String getCodePage() {
        return codePage;
    }

    @Override
    public String toString() {
    	return this.name() + "-" + encoding;
    }
}
