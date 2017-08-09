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

package org.deidentifier.arx.aggregates.utility;

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.metric.v2.DomainShare;

/**
 * This class implements the KL Divergence metric.<br>
 * Ashwin Machanavajjhala, Daniel Kifer, Johannes Gehrke, Muthuramakrishnan Venkitasubramaniam: <br>
 * L-diversity: Privacy beyond k-anonymity<br>
 * ACM Transactions on Knowledge Discovery from Data (TKDD), Volume 1 Issue 1, March 2007
 * 
 * @author Fabian Prasser
 */
class UtilityModelRowOrientedKLDivergence extends UtilityModelRowOriented {
    
    /** Domain shares */
    private final DomainShare   shares;
    /** Header */
    private final String[]      header;
    /** Distribution */
    private double[]            inputDistribution = null;
    /** Log */
    private static final double LOG2              = Math.log(2);
    
    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     */
    UtilityModelRowOrientedKLDivergence(WrappedBoolean interrupt, DataHandleInternal input) {
        super(interrupt, input);
        this.shares = new DomainShare(hierarchies, header);
        this.inputDistribution = getDistribution(input);
    }
    
    @Override
    double evaluate(DataHandleInternal output) {
        
        double[] outputDistribution = getDistribution(output);
        
        // Init
        double result = 0d;
        
        // For each tuple
        for (int row = 0; row < output.length; row++) {
            
            // Obtain frequency
            double inputFrequency = inputDistribution[row];
            double outputFrequency = outputDistribution[row];
            outputFrequency /= getArea(output[row], generalization);
            
            // Compute KL-Divergence
            result += inputFrequency * log2(inputFrequency / outputFrequency);
            
        }
        
        return result;
    }
    
    /**
     * Returns the area
     * @param tuple
     * @return
     */
    private double getArea(String[] tuple, int[] generalization) {
        double area = 1d;
        for (int i = 0; i < tuple.length; i++) {
            double loss = shares.getShare(header[i], tuple[i], generalization[i]);
            area *= loss * shares.domainSize[i];
        }
        return area;
    }
    
    /**
     * Returns a distribution
     * @param output
     * @return
     */
    private double[] getDistribution(String[][] output) {
        
        // Groupify
        Map<TupleWrapper, Integer> groupify = new HashMap<TupleWrapper, Integer>();
        for (int row = 0; row < output.length; row++) {
            TupleWrapper wrapper = new TupleWrapper(output[row]);
            Integer count = groupify.get(wrapper);
            count = count == null ? 1 : count + 1;
            groupify.put(wrapper, count);
        }
        
        // Build input distribution
        double[] result = new double[output.length];
        for (int row = 0; row < output.length; row++) {
            TupleWrapper wrapper = new TupleWrapper(output[row]);
            double frequency = groupify.get(wrapper).doubleValue() / output.length;
            result[row] = frequency;
        }
        
        // Return
        return result;
    }
    
    /**
     * Log base-2
     * @param d
     * @return
     */
    private double log2(double d) {
        return Math.log(d) / LOG2;
    }
}
