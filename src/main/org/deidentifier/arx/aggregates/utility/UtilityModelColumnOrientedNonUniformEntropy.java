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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implementation of the Non-Uniform Entropy measure that can handle local recoding. See:<br>
 * A. De Waal and L. Willenborg: "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17-20, 1999.
 * 
 * @author Fabian Prasser
 */
public class UtilityModelColumnOrientedNonUniformEntropy extends UtilityModel<UtilityMeasureColumnOriented> {

    /** Header */
    private final int[]                   indices;
    /** Hierarchies */
    private final String[][][]            hierarchies;

    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    public UtilityModelColumnOrientedNonUniformEntropy(WrappedBoolean interrupt,
                                                       DataHandleInternal input,
                                                       UtilityConfiguration config) {

        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);
        this.hierarchies = getHelper().getHierarchies(input, indices);
    }

    @Override
    public UtilityMeasureColumnOriented evaluate(final DataHandleInternal output) {
        

        // Prepare
        double[] result = new double[indices.length];
        double[] min = new double[indices.length];
        double[] max = new double[indices.length];
        
        // For each column
        for (int i = 0; i < result.length; i++) {

            // Map
            int column = indices[i];

            try {
                
                // Prepare
                Map<String, String>[] generalizationFunctions = getHelper().getGeneralizationFunctions(hierarchies, i);
                Map<String, Integer> inverseGeneralizationFunction = getHelper().getInverseGeneralizationFunction(hierarchies, i);
                
                // Determine generalization levels
                final int[] transformations = new int[output.getNumRows()];
                for (int row = 0; row < output.getNumRows(); row++) {
                    transformations[row] = inverseGeneralizationFunction.get(output.getValue(row, column));
                }
                
                // Group and sort all generalization levels
                Set<Integer> _levels = new HashSet<Integer>();
                for (int level : transformations) {
                    _levels.add(level);
                }
                List<Integer> levels = new ArrayList<Integer>();
                levels.addAll(_levels);
                Collections.sort(levels);
                
                // For each generalization level
                for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
                    
                    // Obtain level
                    int currentLevel = levels.get(levelIndex);
                    int previousLevel = levelIndex > 0 ? levels.get(levelIndex - 1) : currentLevel;
                    
                    // Frequencies in input or levelindex - 1 for all rows with transformation level >= level
                    Map<String, Double> inputFrequencies = levelIndex == 0 ? inputFrequencies = getInputFrequencies(transformations, column, currentLevel)
                                                                           : getOutputFrequencies(transformations, generalizationFunctions, column, currentLevel, previousLevel);

                    // Frequencies of values on the given level in output for all rows with transformation level >= level
                    Map<String, Double> outputFrequencies = getOutputFrequencies(transformations, generalizationFunctions, column, currentLevel, currentLevel);
                    
                    // Sum up loss of values transformation level >= level
                    DataHandleInternal input = getInput();
                    for (int row = 0; row < input.getNumRows(); row++) {

                        // Input and output value for this cell
                        String value = input.getValue(row, column);
                        String inputValue = levelIndex == 0 ? value : generalizationFunctions[previousLevel].get(value);
                        String outputValue = generalizationFunctions[levels.get(currentLevel)].get(value);
                        
                        // Calculate result
                        if (transformations[row] >= currentLevel) {
                            result[i] += log2(inputFrequencies.get(inputValue) / outputFrequencies.get(outputValue));
                        }
                        
                        // Calculate maximum
                        max[i] += log2(inputFrequencies.get(inputValue) / (double)input.getNumRows());

                        // Check
                        checkInterrupt();
                    }
                }
                    
                // Invert sign
                result[i] *= -1;
                
                // Explicitly define minimum
                min[i] = 0;
                
            } catch (Exception e) {
                // Silently catch exceptions
                result[i] = Double.NaN;
                min[i] = Double.NaN;
                max[i] = Double.NaN;
                break;
            }

            // Check
            checkInterrupt();
            
        }

        // Return
        return new UtilityMeasureColumnOriented(output, indices, min, result, max);
    }

    /**
     * Returns the frequencies of values in input data for all rows with transformation level >= level
     * @param transformations
     * @param column
     * @param level
     * @return
     */
    private Map<String, Double> getInputFrequencies( int[] transformations, int column, int level) {
        DataHandleInternal input = getInput();
        Map<String, Double> result = new HashMap<String, Double>();
        for (int row = 0; row < input.getNumRows(); row++) {
            if (transformations[row] >= level) {
                String value = input.getValue(row, column);
                Double count = result.get(value);
                result.put(value, count != null ? count + 1d : 1d);
            }

            // Check
            checkInterrupt();
        }
        return result;
    }

    /**
     * Returns the frequencies of values in input data on the target level in output for all rows with transformation level >= level
     * @param transformations
     * @param generalizationFunctions
     * @param column
     * @param level
     * @param target
     * @return
     */
    private Map<String, Double> getOutputFrequencies(int[] transformations,
                                                     Map<String, String>[] generalizationFunctions,
                                                     int column, 
                                                     int level, 
                                                     int target) {
        Map<String, Double> result = new HashMap<String, Double>();
        DataHandleInternal input = getInput();
        for (int row = 0; row < input.getNumRows(); row++) {
            if (transformations[row] >= level) {
                String value = generalizationFunctions[target].get(input.getValue(row, column));
                Double count = result.get(value);
                result.put(value, count != null ? count + 1d : 1d);
            }

            // Check
            checkInterrupt();
        }
        return result;
    }
}
