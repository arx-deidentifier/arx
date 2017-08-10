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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implementation of the Sum of Squared Errors introduced in:<br>
 * Soria-Comas, Jordi, et al.:
 * "t-closeness through microaggregation: Strict privacy with enhanced utility preservation."
 * IEEE Transactions on Knowledge and Data Engineering 27.11 (2015): 3098-3110.
 * 
 * @author Fabian Prasser
 */
public class UtilityModelRowOrientedSSE extends UtilityModel<UtilityMeasureRowOriented> {

    /** Header */
    private final int[] indices;
    
    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    public UtilityModelRowOrientedSSE(WrappedBoolean interrupt,
                                      DataHandleInternal input,
                                      UtilityConfiguration config) {
        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);

    }
    
    @Override
    public UtilityMeasureRowOriented evaluate(DataHandleInternal output) {
       
        try {
                
            // Prepare
            List<double[]> columns1 = new ArrayList<>();
            List<double[]> columns2 = new ArrayList<>();
            List<Double> stdDevs = new ArrayList<>();
            String[][][] hierarchies = getHelper().getHierarchies(getInput(), indices);
            
            // Collect
            for (int index = 0; index < indices.length; index++) {
                try {
                    int column = indices[index];
                    double[][] columnsAsNumbers = getHelper().getColumnsAsNumbers(getInput(), output, 
                                                                                  hierarchies[index], column);
                    if (columnsAsNumbers != null) {
                        double stdDev = getHelper().getStandardDeviation(columnsAsNumbers[0]);
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
                return new UtilityMeasureRowOriented();
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
            realDistance /= (double)output.getNumRows();
            maxDistance /= (double)columns1.size();
            maxDistance /= (double)output.getNumRows();
            
            // Return
            return new UtilityMeasureRowOriented(0d, realDistance, maxDistance);
            
        } catch (Exception e) {
            return new UtilityMeasureRowOriented();
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
            double[] minmax = getHelper().getMinMax(input[i]);
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
}
