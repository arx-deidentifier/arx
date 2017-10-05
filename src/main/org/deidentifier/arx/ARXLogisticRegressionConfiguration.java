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
 * Configuration for logistic regression
 * @author Fabian Prasser
 */
public class ARXLogisticRegressionConfiguration extends ARXClassificationConfiguration implements Serializable {

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
     * @return the priorFunction
     */
    public PriorFunction getPriorFunction() {
        return prior;
    }
    
    /**
     * @return the stepOffset
     */
    public int getStepOffset() {
        return stepOffset;
    }

    /**
     * @param alpha the alpha to set
     */
    public ARXLogisticRegressionConfiguration setAlpha(double alpha) {
        if (this.alpha != alpha) {
            setModified();
            this.alpha = alpha;
        }
        return this;
    }

    /**
     * @param decayExponent the decayExponent to set
     */
    public ARXLogisticRegressionConfiguration setDecayExponent(double decayExponent) {
        if (this.decayExponent != decayExponent) {
            setModified();
            this.decayExponent = decayExponent;
        }
        return this;
    }

    /**
     * @param lambda the lambda to set
     */
    public ARXLogisticRegressionConfiguration setLambda(double lambda) {
        if (this.lambda != lambda) {
            setModified();
            this.lambda = lambda;
        }
        return this;
    }

    /**
     * @param learningRate the learningRate to set
     */
    public ARXLogisticRegressionConfiguration setLearningRate(double learningRate) {
        if (this.learningRate != learningRate) {
            setModified();
            this.learningRate = learningRate;
        }
        return this;
    }

    /**
     * @param priorFunction the priorFunction to set
     */
    public ARXLogisticRegressionConfiguration setPriorFunction(PriorFunction priorFunction) {
        if (this.prior != priorFunction) {
            setModified();
            this.prior = priorFunction;
        }
        return this;
    }

    /**
     * @param stepOffset the stepOffset to set
     */
    public ARXLogisticRegressionConfiguration setStepOffset(int stepOffset) {
        if (this.stepOffset != stepOffset) {
            setModified();
            this.stepOffset = stepOffset;
        }
        return this;
    }
}
