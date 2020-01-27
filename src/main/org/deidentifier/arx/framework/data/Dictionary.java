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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.deidentifier.arx.DataType;

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

	/** Codes of suppressed values for each dimension */
	private int[]                                    suppressed;

    /**
     * Instantiates a new dictionary by extracting a projection of the given dictionary
     * 
     * @param dimensions
     */
    @SuppressWarnings("unchecked")
    public Dictionary(Dictionary input, int[] columns) {
        maps = new ObjectIntOpenHashMap[columns.length];
        mapping = new String[columns.length][];
        suppressed = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            maps[i] = input.maps == null ? null : input.maps[columns[i]].clone();
            mapping[i] = input.mapping[columns[i]].clone();
            suppressed[i] = input.suppressed[columns[i]];
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
        suppressed = new int[dimensions];
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
            suppressed[i] = -1; // Won't match anything
            final Object[] keys = maps[i].keys;
            final int[] values = maps[i].values;
            final boolean[] allocated = maps[i].allocated;
            for (int j = 0; j < allocated.length; j++) {
                if (allocated[j]) {
                    String key = (String) keys[j];
                    int value = values[j];
                    mapping[i][value] = key;
                    suppressed[i] = key.equals(DataType.ANY_VALUE) ? value : suppressed[i];
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
     * Returns the codes for suppressed values
     * @return
     */
    public int[] getSuppressedCodes() {
        return suppressed;
    }

    /**
     * Returns the map with unfinalized values for the given dimension
     * @param dimension
     * @return
     */
    public ObjectIntOpenHashMap<String> getUnfinalizedValues(final int dimension) {
        return maps[dimension];
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
    
    /**
     * Registers special values
     */
    public void registerSpecialValues() {
        for (int dimension = 0; dimension < maps.length; dimension++) {
            register(dimension, DataType.ANY_VALUE);
            register(dimension, DataType.NULL_VALUE);   
        }
    }

    /**
     * Custom de-serialization for backwards compatibility
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    	
        // Default de-serialization first
    	stream.defaultReadObject();
        
    	// Make backwards compatible between 3.8.0 and prior versions
    	if (this.suppressed == null) {
    		this.suppressed = new int[mapping.length];
    		for (int i = 0; i < mapping.length; i++) {
    			this.suppressed[i] = -1; // Won't match anything
                for (int id = 0; id < mapping[i].length; id++) {
                	String text = mapping[i][id];
                	this.suppressed[i] = text.equals(DataType.ANY_VALUE) ? id : this.suppressed[i];
                }
            }
    	}
    }
}
