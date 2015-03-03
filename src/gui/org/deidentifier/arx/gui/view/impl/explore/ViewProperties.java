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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledBorder;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This view displays properties about the currently selected transformation.
 *
 * @author Fabian Prasser
 */
public class ViewProperties implements IView {

    /**  TODO */
    private final List<TableColumn> columns = new ArrayList<TableColumn>();
    
    /**  TODO */
    private final Controller        controller;
    
    /**  TODO */
    private final NumberFormat      format  = new DecimalFormat("##0.000"); //$NON-NLS-1$
    
    /**  TODO */
    private final List<TableItem>   items   = new ArrayList<TableItem>();
    
    /**  TODO */
    private ARXResult               result;
    
    /**  TODO */
    private final Composite         root;
    
    /**  TODO */
    private Table                   table;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewProperties(final Composite parent,
                          final Controller controller) {

        controller.addListener(ModelPart.RESULT, this);
        controller.addListener(ModelPart.SELECTED_NODE, this);
        this.controller = controller;

        // Create group
        ComponentTitledBorder border = new ComponentTitledBorder(parent, controller, Resources.getMessage("NodePropertiesView.0"), "id-22"); //$NON-NLS-1$
        root = new Composite(border.getControl(), SWT.NONE);
        border.setChild(root);
        border.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupNodeGridLayout = new GridLayout();
        groupNodeGridLayout.numColumns = 1;
        root.setLayout(groupNodeGridLayout);

        create(root);
        reset();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.RESULT) {
            result = (ARXResult) event.data;
            reset();
        } else if (event.part == ModelPart.SELECTED_NODE) {
            if (event.data == null) {
                reset();
            } else {
                update((ARXNode) event.data);
                SWTUtil.enable(root);
            }
        }
    }

    /**
     * Converts an information loss into a relative value in percent.
     *
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss<?> infoLoss) {
        if (result == null) return 0;
        return infoLoss.relativeTo(result.getLattice().getMinimumInformationLoss(), 
                                   result.getLattice().getMaximumInformationLoss()) * 100d;
    }

    /**
     * Creates the required controls.
     *
     * @param parent
     */
    private void create(final Composite parent) {

        table = new Table(parent, SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        final GridData gdata = SWTUtil.createFillGridData();
        table.setLayoutData(gdata);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

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

    /**
     * Updates the view.
     *
     * @param node
     */
    private void update(final ARXNode node) {
        // TODO: Implement unknown
        table.setRedraw(false);
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();
        TableItem c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.18")); //$NON-NLS-1$
        c.setText(1, String.valueOf(node.getAnonymity()));
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.19")); //$NON-NLS-1$
        if (node.getMinimumInformationLoss() != null) {
            c.setText(1, node.getMinimumInformationLoss().toString() +
                         " [" + format.format(asRelativeValue(node.getMinimumInformationLoss())) + "%]"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            c.setText(1, Resources.getMessage("NodePropertiesView.22")); //$NON-NLS-1$
        }
        items.add(c);
        c = new TableItem(table, SWT.NONE);
        c.setText(0, Resources.getMessage("NodePropertiesView.23")); //$NON-NLS-1$
        if (node.getMaximumInformationLoss() != null) {
            c.setText(1, node.getMaximumInformationLoss().toString() +
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
}