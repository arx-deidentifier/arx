/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.metric.MetricConfiguration;
import org.deidentifier.arx.metric.MetricDescription;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This layout manages views for general settings regarding data transformation.
 *
 * @author Fabian Prasser
 */
public class LayoutTransformationModel implements ILayout, IView {

    /** Controller. */
    private final Controller      controller;

    /** Model. */
    private Model                 model;

    /** View. */
    private ComponentTitledFolder folder;

    /** View. */
    private IView                 viewCodingModel;

    /** View. */
    private IView                 viewAttributeWeights;

    /** View. */
    private Composite             root;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutTransformationModel(final Composite parent,
                                        final Controller controller) {

        this.controller = controller;
        
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.METRIC, this);
                
        this.root = build(parent);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        hideSettingsAttributeWeights();
        hideSettingsCodingModel();
    }
    
    @Override
    public void update(ModelEvent event) {
        
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            updateControls();
        } 
        
        if (event.part == ModelPart.ATTRIBUTE_TYPE ||
            event.part == ModelPart.INPUT ||
            event.part == ModelPart.METRIC) {
            updateControls();
        }
    }

    /**
     * 
     *
     * @param parent
     * @return
     */
    private Composite build(final Composite parent) {

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(new FillLayout());

        folder = new ComponentTitledFolder(group, controller, null, "id-60"); //$NON-NLS-1$
        
        // Create general tab
        group = folder.createItem(Resources.getMessage("CriterionDefinitionView.61"), null);  //$NON-NLS-1$
        group.setLayout(new FillLayout());
        new ViewTransformationSettings(group, controller);
        
        // Create metrics tab
        Composite composite1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.66"), null);  //$NON-NLS-1$
        composite1.setLayout(new FillLayout());
        new ViewUtilityMeasures(composite1, controller);
        
        // Select first and finish
        folder.setSelection(0);
        return group;
    }
  
    /**
     * Hides the settings for the attribute weights.
     */
    private void hideSettingsAttributeWeights(){

        if (this.viewAttributeWeights != null) {
            this.viewAttributeWeights.dispose();
            this.viewAttributeWeights = null;
            folder.disposeItem(Resources.getMessage("CriterionDefinitionView.63"));  //$NON-NLS-1$
        }
    }

    /**
     * Hides the settings for the coding model.
     */
    private void hideSettingsCodingModel(){
        if (this.viewCodingModel != null) {
            this.viewCodingModel.dispose();
            this.viewCodingModel = null;
            folder.disposeItem(Resources.getMessage("CriterionDefinitionView.65"));  //$NON-NLS-1$
        }
    }

    /**
     * Shows the settings for the attribute weights.
     */
    private void showSettingsAttributeWeights(){
        if (this.viewAttributeWeights != null) return;
        Composite composite1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.63"), null);  //$NON-NLS-1$
        composite1.setLayout(new FillLayout());
        this.viewAttributeWeights = new ViewAttributeWeights(composite1, controller);
        this.viewAttributeWeights.update(new ModelEvent(this, ModelPart.MODEL, this.model));
    }

    /**
     * Shows the settings for the coding model.
     */
    private void showSettingsCodingModel(){
        if (this.viewCodingModel != null) return;
        Composite composite2 = folder.createItem(Resources.getMessage("CriterionDefinitionView.65"), null);  //$NON-NLS-1$
        composite2.setLayout(new FillLayout());
        this.viewCodingModel = new ViewCodingModel(composite2, controller);
        this.viewCodingModel.update(new ModelEvent(this, ModelPart.MODEL, this.model));
    }

    /**
     * This method updates the view
     */
    private void updateControls() {
        
        root.setRedraw(false);

        MetricConfiguration config = model.getMetricConfiguration();
        MetricDescription description = model.getMetricDescription();

        if (config != null && description != null) {

            if (model == null || model.getInputDefinition() == null || model.getInputConfig() == null ||
                model.getInputDefinition().getQuasiIdentifyingAttributes().isEmpty() ||
                !description.isAttributeWeightsSupported()) {
                hideSettingsAttributeWeights();
            } else {
                showSettingsAttributeWeights();
            }
            
            if (description.isConfigurableCodingModelSupported()) {
                showSettingsCodingModel();
            } else {
                hideSettingsCodingModel();
            }
        }

        root.setRedraw(true);        
    }
}
