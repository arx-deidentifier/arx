/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
    public static void main(Display display, final String[] args) {

        try {
            // Make fall-back toolkit look like the native UI
            if (!isUnix()) { // See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=341799
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Ignore
        }
        
        try {
        	
            // Update display settings
            Display.setAppName(Resources.getMessage("MainWindow.0"));
            Display.setAppVersion(Resources.getVersion());
            
            // Display
            if (display == null) {
                display = new Display();
            }
            
            // Monitor
            Monitor monitor = getMonitor(display);
            
            // Splash
            splash = new MainSplash(display, monitor);
            splash.show();
            
            // Create main window
            main = new MainWindow(display, monitor);

            // Handler for loading a project
            if (args.length > 0 && args[0].endsWith(".deid")) { //$NON-NLS-1$
                main.onShow(new Runnable() {
                    public void run(){
                        load(main, args[0]);
                    }
                });
            }
            
            // Show window
            main.show();
            
            // Check for updates
            new Update(main.getShell());
            
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
            JOptionPane.showMessageDialog(null, trace, "Unexpected error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            System.exit(1);

        }
    }

    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        main(null, args);
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
     * Determine os
     * @return
     */
    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
