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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays the population settings
 * 
 * @author Fabian Prasser
 */
public class ViewRisksPopulationModel implements IView {

    /** Controller */
    private final Controller controller;
    
    /** View */
    private final Composite  root;
    /** View */
    private DynamicTable     table;
    /** View */
    private Text             textSampleFraction;
    /** View */
    private Text             textPopulationSize;
    /** View */
    private DecimalFormat    format = new DecimalFormat("0.########################################"); //$NON-NLS-1$
    /** View */
    private Button           buttonUse;
    
    /** Model */
    private Model            model;
    /** Model */
    private final boolean    output;


    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param output
     */
    public ViewRisksPopulationModel(final Composite parent,
                                    final Controller controller,
                                    final boolean output) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.RESULT, this);
        this.controller = controller;
        this.output = output;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
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
        table.select(0);
        textSampleFraction.setText(""); //$NON-NLS-1$
        textPopulationSize.setText(""); //$NON-NLS-1$
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
           update();
        } else if (event.part == ModelPart.INPUT ||
                   event.part == ModelPart.POPULATION_MODEL ||
                   event.part == ModelPart.OUTPUT ||
                   event.part == ModelPart.RESULT) {
           update();
        }
    }
    
    /**
     * Creates the required controls.
     * 
     * @param parent
     */
    private void create(final Composite parent) {
        
        buttonUse = new Button(parent, SWT.CHECK);
        buttonUse.setText(Resources.getMessage("ViewRisksPopulationModel.3")); //$NON-NLS-1$
        buttonUse.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());
        buttonUse.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                model.getRiskModel().setUseOutputPopulationModelIfAvailable(output ? buttonUse.getSelection()
                                                                                   : !buttonUse.getSelection());
                controller.update(new ModelEvent(controller, ModelPart.POPULATION_MODEL, null));
            }
        });
        
        Label lbl1 = new Label(parent, SWT.NONE);
        lbl1.setText(Resources.getMessage("ViewRisksPopulationModel.4")); //$NON-NLS-1$
        lbl1.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).create());
        
        table = SWTUtil.createTableDynamic(root, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(false);
        table.setLinesVisible(true);

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("100%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(""); //$NON-NLS-1$
        c.setResizable(false);
        
        for (Region region : Region.values()) {
            final TableItem item = new TableItem(table, SWT.NONE);
            item.setText(region.getName());
        }
        
        Label lbl2 = new Label(parent, SWT.NONE);
        lbl2.setText(Resources.getMessage("ViewRisksPopulationModel.5")); //$NON-NLS-1$
        
        textSampleFraction = new Text(parent, SWT.BORDER | SWT.SINGLE);
        textSampleFraction.setText("0"); //$NON-NLS-1$
        textSampleFraction.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        textSampleFraction.setEditable(false);
        
        Label lbl3 = new Label(parent, SWT.NONE);
        lbl3.setText(Resources.getMessage("ViewRisksPopulationModel.7")); //$NON-NLS-1$
        
        textPopulationSize = new Text(parent, SWT.BORDER | SWT.SINGLE);
        textPopulationSize.setText("0"); //$NON-NLS-1$
        textPopulationSize.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        textPopulationSize.setEditable(false);
        
        table.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.detail = SWT.NONE;
                event.type = SWT.None;
                event.doit = false;
                try
                {
                    table.setRedraw(false);
                    table.deselectAll();
                } finally {
                    table.setRedraw(true);
                    table.getParent().setFocus();
                }
            }
        });
    }

    /**
     * Is an output model available
     * @return
     */
    private boolean isOutputPopulationModelAvailable() {
        if (model == null || model.getOutputConfig() == null) { return false; }
        for (PopulationUniqueness t : model.getOutputConfig()
                                                          .getCriteria(PopulationUniqueness.class)) {
            if (t.getPopulationModel() != null) { return true; }
        }
        return false;
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        // Check
        if (model == null || model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null) { 
            return; 
        }
        
        root.setRedraw(false);
        SWTUtil.enable(root);
        
        boolean mayUseOutput = isOutputPopulationModelAvailable() && model.getRiskModel().isUseOutputPopulationModelIfAvailable();
        boolean enabled = output ? mayUseOutput : !mayUseOutput;
        this.buttonUse.setSelection(enabled);
        
        if (output && !isOutputPopulationModelAvailable()) {
            reset();
        } else {

            ARXPopulationModel popmodel = model.getInputPopulationModel();
            if (output && isOutputPopulationModelAvailable()) {
                popmodel = model.getOutputPopulationModel();
            }
            
            table.deselectAll();
            TableItem selected = null;
            for (TableItem item : table.getItems()) {
                if (item.getText().equals(popmodel.getRegion().getName())) {
                    item.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
                    selected = item;
                } else {
                    item.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                }
            }
            if (selected != null) {
                table.showItem(selected);
            }
            table.getParent().setFocus();
            DataHandle handle = model.getInputConfig().getInput().getHandle();
            textSampleFraction.setText(format.format(popmodel.getSamplingFraction(handle)));
            textSampleFraction.setEnabled(true);
            textPopulationSize.setText(format.format(popmodel.getPopulationSize(handle)));
            textPopulationSize.setEnabled(true);
        }
        root.setRedraw(true);
    }
}