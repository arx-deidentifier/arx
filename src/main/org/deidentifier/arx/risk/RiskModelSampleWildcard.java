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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;
import org.deidentifier.arx.reliability.ParameterTranslation;

/**
 * This class implements risk measures as proposed by El Emam in
 * "Guide to the De-Identification of Personal Health Information",
 * "Measuring the Probability of Re-Identification" considering
 * suppressed values as a wildcard
 * 
 * @author Fabian Prasser
 */
public class RiskModelSampleWildcard {
    
    /**
     * Inner node in the index
     * @author Fabian Prasser
     */
    private class InnerNode extends Node {
        
        /** Children*/
        private final List<Node> children = new ArrayList<Node>();
        
        /**
         * Creates a new instance
         * @param value
         */
        protected InnerNode(String value) {
            super(value);
        }   
    }

    /**
     * Leaf node in the index
     * @author Fabian Prasser
     */
    private class LeafNode extends Node {
     
        /** Pointer to records */
        private final Group<TupleWrapper> records;

        /**
         * Creates a new instance
         * @param value
         * @param records
         * @param frequency
         */
        protected LeafNode(String value, Group<TupleWrapper> records) {
            super(value);
            this.records = records;
        }
    }

    /**
     * Abstract base class for nodes
     * @author Fabian Prasser
     */
    private abstract class Node {

        /** Value at this level*/
        protected final String value;
        
        /**
         * Creates a new instance
         * @param value
         */
        protected Node(String value) {
            this.value = value;
        }
    }

    /** Average risk */
    private final double averageRisk;
    /** Highest risk */
    private final double highestRisk;
    /** Threshold*/
    private final double threshold;
    /** Size threshold */
    private final int    sizeThreshold;
    /** Records at risk */
    private final double recordsAtRisk;
    /** Suppressed records */
    private final String wildcard;

    /**
     * Creates a new instance
     * @param handle handle
     * @param identifiers quasi-identifiers
     * @param threshold Note: due to rounding issues this threshold may be exceeded by up to 1%
     * @param wildcard string representing suppressed values
     * @param stop stop flag
     * @param progress progress
     */
    public RiskModelSampleWildcard(DataHandleInternal handle,
                                  Set<String> identifiers,
                                  double threshold,
                                  String wildcard,
                                  WrappedBoolean stop,
                                  WrappedInteger progress) {

        // Init
        this.wildcard = wildcard;
        this.threshold = threshold;
        this.sizeThreshold = ParameterTranslation.getSizeThreshold(threshold);
        
        if (wildcard == null) {
            throw new IllegalArgumentException("Wildcard must not be null");
        }
        
        // Calculate groups
        Groupify<TupleWrapper> groups = getGroups(handle, identifiers, 0d, 0.3d, wildcard, stop, progress);
        
        // Map of original frequencies
        Map<Group<TupleWrapper>, Integer> frequencies = new HashMap<>();
        
        // Now determine frequencies
        List<Node> index = new ArrayList<Node>();
        Group<TupleWrapper> group = groups.first();
        int progressMax = groups.size();
        int progressCount = 0;
        int numRecords = 0;
        while (group != null) {
            progress.value = (int)Math.round((0.3d + (double)(progressCount++) / (double)progressMax * 0.6d) * 100d); 
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
            frequencies.put(group, group.getCount());
            numRecords += group.getCount();
            add(stop, frequencies, group, index, 0);
            index(stop, frequencies, group, index, 0);
            group = group.next();
        }
        
        // And evaluate
        double totalRisk = 0d;
        double highestRisk = 0d;
        int numAtRisk = 0;
        group = groups.first();
        progressCount = 0;
        while (group != null) {
            progress.value = (int)Math.round((0.9d + (double)(progressCount++) / (double)progressMax * 0.1d) * 100d);
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
            double risk = 1d / (double) group.getCount();
            highestRisk = Math.max(highestRisk, risk);
            totalRisk += risk * (double) frequencies.get(group);
            if (group.getCount() < sizeThreshold) {
                numAtRisk += frequencies.get(group);
            }
            group = group.next();
        }
        
        // Records at risk
        this.recordsAtRisk = numRecords == 0 ? 0d : (double)numAtRisk / (double)numRecords;

        // Highest risk
        this.highestRisk = numRecords == 0 ? 0d : highestRisk;
        
        // Average risk
        this.averageRisk = numRecords == 0 ? 0d : (double)totalRisk / (double)numRecords;
    }

    /**
     * Returns the average risk
     * @return the average risk
     */
    public double getAverageRisk() {
        return averageRisk;
    }

    /**
     * Returns the effective risk threshold, which may differ from the specified risk threshold due to rounding issues
     * @return the effective risk threshold
     */
    public double getEffectiveRiskThreshold() {
        return ParameterTranslation.getEffectiveRiskThreshold(threshold);
    }
    
