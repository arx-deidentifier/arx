/*
 * ARX: Powerful Data Anonymization
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

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements a splash window.
 *
 * @author Fabian Prasser, Florian Kohlmayer
 */
public class MainSplash {

    /** Version. */
    private final String version;
    
    /** Splash. */
    private final Image splash;
    
    /** Shell. */
    private final Shell shell;

    /**
     * Creates a new instance.
     *
     * @param display
     * @param monitor
     */
    public MainSplash(Display display, Monitor monitor) {
        
        this.version = Resources.getVersion();
        this.splash = Resources.getSplash(display);
        this.shell = new Shell(SWT.ON_TOP | (isMac() ? 0 : SWT.NO_TRIM));
        this.shell.setImages(Resources.getIconSet(display));
        this.shell.setSize(splash.getBounds().width, splash.getBounds().height);
        
        // Center
        SWTUtil.center(shell, monitor);
        
        // Paint
        shell.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent arg0) {
                paint(arg0.gc);
            }
        });
    }

    /**
     * Paint.
     *
     * @param gc
     */
    private void paint(GC gc) {
    	Point size = shell.getSize();
        Point offsets = gc.textExtent(version);
        gc.setAdvanced(true);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.ON);
        gc.drawImage(splash, 0,  0, splash.getBounds().width, splash.getBounds().height, 0, 0, size.x,  size.y);
        gc.setForeground(GUIHelper.COLOR_BLACK);
        gc.drawString(version, size.x - (int)offsets.x - 10, size.y - (int)offsets.y - 10, true);
    }

    /**
     * Shows the splash. It is automatically hidden after 2000 ms
     */
    public void show() {
        
        // Check
        if (this.shell == null || this.shell.isDisposed()) {
            return;
        }
        
        // Show
        this.shell.open();
        
        // Automatically hide the splash screen
        Display.getCurrent().timerExec(2000, new Runnable(){
            public void run(){
                hide();
            }
        });
    }

    /**
     * Is this shell disposed.
     *
     * @return
     */
    public boolean isDisposed() {
        return this.shell == null || this.shell.isDisposed();
    }

    /**
     * Disposes the shell.
     */
    public void hide() {
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    /**
     * Detect os x.
     *
     * @return
     */
    private boolean isMac() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }
}
