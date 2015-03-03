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

package org.deidentifier.arx.gui.view.impl.analyze;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;

/**
 * This class implements a base class for views that show statistic properties of the data.
 *
 * @author Fabian Prasser
 */
public class AnalysisContext {
    
    /**
     * This class implements a context for drawing statistics.
     *
     * @author Fabian Prasser
     */
    public static class Context{
        
        /** The according config. */
        public final ModelConfiguration config;
        
        /** The according handle. */
        public final DataHandle handle;
        
        /**
         * Initial constructor.
         *
         * @param config
         * @param handle
         */
        private Context(ModelConfiguration config, DataHandle handle) {
            this.config = config;
            this.handle = handle;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result +
                     ((config == null) ? 0 : config.hashCode());
            result = prime * result +
                     ((handle == null) ? 0 : handle.hashCode());
            return result;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Context other = (Context) obj;
            if (config == null) {
                if (other.config != null) return false;
            } else if (!config.equals(other.config)) return false;
            if (handle == null) {
                if (other.handle != null) return false;
            } else if (!handle.equals(other.handle)) return false;
            return true;
        }
    }
    
    /** The target (input or output). */
    private ModelPart target;
    
    /** The model. */
    private Model model;

    /**
     * Returns the current context, consisting of a consistent combination of
     * a configuration and a data handle.
     *
     * @return
     */
    public Context getContext(){
        
        // Prepare
        DataHandle handle = null;
        ModelConfiguration config = null;
        
        // Check
        if (model==null) return null;
        
        // If input 
        if (target == ModelPart.INPUT){
            
            // If output available
            if (model.getOutputConfig() != null && 
                model.getOutputConfig().getInput() != null){
                config = model.getOutputConfig();
            } else {
                config = model.getInputConfig();
            }
            if (config.getInput() == null) return null;
            handle = config.getInput().getHandle();
           
        // If output
        } else {
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
        return new Context(config, handle);
    }

    /**
     * Returns a generalization hierarchy for the attribute, if available.
     *
     * @param context
     * @param attribute
     * @return
     */
    public Hierarchy getHierarchy(Context context, String attribute) {

        // We only accept sanitized input
        if (context.config == null || 
        	context.config == model.getInputConfig() ||
        	model.getOutputDefinition() == null) {
        	return null;
        }
        
        // First, check hierarchies for QIs
        AttributeType type = model.getOutputDefinition().getAttributeType(attribute);
        if (type instanceof Hierarchy){
            return (Hierarchy)type;
        }
        
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
     * Gets the target.
     *
     * @return
     */
    public ModelPart getTarget() {
        return target;
    }

    /**
     * Sets the target.
     *
     * @param target
     */
    public void setTarget(ModelPart target) {
        this.target = target;
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
     * Sets the model.
     *
     * @param model
     */
    public void setModel(Model model) {
        this.model = model;
    }
}
