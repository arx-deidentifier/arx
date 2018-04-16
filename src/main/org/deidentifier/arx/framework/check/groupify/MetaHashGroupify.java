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

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.framework.data.DataMatrix;

/**
 * A hash groupify operator for entries of hash groupifies
 * 
 * @author Fabian Prasser
 */
public class MetaHashGroupify {

    /** The entry array. */
    private MetaHashGroupifyEntry[] hashTableBuckets;

    /** Current number of elements. */
    private int                     hashTableElementCount;

    /** The first entry. */
    private MetaHashGroupifyEntry   hashTableFirstEntry;

    /** The last entry. */
    private MetaHashGroupifyEntry   hashTableLastEntry;

    /** Load factor. */
    private final float             hashTableLoadFactor = 0.75f;

    /** Output data */
    private final DataMatrix        output;

    /** Index of attribute to analyze */
    private final int               analysisIndex;

    /** Maximum number of elements that can be put in this map before having to rehash. */
    private int                     hashTableThreshold;

    /**
     * Constructs a new operator.
     *
     * @param groupify
     * @param index
     */
    public MetaHashGroupify(HashGroupify groupify, int index) {
        
        // Set capacity
        int capacity = HashTableUtil.calculateCapacity(groupify.getNumberOfEquivalenceClasses());
        this.output = groupify.getOutputData();
        this.hashTableElementCount = 0;
        this.hashTableBuckets = new MetaHashGroupifyEntry[capacity];
        this.hashTableThreshold = HashTableUtil.calculateThreshold(hashTableBuckets.length, hashTableLoadFactor);
        this.analysisIndex = index;
    }
    
    /**
     * Adds a tuple from the buffer
     * @param _entry
     */
    public void add(HashGroupifyEntry _entry) {
        
        // Check
        if (!_entry.isNotOutlier) {
            throw new IllegalArgumentException("Suppressed entries may not be added");
        }
        
        // Calculate hash
        final int hash = output.hashCodeIgnore(_entry.row, analysisIndex);
        
        // Find or create entry
        int index = hash & (hashTableBuckets.length - 1);
        MetaHashGroupifyEntry entry = findEntry(_entry.row, index, hash);
        if (entry == null) {
            if (++hashTableElementCount > hashTableThreshold) {
                rehash();
                index = hash & (hashTableBuckets.length - 1);
            }
            entry = createEntry(_entry.row, index, hash);
        }
        
        // Add
        entry.distribution.add(output.get(_entry.row, analysisIndex), _entry.count);
    }
    
    /**
     * Returns the first entry
     * @return
     */
    public MetaHashGroupifyEntry getFirstEntry() {
        return hashTableFirstEntry;
    }
    
    /**
     * Creates a new entry.
     * 
     * @param row the row
     * @param index the index
     * @param hash the hash
     * @return the hash groupify entry
     */
    private MetaHashGroupifyEntry createEntry(final int row, final int index, final int hash) {
        final MetaHashGroupifyEntry entry = new MetaHashGroupifyEntry(row, hash);
        entry.next = hashTableBuckets[index];
        hashTableBuckets[index] = entry;
        if (hashTableFirstEntry == null) {
            hashTableFirstEntry = entry;
            hashTableLastEntry = entry;
        } else {
            hashTableLastEntry.nextOrdered = entry;
            hashTableLastEntry = entry;
        }
        return entry;
    }

    /**
     * Returns the according entry.
     * 
     * @param row the row
     * @param index the index
     * @param keyHash the key hash
     * @return the hash groupify entry
     */
    private MetaHashGroupifyEntry findEntry(final int row, final int index, final int keyHash) {
        MetaHashGroupifyEntry m = hashTableBuckets[index];
        while ((m != null) && ((m.hashcode != keyHash) || !output.equalsIgnore(row, m.row, analysisIndex))) {
            m = m.next;
        }
        return m;
    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {
        
        final int length = HashTableUtil.calculateCapacity((hashTableBuckets.length == 0 ? 1 : hashTableBuckets.length << 1));
        final MetaHashGroupifyEntry[] newData = new MetaHashGroupifyEntry[length];
        MetaHashGroupifyEntry entry = hashTableFirstEntry;
        while (entry != null) {
            final int index = entry.hashcode & (length - 1);
            entry.next = newData[index];
            newData[index] = entry;
            entry = entry.nextOrdered;
        }
        hashTableBuckets = newData;
        hashTableThreshold = HashTableUtil.calculateThreshold(hashTableBuckets.length, hashTableLoadFactor);
    }
}
