/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * This view displays general settings regarding data transformation.
 *
 * @author Fabian Prasser
 */
public class ViewTransformationSettings implements IView {

    /** Static settings. */
    private static final int      LABEL_WIDTH  = 50;
    
    /** Static settings. */
    private static final int      LABEL_HEIGHT = 20;

    /** Controller. */
    private final Controller      controller;
    
    /** Model. */
    private Model                 model;

    /** View */
    private Scale                 sliderOutliers;
    
    /** View. */
    private Label                 labelOutliers;
    
    /** View. */
    private Button                buttonPracticalMonotonicity;
    
    /** View. */
    private Composite             root;
    
    /** View. */
    private Button                precomputedVariant;
    
    /** View. */
    private Scale                 precomputationThreshold;
    
    /** View. */
    private Label                 labelThreshold;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewTransformationSettings(final Composite parent,
                                      final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.METRIC, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.root = build(parent);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void reset() {
        precomputedVariant.setSelection(false);
        precomputationThreshold.setSelection(0);
        sliderOutliers.setSelection(0);
        labelOutliers.setText("0%"); //$NON-NLS-1
        labelThreshold.setText("0%"); //$NON-NLS-1
        buttonPracticalMonotonicity.setSelection(false);
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            updateControls();            
        } else if (event.part == ModelPart.INPUT || event.part == ModelPart.METRIC) {
            updateControls();
        } 
    }

    /**
     * Builds this view
     * 
     * @param parent
     * @return
     */
    private Composite build(final Composite parent) {

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayout(SWTUtil.createGridLayout(4, false));

        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.11")); //$NON-NLS-1$

        Composite outliersBase = new Composite(group, SWT.NONE);
        GridData baseData = SWTUtil.createFillHorizontallyGridData();
        baseData.horizontalSpan = 3;
        outliersBase.setLayoutData(baseData);
        outliersBase.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        
        labelOutliers = new Label(outliersBase, SWT.BORDER | SWT.CENTER);
        GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        d2.heightHint = LABEL_HEIGHT;
        labelOutliers.setLayoutData(d2);
        labelOutliers.setText("0%"); //$NON-NLS-1$

        sliderOutliers = new Scale(outliersBase, SWT.HORIZONTAL);
        sliderOutliers.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        sliderOutliers.setMaximum(SWTUtil.SLIDER_MAX);
        sliderOutliers.setMinimum(0);
        sliderOutliers.setSelection(0);
        sliderOutliers.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig().setSuppressionLimit(SWTUtil.sliderToDouble(0d, 1d, sliderOutliers.getSelection()));
                labelOutliers.setText(SWTUtil.getPrettyString(model.getInputConfig().getSuppressionLimit()*100d)+"%");
                if (model.getInputConfig().getSuppressionLimit() != 0) {
                    buttonPracticalMonotonicity.setEnabled(true);
                } else {
                    buttonPracticalMonotonicity.setSelection(false);
                    buttonPracticalMonotonicity.setEnabled(false);
                    model.getInputConfig().setPracticalMonotonicity(false);
                }
            }
        });

        // Build approximate button
        final Label m2Label = new Label(group, SWT.PUSH);
        m2Label.setText(Resources.getMessage("CriterionDefinitionView.31")); //$NON-NLS-1$
        d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        m2Label.setLayoutData(d2);

        final GridData d82 = SWTUtil.createFillHorizontallyGridData();
        d82.horizontalSpan = 3;
        buttonPracticalMonotonicity = new Button(group, SWT.CHECK);
        buttonPracticalMonotonicity.setText(Resources.getMessage("CriterionDefinitionView.53")); //$NON-NLS-1$
        buttonPracticalMonotonicity.setSelection(false);
        buttonPracticalMonotonicity.setEnabled(false);
        buttonPracticalMonotonicity.setLayoutData(d82);
        buttonPracticalMonotonicity.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setPracticalMonotonicity(buttonPracticalMonotonicity.getSelection());
            }
        });
        

        // Create slider for precomputation threshold
        final Label sLabel2 = new Label(group, SWT.PUSH);
        sLabel2.setText(Resources.getMessage("CriterionDefinitionView.71")); //$NON-NLS-1$

        precomputedVariant = new Button(group, SWT.CHECK);
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
                    labelThreshold.setText(SWTUtil.getPrettyString((model.getMetricConfiguration().getPrecomputationThreshold()*100d))+"%");
                } else {
                    precomputationThreshold.setEnabled(false);
                }
                controller.update(new ModelEvent(this, ModelPart.METRIC, model.getMetricDescription()));
            }
        });
        
        labelThreshold = new Label(group, SWT.BORDER | SWT.CENTER);
        GridData d24 = new GridData();
        d24.minimumWidth = LABEL_WIDTH;
        d24.widthHint = LABEL_WIDTH;
        d24.heightHint = LABEL_HEIGHT;
        labelThreshold.setLayoutData(d24);
        labelThreshold.setText("0%"); //$NON-NLS-1$

        precomputationThreshold = new Scale(group, SWT.HORIZONTAL);
        precomputationThreshold.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        precomputationThreshold.setMaximum(SWTUtil.SLIDER_MAX);
        precomputationThreshold.setMinimum(0);
        precomputationThreshold.setSelection(0);
        precomputationThreshold.setEnabled(false);
        precomputationThreshold.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getMetricConfiguration().setPrecomputationThreshold(SWTUtil.sliderToDouble(0d, 1d, precomputationThreshold.getSelection()));
                labelThreshold.setText(SWTUtil.getPrettyString(model.getMetricConfiguration().getPrecomputationThreshold()*100d)+"%");
            }
        });

        return group;
    }
    
    /**
     * This method updates the controls
     */
    private void updateControls(){
        
        // Disable
        root.setRedraw(false);
        
        // Metric-related stuff
        double threshold = model.getMetricConfiguration().getPrecomputationThreshold();
        boolean supported = model.getMetricDescription().isPrecomputationSupported();
        boolean precomputed = model.getMetricConfiguration().isPrecomputed();
        this.precomputedVariant.setSelection(precomputed);
        this.precomputedVariant.setEnabled(supported);
        this.precomputationThreshold.setSelection(SWTUtil.doubleToSlider(0d, 1d, threshold));
        this.precomputationThreshold.setEnabled(supported);
        this.labelThreshold.setText(SWTUtil.getPrettyString(threshold*100d)+"%");
        
        // Other stuff
        sliderOutliers.setSelection(SWTUtil.doubleToSlider(0d, 0.999d, model.getInputConfig().getSuppressionLimit()));
        labelOutliers.setText(SWTUtil.getPrettyString(model.getInputConfig().getSuppressionLimit()*100d)+"%");
        buttonPracticalMonotonicity.setSelection(model.getInputConfig().isPracticalMonotonicity());
        
        // Enable
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
}
