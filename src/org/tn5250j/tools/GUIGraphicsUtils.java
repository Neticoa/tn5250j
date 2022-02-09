/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,202,2003
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
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
package org.tn5250j.tools;

import java.util.LinkedList;
import java.util.List;

import org.tn5250j.gui.FontMetrics;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.system.OperatingSystem;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class GUIGraphicsUtils {

//    private static final Insets GROOVE_INSETS = new Insets(2, 2, 2, 2);
//    private static final Insets ETCHED_INSETS = new Insets(2, 2, 2, 2);
    public static final int RAISED = 1;
    public static final int INSET = 2;
    public static final int WINDOW_NORMAL = 3;
    public static final int WINDOW_GRAPHIC = 4;
    private static String defaultFont;

    private static LinkedList<Image> tnicon;

    public static void draw3DLeft(final GraphicsContext g, final int which,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {

            //      g.translate(x, y);

            if (which == RAISED) {
                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth,
                        y + 1);

                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight - 2);

                // | vertical
                g.strokeLine(x + 1,
                        y,
                        x + 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // --  horizontal bottom
                g.strokeLine(x + 1,
                        y + fmHeight - 3,
                        x + fmWidth,
                        y + fmHeight - 3);
            }
            if (which == INSET) {

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth,
                        y + 1);

                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight - 2);

                // | vertical
                g.strokeLine(x + 1,
                        y,
                        x + 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.WHITE);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // --  horizontal bottom
                g.strokeLine(x + 1,
                        y + fmHeight - 3,
                        x + fmWidth,
                        y + fmHeight - 3);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void draw3DMiddle(final GraphicsContext g, final int which,
            final double x, final double y, final double fmWidth, final double fmHeight) {
        final Paint oldColor = g.getFill(); // make sure we leave it as we found it

        try {
            //      g.translate(x, y);
            if (which == RAISED) {
                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth,
                        y + 1);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 3,
                        x + fmWidth,
                        y + fmHeight - 3);
            }
            if (which == INSET) {

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth,
                        y + 1);

                g.setFill(javafx.scene.paint.Color.WHITE);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 3,
                        x + fmWidth,
                        y + fmHeight - 3);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void draw3DRight(final GraphicsContext g, final int which,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {

            //      g.translate(x, y);
            if (which == RAISED) {

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth - 2,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth - 3,
                        y + 1);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth - 2,
                        y + fmHeight - 2);

                // | vertical
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // | vertical
                g.strokeLine(x + fmWidth - 2,
                        y + 1,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 3,
                        x + fmWidth - 2,
                        y + fmHeight - 3);
            }
            if (which == INSET) {

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth - 2,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth - 3,
                        y + 1);

                g.setFill(javafx.scene.paint.Color.WHITE);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth - 2,
                        y + fmHeight - 2);

                // | vertical
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // | vertical
                g.strokeLine(x + fmWidth - 2,
                        y + 1,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 3,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }
    public static void draw3DOne(final GraphicsContext g, final double which,
            final double x, final double y, final double fmWidth, final double fmHeight) {
        final Paint oldColor = g.getFill(); // make sure we leave it as we found it

        try {
            //      g.translate(x, y);
            if (which == INSET) {

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth - 2,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth - 3,
                        y + 1);

                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight - 2);

                // | vertical
                g.strokeLine(x + 1,
                        y,
                        x + 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.WHITE);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth - 2,
                        y + fmHeight - 2);

                // | vertical right
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // | vertical right
                g.strokeLine(x + fmWidth - 2,
                        y + 1,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

                // --  horizontal bottom
                g.strokeLine(x + 1,
                        y + fmHeight - 3,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

            }
            if (which == RAISED) {

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal top
                g.strokeLine(x,
                        y,
                        x + fmWidth - 2,
                        y);

                // --  horizontal top
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth - 3,
                        y + 1);

                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight - 2);

                // | vertical
                g.strokeLine(x + 1,
                        y,
                        x + 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.DARKGRAY);

                // --  horizontal bottom
                g.strokeLine(x,
                        y + fmHeight - 2,
                        x + fmWidth - 2,
                        y + fmHeight - 2);

                // | vertical right
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight - 2);

                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);

                // | vertical right
                g.strokeLine(x + fmWidth - 2,
                        y + 1,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

                // --  horizontal bottom
                g.strokeLine(x + 1,
                        y + fmHeight - 3,
                        x + fmWidth - 2,
                        y + fmHeight - 3);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawScrollBar(final GraphicsContext g, final int which, final int direction,
            final double x, final double y, final double fmWidth, final double fmHeight,
            final javafx.scene.paint.Color fg, final javafx.scene.paint.Color bg) {
        final Paint oldColor = g.getFill(); // make sure we leave it as we found it

        //      g.translate(x, y);
        try {
            if (which == INSET) {
                g.setFill(bg);
                g.fillRect(x, y, fmWidth, fmHeight);
                g.setFill(fg);
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight);
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight);

                //            g.drawRect(x,y,fmWidth-2,fmHeight);
            }
            if (which == RAISED) {
                g.setFill(bg);
                g.fillRect(x, y, fmWidth, fmHeight);
                g.setFill(fg);
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight);
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight);

                //            g.drawRect(x,y,fmWidth-2,fmHeight);

            }

            if (direction == 1) {
                g.setFill(fg.brighter());
                g.strokeLine(x + (fmWidth / 2),
                        y + 2,
                        x + 2,
                        y + fmHeight - 4);

                g.setFill(fg.darker());

                g.strokeLine(x + (fmWidth / 2),
                        y + 2,
                        x + fmWidth - 2,
                        y + fmHeight - 4);

                g.strokeLine(x + 2,
                        y + fmHeight - 4,
                        x + fmWidth - 2,
                        y + fmHeight - 4);

                g.setFill(fg);
                g.strokeLine(x,
                        y,
                        x + fmWidth - 1,
                        y);

                g.strokeLine(x,
                        y + fmHeight - 1,
                        x + fmWidth - 1,
                        y + fmHeight - 1);

            }

            if (direction == 2) {
                g.setFill(fg.brighter());
                g.strokeLine(x + (fmWidth / 2),
                        y + fmHeight - 4,
                        x + 2,
                        y + 2);
                g.strokeLine(x + 2,
                        y + 2,
                        x + fmWidth - 2,
                        y + 2);


                g.setFill(fg.darker());

                g.strokeLine(x + (fmWidth / 2),
                        y + fmHeight - 4,
                        x + fmWidth - 2,
                        y + 2);

                g.setFill(fg);
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                g.strokeLine(x,
                        y + fmHeight - 1,
                        x + fmWidth,
                        y + fmHeight - 1);

            }

            if (direction == 3) {
                g.setFill(fg);
                g.fillRect(x + 2, y, fmWidth - 4, fmHeight);
            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawWinUpperLeft(final GraphicsContext g, final double which,
            final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {
            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {
                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal
                g.strokeLine(x, y, x + fmWidth, y);
                // | vertical
                g.strokeLine(x, y, x, y + fmHeight);

            }
            if (which == WINDOW_NORMAL) {
                // --  horizontal
                g.strokeLine(x + fmWidth / 2, y + fmHeight / 2, x + fmWidth, y + fmHeight / 2);
                // | vertical
                g.strokeLine(x + fmWidth / 2, y + fmHeight / 2, x + fmWidth / 2, y + fmHeight);

            }
        } finally {
            g.setFill(oldColor);
        }
    }

    public static void drawWinUpper(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {
        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        //      g.translate(x, y);
        try {
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal
                g.strokeLine(x,
                        y + fmHeight - 1,
                        x + fmWidth,
                        y + fmHeight - 1);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal
                g.strokeLine(x,
                        y + fmHeight,
                        x + fmWidth,
                        y + fmHeight);

            }
            if (which == WINDOW_NORMAL) {

                g.strokeLine(x,
                        y + fmHeight / 2,
                        x + fmWidth,
                        y + fmHeight / 2);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawWinUpperRight(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {
            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // | vertical
                g.strokeLine(x + fmWidth,
                        y,
                        x + fmWidth,
                        y + fmHeight);


            }
            if (which == WINDOW_NORMAL) {

                // | vertical
                g.strokeLine(x + fmWidth / 2,
                        y + fmHeight / 2,
                        x + fmWidth / 2,
                        y + fmHeight);
                // -- horizontal
                g.strokeLine(x,
                        y + fmHeight / 2,
                        x + fmWidth / 2,
                        y + fmHeight / 2);

                g.setFill(fill.darker());

                int w = 0;

                while (w < 3) {
                    g.fillRect((x + fmWidth / 2) + (3 + w),
                            y + (++w) + fmHeight / 2,
                            1,
                            (fmHeight / 2));
                }

            }
        } finally {
            g.setFill(oldColor);
        }
    }

    public static void drawWinLeft(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {

            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight);

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  vertical
                g.strokeLine(x + fmWidth - 1,
                        y,
                        x + fmWidth - 1,
                        y + fmHeight);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  vertical
                g.strokeLine(x + fmWidth,
                        y,
                        x + fmWidth,
                        y + fmHeight);

            }
            if (which == WINDOW_NORMAL) {

                g.strokeLine(x + fmWidth / 2,
                        y,
                        x + fmWidth / 2,
                        y + fmHeight);


            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawWinRight(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {
            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // | vertical
                g.strokeLine(x + fmWidth,
                        y,
                        x + fmWidth,
                        y + fmHeight);

                g.setFill(javafx.scene.paint.Color.WHITE);

                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // | vertical
                g.strokeLine(x + 1,
                        y,
                        x + 1,
                        y + fmHeight);

            }
            if (which == WINDOW_NORMAL) {

                g.strokeLine(x + fmWidth / 2,
                        y,
                        x + fmWidth / 2,
                        y + fmHeight);

                g.setFill(fill.darker());
                g.fillRect((x + fmWidth / 2) + 3,
                        y,
                        3,
                        fmHeight);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawWinRight(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
                                    final int x, final int y,
                                    final int fmWidth, final int fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        //      g.translate(x, y);
        g.setFill(fill);

        if (which == WINDOW_GRAPHIC) {

            g.fillRect(x, y, fmWidth, fmHeight);

            g.setFill(javafx.scene.paint.Color.BLACK);

            // | vertical
            g.strokeLine(x + fmWidth,
                    y,
                    x + fmWidth,
                    y + fmHeight);

            g.setFill(javafx.scene.paint.Color.WHITE);

            // | vertical
            g.strokeLine(x,
                    y,
                    x,
                    y + fmHeight);

            g.setFill(javafx.scene.paint.Color.BLACK);

            // | vertical
            g.strokeLine(x + 1,
                    y,
                    x + 1,
                    y + fmHeight);

        }
        if (which == WINDOW_NORMAL) {

            g.strokeLine(x + fmWidth / 2,
                    y,
                    x + fmWidth / 2,
                    y + fmHeight);

            g.setFill(fill.darker());
            g.fillRect((x + fmWidth / 2) + 3,
                    y,
                    3,
                    fmHeight);

        }
        //      g.translate(-x, -y);
        g.setFill(oldColor);
    }

    public static void drawWinLowerLeft(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {

        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {
            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal
                g.strokeLine(x,
                        y + fmHeight - 1,
                        x + fmWidth,
                        y + fmHeight - 1);

                g.setFill(javafx.scene.paint.Color.WHITE);

                // | vertical
                g.strokeLine(x,
                        y,
                        x,
                        y + fmHeight - 1);

            }
            if (which == WINDOW_NORMAL) {


                // | horizontal
                g.strokeLine(x + fmWidth / 2,
                        y + fmHeight / 2,
                        x + fmWidth / 2,
                        y);

                // -- vertical
                g.strokeLine(x + fmWidth / 2,
                        y + fmHeight / 2,
                        x + fmWidth,
                        y + fmHeight / 2);

                g.setFill(fill.darker());
                int w = 0;

                while (w < 3) {
                    g.fillRect((x + fmWidth / 2) + ++w,
                            y + fmHeight / 2 + (2 + w),
                            fmWidth / 2,
                            1);
                }

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawWinBottom(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {
        final Paint oldColor = g.getFill(); // make sure we leave it as we found it

        try {
            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.BLACK);

                // | horizontal
                g.strokeLine(x,
                        y + fmHeight - 1,
                        x + fmWidth,
                        y + fmHeight - 1);

                g.setFill(javafx.scene.paint.Color.WHITE);
                // --  horizontal
                g.strokeLine(x,
                        y,
                        x + fmWidth,
                        y);

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal
                g.strokeLine(x,
                        y + 1,
                        x + fmWidth,
                        y + 1);


            }
            if (which == WINDOW_NORMAL) {

                g.strokeLine(x,
                        y + fmHeight / 2,
                        x + fmWidth,
                        y + fmHeight / 2);

                // bottom
                g.setFill(fill.darker());
                g.fillRect(x,
                        (y + fmHeight / 2) + 3,
                        fmWidth,
                        3);

            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }

    public static void drawWinLowerRight(final GraphicsContext g, final int which, final javafx.scene.paint.Color fill,
            final double x, final double y, final double fmWidth, final double fmHeight) {
        final Paint oldColor = g.getFill(); // make sure we leave it as we found it
        try {
            //      g.translate(x, y);
            g.setFill(fill);

            if (which == WINDOW_GRAPHIC) {

                g.fillRect(x, y, fmWidth, fmHeight);

                g.setFill(javafx.scene.paint.Color.BLACK);
                // --  horizontal
                g.strokeLine(x,
                        y + fmHeight - 1,
                        x + fmWidth,
                        y + fmHeight - 1);

                // | vertical
                g.strokeLine(x + fmWidth,
                        y,
                        x + fmWidth,
                        y + fmHeight - 1);


            }
            if (which == WINDOW_NORMAL) {

                // vertical
                g.strokeLine(x + fmWidth / 2,
                        y,
                        x + fmWidth / 2,
                        y + fmHeight / 2);
                // horizontal
                g.strokeLine(x + fmWidth / 2,
                        y + fmHeight / 2,
                        x,
                        y + fmHeight / 2);

                g.setFill(fill.darker());
                // right part
                g.fillRect((x + fmWidth / 2) + 3,
                        y,
                        3,
                        (fmHeight / 2) + 3);
                // bottom part
                g.fillRect(x,
                        (y + fmHeight / 2) + 3,
                        (fmWidth / 2) + 6,
                        3);
            }
        } finally {
            //      g.translate(-x, -y);
            g.setFill(oldColor);
        }
    }
    public static Font getDerivedFont(final Font font,
            final double width, final double height,
            final int numRows, final int numCols,
            float pointSize) {
        // get the new proposed width and height of the screen that we
        // are suppose to fit within
        final double w = width / numCols;     // proposed width
        final double h = height / (numRows + 2);     // proposed height

        double sw = 0;
        double sh = 0;

        Font k = null;
        if (numCols != 132) {
            pointSize = 0;
        }
        //         at.setToScale( 1.0f, 1.0f );
        //         pointSize = 0;

        float j = 1;

        if (pointSize == 0) {

            // loop through the sizes of the fonts until we find one that will not
            // fit within the width or the height of the new proposed size
            for (; j < 36; j++) {

                k = UiUtils.deriveFont(font, j);
                final FontMetrics l = FontMetrics.deriveFrom(k);

                sw = FontMetrics.getStringBounds("W", k).getWidth() + 2;
                sh = FontMetrics.getStringBounds("y", k).getHeight() + l.getDescent() + l.getLeading();
                if (w < sw || h < sh) {
                    break;
                }
            }
        } else {
            k = UiUtils.deriveFont(font, pointSize);
        }


        // since we obtained one that will not fit within the proposed size
        // we need to decrement it so that we obtain the last one that did fit
        if (j > 1)
            k = UiUtils.deriveFont(font, --j);

        return k;
    }

    /**
     * Windows fonts to search for in order of precedence
     */
    static final String[] windowsFonts = {"Andale Mono", "Letter Gothic Bold",
            "Lucida Sans Typewriter Regular",
            "Lucida Sans Typewriter Bold",
            "Lucida Console",
            "Courier New Bold",
            "Courier New", "Courier"};

    /**
     * *nix fonts to search for in order of precedence
     */
    static final String[] nixFonts = {"Lucida Sans Typewriter Regular",
            "Lucida Sans Typewriter Bold",
            "Courier New Bold",
            "Courier New",
            "Courier Bold",
            "Courier"};

    /**
     * Mac fonts to search for in order of precedence
     */
    static final String[] macFonts = {"Monaco",
            "Courier New Bold",
            "Courier New", "Courier"};

    public static String getDefaultFont() {

        if (defaultFont == null) {
            String[] fonts = windowsFonts;
            if (OperatingSystem.isMacOS()) {
                fonts = macFonts;
            } else if (OperatingSystem.isUnix()) {
                fonts = nixFonts;
            }

            for (int x = 0; x < fonts.length; x++) {
                if (isFontNameExists(fonts[x])) {
                    defaultFont = fonts[x];
                    break;
                }
            }

            // we will just make if a space at this time until we come up with
            //  a better solution
            if (defaultFont == null) {
                defaultFont = "";
            }
        }

        return defaultFont;
    }

    /**
     * Checks to see if the font name exists within our environment
     *
     * @return whether the font passed exists or not.
     */
    public static boolean isFontNameExists(final String fontString) {

        // fonts from the environment
        for (final String fontName : Font.getFontNames()) {
            if (fontName.indexOf('.') < 0 && fontName.equals(fontString))
                return true;
        }

        return false;
    }

    public static List<javafx.scene.image.Image> getApplicationIcons() {
        if (tnicon == null) {
            tnicon = new LinkedList<>();
            tnicon.add(new javafx.scene.image.Image(ClassLoader.getSystemClassLoader().getResource("tn5250j-16x16.png").toString()));
            tnicon.add(new javafx.scene.image.Image(ClassLoader.getSystemClassLoader().getResource("tn5250j-32x32.png").toString()));
            tnicon.add(new javafx.scene.image.Image(ClassLoader.getSystemClassLoader().getResource("tn5250j-48x48.png").toString()));
        }
        return tnicon;
    }
}
