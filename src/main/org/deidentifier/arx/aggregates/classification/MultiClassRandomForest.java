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
import org.deidentifier.arx.common.WrappedBoolean;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import smile.classification.DecisionTree.SplitRule;
import smile.classification.RandomForest;
import smile.classification.TrainingInterrupt;
import smile.data.Attribute;

/**
 * Implements a classifier
 * 
 * @author Fabian Prasser
 */
public class MultiClassRandomForest extends ClassificationMethod {

    /** Config */
    private final ClassificationConfigurationRandomForest config;
    /** Instance */
    private RandomForest                                  rm;
    /** Specification */
    private final ClassificationDataSpecification         specification;
    /** Data */
    private List<double[]>                                features        = new ArrayList<double[]>();
    /** Data */
    private IntArrayList                                  classes         = new IntArrayList();
    /** Config */
    private final int                                     numberOfVariablesToSplit;
    /** Input handle */
    private final DataHandleInternal                      inputHandle;
    /** Because SMILE sucks */
    private IntIntOpenHashMap                             mapping;

    /**
     * Creates a new instance
     * @param interrupt
     * @param specification
     * @param config
     * @param inputHandle
     */
    public MultiClassRandomForest(WrappedBoolean interrupt,
                                  ClassificationDataSpecification specification,
                                  ClassificationConfigurationRandomForest config,
                                  DataHandleInternal inputHandle) {

        super(interrupt);

        // Store
        this.config = config;
        this.specification = specification;
        this.inputHandle = inputHandle;
        
        // Set number of variables to split as floor(sqrt(number of features)) if default value was chosen
        if (config.getNumberOfVariablesToSplit() == ClassificationConfigurationRandomForest.DEFAULT_NUMBER_OF_VARIABLES_TO_SPLIT) {
            this.numberOfVariablesToSplit = (int) Math.floor(Math.sqrt(this.specification.featureIndices.length));
        } else {
            this.numberOfVariablesToSplit = config.getNumberOfVariablesToSplit();
        }
    }

    @Override
    public ClassificationResult classify(DataHandleInternal features, int row) {

        // Call SMILE
        double[] _probabilities = new double[mapping.size()];
        int _result = rm.predict(encodeFeatures(features, row, true), _probabilities);
        
        // Mapping
        int result = mapping.get(_result);
        double[] probabilities = new double[specification.classMap.size()];
        for (int i = 0; i < _probabilities.length; i++) {
            probabilities[mapping.get(i)] = _probabilities[i];
        }
        
        // Return
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
        
        // Encode classes because SMILE sucks!
        this.mapping = new IntIntOpenHashMap();
        IntIntOpenHashMap classMap = new IntIntOpenHashMap();
        int[] encodedClasses = new int[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            int value = classes.get(i);
            int encoded = classMap.size();
            if (classMap.containsKey(value)) {
                encoded = classMap.lget();
            } else {
                classMap.put(value, encoded);
                this.mapping.put(encoded, value);
            }
            encodedClasses[i] = encoded;
        }
        
        // Learn now
        rm = new RandomForest((Attribute[])null, features.toArray(new double[features.size()][]), encodedClasses, 
                              config.getNumberOfTrees(), config.getMaximumNumberOfLeafNodes(), config.getMinimumSizeOfLeafNodes(),
                              this.numberOfVariablesToSplit, config.getSubsample(), rule, new TrainingInterrupt() {
                                @Override
                                public boolean isInterrupted() {
                                    return interrupt.value;
                                }
        });
        
        // Clear
        features.clear();
        classes.clear();
        features = new ArrayList<double[]>();
        classes = new IntArrayList();
    }

    @Override
    public void train(DataHandleInternal features, DataHandleInternal clazz, int row) {
        // The Random Forest does not support online learning, so we have to cache data
        this.features.add(encodeFeatures(features, row, false));
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
     * @param classify
     * @return
     */
    private double[] encodeFeatures(DataHandleInternal handle, int row, boolean classify) {

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
            String value = null;
            if (classify && metadata.isNumericMicroaggregation()) {
                value = inputHandle.getValue(row, index, true);
            } else {
                value = handle.getValue(row, index, true);
            }
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