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

package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.Model.Perspective;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IAnalysis;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatus;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a base class for displaying utility data.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class ViewStatistics<T extends AnalysisContextVisualization> implements IView, IAnalysis {

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

    /** Internal stuff. */
    private final boolean         dependsOnAttribute;

    /** Is this view enabled */
    private boolean               enabled              = false;

    /** Parent */
    private final Composite       parent;

	/**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     * @param dependsOnAttribute
     */
    public ViewStatistics( final Composite parent,
                           final Controller controller,
                           final ModelPart target,
                           final ModelPart reset,
                           final boolean dependsOnAttribute) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_VIEW_CONFIG, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.SELECTED_UTILITY_VISUALIZATION, this);
        controller.addListener(ModelPart.ATTRIBUTE_VALUE, this);
        controller.addListener(target, this);
        if (reset != null) {
            controller.addListener(reset, this);
        }
        
        // Init
        this.controller = controller;
        this.reset = reset;
        this.target = target;
        this.dependsOnAttribute = dependsOnAttribute;
        this.parent = parent;

        // Create controls
        parent.setLayout(new StackLayout());
        Control control = this.createControl(parent);
        
        // Obtain progress provider
        ComponentStatusLabelProgressProvider provider = getProgressProvider();

        // Update status
        if (provider == null) {
            this.status = new ComponentStatus(controller,
                                              parent, 
                                              control,
                                              this);
        } else {
            this.status = new ComponentStatus(controller,
                                              parent,                                                  
                                              control,            
                                              this,
                                              getProgressProvider());
        }
        
        // Reset
        this.reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns the type
     * @return
     */
    public abstract LayoutUtility.ViewUtilityType getType();

    @Override
    public void reset() {
        this.doReset();
        status.setEmpty();
    }

    @Override
    public void triggerStop() {
        this.viewContext = null;
        this.doReset();
        this.setStatusEmpty();
    }

    @Override
    public void triggerUpdate() {
        this.viewContext = null;
        this.update();
    }
    
    @Override
    public void update(final ModelEvent event) {

        // Store
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.model.resetAttributePair();
            this.context.setModel(model);
            this.context.setTarget(target);
            this.viewContext = null;
            this.reset();
        }
        
        // Reset on null-target
        if (event.part == target && event.data==null) {
            this.viewContext = null;
            this.reset();
            return;
        }
        
        // Invalidate
        if (event.part == ModelPart.OUTPUT || event.part == target || event.part == ModelPart.SELECTED_VIEW_CONFIG) {
            this.triggerUpdate();
            return;
        }

        // Invalidate
        if (event.part == ModelPart.SELECTED_ATTRIBUTE || event.part == ModelPart.ATTRIBUTE_VALUE) {
            if (dependsOnAttribute) {
                this.triggerUpdate();
                return;
            }
        }
        
        // Potentially invalidate
        if (event.part == ModelPart.DATA_TYPE ||
            event.part == ModelPart.ATTRIBUTE_TYPE ||
            event.part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE) {
            if (dependsOnAttribute) {
                if (model == null ||  viewContext == null || viewContext.isAttributeSelected(model.getSelectedAttribute())) {
                    this.triggerUpdate();
                    return;
                }
            }
        }

        // Reset
        if (event.part == reset) {
            this.viewContext = null;
            this.reset();
            return;
        }
        
        // Update
        if (event.part == target ||
            event.part == ModelPart.SELECTED_VIEW_CONFIG ||
            event.part == ModelPart.SELECTED_UTILITY_VISUALIZATION ||
            (event.part == ModelPart.SELECTED_PERSPECTIVE && model != null && model.getPerspective() == Perspective.ANALYSIS)) {
            this.update();
        }
    }

    /**
     * Redraws the plot.
     */
    private void update() {

        // Disable the view
        if (model != null && !model.isVisualizationEnabled()) {
            this.triggerStop();
            this.enabled = false;
            return;
        }

        // Check visibility
        if (!this.status.isVisible() || this.status.isStopped()) {
            return;
        }

        // Enable the view
        if (model != null && model.isVisualizationEnabled() && !this.enabled) {
            this.enabled = true;
            this.viewContext = null;
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
     * 
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
     * Returns the controller
     * @return
     */
    protected Controller getController() {
        return this.controller;
    }
    
    /**
     * Returns the model
     * @return
     */
    protected Model getModel() {
        return this.model;
    }

    /**
     * Returns the parent composite
     */
    protected Composite getParent() {
        return this.parent;
                
    }
    
    /**
     * Overwrite this to return a progress provider
     * @return
     */
    protected ComponentStatusLabelProgressProvider getProgressProvider() {
        return null;
    }

    /**
     * Returns the target
     * @return
     */
    protected ModelPart getTarget() {
        return target;
    }

    /**
     * Is this view enabled
     * @return
     */
    protected boolean isEnabled() {
        return viewContext != null && enabled;
    }

    /**
     * Is a job running
     * @return
     */
    protected abstract boolean isRunning();           

    /**
     * Returns whether the view displays an empty result
     * @return
     */
    protected boolean isEmpty() {
       return this.status.isEmpty();
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
