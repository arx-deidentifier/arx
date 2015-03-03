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

package org.deidentifier.arx.gui.view.impl.define;

import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.metric.MetricConfiguration;
import org.deidentifier.arx.metric.MetricDescription;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This view displays settings regarding the utility metrics.
 *
 * @author Fabian Prasser
 */
public class ViewMetric implements IView {

    /** Static settings. */
    private static final int                     LABEL_HEIGHT = 20;

    /** Static settings. */
    private static final List<MetricDescription> METRICS      = Metric.list();
    
    /** Static settings. */
    private static final String[]                LABELS       = getLabels(METRICS);

    /**
     * Returns a list of names of all available metrics.
     *
     * @param metrics
     * @return
     */
    private static String[] getLabels(List<MetricDescription> metrics) {
        String[] labels = new String[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            labels[i] = metrics.get(i).getName();
        }
        return labels;
    }

    /** Controller. */
    private final Controller      controller;
    
    /** Model. */
    private Model                 model;
    
    /** View. */
    private Combo                 comboMetric;
    
    /** View. */
    private Composite             root;
    
    /** View. */
    private ComponentTitledFolder folder;
    
    /** View. */
    private IView                 viewCodingModel;
    
    /** View. */
    private IView                 viewAttributeWeights;
    
    /** View. */
    private Button                monotonicVariant;
    
    /** View. */
    private Combo                 comboAggregate;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param folder
     */
    public ViewMetric(final Composite parent,
                      final Controller controller,
                      final ComponentTitledFolder folder) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.METRIC, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.folder = folder;
        this.root = build(parent);
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

        comboMetric.select(0);
        monotonicVariant.setSelection(false);
        SWTUtil.disable(root);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            root.setRedraw(false);
            
            boolean found = false;
            for (int i = 0; i < METRICS.size(); i++) {
                if (METRICS.get(i).isInstance(model.getInputConfig().getMetric())) {
                    comboMetric.select(i);
                    found = true;
                    break;
                }
            }
            
            // Sanity check for tracking potential bugs introduced by ARX 2.3
            if (!found) {
                controller.actionShowErrorDialog(this.root.getShell(), "Metric not found", new RuntimeException(model.getInputConfig().getMetric().getClass().getSimpleName()));
            }
            
