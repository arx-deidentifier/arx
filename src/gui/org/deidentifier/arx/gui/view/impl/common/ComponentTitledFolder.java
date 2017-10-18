/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This class implements a titled folder.
 *
 * @author Fabian Prasser
 */
public class ComponentTitledFolder implements IComponent {

    /**
     * An entry in a folder
     * 
     * @author Fabian Prasser
     */
    private class TitledFolderEntry {

        /** Field */
        private String  text;
        /** Field */
        private Control control;
        /** Field */
        private Image   image;
        /** Field */
        private int     index;
        /** Field */
        private boolean hideable;
        
        /**
         * Creates a new instance
         * @param text
         * @param control
         * @param image
         * @param index
         * @param hideable
         */
        public TitledFolderEntry(String text, Control control, Image image, int index, boolean hideable) {
            this.text = text;
            this.control = control;
            this.image = image;
            this.index = index;
            this.hideable = hideable;
        }
    }

    /** Entries*/
    private List<TitledFolderEntry>    entries = new ArrayList<TitledFolderEntry>();

    /** The folder */
    private final CTabFolder           folder;

    /** Flag */
    private final boolean              hasHidingMenu;

    /** Listener */
    private SelectionListener          itemVisibilityListener;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param bar
     * @param id
     */
    public ComponentTitledFolder(Composite parent, Controller controller, ComponentTitledFolderButtonBar bar, String id){
        this(parent, controller, bar, id, null, false, false);
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
    public ComponentTitledFolder(Composite parent, 
                                 Controller controller, 
                                 ComponentTitledFolderButtonBar bar, 
                                 String id, 
                                 boolean bottom,
                                 boolean supportsHidingElements){
        this(parent, controller, bar, id, null, bottom, supportsHidingElements);
    }

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param bar
     * @param id
     * @param helpids
     */
    public ComponentTitledFolder(Composite parent, Controller controller, ComponentTitledFolderButtonBar bar, String id, Map<Composite, String> helpids){
        this(parent, controller, bar, id, helpids, false, false);
    }

    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param bar
     * @param id
     * @param bottom
     * @param hasHidingMenu
     */
    public ComponentTitledFolder(Composite parent, 
                                 Controller controller, 
                                 ComponentTitledFolderButtonBar bar, 
                                 String id, 
                                 Map<Composite, String> helpids,
                                 boolean bottom,
                                 boolean hasHidingMenu){

        int flags = SWT.BORDER | SWT.FLAT;
        if (bottom) flags |= SWT.BOTTOM;
        else flags |= SWT.TOP;
        
        this.hasHidingMenu = hasHidingMenu;
        
        this.folder = new CTabFolder(parent, flags);
        this.folder.setUnselectedCloseVisible(false);
        this.folder.setSimple(false);
        
        // Create help button
        if (bar != null || controller != null) {
            if (bar == null) SWTUtil.createHelpButton(controller, folder, id, helpids);
            else createBar(controller, folder, bar);
        }

        // Prevent closing
        this.folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
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
     * @return
     */
    public Composite createItem(String title, Image image){
        return createItem(title, image, getItemCount(), false);
    }

    /**
     * Creates a new entry in the folder.
     *
     * @param title
     * @param image
     * @param hideable
     * @return
     */
    public Composite createItem(String title, Image image, boolean hideable){
        return createItem(title, image, hideable, new GridLayout());
    }
    
    /**
     * Creates a new entry in the folder.
     * 
     * @param title
     * @param image
     * @param hideable
     * @param layout
     * @return
     */
    public Composite createItem(String title, Image image, boolean hideable, Layout layout) {
        return createItem(title, image, getItemCount(), hideable, layout);
    }

    /**
     * Creates a new entry in the folder.
     *
     * @param title
     * @param image
     * @param index
     * @param hideable
     * @return
     */
    public Composite createItem(String title, Image image, int index, boolean hideable){
        return createItem(title, image, index, hideable, new GridLayout());
    }
    
    /**
     * Creates a new entry in the folder.
     *  
     * @param title
     * @param image
     * @param index
     * @param hideable
     * @param layout
     * @return
     */
    public Composite createItem(String title, Image image, int index, boolean hideable, Layout layout) {
        
        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(layout);
        
        CTabItem item = new CTabItem(folder, SWT.NULL, index);
        item.setText(title);
        if (image!=null) item.setImage(image);
        item.setShowClose(false);
        item.setControl(composite);
        entries.add(new TitledFolderEntry(title, composite, image, index, hideable));
        return composite;
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
     * Returns the selected control
     * @return
     */
    public Control getSelectedControl() {
        return folder.getSelection().getControl();
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
     * @return
     * @see org.eclipse.swt.widgets.Control#getSize()
     */
    public Point getSize() {
        return folder.getSize();
    }

    /**
     * Returns all visible items
     * @return
     */
    public List<String> getVisibleItems() {
        List<String> result = new ArrayList<String>();
        for (CTabItem item : folder.getItems()) {
            result.add(item.getText());
        }
        return result;
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
     * Sets the item visibility listener
     * @param listener
     */
    public void setItemVisibilityListener(SelectionListener listener) {
        this.itemVisibilityListener = listener;
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
     * Selects the item with the given control
     * @param c
     */
    public void setSelectedControl(Control c) {
        for (CTabItem item : folder.getItems()) {
            if (item.getControl() == c) {
                folder.setSelection(item);
                return;
            }
        }
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
     * Sets the according item visible
     * @param item
     * @param visible
     */
    public void setVisible(String item, boolean visible) {
        boolean changed = false;
        if (visible) {
            changed = this.setVisible(item);
        } else {
            changed = this.setInvisible(item);
        }
        if (changed && this.itemVisibilityListener != null) {
            Event event = new Event();
            event.widget = this.folder;
            this.itemVisibilityListener.widgetSelected(new SelectionEvent(event));
        }
    }

    /**
     * Sets the given items as visible
     * @param item
     */
    public void setVisibleItems(List<String> items) {
        
        boolean changed = false;
        
        for (String item : getAllHideableItems()) {
            if (items.contains(item)) {
                changed |= setVisible(item);
                if (this.folder.getItemCount() == 1) {
                    this.folder.setSelection(0);
                }
            } else {
                changed |= setInvisible(item);
            }
        }
        
        if (changed && this.itemVisibilityListener != null) {
            Event event = new Event();
            event.widget = this.folder;
            this.itemVisibilityListener.widgetSelected(new SelectionEvent(event));
        }
    }

    /**
     * Creates the bar .
     *
     * @param controller
     * @param folder
     * @param bar
     */
    private void createBar(final Controller controller, final CTabFolder folder, final ComponentTitledFolderButtonBar bar) {
        ToolBar toolbar = new ToolBar(folder, SWT.FLAT);
        folder.setTopRight( toolbar, SWT.RIGHT );

        if (this.hasHidingMenu) {

            ToolItem item = new ToolItem( toolbar, SWT.PUSH );
            item.setImage(controller.getResources().getManagedImage("manage.png"));  //$NON-NLS-1$
            item.setToolTipText(Resources.getMessage("General.1")); //$NON-NLS-1$
            SWTUtil.createDisabledImage(item);
            item.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    List<String> result = controller.actionShowMultiSelectionDialog(folder.getShell(),
                                                                                    Resources.getMessage("ComponentTitledFolder.0"), //$NON-NLS-1$
                                                                                    Resources.getMessage("ComponentTitledFolder.1"), //$NON-NLS-1$
                                                                                    getAllHideableItems(),
                                                                                    getVisibleItems());
                    
                    if (result != null) {
                        setVisibleItems(result);
                    }
                }
            });
        }
        
        for (String title : bar.getTitles()){
            
            final String key = title;
            ToolItem item = null;
            if (bar.isToggle(title)) item = new ToolItem( toolbar, SWT.CHECK);
            else item = new ToolItem( toolbar, SWT.PUSH);
            item.setImage(bar.getImage(key));
            item.setToolTipText(title);
            SWTUtil.createDisabledImage(item);
            item.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    bar.getRunnable(key).run();
                }
            });
        }
        
        if (bar.getHelpId() != null || (bar.getHelpIds() != null && !bar.getHelpIds().isEmpty())) {
            ToolItem item = new ToolItem( toolbar, SWT.PUSH );
            item.setImage(controller.getResources().getManagedImage("help.png"));  //$NON-NLS-1$
            item.setToolTipText(Resources.getMessage("General.0")); //$NON-NLS-1$
            SWTUtil.createDisabledImage(item);
            item.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    if (bar.getHelpIds() == null || bar.getHelpIds().get(folder.getSelection().getControl()) == null) {
                        controller.actionShowHelpDialog(bar.getHelpId());
                    } else {
                        controller.actionShowHelpDialog(bar.getHelpIds().get(folder.getSelection().getControl()));
                    }
                }
            });
        }   
        int height = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        folder.setTabHeight(Math.max(height, folder.getTabHeight()));
    }
    
    /**
     * Returns all items
     * @return
     */
    private List<String> getAllHideableItems() {
        List<String> result = new ArrayList<String>();
        for (TitledFolderEntry entry : this.entries) {
            if (entry.hideable) {
                result.add(entry.text);
            }
        }
        return result;
    }

    /**
     * Returns a list of all invisible entries
     * @return
     */
    private List<TitledFolderEntry> getInvisibleEntries() {
        List<TitledFolderEntry> result = new ArrayList<TitledFolderEntry>();
        result.addAll(this.entries);
        for (CTabItem item : folder.getItems()){
            Iterator<TitledFolderEntry> iter = result.iterator();
            while (iter.hasNext()) {
                if (item.getText().equals(iter.next().text)) {
                    iter.remove();
                }
            }
        }
        return result;
    }
    
    /**
     * Sets the given item invisible
     * @param item
     */
    private boolean setInvisible(String text) {
        for (CTabItem item : folder.getItems()){
            label: if (item.getText().equals(text)) {
                for (TitledFolderEntry entry : this.entries) {
                    if (entry.text.equals(text) && !entry.hideable) {
                        break label;
                    }
                }
                item.dispose();
                return true;
            }
        }
        return false;
    }

    /**
     * Sets an entry visible
     * @return
     */
    private boolean setVisible(String text) {
        List<TitledFolderEntry> list = getInvisibleEntries();
        
        // Find
        for (TitledFolderEntry entry : list) {
            if (entry.text.equals(text)) {

                // Shift
                int index = entry.index;
                for (TitledFolderEntry other : list) {
                    if (other.index < entry.index) {
                        index--;
                    }
                }
                
                // Show
                CTabItem item = new CTabItem(folder, SWT.NULL, index);
                item.setText(entry.text);
                if (entry.image!=null) item.setImage(entry.image);
                item.setShowClose(false);
                item.setControl(entry.control);
                return true;
            }
        }
        return false;
    }
}
