/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.explore;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IAttachable;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import cern.colt.Arrays;

/**
 * This class implements a list view on selected nodes
 * 
 * @author prasser
 */
public class ListView implements IView, IAttachable {

    /** The optimum */
    private ARXNode             optimum;

    /** The selected node */
    private ARXNode             selectedNode;

    /** The controller */
    private final Controller    controller;

    /** The format */
    private final NumberFormat  format = new DecimalFormat("##0.000"); //$NON-NLS-1$

    /** The table */
    private final Table         table;

    /** The model */
    private Model               model;

    /** The list */
    private final List<ARXNode> list   = new ArrayList<ARXNode>();

    /** The listener */
    private Listener            listener;

    /**
     * Init
     * 
     * @param parent
     * @param controller
     */
    public ListView(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(EventTarget.SELECTED_NODE, this);
        controller.addListener(EventTarget.FILTER, this);
        controller.addListener(EventTarget.MODEL, this);

        this.controller = controller;

        table = new Table(parent, SWT.SINGLE | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setHeaderVisible(true);
        
        final TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setText(Resources.getMessage("ListView.1")); //$NON-NLS-1$
        final TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setText(Resources.getMessage("ListView.2")); //$NON-NLS-1$
        final TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setText(Resources.getMessage("ListView.3")); //$NON-NLS-1$
        final TableColumn column3 = new TableColumn(table, SWT.LEFT);
        column3.setText(Resources.getMessage("ListView.4")); //$NON-NLS-1$

        table.setItemCount(0);
        
        column1.pack();
        column2.pack();
        column3.pack();
        column4.pack();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public Control getControl() {
        return table;
    }

    /**
     * Resets the view
     */
    @Override
    public void reset() {
        optimum = null;
        selectedNode = null;
        table.setRedraw(false);
        table.clearAll();
        table.setRedraw(true);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.target == EventTarget.SELECTED_NODE) {
            selectedNode = (ARXNode) event.data;
        } else if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
        } else if (event.target == EventTarget.FILTER) {
            if (model != null) {
                initialize(model.getResult(), (ModelNodeFilter) event.data);
            }
        }
    }

    /**
     * Converts an information loss into a relative value in percent
     * 
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss infoLoss) {
        return ((infoLoss.getValue() - model.getResult()
                                            .getLattice()
                                            .getBottom()
                                            .getMinimumInformationLoss()
                                            .getValue()) / model.getResult()
                                                                .getLattice()
                                                                .getTop()
                                                                .getMaximumInformationLoss()
                                                                .getValue()) * 100d;
    }

    private void createItem(final TableItem item, final int index) {

        final ARXNode node = list.get(index);

        final String transformation = Arrays.toString(node.getTransformation());
        item.setText(0, transformation);

        final String anonymity = node.isAnonymous().toString();
        item.setText(1, anonymity);

        String min = null;
        if (node.getMinimumInformationLoss() != null) {
            min = String.valueOf(node.getMinimumInformationLoss().getValue()) +
                  " [" + format.format(asRelativeValue(node.getMinimumInformationLoss())) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            min = Resources.getMessage("ListView.7"); //$NON-NLS-1$
        }
        item.setText(2, min);

        String max = null;
        if (node.getMaximumInformationLoss() != null) {
            max = String.valueOf(node.getMaximumInformationLoss().getValue()) +
                  " [" + format.format(asRelativeValue(node.getMaximumInformationLoss())) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            max = Resources.getMessage("ListView.10"); //$NON-NLS-1$
        }
        item.setText(3, max);
    }

    private void initialize(final ARXResult result, final ModelNodeFilter filter) {

        controller.getResources().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                table.setRedraw(false);
                table.clearAll();
                list.clear();

                final ARXLattice l = result.getLattice();
                optimum = result.getGlobalOptimum();
                for (final ARXNode[] level : l.getLevels()) {
                    for (final ARXNode node : level) {
                        if (filter.isAllowed(node)) {
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
                if (list.size() > model.getMaxNodesInViewer()) {
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
}
