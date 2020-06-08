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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * The t-closeness criterion with equal-distance EMD.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class EqualDistanceTCloseness extends TCloseness {

    /**  SVUID */
    private static final long serialVersionUID = -1383357036299011323L;

    /** The original distribution. */
    private double[]          distribution;
    
    /**
     * Creates a new instance of the t-closeness criterion with equal earth-movers-distance as proposed in:
     * Li N, Li T, Venkatasubramanian S.
     * t-Closeness: Privacy beyond k-anonymity and l-diversity.
     * 23rd International Conference on Data Engineering. 2007:106-115.
     *
     * @param attribute
     * @param t
     */
    public EqualDistanceTCloseness(String attribute, double t) {
        super(attribute, t);
    }

    @Override
    public EqualDistanceTCloseness clone() {
        return new EqualDistanceTCloseness(this.getAttribute(), this.getT());
    }
    
    @Override
    public void initialize(DataManager manager, ARXConfiguration config) {
        super.initialize(manager, config);
        distribution = manager.getDistribution(attribute);
    }

    @Override
    public boolean isAnonymous(Transformation<?> node, HashGroupifyEntry entry) {

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
         * As a result, we implement the distance measure as follows
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
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("t-Closeness");
        result.addProperty("Attribute", attribute);
        result.addProperty("Threshold (t)", this.t);
        result.addProperty("Distance", "Equal");
        return result;
    }

    @Override
	public String toString() {
		return t+"-closeness with equal ground-distance for attribute '"+attribute+"'";
	}
}
