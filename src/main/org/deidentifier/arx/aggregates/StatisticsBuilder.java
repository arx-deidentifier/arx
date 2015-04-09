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

package org.deidentifier.arx.aggregates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandleStatistics;
import org.deidentifier.arx.DataHandleStatistics.InterruptHandler;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.DataType.ARXString;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.aggregates.StatisticsSummary.ScaleOfMeasure;
import org.deidentifier.arx.aggregates.StatisticsSummary.StatisticsSummaryOrdinal;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * A class offering basic descriptive statistics about data handles.
 *
 * @author Fabian Prasser
 */
public class StatisticsBuilder {
    
    /**
     * Local class for interrupts.
     *
     * @author Fabian Prasser
     */
    class ComputationInterruptedException extends RuntimeException {
        
        /**  TODO */
        private static final long serialVersionUID = 5339918851212367422L;

        /**
         * 
         *
         * @param message
         */
        public ComputationInterruptedException(String message) {
            super(message);
        }

        /**
         * 
         *
         * @param cause
         */
        public ComputationInterruptedException(Throwable cause) {
            super(cause);
        }
    }
    
    /** The equivalence class statistics. */
    private StatisticsEquivalenceClasses ecStatistics;
    
    /** The handle. */
    private DataHandleStatistics handle;
    
    /** The stop flag. */
    private volatile boolean interrupt;
    
    /**
     * Creates a new instance.
     *
     * @param handle
     * @param ecStatistics
     */
    public StatisticsBuilder(DataHandleStatistics handle,
                          StatisticsEquivalenceClasses ecStatistics) {
        this.ecStatistics = ecStatistics;
        this.handle = handle;
    }
    
    /**
     * Returns a contingency table for the given columns.
     *
     * @param column1 The first column
     * @param orderFromDefinition1 Indicates whether the order that should be assumed for string data items
     *            can (and should) be derived from the hierarchy provided in the data
     *            definition (if any)
     * @param column2 The second column
     * @param orderFromDefinition2 Indicates whether the order that should be assumed for string data items
     *            can (and should) be derived from the hierarchy provided in the data
     *            definition (if any)
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

        // Reset stop flag
        interrupt = false;
        
        // Init
        String[] values1 = getDistinctValuesOrdered(column1, hierarchy1);
        String[] values2 = getDistinctValuesOrdered(column2, hierarchy2);
        
        // Create maps of indexes
        Map<String, Integer> indexes1 = new HashMap<String, Integer>();
        for (int i=0; i<values1.length; i++){
            checkInterrupt();
            indexes1.put(values1[i], i);
        }
        Map<String, Integer> indexes2 = new HashMap<String, Integer>();
        for (int i=0; i<values2.length; i++){
            checkInterrupt();
            indexes2.put(values2[i], i);
        }
        
        // Create entry set
        int max = Integer.MIN_VALUE;
        final Map<Entry, Integer> entries = new HashMap<Entry, Integer>();
        for (int row=0; row<handle.getNumRows(); row++){
            checkInterrupt();
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

            private Map<Entry, Integer> _entries = entries;
            private Iterator<Entry> _internal = internal;
            
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
     * Returns a contingency table for the given columns.
     *
     * @param column1 The first column
     * @param size1 The maximal size in this dimension
     * @param orderFromDefinition1 Indicates whether the order that should be assumed for string data items
     *            can (and should) be derived from the hierarchy provided in the data
     *            definition (if any)
     * @param column2 The second column
     * @param size2 The maximal size in this dimension
     * @param orderFromDefinition2 Indicates whether the order that should be assumed for string data items
     *            can (and should) be derived from the hierarchy provided in the data
     *            definition (if any)
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

        // Reset stop flag
        interrupt = false;
        
        // Check
        if (size1 <= 0 || size2 <= 0) {
            throw new IllegalArgumentException("Size must be > 0");
        }

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
            values1 = getScaledValues(table.values1, size1);
        } else {
            factor1 = 1;
            values1 = table.values1;
        }
        if (table.values2.length>size2) {
            factor2 = (double)size2 / (double)table.values2.length;
            values2 = getScaledValues(table.values2, size2);
        } else {
            factor2 = 1;
            values2 = table.values2;
        }

        // Create entry set
        final Map<Entry, Double> entries = new HashMap<Entry, Double>();
        Iterator<Entry> iter = table.iterator;
        double max = 0d;
        while (iter.hasNext()) {
            checkInterrupt();
            Entry old = iter.next();
            int index1 = (int)Math.round((double)old.value1 * factor1);
            int index2 = (int)Math.round((double)old.value2 * factor2);
            index1 = index1 < size1 ? index1 : size1 - 1;
            index2 = index2 < size2 ? index2 : size2 - 1;
            Entry entry = new Entry(index1, index2);
            Double previous = entries.get(entry);
            double value = previous != null ? previous + old.frequency : old.frequency;
            max = Math.max(value, max);
            entries.put(entry, value);
        }
                
        // Create iterator
        final Iterator<Entry> internal = entries.keySet().iterator();
        final Iterator<Entry> iterator = new Iterator<Entry>(){

            private Map<Entry, Double> _entries = entries;
            private Iterator<Entry> _internal = internal;
            
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
     * Returns the distinct set of data items from the given column.
     *
     * @param column The column
     * @return
     */
    public String[] getDistinctValues(int column) {
        return this.handle.getDistinctValues(column, new InterruptHandler(){
            @Override
            public void checkInterrupt() {
                StatisticsBuilder.this.checkInterrupt();
            }
        });
    }

