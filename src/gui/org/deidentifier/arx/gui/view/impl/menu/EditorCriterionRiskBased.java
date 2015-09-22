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

package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.gui.model.ModelRiskBasedCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * A view on a risk-based criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionRiskBased extends EditorCriterion<ModelRiskBasedCriterion>{

    /** View */
    private Scale sliderThreshold;

    /** View */
    private Label labelThreshold;

    /** View */
    private Combo cmbModel;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionRiskBased(final Composite parent, 
                                    final ModelRiskBasedCriterion model) {
        super(parent, model);
    }

    /**
     * Updates the "threshold" label and tool tip text.
     *
     * @param text
     */
    private void updateThresholdLabel(String text) {
        labelThreshold.setText(text);
        labelThreshold.setToolTipText(text);
    }
    
    /**
     * Build
     * @param parent
     * @return
     */
    protected Composite build(Composite parent) {
        boolean isPopulation = model.getVariant() == ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR ||
                               model.getVariant() == ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_PITMAN ||
                               model.getVariant() == ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_ZAYATZ ||
                               model.getVariant() == ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_SNB;

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = isPopulation ? 5 : 3;
        group.setLayout(groupInputGridLayout);

        // Create threshold slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.81")); //$NON-NLS-1$

        labelThreshold = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelThreshold.setLayoutData(d9);
        updateThresholdLabel("0"); //$NON-NLS-1$

        sliderThreshold = new Scale(group, SWT.HORIZONTAL);
        sliderThreshold.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        sliderThreshold.setMaximum(SWTUtil.SLIDER_MAX);
        sliderThreshold.setMinimum(0);
        sliderThreshold.setSelection(0);
        sliderThreshold.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setThreshold(SWTUtil.sliderToDouble(0, 1, sliderThreshold.getSelection()));
                String dmin = String.valueOf(model.getThreshold());
                updateThresholdLabel(dmin);
            }
        });
        
        if (isPopulation) {
            
            Label lblModel = new Label(group, SWT.NONE);
            lblModel.setText("Model:");
            
            cmbModel = new Combo(group, SWT.READ_ONLY);
            cmbModel.setItems(new String[]{"Dankar", "Pitman", "Zayatz", "SNB"});
            cmbModel.select(0);
            cmbModel.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent arg0) {
                    switch(cmbModel.getSelectionIndex()) {
                        case 0:
                            model.setVariant(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR);
                            break;
                        case 1:
                            model.setVariant(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_PITMAN);
                            break;
                        case 2:
                            model.setVariant(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_ZAYATZ);
                            break;
                        case 3:
                            model.setVariant(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_SNB);
                            break;
                    }
                }
            });
        }

        return group;
    }

    /**
     * Parses the input
     */
    protected void parse(ModelRiskBasedCriterion model) {
        updateThresholdLabel(String.valueOf(model.getThreshold()));
        sliderThreshold.setSelection(SWTUtil.doubleToSlider(0, 1d, model.getThreshold()));
        switch(model.getVariant()) {
            case ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR:
                cmbModel.select(0);
                break;
            case ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_PITMAN:
                cmbModel.select(1);
                break;
            case ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_ZAYATZ:
                cmbModel.select(2);
                break;
            case ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_SNB:
                cmbModel.select(3);
                break;
        }
    }
}
