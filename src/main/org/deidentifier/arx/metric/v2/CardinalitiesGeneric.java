/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.metric.v2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.Dictionary;

/**
 * This class represents attribute counts.
 * 
 * @author Fabian Prasser
 */
public class CardinalitiesGeneric implements Serializable {

    /** SVUID. */
    private static final long             serialVersionUID = -5545884263748640237L;

    /** Cardinalities: Column -> Id -> Count. */
    private final int[][]                 cardinalities;

    /** Maps */
    private final Map<Integer, Integer>[] maps;

    /**
     * Creates a new instance for the given data set.
     *
     * @param data
     * @param subset
     */
    public CardinalitiesGeneric(Data data, RowSet subset){

        int[][] array = data.getArray();
        Dictionary dictionary = data.getDictionary();
        
        // Initialize counts
        cardinalities = new int[array[0].length][];
        for (int i = 0; i < cardinalities.length; i++) {
            cardinalities[i] = new int[dictionary.getMapping()[i].length];
        }

        // Compute counts
        for (int i = 0; i < array.length; i++) { 
            if (subset == null || subset.contains(i)) {
                final int[] row = array[i];
                for (int column = 0; column < row.length; column++) {
                    cardinalities[column][row[column]]++;
                }
            }
        }
        
        // Null
        this.maps = null;
    }

    /**
     * Creates a new instance for the given data set.
     *
     * @param data
     * @param subset
     */
    @SuppressWarnings("unchecked")
    public CardinalitiesGeneric(HashGroupify groupify){
        
        // Null
        this.cardinalities = null;
        
        // Init
        this.maps = new Map[groupify.getFirstEquivalenceClass().key.length];
        for (int i = 0; i < maps.length; i++) {
            this.maps[i] = new HashMap<Integer, Integer>();
        }

        // Compute
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count > 0) {
                for (int i = 0; i < maps.length; i++) {
                    int key = m.isNotOutlier ? m.key[i] : -1;
                    Integer count = this.maps[i].get(key);
                    this.maps[i].put(key, count == null ? m.count : count + m.count);
                }
            }
            m = m.nextOrdered;
        }
    }
    
    /**
     * Returns the cardinalities of the given value.
     *
     * @return
     */
    public int getCardinality(int column, int value){
        if (cardinalities != null) {
            return cardinalities[column][value];
        } else {
            return this.maps[column].get(value);
        }
    }
}
