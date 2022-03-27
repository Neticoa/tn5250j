/*
 * Title: PrinterTask
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j;

import org.tn5250j.framework.tn5250.Screen5250Facade;
import org.tn5250j.gui.FontMetrics;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.sessionsettings.PrinterAttributesHelper;

import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PrintQuality;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

class PrinterTask {

    private char[] screen;
    private char[] screenExtendedAttr;
    private char[] screenAttrPlace;
    private int numCols;
    private int numRows;
    private Font font;
    private SessionGui session;
    private AbstractSessionConfig config;

    PrinterTask(final Screen5250Facade scr, final Font font, final int cols, final int rows, final SessionGui ses) {
        session = ses;
        session.setWaitCursor();
        config = ses.getSession().getConfiguration();

        final int len = scr.getScreenLength();
        screen = new char[len];
        screenExtendedAttr = new char[len];
        screenAttrPlace = new char[len];
        scr.GetScreen(screen, len, TN5250jConstants.PLANE_TEXT);
        scr.GetScreen(screenExtendedAttr, len, TN5250jConstants.PLANE_EXTENDED);
        scr.GetScreen(screenAttrPlace, len, TN5250jConstants.PLANE_IS_ATTR_PLACE);

        numCols = cols;
        numRows = rows;
        this.font = font;
    }

    public void run() {
        //--- Create a printerJob object
        final PrinterJob printJob = PrinterJob.createPrinterJob();
        printJob.getJobSettings().setJobName("tn5250j");

        // will have to remember this for the next time.
        //   Always set a page format before call setPrintable to
        //   set the orientation.
        final PrinterAttributesHelper helper = new PrinterAttributesHelper(config);
        printJob.getJobSettings().setPageLayout(numCols != 132 ? helper.getPappyPort() : helper.getPappyLand());

        final Font fontFromProperties = helper.getFontProperty("print.font", 8);
        if (fontFromProperties != null) {
            font = fontFromProperties;
        }

        //--- Set the printable class to this one since we
        //--- are implementing the Printable interface

        // set the cursor back
        session.setDefaultCursor();

        //--- Show a print dialog to the user. If the user
        //--- clicks the print button, then print, otherwise
        //--- cancel the print job
        if (printJob.showPrintDialog(session.getWindow())) {
            try {
                final Node node = buildPrintable(printJob.getJobSettings());

                printJob.getJobSettings().setPrintQuality(PrintQuality.HIGH);
                if (printJob.printPage(node)) {
                    printJob.endJob();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else {
            // we do this because of loosing focus with jdk 1.4.0
        }
    }

    private Canvas buildPrintable(final JobSettings settings) {
        final PageLayout pageFormat = settings.getPageLayout();

        final double w = pageFormat.getPrintableWidth();
        final double h = pageFormat.getPrintableHeight();
        final Canvas canvas = new Canvas(w, h);

        final Font font = getBetterFont(pageFormat, w, h);
        final FontMetrics f = FontMetrics.deriveFrom(font);

        //--- Create a graphic2D object and set the default parameters
        final GraphicsContext g2 = canvas.getGraphicsContext2D();
        g2.setStroke(Color.BLACK);
        g2.setFill(Color.BLACK);
        g2.setFont(font);
        g2.setLineWidth(1);

        // get the width and height of the character bounds
        final double w1 = FontMetrics.getStringBounds("W", font).getWidth();
        final double h1 = (FontMetrics.getStringBounds("y", font).getHeight() +
                f.getDescent() + f.getLeading());

        // loop through all the screen characters and print them out.
        for (int m = 0; m < numRows; m++) {
            for (int i = 0; i < numCols; i++) {
                final double x = w1 * i;
                final double y = h1 * (m + 1);

                // only draw printable characters (in this case >= ' ')
                if (screen[getPos(m, i)] >= ' ' && ((screenExtendedAttr[getPos(m, i)]
                        & TN5250jConstants.EXTENDED_5250_NON_DSP) == 0)) {

                    final String text = new String(screen, getPos(m, i), 1);
                    g2.fillText(text, x, y + h1 - (f.getDescent() + f.getLeading()) - 2);
                }

                // if it is underlined then underline the character
                if ((screenExtendedAttr[getPos(m, i)] & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0 &&
                        screenAttrPlace[getPos(m, i)] != 1)
                    g2.strokeLine(x, y + (h1 - f.getLeading() - 3), (x + w1), (int) (y + (h1 - f.getLeading()) - 3));
            }
        }

        return canvas;
    }

    private Font getBetterFont(final PageLayout pageFormat, final double width, final double height) {
        final double w = width / numCols;     // proposed width
        final double h = height / numRows;     // proposed height

        Font k;
        float j = 1;

        for (; j < 50; j++) {
            // derive the font and obtain the relevent information to compute
            // the width and height
            k = UiUtils.deriveFont(font, j);
            final FontMetrics l = FontMetrics.deriveFrom(k);

            if ((w < FontMetrics.getStringBounds("W", k).getWidth()) ||
                    h < (FontMetrics.getStringBounds("y", k).getHeight() + l.getDescent() + l.getLeading()) ) {
                break;
            }
        }

        // since we were looking for an overrun of the width or height we need
        // to adjust the font one down to get the last one that fit.
        return UiUtils.deriveFont(font, --j);
    }

    private int getPos(final int row, final int col) {
        return (row * numCols) + col;
    }
}
