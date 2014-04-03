/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl;

import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

/**
 * This class implements a global popup menu. It is required to ensure
 * consistent design amongst the different SWT/AWT/Swing components.
 * 
 * @author Fabian Prasser
 */
public class MainPopUp {

    private MainToolTip  tooltip;
    private boolean      visible;
    private MenuListener listener;

    /**
     * Creates a new pop up
     * 
     * @param parent
     */
    public MainPopUp(final MainToolTip tooltip) {        
        this.tooltip = tooltip;
        this.listener = new MenuAdapter() {
            @Override
            public void menuHidden(MenuEvent arg0) {
                ((Menu) arg0.widget).removeMenuListener(this);
                setVisible(false);
            }
        };
    }

    /**
     * Returns whether the popup is visible
     * 
     * @return
     */
    public synchronized boolean isVisible() {
        return visible;
    }

    private synchronized void setVisible(boolean value) {
        this.visible = value;
    }

    /**
     * Sets the options displayed by the popup
     * 
     * @param items
     * @param listener
     */
    public synchronized void show(final Menu menu, final int x, final int y) {
        setVisible(true);
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                tooltip.hide();
                menu.setLocation(x, y);
                menu.setVisible(true);
                menu.addMenuListener(listener);
            }
        });
    }
}
