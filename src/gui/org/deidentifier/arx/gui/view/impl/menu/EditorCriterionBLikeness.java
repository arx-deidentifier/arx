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

import org.deidentifier.arx.gui.model.ModelBLikenessCriterion;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;

/**
 * Implements a view on a b-likeness privacy model
 *
 * @author Fabian Prasser
 */
public class EditorCriterionBLikeness extends EditorCriterion<ModelBLikenessCriterion> {

    /** View */
    private Knob<Double> knobB;

    /** View */
    private Button       btnEnhanced;

    /** View */
    private Text         labelB;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionBLikeness(final Composite parent,
                                     final ModelBLikenessCriterion model) {

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

        // Create t slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.130")); //$NON-NLS-1$

        labelB = createLabel(group);
        knobB = createKnobDouble(group, 0.000001d, 1000000d);
        updateLabel(labelB, knobB.getValue());
        knobB.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setB(knobB.getValue());
                updateLabel(labelB, model.getB());
            }
        });

        // Allow attack
        Label lblEnhanced = new Label(group, SWT.NONE);
        lblEnhanced.setText(Resources.getMessage("CriterionDefinitionView.131"));

        btnEnhanced = new Button(group, SWT.CHECK);
        btnEnhanced.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                model.setEnhanced(btnEnhanced.getSelection());
            }
        });
        
        return group;
    }
    
    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 0.01, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 0.1, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 1, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 2, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 3, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 4, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 5, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 6, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 7, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 8, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 9, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 10, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 100, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 1000, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 10000, true));
        result.add(new ModelBLikenessCriterion(this.model.getAttribute(), 100000, true));
        return result;
    }


    @Override
    protected void parse(ModelBLikenessCriterion model, boolean _default) {
        knobB.setValue(model.getB());
        updateLabel(labelB, model.getB());
        btnEnhanced.setSelection(model.isEnhanced());
    }
}
