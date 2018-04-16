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

import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.metric.MetricConfiguration;
import org.deidentifier.arx.metric.MetricDescription;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
public class ViewUtilityMeasures implements IView {

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
    public ViewUtilityMeasures(final Composite parent,
                      final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.METRIC, this);
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
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;

            // Ensure backwards compatibility with older project files
            Metric<?> metric = model.getInputConfig().getMetric();
            if (metric != null) {
                for (int i = 0; i < METRICS.size(); i++) {
                    if (METRICS.get(i).isInstance(metric)) {
                        comboMetric.select(i);
                        break;
                    }
                }
            }
            updateControls();
        } 
        
        if (event.part == ModelPart.INPUT || event.part == ModelPart.METRIC) {
            updateControls();
        }
    }

    /**
     * Builds the component
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
                    MetricDescription metric = METRICS.get(comboMetric.getSelectionIndex());
                    if (metric != null && model != null) {
                        model.setMetricDescription(metric);
                        controller.update(new ModelEvent(this, ModelPart.METRIC, model.getMetricDescription()));
                        updateControls();
                    }
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
                controller.update(new ModelEvent(this, ModelPart.METRIC, model.getMetricDescription()));
            }
        });

        // Create section about aggregate functions
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
                        controller.update(new ModelEvent(this, ModelPart.METRIC, model.getMetricDescription()));
                    }
                }
            }
        });

        return mBase;
    }

    /**
     * This method updates the view
     */
    private void updateControls(){

        // Check
        if (this.model == null) {
            return;
        }
        
        // Configure
        MetricConfiguration config = this.model.getMetricConfiguration();
        MetricDescription description = this.model.getMetricDescription();

        // Check
        if (config == null || description == null) {
            reset();
            return;
        }

        // Disable redrawing
        this.root.setRedraw(false);
        
        // Monotonicity
        if (!description.isMonotonicVariantSupported()) {
            this.monotonicVariant.setSelection(false);
        } else {
            this.monotonicVariant.setSelection(config.isMonotonic());
        }

        // Aggregate function
        this.comboAggregate.removeAll();
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
            this.comboAggregate.select(selected);
        }

        // Enable everything
        SWTUtil.enable(this.root);        

        // Disable some components
        if (this.comboAggregate.getItemCount() == 0) {
            this.comboAggregate.add(Resources.getMessage("ViewMetric.0")); //$NON-NLS-1$
            this.comboAggregate.select(0);
            this.comboAggregate.setEnabled(false);
        }
        this.monotonicVariant.setEnabled(description.isMonotonicVariantSupported());
        
        // Redraw
        this.root.setRedraw(true);
    }
}
