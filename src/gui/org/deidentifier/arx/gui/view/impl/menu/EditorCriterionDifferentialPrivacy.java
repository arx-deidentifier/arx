/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import org.apache.commons.math3.analysis.function.Log;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.gui.model.ModelDifferentialPrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * A view on an (e,d)-DP criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionDifferentialPrivacy extends EditorCriterion<ModelDifferentialPrivacyCriterion> {

    /**  View */
    private Scale               sliderDelta;
    
    /**  View */
    private Scale               sliderEpsilon;
    
    /**  View */
    private Combo               comboGeneralization;
    
    /**  View */
    private Label               labelEpsilon;
    
    /**  View */
    private Label               labelDelta;
    

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

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionDifferentialPrivacy(final Composite parent,
                                              final ModelDifferentialPrivacyCriterion model) {
        super(parent, model);
    }

    /**
     * Updates the "epsilon" label and tool tip text.
     *
     * @param text
     */
    private void updateEpsilonLabel(String text) {
        labelEpsilon.setText(text);
        labelEpsilon.setToolTipText(text);
    }

    /**
     * Updates the "delta" label and tool tip text.
     *
     * @param text
     */
    private void updateDeltaLabel(String text) {
        labelDelta.setText(text);
        labelDelta.setToolTipText(text);
    }

    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 8;
        group.setLayout(groupInputGridLayout);
        

        // Create epsilon slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.90")); //$NON-NLS-1$

        labelEpsilon = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelEpsilon.setLayoutData(d9);
        updateEpsilonLabel("0.001"); //$NON-NLS-1$

        sliderEpsilon = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderEpsilon.setLayoutData(d6);
        sliderEpsilon.setMaximum(SWTUtil.SLIDER_MAX);
        sliderEpsilon.setMinimum(0);
        sliderEpsilon.setSelection(0);
        sliderEpsilon.setEnabled(false);
        sliderEpsilon.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setEpsilon(SWTUtil.sliderToDouble(0.01d, 2d, sliderEpsilon.getSelection()));
                updateEpsilonLabel(String.valueOf(model.getEpsilon()));
            }
        });

        // Create delta slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.91")); //$NON-NLS-1$

        labelDelta = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        labelDelta.setLayoutData(d);
        updateDeltaLabel("2"); //$NON-NLS-1$

        sliderDelta = new Scale(group, SWT.HORIZONTAL);
        final GridData d4 = SWTUtil.createFillHorizontallyGridData();
        d4.horizontalSpan = 1;
        sliderDelta.setLayoutData(d4);
        sliderDelta.setMaximum(SWTUtil.SLIDER_MAX);
        sliderDelta.setMinimum(0);
        sliderDelta.setSelection(0);
        sliderDelta.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setDelta(SWTUtil.sliderToDouble(0.00000000001d, 0.00001d, sliderDelta.getSelection()));
                updateDeltaLabel(String.valueOf(model.getDelta()));
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.92")); //$NON-NLS-1$

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
                if (index != -1) {
                    model.setGeneralization(DataGeneralizationScheme.create(getGeneralizationDegree(index)));
                }
            }
        });

        return group;
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
    /**
     * Returns a generalization degree
     * @return
     */
    private GeneralizationDegree getGeneralizationDegree(int index) {
        return GeneralizationDegree.values()[index];
    }
    
    @Override
    protected void parse(ModelDifferentialPrivacyCriterion model) {
        
        updateEpsilonLabel(String.valueOf(model.getEpsilon()));
        updateDeltaLabel(String.valueOf(model.getDelta()));
        sliderDelta.setSelection(SWTUtil.doubleToSlider(0.00000000001d, 0.00001d, model.getDelta()));
        sliderEpsilon.setSelection(SWTUtil.doubleToSlider(0.01d, 2d, model.getEpsilon()));
        comboGeneralization.select(getIndexOfGeneralizationDegree(model.getGeneralization().getGeneralizationDegree()));
    }
}
