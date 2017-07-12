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
 * Configuration for naive bayes classification
 * @author Fabian Prasser
 */
public class ARXNaiveBayesConfiguration extends ARXClassificationConfiguration implements Serializable {

    /** 
     * Type of bayes classifier
     */
    public static enum Type {
        MULTINOMIAL,
        BERNOULLI
    }

    /** SVUID */
    private static final long serialVersionUID = 5899021797968063868L;

    /**
     * Returns a new instance
     * @return
     */
    public static ARXNaiveBayesConfiguration create() {
        return new ARXNaiveBayesConfiguration();
    }

    /** Type */
    private Type    type          = Type.BERNOULLI;
    /** Prior count */
    private double  sigma         = 1.0d;
    /** Configuration */
    private int     vectorLength  = 1000;

    /** Max records */
    private int     maxRecords    = 100000;
    /** Seed */
    private long    seed          = Integer.MAX_VALUE;
    /** Folds */
    private int     numberOfFolds = 10;
    /** Deterministic */
    private boolean deterministic = true;

    /**
     * Constructor
     */
    private ARXNaiveBayesConfiguration(){
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
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Gets the prior count of add-k smoothing of evidence.
     * @return the sigma
     */
    public double getSigma() {
        return sigma;
    }
    
    /**
     * Type
     * @return the type
     */
    public Type getType() {
        return type;
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
    public ARXNaiveBayesConfiguration setDeterministic(boolean deterministic) {
        if (this.deterministic != deterministic) {
            setModified();
            this.deterministic = deterministic;
        }
        return this;
    }
    
    /**
     * @param maxRecords the maxRecords to set
     */
    public ARXNaiveBayesConfiguration setMaxRecords(int maxRecords) {
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
    public ARXNaiveBayesConfiguration setNumFolds(int numberOfFolds) {
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
    public ARXNaiveBayesConfiguration setSeed(int seed) {
        if (this.seed != seed) {
            setModified();
            this.seed = seed;
        }
        return this;
    }

    /**
     * Sets the prior count of add-k smoothing of evidence.
     * @param sigma the sigma to set
     */
    public ARXNaiveBayesConfiguration setSigma(double sigma) {
        if (sigma < 0) {
            throw new IllegalArgumentException("Invalid add-k smoothing parameter: " + sigma);
        }
        if (this.sigma != sigma) {
            setModified();
            this.sigma = sigma;
        }
        return this;
    }

    /**
     * Type
     * @param type the type to set
     */
    public ARXNaiveBayesConfiguration setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Invalid type parameter: " + type);
        }
        if (this.type != type) {
            setModified();
            this.type = type;
        }
        return this;
    }

    /**
     * @param vectorLength the vectorLength to set
     */
    public ARXNaiveBayesConfiguration setVectorLength(int vectorLength) {
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
