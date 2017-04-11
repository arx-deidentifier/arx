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
 * @author Raffael Bild
 */
public class EditorCriterionDifferentialPrivacy extends EditorCriterion<ModelDifferentialPrivacyCriterion> {

    /** View */
    private Knob<Double>          knobDelta;

    /** View */
    private Knob<Double>          knobEpsilonAnon;
    
    /** View */
    private Knob<Double>          knobEpsilonSearch;
    
    /** View */
    private Knob<Integer>         knobSteps;

    /** View */
    private Combo                 comboGeneralization;

    /** View */
    private Text                  labelEpsilonAnon;
    
    /** View */
    private Text                  labelEpsilonSearch;

    /** View */
    private Text                  labelDelta;
    
    /** View */
    private Text                  labelSteps;

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
     * @param controller
     * @param arxmodel
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
     * @param index
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

        // Create input groups
        final Composite groups = new Composite(parent, SWT.NONE);
        groups.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 1;
        groups.setLayout(groupInputGridLayout);
        
        final Composite topGroup = new Composite(groups, SWT.NONE);
        topGroup.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout topGroupInputGridLayout = new GridLayout();
        topGroupInputGridLayout.numColumns = 9;
        topGroup.setLayout(topGroupInputGridLayout);
        
        final Composite bottomGroup = new Composite(groups, SWT.NONE);
        bottomGroup.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout bottomGroupInputGridLayout = new GridLayout();
        bottomGroupInputGridLayout.numColumns = 5;
        bottomGroup.setLayout(bottomGroupInputGridLayout);
        

        // Create epsilon anon slider
        final Label aLabel = new Label(topGroup, SWT.NONE);
        aLabel.setText(Resources.getMessage("CriterionDefinitionView.92")); //$NON-NLS-1$

        labelEpsilonAnon = createLabel(topGroup);
        knobEpsilonAnon = createKnobDouble(topGroup, 0.01d, 2d);
        updateLabel(labelEpsilonAnon, knobEpsilonAnon.getValue()); //$NON-NLS-1$
        knobEpsilonAnon.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setEpsilonAnon(knobEpsilonAnon.getValue());
                updateLabel(labelEpsilonAnon, model.getEpsilonAnon());
            }
        });
        
        // Create epsilon search slider
        final Label sLabel = new Label(topGroup, SWT.NONE);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.124")); //$NON-NLS-1$

        labelEpsilonSearch = createLabel(topGroup);
        knobEpsilonSearch = createKnobDouble(topGroup, 0d, 2d);
        updateLabel(labelEpsilonSearch, knobEpsilonSearch.getValue()); //$NON-NLS-1$

        // Create delta slider
        final Label lLabel = new Label(topGroup, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.93")); //$NON-NLS-1$

        labelDelta = createLabel(topGroup);
        knobDelta = createKnobDouble(topGroup, 0.00000000001d, 0.00001d);
        updateLabel(labelDelta, knobDelta.getValue());
        knobDelta.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setDelta(knobDelta.getValue());
                updateLabel(labelDelta, model.getDelta());
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(bottomGroup, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.94")); //$NON-NLS-1$

        comboGeneralization = new Combo(bottomGroup, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        d31.horizontalSpan = 1;
        comboGeneralization.setLayoutData(d31);
        comboGeneralization.setItems(getGeneralizationDegrees());
        comboGeneralization.select(0);
        comboGeneralization.setEnabled(knobEpsilonSearch.getValue() == 0d);

        comboGeneralization.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = comboGeneralization.getSelectionIndex();
                if (index == comboGeneralization.getItemCount()-1) {
                    
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
        });
        
        // Create steps slider
        final Label tLabel = new Label(bottomGroup, SWT.NONE);
        tLabel.setText(Resources.getMessage("CriterionDefinitionView.125")); //$NON-NLS-1$

        labelSteps = createLabel(bottomGroup);
        labelSteps.setEnabled(knobEpsilonSearch.getValue() > 0d);
        knobSteps = createKnobInteger(bottomGroup, 0, 1000);
        knobSteps.setEnabled(knobEpsilonSearch.getValue() > 0d);
        
        updateLabel(labelSteps, knobSteps.getValue());
        knobSteps.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setSteps(knobSteps.getValue());
                updateLabel(labelSteps, model.getSteps());
            }
        });
        
        // Create selection listener for epsilon search slider 
        knobEpsilonSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setEpsilonSearch(knobEpsilonSearch.getValue());
                updateLabel(labelEpsilonSearch, model.getEpsilonSearch());
                comboGeneralization.setEnabled(knobEpsilonSearch.getValue() == 0d);
                knobSteps.setEnabled(knobEpsilonSearch.getValue() > 0d);
                labelSteps.setEnabled(knobEpsilonSearch.getValue() > 0d);
            }
        });

        return groups;
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
        
        updateLabel(labelEpsilonAnon, model.getEpsilonAnon());
        updateLabel(labelEpsilonSearch, model.getEpsilonSearch());
        updateLabel(labelDelta, model.getDelta());
        updateLabel(labelSteps, model.getSteps());
        knobDelta.setValue(model.getDelta());
        // TODO this is a hack because the initial value of epsilonAnon = 2 is somehow not being overtaken but set to 0
        knobEpsilonAnon.setValue(model.getEpsilonAnon() == 0d ? 2d : model.getEpsilonAnon());
        knobEpsilonSearch.setValue(model.getEpsilonSearch());
        // TODO this is a hack because the initial value of steps = 100 is somehow not being overtaken but set to 0
        knobSteps.setValue(model.getSteps() == 0 ? 100 : model.getSteps());
        if (!_default) {
            int index = getIndexOfGeneralizationDegree(model.getGeneralization().getGeneralizationDegree());
            if (index != -1) {
                comboGeneralization.select(index);
            } else {
                comboGeneralization.select(comboGeneralization.getItemCount() - 1);
            }
        }
    }
}
