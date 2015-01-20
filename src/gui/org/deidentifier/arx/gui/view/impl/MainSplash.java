/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
