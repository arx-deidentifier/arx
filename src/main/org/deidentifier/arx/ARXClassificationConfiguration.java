/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx;

import java.io.Serializable;

/**
 * An base class for configuration classes for classification experiments
 * 
 * @author Fabian Prasser
 */
public abstract class ARXClassificationConfiguration implements Serializable{

    /** SVUID */
    private static final long serialVersionUID = -8751059558718015927L;
    /** Deterministic */
    private boolean           deterministic    = true;
    /** Max records */
    private int               maxRecords       = 100000;
    /** Modified */
    private boolean           modified         = false;
    /** Folds */
    private int               numberOfFolds    = 10;
    /** Seed */
    private long              seed             = Integer.MAX_VALUE;
    /** Configuration */
    private int               vectorLength     = 1000;

    /**
     * @return the maxRecords to consider
     */
    public int getMaxRecords() {
        return maxRecords;
    }

    /**
     * @return the numberOfFolds
     */
    public int getNumFolds() {
        return numberOfFolds;
    }

    /**
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * @return the vectorLength
     */
    public int getVectorLength() {
        return vectorLength;
    }

    /**
     * Returns whether the process should be deterministic
     * @return
     */
    public boolean isDeterministic() {
        return deterministic;
    }
    
    /**
     * Is this configuration modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Sets whether the process should be deterministic
     * @param deterministic
     * @return
     */
    public ARXClassificationConfiguration setDeterministic(boolean deterministic) {
        if (this.deterministic != deterministic) {
            setModified();
            this.deterministic = deterministic;
        }
        return this;
    }
    
    /**
     * @param maxRecords the maxRecords to set
     */
    public ARXClassificationConfiguration setMaxRecords(int maxRecords) {
        if (maxRecords <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.maxRecords != maxRecords) {
            setModified();
            this.maxRecords = maxRecords;
        }
        return this;
    }
    
    /**
     * Sets modified
     */
    protected void setModified() {
        this.modified = true;
    }
    
    /**
     * @param numberOfFolds the numberOfFolds to set
     */
    public ARXClassificationConfiguration setNumFolds(int numberOfFolds) {
        if (numberOfFolds <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.numberOfFolds != numberOfFolds) {
            setModified();
            this.numberOfFolds = numberOfFolds;
        }
        return this;
    }
    
    /**
     * Seed for randomization. Set to Integer.MAX_VALUE for randomization.
     * @param seed the seed to set
     */
    public ARXClassificationConfiguration setSeed(int seed) {
        if (this.seed != seed) {
            setModified();
            this.seed = seed;
        }
        return this;
    }
    
    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
    }
    
    /**
     * @param vectorLength the vectorLength to set
     */
    public ARXClassificationConfiguration setVectorLength(int vectorLength) {
        if (vectorLength <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.vectorLength != vectorLength) {
            setModified();
            this.vectorLength = vectorLength;
        }
        return this;
    }
}
