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

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements a global tool tip
 * @author Fabian Prasser
 */
public class MainPopUp {

    private final Shell shell;
    private final List  list;
    private Rectangle   bounds   = null;
    private boolean     visible  = false;
    private Menu        menu;
    private long        WAIT     = 100;
    private MainToolTip tooltip;

    /**
     * Creates a new instance
     * @param parent
     */
    public MainPopUp(final Shell parent, final MainToolTip tooltip) {        
        this.tooltip = tooltip;
        shell = new Shell(parent, SWT.TOOL | SWT.ON_TOP);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        list = new List(shell, SWT.NONE);
        list.addMouseMoveListener(new MouseMoveListener(){
            @Override
            public void mouseMove(MouseEvent event) {
                
            }
        });
        
        shell.pack();
        shell.setVisible(false);
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (this) {
                        if (visible && bounds != null) {
                            final Point p = MouseInfo.getPointerInfo().getLocation();
                            if (!bounds.contains(p)) {
                                Display.getDefault().syncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!shell.getBounds().contains(p.x, p.y)) {
                                            hide();
                                       }  
                                    }
                                });
                            }
                        }
                    }
                    try {
                        Thread.sleep(WAIT);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void show() {
        synchronized (this) {
            if (this.menu != null) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        tooltip.hide();
                        list.removeAll();
                        for (MenuItem item : menu.getItems()) {
                            list.add(item.getText());
                        }
                        shell.pack();
                        shell.setVisible(true);
                        visible = true;
                    }
                });
            }
        }
    }

    protected void hide() {
        synchronized (this) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    shell.setVisible(false);
                    visible = false;
                    menu = null;
                    bounds = null;
                    list.removeAll();
                }
            });
        }
    }

    /**
     * Sets the options displayed by the popup
     * 
     * @param items
     * @param listener
     */
    public synchronized void show(final Menu menu, final int x, final int y, final org.eclipse.swt.graphics.Rectangle bounds) {
        synchronized (this) {
            if (this.visible) return;
            MainPopUp.this.menu = menu;
            this.shell.setLocation(x, y);
            MainPopUp.this.bounds = new Rectangle(bounds.x,
                                                   bounds.y,
                                                   bounds.width,
                                                   bounds.height);
            show();
        }
    }
}
