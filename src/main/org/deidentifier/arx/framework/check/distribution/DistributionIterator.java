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
package org.deidentifier.arx.framework.check.distribution;

import java.util.Iterator;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

/**
 * An iterator over values of a distribution
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public abstract class DistributionIterator<T> implements Iterator<T>{

    /**
     * Double iterator for distributions.
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     */
    private static class DistributionIteratorDouble extends DistributionIterator<Double> {
        
        /** The data type */
        @SuppressWarnings("rawtypes")
        private final DataTypeWithRatioScale type;


        /**
         * Constructor
         * @param distribution
         * @param dictionary
         * @param type
         */
        DistributionIteratorDouble(Distribution distribution, String[] dictionary, DataTypeWithRatioScale<?> type) {
            super(distribution, dictionary);
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Double parse(String value) {
            return type.toDouble(type.parse(value));
        }
    }

    /**
     * Double iterator for distributions.
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     */
    private static class DistributionIteratorString extends DistributionIterator<String> {
        
        /**
         * Constructor
         * @param distribution
         * @param dictionary
         */
        DistributionIteratorString(Distribution distribution, String[] dictionary) {
            super(distribution, dictionary);
        }

        @Override
        protected String parse(String value) {
            return value;
        }
    }

    /**
     * Returns a string iterator
     * @param distribution
     * @param dictionary
     * @param type
     * @return
     */
    static Iterator<Double> createIteratorDouble(Distribution distribution, String[] dictionary, DataTypeWithRatioScale<?> type) {
        return new DistributionIteratorDouble(distribution, dictionary, type);
    }

    /**
     * Returns a string iterator
     * @param distribution
     * @param dictionary
     * @return
     */
    static Iterator<String> createIteratorString(Distribution distribution, String[] dictionary) {
        return new DistributionIteratorString(distribution, dictionary);
    }

    /** The distribution. */
    private final int[]    buckets;

    /** The dictionary. */
    private final String[] dictionary;

    /** The index of the next bucket. */
    private int            nextBucket = 0;
    
    /** The frequency of the current value. */
    private int            currentFrequency;

    /** The value of the current bucket. */
    private T              currentValue;
    
    /** Is the current bucket valid. */
    private boolean        currentValid = false;

    /**
     * Instantiates the iterator.
     *
     * @param distribution the values
     * @param dictionary the dictionary
     */
    DistributionIterator(Distribution distribution, String[] dictionary) {
        this.buckets = distribution.getBuckets();
        this.dictionary = dictionary;
        this.currentValid = false;
    }
    
    @Override
    public boolean hasNext() {
        if (!currentValid) {
            pull();
        }
        return currentValid;
    }
    
    @Override
    public T next() {

        if (!currentValid) {
            pull();
        }
        currentFrequency--;
        if (currentFrequency == 0) {
            currentValid = false;
        }
        return currentValue;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }   
    
    /**
     * Pulls the next element from the distribution
     */
    private void pull() {
        
        // Check
        if (nextBucket >= buckets.length) {
            currentValue = null;
            currentFrequency = 0;
            currentValid = false;
            return;
        }
            
        // Pull
        int value = -1; // Bucket empty
        while (nextBucket < buckets.length && value == -1) {
            value = buckets[nextBucket];
            nextBucket += 2;
        }
        
        // End of stream
        if (value == -1) {
            currentValue = null;
            currentFrequency = 0;
            currentValid = false;
            return;
        }
        
        // Store
        currentValue = parse(dictionary[value]);
        currentFrequency = buckets[nextBucket - 1];
        currentValid = true;
    }

    /**
     * Parses the given value
     * @param value
     * @return
     */
    protected abstract T parse(String value);
}
