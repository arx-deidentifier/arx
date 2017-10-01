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

package org.deidentifier.arx.gui.view.impl.risk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.Model.Perspective;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatus;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelHistogram;
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
    protected final Controller    controller;

    /** Internal stuff. */
    private Model                 model;

    /** Internal stuff. */
    private final ModelPart       reset;

    /** Internal stuff. */
    private final ModelPart       target;

    /** Internal stuff. */
    private final ComponentStatus status;

    /** Internal stuff. */
    private boolean               enabled              = true;

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
        controller.addListener(ModelPart.MODEL, this);
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
                                          control,
                                          this,
                                          getProgressProvider());
        
        // Reset
        this.reset();
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Is this view enabled
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reset() {
        this.doReset();
        status.setEmpty();
    }

    /**
     * Enables or disables this view
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            this.viewContext = null;
            this.update();
        }
    }
    
    /**
     * Stops all computations
     */
    public void triggerStop() {
        this.viewContext = null;
        this.doReset();
        this.setStatusEmpty();
    }

    /**
     * Triggers an update
     */
    public void triggerUpdate() {
        this.viewContext = null;
        this.update();
    }
    
    @Override
    public void update(final ModelEvent event) {

        // Store
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.context.setModel(model);
            this.context.setTarget(target);
            this.viewContext = null;
            this.reset();
            return;
        }

        // Invalidate
        if (event.part == ModelPart.SELECTED_VIEW_CONFIG) {
            triggerUpdate();
            return;
        }

        // Reset on null-target
        if (event.part == target && event.data == null || event.part == reset) {
            this.viewContext = null;
            this.reset();
            return;
        }

        // Update
        if (event.part == target ||
            event.part == ModelPart.SELECTED_RISK_VISUALIZATION ||
            (event.part == ModelPart.SELECTED_PERSPECTIVE && model != null && model.getPerspective() == Perspective.RISK)) {
            this.update();
            return;
        }
    }

    /**
     * Redraws the plot.
     */
    private void update() {
        
        // Disable the view
        if (!this.isEnabled()) {
            triggerStop();
            return;
        }

        // Check visibility
        if (!this.status.isVisible()){
            return;
        }

        // Check if already done
        if (this.viewContext != null) {
            if (!isRunning()) {
                this.status.setDone();
            }
            return;
        }

        // Update
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
     * Implement this to create the widget.
     *
     * @param parent
     * @return
     */
    protected abstract Control createControl(Composite parent);
    
    /**
     * Creates a view config
     *
     * @param context
     * @return
     */
    protected abstract T createViewConfig(AnalysisContext context);
    
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
     * Creates a risk estimate builder
     * @param context
     * @return
     */
    protected RiskEstimateBuilderInterruptible getBuilder(AnalysisContextRisk context) {
        
        AnalysisContext analysisContext = context.context;
        if (analysisContext.getData() == null || analysisContext.getData().definition == null) {
            return null;
        }
        return context.handle.getRiskEstimator(analysisContext.getPopulationModel(),
                                               analysisContext.getData().definition.getQuasiIdentifyingAttributes(),
                                               analysisContext.getModel().getRiskModel().getSolverConfiguration())
                                               .getInterruptibleInstance();
    }

    /**
     * Creates a risk estimate builder
     * @param context
     * @param population
     * @param classes
     * @return
     */
    protected RiskEstimateBuilderInterruptible getBuilder(AnalysisContextRisk context, 
                                                          ARXPopulationModel population, 
                                                          RiskModelHistogram classes) {

        AnalysisContext analysisContext = context.context;
        return context.handle.getRiskEstimator(population, 
                                               classes, 
                                               analysisContext.getModel().getRiskModel().getSolverConfiguration())
                                               .getInterruptibleInstance();
    }
    
    /**
     * Creates a risk estimate builder
     * @param context
     * @param identifiers
     * @return
     */
    protected RiskEstimateBuilderInterruptible getBuilder(AnalysisContextRisk context,
                                                          Set<String> identifiers) {
        
        AnalysisContext analysisContext = context.context;
        return context.handle.getRiskEstimator(analysisContext.getPopulationModel(),
                                               identifiers,
                                               analysisContext.getModel().getRiskModel().getSolverConfiguration())
                                               .getInterruptibleInstance();
    }

    /**
     * Returns the model
     * @return
     */
    protected Model getModel() {
        return this.model;
    }

    
    /**
     * May return a progress provider, if any
     * @return
     */
    protected abstract ComponentStatusLabelProgressProvider getProgressProvider();

    /**
     * Returns a string containing all quasi-identifiers
     * @param context
     * @return
     */
    protected String getQuasiIdentifiers(AnalysisContextRisk context) {
        AnalysisContext analysisContext = context.context;
        List<String> list = new ArrayList<String>();
        list.addAll(analysisContext.getData().definition.getQuasiIdentifyingAttributes());
        Collections.sort(list);
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1){
                builder.append(", "); //$NON-NLS-1$
            }
        }
        return builder.toString();
    }

    /**
     * Returns the according type of view
     * @return
     */
    protected abstract ViewRiskType getViewType();
    
    /**
     * Is this an input data oriented control
     * @return
     */
    protected boolean isInput() {
        return target == ModelPart.INPUT;
    }

    /**
     * Is a job running
     * @return
     */
    protected abstract boolean isRunning();
    
    /**
     * Is there still some data to show
     * @return
     */
    protected boolean isValid() {
        if (this.target == ModelPart.INPUT) {
            return this.viewContext != null && this.model != null && this.model.getInputConfig() != null && this.model.getInputConfig().getInput() != null;
        } else {
            return this.viewContext != null && this.model != null && this.model.getOutput() != null;
        }
    }
    
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
}
