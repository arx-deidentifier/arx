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

package org.deidentifier.arx.gui.view.impl.define;

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
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
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

    private static final String ITEM_ALL  = Resources.getMessage("HierarchyView.0"); //$NON-NLS-1$

    /** Controller */
    private Controller          controller;

    /** Bottom parent container */
    private Composite           base;

    /** The column attribute */
    private final String        attribute;

    /** The model */
    private Model               model;

    /** The combo for min generalization */
    private Combo               min;

    /** The combo for max generalization */
    private Combo               max;

    /** The current hierarchy */
    private String[][]          hierarchy = new String[][] { new String[] {} };

    /** Is the view editable? */
    private boolean             editable  = true;

    /** The underlying table */
    private ComponentTable      table     = null;

    /** Menu */
    private Menu                menu      = null;

    /**
     * Constructor for non-editable views
     * 
     * @param parent
     */
    public ViewHierarchy(final Composite parent) {

        this.attribute = null;
        this.editable = false;
        create(parent);

    }
    
    /**
     * Constructor for non-editable views
     * 
     * @param parent
     * @param attribute
     */
    public ViewHierarchy(final Composite parent, final String attribute) {

        this.attribute = attribute;
        this.editable = false;
        create(parent);

    }

    /**
     * Constructor for editable views
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
        create(parent);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        if (!base.isDisposed()) base.dispose();
    }

    @Override
    public void reset() {
        setHierarchy(Hierarchy.create());
        if (!base.isDisposed()) base.redraw();
    }

    /**
     * Sets the hierarchy displayed by this view
     * @param type
     */
    public void setHierarchy(final AttributeType.Hierarchy type) {
        if (type == null) {
            this.hierarchy = null;
            this.table.setEmpty();
            this.updateCombos();
        } else {
            setHierarchyData(type.getHierarchy());
        }
    }

    /**
     * Sets the hierarchy displayed by this view
     * @param array
     */
    private void setHierarchyData(String[][] array) {
        this.hierarchy = array;
        if (hierarchy == null || hierarchy.length == 0 || 
            hierarchy[0] == null || hierarchy[0].length==0){
            this.hierarchy = new String[][]{new String[]{}};
            this.table.setEmpty();
        } else {
            String[] header = new String[hierarchy[0].length];
            for (int i=0; i<header.length; i++) {
                header[i] = Resources.getMessage("HierarchyView.17") + i;
            }
            this.table.setTable(hierarchy, header);
            this.updateCombos();
        }
    }

    /**
     * Sets the layout data
     * @param d
     */
    public void setLayoutData(final Object d) {
        base.setLayoutData(d);
    }

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
     * Initializes the hierarchy with identity mapping
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
        setHierarchyData(hierarchy);
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     * Clears the hierarchy
     */
    private void actionClear() {
        
        setHierarchy(null);
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     *  Deletes a column
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
            setHierarchyData(temp);
        }
        
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     * Deletes a row
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
            setHierarchyData(temp);
        }
        
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Inserts a column
     */
    private void actionInsertColumn() {

        if (table.getSelectedColumn() == null) { 
            return; 
        }
        
        // 0 1 2 3 4 5 6
        // (insert 2)                
        // cp 0, 0, 3 (idx+1)
        // cp 3, 4, 4 (idx+1), (idx+2), length-idx-2

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
        
        setHierarchyData(temp);
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Inserts a row
     */
    private void actionInsertRow() {

        if (table.getSelectedRow() == null) { 
            return; 
        }

        int selected = table.getSelectedRow();
        int columns = hierarchy[0].length;
        int rows = hierarchy.length+1;
        String[][] temp = new String[rows][columns];
        

        // 0 1 2 3 4 5 6
        // (insert 2)                
        // cp 0, 0, 3 (idx+1)
        // cp 3, 4, 4 (idx+1), (idx+2), length-idx-2
        System.arraycopy(hierarchy, 0, temp, 0, selected+1);
        System.arraycopy(hierarchy, selected+1, temp, selected+2, rows-selected-2);
        temp[selected+1] = new String[columns];
        Arrays.fill(temp[selected+1], "");
        
        setHierarchyData(temp);
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Mouse down action
     * @param point
     */
    private void onMouseDown(Point point) {
        this.menu.setLocation(point);
        this.menu.setVisible(true);
    }

    /**
     * Moves an element down
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
        
        setHierarchyData(hierarchy);
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Moves an element up
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
        
        setHierarchyData(hierarchy);
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Renames an item
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
        
        setHierarchyData(hierarchy);
        pushHierarchy();
        pushMin();
        pushMax();
    }
    /**
     * Renames an item
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
        
        setHierarchyData(hierarchy);
        pushHierarchy();
        pushMin();
        pushMax();
    }
    /**
     * Creates the control
     * @param parent
     */
    private void create(final Composite parent) {

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

        // Create table
        this.table = new ComponentTable(base, SWT.BORDER);
        this.table.getControl().setLayoutData(SWTUtil.createFillGridData());

        // Create the menu and editing controls
        if (editable) {
            createMenu();
        }
    }

    /**
     * Creates all components required for making the table editable
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

        // Creates the editors menu
        this.menu = new Menu(table.getControl());
        table.getControl().setMenu(menu);
        
        // Insert row action
        final MenuItem insertRow = new MenuItem(menu, SWT.NONE);
        insertRow.setText(Resources.getMessage("HierarchyView.7")); //$NON-NLS-1$
        insertRow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionInsertRow();
            }
        });

        // Delete row action
        final MenuItem deleteRow = new MenuItem(menu, SWT.NONE);
        deleteRow.setText(Resources.getMessage("HierarchyView.8")); //$NON-NLS-1$
        deleteRow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionDeleteRow();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);

        // Insert column action
        final MenuItem insertColumn = new MenuItem(menu, SWT.NONE);
        insertColumn.setText(Resources.getMessage("HierarchyView.9")); //$NON-NLS-1$
        insertColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionInsertColumn();
            }
        });

        // Delete column action
        final MenuItem deleteColumn = new MenuItem(menu, SWT.NONE);
        deleteColumn.setText(Resources.getMessage("HierarchyView.10")); //$NON-NLS-1$
        deleteColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionDeleteColumn();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);

        // Move up
        final MenuItem up = new MenuItem(menu, SWT.NONE);
        up.setText(Resources.getMessage("HierarchyView.11")); //$NON-NLS-1$
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionMoveRowUp();
            }
        });

        // Move down
        final MenuItem down = new MenuItem(menu, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyView.12")); //$NON-NLS-1$
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionMoveRowDown();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);

        // Edit item action
        final MenuItem editItem = new MenuItem(menu, SWT.NONE);
        editItem.setText(Resources.getMessage("HierarchyView.18")); //$NON-NLS-1$
        editItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionEditItem();
            }
        });

        // Rename item action
        final MenuItem renameItem = new MenuItem(menu, SWT.NONE);
        renameItem.setText(Resources.getMessage("HierarchyView.15")); //$NON-NLS-1$
        renameItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionRenameItem();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Action clear
        final MenuItem clear = new MenuItem(menu, SWT.NONE);
        clear.setText(Resources.getMessage("HierarchyView.16")); //$NON-NLS-1$
        clear.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionClear();
            }
        });

        // Action intialize
        final MenuItem initialize = new MenuItem(menu, SWT.NONE);
        initialize.setText(Resources.getMessage("HierarchyView.19")); //$NON-NLS-1$
        initialize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionInitialize();
            }
        });
    }

    /**
     * Returns the index of
     * @param selection
     * @return
     */
    private int minIndexOf(String selection){
        for (int i=0; i<min.getItems().length; i++){
            if (min.getItem(i).equals(selection)) return i;
        }
        return -1;
    }

    /**
     * Returns the index of
     * @param selection
     * @return
     */
    private int maxIndexOf(String selection){
        for (int i=0; i<max.getItems().length; i++){
            if (max.getItem(i).equals(selection)) return i;
        }
        return -1;
    }
    /**
     * Updates the global hierarchy definition
     */
    private void pushHierarchy() {

        // Just write it to the model
        if (model == null || model.getInputConfig() == null) { return; }
        Hierarchy h = Hierarchy.create(hierarchy);
        model.getInputConfig().setHierarchy(attribute, h);
    }

    private void updateCombos(){
        
        // Check whether min & max are still ok
        if (model==null || min == null) { return; }

        final List<String> minItems = new ArrayList<String>();
        final List<String> maxItems = new ArrayList<String>();
        minItems.add(ITEM_ALL);
        for (int i = 1; i <= (hierarchy==null ? 0 : hierarchy[0].length); i++) {
            minItems.add(String.valueOf(i));
            maxItems.add(String.valueOf(i));
        }
        maxItems.add(ITEM_ALL);

        // Compute from model
        Integer minModel = model.getInputConfig().getMinimumGeneralization(attribute);
        String minSelected = ITEM_ALL;
        if (minModel != null) minSelected = String.valueOf(minModel+1);
        int minIndex = minIndexOf(minSelected);
                
        Integer maxModel = model.getInputConfig().getMaximumGeneralization(attribute);
        String maxSelected = ITEM_ALL;
        if (maxModel != null) maxSelected = String.valueOf(maxModel+1);
        int maxIndex = maxIndexOf(maxSelected);

        if (minIndex > (maxIndex + 1)) {
            minIndex = maxIndex + 1;
        }

        min.setItems(minItems.toArray(new String[] {}));
        max.setItems(maxItems.toArray(new String[] {}));

        min.select(minIndex);
        max.select(maxIndex);
        pushMin();
        pushMax();
    }

    /**
     * Updates the max generalization level
     * @return
     */
    private boolean pushMax(){
        if (max.getSelectionIndex() >= 0 && max.getItemCount()>1) {
            if (max.getSelectionIndex() < (min.getSelectionIndex() - 1)) {
                max.select(min.getSelectionIndex() - 1);
            }
            if (model != null) {
                String val = max.getItem(max.getSelectionIndex());
                if (val.equals(ITEM_ALL)) {
                    model.getInputConfig().setMaximumGeneralization(attribute, null);
                } else {
                    model.getInputConfig().setMaximumGeneralization(attribute, Integer.valueOf(val) - 1);
                }
                return true;
            } 
        } 
        return false;
    }

    /**
     * Updates the min generalization level
     * @return
     */
    private boolean pushMin() {
        
        if (min.getSelectionIndex() >= 0 && min.getItemCount() > 1) {
            if (min.getSelectionIndex() > (max.getSelectionIndex() + 1)) {
                min.select(max.getSelectionIndex() + 1);
            } 
            if (model != null) {
                String val = min.getItem(min.getSelectionIndex());
                if (val.equals(ITEM_ALL)) {
                    model.getInputConfig().setMinimumGeneralization(attribute, null);
                } else {
                    model.getInputConfig().setMinimumGeneralization(attribute, Integer.valueOf(val) - 1);
                }
                return true;
            }
        }
        return false;
    }
}
