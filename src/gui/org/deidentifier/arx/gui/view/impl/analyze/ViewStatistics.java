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

package org.deidentifier.arx.gui.view.impl.analyze;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatus;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a base class for displaying contingency tables.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class ViewStatistics<T extends AnalysisContextVisualization> implements IView {

    /** Our users are patient. */
    public static final int MINIMAL_WORKING_TIME = 500;
    
    /** Internal stuff. */
    private AnalysisContext       context  = new AnalysisContext();
    
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
    private T viewContext;
    
	/**
     * Creates a new density plot.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatistics(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.VISUALIZATION, this);
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
        if (event.part == ModelPart.OUTPUT ||
            event.part == target ||
            event.part == ModelPart.SELECTED_ATTRIBUTE ||
            event.part == ModelPart.VIEW_CONFIG) {
            
            this.viewContext = null;
            this.update();
            return;
        }
        
        // Potentially invalidate
        if (event.part == ModelPart.DATA_TYPE ||
            event.part == ModelPart.ATTRIBUTE_TYPE) {
            
            if (model == null || 
                viewContext == null ||
                viewContext.isAttributeSelected(model.getSelectedAttribute())) {
                
                this.viewContext = null;
                this.update();
                return;
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
           event.part == ModelPart.SELECTED_ATTRIBUTE ||
           event.part == ModelPart.ATTRIBUTE_TYPE ||
           event.part == ModelPart.VIEW_CONFIG ||
           event.part == ModelPart.VISUALIZATION) {
            
            this.update();
        }
    }

    /**
     * Redraws the plot.
     */
    private void update() {

        if (!this.status.isVisible() || !model.isVisualizationEnabled()){
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
}
