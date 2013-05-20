/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.CompressedBitSet;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.data.Data;

/**
 * A hash groupify operator. It implements a hash table with chaining and keeps
 * track of additional properties per equivalence class
 * 
 * @author Prasser, Kohlmayer
 */
public class HashGroupify implements IHashGroupify {

    /** The current outliers. */
    private int                    currentOutliers;

    /** Current number of elements. */
    private int                    elementCount;

    /** The entry array. */
    private HashGroupifyEntry[]    buckets;

    /** The first entry. */
    private HashGroupifyEntry      firstEntry;

    /** The current config */
    private final ARXConfiguration config;

    /** The last entry. */
    private HashGroupifyEntry      lastEntry;

    /** Load factor. */
    private final float            loadFactor = 0.75f;

    /**
     * Maximum number of elements that can be put in this map before having to
     * rehash.
     */
    private int                    threshold;

    /** Allowed tuple outliers */
    private final int              absoluteMaxOutliers;

    /** The parameter k, if k-anonymity is contained in the set of criteria */
    private final int              k;
    
    /** The research subset, if d-presence is contained in the set of criteria */
    private final CompressedBitSet subset;

    /**
     * Constructs a new hash groupify operator
     * 
     * @param capacity
     *            The capacity
     * @param config
     *            The config
     */
    public HashGroupify(int capacity, final ARXConfiguration config) {

        // Set capacity
        capacity = HashTableUtil.calculateCapacity(capacity);
        this.elementCount = 0;
        this.buckets = new HashGroupifyEntry[capacity];
        this.threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);

        this.config = config;
        this.currentOutliers = 0;
        this.absoluteMaxOutliers = config.getAbsoluteMaxOutliers();

        // Extract monotonic subcriterion
        if (config.containsCriterion(KAnonymity.class)) {
            k = config.getCriteria(KAnonymity.class).iterator().next().getK();
        } else {
            k = Integer.MAX_VALUE;
        }
        
