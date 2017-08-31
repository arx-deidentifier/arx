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

package org.deidentifier.arx.aggregates.quality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Base class for quality models
 * 
 * @author Fabian Prasser
 * 
 * @param <T>
 */
abstract class QualityModel<T> {

    /** Log */
    private static final double          LOG2 = Math.log(2);

    /** Input */
    private final DataHandle             input;

    /** Output */
    private final DataHandle             output;

    /** Grouped */
    private final Groupify<TupleWrapper> groupedInput;

    /** Grouped */
    private final Groupify<TupleWrapper> groupedOutput;

    /** Input */
    private final int[]                  indices;

    /** Flag */
    private final WrappedBoolean         interrupt;

    /** Hierarchies */
    private final String[][][]           hierarchies;

    /** Shares */
    private final QualityDomainShare[]   shares;

    /** Value */
    private final String                 suppressedValue;

    /** Roots*/
    private final Map<Integer, String>   roots = new HashMap<>();

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param input
     * @param output
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    QualityModel(WrappedBoolean interrupt,
                 DataHandle input,
                 DataHandle output,
                 Groupify<TupleWrapper> groupedInput,
                 Groupify<TupleWrapper> groupedOutput,
                 String[][][] hierarchies,
                 QualityDomainShare[] shares,
                 int[] indices,
                 QualityConfiguration config) {
        
        // Store data
        this.input = input;
        this.output = output;
        this.groupedInput = groupedInput;
        this.groupedOutput = groupedOutput;
        this.indices = indices;
        this.shares = shares;
        this.hierarchies = hierarchies;
        this.interrupt = interrupt;
        this.suppressedValue = config.getSuppressedValue();
        
        // Collect roots
        for (int index = 0; index < indices.length; index++) {
            int column = indices[index];
            String root = getRoot(hierarchies[index]);
            this.roots.put(column,  root);
        }
    }

    /**
     * Returns the root for the given hierarchy
     * @param strings
     * @return
     */
    private String getRoot(String[][] hierarchy) {
        Set<String> roots = new HashSet<>();
        for (String[] row : hierarchy) {
            roots.add(row[row.length - 1]);
        }
        return (roots.size() == 1) ? roots.iterator().next() : null;
    }

    /**
     * Checks whether an interruption happened.
     */
    void checkInterrupt() {
        if (interrupt.value) { throw new ComputationInterruptedException("Interrupted"); }
    }

    /**
     * Evaluates the utility measure
     * 
     * @return
     */
    abstract T evaluate();

    /**
     * Returns the domain shares
     */
    QualityDomainShare[] getDomainShares() {
        return shares;
    }
    
    /**
     * Returns grouped input
     */
    Groupify<TupleWrapper> getGroupedInput() {
        return groupedInput;
    }

    /**
     * Returns grouped output
     */
    Groupify<TupleWrapper> getGroupedOutput() {
        return groupedOutput;
    }

    /**
     * Returns the hierarchies
     */
    String[][][] getHierarchies() {
        return hierarchies;
    }

    /**
     * Returns relevant indices
     */
    int[] getIndices() {
        return indices;
    }

    /**
     * Returns input
     * 
     * @return
     */
    DataHandle getInput() {
        return this.input;
    }

    /**
     * Returns output
     * 
     * @return
     */
    DataHandle getOutput() {
        return this.output;
    }

    /**
     * Returns the suppression string
     * @return
     */
    String getSuppressionString() {
        return suppressedValue;
    }

    /**
     * Returns whether a value is suppressed
     * 
     * @param handle
     * @param row
     * @param column
     * @return
     */
    boolean isSuppressed(DataHandle handle, int row, int column) {

        // Check flag
        if (handle.isOutlier(row)) {
            return true;
        } else {
            return isSuppressed(column, handle.getValue(row, column));
        }
    }

    /**
     * We assume that an entry is suppressed, if all values are equal
     * 
     * @param entry
     * @return
     */
    boolean isSuppressed(DataHandle handle, int[] indices, int row) {

        // Check flag
        if (handle.isOutlier(row)) { return true; }

        // Check values
        for (int i = 1; i < indices.length; i++) {
            if (!handle.getValue(row, indices[i - 1]).equals(handle.getValue(row, indices[i]))) { return false; }
        }
        return true;
    }

    /**
     * We assume that an entry is suppressed, if all values are equal
     * 
     * @param entry
     * @return
     */
    boolean isSuppressed(Group<TupleWrapper> entry) {

        // Check flag
        if (entry.getElement().isOutlier()) { return true; }

        // Check values
        String[] array = entry.getElement().getValues();
        for (int i = 1; i < array.length; i++) {
            if (!array[i - 1].equals(array[i])) { return false; }
        }
        return true;
    }

    /**
     * Returns whether a value is suppressed
     * 
     * @param column
     * @param value
     * @return
     */
    boolean isSuppressed(int column, String value) {
        return value.equals(suppressedValue) || value.equals(roots.get(column));
    }

    /**
     * Log base-2
     * 
     * @param d
     * @return
     */
    double log2(double d) {
        return Math.log(d) / LOG2;
    }
}
