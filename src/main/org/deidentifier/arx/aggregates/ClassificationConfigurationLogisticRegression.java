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
package org.deidentifier.arx.aggregates;

import java.io.Serializable;

import org.deidentifier.arx.ARXClassificationConfiguration;

/**
 * Configuration for logistic regression
 * @author Fabian Prasser
 */
public class ClassificationConfigurationLogisticRegression extends ARXClassificationConfiguration<ClassificationConfigurationLogisticRegression> implements Serializable, Cloneable {

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
    public static ClassificationConfigurationLogisticRegression create() {
        return new ClassificationConfigurationLogisticRegression();
    }
    
    /** Default value */
    public static final double        DEFAULT_ALPHA          = 1 - 1.0e-3;
    /** Default value */
    public static final double        DEFAULT_DECAY_EXPONENT = -0.5d;
    /** Default value */
    public static final double        DEFAULT_LAMBDA         = 1.0e-5;
    /** Default value */
    public static final double        DEFAULT_LEARNING_RATE  = 1d;
    /** Default value */
    public static final int           DEFAULT_STEP_OFFSET    = 10000;
    /** Default value */
    public static final PriorFunction DEFAULT_PRIOR          = PriorFunction.L1;

    /** Configuration */
    private double                    alpha                  = DEFAULT_ALPHA;
    /** -1 equals even weighting of all examples, 0 means only use exponential annealing */
    private double                    decayExponent          = DEFAULT_DECAY_EXPONENT;
    /** Configuration */
    private double                    lambda                 = DEFAULT_LAMBDA;
    /** Configuration */
    private double                    learningRate           = DEFAULT_LEARNING_RATE;
    /** Configuration */
    private int                       stepOffset             = DEFAULT_STEP_OFFSET;
    /** Configuration. TODO: We needed to replicate this here for backwards compatibility */
    private int                       vectorLength           = 1000;
    /** Max records TODO: We needed to replicate this here for backwards compatibility */
    private int                       maxRecords             = 100000;
    /** Seed TODO: We needed to replicate this here for backwards compatibility */
    private int                       seed                   = Integer.MAX_VALUE;
    /** Folds TODO: We needed to replicate this here for backwards compatibility */
    private int                       numberOfFolds          = 10;
    /** Deterministic TODO: We needed to replicate this here for backwards compatibility */
    private boolean                   deterministic          = true;
    /** Configuration */
    private PriorFunction             prior                  = DEFAULT_PRIOR;

    /**
     * Constructor
     */
    private ClassificationConfigurationLogisticRegression(){
        // Empty by design
    }
    
    /**
     * Clone constructor
     * @param alpha
     * @param decayExponent
     * @param lambda
     * @param learningRate
     * @param stepOffset
     * @param vectorLength
     * @param maxRecords
     * @param seed
     * @param numberOfFolds
     * @param deterministic
     * @param prior
     */
    protected ClassificationConfigurationLogisticRegression(double alpha,
                                                            double decayExponent,
                                                            double lambda,
                                                            double learningRate,
                                                            int stepOffset,
                                                            int vectorLength,
                                                            int maxRecords,
                                                            int seed,
                                                            int numberOfFolds,
                                                            boolean deterministic,
                                                            PriorFunction prior) {
        super(deterministic, maxRecords, numberOfFolds, seed, vectorLength);
        this.alpha = alpha;
        this.decayExponent = decayExponent;
        this.lambda = lambda;
        this.learningRate = learningRate;
        this.stepOffset = stepOffset;
        this.vectorLength = vectorLength;
        this.maxRecords = maxRecords;
        this.seed = seed;
        this.numberOfFolds = numberOfFolds;
        this.deterministic = deterministic;
        this.prior = prior;
    }

    @Override
    public ClassificationConfigurationLogisticRegression clone() {
        return new ClassificationConfigurationLogisticRegression(alpha,
                                                                 decayExponent,
                                                                 lambda,
                                                                 learningRate,
                                                                 stepOffset,
                                                                 vectorLength,
                                                                 maxRecords,
                                                                 seed,
                                                                 numberOfFolds,
                                                                 deterministic,
                                                                 prior);
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
    public long getSeed() {
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

    @Override
    public void parse(ARXClassificationConfiguration<?> config) {
        super.parse(config);
        if (config instanceof ClassificationConfigurationLogisticRegression) {
            ClassificationConfigurationLogisticRegression iconfig = (ClassificationConfigurationLogisticRegression)config;
            this.setAlpha(iconfig.alpha);
            this.setDecayExponent(iconfig.decayExponent);
            this.setLambda(iconfig.lambda);
            this.setLearningRate(iconfig.learningRate);
            this.setPriorFunction(iconfig.prior);
            this.setStepOffset(iconfig.stepOffset);
        }
    }

    /**
     * @param alpha the alpha to set
     */
    public ClassificationConfigurationLogisticRegression setAlpha(double alpha) {
        if (this.alpha != alpha) {
            setModified();
            this.alpha = alpha;
        }
        return this;
    }

    /**
     * @param decayExponent the decayExponent to set
     */
    public ClassificationConfigurationLogisticRegression setDecayExponent(double decayExponent) {
        if (this.decayExponent != decayExponent) {
            setModified();
            this.decayExponent = decayExponent;
        }
        return this;
    }

    /**
     * Sets whether the process should be deterministic
     * @param deterministic
     * @return
     */
    public ClassificationConfigurationLogisticRegression setDeterministic(boolean deterministic) {
        if (this.deterministic != deterministic) {
            setModified();
            this.deterministic = deterministic;
        }
        return this;
    }

    /**
     * @param lambda the lambda to set
     */
    public ClassificationConfigurationLogisticRegression setLambda(double lambda) {
        if (this.lambda != lambda) {
            setModified();
            this.lambda = lambda;
        }
        return this;
    }

    /**
     * @param learningRate the learningRate to set
     */
    public ClassificationConfigurationLogisticRegression setLearningRate(double learningRate) {
        if (this.learningRate != learningRate) {
            setModified();
            this.learningRate = learningRate;
        }
        return this;
    }

    /**
     * @param maxRecords the maxRecords to set
     */
    public ClassificationConfigurationLogisticRegression setMaxRecords(int maxRecords) {
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
    public ClassificationConfigurationLogisticRegression setNumFolds(int numberOfFolds) {
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
     * @param priorFunction the priorFunction to set
     */
    public ClassificationConfigurationLogisticRegression setPriorFunction(PriorFunction priorFunction) {
        if (this.prior != priorFunction) {
            setModified();
            this.prior = priorFunction;
        }
        return this;
    }

    /**
     * Seed for randomization. Set to Integer.MAX_VALUE for randomization.
     * @param seed the seed to set
     */
    public ClassificationConfigurationLogisticRegression setSeed(int seed) {
        if (this.seed != seed) {
            setModified();
            this.seed = seed;
        }
        return this;
    }

    /**
     * @param stepOffset the stepOffset to set
     */
    public ClassificationConfigurationLogisticRegression setStepOffset(int stepOffset) {
        if (this.stepOffset != stepOffset) {
            setModified();
            this.stepOffset = stepOffset;
        }
        return this;
    }

    /**
     * @param vectorLength the vectorLength to set
     */
    public ClassificationConfigurationLogisticRegression setVectorLength(int vectorLength) {
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