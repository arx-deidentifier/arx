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

package org.deidentifier.arx.framework.check.groupify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.risk.RiskModelEquivalenceClasses;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * A distribution of equivalence classes
 * @author Fabian Prasser
 */
public class HashGroupifyDistribution {

    /** The backing map */
    private IntIntOpenHashMap   distribution = new IntIntOpenHashMap();
    /** The number of suppressed tuples */
    private int                 suppressed   = 0;
    /** Entries that can be suppressed */
    private HashGroupifyEntry[] entries;
    /** Offset into the array of entries that can be suppressed */
    private int                 offset       = 0;
    /** Number of tuples in the data set */
    private double              numTuples;
    /** Number of classes in the data set */
    private double              numClasses   = 0;

    /**
     * Creates a new instance
     * @param entry
     */
    HashGroupifyDistribution(HashGroupifyEntry entry) {
        
        // Initialize
        List<HashGroupifyEntry> list = new ArrayList<HashGroupifyEntry>();
        while(entry != null) {
            if (entry.isNotOutlier && entry.count > 0) {
                this.numClasses++;
                addToDistribution(entry.count);
                list.add(entry);
            } else {
                this.suppressed += entry.count;
            }
            numTuples += entry.count;
            entry = entry.nextOrdered;
        }
        
        // Suppressed tuples form one equivalence class
        if (suppressed != 0) {
            this.distribution.putOrAdd(suppressed, 1, 1);
            this.numClasses++;
        }
        
        // Sort & store suppressible entries
        Collections.sort(list, new Comparator<HashGroupifyEntry>(){
            public int compare(HashGroupifyEntry o1, HashGroupifyEntry o2) {
                int cmp = Integer.compare(o1.count, o2.count);
                return cmp != 0 ? cmp : Integer.compare(o1.representant, o2.representant);
            }
        });
        this.entries = list.toArray(new HashGroupifyEntry[list.size()]);
    }
    
    /**
     * Returns a set of classes as an input for the risk model
     */
    public RiskModelEquivalenceClasses getEquivalenceClasses() {
        return new RiskModelEquivalenceClasses(this.distribution);
    }

    /**
     * Suppresses one entry. Returns the size of the suppressed class or 0, if no entry existed that could be suppressed
     * @return
     */
    public int suppressNextClass() {
        if (offset == entries.length) {
            return 0;
        }
        HashGroupifyEntry entry = entries[offset++];
        entry.isNotOutlier = false;
        removeFromDistribution(entry.count);
        if (this.suppressed != 0) removeFromDistribution(suppressed);
        this.suppressed += entry.count;
        addToDistribution(suppressed);
        return entry.count;
    }

    /**
     * Adds an entry
     * @param size
     */
    private void addToDistribution(int size) {
        this.numClasses++;
        this.distribution.putOrAdd(size, 1, 1);   
    }

    /**
     * Removes an entry
     * @param size
     */
    private void removeFromDistribution(int size) {
        this.numClasses--;
        int previous = distribution.remove(size);
        if (previous != 1) {
            distribution.put(size, previous - 1);
        }
    }

    /**
     * Returns the average class size
     * @return
     */
    public double getAverageClassSize() {
        return numClasses / numTuples;
    }

    /**
     * Returns the fraction of tuples that are in classes of the given size
     * @param size
     * @return
     */
    public double getFractionOfTuplesInClassesOfSize(int size) {
        return (double)distribution.get(size) * (double)size / numTuples;
    }

    /**
     * Returns the number of tuples
     * @return
     */
    public int getNumberOfTuples() {
        return (int)this.numTuples;
    }
}
