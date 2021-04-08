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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;

import com.carrotsearch.hppc.IntIntOpenHashMap;

import org.deidentifier.arx.aggregates.StatisticsSummary;


// Doc for SMILE 1.3.0 https://javadoc.io/doc/com.github.haifengl/smile-core/1.3.0/index.html
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
public class ShadowModelMembershipRisk {

    // TODO for dev only - remove when finished
    public static long featureTime = 0;
    public static long timeMeasurement1 = 0;
    
    /** Supported feature types */
    public enum FeatureType{
        NAIVE, CORR, HIST, ALL
    }
    
    public enum ClassifierType {
        KNN, LR, RF
    }
    
    // Used to choose the sampling strategy 
    //TODO remove from final code
    private final static boolean INDEPENDENT_SAMPLES = true;
    
    /** Number of bins ot use for HIST feature */
    private final static int NUM_BINS = 10;
    
    /**
     * Returns an estimate of membership disclosure risks based on shadow models. 
     * All quasi-identifiers will be used to construct features used in the attack.
     * @param result ARXResult
     * @param outputHandle Output data
     * @param samplingFraction Fraction of records to sample randomly when building the shadow model
     * @param targetRow Row number of the target record
     * @param repetitions Number of training examples to create
     * @return
     */
    public double getShadowModelBasedMembershipRisk(DataHandle outputHandle,
                                                    double samplingFraction,
                                                    int targetRow,
                                                    int repetitions,
                                                    FeatureType featureType,
                                                    ClassifierType classifierType) {

        // Use all quasi-identifiers as relevant attributes
        return getShadowModelBasedMembershipRisk(outputHandle,
                                                 outputHandle.getDefinition().getQuasiIdentifyingAttributes(),
                                                 samplingFraction,
                                                 targetRow,
                                                 repetitions,
                                                 featureType,
                                                 classifierType);
    
    }

    
    /**
     * Create random sample of Ids excluding the target
     * 
     * @param availableIds
     * @param sampleSize
     * @return
     */
    private int[] createSample(List<Integer> availableIds, int sampleSize) {
        // Shuffle list
        Collections.shuffle(availableIds);
        
        int[] sample = new int[Math.min(sampleSize, availableIds.size())]; 
        for (int i = 0; i < sample.length; i++) {
            sample[i] = availableIds.get(i);
        }
        
        return sample;
    }
    
    /**
     * Create random sample of Ids including the target
     * 
     * @param availableIds
     * @param sampleSize
     * @return
     */
    private int[] createSample(List<Integer> availableIds, int sampleSize, int targetId) {
        // Shuffle list
        Collections.shuffle(availableIds);
        
        int[] sample = new int[Math.min(sampleSize, availableIds.size())]; 
        sample[0] = targetId; // TODO randomly decide on a position??
        for (int i = 1; i < sample.length; i++) {
            sample[i] = availableIds.get(i);
        }
        
        return sample;
    }
    
    /**
     * Returns an estimate of membership disclosure risks based on shadow models.
     * All provided attributes will be used to construct features used in the attack.
     * @param outputHandle Output data
     * @param attributes Attributes
     * @param samplingFraction Fraction of records to sample randomly when building the shadow model
     * @param targetRow Row number of the target record
     * @param repetitions Number of training examples to create
     * @return
     */
    public double getShadowModelBasedMembershipRisk(DataHandle outputHandle,
                                                    Set<String> attributes,
                                                    double samplingFraction,
                                                    int targetRow,
                                                    int repetitions,
                                                    FeatureType featureType,
                                                    ClassifierType classifierType) {

        // Various checks
        if (outputHandle == null) { throw new NullPointerException("Handle is null"); }
        if (!(outputHandle instanceof DataHandleOutput)) { throw new IllegalArgumentException("Handle must represent output data"); }
        if (attributes == null) { throw new NullPointerException("Attributes to consider must not be null"); }
        for (String attribute : attributes) {
            if (outputHandle.getColumnIndexOf(attribute) == -1) { throw new IllegalArgumentException(attribute + " is not an attribute"); }
        }
        if (targetRow <0 || targetRow > outputHandle.getNumRows() - 1) {
            throw new IllegalArgumentException("Row of target record is not in range");
        }
        
        // Construct column indices relating to attributes in ascending order
        int[] columns = new int[attributes.size()];
        int offset = 0;
        for (int i = 0; i < outputHandle.getNumColumns(); i++) {
            String attribute = outputHandle.getAttributeName(i);
            if (attributes.contains(attribute)) {
                columns[offset++] = i;
            }
        }
        
        // Create MetaData obhject
        MetaData metaData = new MetaData(outputHandle, columns);
        
        // Initialize arrays for features and labels
        double[][] xTrain = new double[repetitions*2][];
        int[] yTrain = new int[repetitions*2];
        
        
        // Create list of available indices excluding target
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < outputHandle.getNumRows(); i++) {
            if (i != targetRow) {
                indices.add(i);
            }
        }
        
