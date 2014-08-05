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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class implements a titled border
 * @author Fabian Prasser
 */
public class ComponentTitledBorder implements IComponent{
    
    private final CTabFolder folder;
    private final CTabItem tab; 
    
    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param title
     * @param id
     */
    public ComponentTitledBorder(Composite parent, Controller controller, String title, String id){

        folder = new CTabFolder(parent, SWT.TOP | SWT.BORDER | SWT.FLAT);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(false);
        
        // Create help button
        SWTUtil.createHelpButton(controller, folder, id);

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
        
        // Create general tab
        tab = new CTabItem(folder, SWT.NULL);
        tab.setText(title);
        tab.setShowClose(false);

        folder.setSelection(tab);
    }
    
    /**
     * Returns the underlying control
     * @return
     */
    public Composite getControl(){
        return folder;
    }
    
    /**
     * Sets the child control
     * @param child
     */
    public void setChild(Control child){
        this.tab.setControl(child);
    }
    
    /**
     * Sets layout data
     * @param data
     */
    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }
}
