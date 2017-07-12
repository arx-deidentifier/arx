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
    private ARXClassificationConfiguration     configCurrent      = null;
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
    public ARXClassificationConfiguration getARXClassificationConfigurationCurrent(){
        return configCurrent;
    }
    
    /**
     * Returns a logistic regression config for ARX
     * @return
     */
    public ARXLogisticRegressionConfiguration getARXLogisticRegressionConfiguration() {
        return getConfig();
    }
    
    /**
     * Returns a naive bayes config for ARX
     * @return
     */
    public ARXNaiveBayesConfiguration getARXNaiveBayesConfiguration() {
        return getConfigNaiveBayes();
    }

    /**
     * Returns a random forest config for ARX
     * @return
     */
    public ARXRandomForestConfiguration getARXRandomForestConfiguration() {
        return getConfigRandomForest();
    }
    
    /**
     * Returns a SVM config for ARX
     * @return
     */
    public ARXSVMConfiguration getARXSVMConfiguration() {
        return getConfigSVM();
    }
    
    /**
     * For backwards compatibility
     */
    private ARXLogisticRegressionConfiguration getConfig() {
        if (this.config == null || this.configCurrent == null) {
            this.config = ARXLogisticRegressionConfiguration.create();
            this.configCurrent = this.config;
        }
        return this.config;
    }

    /**
     * For backwards compatibility
     */
    private ARXNaiveBayesConfiguration getConfigNaiveBayes() {
        if (this.configNaiveBayes == null || this.configCurrent == null) {
            this.configNaiveBayes = ARXNaiveBayesConfiguration.create();
            this.configCurrent = this.configNaiveBayes;
        }
        return this.configNaiveBayes;
    }
    
    /**
     * For backwards compatibility
     */
    private ARXRandomForestConfiguration getConfigRandomForest() {
        if (this.configRandomForest == null || this.configCurrent == null) {
            this.configRandomForest = ARXRandomForestConfiguration.create();
            this.configCurrent = this.configRandomForest;
        }
        return this.configRandomForest;
    }
    
    /**
     * For backwards compatibility
     */
    private ARXSVMConfiguration getConfigSVM() {
        if (this.configSVM == null || this.configCurrent == null) {
            this.configSVM = ARXSVMConfiguration.create();
            this.configCurrent = this.configSVM;
        }
        return this.configSVM;
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
     * Is one of the configurations modified
     * 
     * @return
     */
    public boolean isModified() {
        return this.modified || config.isModified() || configNaiveBayes.isModified() || configRandomForest.isModified() || configSVM.isModified();
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
     * Set current config unmodified
     */
    public void setUnmodified() {
        this.modified = false;
        if (configCurrent != null) {
            configCurrent.setUnmodified();
        }
    }
}
