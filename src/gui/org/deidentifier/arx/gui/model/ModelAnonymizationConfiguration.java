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

import java.io.Serializable;

/**
 * Anonymization configuration
 * 
 * @author Fabian Prasser
 */
public class ModelAnonymizationConfiguration implements Serializable {

    /**
     * Search type
     * @author Fabian Prasser
     */
    public static enum SearchType {
        OPTIMAL,
        STEP_LIMIT,
        TIME_LIMIT
    }
    /**
     * Transformation type
     * @author Fabian Prasser
     */
    public static enum TransformationType {
        GLOBAL,
        LOCAL
    }

    /** SVUID */
    private static final long                                  serialVersionUID   = -1135902359268189624L;

    /** Model */
    private Model                                              model;
    /** Result */
    private ModelAnonymizationConfiguration.SearchType         searchType         = SearchType.OPTIMAL;
    /** Result */
    private ModelAnonymizationConfiguration.TransformationType transformationType = TransformationType.GLOBAL;

    /**
     * Creates a new instance
     * @param model
     */
    ModelAnonymizationConfiguration(Model model) {
        this.model = model;
    }
    
    /**
     * Search step limit for SearchType.STEP_LIMIT
     * 
     * @return the heuristicSearchStepLimit
     */
    public int getHeuristicSearchStepLimit() {
        return model.getHeuristicSearchStepLimit();
    }
    /**
     * Search time limit for SearchType.TIME_LIMIT
     * 
     * @return the heuristicSearchTimeLimit
     */
    public double getHeuristicSearchTimeLimit() {
        return (double)model.getHeuristicSearchTimeLimit() / 1000d;
    }
    
    /**
     * Number of iterations for TransformationType.LOCAL
     * 
     * @return the numIterations
     */
    public int getNumIterations() {
        return model.getLocalRecodingModel().getNumIterations();
    }
    
    /**
     * Returns the search type
     * 
     * @return the searchType
     */
    public ModelAnonymizationConfiguration.SearchType getSearchType() {
        return searchType;
    }
    
    /**
     * Returns the transformation type
     * 
     * @return the transformationType
     */
    public ModelAnonymizationConfiguration.TransformationType getTransformationType() {
        return transformationType;
    }
    
    /**
     * @param heuristicSearchStepLimit the heuristicSearchStepLimit to set
     */
    public void setHeuristicSearchStepLimit(int heuristicSearchStepLimit) {
        this.model.setHeuristicSearchStepLimit(heuristicSearchStepLimit);
    }
    
    /**
     * @param heuristicSearchTimeLimit the heuristicSearchTimeLimit to set
     */
    public void setHeuristicSearchTimeLimit(double heuristicSearchTimeLimit) {
        model.setHeuristicSearchTimeLimit((int)(heuristicSearchTimeLimit * 1000d));
    }
    
    /**
     * @param numIterations the numIterations to set
     */
    public void setNumIterations(int numIterations) {
        model.getLocalRecodingModel().setNumIterations(numIterations);
    }
    
    /**
     * @param searchType the searchType to set
     */
    public void setSearchType(ModelAnonymizationConfiguration.SearchType searchType) {
        this.searchType = searchType;
    }
    
    /**
     * @param transformationType the transformationType to set
     */
    public void setTransformationType(ModelAnonymizationConfiguration.TransformationType transformationType) {
        this.transformationType = transformationType;
    }
}