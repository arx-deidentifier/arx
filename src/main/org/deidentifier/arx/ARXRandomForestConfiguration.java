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
 * Configuration for Random Forest classifiers
 * @author Fabian Prasser
 */
public class ARXRandomForestConfiguration extends ARXClassificationConfiguration implements Serializable {

    /** SVUID */
    private static final long serialVersionUID = 7928077920858462047L;

    /**
     * Returns a new instance
     * @return
     */
    public static ARXRandomForestConfiguration create() {
        return new ARXRandomForestConfiguration();
    }

    /** Number of trees */
    private int     numberOfTrees = 10;

    /** Configuration */
    private int     vectorLength  = 1000;
    /** Max records */
    private int     maxRecords    = 100000;
    /** Folds */
    private int     numberOfFolds = 10;
    /** Deterministic */
    private boolean deterministic = true;

    /**
     * Constructor
     */
    private ARXRandomForestConfiguration(){
        // Empty by design
    }
    
    @Override
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
     * Sets whether the process should be deterministic
     * @param deterministic
     * @return
     */
    public ARXRandomForestConfiguration setDeterministic(boolean deterministic) {
        if (this.deterministic != deterministic) {
            setModified();
            this.deterministic = deterministic;
        }
        return this;
    }
    
    /**
     * @param maxRecords the maxRecords to set
     */
    public ARXRandomForestConfiguration setMaxRecords(int maxRecords) {
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
     * @param numberOfFolds the numberOfFolds to set
     */
    public ARXRandomForestConfiguration setNumFolds(int numberOfFolds) {
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
     * @param vectorLength the vectorLength to set
     */
    public ARXRandomForestConfiguration setVectorLength(int vectorLength) {
        if (vectorLength <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.vectorLength != vectorLength) {
            setModified();
            this.vectorLength = vectorLength;
        }
        return this;
    }

    /**
     * @return the numberOfTrees
     */
    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    /**
     * @param numberOfTrees the numberOfTrees to set
     */
    public ARXRandomForestConfiguration setNumberOfTrees(int numberOfTrees) {
        if (this.numberOfTrees != numberOfTrees) {
            setModified();
            this.numberOfTrees = numberOfTrees;
        }
        return this;
    }
}
