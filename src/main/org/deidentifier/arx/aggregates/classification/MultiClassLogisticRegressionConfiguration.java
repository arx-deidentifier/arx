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
package org.deidentifier.arx.aggregates.classification;

import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.PriorFunction;

/**
 * Configuration for logistic regression
 * @author Fabian Prasser
 */
public class MultiClassLogisticRegressionConfiguration {

    /** Configuration */
    private double        alpha         = 1d;
    /** Configuration */
    private double        decayExponent = 0.2d;
    /** Configuration */
    private double        lambda        = 0.000001d;
    /** Configuration */
    private double        learningRate  = 1d;
    /** Configuration */
    private PriorFunction priorFunction = new L1();
    /** Configuration */
    private int           stepOffset    = 10000;
    /** Configuration */
    private int           vectorLength  = 1000;

    /**
     * Constructor
     */
    public MultiClassLogisticRegressionConfiguration(){
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
        return priorFunction;
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
     * @param alpha the alpha to set
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * @param decayExponent the decayExponent to set
     */
    public void setDecayExponent(double decayExponent) {
        this.decayExponent = decayExponent;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * @param learningRate the learningRate to set
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    /**
     * @param priorFunction the priorFunction to set
     */
    public void setPriorFunction(PriorFunction priorFunction) {
        this.priorFunction = priorFunction;
    }

    /**
     * @param stepOffset the stepOffset to set
     */
    public void setStepOffset(int stepOffset) {
        this.stepOffset = stepOffset;
    }

    /**
     * @param vectorLength the vectorLength to set
     */
    public void setVectorLength(int vectorLength) {
        this.vectorLength = vectorLength;
    }
}
