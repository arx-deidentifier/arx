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

import java.io.Serializable;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * A dictionary mapping integers to strings for different dimensions.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Dictionary implements Serializable {

    /** SVUID */
    private static final long                        serialVersionUID = 6448285732641604559L;

    /** The resulting array mapping dimension->integer->string. */
    private final String[][]                         mapping;

    /** Map used when building the dictionary. */
    private transient ObjectIntOpenHashMap<String>[] maps;

    /**
     * Instantiates a new dictionary by extracting a projection of the given dictionary
     * 
     * @param dimensions
     */
    @SuppressWarnings("unchecked")
    public Dictionary(Dictionary input, int[] columns) {
        maps = new ObjectIntOpenHashMap[columns.length];
        mapping = new String[columns.length][];
        for (int i = 0; i < columns.length; i++) {
            maps[i] = input.maps == null ? null : input.maps[columns[i]].clone();
            mapping[i] = input.mapping[columns[i]].clone();
        }
    }
    
    /**
     * Instantiates a new dictionary.
     * 
     * @param dimensions
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
     * Definalizes the dictionary
     */
    @SuppressWarnings("unchecked")
    public void definalizeAll() {
        
        // Re-instantiate maps
        maps = new ObjectIntOpenHashMap[mapping.length];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new ObjectIntOpenHashMap<String>();
        }
        
        // Add from mapping
        for (int i = 0; i < mapping.length; i++) {
            if (mapping[i] != null) {
                for (int j = 0; j < mapping[i].length; j++) {
                    maps[i].put(mapping[i][j], j);
                }
            }
        }
        
        // Remove mapping
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = null;
        }
    }

    /**
     * Finalizes all dimensions.
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
