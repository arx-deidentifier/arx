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

import java.util.Arrays;

import org.deidentifier.arx.framework.check.groupify.HashTableUtil;

/**
 * This class can be utilized to track the distributions of values. It is backed by a hash table
 * implementing open addressing with linear probing.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Distribution {

    /** The size. */
    private int                 size;
    
    /** The threshold used for rehashing. */
    private int                 threshold;

    /** The elements. Even index contains value, odd index contains frequency */
    private int[]               elements;
    
    /** The sorted element array - used for history entries only. */
    private int[]               packedElements;
    
    /** The sorted frequency array - used for history entries only. */
    private int[]               packedFrequencies;

    /** The loadfactor. */
    private final static float  LOADFACTOR       = 0.75f;

    /** The initial default capacity of the hashtable. */
    private static final int    DEFAULT_CAPACITY = 8;          // power of two

    /**
     * Default constructor.
     */
    public Distribution() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructor used to create frequency set from a history entry.
     *
     * @param element
     * @param frequency
     */
    public Distribution(final int[] element, final int[] frequency) {
        this(element.length);
        for (int i = 0; i < element.length; i++) {
            if (element[i] != -1) {
                this.add(element[i], frequency[i]);
            }
        }
    }

    /**
     * Constructor using next power of two starting at capacity as initial
     * capacity.
     * 
     * @param capacity
     */
    private Distribution(int capacity) {
        capacity = HashTableUtil.calculateCapacity(capacity);
        size = 0;
        elements = new int[capacity << 1];
        Arrays.fill(elements, -1);
        threshold = HashTableUtil.calculateThreshold(capacity, LOADFACTOR);
    }

    /**
     * Adds a element to the hashtable. Frequency value 1.
     * 
     * @param element
     */
    public final void add(final int element) {
        this.add(element, 1);
    }

    /**
     * Clears the table.
     */
    public void clear() {
        Arrays.fill(elements, -1);
        size = 0;
    }

    /**
     * Gets all buckets of the hash table.
     *
     * @return
     */
    public int[] getBuckets() {
        return elements;
    }

    /**
     * Gets all elements of the packed table.
     *
     * @return
     */
    public int[] getPackedElements() {
        return packedElements;
    }

    /**
     * Gets the frequency of the packed table.
     *
     * @return
     */
    public int[] getPackedFrequency() {
        return packedFrequencies;
    }

    /**
     * Merges two frequency sets.
     * 
     * @param other
     */
    public void merge(final Distribution other) {
        final int[] otherElements = other.elements;
        for (int i = 0; i < otherElements.length; i += 2) {
            if (otherElements[i] != -1) {
                this.add(otherElements[i], otherElements[i + 1]);
            }
        }
    }

    /**
     * Merge a frequency set with a history entry.
     *
     * @param elements
     * @param frequency
     */
    public void merge(final int[] elements, final int[] frequency) {
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != -1) {
                this.add(elements[i], frequency[i]);
            }
        }
    }

    /**
     * Packs the frequency table; removes null values and generates
     * sortedElements and sortedFrequency arrays. In case a collission occured
     * this method also sorts the elements.
     */
    public void pack() {
        final int[] sortedelements = new int[size];
        final int[] sortedfrequency = new int[size];
        if (size > 0) {
            // compress & copy
            int count = 0;
            for (int i = 0; i < elements.length; i += 2) {
                if (elements[i] != -1) { // bucket not empty
                    sortedelements[count] = elements[i];
                    sortedfrequency[count] = elements[i + 1];
                    count++;
                }
            }
        }
        this.packedElements = sortedelements;
        this.packedFrequencies = sortedfrequency;

    }

    /**
     * Gets the current size.
     * 
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Adds an element with the given frequency.
     *
     * @param element
     * @param value
     */
    private void add(final int element, final int value) {

        final int mask = (elements.length - 1);
        int index = (element & ((elements.length >> 1) - 1)) << 1; // start at
                                                                   // home
                                                                   // bucket
        while (true) {
            if (elements[index] == -1) { // empty bucket, not found

                elements[index] = element;
                elements[index + 1] = value;
                size++;

                if (size > threshold) {
                    rehash();
                }
                break;
            } else if (elements[index] == element) { // element found
                elements[index + 1] += value;
                break;
            }
            index = (index + 2) & mask; // next bucket
        }

    }

    /**
     * Rehashes the frequency set table.
     */
    private void rehash() {
        final int capacity = HashTableUtil.calculateCapacity(elements.length);

        final int[] newelements = new int[capacity << 1];
        Arrays.fill(newelements, -1);

        final int mask = (newelements.length - 1);
        for (int i = 0; i < elements.length; i += 2) {
            if (elements[i] != -1) { // bucket not empty

                int index = (elements[i] & ((newelements.length >> 1) - 1)) << 1;
                while (true) {
                    if (newelements[index] == -1) { // empty bucket, not found
                        newelements[index] = elements[i];
                        newelements[index + 1] = elements[i + 1];
                        break;
                    }
                    index = (index + 2) & mask; // next bucket
                }
            }
        }

        threshold = (int) (capacity * LOADFACTOR);
        elements = newelements;
    }
}
