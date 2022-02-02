/*
 * @(#)WizardEvent.java
 * Copyright:    Copyright (c) 2001
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
package org.tn5250j.event;

import org.tn5250j.gui.WizardPage;

/**
 * The event object for Wizard pages.
 */
public class WizardEvent extends java.util.EventObject {

    private static final long serialVersionUID = 1L;
    protected WizardPage currentPage;
    protected WizardPage newPage;
    protected boolean isLastPage;
    protected boolean allowChange;

    public WizardEvent(final Object source, final WizardPage current_page, final WizardPage new_page,
                       final boolean is_last_page, final boolean allow_change) {

        super(source);
        this.currentPage = current_page;
        this.newPage = new_page;
        this.isLastPage = is_last_page;
        this.allowChange = allow_change;
    }

    /**
     * Returns whether the page is the last page.
     */
    public boolean isLastPage() {
        return isLastPage;
    }

    /**
     * Returns whether the event should be allowed to finish processing.
     */
    public boolean getAllowChange() {
        return allowChange;
    }

    /**
     * Sets whether the event should be allowed to finish processing.
     */
    public void setAllowChange(final boolean v) {
        allowChange = v;
    }

    /**
     * Returns the next page.
     */
    public WizardPage getNewPage() {
        return newPage;
    }

    /**
     * Sets the next page.
     */
    public void setNewPage(final WizardPage p) {
        newPage = p;
    }

    /**
     * Returns the current page on which the <code>JCWizardEvent</code> occured.
     */
    public WizardPage getCurrentPage() {
        return currentPage;
    }

}
