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

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

import com.carrotsearch.hppc.IntDoubleOpenHashMap;

/**
 * The t-closeness criterion for ordered attributes.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class OrderedDistanceTCloseness extends TCloseness {

    /** SVUID */
    private static final long serialVersionUID = -2395544663063577862L;

    /** The original distribution. */
    private double[]          distribution;

    /** The order of the elements. */
    private int[]             order;
    
    /**
     * Creates a new instance of the t-closeness criterion for ordered attributes as proposed in:
     * Li N, Li T, Venkatasubramanian S.
     * t-Closeness: Privacy beyond k-anonymity and l-diversity.
     * 23rd International Conference on Data Engineering. 2007:106-115.
     *
     * @param attribute
     * @param t
     */
    public OrderedDistanceTCloseness(String attribute, double t) {
        super(attribute, t);
    }

    @Override
    public OrderedDistanceTCloseness clone() {
        return new OrderedDistanceTCloseness(this.getAttribute(), this.getT());
    }
    
    @Override
    public void initialize(DataManager manager) {
        super.initialize(manager);
        this.distribution = manager.getDistribution(attribute);
        this.order = manager.getOrder(attribute);
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        // Init
        int[] buckets = entry.distributions[index].getBuckets();
        double count = entry.count;
        
        // Prepare
        IntDoubleOpenHashMap map = new IntDoubleOpenHashMap(buckets.length/2);
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                int value = buckets[i];
                double frequency = ((double) buckets[i + 1] / count);
                map.put(value, frequency);
            }
        }
        
        // Calculate
        double distance = 0d;
        double sum_i = 0d;
        for (int i=0; i<order.length; i++) {
            int value = order[i];
            sum_i += (map.getOrDefault(value, 0d) - distribution[value]);
            distance += Math.abs(sum_i);
        }
        distance /= (order.length - 1d);
        
        // Check
        return distance <= t;
    }

	@Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
	public String toString() {
		return t+"-closeness with ordered distance for attribute '"+attribute+"'";
	}
}
