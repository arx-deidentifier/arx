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
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This class implements an editor for generalization hierarchies. It is partly
 * based upon code implemented by Ledian Xhani and Ljubomir Dshevlekov.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Ledian Xhani
 * @author Ljubomir Dshevlekov
 * 
 */
public class ViewHierarchy implements IView {

    /**
     * A data provider for hierarchies.
     *
     * @author Fabian Prasser
     */
    private class HierarchyDataProvider implements IDataProvider {

        /* (non-Javadoc)
         * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getDataValue(int, int)
         */
        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return hierarchy[rowIndex][columnIndex];
        }

        /* (non-Javadoc)
         * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#setDataValue(int, int, java.lang.Object)
         */
        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            // Ignore
        }

        /* (non-Javadoc)
         * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getColumnCount()
         */
        @Override
        public int getColumnCount() {
            if (hierarchy == null) return 0;
            else if (hierarchy.length == 0) return 0;
            else if (hierarchy[0] == null) return 0;
            else return hierarchy[0].length;
        }

        /* (non-Javadoc)
         * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getRowCount()
         */
        @Override
        public int getRowCount() {
            if (hierarchy == null) return 0;
            else return hierarchy.length;
        }
    }

    /**
     * A header data provider for hierarchies.
     *
     * @author Fabian Prasser
     */
    private class HierarchyHeaderDataProvider extends HierarchyDataProvider {

        /* (non-Javadoc)
         * @see org.deidentifier.arx.gui.view.impl.common.ViewHierarchy.HierarchyDataProvider#getDataValue(int, int)
         */
        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return "Level-"+columnIndex;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.gui.view.impl.common.ViewHierarchy.HierarchyDataProvider#getRowCount()
         */
        @Override
        public int getRowCount() {
            return 1;
        }
    }

    /** Constant. */
    private static final String ITEM_ALL  = Resources.getMessage("HierarchyView.0"); //$NON-NLS-1$

    /** Controller. */
    private Controller          controller;

    /** Widget. */
    private Composite           base;
    
    /** Widget. */
    private Combo               min;
    
    /** Widget. */
    private Combo               max;
    
    /** Widget. */
    private ComponentTable      table     = null;

    /** Model. */
    private final String        attribute;
    
    /** Model. */
    private Model               model;
    
    /** Model. */
    private String[][]          hierarchy = new String[][] { new String[] {} };
    
    /** Model. */
    private boolean             editable  = true;

    /** Menu. */
    private Menu                menu      = null;
    
    /** Item. */
    private MenuItem            itemInsertRow;
    
    /** Item. */
    private MenuItem            itemDeleteRow;
    
    /** Item. */
    private MenuItem            itemInsertColumn;
    
    /** Item. */
    private MenuItem            itemDeleteColumn;
    
    /** Item. */
    private MenuItem            itemMoveRowUp;
    
    /** Item. */
    private MenuItem            itemMoveRowDown;
    
    /** Item. */
    private MenuItem            itemEditItem;
    
    /** Item. */
    private MenuItem            itemRenameItem;
    
    /** Item. */
    private MenuItem            itemClear;
    
    /** Item. */
    private MenuItem            itemInitialize;

    /**
     * Constructor for non-editable views.
     *
     * @param parent
     */
    public ViewHierarchy(final Composite parent) {

        this.attribute = null;
        this.editable = false;
        createTable(parent);

    }
    
    /**
     * Constructor for non-editable views.
     *
     * @param parent
     * @param attribute
     */
    public ViewHierarchy(final Composite parent, final String attribute) {

        this.attribute = attribute;
        this.editable = false;
        createTable(parent);

    }

    /**
     * Constructor for editable views.
     *
     * @param parent
     * @param attribute
     * @param controller
     */
    public ViewHierarchy(final Composite parent,
                         final String attribute,
                         final Controller controller) {

        // Register
        controller.addListener(ModelPart.HIERARCHY, this);
        controller.addListener(ModelPart.MODEL, this);

        this.controller = controller;
        this.attribute = attribute;

        // build
        editable = true;
        createTable(parent);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
        if (!base.isDisposed()) {
            base.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        setHierarchy(null);
        if (!base.isDisposed()) {
            base.redraw();
        }
    }

    /**
     * Sets the hierarchy displayed by this view.
     *
     * @param type
     */
    public void setHierarchy(final AttributeType.Hierarchy type) {
        this.hierarchy = (type == null ? null : type.getHierarchy());
        this.table.refresh();
        this.updateCombos();
    }

    /**
     * Sets the layout data.
     *
     * @param d
     */
    public void setLayoutData(final Object d) {
        base.setLayoutData(d);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.HIERARCHY) {
            if (attribute.equals(model.getSelectedAttribute())) {
                setHierarchy((Hierarchy) event.data);
                updateCombos();
                base.setEnabled(true);
                base.redraw();
            }
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        } else if (event.part == ModelPart.INPUT) {
            Hierarchy h = model.getInputConfig().getHierarchy(attribute);
            if (h != null) {
                setHierarchy(h);
                updateCombos();
                base.setEnabled(true);
                base.redraw();
            } else {
                reset();
            }
        }
    }

    /**
     * Clears the hierarchy.
     */
    private void actionClear() {
        
        setHierarchy(null);
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Deletes a column.
     */
    private void actionDeleteColumn() {
        
        if (table.getSelectedColumn() == null) { 
            return; 
        }

        int selected = table.getSelectedColumn();
        int columns = hierarchy[0].length-1;
        int rows = hierarchy.length;
        String[][] temp = new String[rows][columns];
        for (int i=0; i<rows; i++){
            String[] row = new String[columns];
            System.arraycopy(hierarchy[i], 0, row, 0, selected);
            System.arraycopy(hierarchy[i], selected+1, row, selected, columns-selected);
            temp[i] = row;
        }
        
        
        if (columns==0){
            actionClear();
        } else {
            this.hierarchy = temp;
            this.table.refresh();
            this.updateCombos();
        }
        
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     * Deletes a row.
     */
    private void actionDeleteRow() {
        
        if (table.getSelectedRow() == null) { 
            return; 
        }

        int selected = table.getSelectedRow();
        int columns = hierarchy[0].length;
        int rows = hierarchy.length-1;
        String[][] temp = new String[rows][columns];
        
        System.arraycopy(hierarchy, 0, temp, 0, selected);
        System.arraycopy(hierarchy, selected+1, temp, selected, rows-selected);
        
        if (rows==0){
            actionClear();
        } else {
            this.hierarchy = temp;
            this.table.refresh();
            this.updateCombos();
        }
        
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     * Renames an item.
     */
    private void actionEditItem() {

        if (table.getSelectedRow() == null ||
            table.getSelectedColumn() == null) { 
            return; 
        }

        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();
        
        final String oldValue = hierarchy[selectedRow][selectedColumn];
        final String newValue = controller.actionShowInputDialog(controller.getResources().getShell(),
                                                                 Resources.getMessage("HierarchyView.13"), Resources.getMessage("HierarchyView.14"), oldValue); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (newValue != null) {
            hierarchy[selectedRow][selectedColumn] = newValue;
        }
        
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     * Initializes the hierarchy with identity mapping.
     */
    private void actionInitialize() {
        
        // Check
        if (table.getSelectedColumn() != null ||
            table.getSelectedRow() != null ||
            model == null ||
            model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null) {
            return;
        }
        
        // Obtain values
        DataHandle handle = model.getInputConfig().getInput().getHandle();
        int index = handle.getColumnIndexOf(attribute);
        String[] values = handle.getStatistics().getDistinctValuesOrdered(index);
        
        // Create hierarchy
        String[][] hierarchy = new String[values.length][0];
        for (int i=0; i<values.length; i++){
            hierarchy[i] = new String[]{values[i]};
        }
        
        // Set
        this.hierarchy = hierarchy;
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Inserts a column.
     */
    private void actionInsertColumn() {

        if (table.getSelectedColumn() == null) { 
            return; 
        }

        int selected = table.getSelectedColumn();
        int columns = hierarchy[0].length+1;
        int rows = hierarchy.length;
        String[][] temp = new String[rows][columns];
        for (int i=0; i<rows; i++){
            String[] row = new String[columns];
            System.arraycopy(hierarchy[i], 0, row, 0, selected+1);
            System.arraycopy(hierarchy[i], selected+1, row, selected+2, columns-selected-2);
            row[selected+1]="";
            temp[i] = row;
        }
        
        this.hierarchy = temp;
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Inserts a row.
     */
    private void actionInsertRow() {

        if (table.getSelectedRow() == null) { 
            return; 
        }

        int selected = table.getSelectedRow();
        int columns = hierarchy[0].length;
        int rows = hierarchy.length+1;
        String[][] temp = new String[rows][columns];
        
        System.arraycopy(hierarchy, 0, temp, 0, selected+1);
        System.arraycopy(hierarchy, selected+1, temp, selected+2, rows-selected-2);
        temp[selected+1] = new String[columns];
        Arrays.fill(temp[selected+1], "");
        
        this.hierarchy = temp;
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Moves an element down.
     */
    private void actionMoveRowDown() {

        if (table.getSelectedRow() == null) { 
            return; 
        }

        int selected = table.getSelectedRow();
        
        if (selected == hierarchy.length-1) {
            return;
        }
        
        String[] temp = hierarchy[selected+1];
        hierarchy[selected+1] = hierarchy[selected];
        hierarchy[selected] = temp;
        
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Moves an element up.
     */
    private void actionMoveRowUp() {

        if (table.getSelectedRow() == null) { 
            return; 
        }

        int selected = table.getSelectedRow();
        
        if (selected <=0) {
            return;
        }
        
        String[] temp = hierarchy[selected-1];
        hierarchy[selected-1] = hierarchy[selected];
        hierarchy[selected] = temp;
        
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Renames an item.
     */
    private void actionRenameItem() {

        if (table.getSelectedRow() == null ||
            table.getSelectedColumn() == null) { 
            return; 
        }

        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();
        
        final String oldValue = hierarchy[selectedRow][selectedColumn];
        final String newValue = controller.actionShowInputDialog(controller.getResources().getShell(),
                                                                 Resources.getMessage("HierarchyView.13"), Resources.getMessage("HierarchyView.14"), oldValue); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (newValue != null) {
            for (int i=0; i<hierarchy.length; i++){
                if (hierarchy[i][selectedColumn].equals(oldValue)){
                    hierarchy[i][selectedColumn] = newValue;
                }
            }
        }
        
        this.table.refresh();
        this.updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
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

        // Create label
        if (attribute != null) {
            Label l = new Label(base, SWT.NONE);
            l.setText(Resources.getMessage("HierarchyView.2") + attribute + //$NON-NLS-1$  
                      Resources.getMessage("HierarchyView.3")); //$NON-NLS-2$
        }

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

        // Create the menu and editing controls
        if (editable) {
            createMenu();
        }
    }
    
    /**
     * Creates all components required for making the table editable.
     */
    private void createMenu() {
        
        // Create bottom composite
        final Composite bottom = new Composite(base, SWT.NONE);
        bottom.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        bottom.setLayout(layout);

        // Insert min button
        Label l1 = new Label(bottom, SWT.NONE);
        l1.setText(Resources.getMessage("HierarchyView.4")); //$NON-NLS-1$
        this.min = new Combo(bottom, SWT.READ_ONLY);
        this.min.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.min.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                pushMin();
            }
        });

        // Insert max button
        Label l2 = new Label(bottom, SWT.NONE);
        l2.setText(Resources.getMessage("HierarchyView.6")); //$NON-NLS-1$
        this.max = new Combo(bottom, SWT.READ_ONLY);
        this.max.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.max.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                pushMax();
            }
        });
 
        // Saved the reference to the selected row and/or column
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(final MouseEvent e) {
                if (e.button == 3) { 
                    onMouseDown(table.toDisplay(e.x, e.y));
                }
            }
        });
        table.getControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(final MouseEvent e) {
                if (e.button == 3) { 
                    onMouseDown(table.getControl().toDisplay(e.x, e.y));
                }
            }
        });

        // Creates the editors menu
        this.menu = new Menu(table.getControl());
        
        // Insert row action
        itemInsertRow = new MenuItem(menu, SWT.NONE);
        itemInsertRow.setText(Resources.getMessage("HierarchyView.7")); //$NON-NLS-1$
        itemInsertRow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionInsertRow();
            }
        });

        // Delete row action
        itemDeleteRow = new MenuItem(menu, SWT.NONE);
        itemDeleteRow.setText(Resources.getMessage("HierarchyView.8")); //$NON-NLS-1$
        itemDeleteRow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionDeleteRow();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Insert column action
        itemInsertColumn = new MenuItem(menu, SWT.NONE);
        itemInsertColumn.setText(Resources.getMessage("HierarchyView.9")); //$NON-NLS-1$
        itemInsertColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionInsertColumn();
            }
        });

        // Delete column action
        itemDeleteColumn = new MenuItem(menu, SWT.NONE);
        itemDeleteColumn.setText(Resources.getMessage("HierarchyView.10")); //$NON-NLS-1$
        itemDeleteColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionDeleteColumn();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);

        // Move up
        itemMoveRowUp = new MenuItem(menu, SWT.NONE);
        itemMoveRowUp.setText(Resources.getMessage("HierarchyView.11")); //$NON-NLS-1$
        itemMoveRowUp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionMoveRowUp();
            }
        });
        
        // Move down
        itemMoveRowDown = new MenuItem(menu, SWT.NONE);
        itemMoveRowDown.setText(Resources.getMessage("HierarchyView.12")); //$NON-NLS-1$
        itemMoveRowDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionMoveRowDown();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);

        // Edit item action
        itemEditItem = new MenuItem(menu, SWT.NONE);
        itemEditItem.setText(Resources.getMessage("HierarchyView.18")); //$NON-NLS-1$
        itemEditItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionEditItem();
            }
        });

        // Rename item action
        itemRenameItem = new MenuItem(menu, SWT.NONE);
        itemRenameItem.setText(Resources.getMessage("HierarchyView.15")); //$NON-NLS-1$
        itemRenameItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionRenameItem();
            }
        });


        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Action clear
        itemClear = new MenuItem(menu, SWT.NONE);
        itemClear.setText(Resources.getMessage("HierarchyView.16")); //$NON-NLS-1$
        itemClear.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionClear();
            }
        });

        // Action intialize
        itemInitialize = new MenuItem(menu, SWT.NONE);
        itemInitialize.setText(Resources.getMessage("HierarchyView.19")); //$NON-NLS-1$
        itemInitialize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionInitialize();
            }
        });

    }

    /**
     * Mouse down action.
     *
     * @param point
     */
    private void onMouseDown(Point point) {
        
        // Init
        boolean cell = table.getSelectedColumn() != null && table.getSelectedRow() != null;
        boolean row = !cell && table.getSelectedRow() != null;
        boolean column = !cell && table.getSelectedColumn() != null;
        
        // Update menu items
        itemInsertRow.setEnabled(row || cell);
        itemDeleteRow.setEnabled(row || cell);
        itemMoveRowUp.setEnabled(row || cell);
        itemMoveRowDown.setEnabled(row || cell);
        // ---------
        itemInsertColumn.setEnabled(column || cell);
        itemDeleteColumn.setEnabled(column || cell);
        // ---------
        itemEditItem.setEnabled(cell);
        itemRenameItem.setEnabled(cell);
        // ---------
        itemClear.setEnabled(cell || row || column);
        itemInitialize.setEnabled(hierarchy == null || 
                                   hierarchy.length==0 ||
                                   hierarchy[0]==null ||
                                   hierarchy[0].length==0);

        // Show
        this.menu.setLocation(point);
        this.menu.setVisible(true);
    }

    /**
     * Updates the global hierarchy definition.
     */
    private void pushHierarchy() {

        // Just write it to the model
        if (model == null || model.getInputConfig() == null) { return; }
        Hierarchy h = Hierarchy.create(hierarchy);
        model.getInputConfig().setHierarchy(attribute, h);
    }
    
    /**
     * Updates the max generalization level.
     *
     * @return
     */
    private boolean pushMax(){
        
        if (max.getSelectionIndex() >= 0) {
            if (max.getSelectionIndex() < (min.getSelectionIndex() - 1)) {
                max.select(min.getSelectionIndex() - 1);
            }
            if (model != null) {
                String val = max.getItem(max.getSelectionIndex());
                model.getInputConfig().setMaximumGeneralization(attribute, val.equals(ITEM_ALL) ? null : Integer.valueOf(val));
                return true;
            } 
        } 
        return false;
    }

    /**
     * Updates the min generalization level.
     *
     * @return
     */
    private boolean pushMin() {
        
        if (min.getSelectionIndex() >= 0) {
            if (min.getSelectionIndex() > (max.getSelectionIndex() + 1)) {
                min.select(max.getSelectionIndex() + 1);
            } 
            if (model != null) {
                String val = min.getItem(min.getSelectionIndex());
                model.getInputConfig().setMinimumGeneralization(attribute, val.equals(ITEM_ALL) ? null : Integer.valueOf(val));
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the combos.
     */
    private void updateCombos(){
        
        // Check whether min & max are still ok
        if (model==null || min == null || min.isDisposed()) { return; }

        // Prepare lists
        final List<String> minItems = new ArrayList<String>();
        final List<String> maxItems = new ArrayList<String>();
        minItems.add(ITEM_ALL);
        int length = 0;
        if (!(hierarchy==null || hierarchy.length == 0)) {
            length = hierarchy[0].length;
        }
        for (int i = 0; i < length; i++) {
            minItems.add(String.valueOf(i));
            maxItems.add(String.valueOf(i));
        }
        maxItems.add(ITEM_ALL);

        // Determine min index
        Integer minModel = model.getInputConfig().getMinimumGeneralization(attribute);
        int minIndex = minModel != null ? minModel + 1 : 0;

        // Determine max index
        Integer maxModel = model.getInputConfig().getMaximumGeneralization(attribute);
        int maxIndex = maxModel != null ? maxModel : maxItems.size()-1;
        
        // Fix indices
        maxIndex = maxIndex > maxItems.size() - 1 ? maxItems.size() - 1 : maxIndex;
        maxIndex = maxIndex < 0 ? maxItems.size() - 1 : maxIndex;
        minIndex = minIndex > minItems.size() - 1 ? minItems.size() - 1 : minIndex;
        minIndex = minIndex < 0 ? 0 : minIndex;
        minIndex = minIndex > (maxIndex + 1) ? maxIndex + 1 : minIndex;

        // Set items
        min.setItems(minItems.toArray(new String[minItems.size()]));
        max.setItems(maxItems.toArray(new String[maxItems.size()]));

        // Select
        min.select(minIndex);
        max.select(maxIndex);
        pushMin();
        pushMax();
    }
}
