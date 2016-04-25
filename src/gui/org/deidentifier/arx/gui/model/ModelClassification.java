/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
 * This class represents a model
 *
 * @author Fabian Prasser
 */
public class ModelClassification implements Serializable {

    /** SVUID */
    private static final long serialVersionUID         = 5361564507029617616L;

    /** Modified */
    private boolean           modified                 = false;
    /** Max records */
    private int               classificationMaxRecords = 100000;
    /** Seed */
    private Integer           classificationSeed       = Integer.MAX_VALUE;
    /** Folds*/
    private Integer           numberOfFolds            = 10;
    
    /**
     * Returns the max records for classification
     * @return
     */
    public Integer getMaximalNumberOfRecords() {
        return this.classificationMaxRecords;
        
    }
    
    /**
     * Returns the seed for classification
     * @return
     */
    public Integer getSeed() {
        return this.classificationSeed;
    }

    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Sets the max records for classification
     * @param records
     */
    public void setMaximalNumberOfRecords(int records) {
        this.classificationMaxRecords = records;
        this.modified = true;
    }

    /**
     * Sets the seed. Set to Integer.MAX_VALUE for randomization
     * @param seed
     */
    public void setSeed(Integer seed) {
        this.classificationSeed = seed;
        this.modified = true;
    }
    
    

    /**
     * Gets the number of folds
     * @return the numberOfFolds
     */
    public int getNumberOfFolds() {
        if (numberOfFolds == null) {
            numberOfFolds = 10;
        }
        return numberOfFolds;
    }

    /**
     * Sets the number of folds
     * @param numberOfFolds the numberOfFolds to set
     */
    public void setNumberOfFolds(Integer numberOfFolds) {
        this.modified = true;
        this.numberOfFolds = numberOfFolds;
    }

    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
    }
}
