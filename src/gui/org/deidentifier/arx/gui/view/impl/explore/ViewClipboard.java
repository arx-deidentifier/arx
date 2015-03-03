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

package org.deidentifier.arx.gui.view.impl.explore;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import cern.colt.Arrays;

/**
 * This class displays the clipboard.
 *
 * @author Fabian Prasser
 */
public class ViewClipboard implements IView {

    /** Identifier for key in the nodes' attribute maps. */
    private static final int        NODE_COMMENT = 111;
    
    /** Image. */
    private final Image             IMAGE_DOWN;
    
    /** Image. */
    private final Image             IMAGE_UP;
    
    /** Image. */
    private final Image             IMAGE_SORT;
    
    /** Image. */
    private final Image             IMAGE_REMOVE;

    /** Component. */
    private final Table             table;
    
    /** Component. */
    private final List<TableColumn> columns      = new ArrayList<TableColumn>();
    
    /** Component. */
    private final Composite         root;
    
    /** Component. */
    private final Menu              menu;
    
    /** Component. */
    private TableItem               selectedItem = null;

    /** Model. */
    private Model                   model;
    
    /** Controller. */
    private final Controller        controller;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewClipboard(final Composite parent, final Controller controller) {

        IMAGE_DOWN = controller.getResources().getImage("arrow_down.png"); //$NON-NLS-1$
        IMAGE_UP = controller.getResources().getImage("arrow_up.png");//$NON-NLS-1$
        IMAGE_SORT = controller.getResources().getImage("table_sort.png");//$NON-NLS-1$
        IMAGE_REMOVE = controller.getResources().getImage("delete.png");//$NON-NLS-1$

        // Listen
        controller.addListener(ModelPart.CLIPBOARD, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_NODE, this);
        this.controller = controller;

        ComponentTitledFolderButton bar = new ComponentTitledFolderButton("id-23"); //$NON-NLS-1$
        bar.add("Remove", IMAGE_REMOVE, new Runnable(){
            public void run() {
                actionRemove();
            }
        });
        bar.add("Move up", IMAGE_UP, new Runnable(){
            public void run() {
                actionUp();
            }
        });
        bar.add("Move down", IMAGE_DOWN, new Runnable(){
            public void run() {
                actionDown();
            }
        });
        bar.add("Sort by information loss", IMAGE_SORT, new Runnable(){
            public void run() {
                actionSort();
            }
        });

        // Create group
        ComponentTitledFolder border = new ComponentTitledFolder(parent, controller,  bar, null); //$NON-NLS-1$
        border.setLayoutData(SWTUtil.createFillGridData());
        
        // Create root
        root = border.createItem(Resources.getMessage("NodeClipboardView.0"), //$NON-NLS-1$
                                 null);
        root.setLayout(new FillLayout());
        border.setSelection(0);
        border.setEnabled(true);

        // Create table
        table = new Table(root, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Selected node
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                final TableItem[] s = table.getSelection();
                if (s.length > 0) {
                    final ARXNode node = (ARXNode) s[0].getData();
                    selectedItem = s[0];
                    model.setSelectedNode(node);
                    controller.update(new ModelEvent(ViewClipboard.this, ModelPart.SELECTED_NODE, node));
                }
            }
        });
        
        // Menu
        this.menu = createMenu(parent);
        
        
        // Add menu to table
        table.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                if (event.button == 3) {
                    final TableItem i = getItemAt(event.x, event.y);
                    if (i != null) {
                        final ARXNode node = (ARXNode) i.getData();
                        model.setSelectedNode(node);
                        controller.update(new ModelEvent(ViewClipboard.this, ModelPart.SELECTED_NODE, node));
                        selectedItem = i;
                        Point point = table.toDisplay(event.x, event.y);
                        Rectangle bounds = i.getBounds();
                        bounds.x = table.toDisplay(bounds.x, bounds.y).x;
                        bounds.y = table.toDisplay(bounds.x, bounds.y).y;
                        menu.setLocation(point);
                        menu.setVisible(true);
                    }
                }
            }
        });
        
        // First column
        final TableColumn c = new TableColumn(table, SWT.NONE);
        c.setText(Resources.getMessage("NodeClipboardView.6")); //$NON-NLS-1$
        columns.add(c);

        // Second column
        final TableColumn c2 = new TableColumn(table, SWT.NONE);
        c2.setText(Resources.getMessage("NodeClipboardView.7")); //$NON-NLS-1$
        columns.add(c2);

        c.pack();
        c2.pack();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
        IMAGE_DOWN.dispose();
        IMAGE_UP.dispose();
        IMAGE_SORT.dispose();
        IMAGE_REMOVE.dispose();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        
        table.setRedraw(false);
        removeAllItems();
        table.setRedraw(true);
        table.redraw();
        SWTUtil.disable(root);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        } else if (event.part == ModelPart.SELECTED_NODE) {
            final ARXNode selected = (ARXNode) event.data;

            // No result available
            if (selected == null) {
                reset();
            } else {
                final String trans = Arrays.toString(selected.getTransformation());
                for (final TableItem i : table.getItems()) {
                    if (i.getText(0).equals(trans)) {
                        table.setSelection(i);
                        this.selectedItem = i;
                    }
                }
                SWTUtil.enable(root);
            }
        } else if (event.part == ModelPart.CLIPBOARD) {
            
            table.setRedraw(false);
            removeAllItems();
            addAllItemsFromModel();
            table.setRedraw(true);
            table.redraw();
            SWTUtil.enable(root);
            
            if (table.getItemCount()!=0) {
                this.selectedItem = table.getItem(0);
                table.select(0);
            } else {
                this.selectedItem = null;
            }
        }
    }
    
    /**
     * Action.
     */
    private void actionApplyTransformation() {
        if (selectedItem != null) {
            controller.actionApplySelectedTransformation();
            controller.update(new ModelEvent(this, ModelPart.SELECTED_NODE, model.getSelectedNode()));
        }
    }
    
    /**
     * Action.
     */
    private void actionCopy() {
        if (table != null) {
            new ClipboardHandlerTable(this.table).copy();
        }
    }

    /**
     * Action.
     */
    private void actionDown() {
        if (selectedItem != null) {
            int index = getItemIndex(selectedItem);
            if (index != -1 && index<table.getItemCount()-1) {
                table.setRedraw(false);
                ARXNode node = (ARXNode)selectedItem.getData();
                removeItem(selectedItem);
                addItem(node, index+1);
                table.setRedraw(true);
                table.redraw();
                model.getClipboard().moveEntryDown(node);
                table.select(index+1);
                this.selectedItem = table.getItem(index+1);
            }
        }
    }
    
    /**
     * Action.
     */
    private void actionEditComment() {
        if (selectedItem != null) {
            ARXNode node = (ARXNode)selectedItem.getData(); 
            final String label = Arrays.toString(node.getTransformation());
            final String value = controller.actionShowInputDialog(controller.getResources().getShell(),
                                                                  Resources.getMessage("NodeClipboardView.4"),  //$NON-NLS-1$
                                                                  Resources.getMessage("NodeClipboardView.5") + label, //$NON-NLS-1$
                                                                  selectedItem.getText(1));
            if (value != null) {
                selectedItem.setText(1, value);
                node.getAttributes().put(NODE_COMMENT, value);
            }
        }
    }

    /**
     * Action.
     */
    private void actionRemove() {
        if (selectedItem != null) {
            int index = getItemIndex(selectedItem);
            ARXNode node = (ARXNode)selectedItem.getData();
            model.getClipboard().removeFromClipboard(node);
            table.setRedraw(false);
            removeItem(selectedItem);
            table.setRedraw(true);
            table.redraw();
            controller.update(new ModelEvent(ViewClipboard.this, ModelPart.CLIPBOARD, null));
            if (table.getItemCount()==0){
                this.selectedItem = null;
                index = -1;
            } else if (index >= table.getItemCount()) {
                index = table.getItemCount()-1;
            }
            if (index != -1) {
                this.selectedItem = table.getItem(index);
                table.select(index);
            }
        }
    }

    /**
     * Action.
     */
    private void actionSort() {
        if (selectedItem != null) {
            int index = table.getSelectionIndex();
            table.setRedraw(false);
            removeAllItems();
            model.getClipboard().sort();
            addAllItemsFromModel();
            table.setRedraw(true);
            if (index!=-1) {
                table.select(index);
                this.selectedItem = table.getItem(index);
            } else {
                this.selectedItem = null;
            }
        }
    }

    /**
     * Action.
     */
    private void actionUp() {
        if (selectedItem != null) {
            int index = getItemIndex(selectedItem);
            if (index > 0) {
                table.setRedraw(false);
                ARXNode node = (ARXNode)selectedItem.getData();
                removeItem(selectedItem);
                addItem(node, index-1);
                table.setRedraw(true);
                table.redraw();
                model.getClipboard().moveEntryUp(node);
                table.select(index-1);
                this.selectedItem = table.getItem(index-1);
            }
        }
    }

    /**
     * Adds all items from the model.
     */
    private void addAllItemsFromModel() {
        // Add all to table
        for (final ARXNode node : model.getClipboard().getClipboardEntries()) {
            addItem(node, table.getItemCount());
        }
        
        // Pack
        for (final TableColumn c : columns) {
            c.pack();
        }
    }

    /**
     * Adds an item.
     *
     * @param node
     * @param index
     * @return
     */
    private TableItem addItem(ARXNode node, int index) {
        
        final TableItem item = new TableItem(table, SWT.NONE, index);
        item.setText(0, Arrays.toString(node.getTransformation()));
        if (node.getAttributes().get(NODE_COMMENT) != null) {
            item.setText(1, (String) node.getAttributes().get(NODE_COMMENT));
        } else {
            item.setText(1, ""); //$NON-NLS-1$
        }
        item.setData(node);
        return item;
    }

    /**
     * This method creates the context menu.
     *
     * @param parent
     * @return
     */
    private Menu createMenu(Composite parent) {
        
        final Menu menu = new Menu(parent.getShell());

        MenuItem item0 = new MenuItem(menu, SWT.NONE);
        item0.setText(Resources.getMessage("NodeClipboardView.11")); //$NON-NLS-1$
        item0.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionCopy();
            }
        });
        
        new MenuItem(menu, SWT.SEPARATOR);
        
        MenuItem item1 = new MenuItem(menu, SWT.NONE);
        item1.setText(Resources.getMessage("NodeClipboardView.1")); //$NON-NLS-1$
        item1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionEditComment();
            }
        });
        
        MenuItem item2 = new MenuItem(menu, SWT.NONE);
        item2.setText(Resources.getMessage("NodeClipboardView.2")); //$NON-NLS-1$
        item2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionRemove();
            }
        });
        
        MenuItem item3 = new MenuItem(menu, SWT.NONE);
        item3.setText(Resources.getMessage("NodeClipboardView.3")); //$NON-NLS-1$
        item3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionApplyTransformation();
            }
        });
        
        new MenuItem(menu, SWT.SEPARATOR);
        
        MenuItem item4 = new MenuItem(menu, SWT.NONE);
        item4.setText(Resources.getMessage("NodeClipboardView.8")); //$NON-NLS-1$
        item4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionUp();
            }
        });
        
        MenuItem item5 = new MenuItem(menu, SWT.NONE);
        item5.setText(Resources.getMessage("NodeClipboardView.9")); //$NON-NLS-1$
        item5.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionDown();
            }
        });
        
        MenuItem item6 = new MenuItem(menu, SWT.NONE);
        item6.setText(Resources.getMessage("NodeClipboardView.10")); //$NON-NLS-1$
        item6.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionSort();
            }
        });
        
        return menu;
    }

    /**
     * Returns the item at the given location.
     *
     * @param x
     * @param y
     * @return
     */
    private TableItem getItemAt(int x, int y) {
        Point pt = new Point(x, y);
        int index = table.getTopIndex();
        while (index < table.getItemCount()) {
            final TableItem item = table.getItem(index);
            for (int i = 0; i < columns.size(); i++) {
                final Rectangle rect = item.getBounds(i);
                if (rect.contains(pt)) { return item; }
            }
            index++;
        }
        return null;
    }
    
    /**
     * Returns the index of the given item.
     *
     * @param item
     * @return
     */
    private int getItemIndex(TableItem item) {
        int index = -1;
        for (int i = 0; i < table.getItemCount(); i++) {
            if (table.getItem(i) == item) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    /**
     * Removes all items.
     */
    private void removeAllItems() {
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.removeAll();
    }
    
    /**
     * Removes the item.
     *
     * @param item
     * @return
     */
    private int removeItem(final TableItem item) {
        int index = getItemIndex(item);
        if (index == -1) { return -1; }
        table.remove(index);
        return index;
    }
}
