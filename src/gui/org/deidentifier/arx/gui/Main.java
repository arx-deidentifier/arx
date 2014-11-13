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

package org.deidentifier.arx.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.MainSplash;
import org.deidentifier.arx.gui.view.impl.MainWindow;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

/**
 * Main entry point.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Main {

    /** Is the project already loaded. */
    private static String     loaded = null;
    
    /** The splash. */
    private static MainSplash splash = null;
    
    /** The main window. */
    private static MainWindow main   = null;

    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(final String[] args) {

        try {
            
            // Display
            Display display = new Display();
            
            // Monitor
            Monitor monitor = getMonitor(display);
            
            // Splash
            splash = new MainSplash(display, monitor);
            splash.show();
            
            // Main window
            main = new MainWindow(display, monitor);
            main.show();

            // Handler for loading a project
            if (args.length > 0 && args[0].endsWith(".deid")) {
                main.onShow(new Runnable() {
                    public void run(){
                        load(main, args[0]);
                    }
                });
            }
            
            // Main event loop
            while (!main.isDisposed()) {
                try {
                    
                    // Event handling                    
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                } catch (final Exception e) {
                    
                    // Error handling
                    main.showErrorDialog(Resources.getMessage("MainWindow.9") + Resources.getMessage("MainWindow.10"), e); //$NON-NLS-1$ //$NON-NLS-2$
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    main.getController().getResources().getLogger().info(sw.toString());
                }
            }
            
            // Dispose display
            if (!display.isDisposed()) {
                display.dispose();
            }
        } catch (Throwable e) {

            // Error handling outside of SWT
            if (splash != null) splash.hide();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            final String trace = sw.toString();

            // Show message
            JOptionPane.showMessageDialog(null, trace, "Unexpected error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        }
    }

    /**
     * Returns the monitor on which the application was launched.
     *
     * @param display
     * @return
     */
    private static Monitor getMonitor(Display display) {
        Point mouse = display.getCursorLocation();
        for (Monitor monitor : display.getMonitors()) {
            if (monitor.getBounds().contains(mouse)) {
                return monitor;
            }
        }
        return display.getPrimaryMonitor();
    }

    /**
     * Loads a project.
     *
     * @param main
     * @param path
     */
    private static void load(MainWindow main, String path) {
        if (loaded == null) {
            loaded = path;
            if (splash != null) splash.hide();
            main.getController().actionOpenProject(path);
        }
    }
}
