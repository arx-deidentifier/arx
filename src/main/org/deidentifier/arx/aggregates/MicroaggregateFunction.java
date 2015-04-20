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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.DataType.ScaleOfMeasure;
import org.deidentifier.arx.aggregates.StatisticsSummary.StatisticsSummaryOrdinal;
import org.deidentifier.arx.framework.check.distribution.Distribution;

/**
 * This abstract class represents an microaggregate function.
 *
 * @author Florian Kohlmayer
 */
public abstract class MicroaggregateFunction implements Serializable {
    
    /**
     * This class calculates the arithmetic mean for a given distribution.
     * @author Florian Kohlmayer
     *
     */
    public static class ArithmeticMean extends MicroaggregateFunction {
        
        /** SVUID. */
        private static final long           serialVersionUID = 8108686651029571643L;
        
        /** Commons math object to calculate the statistic. */
        private final DescriptiveStatistics stats;
        
        /**
         * Instantiates.
         */
        public ArithmeticMean() {
            this(HandlingOfNullValues.IGNORE);
        }
        
        /**
         * Instantiates.
         *
         * @param nullValueHandling the null value handling
         */
        public ArithmeticMean(HandlingOfNullValues nullValueHandling) {
            super(nullValueHandling);
            stats = new DescriptiveStatistics();
        }
        
        @Override
        public ScaleOfMeasure getMinimalRequiredScale() {
            return ScaleOfMeasure.INTERVAL;
        }
        
        @Override
        public String toString() {
            return "Microaggrate function: ArithmeticMean";
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected String aggregateInternal(Distribution values) {
            DataTypeWithRatioScale castedType = (DataTypeWithRatioScale) type;
            Iterator<Double> it = new DistributionIteratorDouble(values, castedType, dictionary);
            
            stats.clear();
            while (it.hasNext()) {
                Double value = it.next();
                if (value == null) {
                    switch (handleNullValues) {
                    case IGNORE:
                        // Do nothing
                        break;
                    case IDENTITIY:
                        stats.addValue(0d);
                        break;
                    }
                } else {
                    stats.addValue(value);
                }
                
            }
            return castedType.format(castedType.fromDouble(stats.getMean()));
        }
    }
    
    /**
     * This class calculates the geometric mean for a given distribution.
     * 
     * @author Florian Kohlmayer
     *
     */
    public static class GeometricMean extends MicroaggregateFunction {
        
        /** SVUID. */
        private static final long           serialVersionUID = -6715484856330698691L;
        
        /** Commons math object to calculate the statistic. */
        private final DescriptiveStatistics stats;
        
        /**
         * Instantiates.
         */
        public GeometricMean() {
            this(HandlingOfNullValues.IGNORE);
        }
        
        /**
         * Instantiates.
         *
         * @param nullValueHandling the null value handling
         */
        public GeometricMean(HandlingOfNullValues nullValueHandling) {
            super(nullValueHandling);
            stats = new DescriptiveStatistics();
        }
        
        @Override
        public ScaleOfMeasure getMinimalRequiredScale() {
            return ScaleOfMeasure.RATIO;
        }
        
        @Override
        public String toString() {
            return "Microaggrate function: GeometricMean";
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected String aggregateInternal(Distribution values) {
            DataTypeWithRatioScale castedType = (DataTypeWithRatioScale) type;
            Iterator<Double> it = new DistributionIteratorDouble(values, castedType, dictionary);
            
            stats.clear();
            while (it.hasNext()) {
                Double value = it.next();
                if (value == null) {
                    switch (handleNullValues) {
                    case IGNORE:
                        // Do nothing
                        break;
                    case IDENTITIY:
                        stats.addValue(1d);
                        break;
                    }
                } else {
                    stats.addValue(value);
                }
                
            }
            return castedType.format(castedType.fromDouble(stats.getGeometricMean()));
        }
    }
    
    /**
     * Options to handle NULL values.
     */
    public static enum HandlingOfNullValues {
        IGNORE,
        IDENTITIY
    }
    
    /**
     * This class calculates the median for a given distribution.
     * @author Florian Kohlmayer
     *
     */
    public static class Median extends MicroaggregateFunction {
        
        /** SVUID. */
        private static final long        serialVersionUID = -2070029487780931767L;
        /** Commons math object to calculate the statistic. */
        private StatisticsSummaryOrdinal stats;
        
        /**
         * Instantiates.
         */
        public Median() {
            this(HandlingOfNullValues.IGNORE);
        }
        
        /**
         * Instantiates.
         *
         * @param nullValueHandling the null value handling
         */
        public Median(HandlingOfNullValues nullValueHandling) {
            super(nullValueHandling);
        }
        
        @Override
        public ScaleOfMeasure getMinimalRequiredScale() {
            return ScaleOfMeasure.ORDINAL;
        }
        
        @Override
        public String toString() {
            return "Microaggrate function: Median";
        }
        
