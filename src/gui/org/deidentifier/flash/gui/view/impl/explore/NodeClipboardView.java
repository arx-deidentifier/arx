/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.view.impl.explore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import cern.colt.Arrays;

public class NodeClipboardView implements IView {

    private static final String     TITLE        = Resources.getMessage("NodeClipboardView.0"); //$NON-NLS-1$
    private static final int        NODE_COMMENT = 111;

    private final Table             table;
    private final List<TableItem>   items        = new ArrayList<TableItem>();
    private final List<TableColumn> columns      = new ArrayList<TableColumn>();
    private Model                   model;
    private final Group             root;
    private final Controller        controller;

    public NodeClipboardView(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(EventTarget.CLIPBOARD, this);
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(EventTarget.SELECTED_NODE, this);
        this.controller = controller;

        // Create group
        root = new Group(parent, SWT.NULL);
        root.setText(TITLE);
        root.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupNodeGridLayout = new GridLayout();
        root.setLayout(groupNodeGridLayout);
        final IView outer = this;

        table = new Table(root, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL |
                                SWT.H_SCROLL);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Selected node
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                final TableItem[] s = table.getSelection();
                if (s.length > 0) {
                    final FLASHNode node = (FLASHNode) s[0].getData();
                    model.setSelectedNode(node);
                    controller.update(new ModelEvent(outer,
                                                     EventTarget.SELECTED_NODE,
                                                     node));
                }
            }
        });

        // Context menu
        table.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                if (event.button == 3) {
                    final Point pt = new Point(event.x, event.y);
                    final TableItem i = getItemAt(pt);
                    if (i != null) {
                        final String item1 = Resources.getMessage("NodeClipboardView.1"); //$NON-NLS-1$
                        final String item2 = Resources.getMessage("NodeClipboardView.2"); //$NON-NLS-1$
                        final String item3 = Resources.getMessage("NodeClipboardView.3"); //$NON-NLS-1$
                        controller.getPopup().setItems(new String[] { item1,
                                                               item2,
                                                               item3 },
                                                       new SelectionAdapter() {
                                                           @Override
                                                           public void
                                                                   widgetSelected(final SelectionEvent arg0) {
                                                               final FLASHNode node = (FLASHNode) i.getData();
                                                               model.setSelectedNode(node);
                                                               controller.update(new ModelEvent(outer,
                                                                                                EventTarget.SELECTED_NODE,
                                                                                                node));
                                                               if (arg0.data.equals(item1)) {
                                                                   final String label = Arrays.toString(node.getTransformation());
                                                                   final String value = controller.actionShowInputDialog(Resources.getMessage("NodeClipboardView.4"), Resources.getMessage("NodeClipboardView.5") + label, i.getText(1)); //$NON-NLS-1$ //$NON-NLS-2$
                                                                   if (value != null) {
                                                                       i.setText(1,
                                                                                 value);
                                                                       node.getAttributes()
                                                                           .put(NODE_COMMENT,
                                                                                value);
                                                                   }

                                                               } else if (arg0.data.equals(item2)) {
                                                                   model.getClipboard()
                                                                        .remove(node);
                                                                   removeItem(i);
                                                                   controller.update(new ModelEvent(outer,
                                                                                                    EventTarget.CLIPBOARD,
                                                                                                    null));
                                                               } else if (arg0.data.equals(item3)) {
                                                                   controller.actionApplySelectedTransformation();
                                                               }
                                                           }

                                                       });
                        controller.getPopup().show(Display.getDefault()
                                                          .getCursorLocation());
                        controller.getToolTip().hide();
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

    private TableItem getItemAt(final Point pt) {
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
        if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
        } else if (event.target == EventTarget.SELECTED_NODE) {
            final FLASHNode selected = (FLASHNode) event.data;

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
        } else if (event.target == EventTarget.CLIPBOARD) {
            table.setRedraw(false);
            table.removeAll();
            for (final TableItem i : items) {
                i.dispose();
            }
            items.clear();
            final List<FLASHNode> nodes = new ArrayList<FLASHNode>();
            nodes.addAll(model.getClipboard());
            Collections.sort(nodes, new Comparator<FLASHNode>() {
                @Override
                public int compare(final FLASHNode arg0, final FLASHNode arg1) {
                    for (int i = 0; i < arg0.getTransformation().length; i++) {
                        if (arg0.getTransformation()[i] < arg1.getTransformation()[i]) {
                            return -1;
                        } else if (arg0.getTransformation()[i] > arg1.getTransformation()[i]) { return +1; }
                    }
                    return 0;
                }
            });

            for (final FLASHNode node : nodes) {
                final TableItem i = new TableItem(table, SWT.NONE);
                i.setText(0, Arrays.toString(node.getTransformation()));
                if (node.getAttributes().get(NODE_COMMENT) != null) {
                    i.setText(1, (String) node.getAttributes()
                                              .get(NODE_COMMENT));
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
