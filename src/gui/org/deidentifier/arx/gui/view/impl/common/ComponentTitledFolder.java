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
 * This class implements a titled folder
 * @author Fabian Prasser
 */
public class ComponentTitledFolder implements IComponent {
    
    private final CTabFolder folder;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param bar
     * @param id
     */
    public ComponentTitledFolder(Composite parent, Controller controller, ComponentTitledFolderButton bar, String id){
        this(parent, controller, bar, id, false);
    }

    /**
     * Creates a new instance
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
     * Adds a selection listener
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        folder.addSelectionListener(listener);
    }
    
    /**
     * Creates the bar 
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

    /**
     * Creates a new entry in the folder
     * @param title
     * @param image
     * @return
     */
    public Composite createItem(String title, Image image){

        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new GridLayout());
        
        CTabItem item = new CTabItem(folder, SWT.NULL);
        item.setText(title);
        if (image!=null) item.setImage(image);
        item.setShowClose(false);
        item.setControl(composite);
        
        return composite;
    }
    
    /**
     * Returns the tab item for the given text
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
     * Returns the button item for the given text
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
     * Returns the currently selected index
     * @return
     */
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    /**
     * Enables/disables the component
     * @param b
     */
    public void setEnabled(boolean b) {
        folder.setEnabled(b);
    }

    /**
     * Sets layout data
     * @param data
     */
    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }

    /**
     * Sets the current selection
     * @param index
     */
    public void setSelection(int index) {
        folder.setSelection(index);
    }

    /**
     * Disposes the given item
     * @param string
     */
    public void disposeItem(String text) {
        for (CTabItem item : folder.getItems()) {
            if (item.getText().equals(text)) {
                item.dispose();
            }
        }
    }
}
