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
import org.deidentifier.arx.gui.model.ModelDPresenceCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;

/**
 * A view on a d-presence criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionDPresence extends EditorCriterion<ModelDPresenceCriterion>{

    /** View */
    private Knob<Double> knobDMin;

    /** View */
    private Knob<Double> knobDMax;

    /** View */
    private Text         labelDMin;

    /** View */
    private Text         labelDMax;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionDPresence(final Composite parent, 
                                  final ModelDPresenceCriterion model) {
        super(parent, model);
    }

    /**
     * Build
     * @param parent
     * @return
     */
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 6;
        group.setLayout(groupInputGridLayout);

        // Create dmin slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.50")); //$NON-NLS-1$

        labelDMin = createLabel(group);
        knobDMin = createKnobDouble(group, 0d, 1d);
        updateLabel(labelDMin, knobDMin.getValue());
        knobDMin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setDmin(knobDMin.getValue());
                updateLabel(labelDMin, model.getDmin());
                
                if (model.getDmin() > model.getDmax()) {
                    model.setDmax(model.getDmin());
                    updateLabel(labelDMax, model.getDmin());
                    knobDMax.setValue(knobDMin.getValue());
                }
            }
        });

        // Create dax slider
        final Label z2Label = new Label(group, SWT.NONE);
        z2Label.setText(Resources.getMessage("CriterionDefinitionView.51")); //$NON-NLS-1$

        labelDMax = createLabel(group);
        knobDMax = createKnobDouble(group, 0d, 1d);
        updateLabel(labelDMax, knobDMax.getValue());
        knobDMax.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setDmax(knobDMax.getValue());
                updateLabel(labelDMax, model.getDmax());
                
                if (model.getDmax() < model.getDmin()) {
                    model.setDmin(model.getDmax());
                    updateLabel(labelDMin, model.getDmax());
                    knobDMin.setValue(knobDMax.getValue());
                }
            }
        });
        
        return group;
    }

    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelDPresenceCriterion(0.00d, 0.05d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.06d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.07d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.08d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.09d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.10d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.20d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.50d));
        result.add(new ModelDPresenceCriterion(0.00d, 0.70d));
        result.add(new ModelDPresenceCriterion(0.04d, 0.05d));
        result.add(new ModelDPresenceCriterion(0.01d, 0.05d));        
        return result;
    }


    /**
     * Parses the input
     */
    protected void parse(ModelDPresenceCriterion model, boolean _default) {
        updateLabel(labelDMin, model.getDmin());
        knobDMin.setValue(model.getDmin());
        updateLabel(labelDMax, model.getDmax());
        knobDMax.setValue(model.getDmax());
    }
}
