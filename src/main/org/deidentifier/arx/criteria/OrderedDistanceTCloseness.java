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
import org.deidentifier.arx.common.FastIntDoubleMap;
import org.deidentifier.arx.common.FastIntObjectMap;
import org.deidentifier.arx.exceptions.ReliabilityException;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;
import org.deidentifier.arx.reliability.IntervalDouble;

/**
 * The t-closeness criterion for ordered attributes.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Philip Offtermatt
 * @author Raffael Bild
 */
public class OrderedDistanceTCloseness extends TCloseness {

    /** SVUID */
    private static final long serialVersionUID = -2395544663063577862L;

    /** The original distribution. */
    private double[]          distribution;
    
    /** Reliable original distribution */
    private IntervalDouble[]  distributionReliable;

    /** The order of the elements. */
    private int[]             order;
    
    /** The order of the elements. */
    private int[]             orderNumber;
    
    /** Partial distances of the original distribution. */
    private double[]          baseDistances;

    /** Reliable partial distances of the original distribution. */
    private IntervalDouble[]  baseDistancesReliable;

    /** Partial sums of the original distribution. */
    private double[]          baseSums;

    /** Reliable partial sums of the original distribution. */
    private IntervalDouble[]  baseSumsReliable;

    /** Minimal order number that must be present */
    private int               minOrder;
    
    /** Reliable minimal order number that must be present */
    private int               minOrderReliable;
    
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
        
        // Super
        super.initialize(manager, config);
        
        // Obtain data
        this.distribution = manager.getDistribution(attribute);
        this.order = manager.getOrder(attribute);
        this.orderNumber = getOrderNumbers(order);
        this.baseDistances = new double[order.length];
        this.baseSums = new double[order.length];
        try {
            this.distributionReliable = manager.getReliableDistribution(attribute);
        } catch (ReliabilityException e) {
            // Indicate that reliable anonymization is not supported.
            this.distributionReliable = null;
        }
        
        // Prepare
        double threshold = t * (order.length - 1d);
        double distance = 0d;
        double sum_i = 0d;

        // Find minimal order number that must be present and initialize base distances and sums
        this.minOrder = this.order.length;
        for (int orderNum = 0; orderNum < this.order.length; orderNum++) {
            
            // Compute summands and distances
            int value = this.order[orderNum];
            sum_i -= this.distribution[value];
            distance += Math.abs(sum_i);
            this.baseDistances[orderNum] = distance;
            this.baseSums[orderNum] = sum_i;
            
            // Check
            if (distance > threshold) {
                this.minOrder = orderNum;
                break;
            }
        }
        
