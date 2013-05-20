/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.check.distribution;

import java.util.Arrays;

import org.deidentifier.arx.framework.check.groupify.HashTableUtil;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * A distribution implementation. Used for l-diversity and t-closeness. It keeps
 * track of the frequencies of the sensitive values.
 * 
 * @author Prasser, Kohlmayer
 */
public class Distribution {

    /** The size */
    private int                 size;
    /** The threshold used for rehashing */
    private int                 threshold;

    /** The elements. Even index contains value, odd index contains frequency */
    private int[]               elements;
    /** The sorted element array - used for history entries only */
    private int[]               packedElements;
    /** The sorted frequency array - used for history entries only */
    private int[]               packedFrequencies;

    /** The loadfactor */
    private final static float  LOADFACTOR       = 0.75f;

    /** The initial default capacity of the hashtable */
    private static final int    DEFAULT_CAPACITY = 8;          // power of two

    /**
     * Default constructor.
     */
    public Distribution() {
        this(DEFAULT_CAPACITY);
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
     * Constructor used to create frequency set from a history entry
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
     * Adds a element to the hashtable. Frequency value 1.
     * 
     * @param element
     */
    public final void add(final int element) {
        this.add(element, 1);
    }

    /**
     * Adds an element with the given frequency
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
     * Clears the table
     */
    public void clear() {
        Arrays.fill(elements, -1);
        size = 0;
    }

    /**
     * Gets all buckets of the hash table
     * 
     * @return
     */
    public int[] getBuckets() {
        return elements;
    }

    /**
     * Gets all elements of the packed table
     * 
     * @return
     */
    public int[] getPackedElements() {
        return packedElements;
    }

    /**
     * Gets the frequency of the packed table
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
     * Merge a frequency set with a history entry
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
     * Rehashes the frequency set table
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

    /**
     * Gets the current size.
     * 
     * @return
     */
    public int size() {
        return size;
    }
}
