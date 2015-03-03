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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.Controller;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This class implements a table, in which properties can be filtered.
 *
 * @author Fabian Prasser
 */
public class ComponentFilterTable {

    /** Constant. */
    private static final int                  LABEL_WIDTH      = 100;
    
    /** Constant. */
    private static final int                  CHECKBOX_WIDTH   = 20;

    /** Image. */
    private final Image                       IMAGE_ENABLED;
    
    /** Image. */
    private final Image                       IMAGE_DISABLED;

    /** Widget. */
    private Table                             table;
    
    /** Widgets. */
    private Map<String, TableItem>            items;
    
    /** The registered listeners. */
    private List<SelectionListener>           listeners;
    
    /** The selection map. */
    private Map<String, Map<String, Boolean>> selected;
    
    /** The list of properties. */
    private Map<String, List<String>>         itemProperties;
    
    /** The list of properties. */
    private List<String>                      properties;
    
    /** The list of entries. */
    private List<String>                      entries;
    
    /** Selected entry. */
    private String                            selectedEntry    = null;
    
    /** Selected property. */
    private String                            selectedProperty = null;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param properties
     */
    public ComponentFilterTable(Composite parent, 
                                Controller controller, 
                                List<String> properties) {
        
        IMAGE_ENABLED = controller.getResources().getImage("tick.png");
        IMAGE_DISABLED = controller.getResources().getImage("cross.png");
        
        this.listeners = new ArrayList<SelectionListener>();
        this.selected = new HashMap<String, Map<String, Boolean>>();
        this.properties = new ArrayList<String>(properties);
        this.entries = new ArrayList<String>();
        this.items = new HashMap<String, TableItem>();
        this.itemProperties = new HashMap<String, List<String>>();
        this.table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.table.setHeaderVisible(true);
        this.setProperties(properties);
        
        table.addMouseListener(new MouseAdapter(){
            public void mouseDown(MouseEvent arg0) {
                int row = getItemRowAt(arg0.x, arg0.y);
                int column = getItemColumnAt(arg0.x, arg0.y);
                if (row != -1 && column > 0 && column <= ComponentFilterTable.this.properties.size()) {
                    String property = ComponentFilterTable.this.properties.get(column-1);
                    String entry = ComponentFilterTable.this.entries.get(row);
                    if (itemProperties.get(entry).contains(property)) {
                        selectedProperty = property;
                        selectedEntry = entry;
                    } else {
                        selectedProperty = null;
                        selectedEntry = null;
                    }
                } else {
                    selectedProperty = null;
                    selectedEntry = null;
                }
            }
        });
        
        table.addMouseListener(new MouseAdapter(){
            public void mouseDown(MouseEvent arg0) {
                if (selectedProperty != null && selectedEntry != null) {
                    boolean selected = isSelected(selectedEntry, selectedProperty);
                    setSelected(selectedEntry,
                                selectedProperty,
                                !selected);
                    fireSelectionEvent();
                }
            }
        });
        
        table.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                IMAGE_ENABLED.dispose();
                IMAGE_DISABLED.dispose();
            }
        });
    }

    /**
     * Adds a new entry, i.e., a row in the table
     *
     * @param entry
     * @param properties
     */
    public void addEntry(String entry, List<String> properties){
        
        if (!this.properties.containsAll(properties)) {
            throw new RuntimeException("All properties of an entry must be contained in the overall list");
        }
        
        TableItem item = new TableItem(table, SWT.NONE);
        for (int i = 0; i < this.properties.size(); i++) {
            if (properties.contains(this.properties.get(i))) {
                item.setImage(i + 1, IMAGE_DISABLED);
            } 
        }
        item.setImage(0, null);
        item.setText(0, entry);
        this.items.put(entry, item);
        this.itemProperties.put(entry, properties);
        this.entries.add(entry);
        table.redraw();
        
        for (TableColumn c : table.getColumns()) {
            c.pack();
        }
    }

    /**
     * Adds a selection listener.
     *
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        this.listeners.add(listener);
    }
    
    /**
     * Clears the table.
     */
    public void clear(){
        this.table.setRedraw(false);
        for (TableItem item : table.getItems()){
            item.dispose();
        }
        for (TableColumn column : table.getColumns()){
            column.dispose();
        }
        this.table.removeAll();
        this.table.setRedraw(true);
        this.table.redraw();
        this.items.clear();
        this.itemProperties.clear();
        this.properties.clear();
        this.entries.clear();
        this.selected.clear();
    }
    
    /**
     * Disposes this widget.
     */
    public void dispose(){
        this.table.dispose();
    }
    
    /**
     * Returns the entries.
     *
     * @return
     */
    public List<String> getEntries() {
        return entries;
    }
    
    /**
     * Returns the properties.
     *
     * @return
     */
    public List<String> getProperties() {
        return properties;
    }
    
    /**
     * Returns the currently selected entry.
     *
     * @return
     */
    public String getSelectedEntry(){
        return selectedEntry;
    }
    
    /**
     * Returns the currently selected property.
     *
     * @return
     */
    public String getSelectedProperty(){
        return selectedProperty;
    }
    
    /**
     * Returns whether the given property is selected for the given entry.
     *
     * @param entry
     * @param property
     * @return
     */
    public boolean isSelected(String entry, String property){
        if (!this.entries.contains(entry)) {
            throw new RuntimeException("Unknown entry");
        }
        if (!this.properties.contains(property)) {
            throw new RuntimeException("Unknown property");
        }
        Map<String, Boolean> map = selected.get(entry);
        if (map==null) {
            return false;
        }
        else {
            Boolean b = map.get(property);
            return b == null ? false : b;
        }
    }
    
    /**
     * Removes a selection listener.
     *
     * @param listener
     */
    public void removeSelectionListener(SelectionListener listener) {
        this.listeners.remove(listener);
    }
    
    /**
     * Enable/disable.
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.table.setEnabled(enabled);
    } 
    
    /**
     * Sets layout data.
     *
     * @param layoutData
     */
    public void setLayoutData(Object layoutData) {
        this.table.setLayoutData(layoutData);
    }
    
    /**
     * Sets new properties. Clears the table
     * @param properties
     */
    public void setProperties(List<String> properties) {
        this.clear();
        this.properties = new ArrayList<String>(properties);

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setWidth(LABEL_WIDTH);
        column.setText("");
        for (String property : properties) {
            column = new TableColumn(table, SWT.CENTER);
            column.setText(property);
            column.setWidth(CHECKBOX_WIDTH);
        }
        column = new TableColumn(table, SWT.LEFT);
        column.setText("");
    }

    /**
     * Sets the given property selected for the given entry .
     *
     * @param entry
     * @param property
     * @param selected
     */
    public void setSelected(String entry, String property, boolean selected) {
        if (!this.entries.contains(entry)) {
            throw new RuntimeException("Unknown entry");
        }
        if (!this.properties.contains(property)) {
            throw new RuntimeException("Unknown property");
        }
        if (!this.selected.containsKey(entry)) {
            this.selected.put(entry, new HashMap<String, Boolean>());
        }
        if (this.itemProperties.get(entry).contains(property)) {
            this.selected.get(entry).put(property, selected);
            int index = properties.indexOf(property);
            this.items.get(entry).setImage(index+1, selected ? IMAGE_ENABLED : IMAGE_DISABLED);
            table.redraw();
        }
    }

    /**
     * Fires a new event.
     */
    private void fireSelectionEvent(){
        Event event = new Event();
        event.display = table.getDisplay();
        event.item = table;
        event.widget = table;
        SelectionEvent sEvent = new SelectionEvent(event);
        for (SelectionListener listener : listeners) {
            listener.widgetSelected(sEvent);
        }
    }

    /**
     * Returns the item at the given location.
     *
     * @param x
     * @param y
     * @return
     */
    private int getItemColumnAt(int x, int y) {
        Point pt = new Point(x, y);
        int index = table.getTopIndex();
        while (index < table.getItemCount()) {
            final TableItem item = table.getItem(index);
            for (int i = 0; i < table.getColumns().length; i++) {
                final Rectangle rect = item.getBounds(i);
                if (rect.contains(pt)) { 
                    return i;
                }
            }
            index++;
        }
        return -1;
    }

    /**
     * Returns the item at the given location.
     *
     * @param x
     * @param y
     * @return
     */
    private int getItemRowAt(int x, int y) {
        Point pt = new Point(x, y);
        int index = table.getTopIndex();
        while (index < table.getItemCount()) {
            final TableItem item = table.getItem(index);
            for (int i = 0; i < table.getColumns().length; i++) {
                final Rectangle rect = item.getBounds(i);
                if (rect.contains(pt)) { 
                    return index;
                }
            }
            index++;
        }
        return -1;
    }
}
