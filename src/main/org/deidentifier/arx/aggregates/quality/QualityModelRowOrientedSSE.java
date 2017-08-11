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

package org.deidentifier.arx.aggregates.quality;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implementation of the Sum of Squared Errors introduced in:<br>
 * Soria-Comas, Jordi, et al.:
 * "t-closeness through microaggregation: Strict privacy with enhanced utility preservation."
 * IEEE Transactions on Knowledge and Data Engineering 27.11 (2015): 3098-3110.
 * 
 * @author Fabian Prasser
 */
public class QualityModelRowOrientedSSE extends QualityModel<QualityMeasureRowOriented> {

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param input
     * @param output
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    public QualityModelRowOrientedSSE(WrappedBoolean interrupt,
                                      DataHandleInternal input,
                                      DataHandleInternal output,
                                      Groupify<TupleWrapper> groupedInput,
                                      Groupify<TupleWrapper> groupedOutput,
                                      String[][][] hierarchies,
                                      QualityDomainShare[] shares,
                                      int[] indices,
                                      QualityConfiguration config) {
        super(interrupt,
              input,
              output,
              groupedInput,
              groupedOutput,
              hierarchies,
              shares,
              indices,
              config);
    }

    @Override
    public QualityMeasureRowOriented evaluate() {
 
        try {
                
            // Prepare
            int[] indices = getIndices();
            List<double[]> columns1 = new ArrayList<>();
            List<double[]> columns2 = new ArrayList<>();
            List<Double> stdDevs = new ArrayList<>();
            String[][][] hierarchies = getHierarchies();
            
            // Collect
            for (int index = 0; index < indices.length; index++) {
                try {
                    int column = indices[index];
                    double[][] columnsAsNumbers = getColumnsAsNumbers(getInput(), getOutput(), 
                                                                      hierarchies[index], column);
                    if (columnsAsNumbers != null) {
                        double stdDev = getStandardDeviation(columnsAsNumbers[0]);
                        columns1.add(columnsAsNumbers[0]);
                        columns2.add(columnsAsNumbers[1]);
                        stdDevs.add(stdDev);
                    }
                    
                } catch (Exception e) {
                    // Fail silently
                }
            }
            
            // Check
            if (columns1.isEmpty() || columns2.isEmpty() || stdDevs.isEmpty()) {
                return new QualityMeasureRowOriented();
            }
            
            // Real distance
            double realDistance = getEuclideanDistance(columns1.toArray(new double[columns1.size()][]),
                                                       columns2.toArray(new double[columns2.size()][]),
                                                       stdDevs.toArray(new Double[stdDevs.size()]));
            
            // Maximal distance
            double maxDistance = getEuclideanDistance(columns1.toArray(new double[columns1.size()][]),
                                                      stdDevs.toArray(new Double[stdDevs.size()]));
            
            // Normalize
            realDistance /= (double)columns1.size();
            realDistance /= (double)getOutput().getNumRows();
            maxDistance /= (double)columns1.size();
            maxDistance /= (double)getOutput().getNumRows();
            
            // Return
            return new QualityMeasureRowOriented(0d, realDistance, maxDistance);
            
        } catch (Exception e) {
            return new QualityMeasureRowOriented();
        }
    }
    
    /**
     * Returns a columns from the input and output dataset converted to numbers
     * @param input
     * @param output
     * @param hierarchy
     * @param column
     * @return
     */
    private double[][] getColumnsAsNumbers(DataHandleInternal input,
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
     * Returns the maximal sum of the euclidean distance between all records
     * 
     * @param input
     * @param inputStdDev
     * @return
     */
    private double getEuclideanDistance(double[][] input, 
                                        Double[] inputStdDev) {
        
        // Calculate minimum and maximum
        double[] minimum = new double[input.length];
        double[] maximum = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            double[] minmax = getMinMax(input[i]);
            minimum[i] = minmax[0];
            maximum[i] = minmax[1];
        }

        // Prepare
        double resultOverall = 0d;
        
        // For each row
        for(int row=0; row<input[0].length; row+=2){
            
            // Check
            checkInterrupt();
            
            // For each column
            double resultRow = 0;
            for(int column=0; column<input.length; column++){
                
                double minimum1 = input[column][row];
                double maximum1 = input[column][row + 1];
                double minimum2 = minimum[column];
                double maximum2 = maximum[column];
                maximum1 = (maximum2 - minimum1) > (minimum1 - minimum2) ? maximum2 : minimum2;
                double temp = (inputStdDev[column] == 0d) ? 0d : (minimum1 - maximum1) / inputStdDev[column];
                resultRow += (temp * temp);
            }
            
            // Summarize
            resultOverall += Math.sqrt(resultRow);
        }
        
        // Return
        return resultOverall;
    }
    
    /**
     * Returns the sum of the euclidean distance between all records
     * 
     * @param input
     * @param output
     * @param inputStdDev
     * @return
     */
    private double getEuclideanDistance(double[][] input, 
                                        double[][] output,
                                        Double[] inputStdDev) {

        // Prepare
        double resultOverall = 0d;
        
        // For each row
        for(int row=0; row<input[0].length; row+=2){
                
            // For each column
            double resultRow = 0;
            for(int column=0; column<input.length; column++){
                
                double minimum1 = input[column][row];
                double maximum1 = input[column][row + 1];
                double minimum2 = output[column][row];
                double maximum2 = output[column][row + 1];
                maximum1 = (maximum2 - minimum1) > (minimum1 - minimum2) ? maximum2 : minimum2;
                double temp = (inputStdDev[column] == 0d) ? 0d : (minimum1 - maximum1) / inputStdDev[column];
                resultRow += (temp * temp);
            }
            
            // Summarize
            resultOverall += Math.sqrt(resultRow);
        }
        
        // Return
        return resultOverall;
    }
    
    /**
     * Returns minimum, maximum for the input column
     * @param inputColumnAsNumbers
     * @return
     */
    private double[] getMinMax(double[] inputColumnAsNumbers) {
        
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
                if (isSuppressed(value)) {
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
        if (isSuppressed(value)) {
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
     * Returns the standard deviation for input data
     * @param inputColumnAsNumbers
     * @return
     */
    private double getStandardDeviation(double[] inputColumnAsNumbers) {
        
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
