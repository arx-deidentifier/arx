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

package org.deidentifier.arx.gui.view.impl.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Supports interaction with the system clipboard
 * 
 * @author Fabian Prasser
 */
public class ClipboardHandlerTable {
    
    /** The table*/
    private final Table table;

    /**
     * Creates a new instance
     * @param table
     */
    public ClipboardHandlerTable(Table table){
        this.table = table;
    }
    
    /**
     * Copies the table's contents to the clipboard
     */
    public void copy(){
        if (table != null && table.getItemCount()>0) {
            Clipboard clipboard = new Clipboard(table.getDisplay());
            TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents(new String[]{getText(table)}, 
                                  new Transfer[]{textTransfer});
            clipboard.dispose();
        }
    }
    
    /**
     * Creates a pop up menu for this handler
     * @param parent
     * @return
     */
    public Menu getMenu() {
        Menu menu = new Menu(table);
        MenuItem itemCopy = new MenuItem(menu, SWT.NONE);
        itemCopy.setText("Copy");
        itemCopy.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                copy();
            }
        });
        return menu;
    }
    
    /**
     * Renders the table into a string
     * @param table
     */
    private String getText(Table table){
        
        List<String> properties = new ArrayList<String>();
        for (TableColumn column : table.getColumns()){
            properties.add(column.getText());
        }
        
        StringBuilder builder = new StringBuilder();
        
        for (TableItem item : table.getItems()) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
            int added = 0;
            for (int i=0; i<properties.size(); i++) {
                String value = item.getText(i);
                if (value != null && !value.equals("")) {
                    if (added!=0) {
                        builder.append(", ");
                    }
                    added++;
                    builder.append(properties.get(i)).append(": ").append(value);
                }
            }
        }
        
        return builder.toString();
    }
}
