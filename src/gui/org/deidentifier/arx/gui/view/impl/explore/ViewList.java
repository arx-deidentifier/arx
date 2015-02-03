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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.dynamictablecolumns.DynamicTable;
import org.mihalis.opal.dynamictablecolumns.DynamicTableColumn;

import cern.colt.Arrays;

/**
 * This class implements a list view on selected nodes.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ViewList extends ViewSolutionSpace {

    /** Symbol */
    private static final String SYMBOL     = new String(new char[] { '\u26AB' });

    /** The table. */
    private final DynamicTable  table;

    /** The list. */
    private final List<ARXNode> list       = new ArrayList<ARXNode>();

    /** The listener. */
    private Listener            listener;

    /** Color */
    private Color               background = null;

    /**
     * Contructor
     *
     * @param parent
     * @param controller
     */
    public ViewList(final Composite parent, final Controller controller) {
        
        super(parent, controller);

        table = new DynamicTable(parent, SWT.SINGLE | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setHeaderVisible(true);
        
        table.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                ARXNode node = list.get(table.getSelectionIndex());
                ViewList.this.actionSelectNode(node);
            }
        });
        
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseUp(MouseEvent arg0) {
                if (arg0.button == 3) {
                    if (getSelectedNode() != null) {
                        Point display = table.toDisplay(arg0.x, arg0.y);
                        getModel().setSelectedNode(getSelectedNode());
                        controller.update(new ModelEvent(ViewList.this, 
                                                         ModelPart.SELECTED_NODE, getSelectedNode()));
                        actionShowMenu(display.x, display.y);
                    }
                }
            }
        });

        final DynamicTableColumn column0 = new DynamicTableColumn(table, SWT.LEFT);
        column0.setText(""); //$NON-NLS-1$
        column0.setWidth("30px");
        final DynamicTableColumn column1 = new DynamicTableColumn(table, SWT.LEFT);
        column1.setText(Resources.getMessage("ListView.1")); //$NON-NLS-1$
        column1.setWidth("25%", "100px");
        final DynamicTableColumn column4 = new DynamicTableColumn(table, SWT.LEFT);
        column4.setText(Resources.getMessage("ListView.2")); //$NON-NLS-1$
        column4.setWidth("25%", "100px");
        final DynamicTableColumn column2 = new DynamicTableColumn(table, SWT.LEFT);
        column2.setText(Resources.getMessage("ListView.3")); //$NON-NLS-1$
        column2.setWidth("25%", "100px");
        final DynamicTableColumn column3 = new DynamicTableColumn(table, SWT.LEFT);
        column3.setText(Resources.getMessage("ListView.4")); //$NON-NLS-1$
        column3.setWidth("25%", "100px");

        table.setItemCount(0);
        
        column0.pack();
        column1.pack();
        column2.pack();
        column3.pack();
        column4.pack();

        // Create tooltip listener
        // TODO: Does not work on Windows
        Listener tableListener = new Listener() {

            private TableItem previousHighlighted = null;

            public void handleEvent(Event event) {
                if (previousHighlighted != null) {
                    if (!previousHighlighted.isDisposed()) {
                        previousHighlighted.setBackground(background);
                    }
                }

                TableItem item = table.getItem(new Point(event.x, event.y));
                if (item != null) {
                    item.setBackground(GUIHelper.COLOR_GRAY);
                    previousHighlighted = item;
                    ARXNode node = (ARXNode) item.getData();
                    table.redraw();
                    table.setToolTipText(getTooltipDecorator().decorate(node));
                }
            }
        };
        table.addListener(SWT.MouseMove, tableListener);
        table.addListener(SWT.MouseExit, tableListener);
    }

    /**
     * Resets the view.
     */
    @Override
    public void reset() {
        super.reset();
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        list.clear();
        table.setRedraw(true);
        if (listener != null) {
            table.removeListener(SWT.SetData, listener);
        }
        SWTUtil.disable(table);
    }

    /**
     * Creates an item in the list.
     *
     * @param item
     * @param index
     */
    private void createItem(final TableItem item, final int index) {

        final ARXNode node = list.get(index);

        item.setText(0, SYMBOL);
        
        final String transformation = Arrays.toString(node.getTransformation());
        item.setText(1, transformation);

        final String anonymity = node.getAnonymity().toString();
        item.setText(2, anonymity);

        String min = null;
        if (node.getMinimumInformationLoss() != null) {
            min = node.getMinimumInformationLoss().toString() +
                  " [" + getFormat().format(asRelativeValue(node.getMinimumInformationLoss())) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            min = Resources.getMessage("ListView.7"); //$NON-NLS-1$
        }
        item.setText(3, min);
        String max = null;
        if (node.getMaximumInformationLoss() != null) {
            max = node.getMaximumInformationLoss().toString() +
                  " [" + getFormat().format(asRelativeValue(node.getMaximumInformationLoss())) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            max = Resources.getMessage("ListView.10"); //$NON-NLS-1$
        }
        item.setText(4, max);
        item.setData(node);
        item.setForeground(0, super.getInnerColor(node));
        this.background = this.background != null ? this.background : item.getBackground();
    }

    /**
     * Updates the list.
     *
     * @param result
     * @param filter
     */
    private void update(final ARXResult result, final ModelNodeFilter filter) {
        
        if (result == null || result.getLattice() == null) return;
        if (filter == null) return;
        
        getController().getResources().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!table.isEnabled()) {
                    SWTUtil.enable(table);
                }
                table.setRedraw(false);
                for (final TableItem i : table.getItems()) {
                    i.dispose();
                }
                list.clear();
                
                final ARXLattice l = result.getLattice();
                for (final ARXNode[] level : l.getLevels()) {
                    for (final ARXNode node : level) {
                        if (filter.isAllowed(result.getLattice(), node)) {
                            list.add(node);
                        }
                    }
                }

                Collections.sort(list, new Comparator<ARXNode>() {
                    @Override
                    public int compare(final ARXNode arg0,
                                       final ARXNode arg1) {
                        return arg0.getMaximumInformationLoss()
                                   .compareTo(arg1.getMaximumInformationLoss());
                    }
                });

                // Check
                if (list.size() > getModel().getMaxNodesInViewer()) {
                    list.clear();
                }

                if (listener != null) {
                    table.removeListener(SWT.SetData, listener);
                }
                listener = new Listener() {
                    @Override
                    public void handleEvent(final Event event) {
                        final TableItem item = (TableItem) event.item;
                        final int index = table.indexOf(item);
                        createItem(item, index);
                    }

                };
                table.addListener(SWT.SetData, listener);
                table.setItemCount(list.size());
                table.setRedraw(true);
            }
        });
    }

    @Override
    protected void actionRedraw() {
        this.table.redraw();
    }

    @Override
    protected void eventFilterChanged(ARXResult result, ModelNodeFilter filter) {
        update(result, filter);
    }

    @Override
    protected void eventModelChanged() {
        update(getModel().getResult(), getModel().getNodeFilter());
    }

    @Override
    protected void eventNodeSelected() {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(getSelectedNode())) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        this.table.select(index);
    }

    @Override
    protected void eventResultChanged(ARXResult result) {
        if (result == null) reset();
    }
}
