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

package org.deidentifier.arx.gui.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides access to basic resources
 * @author Fabian Prasser
 */
public class Resources {

    private static final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("org.deidentifier.arx.gui.resources.messages"); //$NON-NLS-1$

    /**
     * Returns the logo
     * @return
     * @throws IOException
     */
	public static java.awt.Image getImageIcon() throws IOException {
		return ImageIO.read(Resources.class.getResourceAsStream("logo.png")); //$NON-NLS-1$
	}
    
    /** 
     * Returns the associated message
     * TODO: Make this method non-static
     */
    public static String getMessage(String key) {
        try {
            return MESSAGES_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Returns the splash image
     * @return
     * @throws IOException
     */
    public static java.awt.Image getSplash() throws IOException {
        return ImageIO.read(Resources.class.getResourceAsStream("splash.png")); //$NON-NLS-1$
    }

    /**
     * Returns the version
     * @return
     */
    public static String getVersion() {
        return Resources.getMessage("Resources.0"); //$NON-NLS-1$;
    }

    private final Logger logger = Logger.getRootLogger();

    private final Shell  shell;

    /**
     * Creates a new instance
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
     * Returns the display
     * @return
     */
    public Display getDisplay() {
        return shell.getDisplay();
    }

    /**
     * Returns the size of the gradient used in heatmaps
     * @return
     */
    public int getGradientLength() {
        return 256;
    }

    /**
     * Returns an image
     * @param name
     * @return
     */
    public Image getImage(final String name) {
        if (shell.isDisposed()) return null;
        return new Image(shell.getDisplay(), this.getClass()
                                                 .getResourceAsStream(name));
    }

    /**
     * Returns a stream
     * @param name
     * @return
     */
    public InputStream getStream(final String name) {
        return this.getClass().getResourceAsStream(name);
    }

    /**
     * Returns the logger
     * @return
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the shell
     * @return
     */
    public Shell getShell() {
        return shell;
    }
}
