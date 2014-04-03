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

package org.deidentifier.arx.gui.view.impl.common;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A generic pop up window that closes itself when the mouse leaves its boundaries
 * @author Fabian Prasser
 */
public abstract class Popup {

    protected static final long THRESHOLD = 30;
    protected static final int  WAIT      = 100;
    private Shell               shell;
    private Rectangle           bounds    = null;

    /**
     * Creates a new instance
     * @param parent
     */
    public Popup(final Shell parent) {
        
        this.shell = new Shell(parent, SWT.TOOL | SWT.ON_TOP);
        this.prepare(shell);
        this.shell.setVisible(false);
        new Timer(WAIT, new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                if (bounds != null) {
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    if (!bounds.contains(p)) {
                        hide();
                    }
                }
            }
        }).start();
    }

    /**
     * Show the popup
     * @param x
     * @param y
     */
    protected void show(final int x, final int y) {
        synchronized (this) {
            if (this.bounds == null) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        prepareVisible(shell);
                        shell.setLocation(x, y);
                        org.eclipse.swt.graphics.Rectangle shellRect = shell.getBounds();
                        bounds = new Rectangle((int)(shellRect.x - THRESHOLD), 
                                               (int)(shellRect.y - THRESHOLD), 
                                               (int)(shellRect.width + THRESHOLD * 2), 
                                               (int)(shellRect.height + THRESHOLD * 2));
                        shell.setVisible(true);
                    }
                });
            }
        }
    }

    /**
     * Hide the popup
     */
    protected void hide() {
        synchronized (this) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    shell.setVisible(false);
                    bounds = null;
                }
            });
        }
    }
    
    /**
     * Is the popup visible
     * @return
     */
    protected boolean isVisible() {
        synchronized (this) {
            return bounds != null;
        }
    }

    /**
     * Prepare the shell for displaying
     * @param shell
     */
    protected abstract void prepareVisible(Shell shell);

    /**
     * Prepare the shell
     * @param shell
     */
    protected abstract void prepare(Shell shell);
}