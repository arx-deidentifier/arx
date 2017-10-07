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
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.DataHandleInternal.InterruptHandler;
import org.deidentifier.arx.DataScale;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXString;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.aggregates.StatisticsSummary.StatisticsSummaryOrdinal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * A class offering basic descriptive statistics about data handles.
 *
 * @author Fabian Prasser
 */
public class StatisticsBuilder {

    /** The handle. */
    private DataHandleInternal      handle;

    /** The stop flag. */
    private volatile WrappedBoolean interrupt = new WrappedBoolean(false);

    /** Model */
    private final WrappedInteger    progress  = new WrappedInteger();

    /**
     * Creates a new instance.
     *
     * @param handle
     */
    public StatisticsBuilder(DataHandleInternal handle) {
        this.handle = handle;
    }
    
    /**
     * Creates a new set of statistics for the given classification task
     * @param clazz - The class attribute
     * @param config - The configuration
     * @throws ParseException
     */
    public StatisticsClassification getClassificationPerformance(String clazz, ARXClassificationConfiguration<?> config) throws ParseException {
        return getClassificationPerformance(new String[] {}, clazz, config);
    }

    /**
     * Creates a new set of statistics for the given classification task
     * @param features - The feature attributes
     * @param clazz - The class attributes
     * @param config - The configuration
     * @throws ParseException
     */
    public StatisticsClassification getClassificationPerformance(String[] features,
                                                                 String clazz,
                                                                 ARXClassificationConfiguration<?> config) throws ParseException {
    
        // Return
        return getClassificationPerformance(features, clazz, config, null);
    }
    
