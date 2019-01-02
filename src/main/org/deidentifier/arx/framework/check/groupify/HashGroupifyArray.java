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

import org.deidentifier.arx.DataArray;

/**
 * Builds an array containing only sample uniques to be used by key-based models.
 * Implements a method to suppress individual groupify entries.
 * @author Fabian Prasser
 */
public class HashGroupifyArray {

    /** Suppressed records */
    private int                       numSuppressedRecords = 0;
    /** Matrix */
    private final DataArray           array;
    /** Entry */
    private final HashGroupifyEntry[] entries;

    /**
     * Creates a new instance
     * 
     * @param metric, null if ordering should not be applied
     * @param entry
     */
    HashGroupifyArray(HashGroupifyEntry entry) {
        
        List<HashGroupifyEntry> entries = new ArrayList<HashGroupifyEntry>();
        
        // Initialize
        while(entry != null) {
            if (entry.isNotOutlier) {
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
            } else {
                this.numSuppressedRecords += entry.count;
            }
            entry = entry.nextOrdered;
        }
        
        // Store
        this.entries = entries.toArray(new HashGroupifyEntry[entries.size()]);
        this.array =  new DataArray(entries);
    }
    
    /**
     * Returns the data array
     * @return
     */
    public DataArray getArray() {
        return array;
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
     * @return Whether the entry needed to be suppressed (because it wasn't before)
     */
    public boolean suppress(int index) {
        HashGroupifyEntry entry = entries[index];
        this.numSuppressedRecords += entry.isNotOutlier ? entry.count : 0;
        boolean suppressed = entry.isNotOutlier;
        entry.isNotOutlier = false;
        return suppressed;
    }
}
