/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.gui.model.ModelMinimumKeySizeCriterion;
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
 * A view on a minimum key size criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionMinimumKeySize extends EditorCriterion<ModelMinimumKeySizeCriterion>{

    /** View */
    private Text          labelK;

    /** View */
    private Knob<Integer> knobK;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionMinimumKeySize(final Composite parent,
                                         final ModelMinimumKeySizeCriterion model) {
        super(parent, model);
    }

    /**
     * Build
     * @param parent
     * @return
     */
    protected Composite build(Composite parent) {

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);

        // Create k slider
        Label kLabel = new Label(group, SWT.NONE);
        kLabel.setText(Resources.getMessage("CriterionDefinitionView.22")); //$NON-NLS-1$

        labelK = createLabel(group);
        knobK = createKnobInteger(group, 2, 1000);
        updateLabel(labelK, knobK.getValue());
        knobK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setK(knobK.getValue());
                updateLabel(labelK, model.getK());
            }
        });

        return group;
    }

    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelMinimumKeySizeCriterion(2));
        result.add(new ModelMinimumKeySizeCriterion(3));
        result.add(new ModelMinimumKeySizeCriterion(4));
        result.add(new ModelMinimumKeySizeCriterion(5));
        result.add(new ModelMinimumKeySizeCriterion(6));
        result.add(new ModelMinimumKeySizeCriterion(7));
        result.add(new ModelMinimumKeySizeCriterion(8));
        result.add(new ModelMinimumKeySizeCriterion(9));
        result.add(new ModelMinimumKeySizeCriterion(10));
        return result;
    }
    
    /**
     * Parse
     */
    protected void parse(ModelMinimumKeySizeCriterion model, boolean _default) {
        updateLabel(labelK, model.getK());
        knobK.setValue(model.getK());
    }
}