        @Override
        protected String aggregateInternal(Distribution values) {
            Iterator<String> it = new DistributionIteratorString(values, dictionary);
            
            stats = new StatisticsSummaryOrdinal(type);
            
            while (it.hasNext()) {
                String value = it.next();
                if (value == null) {
                    switch (handleNullValues) {
                    case IGNORE:
                        // Do nothing
                        break;
                    case IDENTITIY:
                        stats.addValue(DataType.NULL_VALUE);
                        break;
                    }
                } else {
                    stats.addValue(value);
                }
                
            }
            stats.analyze();
            return stats.getMedian();
        }
    }
    
    /**
     * This class calculates the mode for a given distribution.
     * @author Florian Kohlmayer
     *
     */
    public static class Modus extends MicroaggregateFunction {
        
        /** SVUID. */
        private static final long        serialVersionUID = -2070029487780931767L;
        /** Commons math object to calculate the statistic. */
        private StatisticsSummaryOrdinal stats;
        
        /**
         * Instantiates.
         */
        public Modus() {
            this(HandlingOfNullValues.IGNORE);
        }
        
        /**
         * Instantiates.
         *
         * @param nullValueHandling the null value handling
         */
        public Modus(HandlingOfNullValues nullValueHandling) {
            super(nullValueHandling);
        }
        
        @Override
        public ScaleOfMeasure getMinimalRequiredScale() {
            return ScaleOfMeasure.NOMINAL;
        }
        
        @Override
        public String toString() {
            return "Microaggrate function: Modus";
        }
        
        @Override
        protected String aggregateInternal(Distribution values) {
            Iterator<String> it = new DistributionIteratorString(values, dictionary);
            
            stats = new StatisticsSummaryOrdinal(type);
            
            while (it.hasNext()) {
                String value = it.next();
                if (value == null) {
                    switch (handleNullValues) {
                    case IGNORE:
                        // Do nothing
                        break;
                    case IDENTITIY:
                        stats.addValue(DataType.NULL_VALUE);
                        break;
                    }
                } else {
                    stats.addValue(value);
                }
                
            }
            stats.analyze();
            return stats.getMode();
        }
    }
    
    /**
     * Double iterator for distributions.
     * @author Florian Kohlmayer
     *
     */
    private class DistributionIteratorDouble implements Iterator<Double> {
        
        /** The distribution. */
        private final Distribution           values;
        
        /** The data type */
        @SuppressWarnings("rawtypes")
        private final DataTypeWithRatioScale type;
        
        /** The dictionary. */
        private final String[]               dictionary;
        
        /** The index of the next bucket. */
        private int                          nextBucket;
        
        /** The frequency of the current value. */
        private int                          currentFrequency;
        
        /** The value of the current bucket. */
        private Double                       currentValue;
        
        /** Counts the remaining entries. */
        private int                          remaining;
        
        /**
         * Instantiates the iterator.
         *
         * @param values the values
         * @param type the type
         * @param dictionary the dictionary
         */
        public DistributionIteratorDouble(Distribution values, DataTypeWithRatioScale<?> type, String[] dictionary) {
            this.values = values;
            this.type = type;
            this.dictionary = dictionary;
            nextBucket = 0;
            currentFrequency = 0;
            currentValue = null;
            
            // Calculate number of entries for "hasNext()"
            // TODO there has to be a more efficient way!
            if (values.size() > 0) {
                int[] buckets = values.getBuckets();
                for (int i = 0; i < buckets.length; i += 2) {
                    if (buckets[i] != -1) { // bucket not empty
                        remaining += buckets[i + 1];
                    }
                }
            }
        }
        
