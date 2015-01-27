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

package org.deidentifier.arx.gui.model;

/**
 * This class implements an event for model changes.
 *
 * @author Fabian Prasser
 */
public class ModelEvent {
    
    /**
     * The part of the model that has changed.
     *
     * @author Fabian Prasser
     */
    public static enum ModelPart {
        
        /**  TODO */
        SELECTED_ATTRIBUTE,
        
        /**  TODO */
        INPUT,
        
        /**  TODO */
        OUTPUT,
        
        /**  TODO */
        ATTRIBUTE_TYPE,
        
        /**  TODO */
        RESULT,
        
        /**  TODO */
        DATA_TYPE,
        
        /**  TODO */
        ALGORITHM,
        
        /**  TODO */
        METRIC,
        
        /**  TODO */
        MAX_OUTLIERS,
        
        /**  TODO */
        FILTER,
        
        /**  TODO */
        SELECTED_NODE,
        
        /**  TODO */
        MODEL,
        
        /**  TODO */
        CLIPBOARD,
        
        /**  TODO */
        HIERARCHY,
        
        /**  TODO */
        CRITERION_DEFINITION,
        
        /**  TODO */
        RESEARCH_SUBSET,
        
        /**  TODO */
        VIEW_CONFIG,
        
        /**  TODO */
        VISUALIZATION
    }

    /** The part of the model that has changed. */
    public final ModelPart   part;
    
    /** The associated data, if any. */
    public final Object      data;
    
    /** The sender. */
    public final Object      source;

    /**
     * Creates a new instance.
     *
     * @param source
     * @param target
     * @param data
     */
    public ModelEvent(final Object source,
                      final ModelPart target,
                      final Object data) {
        this.part = target;
        this.data = data;
        this.source = source;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String sourceLabel = "NULL";
        if (source != null) sourceLabel = source.getClass().getSimpleName()+"@" + source.hashCode();
        String dataLabel = "NULL";
        if (data != null) dataLabel = data.getClass().getSimpleName()+"@" + data.hashCode();
        return "[part=" + part + ", source=" + sourceLabel + ", data=" + dataLabel + "]";
    }
}