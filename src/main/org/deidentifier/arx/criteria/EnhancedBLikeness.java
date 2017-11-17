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

/**
 * Enhanced-beta-Likeness:<br>
 * <br>
 * Jianneng Cao, Panagiotis Karras:<br>
 * Publishing Microdata with a Robust Privacy Guarantee<br>
 * VLDB 2012.
 *
 * @author Fabian Prasser
 */
public class EnhancedBLikeness extends ExplicitPrivacyCriterion {

    /** SVUID */
    private static final long serialVersionUID = 5319052409590347904L;

    /** Parameter */
    private final double      b;

    /** The original distribution. */
    private double[]          distribution;
    
    /** Reliable properties of the original distribution*/
    private IntervalDouble[]  reliableDistribution;
    
    /**
     * Creates a new instance
     *
     * @param attribute
     * @param beta
     */
    public EnhancedBLikeness(String attribute, double beta) {
        super(attribute, false, true);
        if (beta <= 0) {
            throw new IllegalArgumentException("Beta (" + beta + ") must be > 0");
        }
        this.b = beta;
    }

    @Override
    public EnhancedBLikeness clone() {
        return new EnhancedBLikeness(this.getAttribute(), this.getB());
    }

    /**
     * Returns the parameter beta.
     *
     * @return
     */
    public double getB(){
        return this.b;
    }

	@Override
    public int getRequirements(){
        // Requires a distribution
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }

    @Override
    public void initialize(DataManager manager, ARXConfiguration config) {
        super.initialize(manager, config);
        this.distribution = manager.getDistribution(attribute);
        try {
            this.reliableDistribution = manager.getReliableDistribution(attribute);
        } catch (ReliabilityException e) {
            this.reliableDistribution = null;
        }
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {

        // For table t
        // For each class c
        //     For each sensitive value s
        //         (freq(s, c) - freq(s, t)) / freq(s, t) <= min(beta, - ln(freq(s, t))) 
        
        // Init
        int[] buckets = entry.distributions[index].getBuckets();
        double count = entry.count;
        
        // For each value in c
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                double frequencyInT = distribution[buckets[i]];
                double frequencyInC = (double) buckets[i + 1] / count;
                double value = (frequencyInC - frequencyInT) / frequencyInT;
                if (value > Math.min(b, - Math.log(frequencyInT))) {
                    return false;
                }
            }
        }

        // check
        return true;
    }
    
    @Override
    public boolean isReliablyAnonymous(Transformation node, HashGroupifyEntry entry) {

        // For table t
        // For each class c
        //     For each sensitive value s
        //         (freq(s, c) - freq(s, t)) / freq(s, t) <= min(beta, - ln(freq(s, t)))
        
        try {
            // Check
            if (reliableDistribution == null) {
                return isAnonymous(node, entry);
            }
            
            // Init
            IntervalArithmeticDouble ia = new IntervalArithmeticDouble();
            int[] buckets = entry.distributions[index].getBuckets();
            IntervalDouble count = ia.createInterval(entry.count);
            IntervalDouble b = ia.createInterval(this.b);
            
            // For each value in c
            for (int i = 0; i < buckets.length; i += 2) {
                if (buckets[i] != -1) { // bucket not empty
                    IntervalDouble frequencyInT = reliableDistribution[buckets[i]];
                    IntervalDouble frequencyInC = ia.div(ia.createInterval(buckets[i + 1]), count);
                    IntervalDouble value = ia.div(ia.sub(frequencyInC, frequencyInT), frequencyInT);
                    if (ia.greaterThan(value, ia.min(b, ia.mult(ia.MINUS_ONE, ia.log(frequencyInT))))) {
                        return false;
                    }
                }
            }

            // check
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
        ElementData result = new ElementData("Enhanced likeness");
        result.addProperty("Attribute", attribute);
        result.addProperty("Reliable", isReliableAnonymizationSupported());
        result.addProperty("Threshold (beta)", b);
        return result;
    }

    @Override
	public String toString() {
        return "enhanced-" + b + "-likeness for attribute '" + attribute + "'";
	}
}
