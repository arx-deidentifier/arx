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
package org.deidentifier.arx.aggregates;

import java.io.Serializable;

import org.deidentifier.arx.ARXClassificationConfiguration;

/**
 * Configuration for naive bayes classification
 * @author Fabian Prasser
 */
public class ClassificationConfigurationNaiveBayes extends ARXClassificationConfiguration<ClassificationConfigurationNaiveBayes> implements Serializable, Cloneable {

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
    public static ClassificationConfigurationNaiveBayes create() {
        return new ClassificationConfigurationNaiveBayes();
    }
    
    /** Default value */
    public static final Type   DEFAULT_TYPE  = Type.BERNOULLI;
    /** Default value */
    public static final double DEFAULT_SIGMA = 1.0d;

    /** Type */
    private Type               type          = DEFAULT_TYPE;
    /** Prior count */
    private double             sigma         = DEFAULT_SIGMA;
    /**
     * Constructor
     */
    private ClassificationConfigurationNaiveBayes(){
        // Empty by design
    }

    /** 
     * Clone constructor
     * @param deterministic
     * @param maxRecords
     * @param numberOfFolds
     * @param seed
     * @param vectorLength
     * @param type
     * @param sigma
     */
    protected ClassificationConfigurationNaiveBayes(boolean deterministic,
                                                    int maxRecords,
                                                    int numberOfFolds,
                                                    long seed,
                                                    int vectorLength,
                                                    Type type,
                                                    double sigma) {
        super(deterministic, maxRecords, numberOfFolds, seed, vectorLength);
        this.type = type;
        this.sigma = sigma;
    }

    @Override
    public ClassificationConfigurationNaiveBayes clone() {
        return new ClassificationConfigurationNaiveBayes(super.isDeterministic(),
                                                         super.getMaxRecords(),
                                                         super.getNumFolds(),
                                                         super.getSeed(),
                                                         super.getVectorLength(),
                                                         type,
                                                         sigma);
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
    @Override
    public void parse(ARXClassificationConfiguration<?> config) {
        super.parse(config);
        if (config instanceof ClassificationConfigurationNaiveBayes) {
            ClassificationConfigurationNaiveBayes iconfig = (ClassificationConfigurationNaiveBayes)config;
            this.setSigma(iconfig.sigma);
            this.setType(iconfig.type);
        }
    }

    /**
     * Sets the prior count of add-k smoothing of evidence.
     * @param sigma the sigma to set
     */
    public ClassificationConfigurationNaiveBayes setSigma(double sigma) {
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
    public ClassificationConfigurationNaiveBayes setType(Type type) {
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
