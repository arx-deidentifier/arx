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

/**
 * A dictionary mapping integers to strings for different dimensions.
 * 
 * @author Prasser, Kohlmayer
 */
public class Dictionary {

    /** The resulting array mapping dimension->integer->string. */
    private final String[][]           mapping;

    /** Map used when building the dictionary. */
    private HashMap<String, Integer>[] maps;

    /**
     * Instantiates a new dictionary.
     * 
     * @param dimensions
     *            the dimensions
     */
    @SuppressWarnings("unchecked")
    public Dictionary(final int dimensions) {
        maps = new HashMap[dimensions];
        mapping = new String[dimensions][];
        for (int i = 0; i < dimensions; i++) {
            maps[i] = new HashMap<String, Integer>(10000);
        }
    }

    /**
     * Finalizes all dimensions. @see finalize()
     */
    public void finalizeAll() {
        for (int i = 0; i < maps.length; i++) {
            mapping[i] = new String[maps[i].size()];
            for (final String val : maps[i].keySet()) {
                mapping[i][maps[i].get(val)] = val;
            }
        }
        maps = null;
    }

    /**
     * Returns the mapping array
     * 
     * @return
     */
    public String[][] getMapping() {
        return mapping;
    }

    /**
     * Returns the number of dimensions in the dictionary
     * 
     * @return
     */
    public int getNumDimensions() {
        return mapping.length;
    }

    /**
     * Returns the number of unique values contained before finalizing the
     * dictionary
     * 
     * @return
     */
    public int getNumUniqueUnfinalizedValues(final int dimension) {
        return maps[dimension].size();
    }

    /**
     * Returns the registered value if present, null otherwise
     * 
     * @param dimension
     * @param string
     * @return
     */
    public Integer probe(final int dimension, final String string) {
        return maps[dimension].get(string);
    }

    /**
     * Registers a new string at the dictionary.
     * 
     * @param dimension
     *            the dimension
     * @param string
     *            the string
     * @return the int
     */
    public int register(final int dimension, final String string) {
        final Integer current = maps[dimension].get(string);
        if (current != null) { return current; }
        final int idx = maps[dimension].size();
        maps[dimension].put(string, idx);
        return idx;
    }

    /**
     * Merges this dictionary with another dictionary
     * 
     * @param targetDimension
     * @param dictionary
     * @param sourceDimension
     */
    public void registerAll(final int targetDimension,
                            final Dictionary dictionary,
                            final int sourceDimension) {
        final String[] vals = dictionary.mapping[sourceDimension];
        for (int id = 0; id < vals.length; id++) {
            maps[targetDimension].put(vals[id], id);
        }
    }

    /**
     * Returns the size of the according dictionary
     * 
     * @param dimension
     * @return
     */
    public int size(final int dimension) {
        return maps[dimension].size();
    }
}
