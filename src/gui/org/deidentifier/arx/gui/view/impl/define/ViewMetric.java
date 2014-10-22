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
import org.eclipse.swt.widgets.Scale;

/**
 * This view displays settings regarding the utility metrics
 * @author Fabian Prasser
 */
public class ViewMetric implements IView {

    /** Static settings*/
    private static final int                     LABEL_WIDTH  = 50;
    /** Static settings*/
    private static final int                     LABEL_HEIGHT = 20;

    /** Static settings*/
    private static final List<MetricDescription> METRICS      = Metric.list();
    /** Static settings */
    private static final String[]                LABELS       = getLabels(METRICS);

    /**
     * Returns a list of names of all available metrics
     * @param metrics
     * @return
     */
    private static String[] getLabels(List<MetricDescription> metrics) {
        String[] labels = new String[metrics.size()];
        for (int i=0; i<metrics.size(); i++) {
            labels[i] = metrics.get(i).getName();
        }
        return labels;
    }

    /** Controller */
    private final Controller      controller;
    /** Model */
    private Model                 model;
    /** View */
    private Combo                 comboMetric;
    /** View */
    private Composite             root;
    /** View */
    private ComponentTitledFolder folder;
    /** View */
    private IView                 viewCodingModel;
    /** View */
    private IView                 viewAttributeWeights;
    /** View */
    private Button                monotonicVariant;
    /** View */
    private Button                precomputedVariant;
    /** View */
    private Scale                 precomputationThreshold;
    /** View */
    private Label                 labelThreshold;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
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

    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void reset() {

        comboMetric.select(0);
        monotonicVariant.setSelection(false);
        precomputedVariant.setSelection(false);
        precomputationThreshold.setSelection(0);
        labelThreshold.setText("0");
        SWTUtil.disable(root);
    }

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

    private Composite build(final Composite parent) {

        final Composite mBase = new Composite(parent, SWT.NONE);
        mBase.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());

        // Create metric combo
        final Label mLabel = new Label(mBase, SWT.PUSH);
        mLabel.setText(Resources.getMessage("CriterionDefinitionView.32")); //$NON-NLS-1$
        GridData d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        mLabel.setLayoutData(d2);

        comboMetric = new Combo(mBase, SWT.READ_ONLY);
        GridData d30 = SWTUtil.createFillHorizontallyGridData();
        d30.verticalAlignment = SWT.CENTER;
        d30.horizontalSpan = 3;
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
        mLabel2.setLayoutData(d22);

        monotonicVariant = new Button(mBase, SWT.CHECK);
        monotonicVariant.setText(Resources.getMessage("CriterionDefinitionView.68")); //$NON-NLS-1$
        monotonicVariant.setSelection(false);
        monotonicVariant.setEnabled(false);
        monotonicVariant.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
        monotonicVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getMetricConfiguration().setMonotonic(monotonicVariant.getSelection());
                