        // calculate size of subsamples
        int sampleSize = (int)Math.round(samplingFraction * (double)outputHandle.getNumRows());
        
        // For each training example
        for (int repetition = 0; repetition < repetitions; repetition++) {
            
            int[] sampleExcludingTarget;
            int[] sampleIncludingTarget;
            
            if(INDEPENDENT_SAMPLES) {
                sampleExcludingTarget = createSample(indices, sampleSize);
                sampleIncludingTarget = createSample(indices, sampleSize, targetRow);
            } else {
                // Shuffle list
                Collections.shuffle(indices);
                
                // Create array of indices in sample excluding target
                sampleExcludingTarget = new int[Math.min(sampleSize, indices.size())]; // Just to make sure that nothing does wrong when samplingFraction = 100%
                for (int i = 0; i < sampleExcludingTarget.length; i++) {
                    sampleExcludingTarget[i] = indices.get(i);
                }
                
                // Create array of indices in sample including target
                // TODO same sized samples and two times draw
                sampleIncludingTarget = new int[sampleExcludingTarget.length + 1];
                System.arraycopy(sampleExcludingTarget, 0, sampleIncludingTarget, 0, sampleExcludingTarget.length);
                sampleIncludingTarget[sampleIncludingTarget.length-1] = targetRow;
                
                // Make sure that both sets are sorted
                // TODO Why?
                Arrays.sort(sampleExcludingTarget);
                Arrays.sort(sampleIncludingTarget);
            }
            
            // Anonymize both datasets
            DataHandle datasetExcludingTarget = getAnonymizedOutput(outputHandle, sampleExcludingTarget);
            //System.out.println(Arrays.toString(sampleExcludingTarget));
            //System.out.println("Excluding Sample before: "+ sampleExcludingTarget.length + " | After Anon: " + datasetExcludingTarget.getNumRows());
            //printHead10(datasetExcludingTarget, columns);
            
            DataHandle datasetIncludingTarget = getAnonymizedOutput(outputHandle, sampleIncludingTarget);
            //System.out.println(Arrays.toString(sampleIncludingTarget));
            //System.out.println("Including Sample before: "+ sampleIncludingTarget.length + " | After Anon: " + datasetIncludingTarget.getNumRows());
            //printHead10(datasetIncludingTarget, columns);


            // Get features and store in feature-array
            long tempTime = System.currentTimeMillis();
            xTrain[repetition*2] = new FeatureSet(datasetExcludingTarget, columns, metaData).getFeatures(featureType);
            xTrain[repetition*2+1] = new FeatureSet(datasetIncludingTarget, columns, metaData).getFeatures(featureType);
            featureTime += System.currentTimeMillis() - tempTime;
            
            // Store labels in label-array
            yTrain[repetition*2] = 0;
            yTrain[repetition*2+1] = 1;
            
        }
        
        // TODO: dev stuff - remove eventually
        if (1 == 1) {
            double[] featuresAttackedDataset = new FeatureSet(outputHandle,
                                                              columns,
                                                              metaData).getFeatures(featureType);
            return 0d;
        }
        
        // Initialize and train classifier
        SoftClassifier<double[]> classifier;
        
