/**
 *
 */
package org.tn5250j.sessionsettings;

import org.tn5250j.AbstractSessionConfig;
import org.tn5250j.gui.UiUtils;

import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class AttributesSupport {

    protected final AbstractSessionConfig changes;

    public AttributesSupport(final AbstractSessionConfig changes) {
        super();
        this.changes = changes;
    }

    @SuppressWarnings("deprecation")
    public final String getStringProperty(final String prop) {

        if (hasProperty(prop))
            return changes.getStringProperty(prop);
        else
            return "";

    }

    /**
     * @param prop property name.
     * @return true if exists property with given name, false otherwise
     */
    public boolean hasProperty(final String prop) {
        return changes.isPropertyExists(prop);
    }

    public String getStringProperty(final String prop, final String defaultValue) {

        if (hasProperty(prop)) {
            @SuppressWarnings("deprecation")
            final String p = changes.getStringProperty(prop);
            if (p.length() > 0)
                return p;
            else
                return defaultValue;
        } else
            return defaultValue;

    }

    public Color getColorProperty(final String prop) {
        return getColorProperty(prop, null);
    }

    @SuppressWarnings("deprecation")
    public Color getColorProperty(final String prop, final Color defColor) {
        if (hasProperty(prop)) {
            return UiUtils.rgb(changes.getIntegerProperty(prop));
        } else
            return defColor;
    }

    public boolean getBooleanProperty(final String prop, final boolean dflt) {

        if (hasProperty(prop)) {
            @SuppressWarnings("deprecation")
            final String b = changes.getStringProperty(prop).toLowerCase();
            if (b.equals("yes") || b.equals("true"))
                return true;
            else
                return false;
        } else
            return dflt;

    }

    public Rectangle2D getRectangleProperty(final String key) {
        return changes.getRectangleProperty(key);
    }

    @SuppressWarnings("deprecation")
    public Font getFontProperty(final String key, final int size) {
        if (hasProperty(key)) {
            return new Font(changes.getStringProperty(key), size);
        }
        return null;
    }
}
