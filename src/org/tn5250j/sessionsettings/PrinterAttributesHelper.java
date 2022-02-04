package org.tn5250j.sessionsettings;
/*
 * Title: PrinterAttributesPanel
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

import java.util.Optional;
import java.util.Set;

import org.tn5250j.SessionConfig;

import javafx.geometry.Rectangle2D;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;

public class PrinterAttributesHelper extends AttributesSupport {

    // One unit is `1/72 of inch
    // 1 mm = 0,0393701 inch
    // 1 / 72 = 0,0393701 (incha / 72) = 0,0393701 of unit
    // 1 mm = 72 * 0,0393701
    public static final double INCHES_IN_MM = 0.0393701;
    public static final double ROUND_QUALITY = 72 * INCHES_IN_MM / 2;

    private final Printer printer = Printer.getDefaultPrinter();
    private PageLayout pappyPort;
    private PageLayout pappyLand;

    public PrinterAttributesHelper(final SessionConfig config) {
        super(config);

        pappyPort = getDefaultPageLayout(PageOrientation.PORTRAIT);
        pappyLand = getDefaultPageLayout(PageOrientation.LANDSCAPE);

        // Portrait paper parameters
        pappyPort = getPageLayout(
                "print.portImage.X",
                "print.portImage.Y",
                "print.portImageWidth",
                "print.portImageHeight",
                "print.portWidth",
                "print.portHeight",
                PageOrientation.PORTRAIT).orElse(pappyPort);

        // Landscape paper parameters
        pappyLand = getPageLayout(
                "print.landImage.X",
                "print.landImage.Y",
                "print.landImageWidth",
                "print.landImageHeight",
                "print.landWidth",
                "print.landHeight",
                PageOrientation.LANDSCAPE).orElse(pappyLand);
    }

    private PageLayout getDefaultPageLayout(final PageOrientation oritntation) {
        final PageLayout def = printer.getDefaultPageLayout();
        return printer.createPageLayout(def.getPaper(), oritntation,
                def.getLeftMargin(), def.getRightMargin(),
                def.getTopMargin(), def.getBottomMargin());
    }

    private Optional<PageLayout> getPageLayout(final String propImageX,
            final String propImageY,
            final String propImageWidth,
            final String propImageHeight,
            final String propWidth,
            final String propHeight,
            final PageOrientation orientation) {
        if (hasProperty(propImageX)
                && hasProperty(propImageY)
                && hasProperty(propImageWidth)
                && hasProperty(propImageHeight)
                && hasProperty(propWidth)
                && hasProperty(propHeight)) {

            try {
                final double imageX = getDoubleProperty(propImageX);
                final double imageY = getDoubleProperty(propImageY);
                final double imageWidth = getDoubleProperty(propImageWidth);
                final double imageHeight = getDoubleProperty(propImageHeight);
                final double width = getDoubleProperty(propWidth);
                final double height = getDoubleProperty(propHeight);

                final Optional<Paper> paperOptional = findPaper(width, height);
                if (paperOptional.isPresent()) {
                    final Paper paper = paperOptional.get();

                    final double left = Math.min(imageX, paper.getWidth() - imageWidth);
                    final double top = Math.min(imageY, paper.getHeight() - imageHeight);
                    final double right = Math.max(0, width - imageWidth - left);
                    final double bottom = Math.max(0, height - imageHeight - top);

                    final PageLayout pageLayout = printer.createPageLayout(paper, orientation, left, right, top, bottom);
                    return Optional.of(pageLayout);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                // nothing, just return empty optional
            }
       }

        return Optional.empty();
    }

    private Optional<Paper> findPaper(final double w, final double h) {
        final Set<Paper> papers = printer.getPrinterAttributes().getSupportedPapers();
        for (final Paper paper : papers) {
            if (equalsInches(paper.getWidth(), w) && equalsInches(paper.getHeight(), h)) {
                return Optional.of(paper);
            }
        }

        return Optional.empty();
    }

    private boolean equalsInches(final double l1, final double l2) {
        return Math.abs(l1 - l2) <= ROUND_QUALITY;
    }

    private double getDoubleProperty(final String name) {
        return Double.parseDouble(getStringProperty(name));
    }

    public Rectangle2D getImageableArea(final PageLayout layout) {
        final double w = layout.getPaper().getWidth();
        final double h = layout.getPaper().getHeight();

        return new Rectangle2D(
                layout.getLeftMargin(),
                layout.getTopMargin(),
                w - layout.getLeftMargin() - layout.getRightMargin(),
                h - layout.getTopMargin() - layout.getBottomMargin());
    }

    public PageLayout getPappyPort() {
        return pappyPort;
    }
    public void setPappyPort(final PageLayout pappyPort) {
        this.pappyPort = pappyPort;
    }
    public PageLayout getPappyLand() {
        return pappyLand;
    }
    public void setPappyLand(final PageLayout pappyLand) {
        this.pappyLand = pappyLand;
    }
    public Printer getPrinter() {
        return printer;
    }
}
