/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * The entropy l-diversity criterion.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class EntropyLDiversity extends LDiversity {

    /**  SVUID */
    private static final long   serialVersionUID = -354688551915634000L;

    /**
     * Creates a new instance of the entropy l-diversity criterion as proposed in:
     * Machanavajjhala A, Kifer D, Gehrke J.
     * l-diversity: Privacy beyond k-anonymity.
     * Transactions on Knowledge Discovery from Data (TKDD). 2007;1(1):3.
     *
     * @param attribute
     * @param l
     */
    public EntropyLDiversity(String attribute, double l){
        super(attribute, l, false, true);
    }
    
    @Override
    public EntropyLDiversity clone() {
        return new EntropyLDiversity(this.getAttribute(), this.getL());
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {

        Distribution d = entry.distributions[index];

        // If less than l values are present skip
        if (d.size() < minSize) { return false; }

        // Sum of the frequencies in distribution (=number of elements)
        final int total = entry.count;
        // Sum must stay smaller than this constant term
        final double C = total * Math.log(total / l);
        double sum1 = 0d;

        final int[] buckets = d.getBuckets();
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                final double frequency = buckets[i + 1];
                sum1 += frequency * Math.log(frequency);
                // If the sum grows over C, we can abort the loop earlier.
                if (C < sum1) { return false; }
            }
        }

        // If we reach this point, the loop did not return false.
        return true;
    }

	@Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
	public String toString() {
		return "entropy-"+l+"-diversity for attribute '"+attribute+"'";
	}
}
