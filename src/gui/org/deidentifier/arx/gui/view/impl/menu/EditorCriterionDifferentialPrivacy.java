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
    private Knob<Double>          knobSearchBudget;

    /** View */
    private Knob<Integer>         knobSearchSteps;

    /** View */
    private Combo                 comboGeneralization;

    /** View */
    private Text                  labelEpsilon;

    /** View */
    private Text                  labelDelta;
    
    /** View */
    private Text                  labelSearchBudget;

    /** View */
    private Text                  labelSearchSteps;

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
     * Returns a generalization degree
     * @return
     */
    private GeneralizationDegree getGeneralizationDegree(int index) {
        return GeneralizationDegree.values()[index];
    }
    
    /**
     * Returns a set of all generalization degrees
     * @return
     */
    private String[] getGeneralizationDegrees() {
        List<String> result = new ArrayList<String>();
        for (GeneralizationDegree degree : GeneralizationDegree.values()) {
            String label = degree.toString().replace("_", "-").toLowerCase();
            label = label.substring(0,1).toUpperCase() + label.substring(1);
            result.add(label);
        }
        result.add("Custom...");
        result.add("Data-dependent search");
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the according index
     * @param generalization
     * @return
     */
    private int getIndexOfGeneralizationDegree(GeneralizationDegree generalization) {
        int index = 0;
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
        groupInputGridLayout.numColumns = 14;
        group.setLayout(groupInputGridLayout);
        

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
        comboGeneralization.setItems(getGeneralizationDegrees());
        comboGeneralization.select(0);

        comboGeneralization.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = comboGeneralization.getSelectionIndex();
                if (index == comboGeneralization.getItemCount()-1) {
                    model.setGeneralization(null);
                    knobSearchBudget.setEnabled(true);
                    knobSearchSteps.setEnabled(true);
                    labelSearchBudget.setEnabled(true);
                    labelSearchSteps.setEnabled(true);
                } else {
                    knobSearchBudget.setEnabled(false);
                    knobSearchSteps.setEnabled(false);
                    labelSearchBudget.setEnabled(false);
                    labelSearchSteps.setEnabled(false);
                    if (index == comboGeneralization.getItemCount()-2) {

                        DialogGeneralizationSelection dialog = new DialogGeneralizationSelection(comboGeneralization.getShell(),
                                                                                                 controller,
                                                                                                 arxmodel,
                                                                                                 model.getGeneralization());
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
        
        // Create search budget slider
        final Label bLabel = new Label(group, SWT.NONE);
        bLabel.setText(Resources.getMessage("CriterionDefinitionView.95")); //$NON-NLS-1$

        labelSearchBudget = createLabel(group);
        knobSearchBudget = createKnobDouble(group, 0.01d, 2d);
        updateLabel(labelSearchBudget, knobSearchBudget.getValue()); //$NON-NLS-1$
        knobSearchBudget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setSearchBudget(knobSearchBudget.getValue());
                updateLabel(labelSearchBudget, model.getSearchBudget());
            }
        });
        knobSearchBudget.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
        labelSearchBudget.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);

        // Create search steps slider
        final Label sLabel = new Label(group, SWT.NONE);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.96")); //$NON-NLS-1$

        labelSearchSteps = createLabel(group);
        knobSearchSteps = createKnobInteger(group, 0, 10000);
        updateLabel(labelSearchSteps, knobSearchSteps.getValue());
        knobSearchSteps.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setSearchSteps(knobSearchSteps.getValue());
                updateLabel(labelSearchSteps, model.getSearchSteps());
            }
        });
        knobSearchSteps.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
        labelSearchSteps.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);

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
        updateLabel(labelSearchBudget, model.getSearchBudget());
        updateLabel(labelSearchSteps, model.getSearchSteps());
        knobDelta.setValue(model.getDelta());
        knobEpsilon.setValue(model.getEpsilon());
        knobSearchBudget.setValue(model.getSearchBudget());
        knobSearchSteps.setValue(model.getSearchSteps());
        if (!_default) {
            if (model.getGeneralization() == null) {
                comboGeneralization.select(comboGeneralization.getItemCount() - 1);
            } else {
                int index = getIndexOfGeneralizationDegree(model.getGeneralization().getGeneralizationDegree());
                if (index != -1) {
                    comboGeneralization.select(index);
                } else {
                    comboGeneralization.select(comboGeneralization.getItemCount() - 2);
                }
            }
            knobSearchSteps.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
            knobSearchBudget.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
            labelSearchSteps.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
            labelSearchBudget.setEnabled(comboGeneralization.getSelectionIndex() == comboGeneralization.getItemCount()-1);
        }
    }
}
