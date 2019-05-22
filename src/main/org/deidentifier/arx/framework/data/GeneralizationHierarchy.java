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

package org.deidentifier.arx.framework.data;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * The class GeneralizationHierarchy.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class GeneralizationHierarchy {

    /** Level->number of distinct values. */
    protected final int[]   distinctValues;

    /** Input->level->output. */
    protected final int[][] map;

    /** Name. */
    protected final String  attribute;

    /**
     * Creates a new generalization hierarchy.
     *
     * @param name
     * @param hierarchy
     * @param dimension
     * @param dictionary
     */
    public GeneralizationHierarchy(final String name,
                                   final String[][] hierarchy,
                                   final int dimension,
                                   final Dictionary dictionary) {

        // Check
        if (hierarchy == null || hierarchy.length == 0) { 
            throw new RuntimeException("Empty generalization hierarchy for attribute '" + name + "'");
        }

        // Init
        this.attribute = name;
        final int height = hierarchy[0].length;
        final int uniqueIn = dictionary.getUnfinalizedValues(dimension).size();

        // Build hierarchy
        this.map = new int[uniqueIn][];
        for (int i = 0; i < hierarchy.length; i++) {
            final String[] input = hierarchy[i];
            if (input == null || input.length == 0) {
                throw new IllegalArgumentException("Attribute '" + name + "': contains empty rule");
            }
            if (input.length != height) {
                throw new IllegalArgumentException("Attribute '" + name + "': a height of " + height + " has been detected for the hierarchy, but rule for " + input[0] + " has " + input[0].length() +" entries");
            }
            final Integer key = dictionary.probe(dimension, input[0]);
            if (key != null && key < uniqueIn) {
                map[key] = new int[height];
                for (int column = 0; column < height; column++) {
                    final String value = input[column];
                    final int incode = dictionary.register(dimension, value);
                    map[key][column] = incode;
                }
            }
        }
        
        // Check for missing rules
        for (int valueID = 0; valueID < map.length; valueID++) {
            
            // Check if missing
            if (map[valueID] == null) {
                
                // Find missing value
                String value = "unknown-value";
                ObjectIntOpenHashMap<String> dictmap = dictionary.getUnfinalizedValues(dimension);
                for (int index = 0; index < dictmap.allocated.length; index++) {
                    if (dictmap.allocated[index] && dictmap.values[index] == valueID) {
                        // Work around some weird casting issues, probably caused by HPPC
                        value = String.valueOf(((Object[]) dictmap.keys)[index]);
                        break;

                    }
                }

                // Throw exception
                throw new IllegalArgumentException("Attribute '" + name + "': hierarchy does not contain a transformation rule for value '" + value + "'");
            }
        }

        // Count distinct values on each level
        this.distinctValues = new int[height];

        // for each column
        final IntOpenHashSet vals = new IntOpenHashSet();
        for (int column = 0; column < map[0].length; column++) {
            for (int row = 0; row < map.length; row++) {
                vals.add(map[row][column]);
            }
            this.distinctValues[column] = vals.size();
            vals.clear();
        }
    }

    /**
     * Throws an exception, if the hierarchy is not monotonic.
     * 
     * @param manager
     */
    public void checkMonotonicity(DataManager manager) {
        
        // Obtain dictionary
        String[] dictionary = null;
        String[] header = manager.getDataGeneralized().getHeader();
        for (int i=0; i<header.length; i++) {
            if (header[i].equals(attribute)) {
                dictionary = manager.getDataGeneralized().getDictionary().getMapping()[i];
            }
        }
        
        // Check
        if (dictionary==null) {
            throw new IllegalStateException("Cannot obtain dictionary for attribute ("+attribute+")");
        }
        
        // Level value -> level+1 value
        final IntIntOpenHashMap hMap = new IntIntOpenHashMap();
        
        // Input->level->output.
        for (int level = 0; level < (map[0].length - 1); level++) {
            hMap.clear();
            for (int i = 0; i < map.length; i++) {
                final int outputCurrentLevel = map[i][level];
                final int outputNextLevel = map[i][level + 1];
                if (hMap.containsKey(outputCurrentLevel)) {
                    final int compare = hMap.get(outputCurrentLevel);
                    if (compare != outputNextLevel) { 
                        String in = dictionary[outputCurrentLevel];
                        String out1 = dictionary[compare];
                        String out2 = dictionary[outputNextLevel];
                        throw new IllegalArgumentException("The transformation rule for the attribute '" + attribute + "' is not a hierarchy. ("+in+") can either be transformed to ("+out1+") or to ("+out2+")");
                    }
                } else {
                    hMap.put(outputCurrentLevel, outputNextLevel);
                }
            }
        }
    }

    /**
     * Returns the array.
     *
     * @return
     */
    public int[][] getArray() {
        return map;
    }

    /**
     * Returns the number of distinct values.
     *
     * @return
     */
    public int[] getDistinctValues() {
        return distinctValues;
    }

    /**
     * Returns the distinct values.
     *
     * @param level
     * @return
     */
    public int[] getDistinctValues(final int level) {

        final IntOpenHashSet vals = new IntOpenHashSet();
        for (int k = 0; k < map.length; k++) {
            vals.add(map[k][level]);
        }

        final int[] result = new int[vals.size()];
        final int[] keys = vals.keys;
        final boolean[] allocated = vals.allocated;
        int index = 0;
        for (int i = 0; i < allocated.length; i++) {
            if (allocated[i]) {
                result[index++] = keys[i];
            }
        }
        return result;
    }

    /**
     * Returns the height of the hierarchy.
     *
     * @return
     */
    public int getHeight() {
        return map[0].length;
    }

    /**
     * Returns the number of levels
     */
    public int getLevels() {
        if (map == null || map[0] == null) {
            return 0;
        } else {
            return map[0].length;
        }
    }

    /**
     * Returns the name.
     *
     * @return
     */
    public String getName() {
        return attribute;
    }
}
