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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.ShadowModelBenchmarkSetup.BenchmarkDataset;

/**
 * Estimate risks for membership attacks, using shadow models
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 */
public class ShadowModelMembershipRisk {

    /**
     * Main entry point
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        // TODO: The current implementation will not work, when a model with a data subset is being used
        // TODO: Examples: d-presence or k-map.
        
        // TODO: Maybe not anonymize the output again? Might also be realistic to assume that the adversary just
        // TODO: transforms the data in a way that she feels fits to known output, and doesn't care whether privacy 
        // TODO: models are satisfied.
        
        // Example scenario
        
        // Create dataset
        Data data = ShadowModelBenchmarkSetup.getData(BenchmarkDataset.TEXAS_10);
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(1));
        config.setSuppressionLimit(0.0d);
        
        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        
        DataHandle output = anonymizer.anonymize(data, config).getOutput();
        
        // Perform risk assessment
        ShadowModelMembershipRisk model = new ShadowModelMembershipRisk();
        
        
        // TODO by setting repetitions to 0 the training is disabled - done for developing
        model.getShadowModelBasedMembershipRisk(output, 0.01d, 0, 0);
    }
    
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
        
        // codemap for uniform mapping between string-labels and int representation
        CodeMap codeMap = new CodeMap();
        
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
            sampleIncludingTarget[sampleIncludingTarget.length-1] = targetRow;
            
            // Make sure that both sets are sorted
            Arrays.sort(sampleExcludingTarget);
            Arrays.sort(sampleIncludingTarget);
            
            // Anonymize both datasets
            DataHandle datasetExcludingTarget = getAnonymizedOutput(outputHandle, sampleExcludingTarget);
            DataHandle datasetIncludingTarget = getAnonymizedOutput(outputHandle, sampleIncludingTarget);

            // ---------------------------------------
            // ---------------------------------------
            // TODO: Implement method "getSummary"
            // ---------------------------------------
            // ---------------------------------------
            
            // Create summarizing features
            int[] featuresExcludingTarget = getSummary(datasetExcludingTarget, columns);
            int[] featuresIncludingTarget = getSummary(datasetIncludingTarget, columns);

            // ---------------------------------------
            // ---------------------------------------
            // TODO: Train classification method
            // ---------------------------------------
            // ---------------------------------------
        }
        

        // Create summarizing features
        int[] featuresOutput = getSummary(outputHandle, columns);
        new FeatureSet(outputHandle, codeMap).getNaiveFeatures();

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
        
        return 0d;
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
     * Simple class used to map categorical string values to numeric labels
     */
    class CodeMap {
        
        /** Label --> Int Mapping */
        private Map<String, Integer> codemap = new TreeMap<>();
        
        /** Next int to use for map */
        private Integer currentPos = 0;
        
        /**
         * Gets int representation for string label
         * 
         * @param str
         * @return
         */
        Integer getCode(String str) {
            if(!codemap.containsKey(str)) {
                codemap.put(str, currentPos++);
            }
            return codemap.get(str);
        }

        /**
         * Gets int representation for multiple string labels
         * 
         * @param str
         * @ret
         */
        List<Integer> getCode(List<String> list) {
            List<Integer> result = new ArrayList<>();
            for(String str : list) {
                result.add(getCode(str));
            }
            return result;
        }
        
        /**
         * Fancy represenation of code map
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Iterator<Entry<String, Integer>> iter = codemap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Integer> entry = iter.next();
                sb.append(entry.getKey());
                sb.append(" -> ");
                sb.append(entry.getValue());
                if (iter.hasNext()) {
                    sb.append(",\n");
                }
            }
            return sb.toString();
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
        
        /** CodeMap for String -> Int mapping */
        private CodeMap codeMap;
        
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
        FeatureSet (DataHandle handle, CodeMap codeMap){
            this.handle = handle;
            this.codeMap = codeMap;
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
            return Arrays.stream(new double[][]{getNaiveFeatures(), getCorrelationFeatures(), getHistogramFeatures()}).flatMapToDouble(Arrays::stream).toArray();
        }
              
        /**
         * Calculates naive features for attributes. 
         * Each column is projected to 3 features.
         * Categorical:
         * [0] --> Number of unique elements
         * [1] --> Label of most frequent element
         * [2] --> Label of least frequent element
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
            
            double[][] result = new double[handle.getNumColumns()][];
            
            for (int i = 0; i < handle.getNumColumns(); i++) {

                String attributeName = handle.getAttributeName(i);
                DataType<?> dt = handle.getDataType(attributeName);
                //TODO Avoid dirty string fix
                switch(dt+"") {
                    case "String":
                        String[] col = getColumnAsString(handle, i);
                        
                        // Map to Integer labels
                        List<Integer> labelList = codeMap.getCode(Arrays.asList(col));
                        // Remove duplicates 
                        //TODO Maybe use method of DataHandle instead
                        List<Integer> uniqueLabelsList = new ArrayList<Integer>(new LinkedHashSet<Integer>(labelList));
                        
                        // count occurences and determin most and least frequent element
                        Map<Integer, Integer> labelOccMap = new TreeMap<>();
                        for(Integer label : uniqueLabelsList) {
                            labelOccMap.put(label, Collections.frequency(labelList, label));
                        }
                        //TODO Use sort instead (to avoid min=max)
                        Integer mostFreqLabel = Collections.max(labelOccMap.entrySet(), Map.Entry.comparingByValue()).getKey();
                        Integer leastFreqLabel = Collections.min(labelOccMap.entrySet(), Map.Entry.comparingByValue()).getKey();
                        
                        // return result
                        result[i] = new double[] {uniqueLabelsList.size(), mostFreqLabel, leastFreqLabel};   
                        
                        
                        break;
                    case "Decimal":
                        Double[] colDouble = getColumnAsDouble(handle, i);

                        double[] colPrimitive = ArrayUtils.toPrimitive(colDouble);
                        
                        double mean = new Mean().evaluate(colPrimitive);
                        double median = new Median().evaluate(colPrimitive);
                        double var = new Variance().evaluate(colPrimitive);
                        
                        result[i] = new double[] {mean, median, var};       
                        

                        break;
                    default:
                        System.out.println("Unsupported DT");
                        break;
                }
            System.out.println(attributeName + " ("+ dt + ") --> " + Arrays.toString(result[i]));  

            }

            // flatten array
            double[] flatResult = Arrays.stream(result).flatMapToDouble(Arrays::stream).toArray();
            System.out.println(Arrays.toString(flatResult));
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
        
    }
}
