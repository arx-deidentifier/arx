/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.aggregates.StatisticsSummary;

import smile.classification.DecisionTree.SplitRule;
import smile.classification.KNN;
import smile.classification.LogisticRegression;
import smile.classification.RandomForest;
import smile.classification.SoftClassifier;
import smile.data.Attribute;

/**
 * Estimate risks for membership attacks, using shadow models
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 */
public class ShadowModel {
    
    /**
     * Classifier Type
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    public static enum ClassifierType {
        KNN, LR, RF
    }
    
    /**
     * Feature type
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    public static enum FeatureType {
        CORRELATION,
        ENSEMBLE,
        HISTOGRAM,
        NAIVE
    }

    /**
     * Simple dictionary class
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class Dictionary {
        
        /** Map for values*/
        private Map<String, Map<String, Integer>> map = new HashMap<>();
        
        /** Probe the dictionary*/
        public int probe(String attribute, String value) {
            
            // Get map
            Map<String, Integer> values = map.get(attribute);
            if (values == null) {
                values = new HashMap<>();
                map.put(attribute, values);
            }
            
            // Probe
            Integer code = values.get(value);
            if (code == null) {
                code = values.size();
                values.put(value, code);
            }
            
            // Done
            return code;
        }

        /**
         * Returns the size for the given dimension
         * @param attribute
         * @return
         */
        public int size(String attribute) {
            return this.map.get(attribute).size();
        }
    }

    /**
     * Base for features
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private interface Feature {
        /** Compile feature data*/
        public double[] compile();
    }

    /**
     * Correlation feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureCorrelation implements Feature {

        /** Features */
        private Map<String, OpenMapRealMatrix> categorical = new HashMap<>();
        /** Features */
        private Map<String, double[]>          numeric     = new HashMap<>();
        /** Rows */
        private int                            rows;

        /**
         * Calculates correlation features using Pearson's product-moment correlation.
         * All columns of continuous attributes are directly used for correlation calculation.
         * Categorical and ordinal attributes are transfered to a sparse representation
         * were each value becomes an own column and whether (or not) the value applies to a row
         * is indicated by the value 1d (or 0d).
         * @param handle
         */
        public FeatureCorrelation(DataHandle handle) {

            // Prepare
            this.rows = handle.getNumRows();
            
            // For each attribute
            for (String attribute : attributes) {

                // Obtain attribute details
                int column = handle.getColumnIndexOf(attribute);
                String attributeName = handle.getAttributeName(column);
                DataType<?> _type = handle.getDefinition().getDataType(attributeName);
                Class<?> _clazz = _type.getDescription().getWrappedClass();

                // Just store numeric values as is
                if (_clazz.equals(Long.class) || _clazz.equals(Double.class) || _clazz.equals(Date.class)) {
                    
                    // Create array
                    double[] values = new double[handle.getNumRows()];
                    
                    // Copy values as double
                    for (int row = 0; row < values.length; row++) {
                        values[row] = getDouble(handle, row, column, _clazz);
                    }
                    
                    // Store
                    numeric.put(attribute, values);
                    
                } else if (_clazz.equals(String.class)) {
                    
                    // Probe all values in advance 
                    // (For ensuring the matrix is initialized with the required dimensions)
                    int[] _values = new int[handle.getNumRows()];
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        _values[row] = dictionary.probe(attribute, handle.getValue(row, column));
                    }
                    
                    // Create matrix
                    OpenMapRealMatrix matrix = new OpenMapRealMatrix(handle.getNumRows(), dictionary.size(attribute));
                    
                    // Store values
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        matrix.setEntry(row, _values[row], 1);
                    }
                    
                    // Store
                    categorical.put(attribute, matrix);
                }
            }
        }

        @Override
        public double[] compile() {

            // Count columns
            int columns = 0;

            // For each attribute
            for (String attribute : attributes) {
                if (numeric.containsKey(attribute)) {
                    columns++;
                } else {
                    columns+=dictionary.size(attribute);
                }
            }
            
            // Prepare matrix
            OpenMapRealMatrix matrix = new OpenMapRealMatrix(rows, columns);
            int column = 0;
            for (String attribute : attributes) {
                
                // Copy numeric data
                if (numeric.containsKey(attribute)) {
                    double[] values = numeric.get(attribute);
                    for (int row = 0; row < rows; row++) {
                        matrix.setEntry(row, column, values[row]);
                    }
                    column++;

                // Copy categorical data
                } else {
                    OpenMapRealMatrix _matrix = categorical.get(attribute);
                    for (int _column = 0; _column < dictionary.size(attribute); _column++) {
                        if (_column < _matrix.getColumnDimension()) {
                            for (int row = 0; row < rows; row++) {
                                matrix.setEntry(row, column, _matrix.getEntry(row, _column));
                            }
                        }
                        column++;
                    }
                }
            }
            
            // Calculate
            double[][] result = new PearsonsCorrelation().computeCorrelationMatrix(matrix).getData();
            
            // Done
            return getFlattenedArray(result);
        }
    }

    /**
     * Ensemble feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureEnsemble implements Feature {

        /** Feature */
        private FeatureCorrelation correlation;
        /** Feature */
        private FeatureHistogram   histogram;
        /** Feature */
        private FeatureNaive       naive;

        /**
         * Creates a new instance
         * @param handle
         */
        public FeatureEnsemble(DataHandle handle) {
            naive = new FeatureNaive(handle);
            histogram = new FeatureHistogram(handle);
            correlation = new FeatureCorrelation(handle);
        }

        @Override
        public double[] compile() {
            double[] _naive = naive.compile();
            double[] _histogram = histogram.compile();
            double[] _correlation = correlation.compile();
            return getFlattenedArray(_naive, _histogram, _correlation);
        }
    }

    /**
     * Histogram feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureHistogram implements Feature {

        /** Features */
        private Map<String, StatisticsFrequencyDistribution> categorical = new HashMap<>();
        /** Features */
        private Map<String, double[]>                        numeric     = new HashMap<>();

        /**
         * Creates a new instance.
         * For categorical attributes this simply refers to the counts of distinct values.
         * For continuous and ordinal attributes, the domain of the values is separated into 10 bins.
         * @param handle
         */
        public FeatureHistogram(DataHandle handle) {

            // For each attribute
            for (String attribute : attributes) {

                // Obtain attribute details
                int column = handle.getColumnIndexOf(attribute);
                DataType<?> _type = handle.getDefinition().getDataType(attribute);
                Class<?> _clazz = _type.getDescription().getWrappedClass();
                checkDataType(attribute, _type);

                // Bining for numerical attributes
                if (_clazz.equals(Long.class) || _clazz.equals(Double.class) || _clazz.equals(Date.class)) {

                    // Prepare
                    double min = minimum.get(attribute);
                    double max = maximum.get(attribute);
                    double binSize = (max - min) / NUM_BINS;
                    double[] freqs = new double[NUM_BINS];

                    // For each value
                    for (int row = 0; row < handle.getNumRows(); row++) {

                        // Parse value
                        double value = getDouble(handle, row, column, _clazz);

                        // Calculate bin
                        int bin = (int) ((value - min) / binSize);

                        // Check range
                        if (0 > bin || bin > NUM_BINS) {
                            throw new RuntimeException("Value out of histogram range");
                        }

                        // TODO Dirty quick-fix
                        if (bin == NUM_BINS) {
                            bin -= 1;
                        }

                        // Increment frequency of bin
                        freqs[bin] += 1d;
                    }
                    
                    // Store
                    numeric.put(attribute, freqs);

                // Frequency distribution for categorial attributes
                } else if (_clazz.equals(String.class)) {
                    categorical.put(attribute, handle.getStatistics().getFrequencyDistribution(column));
                }
            }
        }

        @Override
        public double[] compile() {
            
            // Prepare
            List<double[]> features = new ArrayList<>();
            
            // For each attribute
            for (String attribute : attributes) {
                
                // Numeric attribute
                if (numeric.containsKey(attribute)) {
                    features.add(numeric.get(attribute));
                    
                // Categorical attribute
                } else {
                    StatisticsFrequencyDistribution distribution = categorical.get(attribute);
                    double[] feature = new double[dictionary.size(attribute)];
                    for (int i = 0; i < distribution.values.length; i++) {
                        String value = distribution.values[i];
                        double count = distribution.frequency[i] * distribution.count;
                        int code = dictionary.probe(attribute, value);
                        feature[code] = count;
                    }
                    features.add(feature);
                }
            }
            
            // Done
            return getFlattenedArray(features.toArray(new double[features.size()][]));
        }
    }
    
    /**
     * Naive feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureNaive implements Feature {
        
        // Features
        private double[] features;
        
        /**
         * Creates a new instance
         * @param handle
         */
        @SuppressWarnings("unchecked")
        public FeatureNaive(DataHandle handle) {
            
            // Prepare
            features = new double[attributes.length * 3];
            
            // Calculate statistics
            Map<String, StatisticsSummary<?>> statistics = handle.getStatistics().getSummaryStatistics(false);
            
            // For each attribute
            int index = 0;
            for (String attribute : attributes) {
                
                // Index
                int column = handle.getColumnIndexOf(attribute);

                // Obtain statistics
                StatisticsSummary<?> summary = statistics.get(attribute);
                DataType<?> _type = handle.getDefinition().getDataType(attribute);
                Class<?> _clazz = _type.getDescription().getWrappedClass();
                checkDataType(attribute, _type);

                // Parameters to calculate
                Double mostFreq = null;
                Double leastFreq = null;
                Double uniqueElements = null;
                Double mean = null;
                Double median = null;
                Double var = null;

                // Calculate depending on data type
                if (_clazz.equals(Long.class)) {
                    
                    // Handle data type represented as long
                    DataType<Long> type = (DataType<Long>)_type;
                    mean = summary.getArithmeticMeanAsDouble();
                    var = summary.getSampleVarianceAsDouble();
                    Long _median = type.parse(summary.getMedianAsString());
                    median = _median != null ? _median.doubleValue() : 0d; // TODO: how to handle null here
                    
                } else if (_clazz.equals(Double.class)) {
                    
                    // Handle data type represented as double
                    DataType<Double> type = (DataType<Double>)_type;
                    mean = summary.getArithmeticMeanAsDouble();
                    var = summary.getSampleVarianceAsDouble();
                    Double _median = type.parse(summary.getMedianAsString());
                    median = _median != null ? _median : 0d; // TODO: how to handle null here
                    
                } else if (_clazz.equals(Date.class)) {
                    
                    // Handle data type represented as date
                    DataType<Date> type = (DataType<Date>)_type;
                    mean = summary.getArithmeticMeanAsDouble();
                    var = summary.getSampleVarianceAsDouble();
                    Date _median = type.parse(summary.getMedianAsString());
                    median = _median != null ? _median.getTime() : 0d; // TODO: how to handle null here
                    
                } else if (_clazz.equals(String.class)) {
                    
                    // Count frequencies of values
                    Map<String, Integer> map = new HashMap<>();
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        String value = handle.getValue(row, column);
                        Integer count = map.get(value);
                        if (count == null) {
                            count = 1;
                        } else {
                            count++;
                        }
                        map.put(value, count);
                    }
                    
                    // Determine codes with highest and lowest frequencies
                    int minFreq = Integer.MAX_VALUE;
                    int maxFreq = Integer.MIN_VALUE;
                    
                    // Find most and least frequent
                    for (Entry<String, Integer> entry : map.entrySet()) {
                        String value = entry.getKey();
                        Integer count = entry.getValue();
                        double code = dictionary.probe(attribute, value);
                        if (count < minFreq) {
                            minFreq = count;
                            leastFreq = code;
                        }
                        if (count > maxFreq) {
                            maxFreq = count;
                            mostFreq = code;
                        }
                    }

                    // Get number of assigned keys
                    uniqueElements = (double) map.size();

                    
                } else {
                    throw new IllegalStateException("Unknown data type");
                }
                
                // Switch feature type
                if (mean != null && var != null && median != null) {
                    features[index] = mean;
                    features[index + 1] = median;
                    features[index + 2] = var;
                    
                } else if (mostFreq != null && leastFreq != null && uniqueElements != null) {
                    features[index] = uniqueElements;
                    features[index + 1] = mostFreq;
                    features[index + 2] = leastFreq;
                } else {
                    throw new IllegalStateException("Features unavailable");
                }
                
                // Increment feature index
                index += 3;
            }
        }

        @Override
        public double[] compile() {
            return features;
        }
    }

    /** Number of bins to use for histogram feature */
    private final static int NUM_BINS = 10;

    /** Attributes to consider */
    private String[]                 attributes;
    /** Minimal distance*/
    private double                   minDistance;
    /** Maximal distance*/
    private double                   maxDistance;
    /** Distances of all records*/
    private double[]                 distances;
    /** Classifier */
    private SoftClassifier<double[]> classifier;
    /** Type */
    private ClassifierType           classifierType;
    /** Compiled */
    private boolean                  compiled     = false;
    /** To ensure consistency of data types */
    private Map<String, DataType<?>> dataTypes    = new HashMap<>();
    /** Dictionary */
    private Dictionary               dictionary   = new Dictionary();
    /** Type */
    private FeatureType              featureType;
    /** Maximum */
    private Map<String, Double>      maximum      = new HashMap<>();
    /** Minimum */
    private Map<String, Double>      minimum      = new HashMap<>();
    /** Training data */
    private Map<Feature, Boolean>    trainingData = new HashMap<>();

    /**
     * Creates a new instance
     * @param population
     * @param _attributes
     * @param featureType
     * @param classifierType
     * @throws ParseException 
     */
    public ShadowModel(DataHandle population, Set<String> _attributes, FeatureType featureType, ClassifierType classifierType) throws ParseException {
        this.featureType = featureType;
        this.classifierType = classifierType;
        this.attributes = new String[_attributes.size()];
        int index = 0;
        for (int column = 0; column < population.getNumColumns(); column++) {
            String attribute = population.getAttributeName(column);
            if (_attributes.contains(attribute)) {
                this.attributes[index++] = attribute;
            }
        }
        this.analyzePopulation(population, attributes);
    }
    
    /**
     * Returns the normalized distance in [0, 1] for the record at the given row
     * @param row
     * @return
     */
    public double getDistance(int row) {
        return (distances[row] - minDistance) / (maxDistance - minDistance);
    }

    /**
     * Predicts for all datasets whether the target is included
     * @param handles
     * @return
     */
    @SuppressWarnings("unchecked")
    public Pair<Boolean, Double>[] predict(DataHandle[] handles) {
        
        // Check
        if (this.trainingData.isEmpty()) {
            throw new IllegalStateException("No training data has been provided");
        }
        
        // Calculates features
        Feature[] features  = new Feature[handles.length];
        for (int i = 0; i < handles.length; i++) {
            features[i] = getFeatures(handles[i]);
        }

        // Actually train the classifier
        if (!compiled) {
            compile();
        }
        
        double[][] xValues  = new double[handles.length][];
        for (int i = 0; i < handles.length; i++) {
            xValues[i] = features[i].compile();
        }
        
        // Prepare
        Pair<Boolean, Double>[] result = new Pair[handles.length];
        for (int i = 0; i < handles.length; i++) {

            // Predict label
            double[] probabilities = new double[] {0, 0};
            int target = classifier.predict(xValues[i], probabilities);
            
            //TODO remove
            //System.out.println(Arrays.toString(xValues[i]));
            //System.out.println(xValues[i].length);
            
            double confidence = probabilities[target];
            result[i] = new Pair<>(target == 1, confidence);
        }
        
        // Done
        return result;
    }
    
    /**
     * Train the shadow model
     * @param data
     * @param targetIncluded
     */
    public void train(DataHandle data, boolean targetIncluded) {
        
        // Check
        if (compiled) {
            throw new IllegalStateException("Classifier already compiled to perform predictions");
        }
        
        // Store training data
        trainingData.put(getFeatures(data), targetIncluded);
    }
    
    /**
     * Analyze basic population properties
     * @param population
     * @param attributes
     * @throws ParseException 
     */
    private void analyzePopulation(DataHandle population, String[] attributes) throws ParseException {

        // Calculate statistics
        Map<String, StatisticsSummary<?>> statistics = population.getStatistics().getSummaryStatistics(false);
        
        // For each attribute
        for (String attribute : attributes) {
            
            // Obtain statistics
            StatisticsSummary<?> summary = statistics.get(attribute);
            DataType<?> _type = population.getDefinition().getDataType(attribute);
            Class<?> _clazz = _type.getDescription().getWrappedClass();
            checkDataType(attribute, _type);

            // Calculate depending on data type
            if (_clazz.equals(Long.class)) {
                // Handle data type represented as long
                double min = (Long)summary.getMinAsValue();
                double max = (Long)summary.getMaxAsValue();
                minimum.put(attribute, min);
                maximum.put(attribute, max);
            } else if (_clazz.equals(Double.class)) {
                // Handle data type represented as double
                double min = (Double)summary.getMinAsValue();
                double max = (Double)summary.getMaxAsValue();
                minimum.put(attribute, min);
                maximum.put(attribute, max);
            } else if (_clazz.equals(Date.class)) {
                // Handle data type represented as date
                double min = ((Date)summary.getMinAsValue()).getTime();
                double max = ((Date)summary.getMaxAsValue()).getTime();
                minimum.put(attribute, min);
                maximum.put(attribute, max);
            } else {
                
                // Pre-encode categorical values considering the order
                int column = population.getColumnIndexOf(attribute);
                for (String value : population.getStatistics().getDistinctValuesOrdered(column, true)) {
                    dictionary.probe(attribute, value);
                }
            }
        }
        
        // Calculate centroid
        double[] centroid = new double[attributes.length];
        for (int row = 0; row < population.getNumRows(); row++) {
            double[] vector = getVector(population, row);
            for (int i = 0; i < attributes.length; i++) {
                centroid[i] += vector[i];
            }
        }
        for (int i = 0; i < attributes.length; i++) {
            centroid[i] /= (double)population.getNumRows();
        }
        
        // Calculate min and max-distance
        minDistance = Double.MAX_VALUE;
        maxDistance = -Double.MAX_VALUE;
        distances = new double[population.getNumRows()];
        for (int row = 0; row < population.getNumRows(); row++) {
            double[] vector = getVector(population, row);
            double distance = new EuclideanDistance().compute(centroid, vector);
            distances[row] = distance;
            minDistance = Math.min(minDistance, distance);
            maxDistance = Math.max(maxDistance, distance);
        }
    }

    /**
     * Sanity check to ensure consistency of data types
     * @param attribute
     * @param type
     */
    private void checkDataType(String attribute, DataType<?> type) {
        DataType<?> _type = dataTypes.get(attribute);
        if (_type == null) {
            dataTypes.put(attribute, type);
        } else if (!(_type.equals(type))) {
            throw new IllegalArgumentException("Inconsistent data type detected for attribute: " + attribute);
        }
    }

    /**
     * Called before predictions
     */
    private void compile() {

        // Prepare
        double[][] xTrain = new double[trainingData.size()][];
        int[] yTrain = new int[trainingData.size()];
        
        // Collect data
        int index = 0;
        int featureSize = Integer.MIN_VALUE;
        for (Entry<Feature, Boolean> data : trainingData.entrySet()) {
            
            // Store
            xTrain[index] = data.getKey().compile();
            yTrain[index] = data.getValue() ? 1 : 0;
            
            // Sanity checks
            if (featureSize == Integer.MIN_VALUE) {
                featureSize = xTrain[index].length;
            } else if (featureSize != xTrain[index].length) {
                throw new IllegalArgumentException("Inconsistent feature size: " + featureSize + " and " + xTrain[index].length);
            }
            
            // Next
            index++;
        }
        
        // Train
        switch (classifierType) {
        case KNN:
            // TODO relocated to main() - and to ARX-config eventually
            int numberOfNeighbors = 5; // Privacy Mirage papper: 5
            classifier = KNN.learn(xTrain, yTrain, numberOfNeighbors, null);
            break;
        case LR:
            classifier = new LogisticRegression(xTrain, yTrain, null);
            break;
        case RF:
            // TODO relocated to main() - and to ARX-config eventually
            int numberOfTrees = 100; // sklearn default := 100 | ARX default := 500
            int maxNumberOfLeafNodes = Integer.MAX_VALUE; // sklean default := +INF | ARX default = 100;
            int minSizeOfLeafNodes = 1; // sklean default := 1 | ARX default := 5
            int numberOfVariablesToSplit = (int) Math.floor(Math.sqrt(xTrain[0].length)); // sklearn := auto (i.e. sqrt(#features)) | ARX default := 0
            double subSample = 1d; // skleanr --> provided at total number (2) | ARX default := 1d
            SplitRule splitRule = SplitRule.GINI; // sklearn default := GINI | ARX dedault: = GINI
            classifier = new RandomForest((Attribute[])null, xTrain, yTrain, numberOfTrees, maxNumberOfLeafNodes, minSizeOfLeafNodes, numberOfVariablesToSplit, subSample, splitRule, null);
            break;
        default:
            throw new RuntimeException("Classifier not supported");
        }
    }

    /**
     * Return double for numerical values
     * @param handle
     * @param row
     * @param column
     * @param _clazz
     * @return
     */
    private double getDouble(DataHandle handle, int row, int column, Class<?> _clazz) {

        try {
            // Calculate depending on data type
            if (_clazz.equals(Long.class)) {
                return handle.getLong(row, column);
            } else if (_clazz.equals(Double.class)) {
                return handle.getDouble(row, column);
            } else if (_clazz.equals(Date.class)) {
                return handle.getDate(row, column).getTime();
            } else {
                throw new IllegalStateException("Attribute is not numeric");
            }
        } catch (ParseException e) {
            // TODO Why caused by short heuristic searches?
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Calculates features
     * @return
     */
    private Feature getFeatures(DataHandle handle) {
        switch (featureType) {
        case ENSEMBLE:
            return new FeatureEnsemble(handle);    
        case CORRELATION:
            return new FeatureCorrelation(handle);            
        case HISTOGRAM:
            return new FeatureHistogram(handle);
        case NAIVE:
            return new FeatureNaive(handle);
        default:
            throw new IllegalArgumentException("Unknown feature!");
        }
    }
    
    /**
     * Transforms array of arrays to flatten array
     * 
     * @param input
     * @return
     */
    private double[] getFlattenedArray(double[]... input) {
        
        // calculate size of flatten array
        int outputLength = 0;
        for(double[] part : input) {
            outputLength += part.length;
        }
        
        // copy into flatten array
        double[] output = new double[outputLength];
        int posOutput = 0;
        for(double[] part : input) {
            for(double value : part) {
                output[posOutput++] = value;
            }
        }
        return output;
    }
    

    /**
     * Creates a new instance
     * @param handle
     * @throws ParseException 
     */
    private double[] getVector(DataHandle handle, int row) throws ParseException {
        
        // Prepare
        double[] vector = new double[attributes.length];
        int index = 0;
        
        // For each attribute
        for (String attribute : attributes) {
            
            // Index
            int column = handle.getColumnIndexOf(attribute);

            // Obtain metadata
            DataType<?> _type = handle.getDefinition().getDataType(attribute);
            Class<?> _clazz = _type.getDescription().getWrappedClass();

            // Value
            double value = 0d;
            
            // Calculate depending on data type
            if (_clazz.equals(Long.class)) {
                
                // Handle data type represented as long
                Long _value = handle.getLong(row, column);
                value = _value != null ? _value : 0d; // TODO: how to handle null here
                
            } else if (_clazz.equals(Double.class)) {
                
                // Handle data type represented as double
                Double _value = handle.getDouble(row, column);
                value = _value != null ? _value : 0d; // TODO: how to handle null here
                
            } else if (_clazz.equals(Date.class)) {
                
                // Handle data type represented as date
                Date _value = handle.getDate(row, column);
                value = _value != null ? _value.getTime() : 0d; // TODO: how to handle null here
                
            } else if (_clazz.equals(String.class)) {
                
                // Map via dictionary
                value = dictionary.probe(attribute, handle.getValue(row, column));
                
            } else {
                throw new IllegalStateException("Unknown data type");
            }
            
            // Store
            vector[index++] = value;
        }
        
        // Done
        return vector;
    }
}