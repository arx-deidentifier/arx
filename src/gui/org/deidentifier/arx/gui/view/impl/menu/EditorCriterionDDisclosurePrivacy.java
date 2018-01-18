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
import org.deidentifier.arx.gui.model.ModelDDisclosurePrivacyCriterion;
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
 * Implements a view on a d-disclosure privacy criterion
 *
 * @author Fabian Prasser
 */
public class EditorCriterionDDisclosurePrivacy extends EditorCriterion<ModelDDisclosurePrivacyCriterion> {

    /** View */
    private Knob<Double> knobD;

    /** View */
    private Text         labelD;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionDDisclosurePrivacy(final Composite parent,
                                     final ModelDDisclosurePrivacyCriterion model) {

        super(parent, model);
    }

    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);

        // Create t slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.100")); //$NON-NLS-1$

        labelD = createLabel(group);
        knobD = createKnobDouble(group, 0.000001d, 1000000d);
        updateLabel(labelD, knobD.getValue());
        knobD.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setD(knobD.getValue());
                updateLabel(labelD, model.getD());
            }
        });

        return group;
    }
    
    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelDDisclosurePrivacyCriterion(this.model.getAttribute(), 0.8));
        result.add(new ModelDDisclosurePrivacyCriterion(this.model.getAttribute(), 1.0));
        result.add(new ModelDDisclosurePrivacyCriterion(this.model.getAttribute(), 1.2));
        return result;
    }


    @Override
    protected void parse(ModelDDisclosurePrivacyCriterion model, boolean _default) {
        
        knobD.setValue(model.getD());
        updateLabel(labelD, model.getD());
    }
}
