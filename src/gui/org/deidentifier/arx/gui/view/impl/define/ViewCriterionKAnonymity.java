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
import org.deidentifier.arx.gui.model.ModelKAnonymityCriterion;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
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
 * A view on a k-anonymity criterion
 * @author Fabian Prasser
 */
public class ViewCriterionKAnonymity extends ViewCriterion {

    private Label labelK;
    private Scale sliderK;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param model
     */
    public ViewCriterionKAnonymity(final Composite parent, final Controller controller,
                                   final Model model) {

        super(parent, controller, model);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            this.parse();
        }
        super.update(event);
    }

    @Override
    public void reset() {
        sliderK.setSelection(0);
        updateLabel("2");
        super.reset();
    }

    @Override
    protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
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
        final Object outer = this;
        sliderK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getKAnonymityModel().setK(
                                                SWTUtil.sliderToInt(2, 100, sliderK.getSelection()));
                updateLabel(String
                                     .valueOf(model.getKAnonymityModel().getK()));
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, model.getKAnonymityModel()));
            }
        });

        return group;
    }

    @Override
    protected void parse() {
        ModelKAnonymityCriterion m = model.getKAnonymityModel();
        if (m == null) {
            reset();
            return;
        }
        root.setRedraw(false);
        updateLabel(String.valueOf(m.getK()));
        sliderK.setSelection(SWTUtil.intToSlider(2, 100, m.getK()));
        if (m.isActive() && m.isEnabled()) {
            SWTUtil.enable(root);
        } else {
            SWTUtil.disable(root);
        }
        root.setRedraw(true);
    }

    /**
     * Updates the label and tooltip text
     * @param text
     */
    private void updateLabel(String text) {
        labelK.setText(text);
        labelK.setToolTipText(text);
    }
}
