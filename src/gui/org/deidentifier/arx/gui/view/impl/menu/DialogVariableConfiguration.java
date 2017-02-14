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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelMasking;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.gui.view.impl.masking.DistributionComposite;
import org.deidentifier.arx.gui.view.impl.masking.DistributionCompositeBinomial;
import org.deidentifier.arx.gui.view.impl.masking.DistributionCompositeGeometric;
import org.deidentifier.arx.masking.variable.RandomVariable;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog allowing to configure variable distribution
 * 
 * @TODO Re-consider if it is a good idea to inject the controller into this dialog and handling all of the logic
 * (add variables, send notifications, etc.) within the dialog itself, or whether it should be handled outside.
 * In order to check whether the variable name is unique the dialog needs to know about all defined variables, so
 * it probably needs access to the controller.
 *
 * @TODO Make sure names are unique?
 *
 * @TODO Use databinding (good validation support): http://www.vogella.com/tutorials/EclipseDataBinding/article.html
 *
 * @author Karol Babioch
 */
public class DialogVariableConfiguration extends TitleAreaDialog implements IDialog {

    private Controller controller;
    private RandomVariable variable;

    // Flag whether a new variable is newly created, or an existing one is being edited
    private boolean isNew = false;

    // Widgets
    private Text textVariableName;
    private Combo comboDistribution;
    private Composite compositeParameter;

    private DistributionComposite c1;

    // Constructor for editing an existing random variable
    public DialogVariableConfiguration(Controller controller, RandomVariable variable) {

        super(controller.getResources().getShell());

        this.controller = controller;
        this.variable = variable;

    }

    // Constructor in case a new random variable is created
    public DialogVariableConfiguration(Controller controller) {

        this(controller, new RandomVariable(""));
        this.isNew = true;

    }

    @Override
    public void create() {

        super.create();

        if (isNew) {

            setTitle("Add a new random variable");

        } else {

            setTitle("Configure an existing random variable");

        }

        setMessage("Please configure the random variable by setting the parameters shown below", IMessageProvider.INFORMATION);

    }

    @Override
    protected Control createDialogArea(Composite parent) {

        parent.setLayout(SWTUtil.createGridLayout(2));

        // Variable name
        Label labelVariableName = new Label(parent, SWT.NONE);
        labelVariableName.setText("Variable name");
        textVariableName = new Text(parent, SWT.NONE);
        textVariableName.setText(variable.getName());

        // Variable distribution
        Label labelDistribution = new Label(parent, SWT.NONE);
        labelDistribution.setText("Distribution");
        comboDistribution = new Combo(parent, SWT.READ_ONLY);
        comboDistribution.setItems(new String[]{
            "Binomial distribution (discrete)",
            "Geometric distribution (discrete)",
        });
        comboDistribution.select(0);
        comboDistribution.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                updateParameterComposite();

            }

        });

        // Composite for parameters
        compositeParameter = new Composite(parent, SWT.NONE);
        compositeParameter.setLayoutData(SWTUtil.createSpanColumnsGridData(2));

        // Display parameter composite for default selection
        updateParameterComposite();

        return parent;

    }

    private void updateParameterComposite() {

        // Dispose all children
        for (Control c : compositeParameter.getChildren()) {

            c.dispose();

        }

        // Create new composite for given distribution
        switch (comboDistribution.getSelectionIndex()) {

            case 0: c1 = new DistributionCompositeBinomial(compositeParameter); break;
            case 1: c1 = new DistributionCompositeGeometric(compositeParameter); break;

        }

        // Update layout
        compositeParameter.layout();

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        Button ok = createButton(parent, Window.OK, "OK", true);
        Button cancel = createButton(parent, Window.CANCEL, "Cancel", false);

    }

    @Override
    protected void okPressed() {

        // Configure variable in accordance to user input
        variable.setName(textVariableName.getText());
        variable.setDistribution(c1.getResultingDistribution());

        // Add or replace variable in model
        ModelMasking maskingModel = controller.getModel().getMaskingModel();
        int index = maskingModel.getRandomVariables().indexOf(variable);

        // Variable not yet in model -> add
        if (index == -1) {

            maskingModel.addRandomVariable(variable);

        // Variable already in model -> replace
        } else {

            maskingModel.getRandomVariables().set(index, variable);

        }

        // Send notification about update
        controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE_CHANGED, variable));

        // Dispose dialog, etc.
        super.okPressed();

    }

}
