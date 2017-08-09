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
import java.util.Arrays;
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
 * A. De Waal and L. Willenborg: "Information loss through global recoding and local suppression," 
 * Netherlands Off Stat, vol. 14, pp. 17–20, 1999.
 * 
 * @author Fabian Prasser
 */
class UtilityModelColumnOrientedNonUniformEntropy extends UtilityModelColumnOriented {

    /** Log */
    private static final double                      LOG2 = Math.log(2);
    /** Level map: column -> value -> level */
    private final Map<Integer, Map<String, Integer>> levelMap;
    /** Generalization map: column -> level -> value -> value */
    private final Map<Integer, Map<Integer, Map<String, String>>>  generalizationMap;
    /** Input */
    private final String[][]                         input;
    /** Header */
    private final String[]                           header;


    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     */
    UtilityModelColumnOrientedNonUniformEntropy(WrappedBoolean interrupt, DataHandleInternal input) {
        super(interrupt, input);
    
        // Fix hierarchy (if suppressed character is not contained in generalization hierarchy)
        final String SUPPRESSED = "*";
        
        for(String attr : hierarchies.keySet()) {
        	
        	String[][] hierarchy = hierarchies.get(attr);
        	
        	Set<String> values = new HashSet<String>();
    		for (int i = 0; i < hierarchy.length; i++) {
    			String[] levels = hierarchy[i];
    			values.add(levels[levels.length - 1]);
    		}
        	
    		if (values.size() > 1 || !values.iterator().next().equals(SUPPRESSED)) {
    			for(int i = 0; i < hierarchy.length; i++) {
    				hierarchy[i] = Arrays.copyOf(hierarchy[i], hierarchy[i].length + 1);
    				hierarchy[i][hierarchy[i].length - 1] = SUPPRESSED;
            	}
    		}
        	
    		hierarchies.put(attr, hierarchy);
    	}
        
        // Do rest
        this.input = input;
        this.header = header;
        this.levelMap = new HashMap<>();
        this.generalizationMap = new HashMap<>(); 
        for (int i=0; i<header.length; i++) {
            levelMap.put(i, getLevelMap(hierarchies.get(header[i])));
            Map<Integer, Map<String, String>> generalizations = new HashMap<>();
            for (int level = 0; level < hierarchies.get(header[i])[0].length; level++) {
                generalizations.put(level, getGeneralizationMap(hierarchies.get(header[i]), level));
            }
            generalizationMap.put(i, generalizations);
        }
    }

    /**
     * Builds a generalization map: value -> generalized
     * 
     * @param hierarchy
     * @param level 
     * @return
     */
    private Map<String, String> getGeneralizationMap(String[][] hierarchy, int level) {

        Map<String, String> map = new HashMap<String, String>();
        for (int row = 0; row < hierarchy.length; row++) {
            map.put(hierarchy[row][0], hierarchy[row][level]);
        }
        return map;
    }

    /**
     * Frequencies in input for all rows with transformation level >= level
     * @param input
     * @param transformations
     * @param col
     * @param level
     * @return
     */
    private Map<String, Double> getInputFrequencies(String[][] input, int[][] transformations, int col, int level) {
        Map<String, Double> result = new HashMap<String, Double>();
        for (int row = 0; row < input.length; row++) {
            if (transformations[row][col] >= level) {
                String value = input[row][col];
                Double count = result.get(value);
                result.put(value, count != null ? count + 1d : 1d);
            }
        }
        return result;
    }

    /**
     * Builds a level map: value -> level
     * @param hierarchy
     * @return
     */
    private Map<String, Integer> getLevelMap(String[][] hierarchy) {
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int col = 0; col < hierarchy[0].length; col++) {
            for (int row = 0; row < hierarchy.length; row++) {
                String value = hierarchy[row][col];
                if (!map.containsKey(value)) {
                    map.put(value, col);
                }
            }
        }
        return map;
    }

    /**
     * Frequencies of values on the target level in output for all rows with transformation level >= level
     * @param input
     * @param transformations
     * @param col
     * @param level
     * @param target
     * @return
     */
    private Map<String, Double> getOutputFrequencies(String[][] input, int[][] transformations, int col, int level, int target) {
        Map<String, Double> result = new HashMap<String, Double>();
        for (int row = 0; row < input.length; row++) {
            if (transformations[row][col] >= level) {
                String value = generalizationMap.get(col).get(target).get(input[row][col]);
                Double count = result.get(value);
                result.put(value, count != null ? count + 1d : 1d);
            }
        }
        return result;
    }

    /**
     * Log base-2
     * @param d
     * @return
     */
    private double log2(double d) {
        return Math.log(d) / LOG2;
    }

    @Override
    double[] evaluate(final DataHandleInternal output) {
        
        // Collect transformations
        final int[][] transformations = new int[output.length][];
        for (int row = 0; row < output.length; row++) {
            int[] transformation = new int[output[row].length];
            for (int column = 0; column < transformation.length; column++) {
                transformation[column] = levelMap.get(column).get(output[row][column]);
            }
            transformations[row] = transformation;
        }
        
        // For each column
        double[] result = new double[input[0].length];
        for (int col = 0; col < header.length; col++) {
            
            // Collect all generalization levels
            Set<Integer> _levels = new HashSet<Integer>();
            for (int row = 0; row < input.length; row++) {
                _levels.add(transformations[row][col]);
            }
            List<Integer> levels = new ArrayList<Integer>();
            levels.addAll(_levels);
            Collections.sort(levels);
            
            // For each generalization level
            for (int numlevel = 0; numlevel < levels.size(); numlevel++) {
                
                // Obtain level
                int level = levels.get(numlevel);
                
                // Frequencies in input or numlevel - 1 for all rows with transformation level >= level
                Map<String, Double> inputFrequencies;
                if (numlevel == 0) {
                    inputFrequencies = getInputFrequencies(input, transformations, col, level);
                } else {
                    inputFrequencies = getOutputFrequencies(input,
                                                            transformations,
                                                            col,
                                                            level,
                                                            levels.get(numlevel - 1));
                }

                // Frequencies of values on the given level in output for all rows with transformation level >= level
                Map<String, Double> outputFrequencies = getOutputFrequencies(input, transformations, col, level, level);
                
                // Sum up loss of values transformation level >= level
                for (int row = 0; row < output.length; row++) {
                    if (transformations[row][col] >= level) {
                        
                        // In and out
                        String inputValue;
                        if (numlevel == 0) {
                            inputValue = input[row][col];
                        } else {
                            inputValue = generalizationMap.get(col).get(levels.get(numlevel - 1)).get(input[row][col]);
                        }
                        String outputValue = generalizationMap.get(col).get(level).get(input[row][col]);
                        
                        // Sum up
                        result[col] += log2(inputFrequencies.get(inputValue) /
                                            outputFrequencies.get(outputValue));
                    }
                }
            }
        }
        
        // Invert sign
        for (int i=0; i<result.length; i++) {
            result[i] *= -1;
        }
        return result;
    }
}
