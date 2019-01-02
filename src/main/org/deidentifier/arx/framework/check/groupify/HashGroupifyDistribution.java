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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution.PrivacyCondition.State;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLossWithBound;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskModelHistogram;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * A distribution of equivalence classes
 * @author Fabian Prasser
 */
public class HashGroupifyDistribution {

    /**
     * A condition that may or may not be fulfilled for the distribution
     * @author Fabian Prasser
     */
    public static interface PrivacyCondition {
        
        /**
         * The current state of the search condition
         * @author Fabian Prasser
         */
        public static enum State {
            FULFILLED,
            NOT_FULFILLED,
            ABORT
        }
        
        /**
         * Evaluates the condition on the given distribution
         * @param distribution
         * @return
         */
        public State isFulfilled(HashGroupifyDistribution distribution);
    }

    /** The backing map */
    private IntIntOpenHashMap   distribution = new IntIntOpenHashMap();
    /** The number of suppressed tuples */
    private int                 numSuppressed   = 0;
    /** Entries that can be suppressed */
    private HashGroupifyEntry[] entries;
    /** Number of tuples in the data set */
    private double              numRecords    = 0;
    /** Number of classes in the data set */
    private double              numClasses   = 0;

    /**
     * Creates a new instance
     * 
     * @param metric, null if ordering should not be applied
     * @param transformation
     * @param entry
     */
    HashGroupifyDistribution(final Metric<?> metric,
                             final Transformation transformation,
                             HashGroupifyEntry entry) {
        
        // Initialize
        List<HashGroupifyEntry> list = new ArrayList<HashGroupifyEntry>();
        while(entry != null) {
            if (entry.isNotOutlier && entry.count > 0) {
                addToDistribution(entry.count);
                list.add(entry);
            } else {
                this.numSuppressed += entry.count;
            }
            entry = entry.nextOrdered;
        }
        
        Comparator<HashGroupifyEntry> comparator;
        
        // Blacklist metrics for which information loss of individual entries
        // is equal to the size of the class
        if (metric == null || !metric.isClassBasedInformationLossAvailable()) {
            
            // Create comparator
            comparator = new Comparator<HashGroupifyEntry>(){
                public int compare(HashGroupifyEntry o1, HashGroupifyEntry o2) {
                    int cmp = Integer.compare(o1.count, o2.count);
                    return cmp != 0 ? cmp : Integer.compare(o1.representative, o2.representative);
                }
            };
        } else {
            
            // Cache for information loss
            final Map<HashGroupifyEntry, InformationLossWithBound<?>> cache = 
                    new HashMap<HashGroupifyEntry, InformationLossWithBound<?>>();
            
            // Create comparator
            comparator = new Comparator<HashGroupifyEntry>(){
                public int compare(HashGroupifyEntry o1, HashGroupifyEntry o2) {
                    
                    int cmp = Integer.compare(o1.count, o2.count);
                    if (cmp != 0) {
                        return cmp;
                    }
                    
                    InformationLossWithBound<?> loss1 = cache.get(o1);
                    InformationLossWithBound<?> loss2 = cache.get(o2);
                    if (loss1 == null) {
                        loss1 = metric.getInformationLoss(transformation, o1); 
                        cache.put(o1, loss1);
                    }
                    
                    if (loss2 == null) {
                        loss2 = metric.getInformationLoss(transformation, o2); 
                        cache.put(o2, loss2);
                    }
                    
                    cmp = loss1.getInformationLoss().compareTo(loss2.getInformationLoss());
                    return cmp != 0 ? cmp : Integer.compare(o1.representative, o2.representative);
                }
            };
        }
            
        // Sort & store suppressible entries
        Collections.sort(list, comparator);
        this.entries = list.toArray(new HashGroupifyEntry[list.size()]);
    }
    
    /**
     * Returns the average class size
     * @return
     */
    public double getAverageClassSize() {
        return numRecords / numClasses;
    }

