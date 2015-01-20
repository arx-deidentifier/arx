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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
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
 * A view on an l-diversity criterion.
 *
 * @author Fabian Prasser
 */
public class ViewCriterionLDiversity extends ViewCriterion {

    /**  TODO */
    private static final String VARIANTS[] = { Resources.getMessage("CriterionDefinitionView.6"), Resources.getMessage("CriterionDefinitionView.7"), Resources.getMessage("CriterionDefinitionView.8") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    /**  TODO */
    private Scale               sliderL;
    
    /**  TODO */
    private Scale               sliderC;
    
    /**  TODO */
    private Combo               comboVariant;
    
    /**  TODO */
    private Label               labelC;
    
    /**  TODO */
    private Label               labelL;
    
    /**  TODO */
    private String              attribute;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param model
     */
    public ViewCriterionLDiversity(final Composite parent,
                                   final Controller controller,
                                   final Model model) {

        super(parent, controller, model);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#reset()
     */
    @Override
    public void reset() {
        sliderL.setSelection(0);
        sliderC.setSelection(0);
        sliderC.setEnabled(false);
        updateCLabel("0.001"); //$NON-NLS-1$
        updateLLabel("2"); //$NON-NLS-1$
        comboVariant.select(0);
        super.reset();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            this.attribute = (String) event.data;
            this.parse();
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            if (event.data.equals(this.attribute)) {
                this.parse();
            }
        }
        super.update(event);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#build(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 8;
        group.setLayout(groupInputGridLayout);

        // Create l slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.27")); //$NON-NLS-1$

        labelL = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        labelL.setLayoutData(d);
        updateLLabel("2"); //$NON-NLS-1$

        sliderL = new Scale(group, SWT.HORIZONTAL);
        final GridData d4 = SWTUtil.createFillHorizontallyGridData();
        d4.horizontalSpan = 1;
        sliderL.setLayoutData(d4);
        sliderL.setMaximum(SWTUtil.SLIDER_MAX);
        sliderL.setMinimum(0);
        sliderL.setSelection(0);
        final Object outer = this;
        sliderL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
                m.setL(SWTUtil.sliderToInt(2, 100, sliderL.getSelection()));
                updateLLabel(String.valueOf(m.getL()));
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, m));
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.33")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        d31.horizontalSpan = 1;
        comboVariant.setLayoutData(d31);
        comboVariant.setItems(VARIANTS);
        comboVariant.select(0);

        // Create c slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.34")); //$NON-NLS-1$

        labelC = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelC.setLayoutData(d9);
        updateCLabel("0.001"); //$NON-NLS-1$

        sliderC = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderC.setLayoutData(d6);
        sliderC.setMaximum(SWTUtil.SLIDER_MAX);
        sliderC.setMinimum(0);
        sliderC.setSelection(0);
        sliderC.setEnabled(false);
        sliderC.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
                m.setC(SWTUtil.sliderToDouble(0.001d, 100d, sliderC.getSelection()));
                updateCLabel(String.valueOf(m.getC()));
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, m));
            }
        });

        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
                m.setVariant(comboVariant.getSelectionIndex());
                if (m.getVariant() == 2) {
                    sliderC.setEnabled(true);
                } else {
                    sliderC.setEnabled(false);
                }
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, m));
            }
        });

        return group;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#parse()
     */
    @Override
    protected void parse() {
        ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
        if (m == null) {
            reset();
            return;
        }
        root.setRedraw(false);
        updateCLabel(String.valueOf(m.getC()));
        updateLLabel(String.valueOf(m.getL()));
        sliderL.setSelection(SWTUtil.intToSlider(2, 100, m.getL()));
        sliderC.setSelection(SWTUtil.doubleToSlider(0.001d, 100d, m.getC()));

        comboVariant.select(m.getVariant());
        if (m.isActive() && m.isEnabled()) {
            SWTUtil.enable(root);
        } else {
            SWTUtil.disable(root);
        }

        if (m.getVariant() == 2) {
            sliderC.setEnabled(true);
        } else {
            sliderC.setEnabled(false);
        }

        root.setRedraw(true);
    }

    /**
     * Updates the "c" label and tooltip text.
     *
     * @param text
     */
    private void updateCLabel(String text) {
        labelC.setText(text);
        labelC.setToolTipText(text);
    }

    /**
     * Updates the "l" label and tooltip text.
     *
     * @param text
     */
    private void updateLLabel(String text) {
        labelL.setText(text);
        labelL.setToolTipText(text);
    }
}
