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
 * Configuration for Random Forest classifiers
 * @author Fabian Prasser
 */
public class ClassificationConfigurationRandomForest extends ARXClassificationConfiguration<ClassificationConfigurationRandomForest> implements Serializable, Cloneable {

    /**
     * Split rule for the decision tree
     * @author Fabian Prasser
     *
     */
    public static enum SplitRule {
        GINI,
        ENTROPY,
        CLASSIFICATION_ERROR
    }
    
    /** SVUID */
    private static final long serialVersionUID = 7928077920858462047L;

    /** Default value */
    public static final int       DEFAULT_NUMBER_OF_TREES               = 500;

    /** Default value = 0: sqrt(#features) seems to provide good results */
    public static final int       DEFAULT_NUMBER_OF_VARIABLES_TO_SPLIT  = 0;

    /** Default value */
    public static final int       DEFAULT_MINIMUM_SIZE_OF_LEAF_NODES    = 5;

    /** Default value */
    public static final int       DEFAULT_MAXMIMUM_NUMBER_OF_LEAF_NODES = 100;

    /** 1.0 = sampling with replacement; <1.0 = sampling without replacement */
    public static final double    DEFAULT_SUBSAMPLE                     = 1d;

    /** Split rule */
    public static final SplitRule DEFAULT_SPLIT_RULE                    = SplitRule.GINI;

    /**
     * Returns a new instance
     * @return
     */
    public static ClassificationConfigurationRandomForest create() {
        return new ClassificationConfigurationRandomForest();
    }

    /** Number of trees */
    private int                   numberOfTrees                         = DEFAULT_NUMBER_OF_TREES;
    /** Number of variables to split: sqrt(#features) seems to provide good result */
    private int                   numberOfVariablesToSplit              = DEFAULT_NUMBER_OF_VARIABLES_TO_SPLIT;
    /** Number of variables to split: sqrt(#features) seems to provide good result */
    private int                   minimumSizeOfLeafNodes                = DEFAULT_MINIMUM_SIZE_OF_LEAF_NODES;
    /** Number of variables to split: sqrt(#features) seems to provide good result */
    private int                   maximumNumberOfLeafNodes              = DEFAULT_MAXMIMUM_NUMBER_OF_LEAF_NODES;
    /** 1.0 = sampling with replacement; <1.0 = sampling without replacement */
    private double                subsample                             = DEFAULT_SUBSAMPLE;
    /** Split rule */
    private SplitRule             splitRule                             = DEFAULT_SPLIT_RULE;
    
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
     * @param numberOfVariablesToSplit
     * @param minimumSizeOfLeafNodes
     * @param maximumNumberOfLeafNodes
     * @param subsample
     * @param splitRule
     */
    protected ClassificationConfigurationRandomForest(boolean deterministic,
                                                    int maxRecords,
                                                    int numberOfFolds,
                                                    long seed,
                                                    int vectorLength,
                                                    int numberOfTrees,
                                                    int numberOfVariablesToSplit,
                                                    int minimumSizeOfLeafNodes,
                                                    int maximumNumberOfLeafNodes,
                                                    double subsample,
                                                    SplitRule splitRule) {
        super(deterministic, maxRecords, numberOfFolds, seed, vectorLength);
        this.numberOfTrees = numberOfTrees;
        this.numberOfVariablesToSplit = numberOfVariablesToSplit;
        this.minimumSizeOfLeafNodes = minimumSizeOfLeafNodes;
        this.maximumNumberOfLeafNodes = maximumNumberOfLeafNodes;
        this.subsample = subsample;
        this.splitRule = splitRule;
    }

    @Override
    public ClassificationConfigurationRandomForest clone() {
        return new ClassificationConfigurationRandomForest(super.isDeterministic(),
                                                         super.getMaxRecords(),
                                                         super.getNumFolds(),
                                                         super.getSeed(),
                                                         super.getVectorLength(),
                                                         numberOfTrees,
                                                         numberOfVariablesToSplit,
                                                         minimumSizeOfLeafNodes,
                                                         maximumNumberOfLeafNodes,
                                                         subsample,
                                                         splitRule);
    }
    
    /**
     * @return the maximumNumberOfLeafNodes
     */
    public int getMaximumNumberOfLeafNodes() {
        return maximumNumberOfLeafNodes;
    }

    /**
     * @return the minimumSizeOfLeafNodes
     */
    public int getMinimumSizeOfLeafNodes() {
        return minimumSizeOfLeafNodes;
    }

    /**
     * @return the numberOfTrees
     */
    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    /**
     * @return the numberOfVariablesToSplit
     */
    public int getNumberOfVariablesToSplit() {
        return numberOfVariablesToSplit;
    }

    /**
     * @return the splitRule
     */
    public SplitRule getSplitRule() {
        return splitRule;
    }

    /**
     * @return the subsample
     */
    public double getSubsample() {
        return subsample;
    }

    @Override
    public void parse(ARXClassificationConfiguration<?> config) {
        super.parse(config);
        if (config instanceof ClassificationConfigurationRandomForest) {
            ClassificationConfigurationRandomForest iconfig = (ClassificationConfigurationRandomForest)config;
            this.setNumberOfTrees(iconfig.numberOfTrees);
            this.setNumberOfVariablesToSplit(iconfig.numberOfVariablesToSplit);
            this.setMinimumSizeOfLeafNodes(iconfig.minimumSizeOfLeafNodes);
            this.setMaximumNumberOfLeafNodes(iconfig.maximumNumberOfLeafNodes);
            this.setSubsample(iconfig.subsample);
            this.setSplitRule(iconfig.splitRule);
        }
    }

    /**
     * @param maximumNumberOfLeafNodes the maximumNumberOfLeafNodes to set
     */
    public ClassificationConfigurationRandomForest setMaximumNumberOfLeafNodes(int maximumNumberOfLeafNodes) {
        if (this.maximumNumberOfLeafNodes != maximumNumberOfLeafNodes) {
            setModified();
            this.maximumNumberOfLeafNodes = maximumNumberOfLeafNodes;
        }
        return this;
    }

    /**
     * @param minimumSizeOfLeafNodes the minimumSizeOfLeafNodes to set
     */
    public ClassificationConfigurationRandomForest setMinimumSizeOfLeafNodes(int minimumSizeOfLeafNodes) {
        if (this.minimumSizeOfLeafNodes != minimumSizeOfLeafNodes) {
            setModified();
            this.minimumSizeOfLeafNodes = minimumSizeOfLeafNodes;
        }
        return this;
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

    /**
     * @param numberOfVariablesToSplit the numberOfVariablesToSplit to set
     */
    public ClassificationConfigurationRandomForest setNumberOfVariablesToSplit(int numberOfVariablesToSplit) {
        if (this.numberOfVariablesToSplit != numberOfVariablesToSplit) {
            setModified();
            this.numberOfVariablesToSplit = numberOfVariablesToSplit;
        }
        return this;
    }

    /**
     * @param splitRule the splitRule to set
     */
    public ClassificationConfigurationRandomForest setSplitRule(SplitRule splitRule) {
        if (this.splitRule != splitRule) {
            setModified();
            this.splitRule = splitRule;
        }
        return this;
    }    

    /**
     * @param subsample the subsample to set
     */
    public ClassificationConfigurationRandomForest setSubsample(double subsample) {
        if (this.subsample != subsample) {
            setModified();
            this.subsample = subsample;
        }
        return this;
    }
}
