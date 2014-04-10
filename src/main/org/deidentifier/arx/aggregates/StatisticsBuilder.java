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

package org.deidentifier.arx.aggregates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandleStatistics;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXString;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * A class offering basic descriptive statistics about data handles
 * @author Fabian Prasser
 *
 */
public class StatisticsBuilder {
    
    /**
     * Creates a new instance
     * @param handle
     * @param averageEquivalenceClassSize
     * @param maximalEquivalenceClassSize
     * @param minimalEquivalenceClassSize
     * @param numberOfGroups
     * @param numberOfOutlyingEquivalenceClasses
     * @param numberOfOutlyingTuples
     */
    public StatisticsBuilder(DataHandleStatistics handle,
                          StatisticsEquivalenceClasses ecStatistics) {
        this.ecStatistics = ecStatistics;
        this.handle = handle;
    }
    
    /** The handle*/
    private DataHandleStatistics handle;
    
    /** The equivalence class statistics*/
    private StatisticsEquivalenceClasses ecStatistics;
    
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
        if (_hierarchy == null || level==0){
            sort(list, datatype, handle.getSuppressionString());
        // Sort by hierarchy and data type
        } else {
            // Build order directly from the hierarchy
            final Map<String, Integer> order = new HashMap<String, Integer>();
            int max = 0; // The order to use for the suppression string

            // Create base order
            Set<String> baseSet = new HashSet<String>();
            DataType<?> baseType = handle.getBaseDataType(attribute);
            for (int i = 0; i < _hierarchy.length; i++) {
                String element = _hierarchy[i][0];
                // Make sure that only elements from the hierarchy
                // are added that are included in the data
                // TODO: Calling isValid is only a work-around
                if (baseType.isValid(element)) baseSet.add(element);
            }
            String[] baseArray = baseSet.toArray(new String[baseSet.size()]);
            sort(baseArray, handle.getBaseDataType(attribute), handle.getSuppressionString());
            Map<String, Integer> baseOrder = new HashMap<String, Integer>();
            for (int i = 0; i < baseArray.length; i++) {
                baseOrder.put(baseArray[i], i);
            }

            // Build higher level order from base order
            for (int i = 0; i < _hierarchy.length; i++) {
                if (!order.containsKey(_hierarchy[i][level])) {
                    Integer position = baseOrder.get(_hierarchy[i][0]);
                    if (position != null) {
                        order.put(_hierarchy[i][level], position);
                        max = Math.max(position, max) + 1;
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
    public StatisticsFrequencyDistribution getFrequencyDistribution(int column) {
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
    public StatisticsFrequencyDistribution getFrequencyDistribution(int column, boolean orderFromDefinition) {
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
    public StatisticsFrequencyDistribution getFrequencyDistribution(int column, Hierarchy hierarchy) {

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
        return new StatisticsFrequencyDistribution(values, frequencies, count);
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
    public StatisticsContingencyTable getContingencyTable(int column1, int column2) {
        return getContingencyTable(column1, true, column2, true);
    }

    /**
     * Returns a contingency table for the given columns. This method assumes that the 
     * order of string data items can (and should) be derived from the hierarchies provided 
     * in the data definition (if any)
     * 
     * @param column1 The first column
     * @param size1   The maximal size in this dimension
     * @param column2 The second column
     * @param size2   The maximal size in this dimension
     * @return
     */
    public StatisticsContingencyTable getContingencyTable(int column1, 
                                                          int size1,
                                                          int column2,
                                                          int size2) {
        return getContingencyTable(column1, size1, true, column2, size2, true);
    }
    
    /**
     * Returns a contingency table for the given columns
     * 
     * @param column1                   The first column
     * @param size1                     The maximal size in this dimension
     * @param orderFromDefinition1      Indicates whether the order that should be assumed for string data items 
     *                                  can (and should) be derived from the hierarchy provided in the data 
     *                                  definition (if any)
     * @param column2                   The second column
     * @param size2                     The maximal size in this dimension
     * @param orderFromDefinition2      Indicates whether the order that should be assumed for string data items 
     *                                  can (and should) be derived from the hierarchy provided in the data 
     *                                  definition (if any)
     * @return
     */
    public StatisticsContingencyTable getContingencyTable(int column1, 
                                                          int size1,
                                                          boolean orderFromDefinition1,
                                                          int column2, 
                                                          int size2,
                                                          boolean orderFromDefinition2) {
        
        return getContingencyTable(column1, size1, getHierarchy(column1, orderFromDefinition1),
                                   column2, size2, getHierarchy(column2, orderFromDefinition2));
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
    public StatisticsContingencyTable getContingencyTable(int column1, boolean orderFromDefinition1,
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
    public StatisticsContingencyTable getContingencyTable(int column1,
                                                          Hierarchy hierarchy1,
                                                          int column2,
                                                          Hierarchy hierarchy2) {

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
        int max = Integer.MIN_VALUE;
        final Map<Entry, Integer> entries = new HashMap<Entry, Integer>();
        for (int row=0; row<handle.getNumRows(); row++){
            int index1 = indexes1.get(handle.getValue(row, column1));
            int index2 = indexes2.get(handle.getValue(row, column2));
            Entry entry = new Entry(index1, index2);
            Integer previous = entries.get(entry);
            int value = previous != null ? previous + 1 : 1;
            max = Math.max(max, value);
            entries.put(entry, value);
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
        return new StatisticsContingencyTable(values1, values2, count, (double)max/(double)count, iterator);
    }
    /**
     * Returns a contingency table for the given columns. The order for string data items is derived
     * from the provided hierarchies
     * 
     * @param column1     The first column
     * @param size1       The maximal size in this dimension
     * @param hierarchy1  The hierarchy for the first column, may be null
     * @param column2     The second column
     * @param size2       The maximal size in this dimension
     * @param hierarchy2  The hierarchy for the second column, may be null
     * @return
     */
    public StatisticsContingencyTable getContingencyTable(int column1,
                                                          int size1,
                                                          Hierarchy hierarchy1,
                                                          int column2,
                                                          int size2,
                                                          Hierarchy hierarchy2) {
        
        // Check
        if (size1 <= 0 || size2 <= 0) throw new IllegalArgumentException("Size must be > 0");

        // Obtain default table
        StatisticsContingencyTable table = getContingencyTable(column1,
                                                               hierarchy1,
                                                               column2,
                                                               hierarchy2);
        
        // Check if suitable
        if (table.values1.length<=size1 &&
            table.values2.length<=size2) {
            return table;
        }
        
        // Init
        String[] values1;
        String[] values2;
        double factor1;
        double factor2;
        
        // Compute factors and values
        if (table.values1.length>size1) {
            factor1 = (double)size1 / (double)table.values1.length;
            values1 = getScaledValues(table.values1, factor1);
        } else {
            factor1 = 1;
            values1 = table.values1;
        }
        if (table.values2.length>size2) {
            factor2 = (double)size2 / (double)table.values2.length;
            values2 = getScaledValues(table.values2, factor2);
        } else {
            factor2 = 1;
            values2 = table.values2;
        }
        
        // Create entry set
        final Map<Entry, Double> entries = new HashMap<Entry, Double>();
        Iterator<Entry> iter = table.iterator;
        double max = Double.MIN_VALUE;
        while (iter.hasNext()) {
            Entry old = iter.next();
            int index1 = (int)Math.round((double)old.value1 * factor1);
            int index2 = (int)Math.round((double)old.value2 * factor2);
            Entry entry = new Entry(index1, index2);
            Double previous = entries.get(entry);
            double value = previous != null ? previous + old.frequency : old.frequency;
            max = Math.max(value, max);
            entries.put(entry, value);
        }
                
        // Create iterator
        final Iterator<Entry> internal = entries.keySet().iterator();
        final Iterator<Entry> iterator = new Iterator<Entry>(){

            private Iterator<Entry> _internal = internal;
            private Map<Entry, Double> _entries = entries;
            
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
                e.frequency = _entries.get(e);
                return e;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        // Result result
        return new StatisticsContingencyTable(values1, values2, table.count, max, iterator);
    }
    
    /**
     * Scales the given string array
     * @param values
     * @param factor
     * @return
     */
    private String[] getScaledValues(String[] values, double factor) {
        AggregateFunction<String> function = AggregateFunction.forType(DataType.STRING).createSetFunction(); 
        String[] result = new String[(int)Math.round(factor * (double)values.length)];
        int previous = -1;
        List<String> toAggregate = new ArrayList<String>();
        for (int i=0; i<values.length; i++){
            int index = (int)Math.round((double)i * factor);
            if (index == previous) {
                toAggregate.add(values[i]);
            } else if (previous != -1){
                result[previous] = function.aggregate(toAggregate.toArray(new String[toAggregate.size()]));
                toAggregate.clear();
                previous = index;
            }
        }
        return result;
    }

    /**
     * Orders the given array by data type
     * 
     * @param array
     * @param type
     */
    private void sort(final String[] array, final DataType<?> type, final String suppressionString){
        GenericSorting.mergeSort(0, array.length, new IntComparator(){
            
            @Override
            public int compare(int arg0, int arg1) {
                try {
                    String s1 = array[arg0];
                    String s2 = array[arg1];
                    return (s1 == suppressionString && s2 == suppressionString) ? 0
                            : (s1 == suppressionString ? +1
                                    : (s2 == suppressionString ? -1
                                            : type.compare(s1, s2)));
                } catch (IllegalArgumentException | ParseException e) {
                    throw new RuntimeException("Some values seem to not conform to the data type", e);
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
                Integer order1 = order.get(array[arg0]);
                Integer order2 = order.get(array[arg1]);
                if (order1 == null || order2 == null) {
                    throw new RuntimeException("The hierarchy seems to not cover all data values");
                } else {
                    return order1.compareTo(order2);
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
    
    /**
     * Returns statistics about the equivalence classes
     * @return
     */
    public StatisticsEquivalenceClasses getEquivalenceClassStatistics(){
        return ecStatistics;
    }
}
