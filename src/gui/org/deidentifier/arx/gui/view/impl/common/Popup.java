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

import java.awt.AWTEvent;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A generic pop up window that closes itself when the mouse leaves its boundaries
 * @author Fabian Prasser
 */
public abstract class Popup {

    protected static final long                THRESHOLD   = 20;
    protected static final int                 WAIT        = 100;
    private Shell                              shell;
    private Rectangle                          bounds      = null;
    private org.eclipse.swt.graphics.Rectangle shellBounds = null;

    /**
     * Creates a new instance
     * @param parent
     */
    public Popup(final Shell parent) {
        
        if (isNativeImplementationSupported()) return;

        // Listen for mouse down events in SWT components
        parent.getDisplay().addFilter(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                mouseDown(event.x, event.y);
            }
        });

        // Listen for mouse down in events in AWT components
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                MouseEvent me = (MouseEvent) event;
                if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                    mouseDown(me.getX(), me.getY());
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        
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
     * Mouse down callback
     * @param x
     * @param y
     */
    private void mouseDown(int x, int y) {
        if (shellBounds != null) {
            if (!shellBounds.contains(x, y)) {
                hide();
            }
        }
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
                        shellBounds = shell.getBounds();
                        bounds = new Rectangle((int)(shellBounds.x - THRESHOLD), 
                                               (int)(shellBounds.y - THRESHOLD), 
                                               (int)(shellBounds.width + THRESHOLD * 2), 
                                               (int)(shellBounds.height + THRESHOLD * 2));
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
                    shellBounds = null;
                }
            });
        }
    }
    
    /**
     * Is the native implementation supported on this platform
     * @return
     */
    protected boolean isNativeImplementationSupported(){

        String osName = System.getProperty("os.name");
        if (osName.contains("OS X")) return true;
        else if (osName.contains("Windows")) return true;
        else return false;
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