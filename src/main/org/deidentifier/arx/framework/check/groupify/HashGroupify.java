/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.data.Data;

/**
 * A hash groupify operator. It implements a hash table with chaining and keeps
 * track of additional properties per equivalence class
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class HashGroupify implements IHashGroupify {

    /**
     * Statistics about the groups, excluding outliers
     * @author Fabian Prasser
     */
    public static class GroupStatistics {

        private double averageEquivalenceClassSize;
        private int    maximalEquivalenceClassSize;
        private int    minimalEquivalenceClassSize;
        private double averageEquivalenceClassSizeIncludingOutliers;
        private int    maximalEquivalenceClassSizeIncludingOutliers;
        private int    minimalEquivalenceClassSizeIncludingOutliers;
        private int    numberOfGroups;
        private int    numberOfOutlyingEquivalenceClasses;
        private int    numberOfOutlyingTuples;

        /**
         * Creates a new instance
         * @param handle
         * @param averageEquivalenceClassSize
         * @param maximalEquivalenceClassSize
         * @param minimalEquivalenceClassSize
         * @param averageEquivalenceClassSizeIncludingOutliers
         * @param maximalEquivalenceClassSizeIncludingOutliers
         * @param minimalEquivalenceClassSizeIncludingOutliers
         * @param numberOfGroups
         * @param numberOfOutlyingEquivalenceClasses
         * @param numberOfOutlyingTuples
         */
        protected GroupStatistics(double averageEquivalenceClassSize,
                                  int maximalEquivalenceClassSize,
                                  int minimalEquivalenceClassSize,
                                  double averageEquivalenceClassSizeIncludingOutliers,
                                  int maximalEquivalenceClassSizeIncludingOutliers,
                                  int minimalEquivalenceClassSizeIncludingOutliers,
                                  int numberOfGroups,
                                  int numberOfOutlyingEquivalenceClasses,
                                  int numberOfOutlyingTuples) {
            this.averageEquivalenceClassSize = averageEquivalenceClassSize;
            this.maximalEquivalenceClassSize = maximalEquivalenceClassSize;
            this.minimalEquivalenceClassSize = minimalEquivalenceClassSize;
            this.averageEquivalenceClassSizeIncludingOutliers = averageEquivalenceClassSizeIncludingOutliers;
            this.maximalEquivalenceClassSizeIncludingOutliers = maximalEquivalenceClassSizeIncludingOutliers;
            this.minimalEquivalenceClassSizeIncludingOutliers = minimalEquivalenceClassSizeIncludingOutliers;
            this.numberOfGroups = numberOfGroups;
            this.numberOfOutlyingEquivalenceClasses = numberOfOutlyingEquivalenceClasses;
            this.numberOfOutlyingTuples = numberOfOutlyingTuples;
        }

        /**
         * Returns the maximal size of an equivalence class
         * @return
         */
        public double getAverageEquivalenceClassSize() {
            return averageEquivalenceClassSize;
        }

        /**
         * Returns the maximal size of an equivalence class. This number takes into account one additional
         * equivalence class containing all outliers
         * @return
         */
        public double getAverageEquivalenceClassSizeIncludingOutliers() {
            return averageEquivalenceClassSizeIncludingOutliers;
        }

        /**
         * Returns the maximal size of an equivalence class
         * @return
         */
        public int getMaximalEquivalenceClassSize() {
            return maximalEquivalenceClassSize;
        }

        /**
         * Returns the maximal size of an equivalence class. This number takes into account one additional
         * equivalence class containing all outliers
         * @return
         */
        public int getMaximalEquivalenceClassSizeIncludingOutliers() {
            return maximalEquivalenceClassSizeIncludingOutliers;
        }

        /**
         * Returns the minimal size of an equivalence class
         * @return
         */
        public int getMinimalEquivalenceClassSize() {
            return minimalEquivalenceClassSize;
        }

        /**
         * Returns the minimal size of an equivalence class. This number takes into account one additional
         * equivalence class containing all outliers
         * @return
         */
        public int getMinimalEquivalenceClassSizeIncludingOutliers() {
            return minimalEquivalenceClassSizeIncludingOutliers;
        }

        /**
         * Returns the number of equivalence classes in the currently selected data
         * representation
         * 
         * @return
         */
        public int getNumberOfGroups() {
            return numberOfGroups;
        }

        /**
         * Returns the number of outlying equivalence classes in the currently selected data
         * representation
         * 
         * @return
         */
        public int getNumberOfOutlyingEquivalenceClasses() {
            return numberOfOutlyingEquivalenceClasses;
        }

        /**
         * Returns the number of outliers in the currently selected data
         * representation
         * 
         * @return
         */
        public int getNumberOfOutlyingTuples() {
            return numberOfOutlyingTuples;
        }
    }

    /** Is the result k-anonymous? */
    private boolean                  kAnonymous;

    /** Is the result anonymous */
   private boolean                   anonymous;

    /** The current outliers. */
    private int                      currentOutliers;

    /** Current number of elements. */
    private int                      elementCount;

    /** The entry array. */
    private HashGroupifyEntry[]      buckets;

    /** The first entry. */
    private HashGroupifyEntry        firstEntry;

    /** The last entry. */
    private HashGroupifyEntry        lastEntry;

    /** Load factor. */
    private final float              loadFactor = 0.75f;

    /** Maximum number of elements that can be put in this map before having to rehash. */
    private int                      threshold;

    /** Allowed tuple outliers */
    private final int                absoluteMaxOutliers;

    /** The parameter k, if k-anonymity is contained in the set of criteria */
    private final int                k;

    /** The research subset, if d-presence is contained in the set of criteria */
    private final RowSet             subset;

    /** Criteria*/
    private final PrivacyCriterion[] criteria;

    /**
     * Constructs a new hash groupify operator
     * 
     * @param capacity The capacity
     * @param config The config
     */
    public HashGroupify(int capacity, final ARXConfiguration config) {

        // Set capacity
        capacity = HashTableUtil.calculateCapacity(capacity);
        this.elementCount = 0;
        this.buckets = new HashGroupifyEntry[capacity];
        this.threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);

        this.currentOutliers = 0;
        this.absoluteMaxOutliers = config.getAbsoluteMaxOutliers();

        // Extract research subset
        if (config.containsCriterion(DPresence.class)) {
            this.subset = config.getCriterion(DPresence.class).getSubset().getSet();
        } else {
            this.subset = null;
        }

        // Extract criteria
        this.criteria = config.getCriteriaAsArray();
        this.k = config.getMinimalGroupSize();
    }
    
    @Override
    public void analyze(){
        
        // We have only checked k-anonymity so far
        kAnonymous = (currentOutliers <= absoluteMaxOutliers);
        
        // Abort early, if only k-anonymity was specified
        if (criteria.length == 0) { 
            anonymous = kAnonymous;
            return;
        }
        
        // Abort early, if k-anonymity subcriterion is not fulfilled
        // CAUTION: This leaves GroupifyEntry.isNotOutlier and currentOutliers in an inconsistent state
        //          for non-anonymous transformations
        if (k != Integer.MAX_VALUE && !kAnonymous) {
            anonymous = false;
            return; 
        }
        
        // Iterate over all classes
        currentOutliers = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            
            // Check for anonymity
            final boolean anonymous = isAnonymous(entry);
            
            // Determine outliers
            if (!anonymous) {
                currentOutliers += entry.count;
                
                // Break as soon as too many classes are not anonymous
                // CAUTION: This leaves GroupifyEntry.isNotOutlier and currentOutliers in an inconsistent state
                //          for non-anonymous transformations
                if (currentOutliers > absoluteMaxOutliers) { 
                    this.anonymous = false;
                    return;
                }
            }
            // Next class
            entry.isNotOutlier = anonymous;
            entry = entry.nextOrdered;
        }
        
        this.anonymous = true;
    }

    @Override
    public void addAll(int[] key, int representant, int count, int[] sensitive, int pcount) {

        // Add
        final int hash = HashTableUtil.hashcode(key);
        final HashGroupifyEntry entry = addInternal(key, hash, representant, count, pcount);

        // Is a sensitive attribute provided
        if (sensitive != null) {
            if (entry.distributions == null) {
                entry.distributions = new Distribution[sensitive.length];
                
                // TODO: Improve!
                for (int i=0; i<entry.distributions.length; i++){
                    entry.distributions[i] = new Distribution();
                }
            }

            // Only add sensitive value if in research subset
            if (subset == null || subset.contains(representant)) {

                // TODO: Improve!
                for (int i=0; i<entry.distributions.length; i++){
                    entry.distributions[i].add(sensitive[i]);
                }
            }
        }
    }

    @Override
    public void addGroupify(int[] key, int representant, int count, Distribution[] distributions, int pcount) {

        // Add
        final int hash = HashTableUtil.hashcode(key);
        final HashGroupifyEntry entry = addInternal(key, hash, representant, count, pcount);

        // Is a distribution provided
        if (distributions != null) {
            if (entry.distributions == null) {
                entry.distributions = distributions;
            } else {

                // TODO: Improve!
                for (int i=0; i<entry.distributions.length; i++){
                    entry.distributions[i].merge(distributions[i]);
                }
            }
        }
    }

    @Override
    public void addSnapshot(int[] key, int representant, int count, int[][] elements, int[][] frequencies, int pcount) {

        // Add
        final int hash = HashTableUtil.hashcode(key);
        final HashGroupifyEntry entry = addInternal(key, hash, representant, count, pcount);

        // Is a distribution provided
        if (elements != null) {
            if (entry.distributions == null) {
                
                entry.distributions = new Distribution[elements.length];
                
                // TODO: Improve!
                for (int i=0; i<entry.distributions.length; i++){
                    entry.distributions[i] = new Distribution(elements[i], frequencies[i]);
                }
            } else {

                // TODO: Improve!
                for (int i=0; i<entry.distributions.length; i++){
                    entry.distributions[i].merge(elements[i], frequencies[i]);
                }
            }
        }
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
    public GroupStatistics getGroupStatistics() {

        // Statistics about equivalence classes
        double averageEquivalenceClassSize = 0;
        int averageEquivalenceClassSizeCounter = 0;
        int maximalEquivalenceClassSize = Integer.MIN_VALUE;
        int minimalEquivalenceClassSize = Integer.MAX_VALUE;
        int numberOfEquivalenceClasses = 0;
        int numberOfOutlyingEquivalenceClasses = 0;
        int numberOfOutlyingTuples = 0;
        
        // If there is no subset
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            if (entry.count > 0){
                numberOfEquivalenceClasses++;
                if (this.anonymous && !entry.isNotOutlier) { 
                     numberOfOutlyingEquivalenceClasses++;
                     numberOfOutlyingTuples += entry.count;
                } else {
                    averageEquivalenceClassSizeCounter += entry.count;
                    maximalEquivalenceClassSize = Math.max(maximalEquivalenceClassSize, entry.count);
                    minimalEquivalenceClassSize = Math.min(minimalEquivalenceClassSize, entry.count);
                 }
             }
             entry = entry.nextOrdered;
         }
        
        // Sanitize
        if (minimalEquivalenceClassSize == Integer.MAX_VALUE){
            minimalEquivalenceClassSize = 0;
        }
        if (maximalEquivalenceClassSize == Integer.MIN_VALUE){
            maximalEquivalenceClassSize = 0;
        } 
        if (numberOfEquivalenceClasses - numberOfOutlyingEquivalenceClasses == 0){
            averageEquivalenceClassSize = 0;
        } else {
            averageEquivalenceClassSize = (double) averageEquivalenceClassSizeCounter / 
                                          (double) (numberOfEquivalenceClasses - numberOfOutlyingEquivalenceClasses);
        }
         
         // Statistics including suppression
         double averageEquivalenceClassSizeAll = averageEquivalenceClassSize;
         int maximalEquivalenceClassSizeAll = maximalEquivalenceClassSize;
         int minimalEquivalenceClassSizeAll = minimalEquivalenceClassSize;
         if (averageEquivalenceClassSize != 0 && numberOfOutlyingTuples > 0){
             averageEquivalenceClassSizeAll = (double)(averageEquivalenceClassSizeCounter + numberOfOutlyingTuples) /
                                              (double)(numberOfEquivalenceClasses - numberOfOutlyingEquivalenceClasses + 1);
             
             maximalEquivalenceClassSizeAll = Math.max(maximalEquivalenceClassSize, numberOfOutlyingTuples);
             minimalEquivalenceClassSizeAll = Math.min(minimalEquivalenceClassSize, numberOfOutlyingTuples);
         } else {
             averageEquivalenceClassSizeAll = 0;
             maximalEquivalenceClassSizeAll = 0;
             minimalEquivalenceClassSizeAll = 0;
         }
         
         // Return
         return new GroupStatistics(averageEquivalenceClassSize,
                                    maximalEquivalenceClassSize,
                                    minimalEquivalenceClassSize,
                                    averageEquivalenceClassSizeAll,
                                    maximalEquivalenceClassSizeAll,
                                    minimalEquivalenceClassSizeAll,
                                    numberOfEquivalenceClasses,
                                    numberOfOutlyingEquivalenceClasses,
                                    numberOfOutlyingTuples);
    }
    
    @Override
    public boolean isAnonymous() {
        return anonymous;
    }
    
    @Override
    public boolean isKAnonymous() {
        return kAnonymous;
    }

    @Override
    public void markOutliers(final int[][] data) {
        
        if (!anonymous) return;
        
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

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.groupify.IHashGroupify#size()
     */
    @Override
    public int size() {
        return elementCount;
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
    private final HashGroupifyEntry addInternal(final int[] key, final int hash, final int representant, int count, final int pcount) {

        // Is the line contained in the research subset
        if (subset != null && !subset.contains(representant)) {
            count = 0;
        }

        // Add entry
        int index = hash & (buckets.length - 1);
        HashGroupifyEntry entry = findEntry(key, index, hash);
        if (entry == null) {
            if (++elementCount > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(key, index, hash, representant);
        }
        entry.count += count;

        // indirectly check if we are in d-presence mode
        if (subset != null) {
            entry.pcount += pcount;
            if (count > 0) {
                // this is a research subset line
                // reset representant, necessary for rollup / history
                // (otherwise researchSubset.get(line) would potentially be false)
                entry.representant = representant;
            }
        }

        // Compute current outliers, if k-anonymity is part of the criteria
        if (entry.count >= k) {
            if (!entry.isNotOutlier) {
                entry.isNotOutlier = true;
                currentOutliers -= (entry.count - count);
            }
        } else {
            currentOutliers += count;
        }

        return entry;
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

    /**
     * Checks whether the given entry is anonymous
     * @param entry
     * @return
     */
    private boolean isAnonymous(HashGroupifyEntry entry) {

        // Check minimal group size
        if (k != Integer.MAX_VALUE && entry.count < k) { return false; }

        // Check other criteria
        for (int i = 0; i < criteria.length; i++) {
            if (!criteria[i].isAnonymous(entry)) { return false; }
        }
        return true;
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
}
