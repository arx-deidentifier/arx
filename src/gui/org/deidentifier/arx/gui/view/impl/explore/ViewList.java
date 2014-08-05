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
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import cern.colt.Arrays;

/**
 * This class implements a list view on selected nodes.
 * TODO: Highlight optimum and currently selected node in list
 * 
 * @author prasser
 */
public class ViewList implements IView {

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
    public ViewList(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(ModelPart.SELECTED_NODE, this);
        controller.addListener(ModelPart.FILTER, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.RESULT, this);

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

    /**
     * Resets the view
     */
    @Override
    public void reset() {
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

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.RESULT) {
            if (model.getResult() == null) reset();
        } else  if (event.part == ModelPart.SELECTED_NODE) {
            // selectedNode = (ARXNode) event.data;
        } else if (event.part == ModelPart.MODEL) {
            reset();
            model = (Model) event.data;
            update(model.getResult(), model.getNodeFilter());
        } else if (event.part == ModelPart.FILTER) {
            if (model != null) {
                update(model.getResult(), (ModelNodeFilter) event.data);
            }
        }
    }

    /**
     * Converts an information loss into a relative value in percent
     * 
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss<?> infoLoss) {

        if (model != null && model.getResult() != null && model.getResult().getLattice() != null &&
            model.getResult().getLattice().getBottom() != null && model.getResult().getLattice().getTop() != null) {
            return infoLoss.relativeTo(model.getResult().getLattice().getMinimumInformationLoss(), 
                                       model.getResult().getLattice().getMaximumInformationLoss()) * 100d;
        } else {
            return 0;
        }
    }

    /**
     * Creates an item in the list
     * @param item
     * @param index
     */
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

    /**
     * Updates the list
     * @param result
     * @param filter
     */
    private void update(final ARXResult result, final ModelNodeFilter filter) {
        
        if (result == null || result.getLattice() == null) return;
        if (filter == null) return;
        
        controller.getResources().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                table.setRedraw(false);
                SWTUtil.enable(table);
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
