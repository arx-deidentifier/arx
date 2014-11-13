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
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
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
public class ViewCriterionTCloseness extends ViewCriterion {

    /**  TODO */
    private static final String VARIANTS[] = {
                                           Resources.getMessage("CriterionDefinitionView.9"), Resources.getMessage("CriterionDefinitionView.10") }; //$NON-NLS-1$ //$NON-NLS-2$

    /**  TODO */
    private Scale               sliderT;
    
    /**  TODO */
    private Combo               comboVariant;
    
    /**  TODO */
    private Label               labelT;
    
    /**  TODO */
    private String              attribute;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param model
     */
    public ViewCriterionTCloseness(final Composite parent, final Controller controller,
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
        sliderT.setSelection(0);
        updateLabel("0.001"); //$NON-NLS-1$
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
        final Object outer = this;
        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                ModelTClosenessCriterion m = model.getTClosenessModel().get(
                                                                            attribute);
                m.setVariant(comboVariant.getSelectionIndex());
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, m));
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
                ModelTClosenessCriterion m = model.getTClosenessModel().get(
                                                                            attribute);
                m.setT(SWTUtil.sliderToDouble(0.001d, 1d, sliderT.getSelection()));
                updateLabel(String.valueOf(m.getT()));
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
        ModelTClosenessCriterion m = model.getTClosenessModel().get(attribute);
        if (m == null) {
            reset();
            return;
        }
        root.setRedraw(false);
        sliderT.setSelection(SWTUtil.doubleToSlider(0.001d, 1d, m.getT()));
        updateLabel(String.valueOf(m.getT()));
        comboVariant.select(m.getVariant());
        if (m.isActive() && m.isEnabled()) {
            SWTUtil.enable(root);
        } else {
            SWTUtil.disable(root);
        }
        root.setRedraw(true);
    }
    
    /**
     * Updates the label and tooltip text.
     *
     * @param text
     */
    private void updateLabel(String text) {
        labelT.setText(text);
        labelT.setToolTipText(text);
    }
}