        switch (classifierType) {
        case KNN:
            
            // TODO relocated to main() - and to ARX-config eventually
            int numberOfNeighbors = 5; // Privacy Mirage papper := 5
            
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
        
        // Create summarizing features
        long tempTime = System.currentTimeMillis();
        double[] featuresAttackedDataset = new FeatureSet(outputHandle, columns, metaData).getFeatures(featureType);
        featureTime += System.currentTimeMillis() - tempTime;
        
        // Predict label
        double[] probabilities = new double[] {0, 0};
        int _result = classifier.predict(featuresAttackedDataset, probabilities);
        
        System.out.println("Target ID: " + targetRow + "; Assigned Label: " + _result + "; Probabilities: " + Arrays.toString(probabilities));
        return probabilities[0];

    }

    /**
     * Make sure that all privacy models are also enforced on the sample of output data
     * 
     * @param outputHandle
     * @param sample
     * @return
     */
    private DataHandle getAnonymizedOutput(DataHandle outputHandle, int[] sample) {

        // Extract sample from output
        Data input = getCopy(outputHandle, sample); // Use subset of output data as input
        
        // Anonymize the sample
        ARXConfiguration config = outputHandle.getConfiguration().clone(); // Use same anonymization configuration
        input.getDefinition().read(outputHandle.getDefinition()); // Use same data specification
        
        // Make sure that attributes are not transformed
        for (String attribute : input.getDefinition().getQuasiIdentifyingAttributes()) {
            
            // Construct hierarchy with one level, containing all values
            int column = input.getHandle().getColumnIndexOf(attribute);
            String[] values = input.getHandle().getDistinctValues(column);
            DefaultHierarchy hierarchy = Hierarchy.create();
            for (String value : values) {
                hierarchy.add(value);
            }
            
            // Set value and fix generalization level
            input.getDefinition().setHierarchy(attribute, hierarchy);
            input.getDefinition().setMinimumGeneralization(attribute, 0);
            input.getDefinition().setMaximumGeneralization(attribute, 0);
        }
        
        // TODO: Might be needed, because if limit is too small, everything might be suppressed
        // TODO: Is this really a good idea? Hard to say...
        config.setSuppressionLimit(1.0);
        
        // Perform final anonymization
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        try {
            return anonymizer.anonymize(input, config).getOutput();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a copy of data
     * @param handle
     * @param sample 
     * @return
     */
    private Data getCopy(DataHandle handle, int[] sample) {
        List<String[]> rows = new ArrayList<>();
        rows.add(handle.iterator().next());
        for (int row : sample) {
            rows.add(getRow(handle, row));
        }
        return Data.create(rows);
    }

    /**
     * Extracts a row from the handle
     * @param handle
     * @param row
     * @return
     */
    private String[] getRow(DataHandle handle, int row) {
        String[] result = new String[handle.getNumColumns()];
        for (int column = 0; column < result.length; column++) {
            result[column] = handle.getValue(row, column);
        }
        return result;
    }
    
    /**
     * Extracts a row from the handle
     * @param handle
     * @param row
     * @param columns
     * @return
     */
    private String[] getRow(DataHandle handle, int row, int[] columns) {
        String[] result = new String[columns.length];
        for (int column = 0; column < result.length; column++) {
            result[column] = handle.getValue(row, columns[column]);
        }
        return result;
    }
    
    //TODO Remove eventually
    /**
     * Simple print function for debugging
     * 
     * @param handle
     * @param columns
     */
    private void printHead10(DataHandle handle, int[] columns) {
        for(int i = 0; i < 10; i++){
            for(int c = 0; c < columns.length; c++) {
                System.out.print(handle.getValue(i, columns[c]) + " | ");
            }
            System.out.println();
        }
    }
    
    /**
     * Class used as enclosure for the features and their calculation.
     * 
     * @author Thierry Meurers
     *
     */
    class FeatureSet {
        
        /** DataHandle */
        private DataHandle handle;
        
        /** Columns to consider */
        private int[] columns;
        
        /** Naive feature vector */
        private double[] naiveFeatures;
        
        /** Correlation feature vector */
        private double[] correlationFeatures;
        
        /** Histogram feature vector */
        private double[] histogramFeatures;
        
        /** MetaData of reference population */
        private MetaData metaData;
        
        /**
         * Creates a new FeatureSet object
         * 
         * @param handle
         * @param codeMap
         */
        FeatureSet (DataHandle handle, int[] columns, MetaData metaData){
            this.handle = handle;
            this.columns = columns;
            this.metaData = metaData;
        }
        
        /**
         * Returns a set of features
         * 
         * @return
         */
        double[] getFeatures(FeatureType type) {
            
            switch (type){
            case NAIVE:
                if(naiveFeatures == null) {
                    naiveFeatures = calculateNaiveFeatures();
                }
                return naiveFeatures;
            case CORR:
                if(correlationFeatures == null) {
                    correlationFeatures = calculateCorrelationFeatures();
                }
                return correlationFeatures;
            case HIST:
                if(histogramFeatures == null) {
                    histogramFeatures = calculateHistogramFeatures();
                }
                return histogramFeatures;
            case ALL:
                return flattenArray(new double[][]{getFeatures(FeatureType.NAIVE), getFeatures(FeatureType.CORR), getFeatures(FeatureType.HIST)});
            default:
                throw new IllegalArgumentException("Unknown Feature Type");
            }
        }
        
        
        /**
         * Calculates histogram features.
         * For categorical attributes this simply refers to the counts of distinct values.
         * For continuous and ordinal attributes, the domain of the values is separated into 10 bins.
         * 
         * @return
         */
        private double[] calculateHistogramFeatures() {
        
            // Initialize array used to store the parts of the result
            double[][] result = new double[columns.length][];
            
            for (int i = 0; i < columns.length; i++) {

                // Obtain attribute details
                int c = columns[i];
                String attributeName = handle.getAttributeName(c);
                DataType<?> _type = handle.getDefinition().getDataType(attributeName);
                Class<?> _clazz = _type.getDescription().getWrappedClass();
                
                if (_clazz.equals(Double.class)) {
                    
                    double min = metaData.getMinValue(i);
                    double max = metaData.getMaxValue(i);
                    double binSize = (max - min) / NUM_BINS;
                    
                    double[] freqs = new double[NUM_BINS];
                    Double[] values = getColumnAsDouble(c);
                    
                    /*
                    System.out.println("MIN: " + min + " | MAX: " + max);
                    
                    
                    for(int d = 0; d < NUM_BINS; d++) {
                        System.out.println(d + " | " + (d*binSize+min) + " | " );
                    }
                    */
                    
                    for(Double v : values) {
                        // calculate bin
                        int bin = (int) ((v - min) / binSize);
                        // check range
                        if(0 > bin || bin > NUM_BINS) {
                            throw new RuntimeException("Value out of histogram range");
                        }
                        // TODO Dirty quick-fix
                        if(bin == NUM_BINS) {
                            bin -= 1;
                        }
                        // inc freq of bin
                        freqs[bin] += 1d;
                    }
                    
                    result[i] = freqs;
                    
                } else if (_clazz.equals(String.class)) {
                    
                    // Count frequencies of values
                    int column = columns[i];
                    IntIntOpenHashMap map = new IntIntOpenHashMap();
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        int code = handle.internalGetEncodedValue(row, column, false); // Beware that code can be -1
                        map.putOrAdd(code, 1, 1);
                    }
                    
                    int[] availableValues = metaData.getAvialableValues(i);
                    double[] freqs = new double[availableValues.length];
                    
                    // Count freqs of all available values.
                    for(int v = 0; v < availableValues.length; v++) {
                        freqs[v] = map.getOrDefault(availableValues[v], 0);
                    }
                    
                    result[i] = freqs;
                    //System.out.println(Arrays.toString(freqs));
                }
                //System.out.println(attributeName + " --> " + Arrays.toString(result[i]));
            }
            // flatten array
            double[] flatResult = flattenArray(result);
            //System.out.println(Arrays.toString(flatResult));
            return flatResult;
        }
        
        /**
         * Calculates correlation features using Pearson's product-moment correlation.
         * All columns of continuous attributes are directly used for correlation calculation.
         * Categorical and ordinal attributes are transfered to a sparse representation
         * were each value becomes an own column and whether (or not) the value applies to a row
         * is indicated by the value 1d (or 0d).
         * 
         * @return
         */
        //TODO: Add support for ALL attribute Types
        private double[] calculateCorrelationFeatures() {
            
            // Initialize list used to store the parts of the input matrix to assemble
            List<List<Double>> preparedColumns = new ArrayList<List<Double>>();
            
            for (int i = 0; i < columns.length; i++) {

                // Obtain attribute details
                int c = columns[i];
                String attributeName = handle.getAttributeName(c);
                DataType<?> _type = handle.getDefinition().getDataType(attributeName);
                Class<?> _clazz = _type.getDescription().getWrappedClass();
                
                if (_clazz.equals(Double.class)) {
                    
                    // Directly add column to list of prepared columns
                    preparedColumns.add(Arrays.asList(getColumnAsDouble(c)));
                    
                } else if (_clazz.equals(String.class)) {
                    
                    //TODO replace with more efficient Code
                    
                    //Transfer column to Int-Representation
                    List<Integer> columnAsIntLabels = new ArrayList<Integer>();
                    for(int row = 0; row < handle.getNumRows(); row++) {
                        columnAsIntLabels.add(handle.internalGetEncodedValue(row, c, false));
                    }
                    /* 
                    // Remove duplicates
                    List<Integer> uniqueLabelsList = new ArrayList<Integer>(new LinkedHashSet<Integer>(columnAsIntLabels));
                    sparseColumnRepresentation.remove(uniqueLabelsList.remove(0));
                    */
                    
                    int[] uniqueValues = this.metaData.getAvialableValues(i);
                    
                    
                    // Initialize sparse representation with zeros
                    Map<Integer, List<Double>> sparseColumnRepresentation = new HashMap<>();
                    for(int label : uniqueValues) {
                        sparseColumnRepresentation.put(label, new ArrayList<Double>(Collections.nCopies(handle.getNumRows(), 0d)));
                    }
                    
                    // Fill sparse representation with ones (were applicable)
                    for(int row = 0; row < handle.getNumRows(); row++) {
                        //List<Integer> temp = sparseColumnRepresentation.get(columnAsIntLabels.get(row));
                        sparseColumnRepresentation.get(columnAsIntLabels.get(row)).set(row, 1d);
                    }
                     
                    // Copy to list of prepared columns
                    // Ignore first label for copying into result frame - dont needed for correlation as one value can be condiedered the default
                    for(int j = 1; j < uniqueValues.length; j++) {
                        preparedColumns.add(sparseColumnRepresentation.get(uniqueValues[j]));
                    }
                }
            }
            
            //printMatrix(preparedColumns);
            //System.out.println("-------------");
            
            // 1. transfer lists to primitive arrays and transpose
            // transpose Arrays as the 
            double[][] corrIn = new double[handle.getNumRows()][];
            for(int i = 0; i < handle.getNumRows(); i++) {
                corrIn[i] = new double[preparedColumns.size()];
                for(int j = 0; j < preparedColumns.size(); j++) {
                    corrIn[i][j] = preparedColumns.get(j).get(i);
                }
            }
            
            //printMatrix(corrIn);
            //System.out.println("-------------");
            
            // Calculate correlation coefficients
            long tempTime = System.currentTimeMillis();
            double[][] corrOut = new PearsonsCorrelation().computeCorrelationMatrix(corrIn).getData();
            timeMeasurement1 += System.currentTimeMillis() - tempTime;
            
            //printMatrix(corrOut);
            //System.out.println("-------------");
            
            // flatten array
            double[] flatResult = flattenArray(corrOut);
            System.out.println(Arrays.toString(flatResult));
            return flatResult;
            
        }
        
        //TODO remove - only for debugging
        private void printMatrix(double[][] in) {

            for (int r = 0; r < in[0].length; r++){
                for (int c = 0; c < in.length; c++) 
                    {
                       System.out.print(in[c][r]);
                       System.out.print(" | ");
                    }
                    System.out.println("/");
                }

        }

        //TODO remove - only for debugging
        private void printMatrix(List<List<Double>> in) {

            for (int r = 0; r < in.get(0).size(); r++){
                for (int c = 0; c < in.size(); c++) 
                    {
                       System.out.print(in.get(c).get(r));
                       System.out.print(" | ");
                    }
                    System.out.println();
                }

        }

        /**
         * Calculates naive features for attributes. 
         * Each column is projected to 3 features.
         * Categorical:
         * [0] --> Number of unique elements
         * [1] --> Label of most frequent element*
         * [2] --> Label of least frequent element*
         * (*) if multiple elements qualify, return element which appeared first in dataset
         * 
         * Numeric:
         * [0] --> mean
         * [1] --> median
         * [2] --> var
         * 
         * @param col
         * @return
         */
        private double[] calculateNaiveFeatures() {

            double[][] result = new double[columns.length][];

            // Let ARX compute all statistics
            // TODO really required for -all- attributes?
            long tempTime = System.currentTimeMillis();
            Map<String, StatisticsSummary<?>> statistics = handle.getStatistics().getSummaryStatistics(false);
            timeMeasurement1 += System.currentTimeMillis() - tempTime;
            
            for (int i = 0; i < columns.length; i++) {
                // Obtain attribute name
                String attributeName = handle.getAttributeName(columns[i]);

                // Obtain statistics
                StatisticsSummary<?> summary = statistics.get(attributeName);
                DataType<?> _type = handle.getDefinition().getDataType(attributeName);
                Class<?> _clazz = _type.getDescription().getWrappedClass();

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
                    int column = columns[i];
                    IntIntOpenHashMap map = new IntIntOpenHashMap();
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        int code = handle.internalGetEncodedValue(row, column, false); // Beware that code can be -1
                        map.putOrAdd(code, 1, 1);
                    }
                    
                    // Determine codes with highest and lowest frequencies
                    int minFreq = Integer.MAX_VALUE;
                    int maxFreq = Integer.MIN_VALUE;
                    
                    
                    // Access map buffers
                    final int [] keys = map.keys;
                    final int [] values = map.values;
                    final boolean [] states = map.allocated;
                     
                    // For each slot
                    for (int j = 0; j < states.length; j++) {
                        if (states[j]) {
                            if (values[j] < minFreq) {
                                minFreq = values[j];
                                leastFreq = (double) keys[j];
                            } if (values[j] > maxFreq) {
                                maxFreq = values[j];
                                mostFreq = (double) keys[j];
                            }
                        }
                    }
                    
                    // Get number of assigned keys
                    uniqueElements = (double) map.assigned;

                    
                } else {
                    throw new IllegalStateException("Unknown data type");
                }
                
                // Switch feature type
                if (mean != null && var != null && median != null) {
                    result[i] = new double[]  {mean, median, var};
                    
                } else if (mostFreq != null && leastFreq != null && uniqueElements != null) {
                    result[i] = new double[]  {uniqueElements, mostFreq, leastFreq};
                    
                } else {
                    throw new IllegalStateException("Features unavailable");
                }
                
                //System.out.println(attributeName + " (" + _clazz +") --> " + Arrays.toString(result[i]));  
            }
            
            // flatten array
            double[] flatResult = flattenArray(result);
            //System.out.println(Arrays.toString(flatResult));
            return flatResult;
        }
        
