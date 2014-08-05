/*
 * ARX: Powerful Data Anonymization
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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The t-closeness criterion with equal-distance EMD
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class EqualDistanceTCloseness extends TCloseness {

    private static final long serialVersionUID = -1383357036299011323L;

    /** The original distribution*/
    private double[]          distribution;
    
    /**
     * Creates a new instance of the t-closeness criterion with equal earth-movers-distance as proposed in:
     * Li N, Li T, Venkatasubramanian S. 
     * t-Closeness: Privacy beyond k-anonymity and l-diversity. 
     * 23rd International Conference on Data Engineering. 2007:106-115. 
     * @param t
     */
    public EqualDistanceTCloseness(String attribute, double t) {
        super(attribute, t);
    }

    @Override
    public void initialize(DataManager manager) {
        super.initialize(manager);
        distribution = manager.getDistribution(attribute);
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        // Calculate EMD with equal distance
        int[] buckets = entry.distributions[index].getBuckets();
        double count = entry.count;
        
        /* 
         * P = Set of ids of values in local frequency set
         * Q = Set of ids of values in global dataset
         * 
         * According to Li et al., EMD with equal ground distance is:
         * D[P, Q] = 1/2 SUM_{i \in Q} (|p_i - q_i|)
         * 
         * This can be reformulated as:
         * D[P, Q] = 1/2 * (SUM_{i \in Q\P} q_i + SUM_{i \in P}(|p_i - q_i|))
         * 
         * Additionally,
         * SUM_{i \in Q\P} q_i = 1 - SUM_{i \in P} q_i = 1 + SUM_{i \in P} - q_i
         * 
         * As a result, we implement the metric as follows
         * 
         * D[P, Q] = 1/2 * ( 1 + SUM_{i \in P} (|p_i - q_i| - q_i))
         */
        
        double val = 1.0d;
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                double frequency = distribution[buckets[i]];
                val += Math.abs((frequency - ((double) buckets[i + 1] / count))) - frequency;
            }
        }
        val /= 2;

        // check
        return val <= t;
    }

	@Override
	public String toString() {
		return t+"-closeness with equal distance for attribute '"+attribute+"'";
	}
}
