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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.ARXNaiveBayesConfiguration;
import org.deidentifier.arx.ARXRandomForestConfiguration;
import org.deidentifier.arx.ARXSVMConfiguration;

/**
 * This class represents a model
 *
 * @author Fabian Prasser
 */
public class ModelClassification implements Serializable {

    /** SVUID */
    private static final long                  serialVersionUID   = 5361564507029617616L;

    /** Modified */
    private boolean                            modified           = false;
    /** Current configuration */
    private ARXClassificationConfiguration<?>  configCurrent      = null;
    /** Configuration logistic regression */
    private ARXLogisticRegressionConfiguration config             = null;
    /** Configuration naive bayes */
    private ARXNaiveBayesConfiguration         configNaiveBayes   = null;
    /** Configuration random forest */
    private ARXRandomForestConfiguration       configRandomForest = null;
    /** Configuration SVM */
    private ARXSVMConfiguration                configSVM          = null;
    /** Feature scaling */
    private ARXFeatureScaling                  featureScaling;

    /**
     * Returns the current classification configuration
     * @return
     */
    public ARXClassificationConfiguration<?> getCurrentConfiguration(){
        if (this.configCurrent == null) {
            this.configCurrent = getLogisticRegressionConfiguration();
        }
        return configCurrent;
    }

    /**
     * Returns the feature scaling
     * @return
     */
    public ARXFeatureScaling getFeatureScaling() {
        if (this.featureScaling == null) {
            this.featureScaling = ARXFeatureScaling.create();
        }
        return this.featureScaling;
    }
    
    /**
     * Returns a logistic regression configuration for ARX
     * @return
     */
    public ARXLogisticRegressionConfiguration getLogisticRegressionConfiguration() {
        if (this.config == null) {
            this.config = ARXLogisticRegressionConfiguration.create();
        }
        return this.config;
    }
    
    /**
     * Returns a naive bayes configuration for ARX
     * @return
     */
    public ARXNaiveBayesConfiguration getNaiveBayesConfiguration() {
        if (this.configNaiveBayes == null) {
            this.configNaiveBayes = ARXNaiveBayesConfiguration.create();
        }
        return this.configNaiveBayes;
    }

    /**
     * Returns a random forest configuration for ARX
     * @return
     */
    public ARXRandomForestConfiguration getRandomForestConfiguration() {
        if (this.configRandomForest == null) {
            this.configRandomForest = ARXRandomForestConfiguration.create();
        }
        return this.configRandomForest;
    }
    
    /**
     * Returns a SVM configuration for ARX
     * @return
     */
    public ARXSVMConfiguration getSVMConfiguration() {
        if (this.configSVM == null) {
            this.configSVM = ARXSVMConfiguration.create();
        }
        return this.configSVM;
    }

    /**
     * Is this model or one of the configurations modified
     * 
     * @return
     */
    public boolean isModified() {
        return this.modified || getLogisticRegressionConfiguration().isModified() || getNaiveBayesConfiguration().isModified() || getRandomForestConfiguration().isModified() || getSVMConfiguration().isModified();
    }
    
    /**
     * Sets the current classification configuration.
     * 
     * @param configCurrent
     */
    public void setCurrentConfiguration(ARXClassificationConfiguration<?> configCurrent){
        this.configCurrent = configCurrent;
    }
    
    /**
     * Sets modified
     */
    private void setModified() {
        this.modified = true;
    }
    
    /**
     * Sets a feature scaling function
     * @param attribute
     * @param function
     */
    public void setScalingFunction(String attribute, String function) {
        this.setModified();
        this.getFeatureScaling().setScalingFunction(attribute, function);
    }
    
    /**
     * Set this model and all configurations unmodified
     */
    public void setUnmodified() {
        this.modified = false;
        getLogisticRegressionConfiguration().setUnmodified();
        getNaiveBayesConfiguration().setUnmodified();
        getRandomForestConfiguration().setUnmodified();
        getSVMConfiguration().setUnmodified();
    }
}
