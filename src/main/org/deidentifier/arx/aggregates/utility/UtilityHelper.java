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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Helper class for implementing utility models
 * 
 * @author Fabian Prasser
 */
class UtilityHelper {

    /** Flag */
    private final WrappedBoolean       interrupt;
    /** Value */
    private final String               suppressedValue;

    /**
     * Creates a new instance
     * @param interrupt
     * @param config
     */
    UtilityHelper(WrappedBoolean interrupt, UtilityConfiguration config) {
        this.interrupt = interrupt;
        this.suppressedValue = config.getSuppressedValue();
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (interrupt.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }

    /**
     * Returns domain shares for the handle
     * @param handle
     * @param indices
     * @return
     */
    UtilityDomainShare[] getDomainShares(DataHandleInternal handle, int[] indices) {

        // Prepare
        UtilityDomainShare[] shares = new UtilityDomainShare[indices.length];
        String[][][] hierarchies = getHierarchies(handle, indices);
        
        // Compute domain shares
        for (int i=0; i<shares.length; i++) {
            
            try {
                
                // Extract info
                String[][] hierarchy = hierarchies[i];
                String attribute = handle.getAttributeName(indices[i]);
                HierarchyBuilder<?> builder = handle.getDefinition().getHierarchyBuilder(attribute);
                
                // Create shares for redaction-based hierarchies
                if (builder != null && (builder instanceof HierarchyBuilderRedactionBased) &&
                    ((HierarchyBuilderRedactionBased<?>)builder).isDomainPropertiesAvailable()){
                    shares[i] = new UtilityDomainShareRedaction((HierarchyBuilderRedactionBased<?>)builder);
                    
                // Create fallback-shares for materialized hierarchies
                // TODO: Interval-based hierarchies are currently not compatible
                } else {
                    shares[i] = new UtilityDomainShareRaw(hierarchy, suppressedValue);
                }
                
            } catch (Exception e) {
                // Ignore silently
                shares[i] = null;
            }
        }

        // Return
        return shares;
    }
    
    /**
     * Builds a generalization function mapping input values to the given level of the hierarchy
     * 
     * @param hierarchies
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    Map<String, String>[] getGeneralizationFunctions(String[][][] hierarchies, int index) {
        
        // Prepare
        Map<String, String>[] result = new HashMap[hierarchies.length];

        // For each dimension
        for (int level = 0; level < hierarchies[index][0].length; level++) {
            Map<String, String> map = new HashMap<String, String>();
            for (int row = 0; row < hierarchies[index].length; row++) {
                map.put(hierarchies[index][row][0], hierarchies[index][row][level]);
            }
            result[level] = map;
        }
        
        // Return
        return result;
    }
    
    /**
     * Returns a groupified version of the dataset
     * 
     * @param handle
     * @param indices
     * @return
     */
    Groupify<TupleWrapper> getGroupify(DataHandleInternal handle, int[] indices) {
        
        // Prepare
        int capacity = handle.getNumRows() / 10;
        capacity = capacity > 10 ? capacity : 10;
        Groupify<TupleWrapper> groupify = new Groupify<TupleWrapper>(capacity);
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {
            TupleWrapper tuple = new TupleWrapper(handle, indices, row, false);
            groupify.add(tuple);
            checkInterrupt();
        }
        
        return groupify;
    }

    /**
     * Returns hierarchies, creates trivial hierarchies if no hierarchy is found.
     * Adds an additional level, if there is no root node
     * 
     * @param handle
     * @param indices
     * @return
     */
    String[][][] getHierarchies(DataHandleInternal handle, int[] indices) {
        
        String[][][] hierarchies = new String[indices.length][][];
        
        // Collect hierarchies
        for (int i=0; i<indices.length; i++) {
            
            // Extract and store
            String attribute = handle.getAttributeName(indices[i]);
            String[][] hierarchy = handle.getDefinition().getHierarchy(attribute);
            
            // If not empty
            if (hierarchy != null && hierarchy.length != 0 && hierarchy[0] != null && hierarchy[0].length != 0) {
                
                // Clone
                hierarchies[i] = hierarchy.clone();
                
            } else {
                
                // Create trivial hierarchy
                String[] values = handle.getDistinctValues(indices[i]);
                hierarchies[i] = new String[values.length][2];
                for (int j = 0; j < hierarchies[i].length; j++) {
                    hierarchies[i][j][0] = values[j];
                    hierarchies[i][j][1] = suppressedValue;
                }
            }
        }

        // Fix hierarchy (if suppressed character is not contained in generalization hierarchy)
        for (int j=0; j<indices.length; j++) {
            
            // Access
            String[][] hierarchy = hierarchies[j];
            
            // Check if there is a problem
            Set<String> values = new HashSet<String>();
            for (int i = 0; i < hierarchy.length; i++) {
                String[] levels = hierarchy[i];
                values.add(levels[levels.length - 1]);
            }
            
            // There is a problem
            if (values.size() > 1 || !values.iterator().next().equals(this.suppressedValue)) {
                for(int i = 0; i < hierarchy.length; i++) {
                    hierarchy[i] = Arrays.copyOf(hierarchy[i], hierarchy[i].length + 1);
                    hierarchy[i][hierarchy[i].length - 1] = this.suppressedValue;
                }
            }
            
            // Replace
            hierarchies[j] = hierarchy;
            
            // Check
            checkInterrupt();
        }
        
        // Return
        return hierarchies;
    }

    /**
     * Returns indices of quasi-identifiers
     * 
     * @param handle
     * @return
     */
    int[] getIndicesOfQuasiIdentifiers(DataHandleInternal handle) {
        int[] result = new int[handle.getDefinition().getQuasiIdentifyingAttributes().size()];
        int index = 0;
        for (String qi : handle.getDefinition().getQuasiIdentifyingAttributes()) {
            result[index++] = handle.getColumnIndexOf(qi);
        }
        Arrays.sort(result);
        return result;
    }
    
    /**
     * Returns the inverse generalization function
     * @param hierarchies
     * @param index
     * @return
     */
    Map<String, Integer> getInverseGeneralizationFunction(String[][][] hierarchies, int index) {

        // Prepare
        Map<String, Integer> result = new HashMap<>();

        for (int col = 0; col < hierarchies[index][0].length; col++) {
            for (int row = 0; row < hierarchies[index].length; row++) {
                String value = hierarchies[index][row][col];
                if (!result.containsKey(value)) {
                    result.put(value, col);
                }
            }
        }
        
        // Return
        return result;
    }

    /**
     * Returns precisions
     * @param handle
     * @param indices
     * @return
     */
    Map<String, Double>[] getPrecision(DataHandleInternal handle, int[] indices) {

        // Prepare
        @SuppressWarnings("unchecked")
        Map<String, Double>[] precisions = new Map[indices.length];
        String[][][] hierarchies = getHierarchies(handle, indices);
        
        for (int i=0; i<precisions.length; i++) {
            
            try {
                
                // Extract info
                String[][] hierarchy = hierarchies[i];
                
                // Calculate precision
                Map<String, Double> precision = new HashMap<String, Double>();
                for (int col = 0; col < hierarchy[0].length; col++) {
                    for (int row = 0; row < hierarchy.length; row++) {
                        String value = hierarchy[row][col];
                        if (!precision.containsKey(value)) {
                            precision.put(value, (double)col / ((double)hierarchy[0].length - 1d));
                        }
                    }
                }
                
                // Store
                precisions[i] = precision;
                
            } catch (Exception e) {
                
                // Drop silently
                precisions[i] = null;
            }
        }

        // Return
        return precisions;
    }
}
