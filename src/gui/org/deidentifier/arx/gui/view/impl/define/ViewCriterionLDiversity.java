/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 * A view on an l-diversity criterion
 * @author Fabian Prasser
 */
public class ViewCriterionLDiversity extends ViewCriterion {

    private static final String VARIANTS[] = { Resources.getMessage("CriterionDefinitionView.6"), Resources.getMessage("CriterionDefinitionView.7"), Resources.getMessage("CriterionDefinitionView.8") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private Scale               sliderL;
    private Scale               sliderC;
    private Combo               comboVariant;
    private Label               labelC;
    private Label               labelL;
    private String              attribute;

    /**
     * Creates a new instance
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
     * Updates the "c" label and tooltip text
     * @param text
     */
    private void updateCLabel(String text) {
        labelC.setText(text);
        labelC.setToolTipText(text);
    }

    /**
     * Updates the "l" label and tooltip text
     * @param text
     */
    private void updateLLabel(String text) {
        labelL.setText(text);
        labelL.setToolTipText(text);
    }
}
