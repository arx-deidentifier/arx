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
package org.deidentifier.arx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.IntArrayList;

/**
 * This class represents a the dataset that is to be de-identified 
 * as a subset of the given population table.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataSubset implements Serializable {
    
    /**
     * Wrapper around a string array.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    private static class Entry implements Serializable {
        
        /** SVUID */
        private static final long serialVersionUID = 31695068160887476L;

        /** Record */
        private String[]          data;

        /** Hashcode */
        private int               hashcode;
     
        /**
         * 
         *
         * @param data
         */
        public Entry(String[] data){
            this.data = data;
            this.hashcode = Arrays.hashCode(data);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            Entry other = (Entry) obj;
            return Arrays.equals(data, other.data);
        }

        @Override
        public int hashCode() {
            return hashcode;
        }
    }

    /**  SVUID */
    private static final long serialVersionUID = 3945730896172205344L;
    
    /**
     * Create a subset by matching two data instances.
     *
     * @param data
     * @param subset
     * @return
     */
    public static DataSubset create(Data data, Data subset){
        
        // TODO: Implement more efficiently
        DataHandle bHandle = data.getHandle();
        DataHandle sHandle = subset.getHandle();
        
        // Add background data to map
        Map<Entry, List<Integer>> background = new HashMap<Entry, List<Integer>>();
        
        for (int i=0; i<bHandle.getNumRows(); i++){
            String[] tuple = new String[bHandle.getNumColumns()];
            for (int j=0; j<tuple.length; j++){
                tuple[j] = bHandle.getValue(i, j);
            }
            Entry entry = new Entry(tuple);
            if (!background.containsKey(entry)) {
            	background.put(entry, new ArrayList<Integer>());
            }
            background.get(entry).add(i);
        }
        
        // Init
        RowSet bitset = RowSet.create(data);
        int[] array = new int[sHandle.getNumRows()];
        int idx = 0;
        
        // Match subset
        for (int i=0; i<sHandle.getNumRows(); i++){
            String[] tuple = new String[sHandle.getNumColumns()];
            for (int j=0; j<tuple.length; j++){
                tuple[j] = sHandle.getValue(i, j);
            }
            List<Integer> indices = background.get(new Entry(tuple));
            if (indices == null) {
                throw new IllegalArgumentException("No match found for: "+Arrays.toString(tuple));
            }
            if (indices.isEmpty()) {
            	throw new IllegalArgumentException("Too many matches found for: "+Arrays.toString(tuple));
            }
            int index = indices.remove(0);
            bitset.add(index);
            array[idx++] = index;
        }
        
        // Return
        Arrays.sort(array);
        return new DataSubset(bitset, array);
    }
    
    /**
     * Creates a subset from the given selector.
     *
     * @param data
     * @param selector
     * @return
     */
    public static DataSubset create(Data data, DataSelector selector){
        
        // Init
        int rows = data.getHandle().getNumRows();
        RowSet bitset = RowSet.create(data);
        ArrayList<Integer> list = new ArrayList<Integer>();
        
        // Check
        for (int i=0; i<rows; i++){
            if (selector.isSelected(i)) {
                bitset.add(i);
                list.add(i);
            }
        }
        
        // Convert
        int[] array = new int[list.size()];
        for (int i=0; i<list.size(); i++){
            array[i] = list.get(i);
        }
        
        // Return
        return new DataSubset(bitset, array);
    }

    /**
     * Creates a new subset from the given row set, from which a copy is created.
     *
     * @param data
     * @param subset
     * @return
     */
    public static DataSubset create(Data data, RowSet subset) {
        return create(data.getHandle().getNumRows(), subset);
    }

    /**
     * Creates a new subset from the given set of tuple indices.
     *
     * @param data
     * @param subset
     * @return
     */
    public static DataSubset create(Data data, Set<Integer> subset){
        return create(data.getHandle().getNumRows(), subset);
    }

    /**
     * Creates a new subset from the given row set, from which a copy is created.
     *
     * @param data
     * @param subset
     * @return
     */
    public static DataSubset create(int rows, RowSet subset) {
        RowSet bitset = RowSet.create(rows);
        int[] array = new int[subset.size()];
        int idx = 0;
        for (int i=0; i<rows; i++){
            if (subset.contains(i)) {
                bitset.add(i);
                array[idx++]=i;
            }
        }
        return new DataSubset(bitset, array);
    }
    
    /**
     * Creates a new subset from the given set of tuple indices.
     *
     * @param rows
     * @param subset
     * @return
     */
    public static DataSubset create(int rows, Set<Integer> subset){
        RowSet bitset = RowSet.create(rows);
        int[] array = new int[subset.size()];
        int idx = 0;
        for (Integer line : subset) {
            if (line < 0 || line >= rows) {
                throw new IllegalArgumentException("Subset index out of range!");
            }
            bitset.add(line);
            array[idx++] = line;
        }
        Arrays.sort(array);
        return new DataSubset(bitset, array);
    }
    
    /** The subset as a bitset. */
    protected RowSet set;
    
    /** The subset as a sorted array of indices. */
    protected int[] array;


    /**
     * Creates a new instance.
     *
     * @param bitSet
     * @param sortedIndices
     */
    private DataSubset(RowSet bitSet, int[] sortedIndices) {
        this.set = bitSet;
        this.array = sortedIndices;
    }

    /**
     * Clone
     */
    public DataSubset clone() {
        return new DataSubset(this.set.clone(), Arrays.copyOf(this.array, this.array.length));
    }

    /**
     * Getter
     *
     * @return
     */
    public int[] getArray() {
        return array;
    }
    
    /**
     * Getter
     * 
     * @return
     */
    public RowSet getSet() {
        return set;
    }

    /**
     * Returns the size of the data subset
     * @return
     */
    public int getSize() {
        return array.length;
    }

    /**
     * Returns a new data subset, only containing those rows that are included in the subset
     * @param rowset
     * @return
     */
    protected DataSubset getSubsetInstance(RowSet rowset) {
        int index = -1;
        RowSet newset = RowSet.create(rowset.size());
        IntArrayList list = new IntArrayList();
        for (int row = 0; row < this.set.length(); row++) {
            if (rowset.contains(row)) {
                index++;
                if (this.set.contains(row)) {
                    newset.add(index);
                    list.add(index);
                }
            }
        }
        return new DataSubset(newset, list.toArray());
    }
}
