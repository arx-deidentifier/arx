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
        
        /**  SELECTED_ATTRIBUTE */
        SELECTED_ATTRIBUTE,
        
        /**  INPUT */
        INPUT,
        
        /**  OUTPUT */
        OUTPUT,
        
        /**  ATTRIBUTE_TYPE */
        ATTRIBUTE_TYPE,
        
        /**  RESULT */
        RESULT,
        
        /**  DATA_TYPE */
        DATA_TYPE,
        
        /**  ALGORITHM */
        ALGORITHM,
        
        /**  METRIC */
        METRIC,
        
        /**  MAX_OUTLIERS */
        MAX_OUTLIERS,
        
        /**  FILTER */
        FILTER,
        
        /**  SELECTED_NODE */
        SELECTED_NODE,
        
        /**  MODEL */
        MODEL,
        
        /**  CLIPBOARD */
        CLIPBOARD,
        
        /**  HIERARCHY */
        HIERARCHY,
        
        /**  CRITERION_DEFINITION */
        CRITERION_DEFINITION,
        
        /**  RESEARCH_SUBSET */
        RESEARCH_SUBSET,
        
        /**  SELECTED_VIEW_CONFIG */
        SELECTED_VIEW_CONFIG,
        
        /**  SELECTED_UTILITY_VISUALIZATION */
        SELECTED_UTILITY_VISUALIZATION,
        
        /**  ATTRIBUTE_VALUE */
        ATTRIBUTE_VALUE,

        /**  SELECTED_PERSPECTIVE */
        SELECTED_PERSPECTIVE,

        /**  POPULATION_MODEL */
        POPULATION_MODEL,

        /**  SELECTED_RISK_VISUALIZATION */
        SELECTED_RISK_VISUALIZATION,
        
        /**  SELECTED_QUASI_IDENTIFIERS */
        SELECTED_QUASI_IDENTIFIERS,

        /**  EXPAND */
        EXPAND,

        /**  CLASSIFICATION_CONFIGURATION */
        CLASSIFICATION_CONFIGURATION,
        
        /** RISK THRESHOLDS*/
        RISK_THRESHOLD_MAIN,
        
        /** RISK THRESHOLD*/
        RISK_THRESHOLD_DERIVED,
        
        /** G/S FACTOR*/
        GS_FACTOR,
        
        /** ATTRIBUTE WEIGHT*/
        ATTRIBUTE_WEIGHT,
        
        /** COST/BENEFIT MODEL*/
        COST_BENEFIT_MODEL,
        
        /** SELECTED_CLASS_VALUE */
        SELECTED_CLASS_VALUE,

        /** RESPONSE VARIABLES */
        RESPONSE_VARIABLES,

        /**  ATTRIBUTE_TYPE */
        ATTRIBUTE_TYPE_BULK_UPDATE,

        /** ATTRIBUTES VISUALIZATION*/
        SELECTED_ATTRIBUTES_VISUALIZATION
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

    @Override
    public String toString() {
        String sourceLabel = "NULL"; //$NON-NLS-1$
        if (source != null) sourceLabel = source.getClass().getSimpleName()+"@" + source.hashCode(); //$NON-NLS-1$
        String dataLabel = "NULL"; //$NON-NLS-1$
        if (data != null) dataLabel = data.getClass().getSimpleName()+"@" + data.hashCode(); //$NON-NLS-1$
        return "[part=" + part + ", source=" + sourceLabel + ", data=" + dataLabel + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
