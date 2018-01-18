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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

/**
 * Implementation of the Non-Uniform Entropy measure that can handle local recoding. Enhanced
 * model supporting local recoding as presented in: <br>
 * <br>
 * Fabian Prasser, Raffael Bild, Klaus A. Kuhn.
 * "A Generic Method for Assessing the Quality of De-Identified Health Data."
 * Proceedings of MIE 2016, IOS Press, August 2016.<br>
 * <br>
 * Original model, see:<br>
 * A. De Waal and L. Willenborg: "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17-20, 1999.
 * 
 * @author Fabian Prasser
 */
public class QualityModelColumnOrientedNonUniformEntropy extends QualityModel<QualityMeasureColumnOriented> {

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
    public QualityModelColumnOrientedNonUniformEntropy(WrappedBoolean interrupt,
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
        DataHandle output = getOutput();
        String[][][] hierarchies = getHierarchies();
        double[] result = new double[indices.length];
        double[] min = new double[indices.length];
        double[] max = new double[indices.length];

        // Progress
        setSteps(result.length * 3);
        
        // For each column
        for (int i = 0; i < result.length; i++) {

            // Map
            int column = indices[i];

            try {
                
                // Prepare
                Map<String, String>[] generalizationFunctions = getGeneralizationFunctions(hierarchies, i);
                Map<String, Integer> inverseGeneralizationFunction = getInverseGeneralizationFunction(hierarchies, i);
                
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

                // Progress
                setStepPerformed();
                
                // For each generalization level
                for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
                    
                    // Obtain level
                    int currentLevel = levels.get(levelIndex);
                    int previousLevel = levelIndex > 0 ? levels.get(levelIndex - 1) : currentLevel;
                    
                    // Frequencies in input or levelIndex - 1 for all rows with transformation level >= level
                    Map<String, Double> inputFrequencies = levelIndex == 0 ? getInputFrequencies(transformations, column, currentLevel)
                                                                           : getOutputFrequencies(transformations, generalizationFunctions, column, currentLevel, previousLevel);

                    // Frequencies of values on the given level in output for all rows with transformation level >= level
                    Map<String, Double> outputFrequencies = getOutputFrequencies(transformations, generalizationFunctions, column, currentLevel, currentLevel);
                    
                    // Sum up loss of values transformation level >= level
                    DataHandle input = getInput();
                    for (int row = 0; row < input.getNumRows(); row++) {

                        // Input and output value for this cell
                        String value = input.getValue(row, column);
                        String inputValue = levelIndex == 0 ? value : generalizationFunctions[previousLevel].get(value);
                        String outputValue = generalizationFunctions[currentLevel].get(value);
                        
                        // Calculate result
                        if (transformations[row] >= currentLevel) {
                            result[i] += log2(inputFrequencies.get(inputValue) / outputFrequencies.get(outputValue));
                        }
                        
                        // Check
                        checkInterrupt();
                    }
                }

                // Progress
                setStepPerformed();

                // Calculate maximum
                DataHandle input = getInput();
                Map<String, Double> inputFrequencies = getInputFrequencies(transformations, column, 0);
                for (int row = 0; row < input.getNumRows(); row++) {
                    max[i] += log2(inputFrequencies.get(input.getValue(row, column)) / (double)input.getNumRows());

                    // Check
                    checkInterrupt();
                }

                // Progress
                setStepPerformed();

                // Invert sign
                result[i] *= -1;
                max[i] *= -1;
                
                // Explicitly define minimum
                min[i] = 0;
                
            } catch (Exception e) {
                // Silently catch exceptions
                result[i] = Double.NaN;
                min[i] = Double.NaN;
                max[i] = Double.NaN;
            }

            // Check
            checkInterrupt();
        }

        // Progress
        setStepsDone();
        
        // Return
        return new QualityMeasureColumnOriented(output, indices, min, result, max);
    }

    /**
     * Builds a generalization function mapping input values to the given level of the hierarchy
     * 
     * @param hierarchies
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, String>[] getGeneralizationFunctions(String[][][] hierarchies, int index) {
        
        // Prepare
        Map<String, String>[] result = new HashMap[hierarchies[index][0].length];

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
     * Returns the frequencies of values in input data for all rows with transformation level >= level
     * @param transformations
     * @param column
     * @param level
     * @return
     */
    private Map<String, Double> getInputFrequencies( int[] transformations, int column, int level) {
        DataHandle input = getInput();
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
     * Returns the inverse generalization function
     * @param hierarchies
     * @param index
     * @return
     */
    private Map<String, Integer> getInverseGeneralizationFunction(String[][][] hierarchies, int index) {

        // Prepare
        Map<String, Integer> result = new HashMap<>();

        // Collect
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
        
        // Handle suppressed value
        if (!result.containsKey(getSuppressionString())) {
            result.put(getSuppressionString(), hierarchies[index][0].length-1);
        }
        
        // Return
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
        DataHandle input = getInput();
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