        if (this.distributionReliable != null) {
            
            try {
                
                // Prepare
                IntervalArithmeticDouble ia = new IntervalArithmeticDouble();
                
                IntervalDouble thresholdRel = ia.mult(ia.createInterval(t), ia.createInterval(order.length - 1));
                IntervalDouble distanceRel = ia.ZERO;
                IntervalDouble sumRel_i = ia.ZERO;

                this.minOrderReliable = this.order.length;
                this.baseDistancesReliable = new IntervalDouble[order.length];
                this.baseSumsReliable = new IntervalDouble[order.length];
                
                for (int orderNum = 0; orderNum < this.order.length; orderNum++) {
                    
                    // Compute Reliable summands and distances
                    int value = this.order[orderNum];
                    sumRel_i = ia.sub(sumRel_i, this.distributionReliable[value]);
                    distanceRel = ia.add(distanceRel, ia.abs(sumRel_i));
                    this.baseDistancesReliable[orderNum] = distanceRel;
                    this.baseSumsReliable[orderNum] = sumRel_i;
                    
                    // Check
                    if (ia.greaterThan(distanceRel, thresholdRel)) {
                        this.minOrderReliable = orderNum;
                        break;
                    }
                }
            } catch (IntervalArithmeticException e) {
                // Indicate that reliable anonymization is not supported.
                this.distributionReliable = null;
            }
        }
    }
    
    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {

        // Init
        int[] buckets = entry.distributions[index].getBuckets();
        double count = entry.count;
        
        // Prepare
        int currentMinOrder = Integer.MAX_VALUE;
        FastIntDoubleMap map = new FastIntDoubleMap(buckets.length / 2);
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                int value = buckets[i];
                double frequency = ((double) buckets[i + 1] / count);
                map.put(value, frequency);
                currentMinOrder = Math.min(currentMinOrder,  orderNumber[value]);
            }
        }
        
        // Prune
        if (currentMinOrder > this.minOrder) {
            return false;
        }
        
        // Calculate distance
        double threshold = t * (order.length - 1d);
        double distance = currentMinOrder > 0 ? baseDistances[currentMinOrder - 1] : 0d;
        double sum_i = currentMinOrder > 0 ? baseSums[currentMinOrder - 1] : 0d;
        
        // Calculate and check
        for (int i = currentMinOrder; i < order.length; i++) {
            
            // Compute summands and distance
            int value = order[i];
            sum_i += (map.get(value, 0d) - distribution[value]);
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
        
        try {
            // Check
            if (distributionReliable == null) {
                throw new IllegalStateException("Reliable version of the privacy model is being assessed even though reliable anonymization is not supported");
            }
            
            // Init
            IntervalArithmeticDouble ia = new IntervalArithmeticDouble();
            int[] buckets = entry.distributions[index].getBuckets();
            IntervalDouble count = ia.createInterval(entry.count);
            
            // Prepare
            int currentMinOrder = Integer.MAX_VALUE;
            FastIntObjectMap<IntervalDouble> map = new FastIntObjectMap<IntervalDouble>(buckets.length / 2);
            for (int i = 0; i < buckets.length; i += 2) {
                if (buckets[i] != -1) { // bucket not empty
                    int value = buckets[i];
                    IntervalDouble frequency = ia.div(ia.createInterval(buckets[i + 1]), count);
                    map.put(value, frequency);
                    currentMinOrder = Math.min(currentMinOrder,  orderNumber[value]);
                }
            }
            
            // Prune
            if (currentMinOrder > this.minOrderReliable) {
                return false;
            }
            
            // Calculate distance
            IntervalDouble threshold = ia.mult(ia.createInterval(t), ia.createInterval(order.length - 1));
            IntervalDouble distance = currentMinOrder > 0 ? baseDistancesReliable[currentMinOrder - 1] : ia.ZERO;
            IntervalDouble sum_i = currentMinOrder > 0 ? baseSumsReliable[currentMinOrder - 1] : ia.ZERO;
            
            // Calculate and check
            for (int i=currentMinOrder; i<order.length; i++) {
                
                // Compute summands
                int value = order[i];
                sum_i = ia.add(sum_i, ia.sub(map.get(value, ia.ZERO), distributionReliable[value]));
                
                // Compute distance
                if (sum_i.getLowerBound() < 0 && sum_i.getUpperBound() > 0) {
                    // The sign is undecidable and hence the absolute value of sum_i can not be calculated.
                    // However, it is safe to set the lower bound to zero as this can only overestimate the actual distance.
                    distance = ia.add(distance, ia.createInterval(0d, sum_i.getUpperBound()));
                } else {
                    distance = ia.add(distance, ia.abs(sum_i));
                }
                
                // Early abort
                if (ia.greaterThan(distance, threshold)) {
                    return false;
                }
            }

            // Yes
            return true;
            
        // Check for arithmetic issues
        } catch (IntervalArithmeticException | ArithmeticException | IndexOutOfBoundsException e) {
            // Unable to determine reliably if the equivalence class satisfies the privacy model.
            // Return false, assuming conservatively that it does not.
            return false;
        }
    }

    @Override
    public boolean isReliableAnonymizationSupported() {
        return distributionReliable != null;
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

    /**
     * Maps values to order nums
     * @param order
     * @return
     */
    private int[] getOrderNumbers(int[] order) {
        int[] result = new int[order.length];
        for (int orderNum = 0; orderNum < order.length; orderNum++) {
            result[order[orderNum]] = orderNum;
        }
        return result;
    }
}