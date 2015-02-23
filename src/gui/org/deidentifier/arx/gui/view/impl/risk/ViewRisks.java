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
import org.deidentifier.arx.gui.model.Model.Perspective;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatus;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a base class for displaying risk estimates.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class ViewRisks<T extends AnalysisContextVisualization> implements IView {

    /** Our users are patient. */
    public static final int       MINIMAL_WORKING_TIME = 500;

    /** Internal stuff. */
    private AnalysisContext       context              = new AnalysisContext();

    /** Internal stuff. */
    private final Controller      controller;

    /** Internal stuff. */
    private Model                 model;

    /** Internal stuff. */
    private final ModelPart       reset;

    /** Internal stuff. */
    private final ModelPart       target;

    /** Internal stuff. */
    private final ComponentStatus status;

    /** Internal stuff. */
    private T                     viewContext;
    
	/**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisks( final Composite parent,
                      final Controller controller,
                      final ModelPart target,
                      final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
        controller.addListener(ModelPart.SELECTED_PERSPECTIVE, this);
        controller.addListener(ModelPart.SELECTED_VIEW_CONFIG, this);
        controller.addListener(ModelPart.SELECTED_RISK_VISUALIZATION, this);
        
        controller.addListener(target, this);
        if (reset != null) {
            controller.addListener(reset, this);
        }
        
        // Remember
        this.controller = controller;
        this.reset = reset;
        this.target = target;

        // Create controls
        parent.setLayout(new StackLayout());
        Control control = this.createControl(parent);

        // Update status
        this.status = new ComponentStatus(controller,
                                          parent, 
                                          control);
        
        // Reset
        this.reset();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        this.doReset();
        status.setEmpty();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {

        // Store
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.context.setModel(model);
            this.context.setTarget(target);
            this.viewContext = null;
            this.reset();
        }

        // Potentially invalidate
        if (event.part == ModelPart.POPULATION_MODEL ||
            event.part == ModelPart.SELECTED_VIEW_CONFIG ||
            (event.part == ModelPart.SELECTED_PERSPECTIVE &&
             model != null && model.getPerspective() == Perspective.RISK)) {
            this.viewContext = null;
            this.update();
            return;
        }

        // Reset on null-target
        if (event.part == target && event.data==null) {
            this.viewContext = null;
            this.reset();
            return;
        }

        // Reset
        if (event.part == reset) {
            this.viewContext = null;
            this.reset();
            return;
        }
         
        // Update
        if (event.part == target ||
           event.part == ModelPart.POPULATION_MODEL ||
           event.part == ModelPart.SELECTED_RISK_VISUALIZATION ||
           event.part == ModelPart.ATTRIBUTE_TYPE ||
           (event.part == ModelPart.SELECTED_PERSPECTIVE && 
           model != null && 
           model.getPerspective() == Perspective.RISK)) {
           
            this.update();
        }
    }

    /**
     * Redraws the plot.
     */
    private void update() {

        if (!this.status.isVisible()){
            this.status.setEmpty();
            return;
        }
        
        if (this.viewContext != null) {
            this.status.setDone();
            return;
        }

        T context = createViewConfig(this.context);
        if (context.isValid()) {

            // Update context
            this.viewContext = context;

            // Update
            this.doUpdate(context);
            
            // Update status
            status.setWorking();
        }
    }
    
    /**
     * 
     *
     * @param context
     * @return
     */
    protected abstract T createViewConfig(AnalysisContext context);
    
    /**
     * 
     * Implement this to create the widget.
     *
     * @param parent
     * @return
     */
    protected abstract Control createControl(Composite parent);

    /**
     * Implement this to reset.
     */
    protected abstract void doReset();

    /**
     * Implement this to update.
     *
     * @param context
     */
    protected abstract void doUpdate(T context);

    /**
     * Status update.
     */
    protected void setStatusDone(){
        this.status.setDone();
    }

    /**
     * Status empty.
     */
    protected void setStatusEmpty(){
        this.status.setEmpty();
    }

    /**
     * Status working.
     */
    protected void setStatusWorking(){
        this.status.setWorking();
    }
    
    /**
     * Creates a risk estimate builder
     * @param context
     * @return
     */
    protected RiskEstimateBuilderInterruptible getBuilder(AnalysisContextRisk context) {
        
        AnalysisContext analysisContext = context.context;
        
        return context.handle.getRiskEstimator(analysisContext.getModel().getPopulationModel().getModel(),
                                               analysisContext.getContext().definition.getQuasiIdentifyingAttributes(),
                                               analysisContext.getModel().getPopulationModel().getAccuracy(),
                                               analysisContext.getModel().getPopulationModel().getMaxIterations())
                                               .getInterruptibleInstance();
    }
}
