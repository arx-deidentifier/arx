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
import org.deidentifier.arx.gui.model.ModelStackelbergPrivacyCriterion;
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
 * Editor for the stackelberg game-theoretic privacy model
 * 
 * @author James Gaupp
 * @author Fabian Prasser
 */
public class EditorCriterionStackelbergPrivacy extends EditorCriterion<ModelStackelbergPrivacyCriterion> {

    /** View */
    private Text          labelAdvCost;
    /** View */
    private Text          labelAdvGain;
    /** View */
    private Text          labelPubBenefit;
    /** View */
    private Text          labelPubLoss;

    /** View */
    private Knob<Integer> knobAdvCost;
    /** View */
    private Knob<Integer> knobAdvGain;
    /** View */
    private Knob<Integer> knobPubBenefit;
    /** View */
    private Knob<Integer> knobPubLoss;

	/**
	 * Creates a new instance
	 * @param parent
	 * @param model
	 */
	public EditorCriterionStackelbergPrivacy(final Composite parent, final ModelStackelbergPrivacyCriterion model) {
		super(parent, model);
	}

	@Override
	protected Composite build(Composite parent) {
	    
        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 6;
        group.setLayout(groupInputGridLayout);
        
        Label pubBenefit = new Label(group, SWT.NONE);
        pubBenefit.setText(Resources.getMessage("CriterionDefinitionView.110"));
        labelPubBenefit = createLabel(group);
        knobPubBenefit = createKnobInteger(group, 0, Integer.MAX_VALUE);
        knobPubBenefit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setPublisherBenefit(knobPubBenefit.getValue());
                updateLabel(labelPubBenefit, model.getPublisherBenefit());
            }
        });
        
        Label pubLoss = new Label(group, SWT.NONE);
        pubLoss.setText(Resources.getMessage("CriterionDefinitionView.111"));
        labelPubLoss = createLabel(group);
        knobPubLoss = createKnobInteger(group, 0, Integer.MAX_VALUE);
        knobPubLoss.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setPublisherLoss(knobPubLoss.getValue());
                updateLabel(labelPubLoss, model.getPublisherLoss());
            }
        });
        
        Label advGain = new Label(group, SWT.NONE);
        advGain.setText(Resources.getMessage("CriterionDefinitionView.112"));
        labelAdvGain = createLabel(group);
        knobAdvGain = createKnobInteger(group, 0, Integer.MAX_VALUE);
        knobAdvGain.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setAdversaryGain(knobAdvGain.getValue());
                updateLabel(labelAdvGain, model.getAdversaryGain());
            }
        });
        
        Label advCost = new Label(group, SWT.NONE);
        advCost.setText(Resources.getMessage("CriterionDefinitionView.113"));
        labelAdvCost = createLabel(group);
        knobAdvCost = createKnobInteger(group, 0, Integer.MAX_VALUE);
        knobAdvCost.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setAdversaryCost(knobAdvCost.getValue());
                updateLabel(labelAdvCost, model.getAdversaryCost());
            }
        });

        return group;
	}

	@Override
	protected List<ModelCriterion> getTypicalParameters() {

	    // Create list
	    List<ModelCriterion> result = new ArrayList<ModelCriterion>();
	    
	    // TODO: We could add some more configurations here
		ModelStackelbergPrivacyCriterion mc1 = new ModelStackelbergPrivacyCriterion();
		mc1.setAdversaryCost(4d);
		mc1.setAdversaryGain(300d);
		mc1.setPublisherLoss(300d);
		mc1.setPublisherBenefit(1200d);
		result.add(mc1);
		
		// Return
        return result;
	}

	@Override
	protected void parse(ModelStackelbergPrivacyCriterion model, boolean defaultParameters) {
        updateLabel(labelAdvCost, model.getAdversaryCost());
        updateLabel(labelAdvGain, model.getAdversaryGain());
        updateLabel(labelPubBenefit, model.getPublisherBenefit());
        updateLabel(labelPubLoss, model.getPublisherLoss());
        knobAdvCost.setValue((int) model.getAdversaryCost());
        knobAdvGain.setValue((int) model.getAdversaryGain());
        knobPubBenefit.setValue((int) model.getPublisherBenefit());
        knobPubLoss.setValue((int) model.getPublisherLoss());
	}
}
