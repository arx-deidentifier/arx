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
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
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
 * Implements a view on a t-closeness criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionTCloseness extends EditorCriterion<ModelTClosenessCriterion> {

    /**  View */
    private static final String VARIANTS[] = {Resources.getMessage("CriterionDefinitionView.9"), //$NON-NLS-1$
                                              Resources.getMessage("CriterionDefinitionView.10"), //$NON-NLS-1$
                                              Resources.getMessage("CriterionDefinitionView.102")}; //$NON-NLS-1$

    /** View */
    private Knob<Double>        knobT;

    /** View */
    private Combo               comboVariant;

    /** View */
    private Text                labelT;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionTCloseness(final Composite parent,
                                     final ModelTClosenessCriterion model) {
        super(parent, model);
    }

    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 5;
        group.setLayout(groupInputGridLayout);

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.42")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d32 = SWTUtil.createFillHorizontallyGridData();
        d32.verticalAlignment = SWT.CENTER;
        d32.horizontalSpan = 1;
        comboVariant.setLayoutData(d32);
        comboVariant.setItems(VARIANTS);
        comboVariant.select(0);
        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setVariant(comboVariant.getSelectionIndex());
            }
        });

        // Create t slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.43")); //$NON-NLS-1$

        labelT = createLabel(group);
        knobT = createKnobDouble(group, 0.000001d, 1d);
        updateLabel(labelT, knobT.getValue());
        knobT.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setT(knobT.getValue());
                updateLabel(labelT, model.getT());
            }
        });

        return group;
    }
    
    @Override
    protected List<ModelCriterion> getTypicalParameters() {
        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelTClosenessCriterion(this.model.getAttribute(), ModelTClosenessCriterion.VARIANT_EQUAL, 0.15));
        result.add(new ModelTClosenessCriterion(this.model.getAttribute(), ModelTClosenessCriterion.VARIANT_EQUAL, 0.2));
        result.add(new ModelTClosenessCriterion(this.model.getAttribute(), ModelTClosenessCriterion.VARIANT_HIERARCHICAL, 0.15));
        result.add(new ModelTClosenessCriterion(this.model.getAttribute(), ModelTClosenessCriterion.VARIANT_HIERARCHICAL, 0.2));
        result.add(new ModelTClosenessCriterion(this.model.getAttribute(), ModelTClosenessCriterion.VARIANT_ORDERED, 0.15));
        result.add(new ModelTClosenessCriterion(this.model.getAttribute(), ModelTClosenessCriterion.VARIANT_ORDERED, 0.2));
        return result;
    }

    @Override
    protected void parse(ModelTClosenessCriterion model, boolean _default) {
        knobT.setValue(model.getT());
        updateLabel(labelT, model.getT());
        comboVariant.select(model.getVariant());
    }
}