        /**
         * Extracts a column from the handle
         * @param handle
         * @param column
         * @return
         */
        private String[] getColumnAsString(int column) {
            String[] result = new String[handle.getNumRows()];
            for (int row = 0; row < handle.getNumRows(); row++) {
                result[row] = handle.getValue(row, column);
            }
            return result;
        }
        
        /**
         * Extracts a column from the handle
         * @param handle
         * @param column
         * @return
         */
        private Double[] getColumnAsDouble(int column) {
            Double[] result = new Double[handle.getNumRows()];
            for (int row = 0; row < handle.getNumRows(); row++) {
                try {
                    result[row] = handle.getDouble(row, column);
                } catch (ParseException e) {
                    throw new RuntimeException("Error reading double column");
                }
            }
            return result;
        }
        
        /**
         * Gets int representation for string label
         * 
         * @param str
         * @return
         */
        Integer getCode(int row, int col) {
            return handle.internalGetEncodedValue(row, col, false);  
        }

        /**
         * Gets int representation for multiple string labels
         * 
         * @param str
         * @ret
         */
        List<Integer> getCode(int col) {
            List<Integer> result = new ArrayList<>();
            for(int row = 0; row < handle.getNumRows(); row++) {
                result.add(getCode(row, col));
            }
            return result;
        }