    /**
     * Creates a new set of statistics for the given classification task
     * @param features - The feature attributes
     * @param clazz - The class attributes
     * @param config - The configuration
     * @param scaling - Feature scaling
     * @throws ParseException
     */
    public StatisticsClassification getClassificationPerformance(String[] features,
                                                                 String clazz,
                                                                 ARXClassificationConfiguration<?> config,
                                                                 ARXFeatureScaling scaling) throws ParseException {
    
        // Reset stop flag
        interrupt.value = false;
        progress.value = 0;
        
        // Return
        return new StatisticsClassification(handle.getAssociatedInput(), 
                                            handle, 
                                            features, 
                                            clazz, 
                                            config, 
                                            scaling,
                                            interrupt, 
                                            progress);
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
     * Returns a contingency table for the given columns. This method assumes that the
     * order of string data items will be derived from the hierarchies provided
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
     * Returns a contingency table for the given columns. This method assumes that the
     * order of string data items can (and should) be derived from the hierarchies provided
     * in the data definition (if any)
     * 
     * @param column1 The first column
     * @param size1 The maximal size in this dimension
     * @param column2 The second column
     * @param size2 The maximal size in this dimension
     * @return
     */
    public StatisticsContingencyTable getContingencyTable(int column1,
                                                          int size1,
                                                          int column2,
                                                          int size2) {
        return getContingencyTable(column1, size1, true, column2, size2, true);
    }
    
    /**
     * Returns a contingency table for the given columns. The order for string data items is derived
     * from the provided hierarchies
     * 
     * @param column1 The first column
     * @param size1 The maximal size in this dimension
     * @param hierarchy1 The hierarchy for the first column, may be null
     * @param column2 The second column
     * @param size2 The maximal size in this dimension
     * @param hierarchy2 The hierarchy for the second column, may be null
     * @return
     */
    public StatisticsContingencyTable getContingencyTable(int column1,
                                                          int size1,
                                                          String[][] hierarchy1,
                                                          int column2,
                                                          int size2,
                                                          String[][] hierarchy2) {
        
        // Reset stop flag
        interrupt.value = false;
        
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
        if (table.values1.length <= size1 &&
            table.values2.length <= size2) {
            return table;
        }
        
        // Init
        String[] values1;
        String[] values2;
        double factor1;
        double factor2;
        
        // Compute factors and values
        if (table.values1.length > size1) {
            factor1 = (double) size1 / (double) table.values1.length;
            values1 = getScaledValues(table.values1, size1);
        } else {
            factor1 = 1;
            values1 = table.values1;
        }
        if (table.values2.length > size2) {
            factor2 = (double) size2 / (double) table.values2.length;
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
            int index1 = (int) Math.round((double) old.value1 * factor1);
            int index2 = (int) Math.round((double) old.value2 * factor2);
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
        final Iterator<Entry> iterator = new Iterator<Entry>() {
            
            private Map<Entry, Double> _entries  = entries;
            private Iterator<Entry>    _internal = internal;
            
            @Override
            public boolean hasNext() {
                
                if (_internal == null) return false;
                boolean result = _internal.hasNext();
                
                // Try to release resources as early as possible
                if (!result) {
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
     * Returns a contingency table for the given columns. The order for string data items is derived
     * from the provided hierarchies
     * 
     * @param column1 The first column
     * @param hierarchy1 The hierarchy for the first column, may be null
     * @param column2 The second column
     * @param hierarchy2 The hierarchy for the second column, may be null
     * @return
     */
    public StatisticsContingencyTable getContingencyTable(int column1,
                                                          String[][] hierarchy1,
                                                          int column2,
                                                          String[][] hierarchy2) {
        
        // Reset stop flag
        interrupt.value = false;
        
        // Init
        String[] values1 = getDistinctValuesOrdered(column1, hierarchy1);
        String[] values2 = getDistinctValuesOrdered(column2, hierarchy2);
        
        // Create maps of indexes
        Map<String, Integer> indexes1 = new HashMap<String, Integer>();
        for (int i = 0; i < values1.length; i++) {
            checkInterrupt();
            indexes1.put(values1[i], i);
        }
        Map<String, Integer> indexes2 = new HashMap<String, Integer>();
        for (int i = 0; i < values2.length; i++) {
            checkInterrupt();
            indexes2.put(values2[i], i);
        }
        
        // Create entry set
        int max = Integer.MIN_VALUE;
        final Map<Entry, Integer> entries = new HashMap<Entry, Integer>();
        for (int row = 0; row < handle.getNumRows(); row++) {
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
        final Iterator<Entry> iterator = new Iterator<Entry>() {
            
            private Map<Entry, Integer> _entries  = entries;
            private Iterator<Entry>     _internal = internal;
            
            @Override
            public boolean hasNext() {
                
                if (_internal == null) return false;
                boolean result = _internal.hasNext();
                
                // Try to release resources as early as possible
                if (!result) {
                    _internal = null;
                    _entries = null;
                }
                return result;
            }
            
            @Override
            public Entry next() {
                if (_internal == null) return null;
                Entry e = _internal.next();
                e.frequency = (double) _entries.get(e) / (double) count;
                return e;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        
        // Result result
        return new StatisticsContingencyTable(values1, values2, count, (double) max / (double) count, iterator);
    }
    
    /**
     * Returns the distinct set of data items from the given column.
     *
     * @param column The column
     * @return
     */
    public String[] getDistinctValues(int column) {
        return this.handle.getDistinctValues(column, new InterruptHandler() {
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
    public String[] getDistinctValuesOrdered(int column, String[][] hierarchy) {
        
        // Reset stop flag
        interrupt.value = false;
        
        // Obtain list and data type
        String[] list = getDistinctValues(column);
        String attribute = handle.getAttributeName(column);
        DataType<?> datatype = handle.getDataType(attribute);
        int level = handle.getGeneralization(attribute);
        
        progress.value = 20;
        
        // Sort by data type
        if ((datatype instanceof DataTypeWithRatioScale) || hierarchy == null || level == 0) {
            
            sort(list, datatype);
            
        // Sort by hierarchy and data type
        } else {
            
            // Build order directly from the hierarchy
            Map<String, Integer> order = new HashMap<String, Integer>();
            int max = 0; // The order to use for the suppression string
            
            // Create base order
            Set<String> baseSet = new HashSet<String>();
            DataType<?> baseType = handle.getBaseDataType(attribute);
            for (int i = 0; i < hierarchy.length; i++) {
                String element = hierarchy[i][0];
                checkInterrupt();
                // Make sure that only elements from the hierarchy
                // are added that are included in the data
                if (baseType.isValid(element)) baseSet.add(element);
            }
            String[] baseArray = baseSet.toArray(new String[baseSet.size()]);
            sort(baseArray, handle.getBaseDataType(attribute));
            Map<String, Integer> baseOrder = new HashMap<String, Integer>();
            for (int i = 0; i < baseArray.length; i++) {
                checkInterrupt();
                baseOrder.put(baseArray[i], i);
            }
            
            // Handle optimized handles
            int lower = handle.isOptimized() ? 0 : level;
            int upper = handle.isOptimized() ? hierarchy[0].length : level + 1;
            
            // Build higher level order from base order
            for (int i = 0; i < hierarchy.length; i++) {
                
                // Check
                checkInterrupt();
                
                // Add data from all relevant levels
                for (int j = lower; j < upper; j++) {
                    if (!order.containsKey(hierarchy[i][j])) {
                        Integer position = baseOrder.get(hierarchy[i][0]);
                        if (position != null) {
                            order.put(hierarchy[i][j], position);
                            max = Math.max(position, max) + 1;
                        }
                    }
                }
            }
            
            // Add suppression string
            order.put(DataType.ANY_VALUE, max);

            // Progress
            progress.value = 30;
            
            // Check if all values are covered by the order
            boolean allCovered = true;
            for (String value : list) {
                if (!order.containsKey(value)) {
                    allCovered = false;
                    break;
                }
            }
            
            // Progress
            progress.value = 35;
            
            // Sort according to the given order
            if (allCovered) {
                sort(list, order);
                
            // Sort lexicographically
            } else {
                sort(list);
            }
        }
        
        progress.value = 40;
        
        // Done
        return list;
    }
    
    /**
     * Returns statistics about the equivalence classes.
     *
     * @return
     */
    public StatisticsEquivalenceClasses getEquivalenceClassStatistics() {

        // Reset stop flag
        interrupt.value = false;

        // Prepare
        Set<String> attributes = handle.getDefinition().getQuasiIdentifyingAttributes();
        final int[] indices = new int[attributes.size()];
        int index = 0;
        for (int column = 0; column < handle.getNumColumns(); column++) {
            if (attributes.contains(handle.getAttributeName(column))) {
                indices[index++] = column;
            }
        }

        // Calculate equivalence classes
        int capacity = handle.getNumRows() / 10;
        capacity = capacity > 10 ? capacity : 10;
        Groupify<TupleWrapper> map = new Groupify<TupleWrapper>(capacity);
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {

            TupleWrapper tuple = new TupleWrapper(handle, indices, row, false);
            map.add(tuple);
            checkInterrupt();
        }

        // Now compute the following values
        double averageEquivalenceClassSize = 0d;
        double averageEquivalenceClassSizeIncludingOutliers = 0d;
        int maximalEquivalenceClassSize = Integer.MIN_VALUE;
        int maximalEquivalenceClassSizeIncludingOutliers = Integer.MIN_VALUE;
        int minimalEquivalenceClassSize = Integer.MAX_VALUE;
        int minimalEquivalenceClassSizeIncludingOutliers = Integer.MAX_VALUE;
        int numberOfEquivalenceClasses = 0;
        int numberOfEquivalenceClassesIncludingOutliers = map.size();
        int numberOfTuples = 0;
        int numberOfOutlyingTuples = 0;
         
        // Let's do it
        boolean containsOutliers = false;
        Group<TupleWrapper> element = map.first();
        while (element != null) {
            
            checkInterrupt();
            maximalEquivalenceClassSizeIncludingOutliers = Math.max(element.getCount(), maximalEquivalenceClassSizeIncludingOutliers);
            minimalEquivalenceClassSizeIncludingOutliers = Math.min(element.getCount(), minimalEquivalenceClassSizeIncludingOutliers);
            averageEquivalenceClassSizeIncludingOutliers += element.getCount();
            numberOfTuples += element.getCount();
            
            if (!element.getElement().isOutlier()) {
                
                maximalEquivalenceClassSize = Math.max(element.getCount(), maximalEquivalenceClassSize);
                minimalEquivalenceClassSize = Math.min(element.getCount(), minimalEquivalenceClassSize);
                averageEquivalenceClassSize += element.getCount();
                
            } else {
                
                containsOutliers = true;
                // All suppressed records will collapse into a single group, so we can use the "=" assignment operator here
                numberOfOutlyingTuples = element.getCount();
            }
            
            element = element.next();
        }
        
        numberOfEquivalenceClasses = numberOfEquivalenceClassesIncludingOutliers;
        if (containsOutliers) {
            numberOfEquivalenceClasses -= 1;
        }
        
        averageEquivalenceClassSize /= (double)numberOfEquivalenceClasses;
        averageEquivalenceClassSizeIncludingOutliers /= (double)numberOfEquivalenceClassesIncludingOutliers;
        
        // Fix corner cases
        if (numberOfEquivalenceClasses == 0) {
            averageEquivalenceClassSize = 0;
            maximalEquivalenceClassSize = 0;
            minimalEquivalenceClassSize = 0;
        }

        // And return
        return new StatisticsEquivalenceClasses(averageEquivalenceClassSize,
                                                averageEquivalenceClassSizeIncludingOutliers,
                                                maximalEquivalenceClassSize,
                                                maximalEquivalenceClassSizeIncludingOutliers,
                                                minimalEquivalenceClassSize,
                                                minimalEquivalenceClassSizeIncludingOutliers,
                                                numberOfEquivalenceClasses,
                                                numberOfEquivalenceClassesIncludingOutliers,
                                                numberOfTuples,
                                                numberOfOutlyingTuples);
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
     *                            should be derived from the hierarchy provided in the data definition (if any)
     * @return
     */
    public StatisticsFrequencyDistribution getFrequencyDistribution(int column, boolean orderFromDefinition) {
        return getFrequencyDistribution(column, getHierarchy(column, orderFromDefinition));
    }

    /**
     * Returns a frequency distribution for the values in the given column. The order for string data items
     * is derived from the provided hierarchy
     * 
     * @param column The column
     * @param hierarchy The hierarchy, may be null
     * @return
     */
    public StatisticsFrequencyDistribution getFrequencyDistribution(int column, String[][] hierarchy) {

        // Reset stop flag
        interrupt.value = false;
        
        // Init
        String[] values = getDistinctValuesOrdered(column, hierarchy);
        double[] frequencies = new double[values.length];

        // Create map of indexes
        Map<String, Integer> indexes = new HashMap<String, Integer>();
        for (int i = 0; i < values.length; i++) {
            checkInterrupt();
            indexes.put(values[i], i);
        }
        
        progress.value = 60;
        
        // Count frequencies
        for (int row = 0; row < handle.getNumRows(); row++) {
            checkInterrupt();
            String value = handle.getValue(row, column);
            frequencies[indexes.get(value)]++;
        }
        
        progress.value = 80;
        
        // Divide by count
        int count = handle.getNumRows();
        for (int i = 0; i < frequencies.length; i++) {
            checkInterrupt();
            frequencies[i] /= (double) count;
        }
        
        progress.value = 100;
        
        // Return
        return new StatisticsFrequencyDistribution(values, frequencies, count);
    }

    /**
     * 
     * Returns an interruptible instance of this object.
     *
     * @return
     */
    public StatisticsBuilderInterruptible getInterruptibleInstance() {
        return new StatisticsBuilderInterruptible(handle);
    }

    /**
     * Returns data quality according to various models.
     * 
     * @return
     */
    public StatisticsQuality getQualityStatistics() {
        
        // Build and return
        return getQualityStatistics(this.handle.getHandle());
    }

    /**
     * Returns data quality according to various models. This is a special variant of 
     * the method supporting arbitrary user-defined outputs.
     * 
     * @param output
     * @return
     */
    public StatisticsQuality getQualityStatistics(DataHandle output) {

        // Reset stop flag
        interrupt.value = false;
        progress.value = 0;

        // Prepare
        DataHandleInternal input = this.handle.getAssociatedInput();
        ARXConfiguration config = this.handle.getConfiguration();
        
        // Very basic check        
        if (output.getNumRows() != input.getNumRows() ||
            output.getNumColumns() != input.getNumColumns()) {
            throw new IllegalArgumentException("Input and output do not match");
        }

        // Build and return
        return new StatisticsQuality(input.getHandle(), output, config, interrupt, progress);
    }
    
    /**
     * Returns summary statistics for all attributes.
     * 
     * @param listwiseDeletion A flag enabling list-wise deletion
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Map<String, StatisticsSummary<?>> getSummaryStatistics(boolean listwiseDeletion) {

        // Reset stop flag
        interrupt.value = false;
        
        Map<String, DescriptiveStatistics> statistics = new HashMap<String, DescriptiveStatistics>();
        Map<String, StatisticsSummaryOrdinal> ordinal = new HashMap<String, StatisticsSummaryOrdinal>();
        Map<String, DataScale> scales = new HashMap<String, DataScale>();
        Map<String, GeometricMean> geomean = new HashMap<String, GeometricMean>();
        
        // Detect scales
        for (int col = 0; col < handle.getNumColumns(); col++) {
            
            // Meta
            String attribute = handle.getAttributeName(col);
            DataType<?> type = handle.getDataType(attribute);
            
            // Scale
            DataScale scale = type.getDescription().getScale();
            
            // Try to replace nominal scale with ordinal scale based on base data type
            if (scale == DataScale.NOMINAL && handle.getGeneralization(attribute) != 0) {
                if (!(handle.getBaseDataType(attribute) instanceof ARXString) &&
                    getHierarchy(col, true) != null) {
                    scale = DataScale.ORDINAL;
                }
            }
            
            // Store
            scales.put(attribute, scale);
            statistics.put(attribute, new DescriptiveStatistics());
            geomean.put(attribute, new GeometricMean());
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
                    if (handle.isOutlier(row) || DataType.isNull(handle.getValue(row, col))) {
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
                    
                    // Analyze
                    if (!DataType.isAny(value) && !DataType.isNull(value)) {
                        ordinal.get(attribute).addValue(value);
                        if (type instanceof DataTypeWithRatioScale) {
                            double doubleValue = ((DataTypeWithRatioScale) type).toDouble(type.parse(value));
                            statistics.get(attribute).addValue(doubleValue);
                            geomean.get(attribute).increment(doubleValue + 1d);
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
            DataScale scale = scales.get(attribute);
            DataType<T> type = (DataType<T>) handle.getDataType(attribute);
            ordinal.get(attribute).analyze();
            if (scale == DataScale.NOMINAL) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                result.put(attribute, new StatisticsSummary<T>(DataScale.NOMINAL,
                                                               stats.getNumberOfMeasures(),
                                                               stats.getDistinctNumberOfValues(),
                                                               stats.getMode(),
                                                               type.parse(stats.getMode())));
            } else if (scale == DataScale.ORDINAL) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                result.put(attribute, new StatisticsSummary<T>(DataScale.ORDINAL,
                                                               stats.getNumberOfMeasures(),
                                                               stats.getDistinctNumberOfValues(),
                                                               stats.getMode(),
                                                               type.parse(stats.getMode()),
                                                               stats.getMedian(),
                                                               type.parse(stats.getMedian()),
                                                               stats.getMin(),
                                                               type.parse(stats.getMin()),
                                                               stats.getMax(),
                                                               type.parse(stats.getMax())));
            } else if (scale == DataScale.INTERVAL) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                DescriptiveStatistics stats2 = statistics.get(attribute);
                boolean isPeriod = type.getDescription().getWrappedClass() == Date.class;
                
                // TODO: Something is wrong with commons math's kurtosis
                double kurtosis = stats2.getKurtosis();
                kurtosis = kurtosis < 0d ? Double.NaN : kurtosis;
                double range = stats2.getMax() - stats2.getMin();
                double stddev = Math.sqrt(stats2.getVariance());
                
                result.put(attribute, new StatisticsSummary<T>(DataScale.INTERVAL,
                                                               stats.getNumberOfMeasures(),
                                                               stats.getDistinctNumberOfValues(),
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
            } else if (scale == DataScale.RATIO) {
                StatisticsSummaryOrdinal stats = ordinal.get(attribute);
                DescriptiveStatistics stats2 = statistics.get(attribute);
                GeometricMean geo = geomean.get(attribute);
                
                // TODO: Something is wrong with commons math's kurtosis
                double kurtosis = stats2.getKurtosis();
                kurtosis = kurtosis < 0d ? Double.NaN : kurtosis;
                double range = stats2.getMax() - stats2.getMin();
                double stddev = Math.sqrt(stats2.getVariance());
                
                result.put(attribute, new StatisticsSummary<T>(DataScale.RATIO,
                                                               stats.getNumberOfMeasures(),
                                                               stats.getDistinctNumberOfValues(),
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
                                                               toString(type, geo.getResult() - 1d, false, false),
                                                               toValue(type, geo.getResult() - 1d),
                                                               stats2.getGeometricMean()));
            }
        }
        
        return result;
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (interrupt.value) {
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
    private String[][] getHierarchy(int column, boolean orderFromDefinition) {
        
        // Init
        final String attribute = handle.getAttributeName(column);
        final String[][] hierarchy = handle.getDefinition().getHierarchy(attribute);
        final DataType<?> datatype = handle.getDataType(attribute);
        
        // Check if hierarchy available
        if (orderFromDefinition && datatype instanceof ARXString && hierarchy != null) {
            return hierarchy;
        } else {
            return null;
        }
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
        double factor = (double) length / (double) values.length;
        String[] result = new String[length];
        
        // Aggregate
        int previous = 0;
        List<String> toAggregate = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            
            checkInterrupt();
            
            int index = (int) Math.round((double) i * factor);
            index = index < length ? index : length - 1;
            
            if (index != previous) {
                result[previous] = function.aggregate(toAggregate.toArray(new String[toAggregate.size()]));
                toAggregate.clear();
                previous = index;
            }
            toAggregate.add(values[i]);
        }
        
        result[length - 1] = function.aggregate(toAggregate.toArray(new String[toAggregate.size()]));
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
    private <U, V> StatisticsSummaryOrdinal<?> getSummaryStatisticsOrdinal(final int generalization,
                                                                           final DataType<U> dataType,
                                                                           final DataType<V> baseDataType,
                                                                           final String[][] hierarchy) {
        
        // TODO: It would be cleaner to return an ARXOrderedString for generalized variables
        //       that have a suitable data type directly obtained from the DataHandle
        if (generalization == 0 || !(dataType instanceof ARXString)) {
            return new StatisticsSummaryOrdinal<U>(dataType);
        } else if (baseDataType instanceof ARXString) {
            return new StatisticsSummaryOrdinal<U>(dataType);
        } else if (hierarchy == null) {
            return new StatisticsSummaryOrdinal<U>(dataType);
        } else {
            final Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < hierarchy.length; i++) {
                map.put(hierarchy[i][generalization], hierarchy[i][0]);
            }
            return new StatisticsSummaryOrdinal<V>(new Comparator<String>() {
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
     * Orders the given array lexicographically
     *
     * @param array
     */
    private void sort(final String[] array) {
        GenericSorting.mergeSort(0, array.length, new IntComparator() {
            @Override
            public int compare(int arg0, int arg1) {
                checkInterrupt();
                return array[arg0].compareTo(array[arg1]);
            }
        }, new Swapper() {
            @Override
            public void swap(int arg0, int arg1) {
                String temp = array[arg0];
                array[arg0] = array[arg1];
                array[arg1] = temp;
            }
        });
    }
    
    /**
     * Orders the given array by data type.
     *
     * @param array
     * @param type
     */
    private void sort(final String[] array, final DataType<?> type) {
        GenericSorting.mergeSort(0, array.length, new IntComparator() {
            
            @Override
            public int compare(int arg0, int arg1) {
                checkInterrupt();
                try {
                    String s1 = array[arg0];
                    String s2 = array[arg1];
                    return (s1 == DataType.ANY_VALUE && s2 == DataType.ANY_VALUE) ? 0
                            : (s1 == DataType.ANY_VALUE ? +1
                                    : (s2 == DataType.ANY_VALUE ? -1
                                            : type.compare(s1, s2)));
                } catch (IllegalArgumentException | ParseException e) {
                    throw new RuntimeException("Some values seem to not conform to the data type", e);
                }
            }
        }, new Swapper() {
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
    private void sort(final String[] array, final Map<String, Integer> order) {
        GenericSorting.mergeSort(0, array.length, new IntComparator() {
            @Override
            public int compare(int arg0, int arg1) {
                checkInterrupt();
                Integer order1 = order.get(array[arg0]);
                Integer order2 = order.get(array[arg1]);
                if (order1 == null || order2 == null) {
                    String message = "The hierarchy seems to not cover all data values";
                    message += order1 == null ? " (unknown = "+array[arg0]+")" : "";
                    message += order2 == null ? " (unknown = "+array[arg1]+")" : "";
                    throw new RuntimeException(message);
                } else {
                    return order1.compareTo(order2);
                }
            }
        }, new Swapper() {
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            final int weeks = (int) (value / WEEKS);
            value = value % WEEKS;
            final int days = (int) (value / DAYS);
            value = value % DAYS;
            final int hours = (int) (value / HOURS);
            value = value % HOURS;
            final int minutes = (int) (value / MINUTES);
            value = value % MINUTES;
            final int seconds = (int) (value / SECONDS);
            value = value % SECONDS;
            final int milliseconds = (int) (value);
            
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
        if (type instanceof DataTypeWithRatioScale) {
            DataTypeWithRatioScale rType = (DataTypeWithRatioScale) type;
            return rType.format(rType.fromDouble(value));
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
            return (T) Long.valueOf((long) value);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(value);
        } else if (clazz == Date.class) {
            return (T) new Date((long) value);
        } else {
            return (T) String.valueOf(value);
        }
    }
    
    /**
     * Returns progress data, if available
     *
     * @return
     */
    int getProgress() {
        return this.progress.value;
    }

    /**
     * Stops all computations. May lead to exceptions being thrown. Use with care.
     */
    void interrupt() {
        this.interrupt.value = true;
    }
}
