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
 * Basic-beta-Likeness:<br>
 * <br>
 * Jianneng Cao, Panagiotis Karras:<br>
 * Publishing Microdata with a Robust Privacy Guarantee<br>
 * VLDB 2012.
 *
 * @author Fabian Prasser
 */
public class BasicBLikeness extends ExplicitPrivacyCriterion {


    /** SVUID*/
    private static final long serialVersionUID = 2528498679732389575L;

    /** Parameter */
    private final double        b;

    /** The original distribution. */
    private double[]            distribution;
    
    /**
     * Creates a new instance
     *
     * @param attribute
     * @param beta
     */
    public BasicBLikeness(String attribute, double beta) {
        super(attribute, false, true);
        if (beta <= 0) {
            throw new IllegalArgumentException("Beta (" + beta + ") must be > 0");
        }
        this.b = beta;
    }

    @Override
    public BasicBLikeness clone() {
        return new BasicBLikeness(this.getAttribute(), this.getB());
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
    }

    @Override
    public boolean isAnonymous(Transformation<?> node, HashGroupifyEntry entry) {

        // For table t
        // For each class c
        //     For each sensitive value s
        //         (freq(s, c) - freq(s, t)) / freq(s, t) <= beta 
        
        // Init
        int[] buckets = entry.distributions[index].getBuckets();
        double count = entry.count;
        
        // For each value in c
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                double frequencyInT = distribution[buckets[i]];
                double frequencyInC = (double) buckets[i + 1] / count;
                double value = (frequencyInC - frequencyInT) / frequencyInT;
                if (value > b) {
                    return false;
                }
            }
        }

        // check
        return true;
    }
    
    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("Basic likeness");
        result.addProperty("Attribute", attribute);
        result.addProperty("Threshold (beta)", b);
        return result;
    }

    @Override
	public String toString() {
        return "basic-" + b + "-likeness for attribute '" + attribute + "'";
	}
}
