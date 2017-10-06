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
public class ARXNaiveBayesConfiguration extends ARXClassificationConfiguration<ARXNaiveBayesConfiguration> implements Serializable {

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
    /**
     * Constructor
     */
    private ARXNaiveBayesConfiguration(){
        // Empty by design
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
}