        /**
         * Transforms array of arrays to flatten array
         * 
         * @param input
         * @return
         */
        private double[] flattenArray(double[][] input) {
            
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
        
    }
    
    /**
     * MetaData Object that is used to calculate and encapsul information of the reference population.
     * 
     * 
     * @author Thierry
     *
     */
    class MetaData {

        /** DataHandle */
        private DataHandle handle;

        /** Columns to consider */
        private int[]      columns;

        /** Available Values */
        private int[][]    availableValues;

        private double[]   minValues;

        private double[]   maxValues;

        /**
         * Creates a new MetaData Object
         * 
         * @param handle
         * @param codeMap
         */
        MetaData(DataHandle handle, int[] columns) {
            this.handle = handle;
            this.columns = columns;
        }

        /**
         * Return all available values for a certain column.
         * 
         * @param column
         * @return
         */
        int[] getAvialableValues(int column) {
            if (availableValues == null) {
                calculateMetaData();
            }
            return availableValues[column];
        }

        /**
         * Returns min value of column.
         * 
         * @param column
         * @return
         */
        double getMinValue(int column) {
            if (minValues == null) {
                calculateMetaData();
            }
            return minValues[column];
        }
        
        /**
         * Returns max value of column.
         * 
         * @param column
         * @return
         */
        double getMaxValue(int column) {
            if (maxValues == null) {
                calculateMetaData();
            }
            return maxValues[column];
        }

