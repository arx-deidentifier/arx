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
 * This class implements a titled border.
 *
 * @author Fabian Prasser
 */
public class ComponentTitledBorder implements IComponent{
    
    /**  TODO */
    private final CTabFolder folder;
    
    /**  TODO */
    private final CTabItem tab; 
    
    /**
     * Creates a new instance.
     *
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
     * Returns the underlying control.
     *
     * @return
     */
    public Composite getControl(){
        return folder;
    }
    
    /**
     * Sets the child control.
     *
     * @param child
     */
    public void setChild(Control child){
        this.tab.setControl(child);
    }
    
    /**
     * Sets layout data.
     *
     * @param data
     */
    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }
}
