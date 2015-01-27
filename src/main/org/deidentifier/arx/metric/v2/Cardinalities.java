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

import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * This class represents cardinalities.
 * TODO: This class can potentially be merged with DomainShare
 * TODO: It is not yet sure, which of both mechanisms performs better
 * 
 * @author Fabian Prasser
 */
public class Cardinalities implements Serializable {
    
    /** SVUID. */
    private static final long serialVersionUID = 6164578830669365810L;
    
    /** Cardinalities: Column -> Id -> Level -> Count. */
    private final int[][][] cardinalities;
    
    /**
     * Creates a new instance for the given data set.
     *
     * @param data
     * @param subset
     * @param hierarchies
     */
    public Cardinalities(Data data, RowSet subset, GeneralizationHierarchy[] hierarchies){

        int[][] array = data.getArray();
        Dictionary dictionary = data.getDictionary();
        
        // Initialize counts
        cardinalities = new int[array[0].length][][];
        for (int i = 0; i < cardinalities.length; i++) {
            cardinalities[i] = new int[dictionary.getMapping()[i].length][hierarchies[i].getArray()[0].length];
        }

        // Compute counts
        for (int i = 0; i < array.length; i++) { 
            if (subset == null || subset.contains(i)) {
                final int[] row = array[i];
                for (int column = 0; column < row.length; column++) {
                    cardinalities[column][row[column]][0]++;
                }
            }
        }

        // Create counts for other levels
        for (int column = 0; column < hierarchies.length; column++) {
            final int[][] hierarchy = hierarchies[column].getArray();
            for (int in = 0; in < hierarchy.length; in++) {
                final int cardinality = cardinalities[column][in][0];
                for (int level = 1; level < hierarchy[in].length; level++) {
                    final int out = hierarchy[in][level];
                    cardinalities[column][out][level] += cardinality;
                }
            }
        }
    }
    
    /**
     * For backwards compatibility, derives the cardinalities from the given array.
     *
     * @param cardinalities
     */
    public Cardinalities(int[][][] cardinalities) {
        this.cardinalities = cardinalities;
    }

    /**
     * Returns the cardinalities of the given value.
     *
     * @return
     */
    public int[][][] getCardinalities(){
        return cardinalities;
    }
}
