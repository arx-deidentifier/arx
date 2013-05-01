/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.framework.check.distribution;

import java.util.Arrays;

import org.deidentifier.flash.framework.check.groupify.HashTableUtil;

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

    /** Log 2 */
    private static final double  log2             = Math.log(2);

    // enables sorting during the pack operation
    // intended to be result in better compression
    // currently disabled due to performance degradation on some datasets
    private static final boolean ENABLE_SORTING   = false;

    /** Keeps track if a collision occurred */
    private boolean              colission_occured;

    /** The size */
    private int                  size;
    /** The threshold used for rehashing */
    private int                  threshold;

    /** The elements. Even index contains value, odd index contains frequency */
    private int[]                elements;
    /** The sorted element array - used for history entries only */
    private int[]                sortedelements;
    /** The sorted frequency array - used for history entries only */
    private int[]                sortedfrequency;

    /** The loadfactor */
    private final static float   LOADFACTOR       = 0.75f;

    /** The initial default capacity of the hashtable */
    private static final int     DEFAULT_CAPACITY = 8;          // power of two

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
        colission_occured = false;
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
            colission_occured = true;
        }

    }

    /**
     * Clears the table
     */
    public void clear() {
        Arrays.fill(elements, -1);
        size = 0;
        colission_occured = false;
    }

    /**
     * Gets all elements of the table sorted.
     * 
     * @return
     */
    public int[] getElements() {
        return sortedelements;
    }

    /**
     * Gets the frequency of the elements
     * 
     * @return
     */
    public int[] getFrequency() {
        return sortedfrequency;
    }

    /**
     * Checks if the frequency set is distinct l - divers.
     * 
     * @param l
     * @return
     */
    public boolean isDistinctLDiverse(final int l) {
        // if less than l values are present skip
        if (size < l) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if the frequency set is entropy l - divers.
     * 
     * @param c
     * @param l
     * @return
     */
    public boolean isEntropyLDiverse(final double log2L, final int l) {

        // if less than l values are present skip
        if (size < l) { return false; }

        // copy and pack
        int totalElements = 0;
        final int[] frequencyCopy = new int[size];
        int count = 0;
        for (int i = 0; i < elements.length; i += 2) {
            if (elements[i] != -1) { // bucket not empty
                final int frequency = elements[i + 1];
                frequencyCopy[count++] = frequency;
                totalElements += frequency;
            }
        }

        double val = 0d;
        for (int i = 0; i < frequencyCopy.length; i++) {
            final double p = ((double) frequencyCopy[i] / (double) totalElements);
            val += p * log2(p);
        }
        val = -val;

        // check
        if (val >= log2L) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the frequency set is c,l - divers.
     * 
     * @param c
     * @param l
     * @return
     */
    public boolean isRecursiveCLDiverse(final double c, final int l) {

        // if less than l values are present skip
        if (size < l) { return false; }

        // copy and pack
        final int[] frequencyCopy = new int[size];
        int count = 0;
        for (int i = 0; i < elements.length; i += 2) {
            if (elements[i] != -1) { // bucket not empty
                frequencyCopy[count++] = elements[i + 1];
            }
        }

        Arrays.sort(frequencyCopy);
        // Compute threshold
        double threshold = 0;
        for (int i = frequencyCopy.length - l; i >= 0; i--) {
            threshold += frequencyCopy[i];
        }
        threshold *= c;

        // Check
        if (frequencyCopy[frequencyCopy.length - 1] >= threshold) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if the frequency set is t - close (equal distance).
     * 
     * @param t
     *            the distance threshold
     * @param initialDistribution
     *            the distribution of the original distinct values
     * @return
     */
    public boolean isTCloseEqualDist(final double t,
                                     final double[] initialDistribution) {

        // calculate emd with equal distance

        final int[] calcArray = new int[initialDistribution.length];

        int totalElements = 0;
        for (int i = 0; i < elements.length; i += 2) {
            if (elements[i] != -1) { // bucket not empty
                final int value = elements[i];
                final int frequency = elements[i + 1];
                calcArray[value] = frequency;
                totalElements += frequency;
            }
        }

        double val = 0d;

        for (int i = 0; i < calcArray.length; i++) {
            val += Math.abs((initialDistribution[i] - ((double) calcArray[i] / (double) totalElements)));
        }

        val /= 2;

        // check
        if (val > t) {
            return false;
        } else {
            return true;
        }

    }

    public boolean isTCloseHierachical(final double t, final int[] tree) {

        // init parameters
        final int totalElementsP = tree[0];
        final int numLeafs = tree[1];
        final double height = tree[2]; // cast to double as it is used in double
                                       // calculations
        final int extraStartPos = numLeafs + 3;
        final int extraEndPos = extraStartPos + numLeafs;

        // Copy and count
        int totalElementsQ = 0;
        for (int i = 0; i < elements.length; i += 2) {
            if (elements[i] != -1) { // bucket not empty
                final int value = elements[i];
                final int frequency = elements[i + 1];
                tree[value + extraStartPos] = frequency;
                totalElementsQ += frequency;
            }
        }
        // Tree data format: #p_count, #leafs, height, freqLeaf_1, ...,
        // freqLeaf_n, extra_1,..., extra_n, [#childs, level, child_1, ...
        // child_x, pos_e, neg_e], ...
        double cost = 0;

        // leafs
        for (int i = extraStartPos; i < extraEndPos; i++) {
            tree[i] = (tree[i - numLeafs] * totalElementsQ) -
                      (tree[i] * totalElementsP); // p_i - q_i
        }

        // innerNodes
        for (int i = extraEndPos; i < tree.length; i++) {
            int pos_e = 0;
            int neg_e = 0;

            final int numChilds = tree[i++];
            final int level = tree[i++];

            // iterate over all children
            for (int j = 0; j < numChilds; j++) {
                // differentiate between first level and rest

                int extra = 0;
                if (level == 1) {
                    extra = tree[tree[i + j]];
                } else {
                    final int extra_child_index = tree[i + j] +
                                                  tree[tree[i + j]] + 2; // pointer
                                                                         // to
                                                                         // the
                                                                         // pos_e
                                                                         // of
                                                                         // node
                    final int pos_child = tree[extra_child_index];
                    final int neg_child = tree[extra_child_index + 1];
                    extra = pos_child - neg_child;
                }

                if (extra > 0) { // positive
                    pos_e += extra;
                } else { // negative
                    neg_e += (-extra);
                }
            }

            // save extras
            i += numChilds; // increment pointer to extra
            tree[i++] = pos_e;
            tree[i] = neg_e;

            // sum
            final double cost_n = (level / height) * Math.min(pos_e, neg_e);
            cost += cost_n;

        }

        cost /= ((double) totalElementsP * (double) totalElementsQ);

        // check
        if (cost > t) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Computes log 2
     * 
     * @param num
     * @return
     */
    private final double log2(final double num) {
        return Math.log(num) / log2;
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

            if (ENABLE_SORTING && colission_occured) {
                // sort elements and frequency
                final IntComparator c = new IntComparator() {
                    @Override
                    public final int compare(final int arg0, final int arg1) {
                        return (sortedelements[arg0] < sortedelements[arg1] ? -1
                                : (sortedelements[arg0] == sortedelements[arg1] ? 0
                                        : 1));
                    }
                };
                final Swapper s = new Swapper() {
                    @Override
                    public final void swap(final int arg0, final int arg1) {
                        final int element = sortedelements[arg0];
                        final int frequencye = sortedfrequency[arg0];
                        sortedelements[arg0] = sortedelements[arg1];
                        sortedfrequency[arg0] = sortedfrequency[arg1];
                        sortedelements[arg1] = element;
                        sortedfrequency[arg1] = frequencye;
                    }
                };
                GenericSorting.mergeSort(0, sortedelements.length, c, s);
            }
        }
        this.sortedelements = sortedelements;
        this.sortedfrequency = sortedfrequency;

    }

    /**
     * Rehashes the frequency set table
     */
    private void rehash() {
        final int capacity = HashTableUtil.calculateCapacity(elements.length);

        final int[] newelements = new int[capacity << 1];
        Arrays.fill(newelements, -1);

        final int mask = (newelements.length - 1);
        colission_occured = false;
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
                    colission_occured = true;
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
