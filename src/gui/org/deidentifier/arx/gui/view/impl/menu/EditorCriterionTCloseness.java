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

import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
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
 * Implements a view on a t-closeness criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionTCloseness extends EditorCriterion<ModelTClosenessCriterion> {

    /**  View */
    private static final String VARIANTS[] = {Resources.getMessage("CriterionDefinitionView.9"), Resources.getMessage("CriterionDefinitionView.10") }; //$NON-NLS-1$ //$NON-NLS-2$

    /**  View */
    private Scale               sliderT;
    
    /**  View */
    private Combo               comboVariant;
    
    /**  View */
    private Label               labelT;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionTCloseness(final Composite parent,
                                     final ModelTClosenessCriterion model) {

        super(parent, model);
    }

    /**
     * Updates the label and tool tip text.
     *
     * @param text
     */
    private void updateLabel(String text) {
        labelT.setText(text);
        labelT.setToolTipText(text);
    }

    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 5;
        group.setLayout(groupInputGridLayout);

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.42")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d32 = SWTUtil.createFillHorizontallyGridData();
        d32.verticalAlignment = SWT.CENTER;
        d32.horizontalSpan = 1;
        comboVariant.setLayoutData(d32);
        comboVariant.setItems(VARIANTS);
        comboVariant.select(0);
        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setVariant(comboVariant.getSelectionIndex());
            }
        });

        // Create t slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.43")); //$NON-NLS-1$

        labelT = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelT.setLayoutData(d9);
        updateLabel("0.001"); //$NON-NLS-1$

        sliderT = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderT.setLayoutData(d6);
        sliderT.setMaximum(SWTUtil.SLIDER_MAX);
        sliderT.setMinimum(0);
        sliderT.setSelection(0);
        sliderT.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setT(SWTUtil.sliderToDouble(0.001d, 1d, sliderT.getSelection()));
                updateLabel(String.valueOf(model.getT()));
            }
        });

        return group;
    }
    
    @Override
    protected void parse(ModelTClosenessCriterion model) {
        
        sliderT.setSelection(SWTUtil.doubleToSlider(0.001d, 1d, model.getT()));
        updateLabel(String.valueOf(model.getT()));
        comboVariant.select(model.getVariant());
    }
}
