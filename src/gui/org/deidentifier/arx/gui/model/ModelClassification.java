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
    /** Ignore suppressed records */
    private boolean           ignoreSuppressedRecords  = true;
    
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
     * Ignore suppressed records
     * @return
     */
    public boolean isIgnoreSuppressedRecords() {
        return ignoreSuppressedRecords;
    }

    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /** 
     * Ignore suppressed records
     * @param value
     */
    public void setIgnoreSuppressedRecords(boolean value) {
        if (value != this.ignoreSuppressedRecords) {
            this.modified = true;
        }
        this.ignoreSuppressedRecords = value;
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
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
    }
}
