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

package org.deidentifier.arx.aggregates.quality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Base class for quality models
 * 
 * @author Fabian Prasser
 * 
 * @param <T>
 */
abstract class QualityModel<T> {

    /** Log */
    private static final double          LOG2         = Math.log(2);

    /** Input */
    private final DataHandle             input;

    /** Output */
    private final DataHandle             output;

    /** Grouped */
    private final Groupify<TupleWrapper> groupedInput;

    /** Grouped */
    private final Groupify<TupleWrapper> groupedOutput;

    /** Input */
    private final int[]                  indices;

    /** Flag */
    private final WrappedBoolean         interrupt;

    /** Counter */
    private final WrappedInteger         progress;

    /** Workload */
    private final int                    startWorkload;
    
    /** Workload */
    private final int                    totalWorkload;

    /** Hierarchies */
    private final String[][][]           hierarchies;

    /** Shares */
    private final QualityDomainShare[]   shares;

    /** Value */
    private final String                 suppressedValue;

    /** Roots */
    private final Map<Integer, String>   roots        = new HashMap<>();

    /** Steps */
    private int                          totalSteps   = 0;

    /** Steps */
    private int                          currentSteps = 0;

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param progress
     * @param totalWorkload
     * @param input
     * @param output
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    QualityModel(WrappedBoolean interrupt,
                 WrappedInteger progress,
                 int totalWorkload,
                 DataHandle input,
                 DataHandle output,
                 Groupify<TupleWrapper> groupedInput,
                 Groupify<TupleWrapper> groupedOutput,
                 String[][][] hierarchies,
                 QualityDomainShare[] shares,
                 int[] indices,
                 QualityConfiguration config) {
        
        // Store data
        this.input = input;
        this.output = output;
        this.groupedInput = groupedInput;
        this.groupedOutput = groupedOutput;
        this.indices = indices;
        this.shares = shares;
        this.hierarchies = hierarchies;
        this.interrupt = interrupt;
        this.progress = progress;
        this.startWorkload = progress.value;
        this.totalWorkload = totalWorkload;
        this.suppressedValue = config.getSuppressedValue();
        
        // Collect roots
        for (int index = 0; index < indices.length; index++) {
            int column = indices[index];
            String root = getRoot(hierarchies[index]);
            this.roots.put(column,  root);
        }
    }

    /**
     * Returns the root for the given hierarchy
     * @param strings
     * @return
     */
    private String getRoot(String[][] hierarchy) {
        Set<String> roots = new HashSet<>();
        for (String[] row : hierarchy) {
            roots.add(row[row.length - 1]);
        }
        return (roots.size() == 1) ? roots.iterator().next() : null;
    }
    
    /**
     * Checks whether an interruption happened.
     */
    protected void checkInterrupt() {
        if (interrupt.value) { throw new ComputationInterruptedException("Interrupted"); }
    }
    
    /**
     * Evaluates the utility measure
     * 
     * @return
     */
    protected abstract T evaluate();

    /**
     * Returns the domain shares
     */
    protected QualityDomainShare[] getDomainShares() {
        return shares;
    }

    /**
     * Returns grouped input
     */
    protected Groupify<TupleWrapper> getGroupedInput() {
        return groupedInput;
    }

    /**
     * Returns grouped output
     */
    protected Groupify<TupleWrapper> getGroupedOutput() {
        return groupedOutput;
    }

    /**
     * Returns the hierarchies
     */
    protected String[][][] getHierarchies() {
        return hierarchies;
    }
    
    /**
     * Returns relevant indices
     */
    protected int[] getIndices() {
        return indices;
    }

    /**
     * Returns input
     * 
     * @return
     */
    protected DataHandle getInput() {
        return this.input;
    }

    /**
     * Returns output
     * 
     * @return
     */
    protected DataHandle getOutput() {
        return this.output;
    }

    /**
     * Returns the suppression string
     * @return
     */
    protected String getSuppressionString() {
        return suppressedValue;
    }

