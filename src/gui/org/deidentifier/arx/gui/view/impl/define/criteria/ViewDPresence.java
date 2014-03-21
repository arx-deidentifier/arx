/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.define.criteria;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelDPresenceCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
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
 * A view on a d-presence criterion
 * @author Fabian Prasser
 */
public class ViewDPresence extends ViewCriterion{

    private Scale                  sliderDMin;
    private Scale                  sliderDMax;
    private Label                  labelDMin;
    private Label                  labelDMax;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param model
     */
    public ViewDPresence(final Composite parent,
                         final Controller controller,
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

        sliderDMin.setSelection(0);
        sliderDMax.setSelection(0);
        labelDMin.setText("0.001"); //$NON-NLS-1$
        labelDMax.setText("0.001"); //$NON-NLS-1$
        super.reset();
	}

	@Override
	protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 6;
        group.setLayout(groupInputGridLayout);

        // Create dmin slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.50")); //$NON-NLS-1$

        labelDMin = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelDMin.setLayoutData(d9);
        labelDMin.setText("0.001"); //$NON-NLS-1$

        sliderDMin = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderDMin.setLayoutData(d6);
        sliderDMin.setMaximum(SLIDER_MAX);
        sliderDMin.setMinimum(0);
        sliderDMin.setSelection(0);
        final Object outer = this;
        sliderDMin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getDPresenceModel().setDmin(sliderToDouble(0.001, 1, sliderDMin.getSelection()));
                labelDMin.setText(String.valueOf(model.getDPresenceModel().getDmin()));
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, model.getDPresenceModel()));
            }
        });


        // Create dax slider
        final Label z2Label = new Label(group, SWT.NONE);
        z2Label.setText(Resources.getMessage("CriterionDefinitionView.51")); //$NON-NLS-1$

        labelDMax = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d91 = new GridData();
        d91.minimumWidth = LABEL_WIDTH;
        d91.widthHint = LABEL_WIDTH;
        labelDMax.setLayoutData(d91);
        labelDMax.setText("0.001"); //$NON-NLS-1$

        sliderDMax = new Scale(group, SWT.HORIZONTAL);
        final GridData d62 = SWTUtil.createFillHorizontallyGridData();
        d62.horizontalSpan = 1;
        sliderDMax.setLayoutData(d62);
        sliderDMax.setMaximum(SLIDER_MAX);
        sliderDMax.setMinimum(0);
        sliderDMax.setSelection(0);
        sliderDMax.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getDPresenceModel().setDmax(sliderToDouble(0.001, 1, sliderDMax.getSelection()));
                labelDMax.setText(String.valueOf(model.getDPresenceModel().getDmax()));
                controller.update(new ModelEvent(outer, ModelPart.CRITERION_DEFINITION, model.getDPresenceModel()));
            }
        });

        return group;
	}

	@Override
	protected void parse() {
		ModelDPresenceCriterion m = model.getDPresenceModel();
		if (m==null){
			reset();
			return;
		}
		root.setRedraw(false);
		labelDMin.setText(String.valueOf(m.getDmin()));
		sliderDMin.setSelection(doubleToSlider(0.001d, 1d, m.getDmin()));
		labelDMax.setText(String.valueOf(m.getDmax()));
		sliderDMax.setSelection(doubleToSlider(0.001d, 1d, m.getDmax()));
		if (m.isActive() && m.isEnabled()) {
			SWTUtil.enable(root);
		} else {
			SWTUtil.disable(root);
		}
		root.setRedraw(true);
	}
}