    /**
     * Returns the fraction of tuples that are in classes of the given size
     * @param size
     * @return
     */
    public double getFractionOfRecordsInClassesOfSize(int size) {
        return (double)distribution.get(size) * (double)size / numRecords;
    }

    /**
     * Returns a set of classes as an input for the risk model
     */
    public RiskModelHistogram getHistogram() {
        return new RiskModelHistogram(this.distribution);
    }

    /**
     * Returns the number of records
     * @return
     */
    public int getNumRecords() {
        return (int)this.numRecords;
    }
    
    /**
     * Returns the number of suppressed records
     * @return
     */
    public int getNumSuppressedRecords() {
        return this.numSuppressed;
    }
    

    /**
     * Suppresses entries until the condition is fulfilled
     * @param condition
     * @return the number of tuples that have been suppressed
     */
    public int suppressWhileNotFulfilledBinary(PrivacyCondition condition) {
        
        // Nothing to suppress
        if (entries.length == 0) {
            return this.numSuppressed;
        }

        // Start parameters
        int low = 0;
        int high = entries.length - 1;
        int mid = (low + high) / 2;
        int initiallySuppressed = this.numSuppressed;
        State state = State.ABORT;

        // Initially suppress from low to mid
        for (int i=low; i <= mid; i++) {
            suppressEntry(entries[i]);
        }

        // While not done
        while (low <= high) {

            // Binary search
            state = condition.isFulfilled(this);
            if (state == State.ABORT) {
                break;
            } else if (state == State.FULFILLED) {
                high = mid - 1;
                mid = (low + high) / 2;
                
                // Clear suppression from mid
                for (int i = mid + 1; i < entries.length && !entries[i].isNotOutlier; i++) {
                    unSuppressEntry(entries[i]);
                }
                
            } else { // state == State.NOT_FULFILLED
                
                low = mid + 1;
                mid = (low + high) / 2;
                
                // Suppress from low to mid
                for (int i=low; i <= mid; i++) {
                    suppressEntry(entries[i]);
                }
            }
        }

        // Finally check mid+1
        if (state != State.ABORT) {
            state = condition.isFulfilled(this);
            if (state == State.NOT_FULFILLED && mid + 1 < entries.length && entries[mid + 1].isNotOutlier) {
                suppressEntry(entries[mid + 1]);
            }
        }

        return this.numSuppressed - initiallySuppressed;
    }

    /**
     * Suppresses entries until the condition is fulfilled
     * @param condition
     * @return the number of tuples that have been suppressed
     */
    public int suppressWhileNotFulfilledLinear(PrivacyCondition condition) {

        int initiallySuppressed = this.numSuppressed;

        for (int i=0; i<entries.length; i++) {
            State state = condition.isFulfilled(this);
            if (state == State.NOT_FULFILLED) {
                suppressEntry(entries[i]);
            } else { 
                // State.FULFILLED || State.ABORT
                break;
            }
        }
        
        return this.numSuppressed - initiallySuppressed;
    }

    /**
     * Adds an entry
     * @param size
     */
    private void addToDistribution(int size) {
        this.numClasses++;
        this.numRecords += size;
        this.distribution.putOrAdd(size, 1, 1);   
    }

    /**
     * Removes an entry
     * @param size
     */
    private void removeFromDistribution(int size) {
        this.numClasses--;
        this.numRecords -= size;
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
        this.numSuppressed += entry.count;
        // No need to adjust "numRecords", because this is done in "removeFromDistribution"
    }

    /**
     * Unsuppresses the given entry
     * @param entry
     */
    private void unSuppressEntry(HashGroupifyEntry entry) {
        
        if (this.numSuppressed == 0 || entry.isNotOutlier) {
            throw new IllegalStateException("Internal error. There are no suppressed entries.");
        }
        entry.isNotOutlier = true;
        this.numSuppressed -= entry.count;
        addToDistribution(entry.count);
        // No need to adjust "numRecords", because this is done in "addToDistribution"
    }
}