    /**
     * Returns whether a value is suppressed
     * 
     * @param handle
     * @param row
     * @param column
     * @return
     */
    protected boolean isSuppressed(DataHandle handle, int row, int column) {

        // Check flag
        if (handle.isOutlier(row)) {
            return true;
        } else {
            return isSuppressed(column, handle.getValue(row, column));
        }
    }

    /**
     * We assume that an entry is suppressed, if all values are equal
     * 
     * @param entry
     * @return
     */
    protected boolean isSuppressed(DataHandle handle, int[] indices, int row) {

        // Check flag
        if (handle.isOutlier(row)) { return true; }

        // Check values
        for (int i = 1; i < indices.length; i++) {
            if (!handle.getValue(row, indices[i - 1]).equals(handle.getValue(row, indices[i]))) { return false; }
        }
        return true;
    }

    /**
     * We assume that an entry is suppressed, if all values are equal
     * 
     * @param entry
     * @return
     */
    protected boolean isSuppressed(Group<TupleWrapper> entry) {

        // Check flag
        if (entry.getElement().isSuppressed()) { return true; }

        // Check values
        String[] array = entry.getElement().getValues();
        for (int i = 1; i < array.length; i++) {
            if (!array[i - 1].equals(array[i])) { return false; }
        }
        return true;
    }

    /**
     * Returns whether a value is suppressed
     * 
     * @param column
     * @param value
     * @return
     */
    protected boolean isSuppressed(int column, String value) {
        return value.equals(suppressedValue) || value.equals(roots.get(column));
    }

    /**
     * Log base-2
     * 
     * @param d
     * @return
     */
    protected double log2(double d) {
        return Math.log(d) / LOG2;
    }

    /**
     * One step performed
     */
    protected void setStepPerformed() {
        this.currentSteps++;
        int value = (int)Math.round((double)totalWorkload * (double)currentSteps / (double)totalSteps);
        this.progress.value = startWorkload + value;
    }

    /**
     * Total number of steps
     * @param steps
     */
    protected void setSteps(int steps) {
        this.totalSteps = steps;
    }

    /**
     * All steps performed
     */
    protected void setStepsDone() {
        this.progress.value = startWorkload + totalWorkload;
    }

    /**
     * Returns a columns from the input and output dataset converted to numbers
     * @param input
     * @param output
     * @param hierarchy
     * @param column
     * @return
     */
    protected double[][] getColumnsAsNumbers(DataHandle input,
                                           DataHandle output,
                                           String[][] hierarchy,
                                           int column) {
        
        // Try to parse the input into a number
        double[] inputAsNumbers = getNumbersFromNumericColumn(input, column);
        double[] outputAsNumbers = null;
                
        // If this worked
        if (inputAsNumbers != null) {
            
            // Try to parse the output into a number
            outputAsNumbers = getNumbersFromNumericColumn(inputAsNumbers, output, column);

            // If this worked: return
            if (outputAsNumbers != null) {
                // NUMBER - NUMBER
                return new double[][]{inputAsNumbers, outputAsNumbers};
            }
            
            // Try to parse output based on numeric input
            outputAsNumbers = getRangeFromNumericColumn(inputAsNumbers, output, column);
            
            // If this worked: return
            if (outputAsNumbers != null) {
                // NUMBER - RANGE
                return new double[][]{inputAsNumbers, outputAsNumbers};
            }
            
            // Else: use the hierarchy
            outputAsNumbers = getNumbersFromNumericColumnAndHierarchy(input, inputAsNumbers, output, column, hierarchy);

            // If this worked: return
            if (outputAsNumbers != null) {
                // NUMBER - HIERARCHY
                return new double[][]{inputAsNumbers, outputAsNumbers};
            }
        }    
        
        // In all other cases: fall back to artificial ordinals
        inputAsNumbers = getNumbersFromHierarchy(input, column, hierarchy);
        outputAsNumbers = getNumbersFromHierarchy(output, column, hierarchy);
        
        // HIERARCHY - HIERARCHY
        return new double[][]{inputAsNumbers, outputAsNumbers};
    }