        @Override
        public boolean hasNext() {
            return remaining != 0;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public Double next() {
            if (currentFrequency == 0) {
                int value = values.getBuckets()[nextBucket];
                while (value == -1) { // Bucket not empty
                    nextBucket += 2;
                    value = values.getBuckets()[nextBucket];
                }
                currentValue = type.toDouble(type.parse(dictionary[value]));
                currentFrequency = values.getBuckets()[nextBucket + 1];
                nextBucket += 2;
            }
            currentFrequency--;
            remaining--;
            return currentValue;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    /**
     * Double iterator for distributions.
     * @author Florian Kohlmayer
     *
     */
    private class DistributionIteratorString implements Iterator<String> {
        
        /** The distribution. */
        private final Distribution values;
        
        /** The dictionary. */
        private final String[]     dictionary;
        
        /** The index of the next bucket. */
        private int                nextBucket;
        
        /** The frequency of the current value. */
        private int                currentFrequency;
        
        /** The value of the current bucket. */
        private String             currentValue;
        
        /** Counts the remaining entries. */
        private int                remaining;
        
        /**
         * Instantiates the iterator.
         *
         * @param values the values
         * @param type the type
         * @param dictionary the dictionary
         */
        public DistributionIteratorString(Distribution values, String[] dictionary) {
            this.values = values;
            this.dictionary = dictionary;
            nextBucket = 0;
            currentFrequency = 0;
            currentValue = null;
            
            // Calculate number of entries for "hasNext()"
            // TODO there has to be a more efficient way!
            if (values.size() > 0) {
                int[] buckets = values.getBuckets();
                for (int i = 0; i < buckets.length; i += 2) {
                    if (buckets[i] != -1) { // bucket not empty
                        remaining += buckets[i + 1];
                    }
                }
            }
        }
        
        @Override
        public boolean hasNext() {
            return remaining != 0;
        }
        
        @Override
        public String next() {
            if (currentFrequency == 0) {
                int value = values.getBuckets()[nextBucket];
                while (value == -1) { // Bucket not empty
                    nextBucket += 2;
                    value = values.getBuckets()[nextBucket];
                }
                currentValue = dictionary[value];
                currentFrequency = values.getBuckets()[nextBucket + 1];
                nextBucket += 2;
            }
            currentFrequency--;
            remaining--;
            return currentValue;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public static List<MicroaggregationFunctionDescription> list() {
        return Arrays.asList(new MicroaggregationFunctionDescription[] {
                new MicroaggregationFunctionDescription("Arithmetic Mean",
                                                        ScaleOfMeasure.INTERVAL) {
                    
                    /** SVUID */
                    private static final long serialVersionUID = -1456232652408261065L;
                    
                    @Override
                    public MicroaggregateFunction createInstance(HandlingOfNullValues nullValueHandling) {
                        return new ArithmeticMean(nullValueHandling);
                    }
                    
                    @Override
                    public boolean isInstance(MicroaggregateFunction function) {
                        return (function instanceof ArithmeticMean);
                    }
                }, new MicroaggregationFunctionDescription("Geometric Mean",
                                                           ScaleOfMeasure.RATIO) {
                    
                    /** SVUID */
                    private static final long serialVersionUID = 7737081838418104854L;
                    
                    @Override
                    public MicroaggregateFunction createInstance(HandlingOfNullValues nullValueHandling) {
                        return new GeometricMean(nullValueHandling);
                    }
                    
                    @Override
                    public boolean isInstance(MicroaggregateFunction function) {
                        return (function instanceof GeometricMean);
                    }
                },
                new MicroaggregationFunctionDescription("Mode",
                                                        ScaleOfMeasure.NOMINAL) {
                    
                    /** SVUID */
                    private static final long serialVersionUID = -2500330309804167183L;
                    
                    @Override
                    public MicroaggregateFunction createInstance(HandlingOfNullValues nullValueHandling) {
                        return new Modus(nullValueHandling);
                    }
                    
                    @Override
                    public boolean isInstance(MicroaggregateFunction function) {
                        return (function instanceof Modus);
                    }
                },
                new MicroaggregationFunctionDescription("Median",
                                                        ScaleOfMeasure.ORDINAL) {
                    
                    /** SVUID */
                    private static final long serialVersionUID = 6765052918594701507L;
                    
                    @Override
                    public MicroaggregateFunction createInstance(HandlingOfNullValues nullValueHandling) {
                        return new Median(nullValueHandling);
                    }
                    
                    @Override
                    public boolean isInstance(MicroaggregateFunction function) {
                        return (function instanceof Median);
                    }
                },
        });
    }
    
    /** The NULL value handling. */
    protected HandlingOfNullValues handleNullValues = HandlingOfNullValues.IGNORE;
    
    /** SVUID. */
    private static final long      serialVersionUID = 331877806010996154L;
    
    /** The data type. */
    protected DataType<?>          type;
    
    /** The dictionary. */
    protected String[]             dictionary;
    
    /**
     * Instantiates a new Microaggregation.
     */
    public MicroaggregateFunction() {
        this(HandlingOfNullValues.IGNORE);
    }
    
    /**
     * Instantiates a new Microaggregation.
     *
     * @param nullValueHandling the null value handling
     */
    public MicroaggregateFunction(HandlingOfNullValues nullValueHandling) {
        handleNullValues = nullValueHandling;
    }
    
    /**
     * This function returns an aggregate value.
     *
     * @param values the values
     * @return the string
     */
    public String aggregate(Distribution values) {
        String returnValue = aggregateInternal(values);
        return returnValue.intern();
    }
    
    /**
     * Returns the scale which is needed.
     *
     * @return the minimal required scale
     */
    public abstract ScaleOfMeasure getMinimalRequiredScale();
    
    /**
     * Inits the aggregate function and sets the according dictionary.
     *
     * @param dictionary the dictionary
     * @param attributeType the attribute type
     */
    public void init(String[] dictionary, DataType<?> attributeType) {
        this.dictionary = dictionary;
        type = attributeType;
    }
    
    @Override
    public abstract String toString();
    
    /**
     * This internal function returns an aggregate value for the given distribution.
     *
     * @param values the values
     * @return the string
     */
    protected abstract String aggregateInternal(Distribution values);
    
}
