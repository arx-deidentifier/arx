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
package org.deidentifier.arx;

import java.io.Serializable;

import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;

/**
 * An base class for configuration classes for classification experiments
 * 
 * @author Fabian Prasser
 */
public abstract class ARXClassificationConfiguration<T extends ARXClassificationConfiguration<?>> implements Serializable, Cloneable {

    /** SVUID */
    private static final long serialVersionUID = -8751059558718015927L;
    /**
     * Creates a new instance for logistic regression classifiers
     * @return
     */
    public static ClassificationConfigurationLogisticRegression createLogisticRegression() {
        return ClassificationConfigurationLogisticRegression.create();
    }
    /**
     * Creates a new instance for naive bayes classifiers
     * @return
     */
    public static ClassificationConfigurationNaiveBayes createNaiveBayes() {
        return ClassificationConfigurationNaiveBayes.create();
    }
    /**
     * Creates a new instance for random forest classifiers
     * @return
     */
    public static ClassificationConfigurationRandomForest createRandomForest() {
        return ClassificationConfigurationRandomForest.create();
    }
    
    /** Default value */
    public static final boolean DEFAULT_DETERMINISTIC   = true;
    /** Default value */
    public static final int     DEFAULT_MAX_RECORDS     = 100000;
    /** Default value */
    public static final int     DEFAULT_NUMBER_OF_FOLDS = 10;
    /** Default value */
    public static final int     DEFAULT_VECTOR_LENGTH   = 1000;

    /** Deterministic */
    private boolean             deterministic           = DEFAULT_DETERMINISTIC;
    /** Max records */
    private int                 maxRecords              = DEFAULT_MAX_RECORDS;
    /** Folds */
    private int                 numberOfFolds           = DEFAULT_NUMBER_OF_FOLDS;
    /** Seed */
    private long                seed                    = Integer.MAX_VALUE;
    /** Configuration */
    private int                 vectorLength            = DEFAULT_VECTOR_LENGTH;
    /** Modified */
    private boolean             modified                = false;

    /**
     * Creates a new instance with default settings
     */
    public ARXClassificationConfiguration() {
        // Empty by design
    }

    /**
     * Clone constructor
     * @param deterministic
     * @param maxRecords
     * @param numberOfFolds
     * @param seed
     * @param vectorLength
     */
    protected ARXClassificationConfiguration(boolean deterministic, int maxRecords, int numberOfFolds, long seed, int vectorLength) {
        this.deterministic = deterministic;
        this.maxRecords = maxRecords;
        this.numberOfFolds = numberOfFolds;
        this.seed = seed;
        this.vectorLength = vectorLength;
    }

    @Override
    public abstract ARXClassificationConfiguration<T> clone();

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
     * Parses another configuration
     * @param config
     */
    public void parse(ARXClassificationConfiguration<?> config) {
        this.setDeterministic(config.deterministic);
        this.setMaxRecords(config.maxRecords);
        this.setNumFolds(config.numberOfFolds);
        this.setSeed((int)config.seed);
        this.setVectorLength(config.vectorLength);
    }
    
    /**
     * Sets whether the process should be deterministic
     * @param deterministic
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setDeterministic(boolean deterministic) {
        if (this.deterministic != deterministic) {
            setModified();
            this.deterministic = deterministic;
        }
        return (T)this;
    }
    
    /**
     * @param maxRecords the maxRecords to set
     */
    @SuppressWarnings("unchecked")
    public T setMaxRecords(int maxRecords) {
        if (maxRecords <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.maxRecords != maxRecords) {
            setModified();
            this.maxRecords = maxRecords;
        }
        return (T)this;
    }
    
    /**
     * Sets modified
     */
    public void setModified() {
        this.modified = true;
    }
    
    /**
     * @param numberOfFolds the numberOfFolds to set
     */
    @SuppressWarnings("unchecked")
    public T setNumFolds(int numberOfFolds) {
        if (numberOfFolds <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.numberOfFolds != numberOfFolds) {
            setModified();
            this.numberOfFolds = numberOfFolds;
        }
        return (T)this;
    }
    
    /**
     * Seed for randomization. Set to Integer.MAX_VALUE for randomization.
     * @param seed the seed to set
     */
    @SuppressWarnings("unchecked")
    public T setSeed(int seed) {
        if (this.seed != seed) {
            setModified();
            this.seed = seed;
        }
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T setVectorLength(int vectorLength) {
        if (vectorLength <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        if (this.vectorLength != vectorLength) {
            setModified();
            this.vectorLength = vectorLength;
        }
        return (T)this;
    }
}
