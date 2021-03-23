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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;


import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.io.CSVHierarchyInput;

import com.carrotsearch.hppc.IntIntOpenHashMap;

import org.deidentifier.arx.aggregates.StatisticsSummary;

import smile.classification.DecisionTree.SplitRule;
import smile.classification.RandomForest;
import smile.data.Attribute;


/**
 * Estimate risks for membership attacks, using shadow models
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 */
public class ShadowModelMembershipRisk {

    
    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    @Deprecated
    private static Data createData(final String dataset) throws IOException {
        
        // Load data
        Data data = Data.create("data/" + dataset + ".csv", StandardCharsets.UTF_8, ';');
        
        // Read generalization hierarchies
        FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.matches(dataset + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        // Create definition
        File testDir = new File("data/");
        File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        for (File file : genHierFiles) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                CSVHierarchyInput hier = new CSVHierarchyInput(file, StandardCharsets.UTF_8, ';');
                String attributeName = matcher.group(1);
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
            }
        }
        
        return data;
    }
    
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
                                                    int repetitions) {

        // Use all quasi-identifiers as relevant attributes
        return getShadowModelBasedMembershipRisk(outputHandle,
                                                 outputHandle.getDefinition().getQuasiIdentifyingAttributes(),
                                                 samplingFraction,
                                                 targetRow,
                                                 repetitions);
    
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
                                                    int repetitions) {

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

        // ---------------------------------------
        // ---------------------------------------
        // TODO: Prepare classification model here
        // TODO: Before entering the loop
        // ---------------------------------------
        // ---------------------------------------
        
        
        double[][] xTrain = new double[repetitions*2][];
        int[] yTrain = new int[repetitions*2];
        
        // For each training example
        for (int repetition = 0; repetition < repetitions; repetition++) {
            
            // Create list of available indices excluding target
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < outputHandle.getNumRows(); i++) {
                if (i != targetRow) {
                    indices.add(i);
                }
            }
            // Shuffle list
            Collections.shuffle(indices);
            
            // Create array of indices in sample excluding target
            int sampleSize = (int)Math.round(samplingFraction * (double)outputHandle.getNumRows());
            int[] sampleExcludingTarget = new int[Math.min(sampleSize, indices.size())]; // Just to make sure that nothing does wrong when samplingFraction = 100%
            for (int i = 0; i < sampleExcludingTarget.length; i++) {
                sampleExcludingTarget[i] = indices.get(i);
            }
            
            // Create array of indices in sample including target
            int[] sampleIncludingTarget = new int[sampleExcludingTarget.length + 1];
            System.arraycopy(sampleExcludingTarget, 0, sampleIncludingTarget, 0, sampleExcludingTarget.length);
            sampleIncludingTarget[sampleIncludingTarget.length-1] = targetRow;
            
            // Make sure that both sets are sorted
            // TODO Why?
            Arrays.sort(sampleExcludingTarget);
            Arrays.sort(sampleIncludingTarget);
            
            // Anonymize both datasets
            DataHandle datasetExcludingTarget = getAnonymizedOutput(outputHandle, sampleExcludingTarget);
            //System.out.println(Arrays.toString(sampleExcludingTarget));
            //System.out.println("Excluding Sample before: "+ sampleExcludingTarget.length + " | After Anon: " + datasetExcludingTarget.getNumRows());
            //printHead10(datasetExcludingTarget, columns);
            
            DataHandle datasetIncludingTarget = getAnonymizedOutput(outputHandle, sampleIncludingTarget);
            //System.out.println(Arrays.toString(sampleIncludingTarget));
            //System.out.println("Including Sample before: "+ sampleIncludingTarget.length + " | After Anon: " + datasetIncludingTarget.getNumRows());
            //printHead10(datasetIncludingTarget, columns);

            // ---------------------------------------
            // ---------------------------------------
            // TODO: Implement method "getSummary"
            // ---------------------------------------
            // ---------------------------------------
            
            
            
            // Create summarizing features
            //int[] featuresExcludingTarget = getSummary(datasetExcludingTarget, columns);
            //int[] featuresIncludingTarget = getSummary(datasetIncludingTarget, columns);

            xTrain[repetition*2] = new FeatureSet(datasetExcludingTarget, columns).getNaiveFeatures();
            yTrain[repetition*2] = 0;
            xTrain[repetition*2+1] = new FeatureSet(datasetIncludingTarget, columns).getNaiveFeatures();
            yTrain[repetition*2+1] = 1;
            
            // ---------------------------------------
            // ---------------------------------------
            // TODO: Train classification method
            // ---------------------------------------
            // ---------------------------------------
        }
        
        // TODO relocated to main() - and to ARX-config eventually
        int numberOfTrees = 100; // sklearn default := 100 | ARX default := 500
        int maxNumberOfLeafNodes = 100; // sklean default := +INF | ARX default = 100;
        int minSizeOfLeafNodes = 1; // sklean default := 1 | ARX default := 5
        int numberOfVariablesToSplit = (int) Math.floor(Math.sqrt(xTrain[0].length)); // sklearn := auto (i.e. sqrt(#features)) | ARX default := 0
        double subSample = 1d; // skleanr --> provided at total number (2) | ARX default := 1d
        SplitRule splitRule = SplitRule.GINI; // sklearn default := GINI |ARX dedault: = GINI
        
        RandomForest rm = new RandomForest((Attribute[])null, xTrain, yTrain, numberOfTrees, maxNumberOfLeafNodes, minSizeOfLeafNodes, numberOfVariablesToSplit, subSample, splitRule, null);

        // Create summarizing features
        double[] featuresAttackedDataset  = new FeatureSet(outputHandle, columns).getNaiveFeatures();

        int _result = rm.predict(featuresAttackedDataset, new double[2]);
        System.out.println(_result);
        
        // ---------------------------------------
        // ---------------------------------------
        // TODO: Use classifier to attack output dataset using its features
        // ---------------------------------------
        // ---------------------------------------
        

        // ---------------------------------------
        // ---------------------------------------
        // TODO: Calculate any form of meaningful output (currently a double)
        // ---------------------------------------
        // ---------------------------------------
        
        return _result;
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
    

        
    /**
     * Create a summary vector
     * @param handle
     * @param columns
     * @return
     */
    @Deprecated
    private int[] getSummary(DataHandle handle, int[] columns) {

        // ---------------------------------------
        // ---------------------------------------
        // TODO: Implement method "getSummary"
        // ---------------------------------------
        // ---------------------------------------
        
        // TODO This is just a meaningless example summarizing hash codes to get the code running
        int[] result = new int[columns.length];
        for (int row = 0; row < handle.getNumRows(); row++) {
            String[] array = getRow(handle, row, columns);
            for (int column = 0; column < result.length; column++) {
                result[column] += array[column].hashCode();
            }
        }
        return result;
    }

    /**
     * Simple print function for debugging
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
        
        /**
         * Creates a new FeatureSet
         * 
         * @param handle
         * @param codeMap
         */
        FeatureSet (DataHandle handle, int[] columns){
            this.handle = handle;
            this.columns = columns;
        }
        
        /**
         * Return naive features
         * 
         * @return
         */
        double[] getNaiveFeatures() {
            if(naiveFeatures == null) {
                naiveFeatures = calculateNaiveFeatures();
            }
            return naiveFeatures;
        }
        
        /**
         * Return histogram features
         * 
         * @return
         */
        double[] getHistogramFeatures() {
            //TODO
            return histogramFeatures;
        }
        
        /**
         * Return correlation features
         * 
         * @return
         */
        double[] getCorrelationFeatures() {
            //TODO
            return correlationFeatures;
        }
        
        /**
         * Return all features
         * 
         * @return
         */
        double[] getAllFeatures() {
            return flattenArray(new double[][]{getNaiveFeatures(), getCorrelationFeatures(), getHistogramFeatures()});
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
            Map<String, StatisticsSummary<?>> statistics = handle.getStatistics().getSummaryStatistics(false);

            
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
        private String[] getColumnAsString(DataHandle handle, int column) {
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
        private Double[] getColumnAsDouble(DataHandle handle, int column) {
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
}
