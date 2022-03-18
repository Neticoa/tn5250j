/**
 *
 */
package com.metrixware.eclipse;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Text;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class UiUtils {
    private UiUtils() {}

    public static int getWidthForNumCharacterrs(final Text text, final int numChars) {
        final GC gc = new GC(text);
        try {
            final FontMetrics fm = gc.getFontMetrics();
            return (int) Math.round(numChars * fm.getAverageCharacterWidth());
        } finally {
            gc.dispose();
        }
    }
}
