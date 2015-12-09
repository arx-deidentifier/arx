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
package org.deidentifier.arx.aggregates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Statistics representing the prediction accuracy of a data mining
 * classification operator
 * 
 * @author Fabian Prasser
 */
public class StatisticsClassification {
    
    /**
     * Implements a classifier
     * @author Fabian Prasser
     */
    private interface Classifier {

        /**
         * Classify
         * @param features
         * @return
         */
        public int classify(Vector features);

        /**
         * Close
         */
        public void close();

        /**
         * Train
         * @param features
         * @param clazz
         */
        public void train(Vector features, int clazz);
    }
    
    /**
     * Implements a classifier
     * @author Fabian Prasser
     */
    private class MultiClassLogisticRegression implements Classifier {
        
        /** Instance*/
        private final OnlineLogisticRegression lr;

        /**
         * Creates a new instance
         * @param features
         * @param classes
         */
        public MultiClassLogisticRegression(int features, int classes) {

            // Check
            if (features == 0) {
                features = 1;
            }

            // Prepare classifier
            this.lr = new OnlineLogisticRegression(classes, features, new L1());
            
            // Configure
            this.lr.learningRate(1);
            this.lr.alpha(1);
            this.lr.lambda(0.000001);
            this.lr.stepOffset(10000);
            this.lr.decayExponent(0.2);
        }

        @Override
        public int classify(Vector features) {
            return lr.classifyFull(features).maxValueIndex();
        }

        @Override
        public void close() {
            lr.close();
        }

        @Override
        public void train(Vector features, int clazz) {
            lr.train(clazz, features);
        }
    }

    /** Features and class: last element is the class */
    private final int[]          indexes;
    /** Data types */
    private final DataType<?>[]  type;
    /** Minimum */
    private final double[]       minimum;
    /** Maximum */
    private final double[]       maximum;
    /** Cardinalities */
    private final int[]          cardinality;
    /** Interrupt flag */
    private final WrappedBoolean interrupt;
    /** Random */
    private final Random         random;
    /** Result */
    private double               accuracy;