    /**
     * Returns an ordered list of the distinct set of data items from the given column. This method assumes 
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
     * Returns an ordered list of the distinct set of data items from the given column.
     *
     * @param column The column
     * @param orderFromDefinition Indicates whether the order that should be assumed for string data
     *            items can (and should) be derived from the hierarchy provided in the
     *            data definition (if any)
     * @return
     */
    public String[] getDistinctValuesOrdered(int column, boolean orderFromDefinition) {
        return getDistinctValuesOrdered(column, getHierarchy(column, orderFromDefinition));
    }

    /**
     * Returns an ordered list of the distinct set of data items from the given column. This method assumes 
     * that the order of string data items can (and should) be derived from the provided hierarchy
     * 
     * @param column The column
     * @param hierarchy The hierarchy, may be null
     * @return
     */
    public String[] getDistinctValuesOrdered(int column, Hierarchy hierarchy) {

        // Reset stop flag
        interrupt = false;
        
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
                checkInterrupt();
                // Make sure that only elements from the hierarchy
                // are added that are included in the data
                // TODO: Calling isValid is only a work-around
                if (baseType.isValid(element)) baseSet.add(element);
            }
            String[] baseArray = baseSet.toArray(new String[baseSet.size()]);
            sort(baseArray, handle.getBaseDataType(attribute), handle.getSuppressionString());
            Map<String, Integer> baseOrder = new HashMap<String, Integer>();
            for (int i = 0; i < baseArray.length; i++) {
                checkInterrupt();
                baseOrder.put(baseArray[i], i);
            }

