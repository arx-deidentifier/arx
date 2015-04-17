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
import java.util.Iterator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.DataType.ScaleOfMeasure;
import org.deidentifier.arx.framework.check.distribution.Distribution;

/**
 * This abstract class represents an microaggregate function.
 *
 * @author Florian Kohlmayer
 */
public abstract class MicroaggregateFunction implements Serializable {
    
    public static class ArithmeticMean extends MicroaggregateFunction {
        
        /** SVUID */
        private static final long           serialVersionUID = 8108686651029571643L;
        
        private final DescriptiveStatistics stats;
        
        public ArithmeticMean() {
            this(HandlingOfNullValues.IGNORE);
        }
        
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
    
    public static class GeometricMean extends MicroaggregateFunction {
        
        /** SVUID */
        private static final long           serialVersionUID = -6715484856330698691L;
        
        private final DescriptiveStatistics stats;
        
        public GeometricMean() {
            this(HandlingOfNullValues.IGNORE);
        }
        
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
    
    public static enum HandlingOfNullValues {
        IGNORE,
        IDENTITIY
    }
    

    
    private class DistributionIteratorDouble implements Iterator<Double> {
        
        private final Distribution           values;
        @SuppressWarnings("rawtypes")
        private final DataTypeWithRatioScale type;
        private final String[]               dictionary;
        private int                          nextBucket;
        private int                          currentFrequency;
        private Double                       currentValue;
        private int                          remaining;
        
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
    
    protected HandlingOfNullValues handleNullValues = HandlingOfNullValues.IGNORE;
    
    /** SVUID */
    private static final long      serialVersionUID = 331877806010996154L;
    /** The data type. */
    protected DataType<?>          type;
    
    /** The dictionary */
    protected String[]             dictionary;
    
    public MicroaggregateFunction() {
        this(HandlingOfNullValues.IGNORE);
    }
    
    public MicroaggregateFunction(HandlingOfNullValues nullValueHandling) {
        handleNullValues = nullValueHandling;
    }
    
    /**
     * 
     * This function returns an aggregate value.
     * 
     * @param values
     * @return
     */
    public String aggregate(Distribution values) {
        String returnValue = aggregateInternal(values);
        return returnValue.intern();
    }
    
    /**
     * Returns the scale which is needed
     * @return
     */
    public abstract ScaleOfMeasure getMinimalRequiredScale();
    
    /**
     * Inits the aggregate function and sets the according dictionary.
     * 
     * @param dictionary
     * @param attributeType
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
     * @param values
     * @return
     */
    protected abstract String aggregateInternal(Distribution values);
    
}