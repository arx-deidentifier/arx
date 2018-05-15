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

package org.deidentifier.arx.risk;

/**
 * Represents the matcher for the column headers of an attribute. Implements the levenshtein distance for fuzzy detection.
 * @author David Gassmann
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class HIPAAMatcherAttributeName { // NO_UCD

    /** Field */
    private String value;
    /** Field */
    private int    tolerance;
    
    /**
     * Constructor.
     * @param value
     */
    HIPAAMatcherAttributeName(String value, int tolerance) {
        this.value = value.trim().toLowerCase();
        this.tolerance = tolerance;
    }
    
    /**
     * Calculates the Levenstein distance between two strings.
     * @param s0
     * @param s1
     * @return
     */
    private int levenshteinDistance(String s0, String s1) {
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;
        
        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];
        
        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        
        // dynamically computing the array of distances
        
        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;
            
            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;
                
                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;
                
                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }
            
            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        
        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }
    
    /**
     * Returns the value
     * @return
     */
    String getValue() {
        return value;
    }

    /**
     * Returns true if value matches.
     * @param value
     * @return
     */
    boolean matches(String value) {
        value = value.trim().toLowerCase();
        return levenshteinDistance(value, this.value) <= tolerance;
    }
}