    /**
     * Returns the highest risk
     * @return the highest risk
     */
    public double getHighestRisk() {
        return highestRisk;
    }
    
    /**
     * Returns the fraction of records with a risk higher than the given threshold
     * @return the records at risk
     */
    public double getRecordsAtRisk() {
        return recordsAtRisk;
    }

    /**
     * Returns the user-specified threshold. Note: the effective threshold may differ slightly due to rounding issues.
     * See: <code>getEffectiveRiskThreshold</code>
     * @return
     */
    public double getRiskThreshold() {
        return threshold;
    }
    
    /**
     * Returns the success rate
     * @return
     */
    public double getSuccessRate() {
        return getAverageRisk();
    }

    /**
     * Adds frequency counts
     * @param stop
     * @param frequencies 
     * @param group
     * @param index
     * @param depth
     */
    private void add(WrappedBoolean stop, Map<Group<TupleWrapper>, Integer> frequencies, Group<TupleWrapper> group, List<Node> index, int depth) {

        // Extract
        boolean isLeafLevel = group.getElement().getValues().length - 1 == depth;
        String value = group.getElement().getValues()[depth];
        
        // For each potential match
        for (Node node : index) {
            
            // Check interrupt
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
            
            // Match
            if (node.value.equals(wildcard) || value.equals(wildcard) || node.value.equals(value)) {

                // Leaf
                if (isLeafLevel) {
                    LeafNode leaf = (LeafNode)node;
                    group.incCount(frequencies.get(leaf.records));
                    leaf.records.incCount(frequencies.get(group));
                    
                // Inner node
                } else {
                    add(stop, frequencies, group, ((InnerNode)node).children, depth + 1);
                }
            }
        }
    }

    /**
     * Computes the equivalence classes
     * @param handle
     * @param qis
     * @param offset
     * @param factor
     * @param wildcard
     * @param stop
     * @param progress
     * @return
     */
    private Groupify<TupleWrapper> getGroups(DataHandleInternal handle,
                                             Set<String> qis,
                                             double offset,
                                             double factor,
                                             String wildcard,
                                             WrappedBoolean stop,
                                             WrappedInteger progress) {

        /* ********************************
         * Check 
         * ********************************/
        if (handle == null) { 
            throw new NullPointerException("Handle is null"); 
        }
        if (qis == null) {
            throw new NullPointerException("Quasi-identifiers must not be null");
        }
        for (String q : qis) {
            if (handle.getColumnIndexOf(q) == -1) { 
                throw new IllegalArgumentException(q + " is not an attribute"); 
            }
        }

        /* ********************************
         * Determine indices of QIs
         * ********************************/
        final int[] indices = new int[qis.size()];
        int index = 0;
        for (final String attribute : qis) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        Arrays.sort(indices);


        /* ********************************
         * Build equivalence classes 
         * ********************************/
        int capacity = handle.getNumRows() / 10;
        capacity = capacity > 10 ? capacity : 10;
        Groupify<TupleWrapper> map = new Groupify<TupleWrapper>(capacity);
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {

            int prog = (int) Math.round(offset + (double) row / (double) numRows * factor);
            if (prog != progress.value) {
                progress.value = prog;
            }

            if (!handle.isOutlier(row, indices)) {
                TupleWrapper tuple = new TupleWrapper(handle, indices, row);
                map.add(tuple);
            }
            if (stop.value) { 
                throw new ComputationInterruptedException();
            }
        }

        // Return
        return map;
    }
    
    /**
     * Adds element to index
     * @param stop
     * @param frequencies
     * @param group
     * @param index
     * @param depth
     */
    private void index(WrappedBoolean stop, Map<Group<TupleWrapper>, Integer> frequencies, Group<TupleWrapper> group, List<Node> index, int depth) {

        // Extract
        boolean isLeafLevel = group.getElement().getValues().length - 1 == depth;
        String value = group.getElement().getValues()[depth];
        
        // For each potential match
        for (Node node : index) {

            // Check interrupt
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
            
            // Match
            if (node.value.equals(value)) {

                // Leaf
                if (isLeafLevel) {
                    throw new IllegalStateException("Duplicate entry");
                    
                // Inner node
                } else {
                    
                    // Go on
                    index(stop, frequencies, group, ((InnerNode)node).children, depth + 1);
                    return;
                }
            }
        }
        
        // Add new entry
        if (isLeafLevel) {
            index.add(new LeafNode(value, group));
        } else {
            InnerNode node = new InnerNode(value);
            index.add(node);
            
            // Go on
            index(stop, frequencies, group, node.children, depth + 1);
        }
    }
}