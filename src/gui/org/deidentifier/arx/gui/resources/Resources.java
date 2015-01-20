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

package org.deidentifier.arx.gui.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides access to basic resources.
 *
 * @author Fabian Prasser
 */
public class Resources {

    /**
     * Returns the logo.
     *
     * @param display
     * @return
     */
	public static Image[] getIconSet(Display display) {
	    
	    if (iconset == null){
    	    int[] sizes = new int[]{16,24,32,48,64,96,128,256};
    	    iconset = new Image[sizes.length];
    	    int idx = 0;
    	    for (int size : sizes){
    	        iconset[idx++] = getImage(display, "logo_"+size+".png"); //$NON-NLS-1$ //$NON-NLS-2$
    	    }
	    }
	    return iconset;
	}
    
    /**
     * 
     * Returns the associated message
     * TODO: Make this method non-static.
     *
     * @param key
     * @return
     */
    public static String getMessage(String key) {
        try {
            return MESSAGES_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    /**
     * Returns the splash image.
     *
     * @param display
     * @return
     */
    public static Image getSplash(Display display) {
        if (splash == null) {
            splash = getImage(display, "splash.png"); //$NON-NLS-1$
        }
        return splash;
    }

    /**
     * Returns the version.
     *
     * @return
     */
    public static String getVersion() {
        return Resources.getMessage("Resources.0"); //$NON-NLS-1$;
    }
    
    /**
     * Loads an image. Adds a dispose listener that disposes the image when the display is disposed
     * @param display
     * @param resource
     * @return
     */
    private static final Image getImage(Display display, String resource){
        final Image image = new Image(display, Resources.class.getResourceAsStream(resource));
        display.addListener(SWT.Dispose, new Listener(){
            public void handleEvent(Event arg0) {
                if (image != null && !image.isDisposed()) {
                    image.dispose();
                }
            }
        });
        return image;
    }

    /**  TODO */
    private static final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("org.deidentifier.arx.gui.resources.messages"); //$NON-NLS-1$

    /** The splash. */
    private static Image splash = null;
    
    /** The iconset. */
    private static Image[] iconset = null;

    /** Logger. */
    private final Logger logger = Logger.getRootLogger();

    /** Shell. */
    private final Shell  shell;
    
    /**
     * Creates a new instance.
     *
     * @param shell
     */
    public Resources(final Shell shell) {

        this.shell = shell;
        
        // Release config
        SimpleLayout layout = new SimpleLayout();
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        logger.addAppender(consoleAppender);
        logger.setLevel(Level.OFF);
    }
    
    /**
     * Returns the display.
     *
     * @return
     */
    public Display getDisplay() {
        return shell.getDisplay();
    }

    /**
     * Returns the size of the gradient used in heatmaps.
     *
     * @return
     */
    public int getGradientLength() {
        return 256;
    }

    /**
     * Returns an image.
     *
     * @param name
     * @return
     */
    public Image getImage(final String name) {
        if (shell.isDisposed()) return null;
        return new Image(shell.getDisplay(), this.getClass()
                                                 .getResourceAsStream(name));
    }

    /**
     * Returns the logger.
     *
     * @return
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the shell.
     *
     * @return
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * Returns a stream.
     *
     * @param name
     * @return
     */
    public InputStream getStream(final String name) {
        return this.getClass().getResourceAsStream(name);
    }

    /**
     * Reads the content from the file license.txt located in the package org.deidentifier.arx.gui.resources and
     * returns the content as string.
     * @return
     */
    public static String getLicencseText() {
        InputStream stream = Resources.class.getResourceAsStream("license.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String content = "";
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            content = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }
}
