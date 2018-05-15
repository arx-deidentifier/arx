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

import java.util.Arrays;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * This class implements an editor for generalization hierarchies.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Ledian Xhani
 * @author Ljubomir Dshevlekov
 * 
 */
public class ComponentHierarchy {
    
    /**
     * A data provider for hierarchies.
     *
     * @author Fabian Prasser
     */
    private class HierarchyDataProvider implements IDataProvider {
        
        @Override
        public int getColumnCount() {
            if (hierarchy == null) return 0;
            else if (hierarchy.length == 0) return 0;
            else if (hierarchy[0] == null) return 0;
            else return hierarchy[0].length;
        }
        
        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return hierarchy[rowIndex][columnIndex];
        }
        
        @Override
        public int getRowCount() {
            if (hierarchy == null) return 0;
            else return hierarchy.length;
        }
        
        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            // Ignore
        }
    }
    
    /**
     * A header data provider for hierarchies.
     *
     * @author Fabian Prasser
     */
    private class HierarchyHeaderDataProvider extends HierarchyDataProvider {
        
        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return Resources.getMessage("ViewHierarchy.0") + columnIndex; //$NON-NLS-1$
        }
        
        @Override
        public int getRowCount() {
            return 1;
        }
    }

    /** Widget. */
    private Composite            base;

    /** Widget. */
    private ComponentTable       table     = null;

    /** Model. */
    private String[][]           hierarchy = new String[][] { new String[] {} };

    /** Listener */
    private final ModifyListener listener;
    
    /**
     * Constructor for non-editable views.
     *
     * @param parent
     */
    public ComponentHierarchy(final Composite parent) {
        this.listener = null;
        this.createTable(parent);
    }
    
    /**
     * Constructor for editable views.
     *
     * @param parent
     * @param listener
     */
    public ComponentHierarchy(final Composite parent,
                         final ModifyListener listener) {
        this.listener = listener;
        this.createTable(parent);
    }

    /**
     * Clears the hierarchy.
     */
    public void actionClear() {
        this.setHierarchy(null);
        this.triggerChangedEvent();
    }
    
    /**
     * Deletes a column.
     */
    public void actionDeleteColumn() {
        
        if (table.getSelectedColumn() == null) {
            return;
        }
        
        int selected = table.getSelectedColumn();
        int columns = hierarchy[0].length - 1;
        int rows = hierarchy.length;
        String[][] temp = new String[rows][columns];
        for (int i = 0; i < rows; i++) {
            String[] row = new String[columns];
            System.arraycopy(hierarchy[i], 0, row, 0, selected);
            System.arraycopy(hierarchy[i], selected + 1, row, selected, columns - selected);
            temp[i] = row;
        }
        
        if (columns == 0) {
            actionClear();
        } else {
            this.hierarchy = temp;
            this.table.refresh();
        }
        
        this.triggerChangedEvent();
    }
    
    /**
     * Deletes a row.
     */
    public void actionDeleteRow() {
        
        if (table.getSelectedRow() == null) {
            return;
        }
        
        int selected = table.getSelectedRow();
        int columns = hierarchy[0].length;
        int rows = hierarchy.length - 1;
        String[][] temp = new String[rows][columns];
        
        System.arraycopy(hierarchy, 0, temp, 0, selected);
        System.arraycopy(hierarchy, selected + 1, temp, selected, rows - selected);
        
        if (rows == 0) {
            actionClear();
        } else {
            this.hierarchy = temp;
            this.table.refresh();
        }
        
        this.triggerChangedEvent();
    }
    
    /**
     * Renames an item.
     */
    public void actionEditItem(String newValue) {
        
        if (table.getSelectedRow() == null ||
            table.getSelectedColumn() == null) {
            return;
        }
        
        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();
        
        if (newValue != null) {
            hierarchy[selectedRow][selectedColumn] = newValue;
        }
        
        this.table.refresh();
        this.triggerChangedEvent();
    }
    
    /**
     * Inserts a column.
     */
    public void actionInsertColumn() {
        
        if (table.getSelectedColumn() == null) {
            return;
        }
        
        int selected = table.getSelectedColumn();
        int columns = hierarchy[0].length + 1;
        int rows = hierarchy.length;
        String[][] temp = new String[rows][columns];
        for (int i = 0; i < rows; i++) {
            String[] row = new String[columns];
            System.arraycopy(hierarchy[i], 0, row, 0, selected + 1);
            System.arraycopy(hierarchy[i], selected + 1, row, selected + 2, columns - selected - 2);
            row[selected + 1] = ""; //$NON-NLS-1$
            temp[i] = row;
        }
        
        this.hierarchy = temp;
        this.table.refresh();
        this.triggerChangedEvent();
    }
    
    /**
     * Inserts a row.
     */
    public void actionInsertRow() {
        
        if (table.getSelectedRow() == null) {
            return;
        }
        
        int selected = table.getSelectedRow();
        int columns = hierarchy[0].length;
        int rows = hierarchy.length + 1;
        String[][] temp = new String[rows][columns];
        
        System.arraycopy(hierarchy, 0, temp, 0, selected + 1);
        System.arraycopy(hierarchy, selected + 1, temp, selected + 2, rows - selected - 2);
        temp[selected + 1] = new String[columns];
        Arrays.fill(temp[selected + 1], ""); //$NON-NLS-1$
        
        this.hierarchy = temp;
        this.table.refresh();
        this.triggerChangedEvent();
    }
    
    /**
     * Moves an element down.
     */
    public void actionMoveRowDown() {
        
        if (table.getSelectedRow() == null) {
            return;
        }
        
        int selected = table.getSelectedRow();
        
        if (selected == hierarchy.length - 1) {
            return;
        }
        
        String[] temp = hierarchy[selected + 1];
        hierarchy[selected + 1] = hierarchy[selected];
        hierarchy[selected] = temp;
        
        this.table.refresh();
        this.triggerChangedEvent();
    }
    
    /**
     * Moves an element up.
     */
    public void actionMoveRowUp() {
        
        if (table.getSelectedRow() == null) {
            return;
        }
        
        int selected = table.getSelectedRow();
        
        if (selected <= 0) {
            return;
        }
        
        String[] temp = hierarchy[selected - 1];
        hierarchy[selected - 1] = hierarchy[selected];
        hierarchy[selected] = temp;
        
        this.table.refresh();
        this.triggerChangedEvent();
    }
    
    /**
     * Renames an item.
     */
    public void actionRenameItem(String newValue) {
        
        if (table.getSelectedRow() == null ||
            table.getSelectedColumn() == null) {
            return;
        }
        
        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();
        
        final String oldValue = hierarchy[selectedRow][selectedColumn];
       
        if (newValue != null) {
            for (int i = 0; i < hierarchy.length; i++) {
                if (hierarchy[i][selectedColumn].equals(oldValue)) {
                    hierarchy[i][selectedColumn] = newValue;
                }
            }
        }
        
        this.table.refresh();
        this.triggerChangedEvent();
    }
    
    /**
     * Clears the hierarchy.
     */
    public void actionReset() {
        this.setHierarchy(null);
    }
    
    /**
     * Adds a mouse listener
     * @param listener
     */
    public void addMouseListener(MouseListener listener) {
        this.table.addMouseListener(listener);
        this.table.getControl().addMouseListener(listener);
    }
    
    /**
     * Returns the underlying control
     * @return
     */
    public Control getControl() {
        return table.getControl();
    }
    
    /**
     * Returns the selected value. Returns null if no value is selected.
     * @return
     */
    public String getSelectedValue() {

        if (table.getSelectedRow() == null ||
            table.getSelectedColumn() == null) {
            return null;
        }
        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();
        return hierarchy[selectedRow][selectedColumn];
    }
    
    /**
     * Returns whether a cell is selected
     * @return
     */
    public boolean isCellSelected() {
        return table.getSelectedColumn() != null && table.getSelectedRow() != null;
    }
    
    /**
     * Returns whether a column is selected
     * @return
     */
    public boolean isColumnSelected() {
        return !isCellSelected() && table.getSelectedColumn() != null;
    }
    
    /**
     * Returns whether this component is empty
     * @return
     */
    public boolean isEmpty() {
        return  hierarchy == null ||
                hierarchy.length == 0 ||
                hierarchy[0] == null ||
                hierarchy[0].length == 0;
    }
    
    /**
     * Returns whether a row is selected
     * @return
     */
    public boolean isRowSelected() {
        return !isCellSelected() && table.getSelectedRow() != null;
    }
    
    /**
     * Sets the hierarchy displayed by this component. Call with null to reset.
     *
     * @param hierarchy
     */
    public void setHierarchy(final Hierarchy hierarchy) {
        this.hierarchy = (hierarchy == null ? null : hierarchy.getHierarchy());
        this.table.refresh();
    }

    /**
     * Sets the layout data.
     *
     * @param d
     */
    public void setLayoutData(final Object d) {
        base.setLayoutData(d);
    }

    /**
     * Creates the control.
     *
     * @param parent
     */
    private void createTable(final Composite parent) {
        
        // Create base composite
        this.base = new Composite(parent, SWT.NONE);
        GridData bottomLayoutData = SWTUtil.createFillGridData();
        bottomLayoutData.grabExcessVerticalSpace = true;
        GridLayout bottomLayout = new GridLayout();
        bottomLayout.numColumns = 1;
        this.base.setLayout(bottomLayout);
        this.base.setLayoutData(bottomLayoutData);
        
        // Configure table
        CTConfiguration config = new CTConfiguration(parent, CTConfiguration.STYLE_TABLE);
        config.setHorizontalAlignment(SWT.LEFT);
        config.setCellSelectionEnabled(true);
        config.setColumnSelectionEnabled(true);
        config.setRowSelectionEnabled(false);
        config.setColumnHeaderLayout(CTConfiguration.COLUMN_HEADER_LAYOUT_FILL);
        config.setRowHeaderLayout(CTConfiguration.ROW_HEADER_LAYOUT_FILL);
        
        // Create table
        this.table = new ComponentTable(base, SWT.BORDER, config);
        this.table.getControl().setLayoutData(SWTUtil.createFillGridData());
        this.table.setData(new HierarchyDataProvider(), new HierarchyHeaderDataProvider());
    }
    
    /**
     * Triggers a change event. New hierarchy is passed as event.data
     */
    private void triggerChangedEvent() {
        if (listener != null) {
            Event event = new Event();
            event.data = Hierarchy.create(hierarchy);
            event.widget = table.getControl();
            listener.modifyText(new ModifyEvent(event));
        }
    }
}
