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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelRiskBasedCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;

/**
 * A view on a risk-based criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionRiskBased extends EditorCriterion<ModelRiskBasedCriterion>{

    /** View */
    private Knob<Double> knobThreshold;

    /** View */
    private Text         labelThreshold;

    /** View */
    private Combo        cmbModel;

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

        labelThreshold = createLabel(group);
        knobThreshold = createKnobDouble(group, 0d, 1d);
        updateLabel(labelThreshold, knobThreshold.getValue());
        knobThreshold.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setThreshold(knobThreshold.getValue());
                updateLabel(labelThreshold, model.getThreshold());
            }
        });
        
        if (isPopulation) {
            
            Label lblModel = new Label(group, SWT.NONE);
            lblModel.setText(Resources.getMessage("EditorCriterionRiskBased.0")); //$NON-NLS-1$
            
            cmbModel = new Combo(group, SWT.READ_ONLY);
            cmbModel.setItems(new String[]{Resources.getMessage("EditorCriterionRiskBased.1"), //$NON-NLS-1$
                                           Resources.getMessage("EditorCriterionRiskBased.2"), //$NON-NLS-1$
                                           Resources.getMessage("EditorCriterionRiskBased.3"), //$NON-NLS-1$
                                           Resources.getMessage("EditorCriterionRiskBased.4")}); //$NON-NLS-1$
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

    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR, 0.01d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR, 0.001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR, 0.0001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_PITMAN, 0.01d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_PITMAN, 0.001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_PITMAN, 0.0001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_ZAYATZ, 0.01d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_ZAYATZ, 0.001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_ZAYATZ, 0.0001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_SNB, 0.01d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_SNB, 0.001d));
        result.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_SNB, 0.0001d));
        return result;
    }


    /**
     * Parses the input
     */
    protected void parse(ModelRiskBasedCriterion model, boolean _default) {
        updateLabel(labelThreshold, model.getThreshold());
        knobThreshold.setValue(model.getThreshold());
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
