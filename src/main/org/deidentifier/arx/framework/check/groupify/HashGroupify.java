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

import java.util.concurrent.atomic.AtomicInteger;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.SampleBasedCriterion;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.Metric;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * A hash groupify operator. It implements a hash table with chaining and keeps
 * track of additional properties per equivalence class
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class HashGroupify {

    /** Criteria. */
    private final PrivacyCriterion[]     classBasedCriteria;

    /** The entry array. */
    private HashGroupifyEntry[]          hashTableBuckets;

    /** Current number of elements. */
    private int                          hashTableElementCount;

    /** The first entry. */
    private HashGroupifyEntry            hashTableFirstEntry;

    /** The last entry. */
    private HashGroupifyEntry            hashTableLastEntry;

    /** Load factor. */
    private final float                  hashTableLoadFactor = 0.75f;

    /** Maximum number of elements that can be put in this map before having to rehash. */
    private int                          hashTableThreshold;

    /** Do we ensure optimality for sample-based criteria */
    private final boolean                heuristicForSampleBasedCriteria;

    /** The parameter k, if k-anonymity is contained in the set of criteria. */
    private final int                    minimalClassSize;

    /** Is the result k-anonymous?. */
    private boolean                      minimalClassSizeFulfilled;

    /** True, if the contained d-presence criterion is not inclusion. */
    private final boolean                privacyModelContainsDPresence;

    /** The research subset, if d-presence is contained in the set of criteria. */
    private final RowSet                 privacyModelDefinesSubset;

    /** Is the result anonymous. */
    private boolean                      privacyModelFulfilled;

    /** Criteria. */
    private final SampleBasedCriterion[] sampleBasedCriteria;

    /** Allowed tuple outliers. */
    private final int                    suppressionLimit;

    /** Utility measure */
    private final Metric<?>              utilityMeasure;

    /** Lock manager */
    private final LockManager            lockManager;

    /** Counts the number of active threads */
    private final AtomicInteger          workingThreads      = new AtomicInteger(0);

    /** Counts the number of threads waiting for the rehash lock*/
    private final AtomicInteger          waitingThreads      = new AtomicInteger(0);
    
    /** The number of threads */
    private final int                    threads;

    /** Are we performing a single threaded run? */
    private boolean                      singleThreaded   = true;

    /**
     * Constructs a new hash groupify operator.
     *
     * @param capacity The capacity
     * @param config The config
     */
    public HashGroupify(int capacity, final ARXConfigurationInternal config) {
        
        // Set capacity
        capacity = HashTableUtil.calculateCapacity(capacity);
        this.hashTableElementCount = 0;
        this.hashTableBuckets = new HashGroupifyEntry[capacity];
        this.hashTableThreshold = HashTableUtil.calculateThreshold(hashTableBuckets.length, hashTableLoadFactor);
        this.threads = config.getNumThreads();
        if (threads > 1) {
            lockManager = new LockManager(capacity);
        } else {
            lockManager = null;
        }
        
        // Set params
        this.suppressionLimit = config.getAbsoluteMaxOutliers();
        this.utilityMeasure = config.getMetric();
        this.heuristicForSampleBasedCriteria = config.isUseHeuristicForSampleBasedCriteria();
        
        // Extract research subset
        if (config.containsCriterion(DPresence.class)) {
            this.privacyModelDefinesSubset = config.getCriterion(DPresence.class).getSubset().getSet();
        } else if (config.containsCriterion(EDDifferentialPrivacy.class)) {
            this.privacyModelDefinesSubset = config.getCriterion(EDDifferentialPrivacy.class).getSubset().getSet();
        } else {
            this.privacyModelDefinesSubset = null;
        }
        
        // Extract criteria
        this.classBasedCriteria = config.getClassBasedCriteriaAsArray();
        this.sampleBasedCriteria = config.getSampleBasedCriteriaAsArray();
        this.minimalClassSize = config.getMinimalGroupSize();
        
        // Sanity check: by convention, d-presence must be the first criterion
        // See analyze() and isAnonymous(Entry) for more details
        for (int i = 1; i < classBasedCriteria.length; i++) {
            if (classBasedCriteria[i] instanceof DPresence) {
                throw new RuntimeException("D-Presence must be the first criterion in the array");
            }
        }
        
        // Remember, if (real) d-presence is part of the criteria that must be enforced
        privacyModelContainsDPresence = (classBasedCriteria.length > 0 && (classBasedCriteria[0] instanceof DPresence) && !(classBasedCriteria[0] instanceof Inclusion));
    }
    
    /**
     * Adds a tuple from the buffer
     * @param generalized
     * @param other
     * @param representative
     * @param count
     * @param pcount
     */
    public void addFromBuffer(int[] generalized, int[] other, int representative, int count, int pcount) {

        // Hash
        final int hash = HashTableUtil.hashcode(generalized);

        // Potentially rehash
        synchronizedRehash();

        // Compute bucket
        int index = hash & (hashTableBuckets.length - 1);

        // Add
        final HashGroupifyEntry entry = addInternal(generalized, index, hash, representative, count, pcount);

        // Is a other attribute provided
        if (other != null) {
            if (entry.distributions == null) {
                entry.distributions = new Distribution[other.length];

                // TODO: Improve!
                for (int i = 0; i < entry.distributions.length; i++) {
                    entry.distributions[i] = new Distribution();
                }
            }

            // Only add other value if in research subset
            if (privacyModelDefinesSubset == null || privacyModelDefinesSubset.contains(representative)) {

                // TODO: Improve!
                for (int i = 0; i < entry.distributions.length; i++) {
                    entry.distributions[i].add(other[i]);
                }
            }
        }

        // Release lock for bucket
        @SuppressWarnings("unused")
        int lock = !singleThreaded && lockManager != null ? lockManager.releaseBucket(index) : 1;
    }
    
    /**
     * Adds an entry from another groupify operator
     * @param generalized
     * @param distributions
     * @param representative
     * @param count
     * @param pcount
     */
    public void addFromGroupify(int[] generalized, Distribution[] distributions, int representative, int count, int pcount) {

        // Hash
        final int hash = HashTableUtil.hashcode(generalized);

        // Potentially rehash
        synchronizedRehash();

        // Compute bucket
        int index = hash & (hashTableBuckets.length - 1);

        // Add
        final HashGroupifyEntry entry = addInternal(generalized, index, hash, representative, count, pcount);

        // Is a distribution provided
        if (distributions != null) {
            if (entry.distributions == null) {
                entry.distributions = distributions;
            } else {

                // TODO: Improve!
                for (int i = 0; i < entry.distributions.length; i++) {
                    entry.distributions[i].merge(distributions[i]);
                }
            }
        }
        // Release lock for bucket
        @SuppressWarnings("unused")
        int lock = !singleThreaded && lockManager != null ? lockManager.releaseBucket(index) : 1;
    }
    
    /**
     * Adds a class from a snapshot
     * @param generalized
     * @param elements
     * @param frequencies
     * @param representative
     * @param count
     * @param pcount
     */
    public void addFromSnapshot(int[] generalized, int[][] elements, int[][] frequencies, int representative, int count, int pcount) {

        // Hash
        final int hash = HashTableUtil.hashcode(generalized);

        // Potentially rehash
        synchronizedRehash();

        // Compute bucket
        int index = hash & (hashTableBuckets.length - 1);

        // Add
        final HashGroupifyEntry entry = addInternal(generalized, index, hash, representative, count, pcount);

        // Is a distribution provided
        if (elements != null) {
            if (entry.distributions == null) {

                entry.distributions = new Distribution[elements.length];

                // TODO: Improve!
                for (int i = 0; i < entry.distributions.length; i++) {
                    entry.distributions[i] = new Distribution(elements[i], frequencies[i]);
                }
            } else {

                // TODO: Improve!
                for (int i = 0; i < entry.distributions.length; i++) {
                    entry.distributions[i].merge(elements[i], frequencies[i]);
                }
            }
        }
        // Release lock for bucket
        @SuppressWarnings("unused")
        int lock = !singleThreaded && lockManager != null ? lockManager.releaseBucket(index) : 1;
    }
    
    /**
     * Returns whether all threads are done
     * @return
     */
    public boolean done() {
        return workingThreads.get() == 0;
    }
    
    /**
     * One thread is done
     * @return the number of active threads
     */
    public int end() {
        return workingThreads.decrementAndGet();
    }

    /**
     * Returns the entry for the given tuple
     * @param tuple
     * @return
     */
    public HashGroupifyEntry getEntry(int[] tuple) {
        final int hash = HashTableUtil.hashcode(tuple);
        int index = hash & (hashTableBuckets.length - 1);
        return findEntry(tuple, index, hash);
    }
    
    /**
     * Returns the first entry
     * @return
     */
    public HashGroupifyEntry getFirstEquivalenceClass() {
        return hashTableFirstEntry;
    }
    
    /**
     * Returns the current size in terms of classes
     * @return
     */
    public int getNumberOfEquivalenceClasses() {
        return hashTableElementCount;
    }
    
    /**
     * Returns whether the current state of the dataset fulfills the minimal class-size property
     * @return
     */
    public boolean isMinimalClassSizeFulfilled() {
        return minimalClassSize != Integer.MAX_VALUE && minimalClassSizeFulfilled;
    }
    
    /**
     * Returns whether the current state of the dataset fulfills the privacy model
     * @return
     */
    public boolean isPrivacyModelFulfilled() {
        return privacyModelFulfilled;
    }
    
    /**
     * Microaggregates all according attributes
     * @param data
     * @param start
     * @param num
     * @param functions
     * @param map
     * @param header
     * @param dictionary
     * @return
     */
    public Data performMicroaggregation(int[][] data,
                                        int start,
                                        int num,
                                        DistributionAggregateFunction[] functions,
                                        int[] map,
                                        String[] header,
                                        Dictionary dictionary) {
        
        // Prepare result
        Data result = new Data(new int[data.length][num], header, map, dictionary);

        // TODO: To improve performance, microaggregation and marking of outliers could be performed in one pass
        ObjectIntOpenHashMap<Distribution> cache = new ObjectIntOpenHashMap<Distribution>();
        for (int row = 0; row < data.length; row++) {
            if (privacyModelDefinesSubset == null || privacyModelDefinesSubset.contains(row)) {
                final int[] key = data[row];
                final int hash = HashTableUtil.hashcode(key);
                final int index = hash & (hashTableBuckets.length - 1);
                HashGroupifyEntry m = hashTableBuckets[index];
                while ((m != null) && ((m.hashcode != hash) || !equalsIgnoringOutliers(key, m.key))) {
                    m = m.next;
                }
                if (m == null) { throw new RuntimeException("Invalid state! Groupify the data before microaggregation!"); }
                int dimension = 0;
                result.getArray()[row] = new int[num];
                for (int i = start; i < start + num; i++) {
                    if (!cache.containsKey(m.distributions[i])) {
                        String value = functions[dimension].aggregate(m.distributions[i]);
                        int code = result.getDictionary().register(dimension, value);
                        cache.put(m.distributions[i], code);
                    }
                    result.getArray()[row][dimension] = cache.get(m.distributions[i]);
                    dimension++;
                }
            }
        }
        
        // Finalize
        result.getDictionary().finalizeAll();
        
        // Returns the result
        return result;
    }
    
    /**
     * Marks all outliers in the given (generalized subset of the) input datasets
     * @param data
     */
    public void performSuppression(final int[][] data) {
        
        for (int row = 0; row < data.length; row++) {
            final int[] key = data[row];
            if (privacyModelDefinesSubset == null || privacyModelDefinesSubset.contains(row)) {
                final int hash = HashTableUtil.hashcode(key);
                final int index = hash & (hashTableBuckets.length - 1);
                HashGroupifyEntry m = hashTableBuckets[index];
                while ((m != null) && ((m.hashcode != hash) || !equalsIgnoringOutliers(key, m.key))) {
                    m = m.next;
                }
                if (m == null) {
                    throw new RuntimeException("Invalid state! Groupify the data before marking outliers!");
                }
                if (!m.isNotOutlier) {
                    key[0] |= Data.OUTLIER_MASK;
                }
            } else {
                key[0] |= Data.OUTLIER_MASK;
            }
        }
    }
    
    /**
     * Mark a single-threaded run
     * @param value
     */
    public void setSingleThreaded(boolean value) {
        this.singleThreaded = value;
    }

    /**
     * We are starting the process
     */
    public void start() {
        workingThreads.set(threads);
    }
    
    /**
     * Analyzes the content of the hash table. Checks the privacy criteria against each class.
     * @param transformation
     * @param force
     */
    public void stateAnalyze(Transformation transformation, boolean force) {

        // Iterate over all classes
        boolean dpresent = true;
        int currentNumOutliers = 0;
        HashGroupifyEntry entry = hashTableFirstEntry;
        while (entry != null) {
            
            // Check for anonymity
            int anonymous = isPrivacyModelFulfilled(entry);
            
            // Determine outliers
            if (anonymous != -1) {
                
                // Note: If d-presence exists, it is stored at criteria[0] by convention.
                // If it fails, isAnonymous(entry) thus returns 1.
                // Tuples from the public table that have no matching candidates in the private table
                // and that do not fulfill d-presence cannot be suppressed. In this case, the whole
                // transformation must be considered to not fulfill the privacy criteria.
                if (privacyModelContainsDPresence && entry.count == 0 && anonymous == 1) {
                    dpresent = false;
                }
                
                currentNumOutliers += entry.count;
                
                // Early abort
                if (!force && currentNumOutliers > suppressionLimit) {
                    this.minimalClassSizeFulfilled = false;
                    this.privacyModelFulfilled = false;
                    return;
                }
            }
            
            // We only suppress classes that are contained in the research subset
            entry.isNotOutlier = entry.count != 0 ? (anonymous == -1) : true;
            
            // Next class
            entry = entry.nextOrdered;
        }
        
        int outliers = this.analyzeSampleBasedCriteria(transformation, false);
        currentNumOutliers = outliers != -1 ? outliers : currentNumOutliers;
        this.minimalClassSizeFulfilled = (currentNumOutliers <= suppressionLimit);
        this.privacyModelFulfilled = minimalClassSizeFulfilled && dpresent;
    }
    
    /**
     * Clears all entries
     */
    public void stateClear() {
        if (hashTableElementCount > 0) {
            this.hashTableElementCount = 0;
            this.hashTableFirstEntry = null;
            this.hashTableLastEntry = null;
            HashTableUtil.nullifyArray(hashTableBuckets);
        }
    }
    
    /**
     * This method will reset all flags that indicate that equivalence classes are suppressed.
     */
    public void stateResetSuppression() {
        HashGroupifyEntry entry = hashTableFirstEntry;
        while (entry != null) {
            entry.isNotOutlier = true;
            entry = entry.nextOrdered;
        }
    }
    
    /**
     * Internal adder method.
     *
     * @param generalized the key
     * @param index
     * @param hash the hash
     * @param representative
     * @param count
     * @param pcount
     * @return the hash groupify entry
     */
    private HashGroupifyEntry addInternal(final int[] generalized, final int index, final int hash, final int representative, int count, final int pcount) {

        // If we enforce d-presence and the tuple is not contained in the research subset: set its count to zero
        count = (privacyModelDefinesSubset != null && !privacyModelDefinesSubset.contains(representative)) ? 0 : count;
        
        // Find or create entry
        HashGroupifyEntry entry = findEntry(generalized, index, hash);
        if (entry == null) {
            entry = createEntry(generalized, index, hash, representative);
        }
        
        // Track size: private table for d-presence, overall table, else
        entry.count += count;
        
        // Indirectly check if we enforce d-presence
        if (privacyModelDefinesSubset != null) {
            
            // Increase size of tuples from public table
            entry.pcount += pcount;
            
            // This is a tuple from the research subset, but the class is not represented by a tuple from the subset.
            // Or this is a tuple from the subset with a representative that is smaller than the current representative of the tuple (which is also from the subset)
            // Reset its representative, which is necessary for rollup / history, because
            // otherwise subset.contains(tupleID) could potentially return false.
            // Moreover, we *must* always represent classes by its minimal representative to ensure that roll-ups and snapshots can be
            // utilized correctly. This is guaranteed, if there is no research subset, and needs to be enforced explicitly, if there is one.
            //
            // Consider the following scenario
            //
            // 1. Tuple from G1 (Not in subset)
            // 2. Tuple from G2 (Not in subset)
            // 3. Tuple from G2 <-Representative
            // 4. Tuple from G1 <-Representative
            //
            // We assume that G1 and G2 collapse in the next grouping operation.
            //
            // If we iterate over the whole dataset and always choose the last element, the group is represented by tuple 4
            // If we iterate over a snapshot, G1 will be iterated over before G2 (although it has the larger representative), resetting the representative index 3
            //
            // To prevent this, we always choose the smallest index:
            entry.representative = (count > 0 && (entry.count == count || entry.representative < representative)) ? representative : entry.representative;
        }
        
        // Compute current total number of outliers, if k-anonymity is contained in the set of criteria
        // TODO: Replace with conditional moves
        if (entry.count >= minimalClassSize) {
            if (!entry.isNotOutlier) {
                entry.isNotOutlier = true;
            }
        }
        // Return
        return entry;
    }
    
    /**
     * Analyze sample-based criteria
     * @param transformation
     * @param earlyAbort May we perform an early abort, if we reach the threshold
     * @return the number of suppressed tuples
     */
    private int analyzeSampleBasedCriteria(Transformation transformation, boolean earlyAbort) {
        
        // Nothing to do
        if (this.sampleBasedCriteria.length == 0) {
            return -1;
        }
        
        // Build a distribution
        HashGroupifyDistribution distribution = new HashGroupifyDistribution(heuristicForSampleBasedCriteria ? null : utilityMeasure,
                                                                             transformation,
                                                                             this.hashTableFirstEntry);
        
        // For each criterion
        int suppressed = 0;
        for (SampleBasedCriterion criterion : this.sampleBasedCriteria) {
            
            // Enforce
            criterion.enforce(distribution, earlyAbort ? this.suppressionLimit : Integer.MAX_VALUE);
            
            // Early abort
            suppressed = distribution.getNumOfSuppressedTuples();
            if (earlyAbort && suppressed > suppressionLimit) {
                return suppressed;
            }
        }
        
        // Return
        return suppressed;
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
        entry.next = hashTableBuckets[index];
        entry.representative = line;
        hashTableBuckets[index] = entry;

        // Acquire
        @SuppressWarnings("unused")
        int lock = !singleThreaded && lockManager != null ? lockManager.lockCreate() : 1;

        hashTableElementCount++;
        if (hashTableFirstEntry == null) {
            hashTableFirstEntry = entry;
            hashTableLastEntry = entry;
        } else {
            hashTableLastEntry.nextOrdered = entry;
            hashTableLastEntry = entry;
        }

        // Release
        lock = !singleThreaded && lockManager != null ? lockManager.releaseCreate() : 1;
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
            if (a[i] != (a2[i] & Data.REMOVE_OUTLIER_MASK)) {
                return false;
            }
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
    private HashGroupifyEntry findEntry(final int[] key, final int index, final int keyHash) {
        

        // Acquire lock for bucket
        @SuppressWarnings("unused")
        int lock = !singleThreaded && lockManager != null ? lockManager.lockBucket(index) : 1;
        HashGroupifyEntry m = hashTableBuckets[index];
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
    private int isPrivacyModelFulfilled(HashGroupifyEntry entry) {
        
        // Check minimal group size
        if (minimalClassSize != Integer.MAX_VALUE && entry.count < minimalClassSize) {
            return 0;
        }
        
        // Check other criteria
        // Note: The d-presence criterion must be checked first to ensure correct handling of d-presence with tuple suppression.
        // This is currently ensured by convention. See ARXConfiguration.getCriteriaAsArray();
        for (int i = 0; i < classBasedCriteria.length; i++) {
            if (!classBasedCriteria[i].isAnonymous(entry)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {

        // Wait for all other threads to wait for rehashing
        if (!singleThreaded && lockManager != null) {
            while (waitingThreads.get() != workingThreads.get()) {
                // Spin
            }
        }

        final int length = HashTableUtil.calculateCapacity((hashTableBuckets.length == 0 ? 1 : hashTableBuckets.length << 1));
        final HashGroupifyEntry[] newData = new HashGroupifyEntry[length];
        HashGroupifyEntry entry = hashTableFirstEntry;
        while (entry != null) {
            final int index = entry.hashcode & (length - 1);
            entry.next = newData[index];
            newData[index] = entry;
            entry = entry.nextOrdered;
        }

        // Update lock manager
        @SuppressWarnings("unused")
        int lock = !singleThreaded && lockManager != null ? lockManager.resize(newData.length) : 1;

        // Update hash table
        hashTableBuckets = newData;
        hashTableThreshold = HashTableUtil.calculateThreshold(hashTableBuckets.length, hashTableLoadFactor);
    }

    /**
     * This method tries to rehash
     * @param bucketLock 
     */
    private void synchronizedRehash() {

        // Rehash, if still necessary to rehash
        if (hashTableElementCount > hashTableThreshold) {
    
            // Increment number of waiting threads
            @SuppressWarnings("unused")
            int lock = !singleThreaded && lockManager != null ? waitingThreads.incrementAndGet() : 1;
    
            // Acquire lock for rehashing
            lock = !singleThreaded && lockManager != null ? lockManager.lockRehash() : 1;
    
            // Rehash, if still necessary to rehash
            if (hashTableElementCount > hashTableThreshold) {
    
                // Rehash
                rehash();
            }
            // Release lock for rehashing
            lock = !singleThreaded && lockManager != null ? lockManager.releaseRehash() : 1;
    
            // Decrement number of waiting threads
            lock = !singleThreaded && lockManager != null ? waitingThreads.decrementAndGet() : 1;
        }
    }
}
