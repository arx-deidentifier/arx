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

import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelKMapCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;

/**
 * A view on a k-map criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionKMap extends EditorCriterion<ModelKMapCriterion> {
    
    /** View */
    private Text          labelK;
                          
    /** View */
    private Knob<Integer> knobK;
                          
    /** View */
    private Combo         cmbModel;
                          
    /** View */
    private Knob<Double>  knobSignificanceLevel;
                          
    /** View */
    private Text          labelSignificanceLevel;
                          
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionKMap(final Composite parent,
                               final ModelKMapCriterion model) {
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
        groupInputGridLayout.numColumns = 8;
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
        
        Label lblModel = new Label(group, SWT.NONE);
        lblModel.setText(Resources.getMessage("EditorCriterionKMap.0")); //$NON-NLS-1$
        
        cmbModel = new Combo(group, SWT.READ_ONLY);
        cmbModel.setItems(new String[] { Resources.getMessage("EditorCriterionKMap.1"), //$NON-NLS-1$
                                         Resources.getMessage("EditorCriterionKMap.2"), //$NON-NLS-1$
                                         Resources.getMessage("EditorCriterionKMap.3") }); //$NON-NLS-1$
        cmbModel.select(0);
        cmbModel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                switch (cmbModel.getSelectionIndex()) {
                case 0:
                    model.setEstimator(null);
                    knobSignificanceLevel.setEnabled(false);
                    labelSignificanceLevel.setEnabled(false);
                    break;
                case 1:
                    model.setEstimator(KMap.CellSizeEstimator.POISSON);
                    knobSignificanceLevel.setEnabled(true);
                    labelSignificanceLevel.setEnabled(true);
                    break;
                case 2:
                    model.setEstimator(KMap.CellSizeEstimator.ZERO_TRUNCATED_POISSON);
                    knobSignificanceLevel.setEnabled(true);
                    labelSignificanceLevel.setEnabled(true);
                    break;
                }
            }
        });
        
        Label sigLabel = new Label(group, SWT.NONE);
        sigLabel.setText(Resources.getMessage("CriterionDefinitionView.101")); //$NON-NLS-1$
        labelSignificanceLevel = createLabel(group);
        knobSignificanceLevel = createKnobDouble(group, 0d, 1d);
        updateLabel(labelSignificanceLevel, knobSignificanceLevel.getValue());
        knobSignificanceLevel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setSignificanceLevel(knobSignificanceLevel.getValue());
                updateLabel(labelSignificanceLevel, model.getSignificanceLevel());
            }
        });
        
        knobSignificanceLevel.setEnabled(false);
        labelSignificanceLevel.setEnabled(false);
        
        return group;
    }
    
    @Override
    protected List<ModelCriterion> getTypicalParameters() {
        
        List<ModelCriterion> result = new ArrayList<ModelCriterion>();
        result.add(new ModelKMapCriterion(3));
        result.add(new ModelKMapCriterion(5));
        result.add(new ModelKMapCriterion(10));
        result.add(new ModelKMapCriterion(100));
        return result;
    }
    
    /**
     * Parse
     */
    @Override
    protected void parse(ModelKMapCriterion model, boolean _default) {
        updateLabel(labelK, model.getK());
        knobK.setValue(model.getK());
        if (model.getEstimator() == null) {
            cmbModel.select(0);
            knobSignificanceLevel.setEnabled(false);
            labelSignificanceLevel.setEnabled(false);
        } else {
            switch (model.getEstimator()) {
            case POISSON:
                cmbModel.select(1);
                break;
            case ZERO_TRUNCATED_POISSON:
                cmbModel.select(2);
                break;
            }
            knobSignificanceLevel.setEnabled(true);
            labelSignificanceLevel.setEnabled(true);
        }
        updateLabel(labelSignificanceLevel, model.getSignificanceLevel());
        knobSignificanceLevel.setValue(model.getSignificanceLevel());
    }
}
