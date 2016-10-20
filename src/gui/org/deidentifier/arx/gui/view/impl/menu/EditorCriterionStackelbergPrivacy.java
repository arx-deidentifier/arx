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
import org.deidentifier.arx.gui.model.ModelStackelbergPrivacyCriterion.AttackerModel;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Editor for the stackelberg game-theoretic privacy model
 * 
 * @author James Gaupp
 * @author Fabian Prasser
 */
public class EditorCriterionStackelbergPrivacy extends EditorCriterion<ModelStackelbergPrivacyCriterion> {

    /** View */
    private static final String        LABELS[] = { Resources.getMessage("CriterionDefinitionView.121"), //$NON-NLS-1$ 
                                                    Resources.getMessage("CriterionDefinitionView.122") }; //$NON-NLS-1$

    /** Model */
    private static final AttackerModel MODELS[] = { AttackerModel.PROSECUTOR,
                                                    AttackerModel.JOURNALIST };

    /** View */
    private Combo                      comboAttackerModel;

    /** View */
    private Button                     checkboxAllowAttack;

	/**
	 * Creates a new instance
	 * @param parent
	 * @param model
	 */
	public EditorCriterionStackelbergPrivacy(final Composite parent, final ModelStackelbergPrivacyCriterion model) {
		super(parent, model);
	}

	/**
	 * Returns the index of the given model
	 * @param model
	 * @return
	 */
    private int getIndexOfAttackerModel(AttackerModel model) {
        for (int i=0; i< MODELS.length; i++) {
            if (MODELS[i].equals(model)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown attacker model");
    }

	@Override
	protected Composite build(Composite parent) {
	    
        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 4;
        group.setLayout(groupInputGridLayout);
        
        // Attacker model
        Label labelAttackerModel = new Label(group, SWT.NONE);
        labelAttackerModel.setText(Resources.getMessage("CriterionDefinitionView.120"));

        comboAttackerModel = new Combo(group, SWT.READ_ONLY);
        comboAttackerModel.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        comboAttackerModel.setItems(LABELS);
        comboAttackerModel.select(0);
        comboAttackerModel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (comboAttackerModel.getSelectionIndex() != -1) {
                    model.setAttackerModel(MODELS[comboAttackerModel.getSelectionIndex()]);
                }
            }
        });
        
        // Allow attack
        Label labelAllowAttack = new Label(group, SWT.NONE);
        labelAllowAttack.setText(Resources.getMessage("CriterionDefinitionView.123"));

        checkboxAllowAttack = new Button(group, SWT.CHECK);
        checkboxAllowAttack.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                model.setAllowAttacks(checkboxAllowAttack.getSelection());
            }
        });
        
        return group;
	}

	@Override
	protected List<ModelCriterion> getTypicalParameters() {

	    // Create list
	    List<ModelCriterion> result = new ArrayList<ModelCriterion>();
	    result.add(new ModelStackelbergPrivacyCriterion(AttackerModel.PROSECUTOR, true));
	    result.add(new ModelStackelbergPrivacyCriterion(AttackerModel.JOURNALIST, true));
	    result.add(new ModelStackelbergPrivacyCriterion(AttackerModel.PROSECUTOR, false));
        result.add(new ModelStackelbergPrivacyCriterion(AttackerModel.JOURNALIST, false));
		// Return
        return result;
	}

	@Override
	protected void parse(ModelStackelbergPrivacyCriterion model, boolean defaultParameters) {
        checkboxAllowAttack.setSelection(model.isAllowAttacks());
        comboAttackerModel.select(getIndexOfAttackerModel(model.getAttackerModel()));
	}
}
