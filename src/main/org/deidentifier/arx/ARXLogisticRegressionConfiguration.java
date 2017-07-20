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
package org.deidentifier.arx;

import java.io.Serializable;

/**
 * Configuration for logistic regression
 * @author Fabian Prasser
 */
public class ARXLogisticRegressionConfiguration implements Serializable {

    /** 
     * Prior function for regularization
     */
    public static enum PriorFunction {
        L1,
        L2,
        UNIFORM,
        ELASTIC_BAND,
    }

    /** SVUID */
    private static final long serialVersionUID = -7432423626032273246L;
    /**
     * Returns a new instance
     * @return
     */
    public static ARXLogisticRegressionConfiguration create() {
        return new ARXLogisticRegressionConfiguration();
    }

    /** Configuration */
    private double        alpha         = 1d;
    /** Configuration */
    private double        decayExponent = 0.2d;
    /** Configuration */
    private double        lambda        = 0.000001d;
    /** Configuration */
    private double        learningRate  = 1d;
    /** Configuration */
    private int           stepOffset    = 10000;
    /** Configuration */
    private int           vectorLength  = 1000;
    /** Max records */
    private int           maxRecords    = 100000;
    /** Seed */
    private int           seed          = Integer.MAX_VALUE;
    /** Folds */
    private int           numberOfFolds = 10;
    /** Deterministic */
    private boolean       deterministic = true;
    /** Configuration */
    private PriorFunction prior         = PriorFunction.L1;

    /**
     * Constructor
     */
    private ARXLogisticRegressionConfiguration(){
        // Empty by design
    }

    /**
     * @return the alpha
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * @return the decayExponent
     */
    public double getDecayExponent() {
        return decayExponent;
    }

    /**
     * @return the lambda
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * @return the learningRate
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * @return the maxRecords
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
     * @return the priorFunction
     */
    public PriorFunction getPriorFunction() {
        return prior;
    }
    
    /**
     * @return the seed
     */
    public int getSeed() {
        return seed;
    }

    /**
     * @return the stepOffset
     */
    public int getStepOffset() {
        return stepOffset;
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
     * @param alpha the alpha to set
     */
    public ARXLogisticRegressionConfiguration setAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * @param decayExponent the decayExponent to set
     */
    public ARXLogisticRegressionConfiguration setDecayExponent(double decayExponent) {
        this.decayExponent = decayExponent;
        return this;
    }

    /**
     * Sets whether the process should be deterministic
     * @param deterministic
     * @return
     */
    public ARXLogisticRegressionConfiguration setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
        return this;
    }

    /**
     * @param lambda the lambda to set
     */
    public ARXLogisticRegressionConfiguration setLambda(double lambda) {
        this.lambda = lambda;
        return this;
    }

    /**
     * @param learningRate the learningRate to set
     */
    public ARXLogisticRegressionConfiguration setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    /**
     * @param maxRecords the maxRecords to set
     */
    public ARXLogisticRegressionConfiguration setMaxRecords(int maxRecords) {
        if (maxRecords <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        this.maxRecords = maxRecords;
        return this;
    }

    /**
     * @param numberOfFolds the numberOfFolds to set
     */
    public ARXLogisticRegressionConfiguration setNumFolds(int numberOfFolds) {
        if (numberOfFolds <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        this.numberOfFolds = numberOfFolds;
        return this;
    }

    /**
     * @param priorFunction the priorFunction to set
     */
    public ARXLogisticRegressionConfiguration setPriorFunction(PriorFunction priorFunction) {
        this.prior = priorFunction;
        return this;
    }

    /**
     * Seed for randomization. Set to Integer.MAX_VALUE for randomization.
     * @param seed the seed to set
     */
    public ARXLogisticRegressionConfiguration setSeed(int seed) {
        this.seed = seed;
        return this;
    }

    /**
     * @param stepOffset the stepOffset to set
     */
    public ARXLogisticRegressionConfiguration setStepOffset(int stepOffset) {
        this.stepOffset = stepOffset;
        return this;
    }

    /**
     * @param vectorLength the vectorLength to set
     */
    public ARXLogisticRegressionConfiguration setVectorLength(int vectorLength) {
        if (vectorLength <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        this.vectorLength = vectorLength;
        return this;
    }
}
