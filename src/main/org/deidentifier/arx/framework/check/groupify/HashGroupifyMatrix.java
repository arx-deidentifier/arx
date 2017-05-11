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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataMatrix;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyMatrix.PrivacyCondition.State;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLossWithBound;
import org.deidentifier.arx.metric.Metric;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * A matrix containing only sample uniques to be used by key-based models
 * @author Fabian Prasser
 */
public class HashGroupifyMatrix {

    /**
     * A condition that may or may not be fulfilled by the data in the matrix
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
         * @param matrix
         * @return
         */
        public State isFulfilled(HashGroupifyMatrix matrix);
    }

    /** The number of suppressed tuples */
    private int                     numSuppressedRecords = 0;
    /** The number of suppressed entries */
    private int                     numSuppressedEntries = 0;
    /** Entries that can be suppressed */
    private List<HashGroupifyEntry> entries;
    /** Matrix of entries that can be suppressed */
    private List<int[]>             matrix;

    /**
     * Creates a new instance
     * 
     * @param metric, null if ordering should not be applied
     * @param transformation
     * @param entry
     */
    HashGroupifyMatrix(final Metric<?> metric,
                       final Transformation transformation,
                       HashGroupifyEntry entry) {
        
        // Initialize
        while(entry != null) {
            if (entry.isNotOutlier && entry.count == 1) {
                matrix.add(entry.key);
                entries.add(entry);
            } else {
                this.numSuppressedRecords += entry.count;
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
            
        // Sort & store suppresseable entries
        final Comparator<HashGroupifyEntry> _comparator = comparator;
        GenericSorting.quickSort(0, entries.size(), new IntComparator() {
            @Override
            public int compare(int arg0, int arg1) {
                
                // Delegate to comparator
                return _comparator.compare(entries.get(arg0), entries.get(arg1));
            }
        }, new Swapper() {
            @Override
            public void swap(int arg0, int arg1) {
                
                // Swap in entries
                HashGroupifyEntry entry0 = entries.get(arg0);
                entries.set(arg0, entries.get(arg1));
                entries.set(arg1, entry0);
                
                // Swap in matrix
                int[] line0 = matrix.get(arg0);
                matrix.set(arg0, matrix.get(arg1));
                matrix.set(arg1, line0);
            }
        });
        this.numSuppressedEntries = 0;
    }
    
    /**
     * Returns a set of classes as an input for the risk model
     */
    public DataMatrix getMatrix() {
        int[][] result = new int[this.entries.size() - this.numSuppressedEntries][];
        int index = 0;
        for (int i=0; i<matrix.size(); i++) {
            if (matrix.get(i) != null) {
                result[index++] = matrix.get(i);
            }
        }
        return new DataMatrix(result);
    }

    /**
     * Returns the number of suppressed records
     * @return
     */
    public int getNumSuppressedRecords() {
        return this.numSuppressedRecords;
    }
    

    /**
     * Suppresses entries until the condition is fulfilled
     * @param condition
     * @return the number of tuples that have been suppressed
     */
    public int suppressWhileNotFulfilledBinary(PrivacyCondition condition) {
        
        // Nothing to suppress
        if (entries.size() == 0) {
            return this.numSuppressedRecords;
        }

        // Start parameters
        int low = 0;
        int high = entries.size() - 1;
        int mid = (low + high) / 2;
        int initiallySuppressed = this.numSuppressedRecords;
        State state = State.ABORT;

        // Initially suppress from low to mid
        for (int i=low; i <= mid; i++) {
            suppressEntry(i);
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
                for (int i = mid + 1; i < entries.size() && !entries.get(i).isNotOutlier; i++) {
                    unSuppressEntry(i);
                }
                
            } else { // state == State.NOT_FULFILLED
                
                low = mid + 1;
                mid = (low + high) / 2;
                
                // Suppress from low to mid
                for (int i=low; i <= mid; i++) {
                    suppressEntry(i);
                }
            }
        }

        // Finally check mid+1
        if (state != State.ABORT) {
            state = condition.isFulfilled(this);
            if (state == State.NOT_FULFILLED && mid + 1 < entries.size() && entries.get(mid + 1).isNotOutlier) {
                suppressEntry(mid + 1);
            }
        }

        return this.numSuppressedRecords - initiallySuppressed;
    }

    /**
     * Suppresses entries until the condition is fulfilled
     * @param condition
     * @return the number of tuples that have been suppressed
     */
    public int suppressWhileNotFulfilledLinear(PrivacyCondition condition) {

        int initiallySuppressed = this.numSuppressedRecords;

        for (int i=0; i<entries.size(); i++) {
            State state = condition.isFulfilled(this);
            if (state == State.NOT_FULFILLED) {
                suppressEntry(i);
            } else { 
                // State.FULFILLED || State.ABORT
                break;
            }
        }
        
        return this.numSuppressedRecords - initiallySuppressed;
    }

    /**
     * Suppresses the given entry
     * @param index
     */
    private void suppressEntry(int index) {
        HashGroupifyEntry entry = entries.get(index);
        entry.isNotOutlier = false;
        this.numSuppressedRecords += entry.count;
        this.matrix.set(index, null);
        this.numSuppressedEntries++;
    }

    /**
     * Unsuppresses the given entry
     * @param index
     */
    private void unSuppressEntry(int index) {
        HashGroupifyEntry entry = entries.get(index);
        if (this.numSuppressedRecords == 0 || entry.isNotOutlier) {
            throw new IllegalStateException("Internal error. There are no suppressed entries.");
        }
        entry.isNotOutlier = true;
        this.numSuppressedRecords -= entry.count;
        this.numSuppressedEntries--;
        this.matrix.set(index, entry.key);
    }
}
