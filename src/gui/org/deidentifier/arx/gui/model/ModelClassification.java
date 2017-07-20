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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;

import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration.PriorFunction;

/**
 * This class represents a model
 *
 * @author Fabian Prasser
 */
public class ModelClassification implements Serializable {

    /** SVUID */
    private static final long serialVersionUID         = 5361564507029617616L;

    /** Modified */
    private boolean                            modified         = false;
    /** Config*/
    private ARXLogisticRegressionConfiguration config           = null;

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getAlpha()
     */
    public double getAlpha() {
        return getConfig().getAlpha();
    }

    /**
     * Returns a config for ARX
     * @return
     */
    public ARXLogisticRegressionConfiguration getARXLogisticRegressionConfiguration() {
        return getConfig();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getDecayExponent()
     */
    public double getDecayExponent() {
        return getConfig().getDecayExponent();
    }
    
    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getLambda()
     */
    public double getLambda() {
        return getConfig().getLambda();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getLearningRate()
     */
    public double getLearningRate() {
        return getConfig().getLearningRate();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getMaxRecords()
     */
    public int getMaxRecords() {
        return getConfig().getMaxRecords();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getNumFolds()
     */
    public Integer getNumberOfFolds() {
        return getConfig().getNumFolds();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getPriorFunction()
     */
    public PriorFunction getPriorFunction() {
        return getConfig().getPriorFunction();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getSeed()
     */
    public Integer getSeed() {
        return getConfig().getSeed();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getStepOffset()
     */
    public int getStepOffset() {
        return getConfig().getStepOffset();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#getVectorLength()
     */
    public int getVectorLength() {
        return getConfig().getVectorLength();
    }

    /**
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#isDeterministic()
     */
    public boolean isDeterministic() {
        return getConfig().isDeterministic();
    }

    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @param alpha
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setAlpha(double)
     */
    public void setAlpha(double alpha) {
        setModified();
        getConfig().setAlpha(alpha);
    }

    /**
     * @param decayExponent
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setDecayExponent(double)
     */
    public void setDecayExponent(double decayExponent) {
        setModified();
        getConfig().setDecayExponent(decayExponent);
    }

    /**
     * @param stepOffset
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setDeterministic(boolean)
     */
    public void setDeterministic(boolean deterministic) {
        setModified();
        getConfig().setDeterministic(deterministic);
    }

    /**
     * @param lambda
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setLambda(double)
     */
    public void setLambda(double lambda) {
        setModified();
        getConfig().setLambda(lambda);
    }

    /**
     * @param learningRate
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setLearningRate(double)
     */
    public void setLearningRate(double learningRate) {
        setModified();
        getConfig().setLearningRate(learningRate);
    }

    /**
     * @param maxRecords
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setMaxRecords(int)
     */
    public void setMaxRecords(int maxRecords) {
        setModified();
        getConfig().setMaxRecords(maxRecords);
    }

    /**
     * @param numberOfFolds
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setNumFolds(java.lang.Integer)
     */
    public void setNumberOfFolds(Integer numberOfFolds) {
        setModified();
        getConfig().setNumFolds(numberOfFolds);
    }

    /**
     * @param priorFunction
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setPriorFunction(org.deidentifier.arx.ARXLogisticRegressionConfiguration.PriorFunction)
     */
    public void setPriorFunction(PriorFunction priorFunction) {
        setModified();
        getConfig().setPriorFunction(priorFunction);
    }

    /**
     * @param seed
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setSeed(java.lang.Integer)
     */
    public void setSeed(Integer seed) {
        setModified();
        getConfig().setSeed(seed);
    }

    /**
     * @param stepOffset
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setStepOffset(int)
     */
    public void setStepOffset(int stepOffset) {
        setModified();
        getConfig().setStepOffset(stepOffset);
    }
    
    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
    }
    
    /**
     * @param vectorLength
     * @return
     * @see org.deidentifier.arx.ARXLogisticRegressionConfiguration#setVectorLength(int)
     */
    public ARXLogisticRegressionConfiguration setVectorLength(int vectorLength) {
        setModified();
        return getConfig().setVectorLength(vectorLength);
    }

    /**
     * For backwards compatibility
     */
    private ARXLogisticRegressionConfiguration getConfig() {
        if (this.config == null) {
            this.config = ARXLogisticRegressionConfiguration.create();
        }
        return this.config;
    }

    /**
     * Sets modified
     */
    private void setModified() {
        this.modified = true;
    }
}
