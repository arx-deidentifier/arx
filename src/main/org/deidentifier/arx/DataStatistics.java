/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataStatistics.ContingencyTable.Entry;
import org.deidentifier.arx.DataType.ARXString;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * A class offering basic descriptive statistics about data handles
 * @author Fabian Prasser
 *
 */
public class DataStatistics {

    /**
     * A frequency distribution
     * @author Fabian Prasser
     *
     */
    public static class FrequencyDistribution {

        /** The data values, sorted*/
        public final String[] values;
        /** The corresponding frequencies*/
        public final double[] frequency;
        /** The total number of data values*/
        public final int      count;

        /**
         * Internal constructor
         * @param items
         * @param frequency
         * @param count
         */
        private FrequencyDistribution(String[] items, double[] frequency, int count) {
            this.values = items;
            this.count = count;
            this.frequency = frequency;
        }
    }

    /**
     * A contingency table
     * @author Fabian Prasser
     */
    public static class ContingencyTable {
        
        /**
         * An entry in the contingency table
         * @author Fabian Prasser
         */
        public static class Entry {
            
            /** Index of the value from the first column*/
            public int    value1;
            /** Index of the value from the second column*/
            public int    value2;
            /** Associated frequency*/
            public double frequency;
            
            /**
             * Internal constructor
             * @param value1
             * @param value2
             */
            private Entry(int value1, int value2){
                this.value1 = value1;
                this.value2 = value2;
            }
            
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + value1;
                result = prime * result + value2;
                return result;
            }
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null) return false;
                if (getClass() != obj.getClass()) return false;
                Entry other = (Entry) obj;
                if (value1 != other.value1) return false;
                if (value2 != other.value2) return false;
                return true;
            }
        }

        /** The data values from the first column, sorted*/
        public final String[]        values1;
        /** The data values from the second column, sorted*/
        public final String[]        values2;
        /** The total number of entries in the contingency table*/
        public final int             count;
        /** An iterator over the elements in the contingency table*/
        public final Iterator<ContingencyTable.Entry> iterator;

        /**
         * Internal constructor
         * @param value1
         * @param value2
         * @param count
         * @param iterator
         */
        private ContingencyTable(String[] value1, String[] value2, int count, 
                                 Iterator<ContingencyTable.Entry> iterator) {
            this.values1 = value1;
            this.values2 = value2;
            this.count = count;
            this.iterator = iterator;
        }
    }

    /** The handle*/
    private DataHandle handle;
    
    /**
     * Creates a new instance
     * @param handle
     */
    protected DataStatistics(DataHandle handle){
        this.handle = handle;
    }

    /**
     * Returns the distinct set of data items from the given column
     * 
     * @param column The column
     * @return
     */
    public String[] getDistinctValues(int column) {
        return this.handle.getDistinctValues(column);
    }

    /**
     * Returns an order list of the distinct set of data items from the given column
     * 
     * @param column                   The column
     * @param orderFromDefinition      Indicates whether the order that should be assumed for string data 
     *                                 items can (and should) be derived from the hierarchy provided in the 
     *                                 data definition (if any)
     * @return
     */
    public String[] getDistinctValuesOrdered(int column, boolean orderFromDefinition) {
        return getDistinctValuesOrdered(column, getHierarchy(column, orderFromDefinition));
    }
    
    /**
     * Returns an order list of the distinct set of data items from the given column. This method assumes 
     * that the order of string data items can (and should) be derived from the hierarchy provided in the 
     * data definition (if any)
     * 
     * @param column The column
     * @return
     */
    public String[] getDistinctValuesOrdered(int column) {
        return this.getDistinctValuesOrdered(column, true);
    }
    

    /**
     * Returns an order list of the distinct set of data items from the given column. This method assumes 
     * that the order of string data items can (and should) be derived from the provided hierarchy
     * 
     * @param column The column
     * @param hierarchy The hierarchy, may be null
     * @return
     */
    public String[] getDistinctValuesOrdered(int column, Hierarchy hierarchy) {


        // Obtain list and data type
        final String[] list = getDistinctValues(column);
        final String attribute = handle.getAttributeName(column);
        final DataType<?> datatype = handle.getDataType(attribute);
        final int level = handle.getGeneralization(attribute);
        final String[][] _hierarchy = hierarchy != null ? hierarchy.getHierarchy() : null;

        // Sort by data type
        if (_hierarchy == null){
            sort(list, datatype);
            
        // Sort by hierarchy
        } else {
            // Build order directly from the hierarchy
            final Map<String, Integer> order = new HashMap<String, Integer>();
            int max = 0; // The order to use for the suppression string
            if (level==0 || handle.getBaseDataType(attribute) instanceof ARXString){
                for (int i=0; i<_hierarchy.length; i++){
                    if (!order.containsKey(_hierarchy[i][level])) {
                        order.put(_hierarchy[i][level], order.size());
                    }
                }
                max = order.size();
            // Build order indirectly by using a data type and a hierarchy
            } else {
                // Create base order
                Set<String> baseSet = new HashSet<String>();
                for (int i=0; i<_hierarchy.length; i++){
                    baseSet.add(_hierarchy[i][0]);
                }
                String[] baseArray = baseSet.toArray(new String[baseSet.size()]);
                sort(baseArray, handle.getBaseDataType(attribute));
                Map<String, Integer> baseOrder = new HashMap<String, Integer>();
                for (int i=0; i<baseArray.length; i++){
                    baseOrder.put(baseArray[i], i);
                }
                
                // Build higher level order from base order
                for (int i=0; i<_hierarchy.length; i++){
                    if (!order.containsKey(_hierarchy[i][level])) {
                        int position = baseOrder.get(_hierarchy[i][0]);
                        order.put(_hierarchy[i][level], position);
                        max = Math.max(position, max)+1;
                    }
                }
            }
            
            // Add suppression string
            String supp = handle.getSuppressionString();
            if (supp != null) order.put(supp, max);
            
            // Sort
            sort(list, order);
        }
        
        // Done
        return list;
    }
    
    /**
     * Returns a frequency distribution for the values in the given column. This method assumes that the 
     * order of string data items can (and should) be derived from the hierarchy provided in the data 
     * definition (if any)
     * 
     * @param column The column
     * @return
     */
    public FrequencyDistribution getFrequencyDistribution(int column) {
        return getFrequencyDistribution(column, true);
    }

    /**
     * Returns a frequency distribution for the values in the given column
     * 
     * @param column                   The column
     * @param orderFromDefinition      Indicates whether the order that should be assumed for string data items 
     *                                 can (and should) be derived from the hierarchy provided in the data 
     *                                 definition (if any)
     * @return
     */
    public FrequencyDistribution getFrequencyDistribution(int column, boolean orderFromDefinition) {
        return getFrequencyDistribution(column, getHierarchy(column, orderFromDefinition));
    }

    /**
     * Returns a frequency distribution for the values in the given column. The order for string data items 
     * is derived from the provided hierarchy
     * 
     * @param column    The column
     * @param hierarchy The hierarchy, may be null      
     * @return
     */
    public FrequencyDistribution getFrequencyDistribution(int column, Hierarchy hierarchy) {

        // Init
        String[] values = getDistinctValuesOrdered(column, hierarchy);
        double[] frequencies = new double[values.length];
        
        // Create map of indexes
        Map<String, Integer> indexes = new HashMap<String, Integer>();
        for (int i=0; i<values.length; i++){
            indexes.put(values[i], i);
        }
        
        // Count frequencies
        for (int row=0; row<handle.getNumRows(); row++){
            String value = handle.getValue(row, column);
            frequencies[indexes.get(value)]++;
        }
        
        // Divide by count
        int count = handle.getNumRows();
        for (int i=0; i<frequencies.length; i++){
            frequencies[i] /= (double)count;
        }
        
        // Return
        return new FrequencyDistribution(values, frequencies, count);
    }

    /**
     * Returns a contingency table for the given columns. This method assumes that the 
     * order of string data items can (and should) be derived from the hierarchies provided 
     * in the data definition (if any)
     * 
     * @param column1 The first column
     * @param column2 The second column
     * @return
     */
    public ContingencyTable getContingencyTable(int column1, int column2) {
        return getContingencyTable(column1, true, column2, true);
    }

    /**
     * Returns a contingency table for the given columns
     * 
     * @param column1                   The first column
     * @param orderFromDefinition1      Indicates whether the order that should be assumed for string data items 
     *                                  can (and should) be derived from the hierarchy provided in the data 
     *                                  definition (if any)
     * @param column2                   The second column
     * @param orderFromDefinition2      Indicates whether the order that should be assumed for string data items 
     *                                  can (and should) be derived from the hierarchy provided in the data 
     *                                  definition (if any)
     * @return
     */
    public ContingencyTable getContingencyTable(int column1, boolean orderFromDefinition1,
                                                int column2, boolean orderFromDefinition2) {
        
        return getContingencyTable(column1, getHierarchy(column1, orderFromDefinition1),
                                   column2, getHierarchy(column2, orderFromDefinition2));
    }
    
    /**
     * Returns a contingency table for the given columns. The order for string data items is derived
     * from the provided hierarchies
     * 
     * @param column1     The first column
     * @param hierarchy1  The hierarchy for the first column, may be null
     * @param column2     The second column
     * @param hierarchy2  The hierarchy for the second column, may be null
     * @return
     */
    public ContingencyTable getContingencyTable(int column1, Hierarchy hierarchy1,
                                                int column2, Hierarchy hierarchy2) {

        // Init
        String[] values1 = getDistinctValuesOrdered(column1, hierarchy1);
        String[] values2 = getDistinctValuesOrdered(column2, hierarchy2);
        
        // Create maps of indexes
        Map<String, Integer> indexes1 = new HashMap<String, Integer>();
        for (int i=0; i<values1.length; i++){
            indexes1.put(values1[i], i);
        }
        Map<String, Integer> indexes2 = new HashMap<String, Integer>();
        for (int i=0; i<values2.length; i++){
            indexes2.put(values2[i], i);
        }
        
        // Create entry set
        final Map<Entry, Integer> entries = new HashMap<Entry, Integer>();
        for (int row=0; row<handle.getNumRows(); row++){
            int index1 = indexes1.get(handle.getValue(row, column1));
            int index2 = indexes2.get(handle.getValue(row, column2));
            Entry entry = new Entry(index1, index2);
            Integer previous = entries.get(entry);
            entries.put(entry, previous != null ? previous + 1 : 1);
        }
        
        // Create iterator
        final int count = handle.getNumRows();
        final Iterator<Entry> internal = entries.keySet().iterator();
        final Iterator<Entry> iterator = new Iterator<Entry>(){

            private Iterator<Entry> _internal = internal;
            private Map<Entry, Integer> _entries = entries;
            
            @Override
            public boolean hasNext() {
                
                if (_internal == null) return false;
                boolean result = _internal.hasNext();
                
                // Try to release resources as early as possible
                if (!result){
                    _internal = null;
                    _entries = null;
                }
                return result;
            }

            @Override
            public Entry next() {
                if (_internal == null) return null;
                Entry e = _internal.next();
                e.frequency = (double)_entries.get(e) / (double)count;
                return e;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        // Result result
        return new ContingencyTable(values1, values2, count, iterator);
    }
    
    /**
     * Orders the given array by data type
     * 
     * @param array
     * @param type
     */
    private void sort(final String[] array, final DataType<?> type){
        GenericSorting.mergeSort(0, array.length, new IntComparator(){
            @Override
            public int compare(int arg0, int arg1) {
                try {
                    return type.compare(array[arg0], array[arg1]);
                } catch (NumberFormatException | ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Swapper(){
            @Override
            public void swap(int arg0, int arg1) {
                String temp = array[arg0];
                array[arg0] = array[arg1];
                array[arg1] = temp;
            }
        });
    }
    

    /**
     * Orders the given array by the given sort order
     * 
     * @param array
     * @param type
     */
    private void sort(final String[] array, final Map<String, Integer> order){
        GenericSorting.mergeSort(0, array.length, new IntComparator(){
            @Override
            public int compare(int arg0, int arg1) {
                return order.get(array[arg0]).compareTo(order.get(array[arg1]));
            }
        }, new Swapper(){
            @Override
            public void swap(int arg0, int arg1) {
                String temp = array[arg0];
                array[arg0] = array[arg1];
                array[arg1] = temp;
            }
        });
    }
    
    /**
     * Returns the appropriate hierarchy, if any
     * 
     * @param column
     * @param orderFromDefinition
     */
    private Hierarchy getHierarchy(int column, boolean orderFromDefinition){

        // Init
        final String attribute = handle.getAttributeName(column);
        final AttributeType type = handle.getDefinition().getAttributeType(attribute);
        final DataType<?> datatype = handle.getDataType(attribute);
        final Hierarchy hierarchy;
        
        // Check if hierarchy available
        if (orderFromDefinition && datatype instanceof ARXString && type instanceof Hierarchy) {
            hierarchy = ((Hierarchy) type);
        } else {
            hierarchy = null;
        }
        
        return hierarchy;
    }
}
