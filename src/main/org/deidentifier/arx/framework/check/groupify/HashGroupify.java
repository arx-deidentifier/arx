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

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.Inclusion;
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
     * Statistics about the groups, excluding outliers.
     *
     * @author Fabian Prasser
     */
    public static class GroupStatistics {

        /**  TODO */
        private double averageEquivalenceClassSize;
        
        /**  TODO */
        private int    maximalEquivalenceClassSize;
        
        /**  TODO */
        private int    minimalEquivalenceClassSize;
        
        /**  TODO */
        private double averageEquivalenceClassSizeIncludingOutliers;
        
        /**  TODO */
        private int    maximalEquivalenceClassSizeIncludingOutliers;
        
        /**  TODO */
        private int    minimalEquivalenceClassSizeIncludingOutliers;
        
        /**  TODO */
        private int    numberOfGroups;
        
        /**  TODO */
        private int    numberOfOutlyingEquivalenceClasses;
        
        /**  TODO */
        private int    numberOfOutlyingTuples;

        /**
         * Creates a new instance.
         *
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
         * Returns the maximal size of an equivalence class.
         *
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
         * Returns the maximal size of an equivalence class.
         *
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
         * Returns the minimal size of an equivalence class.
         *
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
         * representation.
         *
         * @return
         */
        public int getNumberOfGroups() {
            return numberOfGroups;
        }

        /**
         * Returns the number of outlying equivalence classes in the currently selected data
         * representation.
         *
         * @return
         */
        public int getNumberOfOutlyingEquivalenceClasses() {
            return numberOfOutlyingEquivalenceClasses;
        }

        /**
         * Returns the number of outliers in the currently selected data
         * representation.
         *
         * @return
         */
        public int getNumberOfOutlyingTuples() {
            return numberOfOutlyingTuples;
        }
    }

    /** Is the result k-anonymous?. */
    private boolean                  kAnonymous;

    /** Is the result anonymous. */
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

    /** Allowed tuple outliers. */
    private final int                absoluteMaxOutliers;

    /** The parameter k, if k-anonymity is contained in the set of criteria. */
    private final int                k;

    /** The research subset, if d-presence is contained in the set of criteria. */
    private final RowSet             subset;

    /** True, if the contained d-presence criterion is not inclusion. */
    private final boolean            dpresence;

    /** Criteria. */
    private final PrivacyCriterion[] criteria;

    /**
     * Constructs a new hash groupify operator.
     *
     * @param capacity The capacity
     * @param config The config
     */
    public HashGroupify(int capacity, final ARXConfigurationInternal config) {

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
        
        // Sanity check: by convention, d-presence must be the first criterion 
        // See analyze() and isAnonymous(Entry) for more details
        for (int i=1; i<criteria.length; i++) {
            if (criteria[i] instanceof DPresence) {
                throw new RuntimeException("D-Presence must be the first criterion in the array");
            }
        }
        
        // Remember, if (real) d-presence is part of the criteria that must be enforced
        dpresence = (criteria.length > 0 && (criteria[0] instanceof DPresence) && !(criteria[0] instanceof Inclusion));
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#addAll(int[], int, int, int[], int)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#addGroupify(int[], int, int, org.deidentifier.arx.framework.check.distribution.Distribution[], int)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#addSnapshot(int[], int, int, int[][], int[][], int)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#analyze(boolean)
     */
    @Override
    public void analyze(boolean force){
        if (force) analyzeAll();
        else analyzeWithEarlyAbort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.groupify.IHashGroupify#clear()
     */
    @Override
    public void clear() {
        if (elementCount > 0) {
            this.elementCount = 0;
            this.currentOutliers = 0;
            this.firstEntry = null;
            this.lastEntry = null;
            HashTableUtil.nullifyArray(buckets);
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#getGroupStatistics()
     */
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
                if (!entry.isNotOutlier) { 
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#isAnonymous()
     */
    @Override
    public boolean isAnonymous() {
        return anonymous;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#isKAnonymous()
     */
    @Override
    public boolean isKAnonymous() {
        return kAnonymous;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.framework.check.groupify.IHashGroupify#markOutliers(int[][])
     */
    @Override
    public void markOutliers(final int[][] data) {
        
        for (int row = 0; row < data.length; row++) {
            if (subset == null || subset.contains(row)){
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
    }

    /**
     * This method will reset all flags that indicate that equivalence classes are suppressed.
     */
    public void resetSuppression() {
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            entry.isNotOutlier = true;
            entry = entry.nextOrdered;
        }
        this.currentOutliers = 0;
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
     * @param key the key
     * @param hash the hash
     * @param representant
     * @param count
     * @param pcount
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
                // this is a tuple from the research subset: Reset its representative, necessary for rollup / history
                // (otherwise subset.contains(tupleID) could potentially return false)
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
     * Analyze.
     */
    private void analyzeAll(){

        // We have only checked k-anonymity so far
        kAnonymous = (currentOutliers <= absoluteMaxOutliers);
        
        // Iterate over all classes
        boolean dpresent = true;
        currentOutliers = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            
            // Check for anonymity
            int anonymous = isAnonymous(entry);
            
            // Determine outliers
            if (anonymous != -1) {
                
                // Note: If d-presence exists, it is stored at criteria[0] by convention.
                // If it fails, isAnonymous(entry) thus returns 1.
                // Tuples from the public table that have no matching candidates in the private table
                // and that do not fulfill d-presence cannot be suppressed. In this case, the whole
                // transformation must be considered to not fulfill the privacy criteria.
                if (dpresence && entry.count == 0 && anonymous == 1) {
                    dpresent = false;
                }
                
                currentOutliers += entry.count;
            }
            
            // We only suppress classes that are contained in the research subset
            entry.isNotOutlier = entry.count != 0 ? (anonymous == -1) : true;
            
            // Next class
            entry = entry.nextOrdered;
        }
        
        this.anonymous = (currentOutliers <= absoluteMaxOutliers) && dpresent;
    }

    /**
     * Analyze.
     */
    private void analyzeWithEarlyAbort(){
        
        // We have only checked k-anonymity so far
        kAnonymous = (currentOutliers <= absoluteMaxOutliers);
        
        // Abort early, if only k-anonymity was specified
        if (criteria.length == 0) { 
            anonymous = kAnonymous;
            return;
        }
        
        // Abort early, if k-anonymity sub-criterion is not fulfilled
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
            int anonymous = isAnonymous(entry);
            
            // Determine outliers
            if (anonymous != -1) {
                
                // Note: If d-presence exists, it is stored at criteria[0] by convention.
                // If it fails, isAnonymous(entry) thus returns 1.
                // Tuples from the public table that have no matching candidates in the private table
                // and that do not fulfill d-presence cannot be suppressed. In this case, the whole
                // transformation must be considered to not fulfill the privacy criteria.
                // CAUTION: This leaves GroupifyEntry.isNotOutlier and currentOutliers in an inconsistent state
                //          for non-anonymous transformations
                if (dpresence && entry.count == 0 && anonymous == 1) {
                    this.anonymous = false;
                    return;
                }
                currentOutliers += entry.count;
                
                // Break as soon as too many classes are not anonymous
                // CAUTION: This leaves GroupifyEntry.isNotOutlier and currentOutliers in an inconsistent state
                //          for non-anonymous transformations
                if (currentOutliers > absoluteMaxOutliers) { 
                    this.anonymous = false;
                    return;
                }
            }
            
            // We only suppress classes that are contained in the research subset
            entry.isNotOutlier = entry.count != 0 ? (anonymous == -1) : true;
            
            // Next class
            entry = entry.nextOrdered;
        }
        
        this.anonymous = true;
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
     * TODO: Ugly!.
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
     * Checks whether the given entry is anonymous.
     *
     * @param entry
     * @return
     * @returns -1, if all criteria are fulfilled, 0, if minimal group size is not fulfilled, (index+1) if criteria[index] is not fulfilled
     */
    private int isAnonymous(HashGroupifyEntry entry) {

        // Check minimal group size
        if (k != Integer.MAX_VALUE && entry.count < k) {
            return 0;
        }

        // Check other criteria
        // Note: The d-presence criterion must be checked first to ensure correct handling of d-presence with tuple suppression.
        //       This is currently ensured by convention. See ARXConfiguration.getCriteriaAsArray();
        for (int i = 0; i < criteria.length; i++) {
            if (!criteria[i].isAnonymous(entry)) {
                return i + 1;
            }
        }
        return -1;
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
