package org.deidentifier.arx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.framework.CompressedBitSet;

/**
 * This class represents a data subset as required for d-presence
 * @author Prasser, Kohlmayer
 *
 */
public class DataSubset {
    
    /**
     * Wrapper around a string array
     * @author Prasser, Kohlmayer
     *
     */
    private static class Entry{
        
        private String[] data;
        private int hashcode;
     
        public Entry(String[] data){
            this.data = data;
            this.hashcode = Arrays.hashCode(data);
        }

        @Override
        public int hashCode() {
            return hashcode;
        }

        @Override
        public boolean equals(Object obj) {
            Entry other = (Entry) obj;
            return Arrays.equals(data, other.data);
        }
    }
    
    /** The subset as a bitset*/
    private CompressedBitSet bitSet;
    
    /** The subset as a sorted array of indices*/
    private int[] sortedIndices;

    /**
     * Creates a new instance
     * @param bitSet
     * @param sortedIndices
     */
    private DataSubset(CompressedBitSet bitSet, int[] sortedIndices) {
        this.bitSet = bitSet;
        this.sortedIndices = sortedIndices;
    }

    /**
     * Create a subset by matching two data instances. 
     * @param data
     * @param file
     * @param separator
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
        CompressedBitSet bitset = new CompressedBitSet(bHandle.getNumRows());
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
                throw new IllegalArgumentException("No match found for: "+tuple);
            }
            if (indices.isEmpty()) {
            	throw new IllegalArgumentException("Too many matches found for: "+tuple);
            }
            int index = indices.remove(0);
            bitset.set(index);
            array[idx++] = index;
        }
        
        // Return
        Arrays.sort(array);
        return new DataSubset(bitset, array);
    }
    
    /**
     * Creates a subset from the given selector
     * @param data
     * @param selector
     * @return
     */
    public static DataSubset create(Data data, DataSelector selector){
        
        // Init
        int rows = data.getHandle().getNumRows();
        CompressedBitSet bitset = new CompressedBitSet(rows);
        ArrayList<Integer> list = new ArrayList<Integer>();
        
        // Check
        for (int i=0; i<rows; i++){
            if (selector.selected(i)) {
                bitset.set(i);
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
     * Creates a new subset from the given set of tuple indices
     * @param data
     * @param subset
     * @return
     */
    public static DataSubset create(Data data, Set<Integer> subset){
        int rows = data.getHandle().getNumRows();
        CompressedBitSet bitset = new CompressedBitSet(rows);
        int[] array = new int[subset.size()];
        int idx = 0;
        for (Integer line : subset) {
            if (line < 0 || line >= rows) {
                throw new IllegalArgumentException("Subset index out of range!");
            }
            bitset.set(line);
            array[idx++] = line;
        }
        Arrays.sort(array);
        return new DataSubset(bitset, array);
    }

    public CompressedBitSet getBitSet() {
        return bitSet;
    }

    public int[] getSortedIndices() {
        // TODO: What is this needed for?
        return sortedIndices;
    }
}
