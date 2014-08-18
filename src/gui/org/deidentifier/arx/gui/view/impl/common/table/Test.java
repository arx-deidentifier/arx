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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Test {

    public static void main(String[] args) {

        // Create display and shell
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Test");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());
        
        // Configure table
        CTConfiguration config = new CTConfiguration(shell, CTConfiguration.STYLE_TABLE);
        config.setHorizontalAlignment(SWT.LEFT);
        config.setCellSelectionEnabled(true);
        config.setColumnSelectionEnabled(true);
        config.setRowSelectionEnabled(false);
        config.setColumnHeaderLayout(CTConfiguration.COLUMN_HEADER_LAYOUT_GRAB_EQUAL);
        config.setRowHeaderLayout(CTConfiguration.ROW_HEADER_LAYOUT_FILL);

        String[][] data = new String[][]{{"a","b","c"},{"a","b","c"},{"a","b","c"}};
        
        // Create table
        ComponentTable table = new ComponentTable(shell, SWT.BORDER, config);
        table.setData(data);

        // Open
        shell.open();

        // Event loop
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }
}