            // Build higher level order from base order
            for (int i = 0; i < _hierarchy.length; i++) {
                checkInterrupt();
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
     * Returns statistics about the equivalence classes.
     *
     * @return
     */
    public StatisticsEquivalenceClasses getEquivalenceClassStatistics(){
        return ecStatistics;
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
     * Returns a frequency distribution for the values in the given column.
     *
     * @param column The column
     * @param orderFromDefinition Indicates whether the order that should be assumed for string data items
     *            can (and should) be derived from the hierarchy provided in the data
     *            definition (if any)
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

        // Reset stop flag
        interrupt = false;
        
        // Init
        String[] values = getDistinctValuesOrdered(column, hierarchy);
        double[] frequencies = new double[values.length];
        
        // Create map of indexes
        Map<String, Integer> indexes = new HashMap<String, Integer>();
        for (int i=0; i<values.length; i++){
            checkInterrupt();
            indexes.put(values[i], i);
        }
        
        // Count frequencies
        for (int row=0; row<handle.getNumRows(); row++){
            checkInterrupt();
            String value = handle.getValue(row, column);
            frequencies[indexes.get(value)]++;
        }
        
        // Divide by count
        int count = handle.getNumRows();
        for (int i=0; i<frequencies.length; i++){
            checkInterrupt();
            frequencies[i] /= (double)count;
        }
        
        // Return
        return new StatisticsFrequencyDistribution(values, frequencies, count);
    }
    
    /**
     * 
     * Returns an interruptible instance of this object.
     *
     * @return
     */
    public StatisticsBuilderInterruptible getInterruptibleInstance(){
        return new StatisticsBuilderInterruptible(handle, ecStatistics);
    }
    
    /**
     * Returns summary statistics for all attributes. 
     * 
     * @param listwiseDeletion A flag enabling list-wise deletion
     * @return
     */
    public <T> Map<String, StatisticsSummary<?>> getSummaryStatistics(boolean listwiseDeletion) {

        Map<String, DescriptiveStatistics> statistics = new HashMap<String, DescriptiveStatistics>();
        Map<String, StatisticsSummaryOrdinal> ordinal = new HashMap<String, StatisticsSummaryOrdinal>();
        Map<String, ScaleOfMeasure> scales = new HashMap<String, ScaleOfMeasure>();
        
        // Detect scales
        for (int col = 0; col < handle.getNumColumns(); col++) {
            
            // Meta
            String attribute = handle.getAttributeName(col);
            DataType<?> type = handle.getDataType(attribute);
            Class<?> clazz = type.getDescription().getWrappedClass();
            
            // Scale
            ScaleOfMeasure scale = ScaleOfMeasure.NOMINAL;
            if (clazz == Long.class || clazz == Double.class) {
                scale = ScaleOfMeasure.RATIO;
            } else if (clazz == Date.class) {
                scale = ScaleOfMeasure.INTERVAL;
            } else if (type instanceof ARXOrderedString) {
                scale = ScaleOfMeasure.ORDINAL;
            }
            
            // Try to replace nominal scale with ordinal scale based on base data type
            if (scale == ScaleOfMeasure.NOMINAL && handle.getGeneralization(attribute) != 0) {
                if (!(handle.getBaseDataType(attribute) instanceof ARXString) &&
                      getHierarchy(col, true) != null) {
                    scale = ScaleOfMeasure.ORDINAL;
                }
            }
            
            // Store
            scales.put(attribute, scale);
            statistics.put(attribute, new DescriptiveStatistics());
            ordinal.put(attribute, getSummaryStatisticsOrdinal(handle.getGeneralization(attribute),
                                                               handle.getDataType(attribute),
                                                               handle.getBaseDataType(attribute),
                                                               getHierarchy(col, true)));
        }
        
        // Compute summary statistics
        for (int row = 0; row < handle.getNumRows(); row++) {
            
            // Check, if we should include this row
            boolean include = true;
            if (listwiseDeletion) {
                for (int col = 0; col < handle.getNumColumns(); col++) {
                    if (DataType.isNull(handle.getValue(row, col))) {
                        include = false;
                        break;
                    }
                }
            }
            
            // Check
            checkInterrupt();
            
            // If yes, add
            if (include) {
                
                // For each column
                for (int col = 0; col < handle.getNumColumns(); col++) {
                    
                    // Meta
                    String value = handle.getValue(row, col);
                    String attribute = handle.getAttributeName(col);
                    DataType<?> type = handle.getDataType(attribute);
                    Class<?> clazz = type.getDescription().getWrappedClass();
                    
                    // Analyze
                    if (value != null && !DataType.isNull(value)) {
                        if (clazz == Long.class || clazz == Double.class) {
                            statistics.get(attribute).addValue(((Number)type.parse(value)).doubleValue());
                            ordinal.get(attribute).addValue(value);
                        } else if (clazz == Date.class) {
                            statistics.get(attribute).addValue(((Date)type.parse(value)).getTime());
                            ordinal.get(attribute).addValue(value);
                        } else {
                            ordinal.get(attribute).addValue(value);
                        }
                    }
                }
            }
        }

        // Convert
        Map<String, StatisticsSummary<?>> result = new HashMap<String, StatisticsSummary<?>>();
        for (int col = 0; col < handle.getNumColumns(); col++) {
            
            // Check
            checkInterrupt();
            
            // Depending on scale
            String attribute = handle.getAttributeName(col);
            ScaleOfMeasure scale = scales.get(attribute);
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>) handle.getDataType(attribute);
            ordinal.get(attribute).analyze();
            if (scale == ScaleOfMeasure.NOMINAL) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                result.put(attribute, new StatisticsSummary<T>(ScaleOfMeasure.NOMINAL, 
                                                            stats.getNumberOfMeasures(), 
                                                            stats.getMode(),
                                                            type.parse(stats.getMode())));
            } else if (scale == ScaleOfMeasure.ORDINAL) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                result.put(attribute, new StatisticsSummary<T>(ScaleOfMeasure.ORDINAL, 
                                                            stats.getNumberOfMeasures(), 
                                                            stats.getMode(),
                                                            type.parse(stats.getMode()),
                                                            stats.getMedian(),
                                                            type.parse(stats.getMedian()),
                                                            stats.getMin(),
                                                            type.parse(stats.getMin()),
                                                            stats.getMax(),
                                                            type.parse(stats.getMax())));
            } else if (scale == ScaleOfMeasure.INTERVAL) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                DescriptiveStatistics stats2 = statistics.get(attribute);
                boolean isPeriod = type.getDescription().getWrappedClass() == Date.class;
                
