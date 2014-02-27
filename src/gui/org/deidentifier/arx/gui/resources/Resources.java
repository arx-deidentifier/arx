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

package org.deidentifier.arx.gui.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class Resources {

    private static final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("org.deidentifier.arx.gui.resources.messages"); //$NON-NLS-1$
    private static final ResourceBundle FORMATS_BUNDLE  = ResourceBundle.getBundle("org.deidentifier.arx.gui.resources.formats"); //$NON-NLS-1$

    private final List<String>          DATE_FORMATS    = new ArrayList<String>();

    private Logger                      LOGGER          = Logger.getRootLogger();
    
    public Resources(final Shell shell) {

        this.shell = shell;

        DATE_FORMATS.add(this.getFormat("Formats.1")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.2")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.3")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.4")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.5")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.6")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.7")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.8")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.9")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.10")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.11")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.12")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.13")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.14")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.15")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.16")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.17")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.18")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.19")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.20")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.21")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.22")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.23")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.24")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.25")); //$NON-NLS-1$
        DATE_FORMATS.add(this.getFormat("Formats.26")); //$NON-NLS-1$

        // Release config
        SimpleLayout layout = new SimpleLayout();
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        LOGGER.addAppender(consoleAppender);
        LOGGER.setLevel(Level.OFF);
    }

    private String getFormat(String key) {
        try {
            return FORMATS_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /*
     * TODO: Make this method non-static
     */
    public static String getMessage(String key) {
        try {
            return MESSAGES_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    private final Shell shell;

    public List<String> getDateFormats() {
        return DATE_FORMATS;
    }

    public Display getDisplay() {
        return shell.getDisplay();
    }

    public Image getImage(final String name) {
        return new Image(shell.getDisplay(), this.getClass()
                                                 .getResourceAsStream(name));
    }

    public static java.awt.Image getSplash() throws IOException {
        return ImageIO.read(Resources.class.getResourceAsStream("splash.png")); //$NON-NLS-1$
    }

    
    public Logger getLogger() {
        return LOGGER;
    }

    public Shell getShell() {
        return shell;
    }

    public static String getVersion() {
        return Resources.getMessage("Resources.0"); //$NON-NLS-1$;
    }

    public int getGradientLength() {
        return 256;
    }

	public static java.awt.Image getImageIcon() throws IOException {
		return ImageIO.read(Resources.class.getResourceAsStream("logo.png")); //$NON-NLS-1$
	}
}
