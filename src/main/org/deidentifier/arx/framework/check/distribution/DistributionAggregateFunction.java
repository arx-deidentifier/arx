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
package org.deidentifier.arx.framework.check.distribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.list.DoubleArrayList;

/**
 * This abstract class represents a function that aggregates values from a frequency distribution
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public abstract class DistributionAggregateFunction implements Serializable {

    /**
     * This class calculates the arithmetic mean for a given distribution.
     * 
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     */
    public static class DistributionAggregateFunctionArithmeticMean extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long               serialVersionUID = 8379579591466576517L;

        /** Commons math object to calculate the statistic. */
        private transient DescriptiveStatistics stats;

        /** Minimum */
        private Double                          minimum          = null;

        /** Maximum */
        private Double                          maximum          = null;

        /**
         * Instantiates.
         * 
         * @param ignoreMissingData
         */
        public DistributionAggregateFunctionArithmeticMean(boolean ignoreMissingData) {
            super(ignoreMissingData, true);
        }
        
        /**
         * Clone constructor
         * @param ignoreMissingData
         * @param minimum
         * @param maximum
         */
        private DistributionAggregateFunctionArithmeticMean(boolean ignoreMissingData,
                                                            Double minimum,
                                                            Double maximum) {
            this(ignoreMissingData);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public <T> String aggregate(Distribution distribution) {
            stats.clear();
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>)this.type;
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) this.type;
            addAll(stats, distribution, rType, 0d);
            return stats.getN() == 0 ? DataType.NULL_VALUE : type.format(rType.fromDouble(stats.getMean()));
        }

        /**
         * Clone method
         */
        public DistributionAggregateFunctionArithmeticMean clone() {
            DistributionAggregateFunctionArithmeticMean result = new DistributionAggregateFunctionArithmeticMean(this.ignoreMissingData,
                                                                                                                 this.minimum,
                                                                                                                 this.maximum);
            if (dictionary != null) {
                result.initialize(dictionary, type);
            }
            return result;
        }

        @Override
        public <T> double getError(Distribution distribution) {
            stats.clear();
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) this.type;
            addAll(stats, distribution, rType, 0d);
            return getNMSE(minimum, maximum, stats.getValues(), stats.getMean());
        }

        @Override
        public void initialize(String[] dictionary, DataType<?> type) {
            super.initialize(dictionary, type);
            this.stats = new DescriptiveStatistics();
            if (minimum == null || maximum == null) {
                double[] values = getMinMax(dictionary, (DataTypeWithRatioScale<?>)type);
                this.minimum = values[0];
                this.maximum = values[1];
            }
        }        
    }

    /**
     * This class calculates the geometric mean for a given distribution.
     * 
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     */
    public static class DistributionAggregateFunctionGeometricMean extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long               serialVersionUID = -3835477735362966307L;

        /** Commons math object to calculate the statistic. */
        private transient DescriptiveStatistics stats;

        /** Minimum */
        private Double                          minimum          = null;

        /** Maximum */
        private Double                          maximum          = null;

        /**
         * Instantiates.
         * 
         * @param ignoreMissingData
         */
        public DistributionAggregateFunctionGeometricMean(boolean ignoreMissingData) {
            super(ignoreMissingData, true);
        }

        /**
         * Clone constructor
         * @param ignoreMissingData
         * @param minimum
         * @param maximum
         */
        private DistributionAggregateFunctionGeometricMean(boolean ignoreMissingData,
                                                           Double minimum,
                                                           Double maximum) {
            this(ignoreMissingData);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public <T> String aggregate(Distribution distribution) {
            stats.clear();
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>)this.type;
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) this.type;
            addAll(stats, distribution, rType, 1d);
            return stats.getN() == 0 ? DataType.NULL_VALUE : type.format(rType.fromDouble(stats.getGeometricMean() - 1d));
        }

        /**
         * Clone method
         */
        public DistributionAggregateFunctionGeometricMean clone() {
            
            DistributionAggregateFunctionGeometricMean result = new DistributionAggregateFunctionGeometricMean(this.ignoreMissingData,
                                                                                                               this.minimum,
                                                                                                               this.maximum);
            if (dictionary != null) {
                result.initialize(dictionary, type);
            }
            return result;
        }

        @Override
        public <T> double getError(Distribution distribution) {
            stats.clear();
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) this.type;
            addAll(stats, distribution, rType, 1d);
            return getNMSE(minimum, maximum, stats.getValues(), stats.getGeometricMean() - 1d);
        }
        
        @Override
        public void initialize(String[] dictionary, DataType<?> type) {
            super.initialize(dictionary, type);
            this.stats = new DescriptiveStatistics();
            if (minimum == null || maximum == null) {
                double[] values = getMinMax(dictionary, (DataTypeWithRatioScale<?>)type);
                this.minimum = values[0];
                this.maximum = values[1];
            }
        }
    }

    /**
     * This class calculates the mode for a given distribution.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     * 
     */
    public static class DistributionAggregateFunctionInterval extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long serialVersionUID = 2349775566497080868L;

        /**
         * Instantiates.
         * 
         * @param ignoreMissingData
         */
        public DistributionAggregateFunctionInterval(boolean ignoreMissingData) {
            super(ignoreMissingData, false);
        }

        @Override
        public <T> String aggregate(Distribution distribution) {

            // Determine min & max
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>)this.type;
            T minT = null;
            T maxT = null;
            int[] buckets = distribution.getBuckets();
            for (int i = 0; i < buckets.length; i += 2) {
                int value = buckets[i];
                if (value != -1) {
                    T valT = type.parse(dictionary[value]);
                    if (minT == null || type.compare(valT, minT) < 0 ) {
                        minT = valT;
                    }
                    if (maxT == null || type.compare(valT, maxT) > 0 ) {
                        maxT = valT;
                    }
                }
            }
            
            // Format
            return minT == null || maxT == null ? DataType.NULL_VALUE : "[" + type.format(minT) + ", " + type.format(maxT) + "]";
        }

        /**
         * Clone method
         */
        public DistributionAggregateFunctionInterval clone() {
            DistributionAggregateFunctionInterval result = new DistributionAggregateFunctionInterval(this.ignoreMissingData);
            if (dictionary != null) {
                result.initialize(dictionary, type);
            }
            return result;
        }

        @Override
        public <T> double getError(Distribution distribution) {
            return getInformationLoss(distribution);
        }
    }

    /**
     * This class calculates the median for a given distribution.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     * 
     */
    public static class DistributionAggregateFunctionMedian extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long serialVersionUID = 4877214760061314248L;

        /** Minimum */
        private Double            minimum          = null;

        /** Maximum */
        private Double            maximum          = null;

        /**
         * Instantiates.
         * 
         * @param ignoreMissingData
         */
        public DistributionAggregateFunctionMedian(boolean ignoreMissingData) {
            super(ignoreMissingData, true);
        }

        /**
         * Clone constructor
         * @param ignoreMissingData
         * @param minimum
         * @param maximum
         */
        private DistributionAggregateFunctionMedian(boolean ignoreMissingData,
                                                    Double minimum,
                                                    Double maximum) {
            this(ignoreMissingData);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public <T> String aggregate(Distribution distribution) {
            
            @SuppressWarnings("unchecked")
            final DataType<T> type = (DataType<T>)this.type;
            
            // Determine median
            final List<T> values = new ArrayList<T>();
            final List<Integer> frequencies = new ArrayList<Integer>();

            // Collect
            int[] buckets = distribution.getBuckets();
            for (int i = 0; i < buckets.length; i += 2) {
                int value = buckets[i];
                if (value != -1) {
                    int frequency = buckets[i + 1];
                    values.add(type.parse(dictionary[value]));
                    frequencies.add(frequency);
                }
            }

            // Sort
            GenericSorting.mergeSort(0, values.size(), new IntComparator() {
                @Override
                public int compare(int arg0, int arg1) {
                    return type.compare(values.get(arg0), values.get(arg1));
                }
            }, new Swapper() {
                @Override
                public void swap(int arg0, int arg1) {
                    T temp = values.get(arg0);
                    values.set(arg0, values.get(arg1));
                    values.set(arg1, temp);
                    Integer temp2 = frequencies.get(arg0);
                    frequencies.set(arg0, frequencies.get(arg1));
                    frequencies.set(arg1, temp2);
                }
            });

            // Accumulate
            int total = 0;
            for (int i = 0; i < frequencies.size(); i++) {
                total += frequencies.get(i);
                frequencies.set(i, total - 1);
            }

            // Switch
            if (total % 2 == 1) {
                return type.format(getValueAt(values, frequencies, total / 2));
            } else if (type instanceof DataTypeWithRatioScale) {
                @SuppressWarnings("unchecked")
                DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) type;
                double median1 = rType.toDouble(getValueAt(values, frequencies, total / 2 - 1));
                double median2 = rType.toDouble(getValueAt(values, frequencies, total / 2));
                return rType.format(rType.fromDouble((median1 + median2) / 2d));
            } else {
                T median1 = getValueAt(values, frequencies, total / 2 - 1);
                T median2 = getValueAt(values, frequencies, total / 2);
                if ((median1 == null && median2 == null) || median1.equals(median2)) {
                    return type.format(median1);
                } else {
                    return DataType.NULL_VALUE;
                }
            }
        }

        /**
         * Clone method
         */
        public DistributionAggregateFunctionMedian clone() {
            DistributionAggregateFunctionMedian result = new DistributionAggregateFunctionMedian(this.ignoreMissingData,
                                                                                                 this.minimum,
                                                                                                 this.maximum);
            if (dictionary != null) {
                result.initialize(dictionary, type);
            }
            return result;
        }

        @Override
        public <T> double getError(Distribution distribution) {
            
            if (!(type instanceof DataTypeWithRatioScale)) {
                return 0d;
            }
            
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) this.type;
            DoubleArrayList list = new DoubleArrayList();
            Iterator<Double> it = DistributionIterator.createIteratorDouble(distribution, dictionary, rType);
            while (it.hasNext()) {
                Double value = it.next();
                value = value == null ? (ignoreMissingData ? null : 0d) : value;
                if (value != null) {
                    list.add(value);
                }
            }
            
            // Determine and check mode
            String mean = aggregate(distribution);
            if (mean == DataType.NULL_VALUE) {
                return 1d;
            }
            
            // Compute error
            return getNMSE(minimum, maximum, Arrays.copyOf(list.elements(), list.size()), 
                                             rType.toDouble(rType.parse(mean)));
        }
        
        @Override
        public void initialize(String[] dictionary, DataType<?> type) {
            super.initialize(dictionary, type);
            if (type instanceof DataTypeWithRatioScale) {
                if (minimum == null || maximum == null) {
                    double[] values = getMinMax(dictionary, (DataTypeWithRatioScale<?>)type);
                    this.minimum = values[0];
                    this.maximum = values[1];
                }
            }
        }

        /**
         * Returns the value at
         * @param values
         * @param frequencies
         * @param index
         * @return
         */
        private <T> T getValueAt(List<T> values, List<Integer> frequencies, int index) {
            int pointer = 0;
            while (frequencies.get(pointer) < index) {
                pointer++;
            }
            return values.get(pointer);
        }
    }


    /**
     * This class calculates the mode for a given distribution.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     * 
     */
    public static class DistributionAggregateFunctionMode extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long serialVersionUID = -3424849372778696640L;

        /** Minimum */
        private double            minimum          = 0d;

        /** Maximum */
        private double            maximum          = 0d;

        /**
         * Instantiates.
         * 
         * @param ignoreMissingData
         */
        public DistributionAggregateFunctionMode(boolean ignoreMissingData) {
            super(ignoreMissingData, true);
        }

        /**
         * Clone constructor
         * @param ignoreMissingData
         * @param minimum
         * @param maximum
         */
        private DistributionAggregateFunctionMode(boolean ignoreMissingData,
                                                  double minimum,
                                                  double maximum) {
            this(ignoreMissingData);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public <T> String aggregate(Distribution distribution) {

            // Determine mode
            int mode = getMode(distribution);
            return mode == -1 ? DataType.NULL_VALUE : dictionary[mode];
        }

        /**
         * Clone method
         */
        public DistributionAggregateFunctionMode clone() {
            DistributionAggregateFunctionMode result = new DistributionAggregateFunctionMode(this.ignoreMissingData,
                                                                                             this.minimum,
                                                                                             this.maximum);
            if (dictionary != null) {
                result.initialize(dictionary, type);
            }
            return result;
        }

        @Override
        public <T> double getError(Distribution distribution) {
            
            if (!(type instanceof DataTypeWithRatioScale)) {
                return 0d;
            }
            
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) this.type;
            DoubleArrayList list = new DoubleArrayList();
            Iterator<Double> it = DistributionIterator.createIteratorDouble(distribution, dictionary, rType);
            while (it.hasNext()) {
                Double value = it.next();
                value = value == null ? (ignoreMissingData ? null : 0d) : value;
                if (value != null) {
                    list.add(value);
                }
            }
            
            // Determine and check mode
            int mode = getMode(distribution);
            if (mode == -1) {
                return 1d;
            }
            
            // Compute error
            return getNMSE(minimum, maximum, Arrays.copyOf(list.elements(), list.size()), 
                                             rType.toDouble(rType.parse(dictionary[mode])));
        }

        @Override
        public void initialize(String[] dictionary, DataType<?> type) {
            super.initialize(dictionary, type);
            if (type instanceof DataTypeWithRatioScale) {
                double[] values = getMinMax(dictionary, (DataTypeWithRatioScale<?>)type);
                this.minimum = values[0];
                this.maximum = values[1];
            }
        }
        
        /**
         * Returns the index of the most frequent element from the distribution, -1 if there is no such element
         * @param distribution
         * @return
         */
        private int getMode(Distribution distribution) {
            int[] buckets = distribution.getBuckets();
            int max = -1;
            int mode = -1;
            for (int i = 0; i < buckets.length; i += 2) {
                int value = buckets[i];
                int frequency = buckets[i + 1];
                if (value != -1 && frequency > max) {
                    max = frequency;
                    mode = value;
                }
            }
            return mode;
        }
    }

    /** SVUID. */
    private static final long       serialVersionUID = 331877806010996154L;

    /** Whether or not null values should be ignored */
    protected boolean               ignoreMissingData;
    
    /** Stores whether this is a type-preserving function */
    private final boolean           typePreserving;
    
    /** Dictionary */
    protected transient String[]    dictionary;
    
    /** Type */
    protected transient DataType<?> type;

    /**
     * Instantiates a new function.
     * 
     * @param ignoreMissingData
     * @param typePreserving
     */
    public DistributionAggregateFunction(boolean ignoreMissingData,
                                         boolean typePreserving) {
        this.ignoreMissingData = ignoreMissingData;
        this.typePreserving = typePreserving;
    }

    /**
     * This function returns an aggregate value.
     * 
     * @param distribution
     * @param dictionary
     * @param type
     * @return the string
     */
    public abstract <T> String aggregate(Distribution distribution);
    
    /**
     * Clones this function
     */
    public abstract DistributionAggregateFunction clone();
    
    /**
     * Returns the normalized error induced by aggregation. In most cases this will be the mean squared error 
     * normalized into [0,1]. In case of generalization, it will return the normalized generalization level
     * (also called generalization intensity). In case of intervals, it will return the normalized number
     * of aggregated values. 
     * 
     * @param distribution
     * @return
     */
    public abstract <T> double getError(Distribution distribution);
    
    /**
     * This will return the normalized number of aggregated values in range [1/#distinct-values, 1].
     * 
     * @param distribution
     * @return
     */
    public <T> double getInformationLoss(Distribution distribution) {
        double result = 0d;
        int[] buckets = distribution.getBuckets();
        for (int i = 0; i < buckets.length; i += 2) {
            result += buckets[i] != -1 ? 1 : 0;
        }
        return result / (double)dictionary.length;
    }
    
    /**
     * Initializes the function
     * @param dictionary
     * @param type
     * @param hierarchy
     */
    public void initialize(String[] dictionary, DataType<?> type) {
        this.dictionary = dictionary;
        this.type = type;
    }
    
    /**
     * Returns whether this is a type-preserving function
     * @return
     */
    public boolean isTypePreserving() {
        return this.typePreserving;
    }
    
    /**
     * Adds all values from the distribution to the given descriptive statistics object
     * @param statistics
     * @param distribution
     * @param type
     * @param offset will be added to values
     */
    protected <T> void addAll(DescriptiveStatistics statistics, 
                           Distribution distribution,
                           DataTypeWithRatioScale<T> type,
                           double offset) {
        Iterator<Double> it = DistributionIterator.createIteratorDouble(distribution, dictionary, type);
        while (it.hasNext()) {
            Double value = it.next();
            value = value == null ? (ignoreMissingData ? null : 0d) : value;
            if (value != null) {
                statistics.addValue(value + offset);
            }
        }
    }

    /**
     * Returns the minimum and maximum value
     * @param dictionary
     * @param type
     * @return
     */
    protected <T> double[] getMinMax(String[] dictionary, DataTypeWithRatioScale<T> type) {
        T min = null;
        T max = null;
        for (String string : dictionary) {
            T value = type.parse(string);
            if (!ignoreMissingData || value != null) {
                min = min == null || type.compare(min, value) > 0 ? value : min;
                max = max == null || type.compare(max, value) < 0 ? value : max;
            }
        }
        Double _min = type.toDouble(min);
        Double _max = type.toDouble(max);
        _min = _min != null ? _min : 0d;
        _max = _max != null ? _max : 0d;
        return new double[]{_min, _max};
    }

    /**
     * Calculates the mean square error after normalizing everything into [0,1]
     * 
     * @param min
     * @param max
     * @param values
     * @param aggregate
     * @return
     */
    protected double getNMSE(double min, double max, double[] values, double aggregate) {
        
        // Prepare
        double normalizationFactor = 1d / (max - min);
        double normalizedAggregate = (aggregate - min) * normalizationFactor;
        
        // NMSE and Sum 1
        double nmse = 0d;
        for (int i = 0; i < values.length; i++) {
            double normalizedValue = (values[i] - min) * normalizationFactor;
            double diff = normalizedValue - normalizedAggregate;
            nmse += diff * diff;
        }

        // Normalize and return
        return nmse / (double)values.length;
    }
}
