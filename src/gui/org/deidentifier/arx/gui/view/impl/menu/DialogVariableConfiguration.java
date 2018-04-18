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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelMasking;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.gui.view.impl.masking.ParameterText;
import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.deidentifier.arx.masking.variable.DistributionType;
import org.deidentifier.arx.masking.variable.DistributionType.DistributionTypeDescription;
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

    // Flag whether a variable is newly created, or an existing one is being edited
    private boolean isNew = false;

    // Map translating parameter names to labels
    Map<String, String> parameterLabels = new HashMap<>();

    // Widgets
    private Text textVariableName;
    private Combo comboDistribution;
    private Composite compositeParameter;


    // Constructor for editing an existing random variable
    public DialogVariableConfiguration(Controller controller, RandomVariable variable) {

        super(controller.getResources().getShell());

        initiliazeParameterLabelMap();

        this.controller = controller;
        this.variable = variable;


    }

    private void initiliazeParameterLabelMap() {

        parameterLabels.put("number", "Number");
        parameterLabels.put("probability", "Probability");
        parameterLabels.put("mean", "Mean");
        parameterLabels.put("stddev", "Standard deviation");
        parameterLabels.put("location", "Location");
        parameterLabels.put("scale", "Scale");
        parameterLabels.put("degrees", "Degrees of freedom");
        parameterLabels.put("rate", "Rate");

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

        setMessage("Please configure the random variable by setting the parameters", IMessageProvider.INFORMATION);

    }

    @Override
    protected Control createDialogArea(Composite composite) {

        composite.setLayout(SWTUtil.createGridLayout(2));

        // Variable name
        Label labelVariableName = new Label(composite, SWT.NONE);
        labelVariableName.setText("Variable name");
        textVariableName = new Text(composite, SWT.BORDER);
        textVariableName.setText(variable.getName());

        // Variable distribution
        Label labelDistribution = new Label(composite, SWT.NONE);
        labelDistribution.setText("Distribution");
        comboDistribution = new Combo(composite, SWT.READ_ONLY);

        // Add all available distributions to combo box
        List<DistributionTypeDescription> distributions = DistributionType.list();

        for (DistributionTypeDescription distribution : distributions) {

            comboDistribution.add(distribution.getLabel());

        }

        // Preselect correct distribution
        if (!isNew) {

            comboDistribution.select(comboDistribution.indexOf(variable.getDistributionType().getDescription().getLabel()));

        // New variable, select first element
        } else {

            comboDistribution.select(0);

        }

        // Update parameters whenever selection changes
        comboDistribution.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                updateParameters();

            }

        });

        // Composite for parameters
        compositeParameter = new Composite(composite, SWT.NONE);
        compositeParameter.setLayoutData(SWTUtil.createSpanColumnsGridData(2));
        compositeParameter.setLayout(SWTUtil.createGridLayout(2));

        // Display parameters initially
        updateParameters();

        return composite;

    }

    private void updateParameters() {

        // Dispose all existing parameter widgets
        for (Control children : compositeParameter.getChildren()) {

            children.dispose();

        }

        // Iterate over parameters for selected distribution
        for (DistributionTypeDescription distribution : DistributionType.list()) {

            if (distribution.getLabel().equals(comboDistribution.getText())) {

                for (DistributionParameter<?> parameter : distribution.getParameters()) {

                    createText(parameter);

                }

            }

        }

        // Update layout
        compositeParameter.layout();

    }

    private void createText(DistributionParameter<?> parameter) {

        // Create label
        Label label = new Label(compositeParameter, SWT.NONE);
        label.setText(parameterLabels.get(parameter.getName()));

        // Create text
        ParameterText text = new ParameterText(parameter, compositeParameter, SWT.BORDER);
        text.setText(String.valueOf(parameter.getInitial()));

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        Button ok = createButton(parent, Window.OK, "OK", true);
        Button cancel = createButton(parent, Window.CANCEL, "Cancel", false);

    }

    @Override
    protected void okPressed() {

        // Set name of variable
        variable.setName(textVariableName.getText());

        // Set variable type
        switch (comboDistribution.getText()) {

            // TODO: Export, so no hardcoded strings?
            case "Binomial distribution (discrete)":
                variable.setDistributionType(DistributionType.DISCRETE_BINOMIAL);
                break;

            case "Geometric distribution (discrete)":
                variable.setDistributionType(DistributionType.DISCRETE_GEOMETRIC);
                break;

        }

        // Iterate over all available parameters and add them
        for (Control children : compositeParameter.getChildren()) {

            if (children instanceof ParameterText) {

                // Replace new parameter
                // TODO Clean this up
                DistributionParameter<?> parameter = ((ParameterText)children).getParameter();
                variable.removeParameter(parameter.getName());
                variable.addParameter(parameter);

            }

        }

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