                if (monotonicVariant.getSelection()) {
                    precomputationThreshold.setSelection(SWTUtil.doubleToSlider(0d, 1d, model.getMetricConfiguration().getGsFactor()));
                    precomputationThreshold.setEnabled(true);
                    labelThreshold.setText(String.valueOf(model.getMetricConfiguration().getGsFactor()));
                }
            }
        });

        // Create slider for precomputation threshold
        final Label sLabel = new Label(mBase, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.71")); //$NON-NLS-1$

        precomputedVariant = new Button(mBase, SWT.CHECK);
        precomputedVariant.setText(Resources.getMessage("CriterionDefinitionView.70")); //$NON-NLS-1$
        precomputedVariant.setSelection(false);
        precomputedVariant.setEnabled(false);
        precomputedVariant.setLayoutData(GridDataFactory.swtDefaults().span(1, 1).create());
        precomputedVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getMetricConfiguration().setPrecomputed(precomputedVariant.getSelection());
                if (precomputedVariant.getSelection()) {
                    precomputationThreshold.setSelection(SWTUtil.doubleToSlider(0d, 1d, model.getMetricConfiguration().getPrecomputationThreshold()));
                    precomputationThreshold.setEnabled(true);
                    labelThreshold.setText(String.valueOf((model.getMetricConfiguration().getPrecomputationThreshold())));
                } else {
                    precomputationThreshold.setSelection(0);
                    precomputationThreshold.setEnabled(false);
                    labelThreshold.setText(String.valueOf(0));
                }
            }
        });
        
        labelThreshold = new Label(mBase, SWT.BORDER | SWT.CENTER);
        GridData d24 = new GridData();
        d24.minimumWidth = LABEL_WIDTH;
        d24.widthHint = LABEL_WIDTH;
        d24.heightHint = LABEL_HEIGHT;
        labelThreshold.setLayoutData(d24);
        labelThreshold.setText("0"); //$NON-NLS-1$

        precomputationThreshold = new Scale(mBase, SWT.HORIZONTAL);
        precomputationThreshold.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        precomputationThreshold.setMaximum(SWTUtil.SLIDER_MAX);
        precomputationThreshold.setMinimum(0);
        precomputationThreshold.setSelection(0);
        precomputationThreshold.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getMetricConfiguration().setPrecomputationThreshold(SWTUtil.sliderToDouble(0d,
                                                            1d,
                                                            precomputationThreshold.getSelection()));
                labelThreshold.setText(String.valueOf(model.getMetricConfiguration()
                                                             .getPrecomputationThreshold()));
            }
        });
        
        return mBase;
    }

    /**
     * Hides the settings for the attribute weights
     */
    private void hideSettingsAttributeWeights(){

        if (this.viewAttributeWeights != null) {
            this.viewAttributeWeights.dispose();
            this.viewAttributeWeights = null;
            folder.disposeItem(Resources.getMessage("CriterionDefinitionView.63"));  //$NON-NLS-1$
        }
    }
    
    /**
     * Hides the settings for the coding model
     */
    private void hideSettingsCodingModel(){
        if (this.viewCodingModel != null) {
            this.viewCodingModel.dispose();
            this.viewCodingModel = null;
            folder.disposeItem(Resources.getMessage("CriterionDefinitionView.65"));  //$NON-NLS-1$
        }
    }

    /**
     * Select metric action
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
     * Shows the settings for the attribute weights
     */
    private void showSettingsAttributeWeights(){
        if (this.viewAttributeWeights != null) return;
        Composite composite1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.63"), null, folder.getItemCount()-1);  //$NON-NLS-1$
        composite1.setLayout(new FillLayout());
        this.viewAttributeWeights = new ViewAttributeWeights(composite1, controller);
        this.viewAttributeWeights.update(new ModelEvent(this, ModelPart.MODEL, this.model));
    }
  
    /**
     * Shows the settings for the coding model
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
     * according to the current state of the model
     */
    private void updateControlls(){

        root.setRedraw(false);
        
        if (model.getMetricConfiguration() != null && 
            model.getMetricDescription() != null) {
            
            // Monotonicity
            if (!model.getMetricDescription().isMonotonicVariantSupported()) {
                this.monotonicVariant.setSelection(false);
                this.monotonicVariant.setEnabled(false);
            } else {
                this.monotonicVariant.setEnabled(true);
                this.monotonicVariant.setSelection(model.getMetricConfiguration().isMonotonic());
            }
            
            // Precomputation
            if (!model.getMetricDescription().isPrecomputationSupported()) {
                this.precomputedVariant.setSelection(false);
                this.precomputedVariant.setEnabled(false);
                this.precomputationThreshold.setSelection(0);
                this.precomputationThreshold.setEnabled(false);
                this.labelThreshold.setText(String.valueOf(0d));
            } else {
                this.precomputedVariant.setEnabled(true);
                this.precomputedVariant.setSelection(model.getMetricConfiguration().isPrecomputed());
                if (model.getMetricConfiguration().isPrecomputed()) {
                    this.precomputationThreshold.setSelection(SWTUtil.doubleToSlider(0d, 1d, model.getMetricConfiguration().getGsFactor()));
                    this.precomputationThreshold.setEnabled(true);
                    this.labelThreshold.setText(String.valueOf(model.getMetricConfiguration().getGsFactor()));
                } else {
                    this.precomputationThreshold.setSelection(0);
                    this.precomputationThreshold.setEnabled(false);
                    this.labelThreshold.setText(String.valueOf(0d));
                }
            }
            
            // Weights
            if (model == null || model.getInputDefinition() == null || model.getInputConfig() == null ||
                model.getInputDefinition().getQuasiIdentifyingAttributes().isEmpty() ||
                model.getMetricDescription() == null ||
               !(model.getMetricDescription().isAttributeWeightsSupported())) {
                hideSettingsAttributeWeights();
                hideSettingsCodingModel();
            } else {
                showSettingsAttributeWeights();
                if (model.getMetricDescription().isConfigurableCodingModelSupported()) {
                    showSettingsCodingModel();
                } else {
                    hideSettingsCodingModel();
                }
            }
            
        } else {
            reset();
        }
        
        
        root.setRedraw(true);
    }
}
