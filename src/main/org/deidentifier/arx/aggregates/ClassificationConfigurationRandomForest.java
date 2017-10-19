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
 * Configuration for Random Forest classifiers
 * @author Fabian Prasser
 */
public class ClassificationConfigurationRandomForest extends ARXClassificationConfiguration<ClassificationConfigurationRandomForest> implements Serializable, Cloneable {

    /** SVUID */
    private static final long serialVersionUID = 7928077920858462047L;

    /**
     * Returns a new instance
     * @return
     */
    public static ClassificationConfigurationRandomForest create() {
        return new ClassificationConfigurationRandomForest();
    }
    
    /** Default value */
    public static final int DEFAULT_NUMBER_OF_TREES = 10;

    /** Number of trees */
    private int             numberOfTrees           = DEFAULT_NUMBER_OF_TREES;

    /**
     * Constructor
     */
    private ClassificationConfigurationRandomForest(){
        // Empty by design
    }

    /** 
     * Clone constructor
     * @param deterministic
     * @param maxRecords
     * @param numberOfFolds
     * @param seed
     * @param vectorLength
     * @param numberOfTrees
     */
    protected ClassificationConfigurationRandomForest(boolean deterministic,
                                                    int maxRecords,
                                                    int numberOfFolds,
                                                    long seed,
                                                    int vectorLength,
                                                    int numberOfTrees) {
        super(deterministic, maxRecords, numberOfFolds, seed, vectorLength);
        this.numberOfTrees = numberOfTrees;
    }

    @Override
    public ClassificationConfigurationRandomForest clone() {
        return new ClassificationConfigurationRandomForest(super.isDeterministic(),
                                                         super.getMaxRecords(),
                                                         super.getNumFolds(),
                                                         super.getSeed(),
                                                         super.getVectorLength(),
                                                         numberOfTrees);
    }
    
    /**
     * @return the numberOfTrees
     */
    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    @Override
    public void parse(ARXClassificationConfiguration<?> config) {
        super.parse(config);
        if (config instanceof ClassificationConfigurationRandomForest) {
            ClassificationConfigurationRandomForest iconfig = (ClassificationConfigurationRandomForest)config;
            this.setNumberOfTrees(iconfig.numberOfTrees);
        }
    }    

    /**
     * @param numberOfTrees the numberOfTrees to set
     */
    public ClassificationConfigurationRandomForest setNumberOfTrees(int numberOfTrees) {
        if (this.numberOfTrees != numberOfTrees) {
            setModified();
            this.numberOfTrees = numberOfTrees;
        }
        return this;
    }
}
