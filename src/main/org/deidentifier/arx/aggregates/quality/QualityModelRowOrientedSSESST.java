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
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

/**
 * SSE / SST as described in Solanas, Agusti, Antoni Martinez-Balleste, and J. Domingo-Ferrer.<br> 
 * V-MDAV: a multivariate microaggregation with variable group size.<br>
 * 17th COMPSTAT Symposium of the IASC, Rome. 2006.
 * 
 * @author Fabian Prasser
 */
public class QualityModelRowOrientedSSESST extends QualityModel<QualityMeasureRowOriented> {

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
    public QualityModelRowOrientedSSESST(WrappedBoolean interrupt,
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
    public QualityMeasureRowOriented evaluate() {
 
        try {
                
            // Prepare
            int[] indices = getIndices();
            List<double[]> columns1 = new ArrayList<>();
            List<double[]> columns2 = new ArrayList<>();
            String[][][] hierarchies = getHierarchies();

            // Progress
            setSteps(indices.length + 2);
            
            // Collect
            for (int index = 0; index < indices.length; index++) {
                try {
                    int column = indices[index];
                    double[][] columnsAsNumbers = getColumnsAsNumbers(getInput(), getOutput(), 
                                                                      hierarchies[index], column);
                    if (columnsAsNumbers != null) {
                        columns1.add(columnsAsNumbers[0]);
                        columns2.add(columnsAsNumbers[1]);
                    }
                    
                } catch (Exception e) {
                    // Fail silently
                }

                // Progress
                setStepPerformed();
            }
            
            // Check
            if (columns1.isEmpty() || columns2.isEmpty()) {

                // Progress
                setStepsDone();
                
                // Return
                return new QualityMeasureRowOriented();
            }
            
            // SSE distance
            double sse = getSSE(columns1.toArray(new double[columns1.size()][]),
                                columns2.toArray(new double[columns2.size()][]));

            // Progress
            setStepPerformed();
            
            // SST distance
            double sst = getSST(columns1.toArray(new double[columns1.size()][]),
                                columns2.toArray(new double[columns2.size()][]));

            // Progress
            setStepsDone();
            
            // Return
            return new QualityMeasureRowOriented(0d, sse, sst);
            
        } catch (Exception e) {

            // Progress
            setStepsDone();
            
            // Silently drop exceptions
            return new QualityMeasureRowOriented();
        }
    }
    
    /**
     * Returns the SSE
     * 
     * @param input
     * @param output
     * @return
     */
    private double getSSE(double[][] input, 
                          double[][] output) {

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
                double inputVal = (minimum1 + maximum1) / 2d;
                double outputVal = (minimum2 + maximum2) / 2d;
                resultRow += Math.pow(inputVal - outputVal, 2d);
            }
            
            // Summarize
            resultOverall += resultRow;
        }
        
        // Return
        return resultOverall;
    }

    /**
     * Returns the SST
     * 
     * @param input
     * @param output
     * @return
     */
    private double getSST(double[][] input, 
                          double[][] output) {
     
        // Prepare
        double[] centroid = new double[input.length];
        
        // Calculate centroid
        // For each row
        for(int row=0; row<input[0].length; row+=2){
                
            // For each column
            for(int column=0; column<input.length; column++){
                double minimum = input[column][row];
                double maximum = input[column][row + 1];
                double value = (minimum + maximum) / 2d;
                centroid[column] += value;
            }
        }
        // For each column
        for(int column=0; column<input.length; column++){
            centroid[column] /= (double)input[0].length / 2d;
        }
        
        // Calculate SST
        double resultOverall = 0d;
        
        // For each row
        for(int row=0; row<input[0].length; row+=2){
                
            // For each column
            double resultRow = 0;
            for(int column=0; column<input.length; column++){
                
                double minimum = output[column][row];
                double maximum = output[column][row + 1];
                double value = (minimum + maximum) / 2d;
                resultRow += Math.pow(value - centroid[column], 2d);
            }
            
            // Summarize
            resultOverall += resultRow;
        }
        
        // Return
        return resultOverall;
    }
}
