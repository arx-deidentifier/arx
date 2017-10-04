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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

/**
 * Implementation of the Mean Squared Error for microaggregated columns.
 * 
 * @author Fabian Prasser
 */
public class QualityModelColumnOrientedMSE extends QualityModel<QualityMeasureColumnOriented> {
    

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
    public QualityModelColumnOrientedMSE(WrappedBoolean interrupt,
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
        super(interrupt,
              progress,
              totalWorkload,
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
    public QualityMeasureColumnOriented evaluate() {
        
        // Prepare
        int[] indices = getIndices();
        DataHandle inputHandle = getInput();
        DataHandle outputHandle = getOutput();
        double[] result = new double[indices.length];
        double[] min = new double[indices.length];
        double[] max = new double[indices.length];

        // Progress
        setSteps(result.length);
        
        // For each column
        for (int i = 0; i < result.length; i++) {
            
            try {
                // Map
                int column = indices[i];
                
                // Convert
                double[] output = getNumbersFromNumericColumn(outputHandle, column);
                double[] input = null;
                if (output != null) {
                    input = getNumbersFromNumericColumn(inputHandle, column);
                }
                
                // Check
                if (input != null && output != null) {
                    
                    // Calculate min and max
                    double[] minmax = getMinMax(input);
                    double inmin = minmax[0];
                    double inmax = minmax[1];
                    
                    // 1 / N * SUM_i (x_i - y_i)^2
                    for (int index = 0; index < output.length; index++) {
                        
                        // For normalization
                        double mindiff = input[index] - inmin;
                        mindiff *= mindiff; // Square
                        double maxdiff = input[index] - inmax;
                        inmax *= inmax; // Square

                        // From output
                        double diff = 0d;
                        if (!Double.isNaN(output[index])) {
                            diff = input[index] - output[index];
                            diff *= diff; // Square
                        } else {
                            diff = Math.max(mindiff, maxdiff);
                        }
                        
                        // Store
                        result[i] += diff;
                        max[i] += Math.max(mindiff, maxdiff);

                        // Check
                        checkInterrupt();
                    }
                    
                // Not available
                } else {
                    result[i] = Double.NaN;
                }
            } catch (Exception e) {
                
                // Ignore silently
                result[i] = Double.NaN;
            }
            
            // Progress
            setStepPerformed();
        }

        // Progress
        setStepsDone();
        
        // Return
        return new QualityMeasureColumnOriented(outputHandle, indices, min, result, max);
    }

    /**
     * Returns minimum, maximum for the given column
     * @param data
     * @return
     */
    private double[] getMinMax(double[] data) {
        
        // Init
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        
        // Calculate min and max
        for (int i = 0; i < data.length; i++) {
            double value = data[i];
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        // Return
        return new double[]{min, max};
    }

    /**
     * Parses numbers from a numeric column
     * @param handle
     * @param column
     * @return
     */
    private double[] getNumbersFromNumericColumn(DataHandle handle, int column) {
        
        try {
            
            // Prepare
            String attribute = handle.getAttributeName(column);
            double[] result = new double[handle.getNumRows()];
            
            // Parse numbers
            if (handle.getDataType(attribute) instanceof DataTypeWithRatioScale) {

                QualityConfigurationValueParser<?> parser = QualityConfigurationValueParser.create(handle.getDataType(attribute));
                for (int row = 0; row < handle.getNumRows(); row++) {
                    String value = handle.getValue(row, column);
                    if (handle.isOutlier(row) || super.isSuppressed(column, value)) {
                        result[row] = Double.NaN;
                    } else {
                        result[row] = parser.getDouble(value);
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
}
