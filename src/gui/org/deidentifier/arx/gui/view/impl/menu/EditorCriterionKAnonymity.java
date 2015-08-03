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

import org.deidentifier.arx.gui.model.ModelKAnonymityCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * A view on a k-anonymity criterion.
 *
 * @author Fabian Prasser
 */
public class EditorCriterionKAnonymity extends EditorCriterion<ModelKAnonymityCriterion>{

    /**  View */
    private Label labelK;
    
    /**  View */
    private Scale sliderK;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public EditorCriterionKAnonymity(final Composite parent, 
                                     final ModelKAnonymityCriterion model) {
        super(parent, model);
    }

    /**
     * Updates the label and tool tip text.
     *
     * @param text
     */
    private void updateLabel(String text) {
        labelK.setText(text);
        labelK.setToolTipText(text);
    }

    /**
     * Build
     * @param parent
     * @return
     */
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);

        // Create k slider
        final Label kLabel = new Label(group, SWT.NONE);
        kLabel.setText(Resources.getMessage("CriterionDefinitionView.22")); //$NON-NLS-1$

        labelK = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        labelK.setLayoutData(d);
        updateLabel("2"); //$NON-NLS-1$

        sliderK = new Scale(group, SWT.HORIZONTAL);
        sliderK.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        sliderK.setMaximum(SWTUtil.SLIDER_MAX);
        sliderK.setMinimum(0);
        sliderK.setSelection(0);
        sliderK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setK(SWTUtil.sliderToInt(2, 100, sliderK.getSelection()));
                updateLabel(String.valueOf(model.getK()));
            }
        });

        return group;
    }

    /**
     * Parse
     */
    protected void parse(ModelKAnonymityCriterion model) {
        updateLabel(String.valueOf(model.getK()));
        sliderK.setSelection(SWTUtil.intToSlider(2, 100, model.getK()));
    }
}
