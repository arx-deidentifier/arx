/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.framework.data;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The class GeneralizationHierarchy.
 * 
 * @author Prasser, Kohlmayer
 */
public class GeneralizationHierarchy {

    /** Level->number of distinct values. */
    protected final int[]   distinctValues;

    /** Input->level->output. */
    protected final int[][] map;

    /** Name. */
    protected final String  name;

    /**
     * Can be used to create a copy of the generalization hierarchy
     * 
     * @param name
     * @param map
     * @param distinctValues
     */
    protected GeneralizationHierarchy(final String name,
                                      final int[][] map,
                                      final int[] distinctValues) {
        this.name = name;
        this.map = map;
        this.distinctValues = distinctValues;
    }

    /**
     * Creates a new generalization hierarchy
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
        if (hierarchy == null || hierarchy.length == 0) { throw new RuntimeException("Empty generalization hierarchy for attribute '" +
                                                                                     name +
                                                                                     "'"); }

        // Init
        this.name = name;
        final int height = hierarchy[0].length;

        // Determine number of unique input values
        final int uniqueIn = dictionary.getNumUniqueUnfinalizedValues(dimension);

        // Build hierarchy
        map = new int[dictionary.size(dimension)][height];
        for (int i = 0; i < hierarchy.length; i++) {
            final String[] input = hierarchy[i];
            final Integer key = dictionary.probe(dimension, input[0]);
            if (key != null) {
                for (int j = 0; j < input.length; j++) {
                    final String value = input[j];
                    final int incode = dictionary.register(dimension, value);
                    map[key][j] = incode;
                }
            }
        }

        // Count distinct values on each level
        distinctValues = new int[height];
        final HashSet<Integer> vals = new HashSet<Integer>();

        // for each column
        for (int i = 0; i < map[0].length; i++) {
            for (int k = 0; k < map.length; k++) {
                vals.add(map[k][i]);
            }
            distinctValues[i] = vals.size();
            vals.clear();
        }

        // Sanity check
        if (distinctValues[0] < uniqueIn) { throw new IllegalArgumentException("Not all data elements are contained in the hierarch for attribute '" +
                                                                               name +
                                                                               "'!"); }
    }

    /**
     * Returns the array
     * 
     * @return
     */
    public int[][] getArray() {
        return map;
    }

    /**
     * Returns the number of distinct values
     * 
     * @return
     */
    public int[] getDistinctValues() {
        return distinctValues;
    }

    /**
     * Returns the distinct values
     * 
     * @param level
     * @return
     */
    public int[] getDistinctValues(final int level) {

        final HashSet<Integer> vals = new HashSet<Integer>();
        for (int k = 0; k < map.length; k++) {
            vals.add(map[k][level]);
        }
        final int[] result = new int[vals.size()];
        int index = 0;
        for (final int j : vals) {
            result[index++] = j;
        }
        return result;
    }

    /**
     * Returns the height of the hierarchy
     * 
     * @return
     */
    public int getHeight() {
        return map[0].length;
    }

    /**
     * Returns the name
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if the hierarchy is monotonic
     * 
     * @return
     */
    public boolean isMonotonic() {
        // level value -> level+1 value
        final HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        // Input->level->output.
        for (int level = 0; level < (map[0].length - 1); level++) {
            hashMap.clear();
            for (int i = 0; i < map.length; i++) {
                final int outputCurrentLevel = map[i][level];
                final int outputNextLevel = map[i][level + 1];
                if (hashMap.containsKey(outputCurrentLevel)) {
                    final int compare = hashMap.get(outputCurrentLevel);
                    if (compare != outputNextLevel) { return false; }
                } else {
                    hashMap.put(outputCurrentLevel, outputNextLevel);
                }
            }
        }
        return true;
    }

}
