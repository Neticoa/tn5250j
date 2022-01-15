/*
 * Title: HotspotsAction.java
 * Copyright:   Copyright (c) 2001,2002
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.keyboard.actions;

import static org.tn5250j.keyboard.KeyMnemonic.HOTSPOTS;

import org.tn5250j.SessionGui;
import org.tn5250j.keyboard.KeyMapper;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Toggle Hot spots
 */
public class HotspotsAction extends EmulatorAction {
    public HotspotsAction(final SessionGui session, final KeyMapper keyMap) {
        super(session,
                HOTSPOTS.mnemonic,
                new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN),
                keyMap);
    }

    @Override
    public void handle() {
        session.toggleHotSpots();
    }
}
