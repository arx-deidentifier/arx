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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.FLASHResult;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.flash.metric.InformationLoss;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class NodePropertiesView implements IView {

    private static final String     TITLE   = Resources.getMessage("NodePropertiesView.0"); //$NON-NLS-1$
    private FLASHResult             result;
    private Table                   table;
    private final List<TableColumn> columns = new ArrayList<TableColumn>();
    private final List<TableItem>   items   = new ArrayList<TableItem>();
    private final NumberFormat      format  = new DecimalFormat("##0.000");                //$NON-NLS-1$
    private final Group             root;
    private final Controller        controller;

    public NodePropertiesView(final Composite parent,
                              final Controller controller) {

        controller.addListener(EventTarget.RESULT, this);
        controller.addListener(EventTarget.SELECTED_NODE, this);
        this.controller = controller;

        // Create group
        root = new Group(parent, SWT.NULL);
        root.setText(TITLE);
        root.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupNodeGridLayout = new GridLayout();
        groupNodeGridLayout.numColumns = 1;
        root.setLayout(groupNodeGridLayout);

        createNodeGroup(root);
        reset();
    }

    /**
     * Converts an information loss into a relative value in percent
     * 
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss infoLoss) {
        return ((infoLoss.getValue() - result.getLattice()
                                             .getBottom()
                                             .getMinimumInformationLoss()
                                             .getValue()) / result.getLattice()
                                                                  .getTop()
                                                                  .getMaximumInformationLoss()
                                                                  .getValue()) * 100d;
    }

    private void createNodeGroup(final Group groupNode) {

        table = new Table(groupNode, SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        final GridData gdata = SWTUtil.createFillGridData();
        // gdata.heightHint = (NUM_PROPERTIES * table.getItemHeight())
        // + table.getHeaderHeight() + (2 * table.getBorderWidth());
        table.setLayoutData(gdata);

        TableColumn c = new TableColumn(table, SWT.NONE);
        c.setText(Resources.getMessage("NodePropertiesView.16")); //$NON-NLS-1$
        columns.add(c);
        c = new TableColumn(table, SWT.NONE);
        c.setText(Resources.getMessage("NodePropertiesView.17")); //$NON-NLS-1$
        columns.add(c);
        for (final TableColumn col : columns) {
            col.pack();
        }
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        table.setRedraw(false);
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();

        TableItem c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.2")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.4")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.6")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.8")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.10")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.12")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.14")); //$NON-NLS-1$
        c.setText(1, ""); //$NON-NLS-1$
        items.add(c);
        for (final TableColumn col : columns) {
            col.pack();
        }
        table.setRedraw(true);
        table.redraw();
        SWTUtil.disable(root);
    }

    private void setSelectedNode(final FLASHNode node) {
        // TODO: Implement unknown
        table.setRedraw(false);
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();
        TableItem c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.18")); //$NON-NLS-1$
        c.setText(1, String.valueOf(node.isAnonymous()));
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.19")); //$NON-NLS-1$
        if (node.getMinimumInformationLoss() != null) {
            c.setText(1,
                      String.valueOf(node.getMinimumInformationLoss()
                                         .getValue()) +
                              " [" + format.format(asRelativeValue(node.getMinimumInformationLoss())) + "%]"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            c.setText(1, Resources.getMessage("NodePropertiesView.22")); //$NON-NLS-1$
        }
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.23")); //$NON-NLS-1$
        if (node.getMaximumInformationLoss() != null) {
            c.setText(1,
                      String.valueOf(node.getMaximumInformationLoss()
                                         .getValue()) +
                              " [" + format.format(asRelativeValue(node.getMaximumInformationLoss())) + "%]"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            c.setText(1, Resources.getMessage("NodePropertiesView.26")); //$NON-NLS-1$
        }
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.27")); //$NON-NLS-1$
        c.setText(1, String.valueOf(node.getSuccessors().length));
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.28")); //$NON-NLS-1$
        c.setText(1, String.valueOf(node.getPredecessors().length));
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.29")); //$NON-NLS-1$
        c.setText(1, Arrays.toString(node.getTransformation()));
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.30")); //$NON-NLS-1$
        c.setText(1, String.valueOf(node.isChecked()));
        items.add(c);
        for (final TableColumn col : columns) {
            col.pack();
        }
        table.setRedraw(true);
        table.redraw();
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.target == EventTarget.RESULT) {
            result = (FLASHResult) event.data;
            reset();
        } else if (event.target == EventTarget.SELECTED_NODE) {

            // No result available
            if (event.data == null) {
                reset();
            } else {
                setSelectedNode((FLASHNode) event.data);
                SWTUtil.enable(root);
            }
        }
    }
}
