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

package org.deidentifier.arx.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.deidentifier.arx.gui.view.impl.MainWindow;
import org.deidentifier.arx.gui.view.impl.MainSplash;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

/**
 * Main entry point
 * 
 * @author Prasser, Kohlmayer
 */
public class Main {

	private static final String JDK16_FRAME = "apple.awt.CEmbeddedFrame";
	private static final String JDK17_FRAME = "sun.lwawt.macosx.CViewEmbeddedFrame";
	
    private static MainSplash splash = null;

    public static void main(final String[] args) {

        try {

            if (!isOSX()) {
                splash = new MainSplash();
                splash.setVisible(true);
            } else {
            	try {
            		Class.forName(JDK16_FRAME);
            	} catch (Exception e){
            		SWT_AWT.embeddedFrameClass = JDK17_FRAME;
            	}
            }

            System.setProperty("sun.awt.noerasebackground", "true"); //$NON-NLS-1$ //$NON-NLS-2$

            MainWindow main = new MainWindow();
            main.addShellListener(new ShellAdapter() {
                @Override
                public void shellActivated(ShellEvent arg0) {
                    hideSplash();
                }
            });
            main.show();

        } catch (Throwable e) {
            
            hideSplash();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            final String trace = sw.toString();

            JOptionPane.showMessageDialog(null, trace, "Unexpected error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        }
    }

    private static void hideSplash() {
        new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (splash != null) splash.setVisible(false);
            }
        }).start();
    }

    private static boolean isOSX() {
        String osName = System.getProperty("os.name");
        return osName.contains("OS X");
    }
}
