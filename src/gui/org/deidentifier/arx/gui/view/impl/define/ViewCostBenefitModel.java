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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This view displays the montary amounts used for cost/benefit analyses
 * 
 * @author Fabian Prasser
 */
public class ViewCostBenefitModel implements IView {

    /**
     * Helper interface
     * @author Fabian Prasser
     *
     * @param <T>
     */
    private interface Callback<T> {
        abstract void call(T value);
    }

    /** Controller */
    private final Controller controller;
    /** View */
    private final Composite  root;
    /** View */
    private Text             textPublisherBenefit;
    /** View */
    private Text             textPublisherLoss;
    /** View */
    private Text             textAdversaryGain;
    /** View */
    private Text             textAdversaryCost;
    
    /** Model */
    private Model            model;

    /**
     * Validator
     */
    private final IInputValidator validator = new IInputValidator(){
        @Override
        public String isValid(String arg0) {
            double value = 0d;
            try {
                value = Double.valueOf(arg0);
            } catch (Exception e) {
                return Resources.getMessage("ViewPopulationModel.5"); //$NON-NLS-1$
            }
            if (value >= 0d) {
                return null;
            } else {
                return Resources.getMessage("ViewPopulationModel.7"); //$NON-NLS-1$
            }
        }
    };
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param layoutCriteria 
     */
    public ViewCostBenefitModel(final Composite parent,
                               final Controller controller) {

        controller.addListener(ModelPart.MODEL, this);
        this.controller = controller;

        // Create group
        root = new Composite(parent, SWT.NONE);
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(6).create());

        // Create text fields
        textPublisherBenefit = createInputField(Resources.getMessage("ViewCostBenefitModel.1"), new Callback<Double>(){ //$NON-NLS-1$
            @Override public void call(Double value){
                model.getInputConfig().setPublisherBenefit(value);
                controller.update(new ModelEvent(ViewCostBenefitModel.this, ModelPart.COST_BENEFIT_MODEL, value));
            }            
        });
        textPublisherLoss = createInputField(Resources.getMessage("ViewCostBenefitModel.2"), new Callback<Double>(){ //$NON-NLS-1$
            @Override public void call(Double value){
                model.getInputConfig().setPublisherLoss(value);
                controller.update(new ModelEvent(ViewCostBenefitModel.this, ModelPart.COST_BENEFIT_MODEL, value));
            }            
        });
        textAdversaryGain = createInputField(Resources.getMessage("ViewCostBenefitModel.3"), new Callback<Double>(){ //$NON-NLS-1$
            @Override public void call(Double value){
                model.getInputConfig().setAdversaryGain(value);
                controller.update(new ModelEvent(ViewCostBenefitModel.this, ModelPart.COST_BENEFIT_MODEL, value));
            }            
        });
        textAdversaryCost = createInputField(Resources.getMessage("ViewCostBenefitModel.4"), new Callback<Double>(){ //$NON-NLS-1$
            @Override public void call(Double value){
                model.getInputConfig().setAdversaryCost(value);
                controller.update(new ModelEvent(ViewCostBenefitModel.this, ModelPart.COST_BENEFIT_MODEL, value));
            }            
        });
        
        this.reset();
    }

    @Override
    public void dispose() {
        if (!root.isDisposed()) {
            root.dispose();
        }
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        textPublisherBenefit.setText(""); //$NON-NLS-1$
        textPublisherLoss.setText(""); //$NON-NLS-1$
        textAdversaryGain.setText(""); //$NON-NLS-1$
        textAdversaryCost.setText(""); //$NON-NLS-1$
        textPublisherBenefit.setToolTipText(textPublisherBenefit.getText());
        textPublisherLoss.setToolTipText(textPublisherLoss.getText());
        textAdversaryGain.setToolTipText(textAdversaryGain.getText());
        textAdversaryCost.setToolTipText(textAdversaryCost.getText());
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           update();
        }
    }

    /**
     * Creates an input field
     * @param caption
     * @param callback
     * @return
     */
    private Text createInputField(String caption, final Callback<Double> callback) {

        // Label
        Label label = new Label(root, SWT.NONE);
        label.setText(caption); 
        
        // Text field
        final Text text = new Text(root, SWT.BORDER | SWT.SINGLE);
        text.setText("0"); //$NON-NLS-1$
        text.setToolTipText("0"); //$NON-NLS-1$
        text.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        text.setEditable(false);
        
        // Button
        Button btn1 = new Button(root, SWT.FLAT);
        btn1.setText(Resources.getMessage("ViewCostBenefitModel.0")); //$NON-NLS-1$
        btn1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                String value = controller.actionShowInputDialog(root.getShell(), 
                                                                Resources.getMessage("ViewCostBenefitModel.5"),  //$NON-NLS-1$
                                                                Resources.getMessage("ViewCostBenefitModel.6"),  //$NON-NLS-1$
                                                                text.getToolTipText(), 
                                                                validator);
                if (value != null) {
                    callback.call(Double.valueOf(value));
                    update();
                }
            }
        });
        
        // Return
        return text;
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        // Check
        if (model == null || model.getInputConfig() == null) {
            return;
        }
        
        // Disable
        root.setRedraw(false);
        
        // Pretty, may involve rounding
        textPublisherBenefit.setText(SWTUtil.getPrettyString(model.getInputConfig().getPublisherBenefit()));
        textPublisherLoss.setText(SWTUtil.getPrettyString(model.getInputConfig().getPublisherLoss()));
        textAdversaryGain.setText(SWTUtil.getPrettyString(model.getInputConfig().getAdversaryGain()));
        textAdversaryCost.setText(SWTUtil.getPrettyString(model.getInputConfig().getAdversaryCost()));
        
        // Not pretty, but precise
        textPublisherBenefit.setToolTipText(String.valueOf(model.getInputConfig().getPublisherBenefit()));
        textPublisherLoss.setToolTipText(String.valueOf(model.getInputConfig().getPublisherLoss()));
        textAdversaryGain.setToolTipText(String.valueOf(model.getInputConfig().getAdversaryGain()));
        textAdversaryCost.setToolTipText(String.valueOf(model.getInputConfig().getAdversaryCost()));
        
        // Enable
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
}
