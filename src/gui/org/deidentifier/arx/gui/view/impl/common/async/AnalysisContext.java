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

package org.deidentifier.arx.gui.view.impl.common.async;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;

/**
 * This class implements a base class for views that show statistic properties of the data.
 *
 * @author Fabian Prasser
 */
public class AnalysisContext {

    /** The target (input or output). */
    private ModelPart target;

    /** The model. */
    private Model     model;

    /**
     * Returns the current context, consisting of a consistent combination of
     * a configuration and a data handle.
     *
     * @return
     */
    public AnalysisData getData(){
        
        // Prepare
        DataHandle handle = null;
        ModelConfiguration config = null;
        DataDefinition definition = null;
        
        // Check
        if (model==null) return null;
        
        // If input 
        if (target == ModelPart.INPUT){
            
            // If output available
            if (model.getOutputConfig() != null && 
                model.getOutputConfig().getInput() != null && 
                model.getOutput() != null){
                
                config = model.getOutputConfig();
                definition = model.getOutputDefinition();
            } else {
                
                config = model.getInputConfig();
                definition = model.getInputDefinition();
            }
            if (config.getInput() == null) return null;
            handle = config.getInput().getHandle();
           
        // If output
        } else {
            definition = model.getOutputDefinition();
            config = model.getOutputConfig();
            handle = model.getOutput();
        }
        
        // If subset view enabled
        if (model.getViewConfig().isSubset() && 
            model.getOutputConfig() != null &&
            model.getOutputConfig().getConfig() != null &&
            handle != null) {
            handle = handle.getView();
        }
        
        // Return
        return new AnalysisData(config, handle, definition);
    }

    /**
     * Returns a generalization hierarchy for the attribute, if available.
     *
     * @param context
     * @param attribute
     * @return
     */
    public Hierarchy getHierarchy(AnalysisData context, String attribute) {

        // We only accept sanitized input
        if (context.config == null || 
        	context.config == model.getInputConfig() ||
        	model.getOutputDefinition() == null) {
        	return null;
        }
        
        // First, check hierarchies for attribute
        Hierarchy hierarchy = model.getOutputDefinition().getHierarchyObject(attribute);
        if (hierarchy != null){
            return hierarchy;
        }
        
        // This is probably not needed anymore, but we leave it just in case...
        // Second, check for hierarchies associated with t-closeness
        for (HierarchicalDistanceTCloseness t : context.config.getCriteria(HierarchicalDistanceTCloseness.class)){
            if (t.getAttribute().equals(attribute)) {
                return t.getHierarchy();
            }
        }
        
        // Nothing found
        return null;
    }

    /**
     * Returns the model.
     *
     * @return
     */
    public Model getModel() {
        return model;
    }

    /**
     * Returns a population model for the given context
     *
     * @return
     */
    public ARXPopulationModel getPopulationModel() {

        // First, try to return a model associated with an output criterion
        if (model.getOutputConfig() != null) {
            for (PrivacyCriterion c : model.getOutputConfig().getCriteria()) {
                if (c.getPopulationModel() != null) {
                    return c.getPopulationModel();
                }
            }
        }
        
        // Fall back to the input model
        return model.getRiskModel().getPopulationModel();
    }

    /**
     * Gets the target.
     *
     * @return
     */
    public ModelPart getTarget() {
        return target;
    }

    /**
     * Sets the model.
     *
     * @param model
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * Sets the target.
     *
     * @param target
     */
    public void setTarget(ModelPart target) {
        this.target = target;
    }
}
