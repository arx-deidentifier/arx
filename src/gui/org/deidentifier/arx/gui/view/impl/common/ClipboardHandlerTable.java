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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
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
 * Supports interaction with the system clipboard.
 *
 * @author Fabian Prasser
 */
public class ClipboardHandlerTable {
    
    /** The table. */
    private final Table table;

    /**
     * Creates a new instance.
     *
     * @param table
     */
    public ClipboardHandlerTable(Table table){
        this.table = table;
    }
    
    /**
     * Copies the table's contents to the clipboard.
     */
    public void copy(){
        if (table != null && table.getItemCount()>0) {
            Clipboard clipboard = new Clipboard(table.getDisplay());
            TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents(new String[]{getText(table)}, new Transfer[]{textTransfer});
            clipboard.dispose();
        }
    }
    
    /**
     * Creates a pop up menu for this handler.
     *
     * @return
     */
    public Menu getMenu() {
        Menu menu = new Menu(table);
        MenuItem itemCopy = new MenuItem(menu, SWT.NONE);
        itemCopy.setText(Resources.getMessage("ClipboardHandlerTable.0")); //$NON-NLS-1$
        itemCopy.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                copy();
            }
        });
        return menu;
    }
    
    /**
     * Renders the table into a string.
     *
     * @param table
     * @return
     */
    private String getText(Table table){

        StringBuilder builder = new StringBuilder();
        
        TableColumn[] columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            TableColumn column = columns[i];
            builder.append(column.getText());
            if (i < columns.length - 1) {
                builder.append(";");
            } else {
                builder.append("\n");
            }
        }
        
        for (TableItem item : table.getItems()) {
            for (int i = 0; i < columns.length; i++) {
                String value = item.getText(i);
                if (value != null && !value.equals("")) { //$NON-NLS-1$
                    builder.append(value); //$NON-NLS-1$
                } else if (item.getData(String.valueOf(i)) != null && 
                           item.getData(String.valueOf(i)) instanceof Double) {
                    builder.append(SWTUtil.getPrettyString(((Double) item.getData(String.valueOf(i))).doubleValue() * 100d) + "%"); //$NON-NLS-1$
                }
                if (i < columns.length - 1) {
                    builder.append(";");
                } else {
                    builder.append("\n");
                }
            }
        }
        
        return builder.toString();
    }
}
