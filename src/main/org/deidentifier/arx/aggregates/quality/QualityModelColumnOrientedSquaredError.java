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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

/**
 * Implementation of the mean squared error for individual columns
 * 
 * @author Fabian Prasser
 */
public class QualityModelColumnOrientedSquaredError extends QualityModel<QualityMeasureColumnOriented> {
    

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param progress
     * @param totalWorkload
     * @param input
     * @param output
     * @param suppressedInput
     * @param suppressedOutput
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    public QualityModelColumnOrientedSquaredError(WrappedBoolean interrupt,
                                                  WrappedInteger progress,
                                                  int totalWorkload,
                                                  DataHandle input,
                                                  DataHandle output,
                                                  int suppressedInput,
                                                  int suppressedOutput,
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
                      suppressedInput,
                      suppressedOutput,
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
        double[] result = new double[indices.length];
        double[] min = new double[indices.length];
        double[] max = new double[indices.length];
        String[][][] hierarchies = getHierarchies();
        
        // Progress
        setSteps(result.length);
        
        // For each column
        for (int i = 0; i < result.length; i++) {
            
            try {
                
                // Map
                int column = indices[i];
                double[] input = null;
                double[] output = null;
                
                // Parse
                try {
                    double[][] columnsAsNumbers = getColumnsAsNumbers(getInput(), getOutput(), 
                                                                      hierarchies[i], column);
                    if (columnsAsNumbers != null) {
                        input = columnsAsNumbers[0];
                        output= columnsAsNumbers[1];
                    }
                } catch (Exception e) {
                    // Fail silently
                }

                // Check
                if (input != null && output != null) {
                    
                    // For normalization
                    double[] minmax = getMinMax(input);
                    double minimum = minmax[0];
                    double maximum = minmax[1];
                    
                    // 1 / N * SUM_i (x_i - y_i)^2
                    for (int index = 0; index < output.length; index += 2) {
                        
                        // Calculate maximum distance
                        double maxMin = input[index] - minimum;
                        double maxMax = input[index] - maximum;
                        
                        // Square for abs()
                        maxMin *= maxMin;
                        maxMax *= maxMax;
                        double maxDiff = Math.max(maxMin, maxMax);
                        
                        // If present
                        if ((!Double.isNaN(output[index]))) {
                            
                            // Try bounds
                            double diff1 = input[index] - output[index];
                            double diff2 = input[index] - output[index + 1];
                            
                            // Square for abs()
                            diff1 *= diff1;
                            diff2 *= diff2;
                            double diff = Math.max(diff1, diff2);
                            
                            // Store
                            result[i] += Math.min(diff, maxDiff);
                            
                        } else {

                            // Store
                            result[i] += maxDiff;
                        }
                        
                        // Max
                        max[i] += maxDiff;

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
        return new QualityMeasureColumnOriented(getOutput(), indices, min, result, max);
    }
}
