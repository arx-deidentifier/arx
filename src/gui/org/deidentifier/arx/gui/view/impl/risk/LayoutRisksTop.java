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

package org.deidentifier.arx.gui.view.impl.risk;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Layouts the risk analysis perspective.
 *
 * @author Fabian Prasser
 */
public class LayoutRisksTop extends LayoutRisksAbstract implements IView {

    /** View */
    private final ToolItem     subsetButton;

    /** Controller */
    protected final Controller controller;

    /** Model */
    protected Model            model;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public LayoutRisksTop(final Composite parent,
                          final Controller controller,
                          final ModelPart target,
                          final ModelPart reset) {

        super(parent, controller, getButtonBar(controller));
        this.controller = controller;
        
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.SELECTED_VIEW_CONFIG, this);
        controller.addListener(ModelPart.MODEL, this);

        registerView(0, new ViewRisksClassDistributionPlot(createTab(Resources.getMessage("RiskAnalysis.4")), controller, target, reset)); //$NON-NLS-1$
        registerView(1, new ViewRisksClassDistributionTable(createTab(Resources.getMessage("RiskAnalysis.0")), controller, target, reset)); //$NON-NLS-1$
        registerView(2, new ViewRisksAttributesTable(createTab(Resources.getMessage("RiskAnalysis.15")), controller, target, reset)); //$NON-NLS-1$
        
        setSelectionIdex(0);
        
        this.subsetButton = getButtonItem(Resources.getMessage("DataView.3")); //$NON-NLS-1$
        this.subsetButton.setEnabled(false);
    }

    /**
     * Creates the button bar
     * @param controller
     * @return
     */
    private static ComponentTitledFolderButton getButtonBar(final Controller controller) {
        ComponentTitledFolderButton bar = new ComponentTitledFolderButton("id-140");
        bar.add(Resources.getMessage("DataView.3"), //$NON-NLS-1$ 
                controller.getResources().getImage("sort_subset.png"),
                true,
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionDataToggleSubset();
                    }
                });
        return bar;
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        subsetButton.setEnabled(false);
    }

    @Override
    public void update(ModelEvent event) {

        // Enable/Disable sort button
        if (event.part == ModelPart.OUTPUT ||
            event.part == ModelPart.INPUT ||
            event.part == ModelPart.SELECTED_VIEW_CONFIG) {
            
            if (model != null && model.getOutput() != null){
                subsetButton.setEnabled(true);
            } else {
                subsetButton.setEnabled(false);
            }
        }
        
        // Update model
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
        }
        
        if (event.part == ModelPart.SELECTED_VIEW_CONFIG) {          
            subsetButton.setSelection(model.getViewConfig().isSubset());
        }
    }
}
