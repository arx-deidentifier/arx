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

package org.deidentifier.arx.metric.v2;

import java.io.Serializable;

import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * A class that supports associating input with output
 * @author Florian Kohlmayer, Fabian Prasser
 *
 */
public class TupleMatcher implements Serializable {
    
    /** SVUID*/
    private static final long serialVersionUID = -5081573765755187296L;
    
    /** Data */
    private final int[][][]   hierarchies;
    /** Data */
    private final int[]       tuple;

    /**
     * Creates a new instance
     * @param input
     */
    TupleMatcher(GeneralizationHierarchy[] hierarchies) {
        
        // Store hierarchies
        this.hierarchies = new int[hierarchies.length][][];
        for (int dimension = 0; dimension < this.hierarchies.length; dimension++) {
            this.hierarchies[dimension] = hierarchies[dimension].getArray();
        }
        
        // Create tuple
        this.tuple = new int[this.hierarchies.length];
    }
    
    /**
     * Returns the entry for the given input tuple
     * @param row
     * @param generalization
     * @param groupify
     * @return
     */
    HashGroupifyEntry getEntry(int row, int[] generalization, HashGroupify groupify) {
        
        // Transform the tuple
        DataMatrix data = groupify.getInputData();
        data.iterator(row);
        for (int dimension = 0; dimension < tuple.length; dimension++) {
            tuple[dimension] = hierarchies[dimension][data.iterator_next()][generalization[dimension]];
        }
        
        // Return
        return groupify.getEntry(tuple);
    }
}
