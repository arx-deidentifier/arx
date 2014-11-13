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