        /**
         * Calculates all available values for all categorical columns.
         * 
         * @return
         */
        //TODO #1 Add support for all datatypes
        //TODO #2 speed up process of getting min and max
        private void calculateMetaData() {

            // Prepare results
            int[][] availableValues = new int[columns.length][];
            double[] minValues = new double[columns.length];
            double[] maxValues = new double[columns.length];

            for (int i = 0; i < columns.length; i++) {

                // Obtain attribute details
                int c = columns[i];
                String attributeName = handle.getAttributeName(c);
                DataType<?> _type = handle.getDefinition().getDataType(attributeName);
                Class<?> _clazz = _type.getDescription().getWrappedClass();

                Map<String, StatisticsSummary<?>> statistics = handle.getStatistics()
                                                                     .getSummaryStatistics(false);

                if (_clazz.equals(String.class)) {
                    // TODO replace with more efficient Code

                    // Transfer column to Int-Representation
                    List<Integer> columnAsIntLabels = new ArrayList<Integer>();
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        columnAsIntLabels.add(handle.internalGetEncodedValue(row, c, false));
                    }

                    // Remove duplicates
                    List<Integer> uniqueLabelsList = new ArrayList<Integer>(new LinkedHashSet<Integer>(columnAsIntLabels));
                    availableValues[i] = ArrayUtils.toPrimitive(uniqueLabelsList.toArray(new Integer[uniqueLabelsList.size()]));
                } else if (_clazz.equals(Double.class)) {

                    StatisticsSummary<?> stats = statistics.get(attributeName);
                    minValues[i] = (double) stats.getMinAsValue();
                    maxValues[i] = (double) stats.getMaxAsValue();
                }
            }
            this.availableValues = availableValues;
            this.minValues = minValues;
            this.maxValues = maxValues;
        }

    }
}