    /**
     * Creates a new set of statistics for the given classification task
     * @param builder - The statistics builder
     * @param handle - The handle
     * @param ignoreSuppressedRows - Ignore suppressed records
     * @param features - The feature attributes
     * @param clazz - The class attributes
     * @param seed - The random seed, null, if the process should be randomized
     * @param samplingFraction - The sampling fraction
     * @throws ParseException 
     */
    StatisticsClassification(StatisticsBuilder builder,
                             DataHandleInternal handle,
                             boolean ignoreSuppressedRows,
                             String[] features,
                             String clazz,
                             Integer seed,
                             double samplingFraction,
                             WrappedBoolean interrupt) throws ParseException {

        // Init
        this.interrupt = interrupt;
        
        // Check and clean up
        if (samplingFraction <= 0d || samplingFraction > 1d) {
            throw new IllegalArgumentException("Samling fraction must be in ]0,1]");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("No class attribute defined");
        }
        if (handle.getColumnIndexOf(clazz) == -1) {
            throw new IllegalArgumentException("Unknown class '"+clazz+"'");
        }
        if (features == null) {
            throw new IllegalArgumentException("No features defined");
        }
        List<String> featuresList = new ArrayList<>();
        for (String feature : features) {
            if (feature == null) {
                throw new IllegalArgumentException("Feature must not be null");    
            }
            if (handle.getColumnIndexOf(feature) == -1) {
                throw new IllegalArgumentException("Unknown feature '"+feature+"'");
            }
            if (!feature.equals(clazz)) {
                featuresList.add(feature);
            }
        }
        features = featuresList.toArray(new String[featuresList.size()]);
        
        // Initialize random
        if (seed == null) {
            this.random = new Random();
        } else {
            this.random = new Random(seed);
        }
        
        // Create indexes
        this.indexes = getAttributeIndexes(handle, features, clazz);
        
        // Map for the target attribute
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        // Maps for the features
        @SuppressWarnings("unchecked")
        Map<String, Integer>[] maps = new HashMap[indexes.length-1];
        
        // Obtain meta data
        type = new DataType[indexes.length];
        minimum = new double[indexes.length];
        maximum = new double[indexes.length];
        cardinality = new int[indexes.length];
        
        // For each attribute
        for (int index = 0; index < indexes.length; index++) {
            
            // Obtain
            int column = indexes[index];
            String attribute = handle.getAttributeName(column);
            String[] values = handle.getDistinctValues(column);

            // Store
            type[index] = handle.getDataType(attribute);
            cardinality[index] = values.length;
            
            // Create map for class attribute
            if (index == indexes.length - 1) {
                for (int i = 0; i < values.length; i++) {
                    
                    // Check
                    checkInterrupt();
                    
                    // Do
                    map.put(values[i], i);
                }
                
            // Compute min and max for others
            } else {
                // Numeric
                if (type[index] instanceof ARXDecimal ||
                    type[index] instanceof ARXInteger ||
                    type[index] instanceof ARXDate) {
                    
                    // Compute minimum and maximum for feature scaling
                    minimum[index] = Double.MAX_VALUE;
                    maximum[index] = - Double.MAX_VALUE;
                    for (String value : values) {
                        
                        // Check
                        checkInterrupt();
                        
                        // Do
                        double numericValue = 0d;
                        if (type[index] instanceof ARXDecimal) {
                            numericValue = (Double)type[index].parse(value);
                        } else if (type[index] instanceof ARXInteger) {
                            numericValue = (Long)type[index].parse(value);
                        } else if (type[index] instanceof ARXDate) {
                            numericValue = ((Date)type[index].parse(value)).getTime();
                        }
                        minimum[index] = Math.min(minimum[index], numericValue);
                        maximum[index] = Math.max(maximum[index], numericValue);
                    }
                    
                // Ordinal or nominal
                } else {
                    
                    maps[index] = new HashMap<String, Integer>();
                    int position = 0;
                    for (String value : builder.getDistinctValuesOrdered(column)) {
                        maps[index].put(value, position++);
                    }
                    
                    minimum[index] = 0d;
                    maximum[index] = maps[index].size();
                }   
            }
        }
        
        // Validate
        int k = handle.getNumRows() > 10 ? 10 : handle.getNumRows();
        
        // ZeroR baseline
        if (this.indexes.length == 1) {
            this.accuracy = getBaselineAccordingToKFoldCrossValidation(handle, ignoreSuppressedRows, map, k, random, samplingFraction);
            
        // Train and cross validate
        } else {
            this.accuracy = getAccuracyAccordingToKFoldCrossValidation(handle, ignoreSuppressedRows, map, maps, k, random, samplingFraction);
        }
    }

    /**
     * Returns the baseline accuracy according to the zeroR method
     * @param handle
     * @param ignoreSuppressedRows
     * @param map 
     * @param k
     * @param random
     * @param samplingFraction
     * @return
     */
    private double getBaselineAccordingToKFoldCrossValidation(DataHandleInternal handle,
                                                              boolean ignoreSuppressedRows,
                                                              Map<String, Integer> map, 
                                                              int k,
                                                              Random random,
                                                              double samplingFraction) {

        // Obtain the folds
        List<List<Integer>> folds = getFolds(handle,
                                             ignoreSuppressedRows,
                                             k,
                                             random,
                                             samplingFraction);

        // Encode all features and classes
        List<int[]> classes = new ArrayList<int[]>();
        
        // For each fold as a validation set
        for (List<Integer> fold : folds) {
            int[] foldClasses = new int[fold.size()];
            int index = 0;
            for (int row : fold) {
                foldClasses[index] = getClass(handle, row, map);
                index++;
            }
            classes.add(foldClasses);
            fold.clear();
        }
        folds.clear();
        folds = null;
        
        // Perform cross validation
        double correct = 0d;
        double total = 0d;

        // For each fold as a validation set
        for (int i = 0; i < classes.size(); i++) {

            // For all training sets
            Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
            for (int j = 0; j < classes.size(); j++) {
                if (j != i) {
                    int[] foldClasses = classes.get(j);
                    for (int index = 0; index < foldClasses.length; index++) {

                        // Check
                        checkInterrupt();

                        // Train
                        Integer previous = counts.get(foldClasses[index]);
                        counts.put(foldClasses[index], previous != null ? previous + 1 : 1);
                    }
                }
            }
            
            // Obtain most frequent element
            int mostFrequentCount = 0;
            int mostFrequentElement = -1;
            for (Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > mostFrequentCount) {
                    mostFrequentElement = entry.getKey();
                    mostFrequentCount = entry.getValue();
                }
            }
            
            // Now validate
            int[] foldClasses = classes.get(i);
            for (int index = 0; index < foldClasses.length; index++) {

                // Check
                checkInterrupt();

                // Count
                total++;
                correct += foldClasses[index] == mostFrequentElement ? 1 : 0;
            }
        }

