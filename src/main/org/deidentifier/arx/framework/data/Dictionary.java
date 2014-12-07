/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.data;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * A dictionary mapping integers to strings for different dimensions.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Dictionary {

    /** The resulting array mapping dimension->integer->string. */
    private final String[][]               mapping;

    /** Map used when building the dictionary. */
    private ObjectIntOpenHashMap<String>[] maps;

    /**
     * Instantiates a new dictionary.
     * 
     * @param dimensions
     *            the dimensions
     */
    @SuppressWarnings("unchecked")
    public Dictionary(final int dimensions) {
        maps = new ObjectIntOpenHashMap[dimensions];
        mapping = new String[dimensions][];
        for (int i = 0; i < dimensions; i++) {
            maps[i] = new ObjectIntOpenHashMap<String>();
        }
    }

    /**
     * Finalizes all dimensions. @see finalize()
     */
    public void finalizeAll() {
        for (int i = 0; i < maps.length; i++) {
            mapping[i] = new String[maps[i].size()];
            final Object[] keys = maps[i].keys;
            final int[] values = maps[i].values;
            final boolean[] allocated = maps[i].allocated;
            for (int j = 0; j < allocated.length; j++) {
                if (allocated[j]) {
                    mapping[i][values[j]] = (String) keys[j];
                }
            }

        }
        maps = null;
    }

    /**
     * Returns the mapping array.
     *
     * @return
     */
    public String[][] getMapping() {
        return mapping;
    }

    /**
     * Returns the number of dimensions in the dictionary.
     *
     * @return
     */
    public int getNumDimensions() {
        return mapping.length;
    }

    /**
     * Returns the number of unique values contained before finalizing the
     * dictionary.
     *
     * @param dimension
     * @return
     */
    public int getNumUniqueUnfinalizedValues(final int dimension) {
        return maps[dimension].size();
    }

    /**
     * Returns the registered value if present, null otherwise.
     *
     * @param dimension
     * @param string
     * @return
     */
    public Integer probe(final int dimension, final String string) {
        if (maps[dimension].containsKey(string)) {
            return maps[dimension].lget();
        } else {
            return null;
        }
        // return maps[dimension].get(string);
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

        // Prepare
        ObjectIntOpenHashMap<String> map = maps[dimension];
        int size = map.size();

        // Return or store
        if (map.putIfAbsent(string, size)) {
            return size;
        } else {
            return map.lget();
        }
    }

    /**
     * Merges this dictionary with another dictionary.
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
}
