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
package org.deidentifier.arx.framework.check.distribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

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
        private static final long           serialVersionUID = 8379579591466576517L;
        
        /** Commons math object to calculate the statistic. */
        private final DescriptiveStatistics stats            = new DescriptiveStatistics();

        /**
         * Instantiates.
         * 
         * @param ignoreNullValues
         */
        public DistributionAggregateFunctionArithmeticMean(boolean ignoreNullValues) {
            super(ignoreNullValues);
        }

        @Override
        public <T> String aggregate(Distribution distribution) {
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>)this.type;
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) type;
            Iterator<Double> it = DistributionIterator.createIteratorDouble(distribution, dictionary, rType);
            stats.clear();
            while (it.hasNext()) {
                Double value = it.next();
                if (value != null || !ignoreNullValues) {
                    stats.addValue(value);
                }
            }
            return type.format(rType.fromDouble(stats.getMean()));
        }
    }

    /**
     * This class generalizes the given distribution.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     * 
     */
    public static class DistributionAggregateFunctionGeneralization extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long serialVersionUID = 5010485066464965464L;

        /**
         * Creates a new instance
         * @param ignoreNullValues
         */
        public DistributionAggregateFunctionGeneralization(boolean ignoreNullValues) {
            super(ignoreNullValues);
        }

        @Override
        public <T> String aggregate(Distribution distribution) {

            // Prepare iteration
            int[] buckets = distribution.getBuckets();
            int[] state = new int[] { -1, 0 }; // value, next offset
            read(buckets, state);
            int current = state[0];
            int previous = -1;

            int lvl = 0;
            int val = hierarchy[current][0];
            while (read(buckets, state)) {
                previous = current;
                current = state[0];
                while (hierarchy[current][lvl] != val) {
                    lvl++;
                    if (lvl == hierarchy[previous].length) {
                        return "*";
                    }
                    val = hierarchy[previous][lvl];
                }
            }
            
            return dictionary[val];
        }

        /**
         * Reads data into the provided array
         * @param buckets
         * @param state
         * @return True, if data was read
         */
        private boolean read(int[] buckets, int[] state) {
            while (state[1] < buckets.length && buckets[state[1]] == -1) {
                state[1] += 2;
            }
            if (state[1] >= buckets.length) {
                return false;
            } else {
                state[0] = buckets[state[1]];
                state[1] += 2;
                return true;
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
        private static final long           serialVersionUID = -3835477735362966307L;
        
        /** Commons math object to calculate the statistic. */
        private final DescriptiveStatistics stats            = new DescriptiveStatistics();

        /**
         * Instantiates.
         * 
         * @param ignoreNullValues
         */
        public DistributionAggregateFunctionGeometricMean(boolean ignoreNullValues) {
            super(ignoreNullValues);
        }

        @Override
        public <T> String aggregate(Distribution distribution) {
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>)this.type;
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> rType = (DataTypeWithRatioScale<T>) type;
            Iterator<Double> it = DistributionIterator.createIteratorDouble(distribution, dictionary, rType);
            stats.clear();
            while (it.hasNext()) {
                Double value = it.next();
                if (value != null || !ignoreNullValues) {
                    stats.addValue(value);
                }
            }
            return type.format(rType.fromDouble(stats.getGeometricMean()));
        }
    }

    /**
     * This class calculates the median for a given distribution.
     * @author Florian Kohlmayer
     * 
     */
    public static class DistributionAggregateFunctionMedian extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long serialVersionUID = 4877214760061314248L;

        /**
         * Instantiates.
         * 
         * @param ignoreNullValues
         */
        public DistributionAggregateFunctionMedian(boolean ignoreNullValues) {
            super(ignoreNullValues);
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
                if (median1.equals(median2)) {
                    return type.format(median1);
                } else {
                    return DataType.NULL_VALUE;
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
            while (frequencies.get(index) < index) {
                pointer++;
            }
            return values.get(pointer);
        }
    }

    /**
     * This class calculates the mode for a given distribution.
     * @author Florian Kohlmayer
     * 
     */
    public static class DistributionAggregateFunctionMode extends DistributionAggregateFunction {

        /** SVUID. */
        private static final long serialVersionUID = -3424849372778696640L;

        /**
         * Instantiates.
         * 
         * @param ignoreNullValues
         */
        public DistributionAggregateFunctionMode(boolean ignoreNullValues) {
            super(ignoreNullValues);
        }

        @Override
        public <T> String aggregate(Distribution distribution) {

            // Determine mode
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
            return mode == -1 ? null : dictionary[mode];
        }
    }

    /** SVUID. */
    private static final long serialVersionUID = 331877806010996154L;

    /** Whether or not null values should be ignored */
    protected boolean         ignoreNullValues;
    /** Dictionary*/
    protected String[] dictionary;
    /** Type*/
    protected DataType<?> type;
    /** Hierarchy*/
    protected int[][] hierarchy;
    
    /**
     * Instantiates a new function.
     * 
     * @param ignoreNullValues
     */
    public DistributionAggregateFunction(boolean ignoreNullValues) {
        this.ignoreNullValues = ignoreNullValues;
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
     * Initializes the function
     * @param dictionary
     * @param type
     * @param hierarchy
     */
    public void initialize(String[] dictionary, DataType<?> type, int[][] hierarchy) {
        this.dictionary = dictionary;
        this.type = type;
        this.hierarchy = hierarchy;
    }
}
