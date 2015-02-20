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

package org.deidentifier.arx.gui.view.impl.risk;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.risk.RiskEstimator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.dynamictablecolumns.DynamicTable;
import org.mihalis.opal.dynamictablecolumns.DynamicTableColumn;

/**
 * This view displays information about the equivalence classes
 * 
 * @author Fabian Prasser
 */
public class ViewDistributionTable implements IView {

    /** Controller */
    private final Controller  controller;

    /** View */
    private final Composite   root;

    /** View */
    private DynamicTable      table;

    /** View */
    private List<TableItem>   items     = new ArrayList<TableItem>();

    /** View */
    private List<TableColumn> columns   = new ArrayList<TableColumn>();

    /** View */
    private DecimalFormat     format    = new DecimalFormat("##0.00000");

    /** Model */
    private RiskEstimator     estimator = null;

    /** Model */
    private Model             model     = null;

    /** Model */
    private Set<String>      qis                = new HashSet<String>();
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewDistributionTable(final Composite parent,
                                  final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
        this.controller = controller;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());

        create(root);
        reset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        qis.clear();
        table.setRedraw(false);
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();
        table.setRedraw(true);
        table.redraw();
        SWTUtil.disable(root);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui
     * .model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
        } else if (event.part == ModelPart.INPUT ||
                   event.part == ModelPart.ATTRIBUTE_TYPE) {
            if (model != null && model.getInputConfig() != null &&
                model.getInputConfig().getInput() != null) {
                if (!model.getInputDefinition().getQuasiIdentifyingAttributes().equals(qis)) {
                    qis = model.getInputDefinition().getQuasiIdentifyingAttributes();
                    estimator = model.getInputConfig()
                                     .getInput()
                                     .getHandle()
                                     .getRiskEstimator(model.getInputDefinition().getQuasiIdentifyingAttributes());
                    update();
                }
            } else {
                reset();
            }
        }
    }

    /**
     * Creates the required controls.
     * 
     * @param parent
     */
    private void create(final Composite parent) {

        table = new DynamicTable(parent, SWT.SINGLE | SWT.BORDER |
                                         SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        final GridData gdata = SWTUtil.createFillGridData();
        table.setLayoutData(gdata);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("33%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewSampleDistribution.1")); //$NON-NLS-1$
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("33%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewSampleDistribution.2")); //$NON-NLS-1$
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("33%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewSampleDistribution.3")); //$NON-NLS-1$
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
    private void update() {

        table.setRedraw(false);
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();

        // Sort descending by size
        int[][] distribution = estimator.getEquivalenceClassSizeDistribution();
        Arrays.sort(distribution, new Comparator<int[]>() {
            public int compare(int[] o1, int[] o2) {
                return Integer.compare(o1[0], o2[0]);
            }
        });

        // Create entries
        for (int i = 0; i < distribution.length; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, String.valueOf(distribution[i][0]));
            item.setText(1, String.valueOf(distribution[i][1]));
            item.setText(2, format.format((double) distribution[i][1] /
                                          (double) estimator.getNumRows() * 100d));
            items.add(item);
        }

        table.setRedraw(true);
        table.redraw();
        SWTUtil.enable(table);
    }
}