            updateControlls();
            root.setRedraw(true);
        } else if (event.part == ModelPart.INPUT) {
            SWTUtil.enable(root);
            updateControlls();
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.CRITERION_DEFINITION ||
                   event.part == ModelPart.METRIC) {
            
            if (model != null){
                updateControlls();
            }
        }
    }

    /**
     * 
     *
     * @param parent
     * @return
     */
    private Composite build(final Composite parent) {

        final Composite mBase = new Composite(parent, SWT.NONE);
        mBase.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());

        // Create metric combo
        final Label mLabel = new Label(mBase, SWT.PUSH);
        mLabel.setText(Resources.getMessage("CriterionDefinitionView.32")); //$NON-NLS-1$
        GridData d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        d2.grabExcessVerticalSpace = true;
        d2.verticalAlignment = GridData.CENTER;
        mLabel.setLayoutData(d2);

        comboMetric = new Combo(mBase, SWT.READ_ONLY);
        GridData d30 = SWTUtil.createFillHorizontallyGridData();
        d30.horizontalSpan = 3;
        d30.verticalAlignment = GridData.CENTER;
        d30.grabExcessVerticalSpace = true;
        comboMetric.setLayoutData(d30);
        comboMetric.setItems(LABELS);
        comboMetric.select(0);
        comboMetric.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (comboMetric.getSelectionIndex() != -1) {
                    selectMetricAction(METRICS.get(comboMetric.getSelectionIndex()));
                }
            }
        });

        // Create monotonicity button
        final Label mLabel2 = new Label(mBase, SWT.PUSH);
        mLabel2.setText(Resources.getMessage("CriterionDefinitionView.67")); //$NON-NLS-1$
        GridData d22 = new GridData();
        d22.heightHint = LABEL_HEIGHT;
        d22.minimumHeight = LABEL_HEIGHT;
        d22.grabExcessVerticalSpace = true;
        d22.verticalAlignment = GridData.CENTER;
        mLabel2.setLayoutData(d22);

        monotonicVariant = new Button(mBase, SWT.CHECK);
        monotonicVariant.setText(Resources.getMessage("CriterionDefinitionView.68")); //$NON-NLS-1$
        monotonicVariant.setSelection(false);
        monotonicVariant.setEnabled(false);
        monotonicVariant.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).grab(false, true).align(GridData.BEGINNING, GridData.CENTER).create());
        monotonicVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getMetricConfiguration().setMonotonic(monotonicVariant.getSelection());
            }
        });

        // Create monotonicity button
        final Label mLabel3 = new Label(mBase, SWT.PUSH);
        mLabel3.setText(Resources.getMessage("CriterionDefinitionView.72")); //$NON-NLS-1$
        GridData d23 = new GridData();
        d23.heightHint = LABEL_HEIGHT;
        d23.minimumHeight = LABEL_HEIGHT;
        d23.grabExcessVerticalSpace = true;
        d23.verticalAlignment = GridData.CENTER;
        mLabel3.setLayoutData(d23);

        comboAggregate = new Combo(mBase, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.horizontalSpan = 3;
        d31.grabExcessVerticalSpace = true;
        d31.verticalAlignment = GridData.CENTER;
        comboAggregate.setLayoutData(d31);
        comboAggregate.setEnabled(false);
        comboAggregate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                String selected = comboAggregate.getItem(comboAggregate.getSelectionIndex());
                for (AggregateFunction function : model.getMetricDescription().getSupportedAggregateFunctions()) {
                    if (function.toString().equals(selected)) {
                        model.getMetricConfiguration().setAggregateFunction(function);
                    }
                }
            }
        });

        return mBase;
    }

    /**
     * Hides the settings for the attribute weights.
     */
    private void hideSettingsAttributeWeights(){

        if (this.viewAttributeWeights != null) {
            this.viewAttributeWeights.dispose();
            this.viewAttributeWeights = null;
            folder.disposeItem(Resources.getMessage("CriterionDefinitionView.63"));  //$NON-NLS-1$
        }
    }
    
    /**
     * Hides the settings for the coding model.
     */
    private void hideSettingsCodingModel(){
        if (this.viewCodingModel != null) {
            this.viewCodingModel.dispose();
            this.viewCodingModel = null;
            folder.disposeItem(Resources.getMessage("CriterionDefinitionView.65"));  //$NON-NLS-1$
        }
    }

    /**
     * Select metric action.
     *
     * @param metric
     */
    private void selectMetricAction(final MetricDescription metric) {
        if (model == null) { return; }
        if (metric != null) {
            
            model.setMetricDescription(metric);
            
            if (metric.isConfigurableCodingModelSupported()) {
                this.showSettingsCodingModel();
            } else {
                this.hideSettingsCodingModel();
            }

            if (metric.isAttributeWeightsSupported() && 
                 model != null &&
                 model.getInputDefinition() != null &&
                 model.getInputDefinition().getQuasiIdentifyingAttributes() != null &&
                 !model.getInputDefinition().getQuasiIdentifyingAttributes().isEmpty()){
                this.showSettingsAttributeWeights();
            } else {
                this.hideSettingsAttributeWeights();
            }
            
            this.updateControlls();
        }
    }
    
    /**
     * Shows the settings for the attribute weights.
     */
    private void showSettingsAttributeWeights(){
        if (this.viewAttributeWeights != null) return;
        Composite composite1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.63"), null, folder.getItemCount()-1);  //$NON-NLS-1$
        composite1.setLayout(new FillLayout());
        this.viewAttributeWeights = new ViewAttributeWeights(composite1, controller);
        this.viewAttributeWeights.update(new ModelEvent(this, ModelPart.MODEL, this.model));
    }
  
    /**
     * Shows the settings for the coding model.
     */
    private void showSettingsCodingModel(){
        if (this.viewCodingModel != null) return;
        Composite composite2 = folder.createItem(Resources.getMessage("CriterionDefinitionView.65"), null, folder.getItemCount()-1);  //$NON-NLS-1$
        composite2.setLayout(new FillLayout());
        this.viewCodingModel = new ViewCodingModel(composite2, controller);
        this.viewCodingModel.update(new ModelEvent(this, ModelPart.MODEL, this.model));
    }

    /**
     * This method adjusts the toolbar attached to the folder with criteria
     * according to the current state of the model.
     */
    private void updateControlls(){

        root.setRedraw(false);
        
        MetricConfiguration config = model.getMetricConfiguration();
        MetricDescription description = model.getMetricDescription();
        
        if (config != null && 
            description != null) {
            
            // Monotonicity
            if (!description.isMonotonicVariantSupported()) {
                this.monotonicVariant.setSelection(false);
                this.monotonicVariant.setEnabled(false);
            } else {
                this.monotonicVariant.setEnabled(true);
                this.monotonicVariant.setSelection(config.isMonotonic());
            }
            
            // Weights
            if (model == null || model.getInputDefinition() == null || model.getInputConfig() == null ||
                model.getInputDefinition().getQuasiIdentifyingAttributes().isEmpty() ||
               !description.isAttributeWeightsSupported()) {
                hideSettingsAttributeWeights();
                hideSettingsCodingModel();
            } else {
                showSettingsAttributeWeights();
                if (description.isConfigurableCodingModelSupported()) {
                    showSettingsCodingModel();
                } else {
                    hideSettingsCodingModel();
                }
            }
            
            // Aggregate function
            comboAggregate.removeAll();
            int index = 0;
            int selected = -1;
            for (AggregateFunction function : description.getSupportedAggregateFunctions()) {
                comboAggregate.add(function.toString());
                if (function.toString().equals(config.getAggregateFunction().toString())) {
                    selected = index;
                }
                index++;
            }
            if (selected != -1) {
                comboAggregate.select(selected);
            }

            if (comboAggregate.getItemCount()==0) {
                comboAggregate.add("None available");
                comboAggregate.select(0);
            }
            
        } else {
            reset();
        }
        
        root.setRedraw(true);
    }
}