    /**
     * Returns a numeric representation, relying on the hierarchy
     * @param handle
     * @param column
     * @param hierarchy
     * @return
     */
    protected double[] getNumbersFromHierarchy(DataHandle handle,
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
            
            // Add min and max for suppressed values
            if (!min.containsKey(getSuppressionString())) {
                min.put(getSuppressionString(), 0);
            }
            if (!max.containsKey(getSuppressionString())) {
                max.put(getSuppressionString(), hierarchy.length - 1);
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
     * Parses numbers from a numeric input column
     * @param input
     * @param column
     * @return
     */
    protected double[] getNumbersFromNumericColumn(DataHandle input, int column) {
        
        try {
            
            // Prepare
            String attribute = input.getAttributeName(column);
            double[] result = new double[input.getNumRows() * 2];
            
            // Parse numbers
            if (input.getDataType(attribute) instanceof DataTypeWithRatioScale) {

                QualityConfigurationValueParser<?> parser = QualityConfigurationValueParser.create(input.getDataType(attribute));
                for (int row = 0; row < input.getNumRows(); row++) {
                    double number = parser.getDouble(input.getValue(row, column));
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
     * Parses numbers from a numeric output column
     * @param inputAsNumbers
     * @param output
     * @param column
     * @return
     */
    protected double[] getNumbersFromNumericColumn(double[] inputAsNumbers, DataHandle output, int column) {
        
        try {

            // Prepare
            String attribute = output.getAttributeName(column);
            double[] result = new double[output.getNumRows() * 2];
            double[] minmax = getMinMax(inputAsNumbers);
            double minimum = minmax[0];
            double maximum = minmax[1];
            
            // Parse numbers
            if (output.getDataType(attribute) instanceof DataTypeWithRatioScale) {

                QualityConfigurationValueParser<?> parser = QualityConfigurationValueParser.create(output.getDataType(attribute));
                for (int row = 0; row < output.getNumRows(); row++) {
                    
                    if (output.isOutlier(row)) {
                        result[row * 2] = minimum;
                        result[row * 2 + 1] = maximum;    
                    } else {   
                        double number = parser.getDouble(output.getValue(row, column));
                        result[row * 2] = number;
                        result[row * 2 + 1] = number;
                    }
                    
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
    protected double[] getNumbersFromNumericColumnAndHierarchy(DataHandle input,
                                                             double[] inputAsNumbers,
                                                             DataHandle output,
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
                if (isSuppressed(column, value)) {
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
     * Tries to parse numbers from output when there is a numeric input column
     * @param inputNumbers
     * @param output
     * @param column
     * @return
     */
    protected double[] getRangeFromNumericColumn(double[] inputNumbers,
                                               DataHandle output,
                                               int column) {
        
        try {
            
            // Prepare
            String attribute = output.getAttributeName(column);
            double[] result = new double[output.getNumRows() * 2];
            double[] minmax = getMinMax(inputNumbers);
            double minimum = minmax[0];
            double maximum = minmax[1];
            
            // Create a sample of the data
            List<String> sample = new ArrayList<>();
            for (int row = 0; row < output.getNumRows() && sample.size() < 50; row++) {
                if (!output.isOutlier(row)) {
                    sample.add(output.getValue(row, column));
                }
            }
            
            // Create parsers
            QualityConfigurationValueParser<?> valueParser = QualityConfigurationValueParser.create(output.getDataType(attribute));
            QualityConfigurationRangeParser rangeParser = QualityConfigurationRangeParser.getParser(valueParser, sample);
            
            // Parse
            for (int row = 0; row < output.getNumRows(); row++) {
                
                // Parse
                double[] range;
                if (output.isOutlier(row)) {
                    range = new double[]{minimum, maximum};
                } else {
                    String value = output.getValue(row, column);
                    if (isSuppressed(column, value)) {
                        range = new double[]{minimum, maximum};    
                    } else {
                        range = rangeParser.getRange(valueParser, value, minimum, maximum);
                    }
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
     * Returns minimum, maximum for the given column
     * @param column
     * @return
     */
    protected double[] getMinMax(double[] column) {
        
        // Init
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        
        // Calculate min and max
        for (int i = 0; i < column.length; i++) {
            double value = column[i];
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        // Return
        return new double[]{min, max};
    }
}
