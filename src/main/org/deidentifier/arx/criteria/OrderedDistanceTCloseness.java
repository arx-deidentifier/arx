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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.exceptions.ReliabilityException;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;
import org.deidentifier.arx.reliability.IntervalDouble;

import com.carrotsearch.hppc.IntDoubleOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;

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
    
    /** Reliable properties of the original distribution*/
    private IntervalDouble[]  reliableDistribution;
    
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
    public void initialize(DataManager manager, ARXConfiguration config) {
        super.initialize(manager, config);
        this.distribution = manager.getDistribution(attribute);
        this.order = manager.getOrder(attribute);
        try {
            this.reliableDistribution = manager.getReliableDistribution(attribute);
        } catch (ReliabilityException e) {
            this.reliableDistribution = null;
        }
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {

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
        double threshold = t * (order.length - 1d);
        double distance = 0d;
        double sum_i = 0d;
        
        // Calculate and check
        for (int i=0; i<order.length; i++) {
            
            // Compute summands and distance
            int value = order[i];
            sum_i += (map.getOrDefault(value, 0d) - distribution[value]);
            distance += Math.abs(sum_i);
            
            // Early abort
            if (distance > threshold) {
                return false;
            }
        }
        
        // Yes
        return true;
    }
    
    @Override
    public boolean isReliablyAnonymous(Transformation node, HashGroupifyEntry entry) {
        
        // TODO merge with master

        try {
            // Check
            if (reliableDistribution == null) {
                return isAnonymous(node, entry);
            }
            
            // Init
            IntervalArithmeticDouble ia = new IntervalArithmeticDouble();
            int[] buckets = entry.distributions[index].getBuckets();
            IntervalDouble count = ia.createInterval(entry.count);
            
            // Prepare
            IntObjectOpenHashMap<IntervalDouble> map = new IntObjectOpenHashMap<IntervalDouble>(buckets.length/2);
            for (int i = 0; i < buckets.length; i += 2) {
                if (buckets[i] != -1) { // bucket not empty
                    int value = buckets[i];
                    IntervalDouble frequency = ia.div(ia.createInterval(buckets[i + 1]), count);
                    map.put(value, frequency);
                }
            }
            IntervalDouble threshold = ia.mult(ia.createInterval(t), ia.createInterval(order.length - 1));
            IntervalDouble distance = ia.createInterval(0);
            IntervalDouble sum_i = ia.createInterval(0);
            IntervalDouble zero = ia.createInterval(0);
            
            // Calculate and check
            for (int i=0; i<order.length; i++) {
                
                // Compute summands and distance
                int value = order[i];
                sum_i = ia.add(sum_i, ia.sub(map.getOrDefault(value, zero), reliableDistribution[value]));
                distance = ia.add(distance, ia.abs(sum_i));
                
                // Early abort
                if (!ia.lessThanOrEqual(distance, threshold)) {
                    return false;
                }
            }

            // Yes
            return true;
            
        // Check for arithmetic issues
        } catch (IntervalArithmeticException | ArithmeticException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean isReliableAnonymizationSupported() {
        return reliableDistribution != null;
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
        result.addProperty("Distance", "Ordered");
        return result;
    }

    @Override
    public String toString() {
        return t+"-closeness with ordered distance for attribute '"+attribute+"'";
    }
}
