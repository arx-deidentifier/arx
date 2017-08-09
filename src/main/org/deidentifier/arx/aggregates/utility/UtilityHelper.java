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
import java.util.Map;

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
    
    /** Flag*/
    private final WrappedBoolean interrupt;
    
    /**
     * Creates a new instance
     * @param interrupt
     */
    UtilityHelper(WrappedBoolean interrupt) {
        this.interrupt = interrupt;
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
     * Returns domain shares for the handle
     * @param handle
     * @param indices
     * @return
     */
    UtilityDomainShare[] getDomainShares(DataHandleInternal handle, int[] indices) {

        // Compute domain shares
        UtilityDomainShare[] shares = new UtilityDomainShare[indices.length];
        for (int i=0; i<shares.length; i++) {
            
            // Extract info
            String attribute = handle.getAttributeName(indices[i]);
            String[][] hierarchy = handle.getDefinition().getHierarchy(attribute);
            HierarchyBuilder<?> builder = handle.getDefinition().getHierarchyBuilder(attribute);
            
            // Create shares for redaction-based hierarchies
            if (builder != null && (builder instanceof HierarchyBuilderRedactionBased) &&
                ((HierarchyBuilderRedactionBased<?>)builder).isDomainPropertiesAvailable()){
                shares[i] = new UtilityDomainShareRedaction((HierarchyBuilderRedactionBased<?>)builder);
                
            // Create fallback-shares for materialized hierarchies
            // TODO: Interval-based hierarchies are currently not compatible
            } else {
                shares[i] = new UtilityDomainShareRaw(hierarchy);
            }
        }

        // Return
        return shares;
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
        for (int i=0; i<precisions.length; i++) {
            
            // Extract info
            String attribute = handle.getAttributeName(indices[i]);
            String[][] hierarchy = handle.getDefinition().getHierarchy(attribute);
            
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
        }

        // Return
        return precisions;
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (interrupt.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }
}
