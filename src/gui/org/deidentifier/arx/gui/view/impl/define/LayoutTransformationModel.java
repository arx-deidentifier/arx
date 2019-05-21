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
    private Composite             root;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutTransformationModel(final Composite parent, final Controller controller) {

        this.controller = controller;
        
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
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
        folder.setVisible(Resources.getMessage("CriterionDefinitionView.63"), false); //$NON-NLS-1$
        folder.setVisible(Resources.getMessage("CriterionDefinitionView.65"), false); //$NON-NLS-1$ 
    }
    
    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        } 
        updateControls();
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
        
        // Coding model
        Composite composite4 = folder.createItem(Resources.getMessage("CriterionDefinitionView.65"), null, true);  //$NON-NLS-1$
        composite4.setLayout(new FillLayout());
        new ViewCodingModel(composite4, controller);
        
        // Attribute weights
        Composite composite3 = folder.createItem(Resources.getMessage("CriterionDefinitionView.63"), null, true);  //$NON-NLS-1$
        composite3.setLayout(new FillLayout());
        new ViewAttributeWeights(composite3, controller);
        
        // Select first and finish
        folder.setSelection(0);
        return group;
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
                model.getInputDefinition().getQuasiIdentifyingAttributes().isEmpty() || !description.isAttributeWeightsSupported()) {
                folder.setVisible(Resources.getMessage("CriterionDefinitionView.63"), false); //$NON-NLS-1$
            } else {
                folder.setVisible(Resources.getMessage("CriterionDefinitionView.63"), true); //$NON-NLS-1$
            }
            
            folder.setVisible(Resources.getMessage("CriterionDefinitionView.65"), //$NON-NLS-1$ 
                              description.isConfigurableCodingModelSupported()); 
        }

        root.setRedraw(true);        
    }
}
