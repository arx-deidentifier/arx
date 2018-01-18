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
package org.deidentifier.arx.aggregates.classification;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;

import smile.classification.DecisionTree.SplitRule;
import smile.classification.RandomForest;
import smile.data.Attribute;

import com.carrotsearch.hppc.IntArrayList;

/**
 * Implements a classifier
 * 
 * @author Fabian Prasser
 */
public class MultiClassRandomForest implements ClassificationMethod {
    
    /** Config */
    private final ClassificationConfigurationRandomForest config;
    /** Instance */
    private RandomForest                                  rm;
    /** Specification */
    private final ClassificationDataSpecification         specification;
    /** Data */
    private List<double[]>                                features = new ArrayList<double[]>();
    /** Data */
    private IntArrayList                                  classes  = new IntArrayList();

    /**
     * Creates a new instance
     * @param specification
     * @param config
     */
    public MultiClassRandomForest(ClassificationDataSpecification specification,
                                  ClassificationConfigurationRandomForest config) {

        // Store
        this.config = config;
        this.specification = specification;
    }

    @Override
    public ClassificationResult classify(DataHandleInternal features, int row) {
        double[] probabilities = new double[specification.classMap.size()];
        int result = rm.predict(encodeFeatures(features, row), probabilities);
        return new MultiClassRandomForestClassificationResult(result, probabilities, specification.classMap);
    }

    @Override
    public void close() {
        
        // Convert split rule
        SplitRule rule = null;
        switch (config.getSplitRule()) {
        case CLASSIFICATION_ERROR:
            rule = SplitRule.CLASSIFICATION_ERROR;
            break;
        case ENTROPY:
            rule = SplitRule.ENTROPY;
            break;
        case GINI:
            rule = SplitRule.GINI;
            break;
        default:
            throw new IllegalStateException("Unknown split rule");
        
        }
        
        // Set number of variables to split as floor(sqrt(number of features)) if default value was chosen
        if (config.getNumberOfVariablesToSplit() == ClassificationConfigurationRandomForest.DEFAULT_NUMBER_OF_VARIABLES_TO_SPLIT) {
            config.setNumberOfVariablesToSplit((int) Math.floor(Math.sqrt(features.size())));
        }
        
        // Learn now
        rm = new RandomForest((Attribute[])null, features.toArray(new double[features.size()][]), classes.toArray(), 
                              config.getNumberOfTrees(), config.getMaximumNumberOfLeafNodes(), config.getMinimumSizeOfLeafNodes(),
                              config.getNumberOfVariablesToSplit(), config.getSubsample(), rule);
        
        // Clear
        features.clear();
        classes.clear();
        features = new ArrayList<double[]>();
        classes = new IntArrayList();
    }

    @Override
    public void train(DataHandleInternal features, DataHandleInternal clazz, int row) {
        // The Random Forst does not support online learning, so we have to cache data
        this.features.add(encodeFeatures(features, row));
        this.classes.add(encodeClass(clazz, row));
    }

    /**
     * Encodes a class
     * @param handle
     * @param row
     * @return
     */
    private int encodeClass(DataHandleInternal handle, int row) {
        return specification.classMap.get(handle.getValue(row, specification.classIndex, true));
    }

    /**
     * Encodes a feature
     * @param handle
     * @param row
     * @return
     */
    private double[] encodeFeatures(DataHandleInternal handle, int row) {

        // Prepare
        double[] vector = new double[specification.featureIndices.length];
        
        // Special case where there are no features
        if (specification.featureIndices.length == 0) {
            return vector;
        }
        
        // For each attribute
        int count = 0;
        for (int index : specification.featureIndices) {
            
            // Obtain data
            ClassificationFeatureMetadata metadata = specification.featureMetadata[count];
            String value = handle.getValue(row, index, true);
            Double numeric = metadata.getNumericValue(value);
            if (Double.isNaN(numeric)) {    
                vector[count] = handle.getValueIdentifier(index, value);
            } else {
                vector[count] = numeric;
            }
            count++;
        }
        
        // Return
        return vector;
    }
}