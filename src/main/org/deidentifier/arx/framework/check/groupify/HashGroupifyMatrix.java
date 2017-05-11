/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.framework.check.groupify;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataMatrix;

/**
 * A matrix containing only sample uniques to be used by key-based models
 * @author Fabian Prasser
 */
public class HashGroupifyMatrix {

    /** Suppressed records */
    private int                       numSuppressedRecords = 0;
    /** Matrix */
    private final int[][]             matrix;
    /** Entry */
    private final HashGroupifyEntry[] entries;

    /**
     * Creates a new instance
     * 
     * @param metric, null if ordering should not be applied
     * @param entry
     */
    HashGroupifyMatrix(HashGroupifyEntry entry) {
        
        List<HashGroupifyEntry> entries = new ArrayList<HashGroupifyEntry>();
        
        // Initialize
        while(entry != null) {
            switch (entry.count) {
            case 0: /* Nothing to do*/ break;
            case 1: /* Add once*/
                entries.add(entry);
                break;
            default: /* Add twice*/
                entries.add(entry);
                entries.add(entry);
                break; 
            }
            this.numSuppressedRecords += !entry.isNotOutlier ? entry.count : 0;
            entry = entry.nextOrdered;
        }
        
        // Store
        this.entries = entries.toArray(new HashGroupifyEntry[entries.size()]);
        this.matrix = new int[entries.size()][];
        for (int i=0; i<entries.size(); i++) {
            this.matrix[i] = entries.get(i).key;
        }
    }
    
    /**
     * Returns the data matrix
     * @return
     */
    public DataMatrix getMatrix() {
        return new DataMatrix(matrix);
    }
    
    /**
     * Returns the number of suppressed records
     * @return
     */
    public int getNumSuppressedRecords() {
        return this.numSuppressedRecords;
    }
    
    /**
     * Suppresses the entry at the given index
     * @param index
     */
    public void suppress(int index) {
        HashGroupifyEntry entry = entries[index];
        this.numSuppressedRecords += entry.isNotOutlier ? entry.count : 0; 
        entry.isNotOutlier = false;
    }
}
