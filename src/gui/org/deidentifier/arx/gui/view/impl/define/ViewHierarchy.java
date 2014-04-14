/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * This class implements an editor for generalization hierarchies. It is partly
 * based upon code implemented by Ledian Xhani and Ljubomir Dshevlekov.
 * 
 * @author Prasser, Kohlmayer, Xhani, Dshevlekov
 * 
 */
public class ViewHierarchy implements IView {

    private static final String ITEM_ALL  = Resources.getMessage("HierarchyView.0"); //$NON-NLS-1$

    /** Editors cell color */
    private static final Color  COLOR     = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    /** Editors table */
    private Table               table;
    
    /** Table editor*/
    private TableEditor         editor;

    /** Controller */
    private Controller          controller;

    /** Bottom parent container */
    private Composite           base;

    /** The column attribute */
    private final String        attribute;

    /** The selected row for the insert/remove row event */
    private TableItem           targetRow;

    /** The selected column for the insert/remove column event */
    private TableColumn         targetColumn;

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

    /**
     * Constructor for not editable views
     * 
     * @param parent
     */
    public ViewHierarchy(final Composite parent) {

        this.attribute = null;
        this.editable = false;
        create(parent);

    }
    
    /**
     * Constructor for not editable views
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

        hierarchy = type.getHierarchy();
        if (table.isDisposed()) return;
        
        table.setRedraw(false);

        for (final TableColumn t : table.getColumns()) {
            t.dispose();
        }
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }

        if ((type.getHierarchy() == null) ||
            (type.getHierarchy().length == 0)) {
            table.setRedraw(true);
            table.redraw();
            return;
        }

        final TableColumn[] column = new TableColumn[type.getHierarchy()[0].length];
        for (int i = 0; i < column.length; i++) {
            column[i] = new TableColumn(table, SWT.NONE);
            column[i].setText(Resources.getMessage("HierarchyView.1") + (i + 1)); //$NON-NLS-1$
            column[i].pack();
        }

        for (int i = 0; i < type.getHierarchy().length; i++) {
            final TableItem item = new TableItem(table, SWT.NONE);
            item.setBackground(Display.getCurrent()
                                      .getSystemColor(SWT.COLOR_GRAY));
            for (int j = 0; j < type.getHierarchy()[i].length; j++) {
                item.setText(j, type.getHierarchy()[i][j]);
            }
        }

        for (final TableColumn t : table.getColumns()) {
            t.pack();
        }

        table.setRedraw(true);
        table.redraw();

        updateCombos();
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
     * Clears the hierarchy
     */
    private void actionClear() {
        
        if (null == targetColumn) { return; }
        
        base.setRedraw(false);
        setHierarchy(Hierarchy.create());
        base.setRedraw(true);
        base.redraw();
        
        targetColumn = null;
        targetRow = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    // Deletes a column
    private void actionDeleteColumn() {
        
        if (null == targetColumn) { return; }
        
        base.setRedraw(false);
        int index = table.indexOf(targetColumn);
        TableColumn toRemove = table.getColumn(index);
        toRemove.dispose();
        updateColumnTitles();
        base.setRedraw(true);
        base.redraw();
        
        targetColumn = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }
    
    /**
     * Deletes a row
     */
    private void actionDeleteRow() {
        
        if (null == targetRow) { return; }
        
        base.setRedraw(false);
        int index = table.indexOf(targetRow);
        TableItem toRemove = table.getItem(index);
        toRemove.dispose();
        base.setRedraw(true);
        base.redraw();
        
        targetRow = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Inserts a column
     */
    private void actionInsertColumn() {
        
        int index = table.getColumnCount();
        if (targetColumn != null) {
            index = table.indexOf(targetColumn) + 1;
        }
        
        base.setRedraw(false);
        TableColumn newColumn = new TableColumn(table, SWT.NONE, index);
        newColumn.setWidth(60);
        updateColumnTitles();
        base.setRedraw(true);
        base.redraw();
        
        targetColumn = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Inserts a row
     */
    private void actionInsertRow() {
        
        int index = table.getItemCount();
        if (targetRow != null) {
            index = table.indexOf(targetRow) + 1;
        }
        
        base.setRedraw(false);
        TableItem newItem = new TableItem(table, SWT.NONE, index);
        newItem.setBackground(COLOR);
        base.setRedraw(true);
        base.redraw();
        
        targetRow = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Double click action, potentially activates the table cell editor
     * @param location
     */
    private void actionMouseDoubleClick(final Point location) {
        final Rectangle clientArea = table.getClientArea();
        int index = table.getTopIndex();
        while (index < table.getItemCount()) {
            boolean visible = false;
            final TableItem item = table.getItem(index);
            for (int i = 0; i < table.getColumnCount(); i++) {
                final Rectangle rect = item.getBounds(i);
                if (rect.contains(location)) {
                    final int column = i;
                    final Text text = new Text(table, SWT.NONE);
                    final int row = index;
                    final Listener textListener = new Listener() {
                        @Override
                        public void handleEvent(final Event e) {
                            switch (e.type) {
                            case SWT.FocusOut: {
                                item.setText(column, text.getText());
                                hierarchy[row][column] = text.getText();
                                text.dispose();
                                break;
                            }
                            case SWT.Traverse: {
                                switch (e.detail) {
                                case SWT.TRAVERSE_RETURN:
                                    item.setText(column,
                                                 text.getText());
                                    hierarchy[row][column] = text.getText();
                                case SWT.TRAVERSE_ESCAPE: {
                                    text.dispose();
                                    e.doit = false;
                                }
                                }
                                break;
                            }
                            }
                        }
                    };
                    text.addListener(SWT.FocusOut, textListener);
                    text.addListener(SWT.Traverse, textListener);
                    editor.setEditor(text, item, i);
                    text.setText(item.getText(i));
                    text.selectAll();
                    text.setFocus();
                    return;
                }
                if (!visible && rect.intersects(clientArea)) {
                    visible = true;
                }
            }
            if (!visible) { return; }
            index++;
        }
    }
    /**
     * Called when the right button is clicked on the table
     * @param location
     */
    private void actionMouseDown(Point location) {
        targetRow = null;
        targetColumn = null;
        targetRow = table.getItem(location);
        if (targetRow == null) { return; }
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (targetRow.getBounds(i).contains(location)) {
                targetColumn = table.getColumn(i);
                break;
            }
        }
    }

    /**
     * Moves an element down
     */
    private void actionMoveDown() {
        if (null == targetRow) { return; }

        int index = table.indexOf(targetRow);
        if (index >= (table.getItemCount() - 1)) { return; }

        table.setRedraw(false);
        TableItem o1 = table.getItems()[index];
        TableItem o2 = table.getItems()[index + 1];
        
        TableItem n1 = new TableItem(table, SWT.NONE, index);
        n1.setBackground(COLOR);
        for (int i = 0; i < table.getColumnCount(); i++) {
            n1.setText(i, o1.getText(i));
        }

        TableItem n2 = new TableItem(table, SWT.NONE, index);
        n2.setBackground(COLOR);
        for (int i = 0; i < table.getColumnCount(); i++) {
            n2.setText(i, o2.getText(i));
        }

        o1.dispose();
        o2.dispose();

        base.setRedraw(true);
        base.redraw();
        
        targetRow = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Moves an element up
     */
    private void actionMoveUp() {
        if (null == targetRow) { return; }

        final int index = table.indexOf(targetRow);
        if (index <= 0) { return; }

        table.setRedraw(false);
        final TableItem o1 = table.getItems()[index - 1];
        final TableItem o2 = table.getItems()[index];

        final TableItem n1 = new TableItem(table, SWT.NONE, index);
        n1.setBackground(COLOR);
        for (int i = 0; i < table.getColumnCount(); i++) {
            n1.setText(i, o1.getText(i));
        }

        final TableItem n2 = new TableItem(table, SWT.NONE, index);
        n2.setBackground(COLOR);
        for (int i = 0; i < table.getColumnCount(); i++) {
            n2.setText(i, o2.getText(i));
        }

        o1.dispose();
        o2.dispose();

        base.setRedraw(true);
        base.redraw();
        
        targetRow = null;
        updateArray();
        updateCombos();
        pushHierarchy();
        pushMin();
        pushMax();
    }

    /**
     * Renames an item
     */
    private void actionRenameItem() {
        if (null == targetColumn) { return; }
        if (null == targetRow) { return; }
        final int index = table.indexOf(targetColumn);

        final String oldValue = targetRow.getText(index);
        final String newValue = controller.actionShowInputDialog(controller.getResources().getShell(),
                                                                 Resources.getMessage("HierarchyView.13"), Resources.getMessage("HierarchyView.14"), oldValue); //$NON-NLS-1$ //$NON-NLS-2$

        if (newValue != null) {
            int row = 0;
            for (final TableItem i : table.getItems()) {
                if (i.getText(index).equals(oldValue)) {
                    i.setText(index, newValue);
                }
                if (hierarchy[row][index].equals(oldValue)) {
                    hierarchy[row][index] = newValue;
                }
                row++;
            }
            table.redraw();
            updateColumnTitles();
        }
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
        int flags = SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION;
        flags |= editable ? SWT.VIRTUAL : 0;
        this.table = new Table(base, flags);
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        this.table.setLayoutData(SWTUtil.createFillGridData());

        // Insert one empty column
        TableColumn column = new TableColumn(table, SWT.NONE, 0);
        column.setWidth(60);
        this.table.redraw();
        this.updateColumnTitles();

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
                    actionMouseDown(new Point(e.x, e.y));
                }
            }
        });

        // Creates the editors menu
        final Menu menu = new Menu(table);
        table.setMenu(menu);
        
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
                actionMoveUp();
            }
        });

        // Move down
        final MenuItem down = new MenuItem(menu, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyView.12")); //$NON-NLS-1$
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                actionMoveDown();
            }
        });

        // Separator
        new MenuItem(menu, SWT.SEPARATOR);

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

        // Create table editor
        this.editor = new TableEditor(table);
        this.editor.horizontalAlignment = SWT.LEFT;
        this.editor.grabHorizontal = true;

        // Make table editable
        table.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                actionMouseDoubleClick(new Point(event.x, event.y));
            }
        });
    }

    /** Updates the titles of the columns after an event */
    private void updateColumnTitles() {
        int idx = 0;
        for (final TableColumn col : table.getColumns()) {
            idx++;
            col.setText(Resources.getMessage("HierarchyView.17") + idx); //$NON-NLS-1$
        }
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
        final Hierarchy h = Hierarchy.create(hierarchy);
        model.getInputConfig().setHierarchy(attribute, h);
    }

    /**
     * Updates the hierarchy
     */
    private void updateArray() {
        

        // Rebuild the array from the table
        final int rows = table.getItemCount();
        final int cols = table.getColumnCount();
        final String[][] s = new String[rows][cols];
        int idx = 0;
        for (final TableItem item : table.getItems()) {
            for (int i = 0; i < cols; i++) {
                s[idx][i] = item.getText(i);
            }
            idx++;
        }
        hierarchy = s;
    }
    
    private void updateCombos(){
        
        // Check whether min & max are still ok
        if (model==null || min == null) { return; }

        final List<String> minItems = new ArrayList<String>();
        final List<String> maxItems = new ArrayList<String>();
        minItems.add(ITEM_ALL);
        for (int i = 1; i <= table.getColumnCount(); i++) {
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
