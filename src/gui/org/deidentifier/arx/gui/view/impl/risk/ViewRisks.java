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
public class ViewRisks implements IView {

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
    public ViewRisks(final Composite parent,
                                  final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
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
        } else if (event.part == ModelPart.INPUT || event.part == ModelPart.POPULATION_MODEL ||
                   event.part == ModelPart.ATTRIBUTE_TYPE) {
            if (model != null && model.getInputConfig() != null &&
                model.getInputConfig().getInput() != null) {
                if (!model.getInputDefinition().getQuasiIdentifyingAttributes().equals(qis) || 
                     event.part == ModelPart.POPULATION_MODEL ) {
                    qis = model.getInputDefinition().getQuasiIdentifyingAttributes();
                    estimator = model.getInputConfig()
                                     .getInput()
                                     .getHandle()
                                     .getRiskEstimator(model.getInputDefinition().getQuasiIdentifyingAttributes(), model.getPopulationModel().getSampleFraction());
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

        table = new DynamicTable(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewSampleDistribution.6")); //$NON-NLS-1$
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewSampleDistribution.7")); //$NON-NLS-1$
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
        
        createItem(Resources.getMessage("ViewSampleDistribution.14"), 1.d / estimator.getMaximalClassSize());
        createItem(Resources.getMessage("ViewSampleDistribution.8"), estimator.getEquivalenceClassRisk());
        createItem(Resources.getMessage("ViewSampleDistribution.9"), estimator.getHighestIndividualRisk());
        createItem(Resources.getMessage("ViewSampleDistribution.10"), estimator.getHighestRiskAffected() / (double)estimator.getNumRows());
        createItem(Resources.getMessage("ViewSampleDistribution.11"), estimator.getSampleUniquesRisk());
        
        if (estimator.getSampleUniquesRisk() != 0d) {
            createItem(Resources.getMessage("ViewSampleDistribution.12"), estimator.getPopulationUniquesRisk());
        } else {
            createItem(Resources.getMessage("ViewSampleDistribution.12"), "N/A");
        }
        
        table.setRedraw(true);
        table.redraw();
        SWTUtil.enable(root);
    }
    
    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(String label, double value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label);
        item.setText(1, format.format(value * 100d));
        items.add(item);
    }

    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(String label, String value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label);
        item.setText(1, value);
        items.add(item);
    }
}
