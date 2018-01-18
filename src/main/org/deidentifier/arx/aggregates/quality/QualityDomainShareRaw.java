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

package org.deidentifier.arx.aggregates.quality;

import java.util.HashMap;
import java.util.Map;

/**
 * Raw domain-share (unencoded).
 * 
 * @author Fabian Prasser
 */
public class QualityDomainShareRaw implements QualityDomainShare {

    /** Domain shares */
    private final Map<String, Double> shares;

    /** Domain size */
    private final double              domainSize;

    /** Suppressed */
    private final String              suppressedValue;

    /**
     * Creates a new instance
     * @param hierarchies
     * @param suppressedValue
     */
    public QualityDomainShareRaw(String[][] hierarchy, String suppressedValue) {
        this.shares = getShares(hierarchy);
        this.domainSize = hierarchy.length;
        this.suppressedValue = suppressedValue;
    }
    
    @Override
    public double getDomainSize() {
        return this.domainSize;
    }
    
    @Override
    public double getShare(String value, int level) {
        Double loss = this.shares.get(value);
        return loss != null ? loss : (value.equals(suppressedValue) ? 1d : 1d / this.getDomainSize());
    }

    /**
     * Build loss
     * @param hierarchy
     * @return
     */
    private Map<String, Double> getShares(String[][] hierarchy) {
        
        Map<String, Double> shares = new HashMap<>();
        
        // Level 0
        double baseFraction = 1d / (double) hierarchy.length;
        for (int row = 0; row < hierarchy.length; row++) {
            String value = hierarchy[row][0];
            shares.put(value, baseFraction);
        }
        
        // For each generalization level
        for (int level = 1; level < hierarchy[0].length; level++) {

            // Calculate shares on this level
            Map<String, Double> temp = new HashMap<>();
            for (int row = 0; row < hierarchy.length; row++) {
                String value = hierarchy[row][level];
                Double count = temp.get(value);
                count = count == null ? 1 : count + 1;
                temp.put(value, count);
            }
            
            // Normalize
            for (String key : temp.keySet()) {
                temp.put(key, temp.get(key) / (double) hierarchy.length);
            }
            
            // Merge
            shares.putAll(temp);
        }
        
        // Return
        return shares;
    }
}
