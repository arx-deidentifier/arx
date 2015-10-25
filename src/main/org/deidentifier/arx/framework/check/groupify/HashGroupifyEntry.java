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

import org.deidentifier.arx.framework.check.distribution.Distribution;

/**
 * Implements an equivalence class.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class HashGroupifyEntry {
    
    /** The number of elements in this class. Excluding elements from the public table. */
    private int               count          = 0;
                                             
    /** The number of elements in this class. Including elements from the public table */
    private int               pcount         = 0;
                                             
    /** The hashcode of this class. */
    private final int         hashcode;
                              
    /** The key of this class. */
    private final int[]       key;
                              
    /** The next element in this bucket. */
    private HashGroupifyEntry next           = null;
                                             
    /** The overall next element in original order. */
    private HashGroupifyEntry nextOrdered    = null;
                                             
    /** The index of the representative row. */
    private int               representative = -1;
                                             
    /** Is this class not an outlier?. */
    private boolean           isNotOutlier   = false;
                                             
    /** Frequency set for other attributes *. */
    private Distribution[]    distributions;
                              
    /**
     * Creates a new entry.
     * 
     * @param key
     * @param hash
     */
    public HashGroupifyEntry(final int[] key, final int hashcode) {
        this.hashcode = hashcode;
        this.key = key;
    }
    
    /**
     * Adds the given count.
     * @param count
     */
    public void addCount(int count) {
        this.count += count;
    }
    
    /**
     * Adds the given population count.
     * @param pcount
     */
    public void addPcount(int pcount) {
        this.pcount += pcount;
    }
    
    /**
     * Returns the count.
     * @return
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Returns the distributions.
     * @return
     */
    public Distribution[] getDistributions() {
        return distributions;
    }
    
    /**
     * Returns the hash code.
     * @return
     */
    public int getHashcode() {
        return hashcode;
    }
    
    /**
     * Returns the key.
     * @return
     */
    public int[] getKey() {
        return key;
    }
    
    /**
     * Returns the next entry for this bucket.
     * @return
     */
    public HashGroupifyEntry getNext() {
        return next;
    }
    
    /**
     * Returns the overall next entry in original order.
     * @return
     */
    public HashGroupifyEntry getNextOrdered() {
        return nextOrdered;
    }
    
    /**
     * Returns the population count.
     * @return
     */
    public int getPcount() {
        return pcount;
    }
    
    /**
     * Returns the representative.
     * @return
     */
    public int getRepresentative() {
        return representative;
    }
    
    /**
     * Returns true if the entry is not an outlier.
     * @return
     */
    public boolean isNotOutlier() {
        return isNotOutlier;
    }
    
    /**
     * Sets the distributions.
     * @param distributions
     */
    public void setDistributions(Distribution[] distributions) {
        this.distributions = distributions;
    }
    
    /**
     * Sets the next entry in the bucket.
     * @param next
     */
    public void setNext(HashGroupifyEntry next) {
        this.next = next;
    }
    
    /**
     * Sets the next overall entry in order.
     * @param nextOrdered
     */
    public void setNextOrdered(HashGroupifyEntry nextOrdered) {
        this.nextOrdered = nextOrdered;
    }
    
    /**
     * Sets the is not outlier property.
     * @param isNotOutlier
     */
    public void setNotOutlier(boolean isNotOutlier) {
        this.isNotOutlier = isNotOutlier;
    }
    
    /**
     * Sets the representative.
     * @param representative
     */
    public void setRepresentative(int representative) {
        this.representative = representative;
    }
}
