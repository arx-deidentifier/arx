/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Resources {

    private static final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("org.deidentifier.flash.gui.resources.messages");
    private static final ResourceBundle FORMATS_BUNDLE  = ResourceBundle.getBundle("org.deidentifier.flash.gui.resources.formats");

    private static final String         VERSION         = Resources.getMessage("Resources.0");                                      //$NON-NLS-1$

    private static final List<String>   DATE_FORMATS    = new ArrayList<String>();

    private static Logger               LOGGER          = Logger.getRootLogger();

    public static String getFormat(String key) {
        try {
            return FORMATS_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getMessage(String key) {
        try {
            return MESSAGES_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    private final Shell shell;

    static {
        DATE_FORMATS.add(Resources.getFormat("Formats.1")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.2")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.3")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.4")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.5")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.6")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.7")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.8")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.9")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.10")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.11")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.12")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.13")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.14")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.15")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.16")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.17")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.18")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.19")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.20")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.21")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.22")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.23")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.24")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.25")); //$NON-NLS-1$
        DATE_FORMATS.add(Resources.getFormat("Formats.26")); //$NON-NLS-1$

        // Release config
        SimpleLayout layout = new SimpleLayout();
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        LOGGER.addAppender(consoleAppender);
        LOGGER.setLevel(Level.OFF);
    }

    public Resources(final Shell shell) {
        this.shell = shell;
    }

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

    public Logger getLogger() {
        return LOGGER;
    }

    public Shell getShell() {
        return shell;
    }

    public String getVersion() {
        return VERSION;
    }

    public int getGradientLength() {
        return 256;
    }
}
