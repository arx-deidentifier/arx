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
package org.deidentifier.arx.aggregates.utility;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Helper class for implementing utility models
 * 
 * @author Fabian Prasser
 */
class UtilityHelper {

    /** Flag */
    private final WrappedBoolean       interrupt;
    /** Value */
    private final String               suppressedValue;

    /**
     * Creates a new instance
     * @param interrupt
     * @param config
     */
    UtilityHelper(WrappedBoolean interrupt, UtilityConfiguration config) {
        this.interrupt = interrupt;
        this.suppressedValue = config.getSuppressedValue();
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
     * Converts a value into a double
     * @param datatype
     * @param value
     * @return
     */
    private double getDouble(DataTypeWithRatioScale<?> datatype, String value) {
        
        if (datatype instanceof ARXDecimal) {
            ARXDecimal type = (ARXDecimal)datatype;
            return type.toDouble(type.parse(value));
            
        } else if (datatype instanceof ARXInteger) {
            ARXInteger type = (ARXInteger)datatype;
            return type.toDouble(type.parse(value));
            
        } else if (datatype instanceof ARXDate) {
            ARXDate type = (ARXDate)datatype;
            return type.toDouble(type.parse(value));
        } else {
            throw new IllegalArgumentException("Unknown data type");
        }
    }
    
    /**
     * Returns a numeric representation, relying on the hierarchy
     * @param handle
     * @param column
     * @param hierarchy
     * @return
     */
    private double[] getNumbersFromHierarchy(DataHandleInternal handle,
                                             int column,
                                             String[][] hierarchy) {

        try {
            
            // Prepare
            double[] result = new double[handle.getNumRows() * 2];
            
            // Build maps
            Map<String, Integer> min = new HashMap<>();
            Map<String, Integer> max = new HashMap<>();
            
            // For each level
            for (int level = 0; level < hierarchy[0].length; level++) {
                for (int id = 0; id < hierarchy.length; id++) {
                    
                    // Access
                    String value = hierarchy[id][level];
                    
                    // Min
                    Integer minval = min.get(value);
                    minval = minval == null ? id : Math.min(minval, id);
                    min.put(value, minval);
                    
                    // Max
                    Integer maxval = max.get(value);
                    maxval = maxval == null ? id : Math.max(maxval, id);
                    max.put(value, maxval);
                }
                
                // Check
                checkInterrupt();
            }
            
            // Map values
            for (int row = 0; row < handle.getNumRows(); row++) {
                String value = handle.getValue(row, column);
                result[row * 2] = min.get(value);
                result[row * 2 + 1] = max.get(value);
                
                // Check
                checkInterrupt();
            }
            
            // Return
            return result;
            
        } catch (Exception e) {
            
            // Fail silently
            return null;
        }
    }
    
    /**
     * Tries to parse numbers from output when there is a numeric input column
     * @param input
     * @param inputNumbers
     * @param output
     * @param column
     * @return
     */
    private double[] getNumbersFromNumericColumn(DataHandleInternal input,
                                                 double[] inputNumbers,
                                                 DataHandleInternal output,
                                                 int column) {
        
        try {
            
            // Prepare
            String attribute = input.getAttributeName(column);
            double[] result = new double[input.getNumRows() * 2];
            double[] minmax = getMinMax(inputNumbers);
            double minimum = minmax[0];
            double maximum = minmax[1];
            DataTypeWithRatioScale<?> type = (DataTypeWithRatioScale<?>)input.getDataType(attribute);
            
            // Parse
            for (int row = 0; row < input.getNumRows(); row++) {
                
                // Parse
                double[] range;
                if (output.isOutlier(row)) {
                    range = new double[]{minimum, maximum};
                } else {
                    String value = output.getValue(row, column);
                    range = getRange(value, type, minimum, maximum);    
                }
                
                result[row * 2] = range[0];
                result[row * 2 + 1] = range[1];
                
                // Check
                checkInterrupt();
            }
            
            // Return
            return result;
            
        } catch (Exception e) {
            
            // Fail silently
            return null;
        }
    }

    /**
     * Parses numbers from a numeric input column
     * @param input
     * @param column
     * @return
     */
    private double[] getNumbersFromNumericColumn(DataHandleInternal input, int column) {
        
        try {
            
            // Prepare
            String attribute = input.getAttributeName(column);
            double[] result = new double[input.getNumRows() * 2];
            
            // Parse numbers
            if (input.getDataType(attribute) instanceof DataTypeWithRatioScale) {
                DataTypeWithRatioScale<?> type = (DataTypeWithRatioScale<?>)input.getDataType(attribute);
                for (int row = 0; row < input.getNumRows(); row++) {
                    double number = getDouble(type, input.getValue(row, column));
                    result[row * 2] = number;
                    result[row * 2 + 1] = number;
                    
                    // Check
                    checkInterrupt();
                }
                
                // Return
                return result;
            } else {
                
                // Return
                return null;
            }
        } catch (Exception e) {
            
            // Fail silently
            return null;
        }
    }

    /**
     * Uses numeric input and a hierarchy to construct ranges
     * @param input
     * @param inputAsNumbers
     * @param output
     * @param column
     * @param hierarchy
     * @return
     */
    private double[] getNumbersFromNumericColumnAndHierarchy(DataHandleInternal input,
                                                             double[] inputAsNumbers,
                                                             DataHandleInternal output,
                                                             int column,
                                                             String[][] hierarchy) {

        try {
            
            // Prepare
            double[] result = new double[input.getNumRows() * 2];
            
            // Build maps
            Map<String, Double> min = new HashMap<>();
            Map<String, Double> max = new HashMap<>();
            double overallMin = Double.MAX_VALUE;
            double overallMax = -Double.MAX_VALUE;
            
            // For each output value
            for (int row = 0; row < output.getNumRows(); row++) {
                
                // Access
                String value = output.getValue(row, column);
                double number = inputAsNumbers[row * 2];
                overallMin = Math.min(overallMin, number);
                overallMax = Math.max(overallMax, number);
                
                // Min
                Double minval = min.get(value);
                minval = minval == null ? number : Math.min(minval, number);
                min.put(value, minval);
                
                // Max
                Double maxval = max.get(value);
                maxval = maxval == null ? number : Math.max(maxval, number);
                max.put(value, maxval);
                
                // Check
                checkInterrupt();
            }
            
            // Map values
            for (int row = 0; row < output.getNumRows(); row++) {
                
                // Check for interrupts
                checkInterrupt();
                
                // Check 1
                if (output.isOutlier(row)) {
                    result[row * 2] = overallMin;
                    result[row * 2 + 1] = overallMax;
                    continue;
                }
                
                String value = output.getValue(row, column);
                
                // Check 2
                if (value.equals(suppressedValue)) {
                    result[row * 2] = overallMin;
                    result[row * 2 + 1] = overallMax;
                    continue;
                }
                
                // Map using hierarchy
                result[row * 2] = min.get(value);
                result[row * 2 + 1] = max.get(value);
            }
            
            // Return
            return result;
            
        } catch (Exception e) {
            
            // Fail silently
            return null;
        }
    }
    
    /**
     * Parses different forms of transformed values
     * @param value
     * @param type 
     * @return
     * @throws ParseException 
     */
    private double[] getRange(String value, DataTypeWithRatioScale<?> type, double minimum, double maximum) throws ParseException {

        double min, max;
        
        // Suppressed
        if (value.equals(suppressedValue)) {
            min = minimum;
            max = maximum;
            
        // Masked
        } else if (value.contains("*")) {
            min = Double.valueOf(value.replace('*', '0'));
            max = Double.valueOf(value.replace('*', '9'));
            
        // Interval
        } else if (value.startsWith("[") && value.endsWith("[")) {
            min = Double.valueOf(value.substring(1, value.indexOf(",")).trim());
            max = Double.valueOf(value.substring(value.indexOf(",") + 1, value.length() - 1).trim()) - 1d;

        // Interval
        } else if (value.startsWith("[") && value.endsWith("]")) {
            min = Double.valueOf(value.substring(1, value.indexOf(";")).trim());
            max = Double.valueOf(value.substring(value.indexOf(";") + 1, value.length() - 1).trim());

        // Upper bound
        } else if (value.startsWith(">") && !value.startsWith(">=")) {
            min = Double.valueOf(value.substring(1, value.length()));
            min += 1d; // TODO only valid for integer values   
            max = maximum;
            
        // Upper bound
        } else if (value.startsWith(">=")) {        
            min = Double.valueOf(value.substring(2, value.length()));
            max = maximum;
            
        // Lower bound
        } else if (value.startsWith("<") && !value.startsWith("<=")) {          
            min = minimum; 
            max = Double.valueOf(value.substring(1, value.length()));
            max -= -1d; // TODO only valid for integer values   
            
        // Lower bound
        } else if (value.startsWith("<=")) {            
            min = minimum; 
            max = Double.valueOf(value.substring(2, value.length()));
            
        // Set
        } else if (value.startsWith("{") && value.endsWith("}")) {
            
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            value = value.replace('{', ' ').replace('}', ' ');
            for (String part : value.split(",")) {
                part = part.trim();
                if (!part.equals("")) {
                    min = Math.min(min, Double.valueOf(part));
                    max = Math.max(max, Double.valueOf(part));
                }
            }
            
        // Ungeneralized
        } else {
            min = getDouble(type, value);
            max = min;
        }
        
        // Truncate values to the actual range of values in the input dataset
        // to prevent erroneous high distance values
        max = Math.min(max, maximum);
        min = Math.max(min, minimum);
        
        // Return
        return new double[]{min, max};
    }

    /**
     * Returns a columns from the input and output dataset converted to numbers
     * @param input
     * @param output
     * @param hierarchy
     * @param column
     * @return
     */
    double[][] getColumnsAsNumbers(DataHandleInternal input,
                                   DataHandleInternal output,
                                   String[][] hierarchy,
                                   int column) {
        
        // Try to parse the input into a number
        double[] inputAsNumbers = getNumbersFromNumericColumn(input, column);
        double[] outputAsNumbers = null;
                
        // If this worked
        if (inputAsNumbers != null) {
            
            // Try to parse output based on numeric input
            outputAsNumbers = getNumbersFromNumericColumn(input, inputAsNumbers, output, column);
            
            // If this worked: return
            if (outputAsNumbers != null) {
                return new double[][]{inputAsNumbers, outputAsNumbers};
            }
            
            // Else: use the hierarchy
            outputAsNumbers = getNumbersFromNumericColumnAndHierarchy(input, inputAsNumbers, output, column, hierarchy);

            // If this worked: return
            if (outputAsNumbers != null) {
                return new double[][]{inputAsNumbers, outputAsNumbers};
            }
        }    
        
        // In all other cases: fall back to artificial ordinals
        inputAsNumbers = getNumbersFromHierarchy(input, column, hierarchy);
        outputAsNumbers = getNumbersFromHierarchy(output, column, hierarchy);
        return new double[][]{inputAsNumbers, outputAsNumbers};
    }
    
    /**
     * Returns domain shares for the handle
     * @param handle
     * @param indices
     * @return
     */
    UtilityDomainShare[] getDomainShares(DataHandleInternal handle, int[] indices) {

        // Prepare
        UtilityDomainShare[] shares = new UtilityDomainShare[indices.length];
        String[][][] hierarchies = getHierarchies(handle, indices);
        
        // Compute domain shares
        for (int i=0; i<shares.length; i++) {
            
            try {
                
                // Extract info
                String[][] hierarchy = hierarchies[i];
                String attribute = handle.getAttributeName(indices[i]);
                HierarchyBuilder<?> builder = handle.getDefinition().getHierarchyBuilder(attribute);
                
                // Create shares for redaction-based hierarchies
                if (builder != null && (builder instanceof HierarchyBuilderRedactionBased) &&
                    ((HierarchyBuilderRedactionBased<?>)builder).isDomainPropertiesAvailable()){
                    shares[i] = new UtilityDomainShareRedaction((HierarchyBuilderRedactionBased<?>)builder);
                    
                // Create fallback-shares for materialized hierarchies
                // TODO: Interval-based hierarchies are currently not compatible
                } else {
                    shares[i] = new UtilityDomainShareRaw(hierarchy, suppressedValue);
                }
                
            } catch (Exception e) {
                // Ignore silently
                shares[i] = null;
            }
        }

        // Return
        return shares;
    }

    /**
     * Builds a generalization function mapping input values to the given level of the hierarchy
     * 
     * @param hierarchies
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    Map<String, String>[] getGeneralizationFunctions(String[][][] hierarchies, int index) {
        
        // Prepare
        Map<String, String>[] result = new HashMap[hierarchies.length];

        // For each dimension
        for (int level = 0; level < hierarchies[index][0].length; level++) {
            Map<String, String> map = new HashMap<String, String>();
            for (int row = 0; row < hierarchies[index].length; row++) {
                map.put(hierarchies[index][row][0], hierarchies[index][row][level]);
            }
            result[level] = map;
        }
        
        // Return
        return result;
    }

    /**
     * Returns a groupified version of the dataset
     * 
     * @param handle
     * @param indices
     * @return
     */
    Groupify<TupleWrapper> getGroupify(DataHandleInternal handle, int[] indices) {
        
        // Prepare
        int capacity = handle.getNumRows() / 10;
        capacity = capacity > 10 ? capacity : 10;
        Groupify<TupleWrapper> groupify = new Groupify<TupleWrapper>(capacity);
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {
            TupleWrapper tuple = new TupleWrapper(handle, indices, row, false);
            groupify.add(tuple);
            checkInterrupt();
        }
        
        return groupify;
    }

    /**
     * Returns hierarchies, creates trivial hierarchies if no hierarchy is found.
     * Adds an additional level, if there is no root node
     * 
     * @param handle
     * @param indices
     * @return
     */
    String[][][] getHierarchies(DataHandleInternal handle, int[] indices) {
        
        String[][][] hierarchies = new String[indices.length][][];
        
        // Collect hierarchies
        for (int i=0; i<indices.length; i++) {
            
            // Extract and store
            String attribute = handle.getAttributeName(indices[i]);
            String[][] hierarchy = handle.getDefinition().getHierarchy(attribute);
            
            // If not empty
            if (hierarchy != null && hierarchy.length != 0 && hierarchy[0] != null && hierarchy[0].length != 0) {
                
                // Clone
                hierarchies[i] = hierarchy.clone();
                
            } else {
                
                // Create trivial hierarchy
                String[] values = handle.getDistinctValues(indices[i]);
                hierarchies[i] = new String[values.length][2];
                for (int j = 0; j < hierarchies[i].length; j++) {
                    hierarchies[i][j][0] = values[j];
                    hierarchies[i][j][1] = suppressedValue;
                }
            }
        }

        // Fix hierarchy (if suppressed character is not contained in generalization hierarchy)
        for (int j=0; j<indices.length; j++) {
            
            // Access
            String[][] hierarchy = hierarchies[j];
            
            // Check if there is a problem
            Set<String> values = new HashSet<String>();
            for (int i = 0; i < hierarchy.length; i++) {
                String[] levels = hierarchy[i];
                values.add(levels[levels.length - 1]);
            }
            
            // There is a problem
            if (values.size() > 1 || !values.iterator().next().equals(this.suppressedValue)) {
                for(int i = 0; i < hierarchy.length; i++) {
                    hierarchy[i] = Arrays.copyOf(hierarchy[i], hierarchy[i].length + 1);
                    hierarchy[i][hierarchy[i].length - 1] = this.suppressedValue;
                }
            }
            
            // Replace
            hierarchies[j] = hierarchy;
            
            // Check
            checkInterrupt();
        }
        
        // Return
        return hierarchies;
    }

    /**
     * Returns indices of quasi-identifiers
     * 
     * @param handle
     * @return
     */
    int[] getIndicesOfQuasiIdentifiers(DataHandleInternal handle) {
        int[] result = new int[handle.getDefinition().getQuasiIdentifyingAttributes().size()];
        int index = 0;
        for (String qi : handle.getDefinition().getQuasiIdentifyingAttributes()) {
            result[index++] = handle.getColumnIndexOf(qi);
        }
        Arrays.sort(result);
        return result;
    }
    
    /**
     * Returns the inverse generalization function
     * @param hierarchies
     * @param index
     * @return
     */
    Map<String, Integer> getInverseGeneralizationFunction(String[][][] hierarchies, int index) {

        // Prepare
        Map<String, Integer> result = new HashMap<>();

        for (int col = 0; col < hierarchies[index][0].length; col++) {
            for (int row = 0; row < hierarchies[index].length; row++) {
                String value = hierarchies[index][row][col];
                if (!result.containsKey(value)) {
                    result.put(value, col);
                }
            }
            
            // Check
            checkInterrupt();
        }
        
        // Return
        return result;
    }

    /**
     * Returns minimum, maximum for the input column
     * @param inputColumnAsNumbers
     * @return
     */
    double[] getMinMax(double[] inputColumnAsNumbers) {
        
        // Init
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        
        // Calculate min and max
        for (int i = 0; i < inputColumnAsNumbers.length; i += 2) {
            double value = inputColumnAsNumbers[i];
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        // Return
        return new double[]{min, max};
    }
    

    /**
     * Returns precisions
     * @param handle
     * @param indices
     * @return
     */
    Map<String, Double>[] getPrecision(DataHandleInternal handle, int[] indices) {

        // Prepare
        @SuppressWarnings("unchecked")
        Map<String, Double>[] precisions = new Map[indices.length];
        String[][][] hierarchies = getHierarchies(handle, indices);
        
        for (int i=0; i<precisions.length; i++) {
            
            try {
                
                // Extract info
                String[][] hierarchy = hierarchies[i];
                
                // Calculate precision
                Map<String, Double> precision = new HashMap<String, Double>();
                for (int col = 0; col < hierarchy[0].length; col++) {
                    for (int row = 0; row < hierarchy.length; row++) {
                        String value = hierarchy[row][col];
                        if (!precision.containsKey(value)) {
                            precision.put(value, (double)col / ((double)hierarchy[0].length - 1d));
                        }
                    }
                    
                    // Check
                    checkInterrupt();
                }
                
                // Store
                precisions[i] = precision;
                
            } catch (Exception e) {
                
                // Drop silently
                precisions[i] = null;
            }
        }

        // Return
        return precisions;
    }
    

    /**
     * Returns the standard deviation for input data
     * @param inputColumnAsNumbers
     * @return
     */
    double getStandardDeviation(double[] inputColumnAsNumbers) {
        
        // Calculate mean
        double mean = 0d;
        for (int i = 0; i < inputColumnAsNumbers.length; i += 2) {
            double value = inputColumnAsNumbers[i];
            mean += value;
        }
        mean /= (double)(inputColumnAsNumbers.length / 2);
        
        // Calculate standard deviation
        double stdDev = 0d;
        for (int i = 0; i < inputColumnAsNumbers.length; i += 2) {
            double value = inputColumnAsNumbers[i];
            double temp = value - mean;
            temp = temp * temp;
            stdDev += temp;
        }
        stdDev /= (double)(inputColumnAsNumbers.length / 2);
        stdDev = Math.sqrt(stdDev);
        
        // Return
        return stdDev;
    }
}
