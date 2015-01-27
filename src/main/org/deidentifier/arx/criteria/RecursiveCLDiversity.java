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

package org.deidentifier.arx.criteria;

import java.util.Arrays;

import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * The recursive-(c,l)-diversity criterion.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class RecursiveCLDiversity extends LDiversity{

    /**  TODO */
    private static final long serialVersionUID = -5893481096346270328L;

    /** The parameter c. */
    private final double c;
    
    /**
     * Creates a new instance of the recursive-(c,l)-diversity criterion as proposed in:
     * Machanavajjhala A, Kifer D, Gehrke J.
     * l-diversity: Privacy beyond k-anonymity.
     * Transactions on Knowledge Discovery from Data (TKDD). 2007;1(1):3.
     *
     * @param attribute
     * @param c
     * @param l
     */
    public RecursiveCLDiversity(String attribute, double c, int l){
        super(attribute, l, false);
        this.c = c;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#isAnonymous(org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry)
     */
    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        Distribution d = entry.distributions[index];
        
        // if less than l values are present skip
        if (d.size() < minSize) { return false; }

        // Copy and pack
        int[] buckets = d.getBuckets();
        final int[] frequencyCopy = new int[d.size()];
        int count = 0;
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                frequencyCopy[count++] = buckets[i + 1];
            }
        }

        // Sort
        Arrays.sort(frequencyCopy);
        
        // Compute threshold
        double threshold = 0;
        for (int i = frequencyCopy.length - minSize; i >= 0; i--) { // minSize=(int)l;
            threshold += frequencyCopy[i];
        }
        threshold *= c;

        // Check
        return frequencyCopy[frequencyCopy.length - 1] < threshold;
    }

    /**
     * Returns the parameter c.
     *
     * @return
     */
    public double getC() {
        return c;
    }
    
	/* (non-Javadoc)
	 * @see org.deidentifier.arx.criteria.PrivacyCriterion#toString()
	 */
	@Override
	public String toString() {
		return "recursive-("+c+","+minSize+")-diversity for attribute '"+attribute+"'";
	}
}
