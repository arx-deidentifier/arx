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

package org.deidentifier.arx.gui.view.impl;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class implements a global tool tip
 * @author Fabian Prasser
 */
public class MainToolTip {

    private final Shell shell;
    private final Text  text;

    /**
     * Creates a new instance
     * @param parent
     */
    public MainToolTip(final Shell parent) {
        shell = new Shell(parent, SWT.TOOL | SWT.ON_TOP);
        shell.setLayout(new GridLayout());
        text = new Text(shell, SWT.MULTI);
        text.setLayoutData(SWTUtil.createFillGridData());
        text.setBackground(shell.getBackground());
        shell.pack();
        shell.setVisible(false);
        shell.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(final MouseEvent arg0) {
                hide();
            }
        });
    }

    /**
     * Hides the tool tip
     */
    public void hide() {
        shell.setVisible(false);
    }

    /**
     * Sets the text of the tool tip
     * @param message
     */
    public void setText(final String message) {
        text.setText(message);
        shell.pack();
    }

    /**
     * Shows the tool tip
     * @param x
     * @param y
     */
    public void show(final int x, final int y) {
        shell.setLocation(x, y);
        shell.setVisible(true);
    }
}
