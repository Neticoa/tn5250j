/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public enum Terminal {
    IBM_5555_C01("IBM-5555-C01 (24 x 80) Double-Byte Character Set color display", 24, 80, true, true),
    IBM_5555_B01("IBM-5555-B01 (24 x 80) Double-Byte Character Set (DBCS)", 24, 80, false, true),
    IBM_3477_FC("IBM-3477-FC (27 x 132) color display", 27, 132, true, false),
    IBM_3477_FG("IBM-3477-FG (27 x 132) monochrome display", 27, 132),
    IBM_3180_2("IBM-3180-2 (27 x 132) monochrome display", 27, 132),
    IBM_3179_2("IBM-3179-2 (24 x 80) color display", 24, 80, true, false),
    IBM_3196_A1("IBM-3196-A1 (24 x 80) monochrome display", 24, 80),
    IBM_5292_2("IBM-5292-2 (24 x 80) color display", 24, 80, true, false),
    IBM_5291_1("IBM-5291-1 (24 x 80) monochrome display", 24, 80),
    IBM_5251_11("IBM-5251-11 (24 x 80) monochrome display", 24, 80),
    IBM_3278_2("IBM-3278-2 IBM-3278-2-E (24 x 80)", 24, 80),
    IBM_3278_3("IBM-3278-3 IBM-3278-3-E (32 x 80)", 32, 80),
    IBM_3278_4("IBM-3278-4 IBM-3278-4-E (43 x 80)", 43, 80),
    IBM_3278_5("IBM-3278-5 IBM-3278-5-E (27 x 132)", 27, 132);

    private final int rows;
    private final int columns;
    private final boolean isColorDisplay;
    private final String description;
    private final boolean isDoubleByteCharSet;

    Terminal(final String description, final int rows, final int columns) {
        this(description, rows, columns, false, false);
    }
    Terminal(final String description, final int rows, final int columns,
            final boolean isColorDisplay, final boolean isDoubleByteCharSet) {
        this.description = description;
        this.rows = rows;
        this.columns = columns;
        this.isColorDisplay = isColorDisplay;
        this.isDoubleByteCharSet = isDoubleByteCharSet;
    }
    /**
     * @return true if has double buffered character set.
     */
    public boolean isDoubleByteCharSet() {
        return isDoubleByteCharSet;
    }
    /**
     * @return number of rows.
     */
    public int getRows() {
        return rows;
    }
    /**
     * @return number of columns.
     */
    public int getColumns() {
        return columns;
    }
    /**
     * @return true if has color display
     */
    public boolean isColorDisplay() {
        return isColorDisplay;
    }
    /**
     * @return terminal description.
     */
    public String getDescription() {
        return description;
    }
}
