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

import org.deidentifier.arx.gui.view.impl.common.Popup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements a global tool tip
 * @author Fabian Prasser
 */
public class MainContextMenu extends Popup{

    private List        list;
    private Menu        menu;
    private MainToolTip tooltip;
    
    /**
     * Creates a new instance
     * 
     * @param parent
     */
    public MainContextMenu(final Shell parent, final MainToolTip tooltip) {
        super(parent);
        this.tooltip = tooltip;
        
    }
    
    @Override
    protected void prepareVisible(Shell shell) {
        tooltip.hide();
        list.removeAll();
        for (MenuItem item : menu.getItems()) {
            list.add("     "+item.getText()+"     ");
        }
        shell.pack();
        list.setSelection(0);
    }

    @Override
    protected void prepare(Shell shell) {
      
        shell.setLayout(new FillLayout());
        list = new List(shell, SWT.NONE);
        list.addMouseMoveListener(new MouseMoveListener(){
            @Override
            public void mouseMove(MouseEvent event) {
                int item = event.y / list.getItemHeight();
                list.setSelection(item);
            }
        });
        list.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseDown(MouseEvent arg0) {
                if (list.getSelectionIndex() >= 0){
                    hide();
                    menu.getItem(list.getSelectionIndex()).notifyListeners(SWT.Selection, new Event());
                }
            }
        });
        list.add("X");
        shell.pack();
    }

    /**
     * Sets the options displayed by the popup
     * 
     * @param items
     * @param listener
     */
    public void show(final Menu menu, final int x, final int y) {
        
        if (isNativeImplementationSupported()){
            menu.setLocation(x, y);
            menu.setVisible(true);
            this.menu = menu;
        } else {
            if (super.isVisible()) return;
            this.menu = menu;
            this.show(x, y);
        }
    }
    
    @Override
    public boolean isVisible() {
        if (isNativeImplementationSupported()) {
            return false;
        }
        else return super.isVisible();
    }
}
