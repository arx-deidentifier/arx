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
                    
                    // For normalization
                    double avg = getAvg(input);
                    
                    // 1 / N * SUM_i (x_i - y_i)^2
                    for (int index = 0; index < output.length; index++) {
                        
                        // Calculate
                        double maxdiff = input[index] - avg;
                        double diff = (!Double.isNaN(output[index])) ? input[index] - output[index] : maxdiff;
                        
                        // Square and store
                        result[i] += diff * diff;
                        max[i] += maxdiff * maxdiff;

                        // Check
                        checkInterrupt();
                    }
                    
                    // We use the average (dataset centroid) for normalization. When very weird things happen, 
                    // the actual result may even be worse. That's why we sanitize results here.
                    if (max[i] < result[i]) {
                        max[i] = result[i];
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
     * Returns the average of the given vector
     * @param input
     * @return
     */
    private double getAvg(double[] input) {
        double result = 0d;
        for (double value : input) {
            result += value;
        }
        return result / (double)input.length;
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
