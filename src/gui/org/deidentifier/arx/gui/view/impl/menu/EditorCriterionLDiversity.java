/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
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
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;

/**
 * A view on an l-diversity criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionLDiversity extends EditorCriterion<ModelLDiversityCriterion> {

    /**  View */
    private static final String VARIANTS[] = { Resources.getMessage("CriterionDefinitionView.6"), //$NON-NLS-1$ 
                                               Resources.getMessage("CriterionDefinitionView.7"), //$NON-NLS-1$ 
                                               Resources.getMessage("CriterionDefinitionView.8") };  //$NON-NLS-1$ 

    /** View */
    private Knob<Integer>       knobL;

    /** View */
    private Knob<Double>        knobC;

    /** View */
    private Combo               comboVariant;

    /** View */
    private Text                labelC;

    /** View */
    private Text                labelL;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionLDiversity(final Composite parent,
                                   final ModelLDiversityCriterion model) {
        super(parent, model);
    }

    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 8;
        group.setLayout(groupInputGridLayout);

        // Create l slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.27")); //$NON-NLS-1$

        labelL = createLabel(group);
        knobL = createKnobInteger(group, 2, 1000);
        updateLabel(labelL, knobL.getValue());
        knobL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setL(knobL.getValue());
                updateLabel(labelL, model.getL());
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.33")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        d31.horizontalSpan = 1;
        comboVariant.setLayoutData(d31);
        comboVariant.setItems(VARIANTS);
        comboVariant.select(0);

        // Create c slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.34")); //$NON-NLS-1$

        labelC = createLabel(group);
        knobC = createKnobDouble(group, 0.00001d, 1000d);
        updateLabel(labelC, knobC.getValue());
        knobC.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setC(knobC.getValue());
                updateLabel(labelC, model.getC());
            }
        });

        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setVariant(comboVariant.getSelectionIndex());
                if (model.getVariant() == 2) {
                    knobC.setEnabled(true);
                } else {
                    knobC.setEnabled(false);
                }
            }
        });

        return group;
    }

    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_DISTINCT, 2, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_DISTINCT, 4, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_DISTINCT, 6, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_DISTINCT, 8, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_DISTINCT, 10, 1.0E-5));
        
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_ENTROPY, 2, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_ENTROPY, 4, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_ENTROPY, 6, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_ENTROPY, 8, 1.0E-5));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_ENTROPY, 10, 1.0E-5));

        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 2, 3));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 4, 3));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 6, 3));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 8, 3));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 10, 3));

        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 2, 4));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 4, 4));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 6, 4));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 8, 4));
        result.add(new ModelLDiversityCriterion(this.model.getAttribute(), ModelLDiversityCriterion.VARIANT_RECURSIVE, 10, 4));
        
        return result;
    }


    @Override
    protected void parse(ModelLDiversityCriterion model, boolean _default) {
        
        updateLabel(labelC, model.getC());
        updateLabel(labelL, model.getL());
        knobL.setValue(model.getL());
        knobC.setValue(model.getC());

        comboVariant.select(model.getVariant());
        
        if (model.getVariant() == 2) {
            knobC.setEnabled(true);
        } else {
            knobC.setEnabled(false);
        }
    }
}
