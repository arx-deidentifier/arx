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
 * A view on a k-anonymity criterion.
 *
 * @author Fabian Prasser
 */
public class ViewCriterionKAnonymity extends ViewCriterion {

    /**  TODO */
    private Label labelK;
    
    /**  TODO */
    private Scale sliderK;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param model
     */
    public ViewCriterionKAnonymity(final Composite parent, final Controller controller,
                                   final Model model) {

        super(parent, controller, model);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            this.parse();
        }
        super.update(event);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#reset()
     */
    @Override
    public void reset() {
        sliderK.setSelection(0);
        updateLabel("2");
        super.reset();
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.define.ViewCriterion#parse()
     */
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
     * Updates the label and tooltip text.
     *
     * @param text
     */
    private void updateLabel(String text) {
        labelK.setText(text);
        labelK.setToolTipText(text);
    }
}
