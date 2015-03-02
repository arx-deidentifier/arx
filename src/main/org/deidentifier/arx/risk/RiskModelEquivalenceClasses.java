/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedInteger;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * This class encapsulates information about equivalence classes in a data set
 * 
 * @author Fabian Prasser
 */
public class RiskModelEquivalenceClasses {
    
    /**
     * For hash tables
     * @author Fabian Prasser
     */
    private static class TupleWrapper {
        
        /** Hash code*/
        private final int        hashCode;
        /** Row */
        private final int        row;
        /** Indices */
        private final int[]      indices;
        /** Handle */
        private final DataHandle handle;

        /**
         * Constructor
         * @param handle
         * @param row
         */
        private TupleWrapper(DataHandle handle, int[] indices, int row) {
            this.handle = handle;
            this.row = row;
            this.indices = indices;
            
            int result = 1;
            for (int index : indices) {
                result = 31 * result + handle.getValue(row, index).hashCode();
            }
            this.hashCode = result;
        }

        @Override
        public boolean equals(Object obj) {
            TupleWrapper other = (TupleWrapper)obj;
            for (int i = 0; i < indices.length; i++) {
                if (handle.getValue(this.row, i) != handle.getValue(other.row, i)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /** The equivalence classes */
    private int[]       equivalenceClasses;
    /** Summary */
    private double      avgClassSize;
    /** Summary */
    private double      numTuples;
    /** Summary */
    private double      numClasses;
    
    /**
     * Creates a new instance
     * @param handle
     */
    public RiskModelEquivalenceClasses(final DataHandle handle) {
        this(handle, handle.getDefinition().getQuasiIdentifyingAttributes());
    }

    /**
     * Creates a new instance
     * @param handle
     * @param qis
     */
    public RiskModelEquivalenceClasses(final DataHandle handle, final Set<String> qis) {
        this(handle, qis, new WrappedBoolean(), new WrappedInteger(), 1.0d);
    }

    /**
     * Creates a new instance
     * @param distribution
     */
    public RiskModelEquivalenceClasses(final IntIntOpenHashMap distribution) {
        this.convertAndAnalyze(distribution, new WrappedBoolean(), new WrappedInteger());
    }
    /**
     * Creates a new instance
     * @param handle
     * @param qis
     */
    RiskModelEquivalenceClasses(final DataHandle handle,
                                final Set<String> qis,
                                final WrappedBoolean stop,
                                final WrappedInteger progress,
                                double factor) {
        
        /* ********************************
         *  Check
         * ********************************/
        if (handle == null) {
            throw new NullPointerException("Handle is null");
        }
        if (qis == null) {
            throw new NullPointerException("Quasi identifiers must not be null");
        }        
        for (String q : qis) {
            if (handle.getColumnIndexOf(q) == -1) {
                throw new IllegalArgumentException(q + " is not an attribute");
            }
        }

        /* ********************************
         *  Build equivalence classes
         * ********************************/
        final int[] indices = new int[qis.size()];
        int index = 0;
        for (final String attribute : qis) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        Arrays.sort(indices);

        // Calculate equivalence classes
        ObjectIntOpenHashMap<TupleWrapper> map = new ObjectIntOpenHashMap<TupleWrapper>();
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {
            
            int prog = (int)Math.round((double)row / (double)numRows * factor * 80d);
            if (prog != progress.value) {
                progress.value = prog;
            }
            
            TupleWrapper tuple = new TupleWrapper(handle, indices, row);
            map.putOrAdd(tuple, 1, 1);
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
        }

        // Group by size
        IntIntOpenHashMap grouped = new IntIntOpenHashMap();
        final int[] values = map.values;
        final boolean[] states = map.allocated;
        for (int i = 0; i < states.length; i++) {
            if (states[i]) {
                int prog = (int)Math.round((80d + (double)i / (double)states.length * 20d) * factor);
                if (prog != progress.value) {
                    progress.value = prog;
                }
                grouped.putOrAdd(values[i], 1, 1);
            }
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
        }
        map = null;
        
        convertAndAnalyze(grouped, stop, progress);
    }

    /**
     * Returns a property of the class distribution
     * @return the avgClassSize
     */
    public double getAvgClassSize() {
        return avgClassSize;
    }

    /**
     * Returns class-size[idx], class-count[idx+1],... ordered ascending by class size
     * 
     * @return the equivalenceClasses
     */
    public int[] getEquivalenceClasses() {
        return equivalenceClasses;
    }

    /**
     * Returns a property of the class distribution
     * @return the numClasses
     */
    public double getNumClasses() {
        return numClasses;
    }

    /**
     * Returns a property of the class distribution
     * @return the numTuples
     */
    public double getNumTuples() {
        return numTuples;
    }

    /**
     * Convert and analyze
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
                temp[idx++] = new int[]{keys2[i], values2[i]};
            }
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
        }
        grouped = null;
        
        // Sort ascending by size
        Arrays.sort(temp, new Comparator<int[]>(){
            public int compare(int[] o1, int[] o2) {
                if (stop.value) {
                    throw new ComputationInterruptedException();
                }
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
            if (stop.value) {
                throw new ComputationInterruptedException();
            }
        }
        this.numTuples = numTuples;
        this.numClasses = numClasses;
        this.avgClassSize = this.numTuples / this.numClasses;
    }
}