        // Return mean
        return correct / total;
    }

    /**
     * Returns the accuracy of the classifier
     */
    public double getFractionCorrect() {
        return accuracy;
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (interrupt.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }

    /**
     * Performs k-fold cross validation
     * @param handle
     * @param ignoreSuppressedRows
     * @param map
     * @param maps 
     * @param k
     * @param random
     * @param samplingFraction 
     * @return
     * @throws ParseException 
     */
    private double getAccuracyAccordingToKFoldCrossValidation(DataHandleInternal handle,
                                                              boolean ignoreSuppressedRows,
                                                              Map<String, Integer> map,
                                                              Map<String, Integer>[] maps,
                                                              int k,
                                                              Random random,
                                                              double samplingFraction) throws ParseException {

        // Obtain the folds
        List<List<Integer>> folds = getFolds(handle,
                                             ignoreSuppressedRows,
                                             k,
                                             random,
                                             samplingFraction);

        // Encode all features and classes
        List<Vector[]> features = new ArrayList<Vector[]>();
        List<int[]> classes = new ArrayList<int[]>();
        ConstantValueEncoder interceptEncoder = new ConstantValueEncoder("intercept");
        StaticWordValueEncoder featureEncoder = new StaticWordValueEncoder("feature");
        
        // For each fold as a validation set
        for (List<Integer> fold : folds) {
            Vector[] foldFeatures = new Vector[fold.size()];
            int[] foldClasses = new int[fold.size()];
            int index = 0;
            for (int row : fold) {
                foldFeatures[index] = getFeatures(handle, row, maps, interceptEncoder, featureEncoder);
                foldClasses[index] = getClass(handle, row, map);
                index++;
            }
            features.add(foldFeatures);
            classes.add(foldClasses);
            fold.clear();
        }
        folds.clear();
        folds = null;
        
        // Perform cross validation
        double correct = 0d;
        double total = 0d;

        // For each fold as a validation set
        for (int i = 0; i < features.size(); i++) {
            
            // Create classifier
            Classifier classifier = new MultiClassLogisticRegression(indexes.length - 1, map.size());

            try {

                // For all training sets
                for (int j = 0; j < features.size(); j++) {
                    if (j != i) {
                        Vector[] foldFeatures = features.get(j);
                        int[] foldClasses = classes.get(j);
                        for (int index = 0; index < foldFeatures.length; index++) {

                            // Check
                            checkInterrupt();

                            // Train
                            classifier.train(foldFeatures[index], foldClasses[index]);
                        }
                    }
                }
               
                // Now validate
                Vector[] foldFeatures = features.get(i);
                int[] foldClasses = classes.get(i);
                for (int index = 0; index < foldFeatures.length; index++) {

                    // Check
                    checkInterrupt();
                    
                    // Count
                    total++;
                    correct += foldClasses[index] == classifier.classify(foldFeatures[index]) ? 1 : 0;
                }
                
            } catch (Exception e) {
                throw (e);
            } finally {
                classifier.close();
            }
        }

        // Return mean
        return correct / total;
    }

    /**
     * Creates the folds
     * @param handle
     * @param ignoreSuppressedRows
     * @param k
     * @param random
     * @param samplingFraction
     * @return
     */
    private List<List<Integer>> getFolds(DataHandleInternal handle,
                                         boolean ignoreSuppressedRows,
                                         int k,
                                         Random random,
                                         double samplingFraction) {
        // Prepare indexes
        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < handle.getNumRows(); row++) {
            if ((!ignoreSuppressedRows || !handle.isOutlier(row)) 
                 && random.nextDouble() <= samplingFraction) {
                rows.add(row);
            }
        }
        Collections.shuffle(rows, random);
        
        // Create folds
        List<List<Integer>> folds = new ArrayList<>();
        int size = rows.size() / k;
        size = size > 1 ? size : 1;
        for (int i = 0; i < k; i++) {
            
            // Check
            checkInterrupt();
            
            // For each fold
            int min = i * size;
            int max = (i + 1) * size;
            if (i == k - 1) {
                max = rows.size();
            }
            
            // Check
            if (max < rows.size()) {
                
                // Collect rows
                List<Integer> fold = new ArrayList<>();
                for (int j = min; j < max; j++) {
                    fold.add(rows.get(j));
                }
                
                // Store
                folds.add(fold);
            }
        }
        
        // Free
        rows.clear();
        rows = null;
        return folds;
    }
    
    /**
     * Returns the indexes of all relevant attributes
     * @param handle
     * @param features
     * @param clazz
     * @return
     */
    private int[] getAttributeIndexes(DataHandleInternal handle, String[] features, String clazz) {
        // Collect
        List<Integer> list = new ArrayList<>();
        for (int column = 0; column < handle.getNumColumns(); column++) {
            String attribute = handle.getAttributeName(column);
            if (isContained(features, attribute)) {
                list.add(column);
            }
        }
        list.add(handle.getColumnIndexOf(clazz));
        
        // Convert
        int[] result = new int[list.size()];
        for (int i=0; i<list.size(); i++) {
            result[i] = list.get(i);
        }
        
        // Return
        return result;
    }

    /**
     * Returns the class for the given row
     * @param handle
     * @param row
     * @param map
     * @return
     */
    private int getClass(DataHandleInternal handle, int row, Map<String, Integer> map) {
        return map.get(handle.getValue(row, indexes[indexes.length - 1]));
    }

    /**
     * Returns the feature vector for the given row
     * @param handle
     * @param row
     * @param maps
     * @param interceptEncoder
     * @param featureEncoder
     * @return
     * @throws ParseException
     */
    private Vector getFeatures(DataHandleInternal handle, int row,
                               Map<String, Integer>[] maps,
                               ConstantValueEncoder interceptEncoder,
                               StaticWordValueEncoder featureEncoder) throws ParseException {
        
        // Prepare
        int length = this.indexes.length - 1;
        DenseVector vector = new DenseVector(length != 0 ? length : 1);
        interceptEncoder.addToVector("1", vector);
        
        // Special case where there are no features
        if (length == 0) {
            featureEncoder.addToVector("Feature:1", 1, vector);
            return vector;
        }
        
        // For each attribute
        for (int index = 0; index < length; index++) {
            
            // Obtain data
            int column = indexes[index];
            String name = handle.getAttributeName(column);
            DataType<?> type = this.type[index];
            double minimum = this.minimum[index];
            double maximum = this.maximum[index];
            
            // Set value
            double value = 0d;
            if (type instanceof ARXDecimal) {
                value = handle.getDouble(row, column);
            } else if (type instanceof ARXInteger) {
                value = handle.getDouble(row, column);
            } else if (type instanceof ARXDate) {
                value = handle.getDate(row, column).getTime();
            } else {
                value = maps[index].get(handle.getValue(row, column));
            }
            featureEncoder.addToVector(name, (value - minimum) / (maximum - minimum), vector);
        }
        
        // Return
        return vector;
    }

    /**
     * Returns whether the given array contains the given value
     * @param array
     * @param value
     * @return
     */
    private boolean isContained(String[] array, String value) {
        for (String element : array) {
            if (element.equals(value)) {
                return true;
            }
        }
        return false;
    }
}