        // Extract research subset
        if (config.containsCriterion(DPresence.class)) {
            subset = config.getCriteria(DPresence.class).iterator().next().getResearchSubset();
        } else {
            subset = null;
        }
    }
    

    @Override
    public void addAll(int[] key, int representant, int count, int sensitive, int pcount) {
        
        // Init
        final int hash = HashTableUtil.hashcode(key);
        final HashGroupifyEntry entry;
        
        // Is a research subset provided
        if (subset!=null && subset.get(representant)) {
            entry = addInternal(key, representant, count, hash, pcount);
        } else {
            entry = addInternal(key, representant, 0, hash, pcount);
        }
        
        // Is a sensitive attribute provided 
        if (sensitive!=-1){
            if (entry.distribution == null) {
                entry.distribution = new Distribution();
            }
            entry.distribution.add(sensitive);
        }
    }

    @Override
    public void addGroupify(int[] key, int representant, int count, Distribution distribution, int pcount) {
        
        // Init
        final int hash = HashTableUtil.hashcode(key);
        final HashGroupifyEntry entry;
        
        // Is a research subset provided
        if (subset!=null && subset.get(representant)) {
            entry = addInternal(key, representant, count, hash, pcount);
        } else {
            entry = addInternal(key, representant, 0, hash, pcount);
        }
        
        // Is a distribution provided 
        if (distribution!=null){
            if (entry.distribution == null) {
                entry.distribution = distribution;
            }
            entry.distribution.merge(distribution);
        }
    }

    @Override
    public void addSnapshot(int[] key, int representant, int count, int[] elements, int[] frequencies, int pcount) {
        
        // Init
        final int hash = HashTableUtil.hashcode(key);
        final HashGroupifyEntry entry;
        
        // Is a research subset provided
        if (subset!=null && subset.get(representant)) {
            entry = addInternal(key, representant, count, hash, pcount);
        } else {
            entry = addInternal(key, representant, 0, hash, pcount);
        }
        
        // Is a distribution provided 
        if (elements!=null){
            if (entry.distribution == null) {
                entry.distribution = new Distribution(elements, frequencies);
            }
            entry.distribution.merge(elements, frequencies);
        }
    }

    /**
     * Internal adder method.
     * 
     * @param key
     *            the key
     * @param line
     *            the line
     * @param value
     *            the value
     * @param hash
     *            the hash
     * @return the hash groupify entry
     */
    private final HashGroupifyEntry addInternal(final int[] key,
                                                final int line,
                                                final int value,
                                                final int hash,
                                                final int pvalue) {

        // Add entry
        int index = hash & (buckets.length - 1);
        HashGroupifyEntry entry = findEntry(key, index, hash);
        if (entry == null) {
            if (++elementCount > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(key, index, hash, line);
        }
        entry.count += value;

        // TODO: What is this?
        // indirectly check if we are in d-presence mode
        if (subset != null) {
            entry.pcount += pvalue;
            if (value > 0) { // this is a research subset line
                // reset representant, necessary for rollup / history (otherwise
                // researchSubset.get(line) would potentially be false)
                entry.representant = line;
            }
        }

        // Compute current outliers, if k-anonymity is part of the criteria
        if (entry.count >= k) {
            if (!entry.isNotOutlier) {
                entry.isNotOutlier = true;
                currentOutliers -= (entry.count - value);
            }
        } else {
            currentOutliers += value;
        }

        return entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.groupify.IHashGroupify#clear()
     */
    @Override
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            HashTableUtil.nullifyArray(buckets);
            currentOutliers = 0;
            firstEntry = null;
            lastEntry = null;
        }
    }

    /**
     * Creates a new entry.
     * 
     * @param key
     *            the key
     * @param index
     *            the index
     * @param hash
     *            the hash
     * @param line
     *            the line
     * @return the hash groupify entry
     */
    private HashGroupifyEntry createEntry(final int[] key, final int index, final int hash, final int line) {
        final HashGroupifyEntry entry = new HashGroupifyEntry(key, hash);
        entry.next = buckets[index];
        entry.representant = line;
        buckets[index] = entry;
        if (firstEntry == null) {
            firstEntry = entry;
            lastEntry = entry;
        } else {
            lastEntry.nextOrdered = entry;
            lastEntry = entry;
        }
        return entry;
    }

    /**
     * TODO: Ugly!
     * 
     * @param a
     * @param a2
     * @return
     */
    private boolean equalsIgnoringOutliers(final int[] a, final int[] a2) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != (a2[i] & Data.REMOVE_OUTLIER_MASK)) { return false; }
        }
        return true;
    }

    /**
     * Returns the according entry.
     * 
     * @param key
     *            the key
     * @param index
     *            the index
     * @param keyHash
     *            the key hash
     * @return the hash groupify entry
     */
    private final HashGroupifyEntry findEntry(final int[] key, final int index, final int keyHash) {
        HashGroupifyEntry m = buckets[index];
        while ((m != null) && ((m.hashcode != keyHash) || !HashTableUtil.equals(key, m.key))) {
            m = m.next;
        }
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#getFirstEntry
     * ()
     */
    @Override
    public HashGroupifyEntry getFirstEntry() {
        return firstEntry;
    }

    @Override
    public int getGroupOutliersCount() {
        // Iterate over all groups
        // TODO: Could be more efficient
        int result = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final boolean anonymous = isAnonymous(entry);
            if (!anonymous) {
                result++;
            }
            entry = entry.nextOrdered;
        }
        return result;
    }

    @Override
    public int getTupleOutliersCount() {

        // Iterate over all groups
        // TODO: Could be more efficient
        int result = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final boolean anonymous = isAnonymous(entry);
            if (!anonymous) {
                result += entry.count;
            }
            entry = entry.nextOrdered;
        }
        return result;
    }

    @Override
    public boolean isAnonymous() {

        // Iterate over all classes
        currentOutliers = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {

            // Check for anonymity
            final boolean anonymous = isAnonymous(entry);

            // Determine outliers
            if (!anonymous) {
                currentOutliers += entry.count;

                // Break as soon as any class is not anonymous
                if (currentOutliers > absoluteMaxOutliers) { return false; }
            }

            // Next class
            entry.isNotOutlier = anonymous;
            entry = entry.nextOrdered;
        }

        // All classes are anonymous
        return true;
    }

    /**
     * Checks whether the given entry is anonymous
     * @param entry
     * @return
     */
    private boolean isAnonymous(HashGroupifyEntry entry) {
        for (PrivacyCriterion c : config.getCriteria()) {
            if (!c.isAnonymous(entry)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is the current transformation k-anonymous? CAUTION: Call before
     * isAnonymous()!
     * 
     * @return
     */
    @Override
    public boolean isKAnonymous() {
        if (currentOutliers > absoluteMaxOutliers) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void markOutliers(final int[][] data) {
        for (int row = 0; row < data.length; row++) {
            final int[] key = data[row];
            final int hash = HashTableUtil.hashcode(key);
            final int index = hash & (buckets.length - 1);
            HashGroupifyEntry m = buckets[index];
            while ((m != null) && ((m.hashcode != hash) || !equalsIgnoringOutliers(key, m.key))) {
                m = m.next;
            }
            if (m == null) { throw new RuntimeException("Invalid state! Groupify the data before marking outliers!"); }
            if (!m.isNotOutlier) {
                key[0] |= Data.OUTLIER_MASK;
            }
        }
    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {

        final int length = HashTableUtil.calculateCapacity((buckets.length == 0 ? 1 : buckets.length << 1));
        final HashGroupifyEntry[] newData = new HashGroupifyEntry[length];
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final int index = entry.hashcode & (length - 1);
            entry.next = newData[index];
            newData[index] = entry;
            entry = entry.nextOrdered;
        }
        buckets = newData;
        threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.groupify.IHashGroupify#size()
     */
    @Override
    public int size() {
        return elementCount;
    }
}
