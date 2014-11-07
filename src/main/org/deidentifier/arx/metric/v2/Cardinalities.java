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
    
    /** SVUID*/
    private static final long serialVersionUID = 6164578830669365810L;
    
    /** Cardinalities: Column -> Id -> Level -> Count */
    private final int[][][] cardinalities;
    
    /**
     * Creates a new instance for the given data set
     * @param hierarchy
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
     * For backwards compatibility, derives the cardinalities from the given array
     * @param cardinalities
     */
    public Cardinalities(int[][][] cardinalities) {
        this.cardinalities = cardinalities;
    }

    /**
     * Returns the cardinalities of the given value
     * @param value
     * @param level
     * @return
     */
    public int[][][] getCardinalities(){
        return cardinalities;
    }
}
