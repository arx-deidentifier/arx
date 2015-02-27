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
    private IntIntOpenHashMap   distribution        = new IntIntOpenHashMap();
    /** The number of suppressed tuples */
    private int                 suppressed          = 0;
    /** Entries that can be suppressed */
    private HashGroupifyEntry[] entries;
    /** Number of tuples in the data set */
    private double              numTuples;
    /** Number of classes in the data set */
    private double              numClasses          = 0;

    /** State of the binary search */
    private int                 binaryLow           = 0;
    /** State of the binary search */
    private int                 binaryHigh          = 0;
    /** State of the binary search */
    private int                 binaryMid           = 0;
    /** State of the binary search */
    private int                 initiallySuppressed = 0;

    /** State of the linear search */
    private int                 linearOffset        = 0;
    
    /**
     * Creates a new instance
     * @param entry
     */
    HashGroupifyDistribution(HashGroupifyEntry entry) {
        
        // Initialize
        List<HashGroupifyEntry> list = new ArrayList<HashGroupifyEntry>();
        while(entry != null) {
            if (entry.isNotOutlier && entry.count > 0) {
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
            addToDistribution(suppressed);
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
     * Returns the average class size
     * @return
     */
    public double getAverageClassSize() {
        return numTuples / numClasses;
    }

    /**
     * Returns a set of classes as an input for the risk model
     */
    public RiskModelEquivalenceClasses getEquivalenceClasses() {
        return new RiskModelEquivalenceClasses(this.distribution);
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
     * Debugging stuff
     * @return
     */
    public int getMaxSuppressedIndex() {
        for (int i=0; i<entries.length; i++) {
            if (entries[i].isNotOutlier) {
                
                int count = 0;
                for (int j=i+1; j<entries.length; j++) {
                    if (entries[i].isNotOutlier) {
                        count++;
                    }
                }
                
                if (count != 0) {
                    throw new RuntimeException("ARGL: "+count);
                }
                
                return i-1;
            }
        }
        return entries.length-1;
    }
    
    /**
     * Returns the number of tuples
     * @return
     */
    public int getNumberOfTuples() {
        return (int)this.numTuples;
    }
    
    /**
     * Returns the number of suppressed tuples
     * @return
     */
    public int getNumOfSuppressedTuples() {
        return this.suppressed;
    }
    
    /**
     * Suppress less tuples
     * @return
     */
    public int suppressBinaryLess() {

        int newBinaryHigh = binaryMid - 1;
        int newBinaryMid = (binaryLow + newBinaryHigh) / 2;

        for (int i = newBinaryMid + 1; i <= binaryHigh; i++) {
            HashGroupifyEntry entry = entries[i];
            unSuppressEntry(entry);
        }
        
        binaryHigh = newBinaryHigh;
        binaryMid = newBinaryMid;
        
        // mid - high
        return suppressed - initiallySuppressed;
    }

    /**
     * Suppress more tuples
     * @return
     */
    public int suppressBinaryMore() {
        
        binaryLow = binaryMid + 1;
        binaryMid = (binaryLow + binaryHigh) / 2;
        
        // low-mid
        
        for (int i = binaryLow; i <= binaryMid; i++) {
            HashGroupifyEntry entry = entries[i];
            suppressEntry(entry);
        }
        
        return suppressed - initiallySuppressed;
    }
    
    /**
     * Starts binary suppression
     * @return
     */
    public int suppressBinaryStart() {
        binaryLow = 0;
        binaryHigh = entries.length - 1;
        binaryMid = (binaryLow + binaryHigh) / 2;
        initiallySuppressed = suppressed;
        
        // low-mid
        
        for (int i = binaryLow; i <= binaryMid; i++) {
            HashGroupifyEntry entry = entries[i];
            suppressEntry(entry);
        }
        return suppressed - initiallySuppressed;
    }

    /**
     * Suppresses one entry. Returns the size of the suppressed class or 0, if no entry existed that could be suppressed
     * @return
     */
    public int suppressLinearMore() {
        if (linearOffset == entries.length) {
            return 0;
        }
        HashGroupifyEntry entry = entries[linearOffset++];
        suppressEntry(entry);
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
     * Suppresses the given entry
     * @param entry
     */
    private void suppressEntry(HashGroupifyEntry entry) {
        entry.isNotOutlier = false;
        removeFromDistribution(entry.count);
        if (this.suppressed != 0) {
            removeFromDistribution(this.suppressed);
        }
        this.suppressed += entry.count;
        addToDistribution(this.suppressed);
    }

    /**
     * Unsuppressed the given entry
     * @param entry
     */
    private void unSuppressEntry(HashGroupifyEntry entry) {
        
        if (this.suppressed == 0) {
            throw new IllegalStateException("Must not happed");
        }
        
        entry.isNotOutlier = true;
        removeFromDistribution(this.suppressed);
        this.suppressed -= entry.count;
        if (this.suppressed != 0) {
            addToDistribution(this.suppressed);
        }
        addToDistribution(entry.count);
    }
}
