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

package org.deidentifier.arx.gui.view.impl.explore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
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
 * This class displays the clipboard
 * @author Fabian Prasser
 */
public class ViewClipboard implements IView {

    private static final int        NODE_COMMENT      = 111;
    private final Table             table;
    private final List<TableItem>   items             = new ArrayList<TableItem>();
    private final List<TableColumn> columns           = new ArrayList<TableColumn>();
    private Model                   model;
    private final Composite         root;
    private final Controller        controller;
    private final Menu              menu;
    private TableItem               selectedTableItem = null;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public ViewClipboard(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(ModelPart.CLIPBOARD, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_NODE, this);
        this.controller = controller;

        // Create group
        ComponentTitledBorder border = new ComponentTitledBorder(parent, controller, 
                                                                 Resources.getMessage("NodeClipboardView.0"), //$NON-NLS-1$ 
                                                                 "id-23"); //$NON-NLS-1$
        root = new Composite(border.getControl(), SWT.NONE);
        border.setChild(root);
        border.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupNodeGridLayout = new GridLayout();
        root.setLayout(groupNodeGridLayout);

        table = new Table(root, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Selected node
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                final TableItem[] s = table.getSelection();
                if (s.length > 0) {
                    final ARXNode node = (ARXNode) s[0].getData();
                    model.setSelectedNode(node);
                    controller.update(new ModelEvent(ViewClipboard.this, ModelPart.SELECTED_NODE, node));
                }
            }
        });
        
        // Menu
        this.menu = new Menu(parent.getShell());
        MenuItem item1 = new MenuItem(menu, SWT.NONE);
        item1.setText(Resources.getMessage("NodeClipboardView.1")); //$NON-NLS-1$
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (selectedTableItem != null) {
                    ARXNode node = (ARXNode)selectedTableItem.getData(); 
                    final String label = Arrays.toString(node.getTransformation());
                    final String value = controller.actionShowInputDialog(controller.getResources().getShell(),
                                                                          Resources.getMessage("NodeClipboardView.4"),  //$NON-NLS-1$
                                                                          Resources.getMessage("NodeClipboardView.5") + label, //$NON-NLS-1$
                                                                          selectedTableItem.getText(1));
                    if (value != null) {
                        selectedTableItem.setText(1, value);
                        node.getAttributes().put(NODE_COMMENT, value);
                    }
                }
            }
        });
        
        MenuItem item2 = new MenuItem(menu, SWT.NONE);
        item2.setText(Resources.getMessage("NodeClipboardView.2")); //$NON-NLS-1$
        item2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (selectedTableItem != null) {
                    ARXNode node = (ARXNode)selectedTableItem.getData();
                    model.getClipboard().remove(node);
                    removeItem(selectedTableItem);
                    controller.update(new ModelEvent(ViewClipboard.this, ModelPart.CLIPBOARD, null));
                }
            }
        });
        
        MenuItem item3 = new MenuItem(menu, SWT.NONE);
        item3.setText(Resources.getMessage("NodeClipboardView.3")); //$NON-NLS-1$
        item3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                controller.actionApplySelectedTransformation();
                controller.update(new ModelEvent(this, ModelPart.SELECTED_NODE, model.getSelectedNode()));
            }
        });
        
        // Context menu
        table.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                if (event.button == 3) {
                    final TableItem i = getItemAt(event.x, event.y);
                    if (i != null) {
                        final ARXNode node = (ARXNode) i.getData();
                        model.setSelectedNode(node);
                        controller.update(new ModelEvent(ViewClipboard.this, ModelPart.SELECTED_NODE, node));
                        selectedTableItem = i;
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
        
        final TableColumn c = new TableColumn(table, SWT.NONE);
        c.setText(Resources.getMessage("NodeClipboardView.6")); //$NON-NLS-1$
        columns.add(c);

        final TableColumn c2 = new TableColumn(table, SWT.NONE);
        c2.setText(Resources.getMessage("NodeClipboardView.7")); //$NON-NLS-1$
        columns.add(c2);

        c.pack();
        c2.pack();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns the item at the given location
     * @param pt
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
     * Removes the item
     * @param item
     */
    private void removeItem(final TableItem item) {
        int index = -1;
        for (int i = 0; i < table.getItemCount(); i++) {
            if (table.getItem(i) == item) {
                index = i;
                break;
            }
        }
        if (index == -1) { return; }
        table.setRedraw(false);
        table.remove(index);
        table.setRedraw(true);
        table.redraw();
    }

    @Override
    public void reset() {
        table.setRedraw(false);
        table.removeAll();
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();
        table.setRedraw(true);
        table.redraw();
        SWTUtil.disable(root);
    }

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
                    }
                }
                SWTUtil.enable(root);
            }
        } else if (event.part == ModelPart.CLIPBOARD) {
            table.setRedraw(false);
            table.removeAll();
            for (final TableItem i : items) {
                i.dispose();
            }
            items.clear();
            final List<ARXNode> nodes = new ArrayList<ARXNode>();
            nodes.addAll(model.getClipboard());
            Collections.sort(nodes, new Comparator<ARXNode>() {
                @Override
                public int compare(final ARXNode arg0, final ARXNode arg1) {
                    for (int i = 0; i < arg0.getTransformation().length; i++) {
                        if (arg0.getTransformation()[i] < arg1.getTransformation()[i]) {
                            return -1;
                        } else if (arg0.getTransformation()[i] > arg1.getTransformation()[i]) { return +1; }
                    }
                    return 0;
                }
            });

            for (final ARXNode node : nodes) {
                final TableItem i = new TableItem(table, SWT.NONE);
                i.setText(0, Arrays.toString(node.getTransformation()));
                if (node.getAttributes().get(NODE_COMMENT) != null) {
                    i.setText(1, (String) node.getAttributes().get(NODE_COMMENT));
                } else {
                    i.setText(1, ""); //$NON-NLS-1$
                }
                i.setData(node);
                items.add(i);
            }
            for (final TableColumn c : columns) {
                c.pack();
            }
            table.setRedraw(true);
            table.redraw();
            SWTUtil.enable(root);
        }
    }
}
