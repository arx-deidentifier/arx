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
    /** Modified */
    private boolean           modified         = false;
    /** Seed */
    private int     seed          = Integer.MAX_VALUE;

    /** Maximal number of records to consider*/
    public abstract int getMaxRecords();

    /** Number of folds*/
    public abstract int getNumFolds();

    /**
     * @return the seed for drawing records
     */
    public int getSeed() {
        return seed;
    }

    /** Deterministic*/
    public abstract boolean isDeterministic();
    
    /**
     * Is this configuration modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Sets modified
     */
    protected void setModified() {
        this.modified = true;
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
}
