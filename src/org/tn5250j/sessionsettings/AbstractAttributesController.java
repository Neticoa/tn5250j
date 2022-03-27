/*
 * Title: AttributesPanel
 * Copyright:   Copyright (c) 2001,2002
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
package org.tn5250j.sessionsettings;

import org.tn5250j.AbstractSessionConfig;
import org.tn5250j.gui.ControllerWithView;
import org.tn5250j.tools.LangTool;

import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;

/**
 * Base class for all attribute panels
 */
public abstract class AbstractAttributesController extends AttributesSupport implements ControllerWithView, Initializable {
    private static final String nodePrefix = "sa.node";

    private String name;
    public AbstractAttributesController(final AbstractSessionConfig config) {
        this(config, "", nodePrefix);
    }

    public AbstractAttributesController(final AbstractSessionConfig config, final String name) {
        this(config, name, nodePrefix);
    }

    public AbstractAttributesController(final AbstractSessionConfig config, final String name, final String prefix) {
        super(config);
        this.name = LangTool.getString(prefix + name);
    }

    public abstract void applyAttributes();

    protected void setRectangleProperty(final String key, final Rectangle2D rect) {
        changes.setRectangleProperty(key, rect);
    }

    protected final void setProperty(final String key, final String val) {
        changes.setProperty(key, val);
    }

    @Override
    public String toString() {
        return name;
    }

    protected void fireStringPropertyChanged(final String name, final String value) {
        changes.firePropertyChange(this, name, getStringProperty(name), value);
        setProperty(name, value);
    }
}
