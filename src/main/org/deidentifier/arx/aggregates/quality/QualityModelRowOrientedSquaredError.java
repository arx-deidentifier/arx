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
 * Implementation of the Sum of Squared Errors introduced in the supplementary material to:<br>
 * D. Sanchez, S. Martinez, and J. Domingo-Ferrer. Comment on unique in the shopping
 * mall: On the reidentifiability of credit card metadata. Science, 351(6279):1274-1274, 2016.
 * 
 * @author Fabian Prasser
 */
public class QualityModelRowOrientedSquaredError extends QualityModel<QualityMeasureRowOriented> {

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
    public QualityModelRowOrientedSquaredError(WrappedBoolean interrupt,
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
    public QualityMeasureRowOriented evaluate() {
 
        try {
                
            // Prepare
            int[] indices = getIndices();
            List<double[]> columns1 = new ArrayList<>();
            List<double[]> columns2 = new ArrayList<>();
            List<Double> stdDevs = new ArrayList<>();
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
                        double stdDev = getStandardDeviation(columnsAsNumbers[0]);
                        columns1.add(columnsAsNumbers[0]);
                        columns2.add(columnsAsNumbers[1]);
                        stdDevs.add(stdDev);
                    }
                    
                } catch (Exception e) {
                    // Fail silently
                }

                // Progress
                setStepPerformed();
            }
            
            // Check
            if (columns1.isEmpty() || columns2.isEmpty() || stdDevs.isEmpty()) {

                // Progress
                setStepsDone();
                
                // Return
                return new QualityMeasureRowOriented();
            }
            
            // Real distance
            double realDistance = getEuclideanDistance(columns1.toArray(new double[columns1.size()][]),
                                                       columns2.toArray(new double[columns2.size()][]),
                                                       stdDevs.toArray(new Double[stdDevs.size()]));

            // Progress
            setStepPerformed();
            
            // Maximal distance
            double maxDistance = getMaximumEuclideanDistance(columns1.toArray(new double[columns1.size()][]),
                                                             columns2.toArray(new double[columns2.size()][]),
                                                             stdDevs.toArray(new Double[stdDevs.size()]));
            
            // Normalize
            realDistance /= (double)columns1.size();
            realDistance /= (double)getOutput().getNumRows();
            maxDistance /= (double)columns1.size();
            maxDistance /= (double)getOutput().getNumRows();

            // Progress
            setStepsDone();
            
            // Return
            return new QualityMeasureRowOriented(0d, realDistance, maxDistance);
            
        } catch (Exception e) {

            // Progress
            setStepsDone();
            
            // Silently drop exceptions
            return new QualityMeasureRowOriented();
        }
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
     * Returns the maximal sum of the euclidean distance between all records
     * 
     * @param input
     * @param output
     * @param inputStdDev
     * @return
     */
    private double getMaximumEuclideanDistance(double[][] input, double[][] output, Double[] inputStdDev) {
        
        // Calculate minimum and maximum
        double[] minimum = new double[input.length];
        double[] maximum = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            double[] iminmax = getMinMax(input[i]);
            double[] ominmax = getMinMax(output[i]);
            minimum[i] = Math.min(iminmax[0], ominmax[0]);
            maximum[i] = Math.max(iminmax[1], ominmax[1]);
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