                // TODO: Something is wrong with commons math's kurtosis
                double kurtosis = stats2.getKurtosis();
                kurtosis = kurtosis < 0d ? Double.NaN : kurtosis;
                double range = stats2.getMax() - stats2.getMin();
                double stddev = Math.sqrt(stats2.getVariance());
                
                result.put(attribute, new StatisticsSummary<T>(ScaleOfMeasure.INTERVAL, 
                                                            stats.getNumberOfMeasures(), 
                                                            stats.getMode(),
                                                            type.parse(stats.getMode()),
                                                            stats.getMedian(),
                                                            type.parse(stats.getMedian()),
                                                            stats.getMin(),
                                                            type.parse(stats.getMin()),
                                                            stats.getMax(),
                                                            type.parse(stats.getMax()),
                                                            toString(type, stats2.getMean(), false, false),
                                                            toValue(type, stats2.getMean()),
                                                            stats2.getMean(),
                                                            toString(type, stats2.getVariance(), isPeriod, true),
                                                            toValue(type, stats2.getVariance()),
                                                            stats2.getVariance(),
                                                            toString(type, stats2.getPopulationVariance(), isPeriod, true),
                                                            toValue(type, stats2.getPopulationVariance()),
                                                            stats2.getPopulationVariance(),
                                                            toString(type, stddev, isPeriod, false),
                                                            toValue(type, stddev),
                                                            stddev,
                                                            toString(type, range, isPeriod, false),
                                                            toValue(type, range),
                                                            stats2.getMax() - stats2.getMin(),
                                                            toString(type, kurtosis, isPeriod, false),
                                                            toValue(type, kurtosis),
                                                            kurtosis));
            } else if (scale == ScaleOfMeasure.RATIO) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                DescriptiveStatistics stats2 = statistics.get(attribute);

                // TODO: Something is wrong with commons math's kurtosis
                double kurtosis = stats2.getKurtosis();
                kurtosis = kurtosis < 0d ? Double.NaN : kurtosis;
                double range = stats2.getMax() - stats2.getMin();
                double stddev = Math.sqrt(stats2.getVariance());
                
                result.put(attribute, new StatisticsSummary<T>(ScaleOfMeasure.RATIO, 
                                                            stats.getNumberOfMeasures(), 
                                                            stats.getMode(),
                                                            type.parse(stats.getMode()),
                                                            stats.getMedian(),
                                                            type.parse(stats.getMedian()),
                                                            stats.getMin(),
                                                            type.parse(stats.getMin()),
                                                            stats.getMax(),
                                                            type.parse(stats.getMax()),
                                                            toString(type, stats2.getMean(), false, false),
                                                            toValue(type, stats2.getMean()),
                                                            stats2.getMean(),
                                                            toString(type, stats2.getVariance(), false, false),
                                                            toValue(type, stats2.getVariance()),
                                                            stats2.getVariance(),
                                                            toString(type, stats2.getPopulationVariance(), false, false),
                                                            toValue(type, stats2.getPopulationVariance()),
                                                            stats2.getPopulationVariance(),
                                                            toString(type, stddev, false, false),
                                                            toValue(type, stddev),
                                                            stddev,
                                                            toString(type, range, false, false),
                                                            toValue(type, range),
                                                            range,
                                                            toString(type, kurtosis, false, false),
                                                            toValue(type, kurtosis),
                                                            kurtosis,
                                                            toString(type, stats2.getGeometricMean(), false, false),
                                                            toValue(type, stats2.getGeometricMean()),
                                                            stats2.getGeometricMean()));
            }
        }
        
        return result;
    }
    
    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt(){
        if (interrupt) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }

    /**
     * Returns the appropriate hierarchy, if any.
     *
     * @param column
     * @param orderFromDefinition
     * @return
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
     * Scales the given string array.
     *
     * @param values
     * @param length The resulting length
     * @return
     */
    private String[] getScaledValues(String[] values, int length) {
        
        // Init
        AggregateFunction<String> function = AggregateFunction.forType(DataType.STRING).createSetFunction();
        double factor = (double)length / (double)values.length;
        String[] result = new String[length];
        
        // Aggregate
        int previous = 0;
        List<String> toAggregate = new ArrayList<String>();
        for (int i=0; i<values.length; i++){
            
            checkInterrupt();
            
            int index = (int)Math.round((double)i * factor);
            index = index < length ? index : length -1;
            
            if (index != previous) {
                result[previous] = function.aggregate(toAggregate.toArray(new String[toAggregate.size()]));
                toAggregate.clear();
                previous = index;
            }
            toAggregate.add(values[i]);
        }
        
        result[length-1] = function.aggregate(toAggregate.toArray(new String[toAggregate.size()]));
        return result;
    }

    /**
     * Returns a summary statistics object for the given attribute
     * @param generalization
     * @param dataType
     * @param baseDataType
     * @param hierarchy
     * @return
     */
    private <U,V> StatisticsSummaryOrdinal getSummaryStatisticsOrdinal(   final int generalization,
                                                                          final DataType<U> dataType,
                                                                          final DataType<V> baseDataType,
                                                                          final Hierarchy hierarchy) {

        // TODO: It would be cleaner to return an ARXOrderedString for generalized variables
        // TODO: that have a suitable data type directly from the DataHandle 
        if (generalization == 0 || !(dataType instanceof ARXString)) {
            return new StatisticsSummaryOrdinal(dataType);
        } else if (baseDataType instanceof ARXString) {
            return new StatisticsSummaryOrdinal(dataType);
        } else if (hierarchy == null){
            return new StatisticsSummaryOrdinal(dataType);
        } else {
            final String[][] array = hierarchy.getHierarchy();
            final Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < array.length; i++) {
                map.put(array[i][generalization], array[i][0]);
            }
            return new StatisticsSummaryOrdinal(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    V _o1 = null;
                    try {
                        _o1 = baseDataType.parse(map.get(o1));
                    } catch (Exception e) {
                        // Nothing to do
                    }
                    V _o2 = null; 
                    try {
                        _o2 = baseDataType.parse(map.get(o2));
                    } catch (Exception e) {
                        // Nothing to do
                    }
                    try {
                        return baseDataType.compare(_o1, _o2);
                    } catch (Exception e) {
                        return 0;
                    }
                }
            });
        }
    }
    

    /**
     * Orders the given array by data type.
     *
     * @param array
     * @param type
     * @param suppressionString
     */
    private void sort(final String[] array, final DataType<?> type, final String suppressionString){
        GenericSorting.mergeSort(0, array.length, new IntComparator(){
            
            @Override
            public int compare(int arg0, int arg1) {
                checkInterrupt();
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
     * Orders the given array by the given sort order.
     *
     * @param array
     * @param order
     */
    private void sort(final String[] array, final Map<String, Integer> order){
        GenericSorting.mergeSort(0, array.length, new IntComparator(){
            @Override
            public int compare(int arg0, int arg1) {
                checkInterrupt();
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
     * Used for building summary statistics 
     * @param type
     * @param value
     * @param isPeriod Defines whether the parameter is a time period
     * @param isSquare Defines whether the period is a squared period
     * @return
     */
    @SuppressWarnings("unchecked")
    private String toString(DataType<?> type, double value, boolean isPeriod, boolean isSquare) {
        
        // Handle corner cases
        if (Double.isNaN(value)) {
            return "Not available";
        } else if (Double.isInfinite(value)) {
            if (value < 0) {
                return "-Infinity";
            } else {
                return "+Infinity";
            }
        }
        
        // Handle periods
        if (isPeriod) {
            
            // Init
            long SECONDS = 1000;
            long MINUTES = 60 * SECONDS;
            long HOURS = 60 * MINUTES;
            long DAYS = 24 * HOURS;
            long WEEKS = 7 * DAYS;
            
            // Square
            if (isSquare) {
                SECONDS *= SECONDS;
                MINUTES *= MINUTES;
                HOURS *= HOURS;
                DAYS *= DAYS;
                WEEKS *= WEEKS;
            }
            
            // Compute
            final int weeks = (int)(value / WEEKS);
            value = value % WEEKS;
            final int days = (int)(value / DAYS);
            value = value % DAYS;
            final int hours = (int)(value / HOURS);
            value = value % HOURS;
            final int minutes = (int)(value / MINUTES);
            value = value % MINUTES;
            final int seconds = (int)(value / SECONDS);
            value = value % SECONDS;
            final int milliseconds = (int)(value);
            
            // Convert
            StringBuilder builder = new StringBuilder();
            if (weeks != 0) builder.append(weeks).append(isSquare ? "w^2, " : "w, ");
            if (days != 0) builder.append(days).append(isSquare ? "d^2, " : "d, ");
            if (hours != 0) builder.append(hours).append(isSquare ? "h^2, " : "h, ");
            if (minutes != 0) builder.append(minutes).append(isSquare ? "m^2, " : "m, ");
            if (seconds != 0) builder.append(seconds).append(isSquare ? "s^2, " : "s, ");
            builder.append(milliseconds).append(isSquare ? "ms^2" : "ms");
            
            // Return
            return builder.toString();
            
        } 
        
        // Handle data types
        Class<?> clazz = type.getDescription().getWrappedClass();
        if (clazz == Long.class || clazz == Double.class) {
            return String.valueOf(value);
        } else if (clazz == Date.class) {
            return ((DataType<Date>) type).format(new Date((long) value));
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * Used for building summary statistics 
     * @param type
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T toValue(DataType<T> type, double value) {
        
        // Handle corner cases
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        
        // Handle data types
        Class<?> clazz = type.getDescription().getWrappedClass();
        if (clazz == Long.class) {
            return (T)Long.valueOf((long)value);
        } else if (clazz == Double.class) {
            return (T)Double.valueOf(value);
        } else if (clazz == Date.class) {
            return (T)new Date((long) value);
        } else {
            return (T)String.valueOf(value);
        }
    }
    
    /**
     * Stops all computations. May lead to exceptions being thrown. Use with care.
     */
    void interrupt(){
        this.interrupt = true;
    }
}
