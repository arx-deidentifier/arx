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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * This class encapsulates information about equivalence classes in a data set
 * 
 * @author Fabian Prasser
 */
public class RiskModelHistogram {

    /** The equivalence classes */
    private int[]  equivalenceClasses;
    /** Summary */
    private double avgClassSize;
    /** Summary */
    private double numRecords;
    /** Summary */
    private double numClasses;

    /**
     * Creates a new instance from the given distribution.
     * IMPORTANT: Suppressed records should have been ignored before calling this.
     * 
     * @param distribution
     */
    public RiskModelHistogram(final IntIntOpenHashMap distribution) {
        this.convertAndAnalyze(distribution,
                               new WrappedBoolean(),
                               new WrappedInteger());
    }

    /**
     * Creates a new instance by analyzing the given data handle. 
     * IMPORTANT: Suppressed records will be ignored!
     * 
     * @param handle
     * @param qis
     */
    RiskModelHistogram(final DataHandleInternal handle,
                       final Set<String> qis,
                       final WrappedBoolean stop,
                       final WrappedInteger progress,
                       double factor) {

        /* ********************************
         * Check 
         * ********************************/
        if (handle == null) { throw new NullPointerException("Handle is null"); }
        if (qis == null) { throw new NullPointerException("Quasi identifiers must not be null"); }
        for (String q : qis) {
            if (handle.getColumnIndexOf(q) == -1) { throw new IllegalArgumentException(q + " is not an attribute"); }
        }

        /* ********************************
         * Build equivalence classes 
         * ********************************/
        final int[] indices = new int[qis.size()];
        int index = 0;
        for (final String attribute : qis) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        Arrays.sort(indices);

        // Calculate equivalence classes
        int capacity = handle.getNumRows() / 10;
        capacity = capacity > 10 ? capacity : 10;
        Groupify<TupleWrapper> map = new Groupify<TupleWrapper>(capacity);
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {

            int prog = (int) Math.round((double) row / (double) numRows * factor * 80d);
            if (prog != progress.value) {
                progress.value = prog;
            }

            if (!handle.isOutlier(row, indices)) {
                TupleWrapper tuple = new TupleWrapper(handle, indices, row);
                map.add(tuple);
            }
            if (stop.value) { throw new ComputationInterruptedException(); }
        }

        // Group by size
        IntIntOpenHashMap grouped = new IntIntOpenHashMap();

        int i = 0;
        int size = map.size();
        Group<TupleWrapper> element = map.first();
        while (element != null) {
            int prog = (int) Math.round((80d + (double) i++ / (double) size * 20d) * factor);
            if (prog != progress.value) {
                progress.value = prog;
            }
            grouped.putOrAdd(element.getCount(), 1, 1);
            element = element.next();
            if (stop.value) { throw new ComputationInterruptedException(); }
        }

        map = null;

        convertAndAnalyze(grouped, stop, progress);
    }

    /**
     * Returns a property of the class distribution
     * 
     * @return the avgClassSize
     */
    public double getAvgClassSize() {
        return Double.isNaN(avgClassSize) ? 0d : avgClassSize;
    }

    /**
     * Returns class-size[idx], class-count[idx+1],... ordered ascending by
     * class size
     * 
     * @return the histogram
     */
    public int[] getHistogram() {
        return equivalenceClasses;
    }

    /**
     * Returns a property of the class distribution
     * 
     * @return the numClasses
     */
    public double getNumClasses() {
        return numClasses;
    }

    /**
     * Returns a property of the class distribution
     * 
     * @return the numRecords
     */
    public double getNumRecords() {
        return numRecords;
    }
    
    /**
     * Returns whether the histogram is empty
     * @return
     */
    public boolean isEmpty() {
        return numRecords == 0d;
    }

    /**
     * Convert and analyze
     * 
     * @param grouped
     * @param stop
     * @param progress
     */
    private void convertAndAnalyze(IntIntOpenHashMap grouped,
                                   final WrappedBoolean stop,
                                   final WrappedInteger progress) {

        // Convert
        int[][] temp = new int[grouped.size()][2];
        int idx = 0;
        final int[] values2 = grouped.values;
        final int[] keys2 = grouped.keys;
        final boolean[] states2 = grouped.allocated;
        for (int i = 0; i < states2.length; i++) {
            if (states2[i]) {
                temp[idx++] = new int[] { keys2[i], values2[i] };
            }
            if (stop.value) { throw new ComputationInterruptedException(); }
        }
        grouped = null;

        // Sort ascending by size
        Arrays.sort(temp, new Comparator<int[]>() {
            public int compare(int[] o1, int[] o2) {
                if (stop.value) { throw new ComputationInterruptedException(); }
                return Integer.compare(o1[0], o2[0]);
            }
        });

        // Convert and analyze
        int numClasses = 0;
        int numTuples = 0;
        this.equivalenceClasses = new int[temp.length * 2];
        idx = 0;
        for (int[] entry : temp) {
            this.equivalenceClasses[idx++] = entry[0];
            this.equivalenceClasses[idx++] = entry[1];
            numClasses += entry[1];
            numTuples += entry[0] * entry[1];
            if (stop.value) { throw new ComputationInterruptedException(); }
        }
        this.numRecords = numTuples;
        this.numClasses = numClasses;
        this.avgClassSize = this.numRecords / this.numClasses;
    }
}
