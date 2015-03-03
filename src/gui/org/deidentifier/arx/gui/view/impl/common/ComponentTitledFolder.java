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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This class implements a titled folder.
 *
 * @author Fabian Prasser
 */
public class ComponentTitledFolder implements IComponent {
    
    /**  TODO */
    private final CTabFolder folder;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param bar
     * @param id
     */
    public ComponentTitledFolder(Composite parent, Controller controller, ComponentTitledFolderButton bar, String id){
        this(parent, controller, bar, id, false);
    }

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param bar
     * @param id
     * @param bottom
     */
    public ComponentTitledFolder(Composite parent, Controller controller, ComponentTitledFolderButton bar, String id, boolean bottom){

        int flags = SWT.BORDER | SWT.FLAT;
        if (bottom) flags |= SWT.BOTTOM;
        else flags |= SWT.TOP;
        
        folder = new CTabFolder(parent, flags);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(false);
        
        // Create help button
        if (bar == null) SWTUtil.createHelpButton(controller, folder, id);
        else createBar(controller, folder, bar);

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
    }
    
    /**
     * Adds a selection listener.
     *
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        folder.addSelectionListener(listener);
    }

    /**
     * Creates a new entry in the folder.
     *
     * @param title
     * @param image
     * @param index
     * @return
     */
    public Composite createItem(String title, Image image, int index){

        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new GridLayout());
        
        CTabItem item = new CTabItem(folder, SWT.NULL, index);
        item.setText(title);
        if (image!=null) item.setImage(image);
        item.setShowClose(false);
        item.setControl(composite);
        
        return composite;
    }

    /**
     * Creates a new entry in the folder.
     *
     * @param title
     * @param image
     * @return
     */
    public Composite createItem(String title, Image image){
        return createItem(title, image, getItemCount());
    }

    /**
     * Disposes the given item.
     *
     * @param text
     */
    public void disposeItem(String text) {
        for (CTabItem item : folder.getItems()) {
            if (item.getText().equals(text)) {
                item.dispose();
            }
        }
    }
    
    /**
     * Returns the button item for the given text.
     *
     * @param text
     * @return
     */
    public ToolItem getButtonItem(String text) {
        Control c = folder.getTopRight();
        if (c == null) return null;
        if (!(c instanceof ToolBar)) return null;
        ToolBar t = (ToolBar)c;
        for (ToolItem i : t.getItems()){
            if (i.getToolTipText().equals(text)) return i;
        }
        return null;
    }

    /**
     * Returns the number of items in the folder.
     *
     * @return
     */
    public int getItemCount() {
        return folder.getItemCount();
    }

    /**
     * Returns the currently selected index.
     *
     * @return
     */
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    /**
     * Returns the tab item for the given text.
     *
     * @param text
     * @return
     */
    public CTabItem getTabItem(String text) {
        for (CTabItem item : folder.getItems()){
            if (item.getText().equals(text)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Enables/disables the component.
     *
     * @param b
     */
    public void setEnabled(boolean b) {
        folder.setEnabled(b);
    }

    /**
     * Sets layout data.
     *
     * @param data
     */
    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }

    /**
     * Sets the current selection.
     *
     * @param index
     */
    public void setSelection(int index) {
        folder.setSelection(index);
    }

    /**
     * Creates the bar .
     *
     * @param controller
     * @param folder
     * @param bar
     */
    private void createBar(final Controller controller, final CTabFolder folder, final ComponentTitledFolderButton bar) {
        ToolBar toolbar = new ToolBar(folder, SWT.FLAT);
        folder.setTopRight( toolbar, SWT.RIGHT );
        
        for (String title : bar.getTitles()){
            
            final String key = title;
            ToolItem item = null;
            if (bar.isToggle(title)) item = new ToolItem( toolbar, SWT.CHECK);
            else item = new ToolItem( toolbar, SWT.PUSH);
            item.setImage(bar.getImage(key));
            item.setToolTipText(title);
            item.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    bar.getRunnable(key).run();
                }
            });
        }
        
        ToolItem item = new ToolItem( toolbar, SWT.PUSH );
        item.setImage(controller.getResources().getImage("help.png"));  //$NON-NLS-1$
        item.setToolTipText(Resources.getMessage("General.0")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                controller.actionShowHelpDialog(bar.getId());
            }
        });
        
        int height = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        folder.setTabHeight(Math.max(height, folder.getTabHeight()));
    }
}
