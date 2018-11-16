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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.analysis.function.Log;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelDifferentialPrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.window.Window;
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
 * A view on an (e,d)-DP criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionDifferentialPrivacy extends EditorCriterion<ModelDifferentialPrivacyCriterion> {

    /** View */
    private Knob<Double>          knobDelta;

    /** View */
    private Knob<Double>          knobEpsilon;
    
    /** View */
    private Knob<Double>          knobEpsilonGeneralization;

    /** View */
    private Combo                 comboGeneralization;

    /** View */
    private Text                  labelEpsilon;

    /** View */
    private Text                  labelDelta;
    
    /** View */
    private Text                  labelEpsilonGeneralization;

    /**
     * Some epsilon values mentioned in "Practicing Differential Privacy in Health Care: A Review"
     */
    private static final double[] EPSILONS = new double[] {
                                          2d,
                                          1.5d,
                                          1.25d,
                                          (new Log()).value(3),
                                          1.0d,
                                          0.75d,
                                          (new Log()).value(2),
                                          0.5d,
                                          0.1d,
                                          0.01d
                                          };
    
    /**
     * Some delta values below the 1E-4d limit mentioned in "Practicing Differential Privacy in Health Care: A Review".
     */
    private static final double[] DELTAS   = new double[] { 1E-5d, 1E-6d, 1E-7d, 1E-8d, 1E-9d, 1E-10d };

    /** Controller*/
    private Controller controller;
    
    /** Model*/
    private Model arxmodel;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionDifferentialPrivacy(final Composite parent,
                                              final ModelDifferentialPrivacyCriterion model,
                                              final Controller controller,
                                              final Model arxmodel) {
        super(parent, model);
        this.controller = controller;
        this.arxmodel = arxmodel;
    }

    /**
     * Returns the generalization degree associated with an index of a generalization option
     * @param index
     * @return the generalization degree or null if index is not associated with a fixed generalization degree
     */
    private GeneralizationDegree getGeneralizationDegree(int index) {
        if (index >= 1 && index <= GeneralizationDegree.values().length) {
            return GeneralizationDegree.values()[index-1];
        }
        return null;
    }
    
    /**
     * Returns an array of all generalization options
     * @return
     */
    private String[] getGeneralizationOptions() {
        List<String> result = new ArrayList<String>();
        result.add("Automatic");
        for (GeneralizationDegree degree : GeneralizationDegree.values()) {
            String label = degree.toString().replace("_", "-").toLowerCase();
            label = label.substring(0,1).toUpperCase() + label.substring(1);
            result.add(label);
        }
        result.add("Custom...");
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the index of the generalization option corresponding to a generalization degree
     * or -1 in case generalization is not a fixed generalization degree
     * @param generalization
     * @return
     */
    private int getIndexOfGeneralizationDegree(GeneralizationDegree generalization) {
        int index = 1;
        for (GeneralizationDegree degree : GeneralizationDegree.values()) {
            if (degree == generalization) {
                return index;
            }
            index ++;
        }
        return -1;
    }
    
    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 11;
        group.setLayout(groupInputGridLayout);
        

        // Create delta slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.93")); //$NON-NLS-1$

        labelDelta = createLabel(group);
        knobDelta = createKnobDouble(group, 0.00000000001d, 0.00001d);
        updateLabel(labelDelta, knobDelta.getValue());
        knobDelta.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setDelta(knobDelta.getValue());
                updateLabel(labelDelta, model.getDelta());
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.94")); //$NON-NLS-1$

        comboGeneralization = new Combo(group, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        d31.horizontalSpan = 1;
        comboGeneralization.setLayoutData(d31);
        comboGeneralization.setItems(getGeneralizationOptions());
        comboGeneralization.select(0);

        comboGeneralization.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = comboGeneralization.getSelectionIndex();
                if (index == 0) {
                    model.setGeneralization(null);
                    knobEpsilonGeneralization.setEnabled(true);
                    labelEpsilonGeneralization.setEnabled(true);
                } else {
                    knobEpsilonGeneralization.setEnabled(false);
                    labelEpsilonGeneralization.setEnabled(false);
                    if (index == comboGeneralization.getItemCount()-1) {
                        
                        DataGeneralizationScheme selectedScheme = model.getGeneralization();
                        if (selectedScheme == null) {
                            selectedScheme = DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM);
                        }

                        DialogGeneralizationSelection dialog = new DialogGeneralizationSelection(comboGeneralization.getShell(),
                                                                                                 controller,
                                                                                                 arxmodel,
                                                                                                 selectedScheme);
                        dialog.create();
                        if (dialog.open() == Window.OK) {
                            DataGeneralizationScheme generalization = DataGeneralizationScheme.create();
                            Map<String, Integer> scheme = dialog.getSelection();
                            for (Entry<String, Integer> entry : scheme.entrySet()){
                                generalization.generalize(entry.getKey(), entry.getValue());
                            }
                            model.setGeneralization(generalization);
                        } 

                    } else if (index != -1) {
                        model.setGeneralization(DataGeneralizationScheme.create(getGeneralizationDegree(index)));
                    }
                }
            }
        });
        
        // Create epsilon slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.92")); //$NON-NLS-1$

        labelEpsilon = createLabel(group);
        knobEpsilon = createKnobDouble(group, 0.01d, 2d);
        updateLabel(labelEpsilon, knobEpsilon.getValue()); //$NON-NLS-1$
        knobEpsilon.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setEpsilon(knobEpsilon.getValue());
                updateLabel(labelEpsilon, model.getEpsilon());
            }
        });
        
        // Create generalization budget slider
        final Label bLabel = new Label(group, SWT.NONE);
        bLabel.setText(Resources.getMessage("CriterionDefinitionView.95")); //$NON-NLS-1$

        labelEpsilonGeneralization = createLabel(group);
        knobEpsilonGeneralization = createKnobDouble(group, 0.01d, 95d);
        updateLabel(labelEpsilonGeneralization, knobEpsilonGeneralization.getValue()); //$NON-NLS-1$
        knobEpsilonGeneralization.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setEpsilonGeneralizationFraction(knobEpsilonGeneralization.getValue() / 100d);
                updateLabel(labelEpsilonGeneralization, knobEpsilonGeneralization.getValue());
            }
        });
        knobEpsilonGeneralization.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
        labelEpsilonGeneralization.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);

        return group;
    }
    
    @Override
    protected List<ModelCriterion> getTypicalParameters() {

        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        for (double delta : DELTAS) {
            for (double epsilon : EPSILONS) {
                result.add(new ModelDifferentialPrivacyCriterion(epsilon, delta));
            }
        }
        return result;
    }

    @Override
    protected void parse(ModelDifferentialPrivacyCriterion model, boolean _default) {
        
        updateLabel(labelEpsilon, model.getEpsilon());
        updateLabel(labelDelta, model.getDelta());
        updateLabel(labelEpsilonGeneralization, model.getEpsilonGeneralizationFraction() * 100d);
        knobDelta.setValue(model.getDelta());
        knobEpsilon.setValue(model.getEpsilon());
        knobEpsilonGeneralization.setValue(model.getEpsilonGeneralizationFraction() * 100d);
        if (!_default) {
            if (model.getGeneralization() == null) {
                comboGeneralization.select(0);
            } else {
                int index = getIndexOfGeneralizationDegree(model.getGeneralization().getGeneralizationDegree());
                if (index != -1) {
                    comboGeneralization.select(index);
                } else {
                    comboGeneralization.select(comboGeneralization.getItemCount() - 1);
                }
            }
            knobEpsilonGeneralization.setEnabled(comboGeneralization.getSelectionIndex() == 0);
            labelEpsilonGeneralization.setEnabled(comboGeneralization.getSelectionIndex() == 0);
        }
    }
}
