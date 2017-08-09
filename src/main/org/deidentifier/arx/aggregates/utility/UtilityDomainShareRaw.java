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

/**
 * Raw domain-share (unencoded).
 * 
 * @author Fabian Prasser
 */
class UtilityDomainShareRaw implements UtilityDomainShare {

    /** Domain shares */
    private final Map<String, Double> shares;

    /** Domain size */
    private final double              domainSize;

    /** Maximum levels */
    private final int                 maxlevels;

    /** Suppressed */
    private final String              suppressedValue;

    /**
     * Creates a new instance
     * @param hierarchies
     * @param suppressedValue
     */
    UtilityDomainShareRaw(String[][] hierarchy, String suppressedValue) {
        this.shares = getLoss(hierarchy);
        this.domainSize = hierarchy.length;
        this.maxlevels = hierarchy[0].length - 1;
        this.suppressedValue = suppressedValue;
    }
    
    @Override
    public double getDomainSize() {
        return this.domainSize;
    }
    
    @Override
    public double getShare(String value, int level) {
        for (; level <= maxlevels; level++) {
            Double loss = this.shares.get(value + level);
            if (loss != null) { return loss; }
        }
        return value.equals(suppressedValue) ? 1d : 1d / this.getDomainSize();
    }

    /**
     * Build loss
     * @param hierarchy
     * @return
     */
    private Map<String, Double> getLoss(String[][] hierarchy) {
        
        Map<String, Double> loss = new HashMap<String, Double>();
        
        // Prepare map:
        // Level -> Value on level + 1 -> Count of values on level that are generalized to this value
        Map<Integer, Map<String, Integer>> map = new HashMap<Integer, Map<String, Integer>>();
        for (int level = 0; level < hierarchy[0].length - 1; level++) {
            for (int row = 0; row < hierarchy.length; row++) {
                
                // Obtain map per level
                Map<String, Integer> levelMap = map.get(level);
                if (levelMap == null) {
                    levelMap = new HashMap<String, Integer>();
                    map.put(level, levelMap);
                }
                
                // Count
                String value = hierarchy[row][level + 1];
                value += (level + 1);
                Integer count = levelMap.get(value);
                count = count == null ? 1 : count + 1;
                levelMap.put(value, count);
            }
        }
        
        // Level 0
        for (int row = 0; row < hierarchy.length; row++) {
            String value = hierarchy[row][0];
            value += 0;
            if (!loss.containsKey(value)) {
                loss.put(value, 1d / (double) hierarchy.length);
            }
        }
        
        // Level > 1
        for (int col = 1; col < hierarchy[0].length; col++) {
            for (int row = 0; row < hierarchy.length; row++) {
                String value = hierarchy[row][col];
                value += col;
                if (!loss.containsKey(value)) {
                    double count = map.get(col - 1).get(value);
                    loss.put(value, (double) count / (double) hierarchy.length);
                }
            }
        }
        
        return loss;
    }
}
