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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageListHelper;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
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

    /**
     * Page loader
     * @author Fabian Prasser
     */
    private class FilterPageLoader implements IPageLoader<PageResult<Pair<String, Set<String>>>> {

        @Override
        public PageResult<Pair<String, Set<String>>> loadPage(PageableController controller) {
            if (keys == null || properties == null || keyProperties == null) {
                return PageListHelper.createPage(new ArrayList<Pair<String, Set<String>>>(), controller);
            } else {
            	List<Pair<String, Set<String>>> list = new ArrayList<>();
            	for (String key : keys) {
            		list.add(new Pair<String, Set<String>>(key, keyProperties.get(key)));
            	}
                return PageListHelper.createPage(list, controller);
            }
        }
    }

    /** Image. */
    private final Image                       IMAGE_ENABLED;

    /** Image. */
    private final Image                       IMAGE_DISABLED;
    
    /** Widget. */
    private final PageableTable               pageableTable;

    /** Widget. */
    private final Table                       table;

    /** State*/
    private List<String>                      keys;

    /** The registered listeners. */
    private List<SelectionListener>           listeners;

    /** The list of properties. */
    private Map<String, Set<String>>          keyProperties;
    
    /** The list of properties. */
    private List<String>                      properties;

    /** State*/
    private String 							  selectedKey;
    
    /** State*/
    private String 							  selectedProperty;

    /** State*/
	private Map<String, Set<String>> 		  permitted;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param properties
     * @param entries
     */
    public ComponentFilterTable(Composite parent, 
                                Controller controller) {
        
        IMAGE_ENABLED = controller.getResources().getManagedImage("tick.png"); //$NON-NLS-1$
        IMAGE_DISABLED = controller.getResources().getManagedImage("cross.png"); //$NON-NLS-1$
        
        this.listeners = new ArrayList<SelectionListener>();
        this.pageableTable = SWTUtil.createPageableTableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, false, false);
        this.pageableTable.getViewer().setContentProvider(new ArrayContentProvider());
        this.pageableTable.setPageLoader(new FilterPageLoader());
        this.pageableTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        this.table = this.pageableTable.getViewer().getTable();
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        this.table.setHeaderVisible(true);
        
        // React on events
        this.table.addMouseListener(new MouseAdapter(){
            
        	@Override
        	@SuppressWarnings("unchecked")
        	public void mouseDown(MouseEvent arg0) {
                int row = getItemRowAt(arg0.x, arg0.y);
                int column = getItemColumnAt(arg0.x, arg0.y);
                if (row != -1 && column > 0 && column <= ComponentFilterTable.this.properties.size()) {
                    String property = ComponentFilterTable.this.properties.get(column-1);
					String entry = ((Pair<String, Set<String>>)ComponentFilterTable.this.table.getItem(row).getData()).getFirst();
					if (permitted != null && permitted.get(entry).contains(property) && property != null && entry != null) {
	                    boolean selected = isSelected(entry, property);
	                    setSelected(entry, property, !selected);
	                    fireSelectionEvent(entry, property);
	                }
                } else {
                    selectedProperty = null;
                    selectedKey = null;
                }
            }
        });
        
        // Init
        this.pageableTable.setCurrentPage(0);
        this.pageableTable.refreshPage();
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
    public void clear() {
        this.selectedKey = null;
        this.keys = null;
        this.selectedProperty = null;
        this.keyProperties = null;
        this.properties = null;
        this.permitted = null;
        this.pageableTable.setRedraw(false);
        for (TableColumn c : this.pageableTable.getViewer().getTable().getColumns()) {
        	c.dispose();
        }
        this.pageableTable.setRedraw(true);
    }

    /**
     * Returns the entries.
     * @param entry
     * @return
     */
    public int getIndexOfEntry(String entry) {
    	return this.keys.indexOf(entry);
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
    public String getSelectedEntry() {
        return selectedKey;
    }

    /**
     * Returns the currently selected property.
     * 
     * @return
     */
    public String getSelectedProperty() {
        return selectedProperty;
    }

    /**
     * Returns whether the given property is selected for the given entry.
     * 
     * @param entry
     * @param property
     * @return
     */
    public boolean isSelected(String entry, String property) {
        if (!this.keyProperties.containsKey(entry)) {
            throw new RuntimeException(Resources.getMessage("ComponentFilterTable.3")); //$NON-NLS-1$
        }
        if (!this.properties.contains(property)) {
            throw new RuntimeException(Resources.getMessage("ComponentFilterTable.4")); //$NON-NLS-1$
        }
        return this.keyProperties.get(entry).contains(property);
    }

    /**
     * Enable/disable.
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.pageableTable.setEnabled(enabled);
    }

    /**
     * Sets layout data.
     * 
     * @param layoutData
     */
    public void setLayoutData(Object layoutData) {
        this.pageableTable.setLayoutData(layoutData);
    }

    /**
     * Specifies the properties permitted per entry
     * @param entries
     * @param permitted
     */
    public void setPermitted(List<String> entries, Map<String, Set<String>> permitted) {
    	
        // Store
        this.keys = entries;
    	this.keyProperties = new HashMap<>(permitted);
    	
    	// Create deep copy of for managing permitted values
    	this.permitted = new HashMap<>();
    	for (Entry<String, Set<String>> entry : keyProperties.entrySet()) {
    	    this.permitted.put(entry.getKey(), new HashSet<>(entry.getValue()));
    	}
    	
    	// Refresh table
    	this.pageableTable.refreshPage();
        this.pageableTable.setCurrentPage(0);
        for (TableColumn c : this.table.getColumns()) {
        	c.pack();
        }
    }

    /**
     * Sets new properties. Clears the table
     * @param properties
     */
    public void setProperties(List<String> properties) {
    	
    	// Clear
        this.clear();
        this.properties = new ArrayList<String>(properties);

        /* First column for attribute names*/
    	TableViewerColumn column = new TableViewerColumn(pageableTable.getViewer(), SWT.NONE);
    	column.setLabelProvider(new ColumnLabelProvider() {
            
			@Override
            public Image getImage(Object element) {
                return null;
            }
            
            @Override
            @SuppressWarnings("unchecked")
            public String getText(Object element) {
               return ((Pair<String, Set<String>>)element).getKey();
            }
        });	
    	
    	TableColumn tColumn = column.getColumn();
    	tColumn.setText(Resources.getMessage("ComponentFilterTable.9")); //$NON-NLS-1$
    	tColumn.setToolTipText(Resources.getMessage("ComponentFilterTable.9")); //$NON-NLS-1$
    	tColumn.setWidth(80);
    	
    	// One column per property
        for (final String property : properties) {

            /* Empty column to make checkboxes appear in an own cell */
        	column = new TableViewerColumn(pageableTable.getViewer(), SWT.NONE);
        	column.setLabelProvider(new ColumnLabelProvider() {
                
                @Override
                @SuppressWarnings("unchecked")
                public Image getImage(Object element) {
                    String key = ((Pair<String, Set<String>>)element).getKey();
                    Set<String> values = ((Pair<String, Set<String>>)element).getValue();
                    if (ComponentFilterTable.this.permitted == null) {
                    	return null;
                    } else if (!ComponentFilterTable.this.permitted.get(key).contains(property)) {
                    	return null;
                    } else if (values.contains(property)) {
                    	return IMAGE_ENABLED;
                    } else {
                        return IMAGE_DISABLED;
                    }
                }
                
                @Override
                public String getText(Object element) {
                   return "";
                }
            });	
        	tColumn = column.getColumn();
        	tColumn.setText(property);
        	tColumn.setToolTipText(property);
        	tColumn.setWidth(30);
        }
    }

    /**
     * Sets the given property selected for the given entry.
     * 
     * @param entry
     * @param property
     * @param selected
     */
    public void setSelected(String entry, String property, boolean selected) {
 
    	// Check
    	if (this.properties == null || this.keyProperties == null) {
    		return;
    	}
    	
    	// We only support this for existing entries and properties
        if (!this.properties.contains(property)) {
            return;
        }
        if (!this.keyProperties.containsKey(entry)) {
            return;
        }
        if (selected) {
        	this.keyProperties.get(entry).add(property);
        } else {
        	this.keyProperties.get(entry).remove(property);
        }
        this.pageableTable.refreshPage();
    }

    /**
     * Fires a new event
     * @param selectedEntry
     * @param selectedProperty
     */
    private void fireSelectionEvent(String selectedEntry, String selectedProperty) {
    	
    	// Maintain state
        this.selectedKey = selectedEntry;
        this.selectedProperty = selectedProperty;
    	
    	// Fire event
        Event event = new Event();
        event.display = pageableTable.getDisplay();
        event.item = pageableTable;
        event.widget = pageableTable;
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
