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
package org.deidentifier.arx.common;


/**
 * A hash groupify operator. It implements a hash table with chaining and keeps
 * track of additional properties per equivalence class
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * 
 * @param <T>
 */
public class Groupify<T> {

    /**
     * Entry
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     * 
     * @param <U>
     */
    public static class Group<U> {

        /** Var */
        private final int hashcode;
        /** Var */
        private final U   element;
        /** Var */
        private int       count       = 0;
        /** Var */
        private Group<U>  next        = null;
        /** Var */
        private Group<U>  nextInOrder = null;

        /**
         * Creates a new instance
         * 
         * @param element
         * @param hashCode
         */
        private Group(U element, int hashCode) {
            this.hashcode = hashCode;
            this.element = element;
        }

        /**
         * The count
         */
        public int getCount() {
            return count;
        }

        /**
         * The element
         */
        public U getElement() {
            return element;
        }

        /**
         * Returns whether a next entry exists
         * 
         * @return
         */
        public boolean hasNext() {
            return nextInOrder != null;
        }

        /**
         * Increments the count
         * @param count
         */
        public void incCount(int count) {
            this.count += count;
        }

        /**
         * Returns the next entry, null if this is the last entry
         * 
         * @return
         */
        public Group<U> next() {
            return nextInOrder;
        }
    }

    /** Current number of elements. */
    private int         count;

    /** The entry array. */
    private Group<T>[]  buckets;

    /** The first entry. */
    private Group<T>    first;

    /** The last entry. */
    private Group<T>    last;

    /** Load factor. */
    private final float loadFactor = 0.75f;

    /**
     * Maximum number of elements that can be put in this map before having to
     * rehash.
     */
    private int         threshold;

    /**
     * Constructs a new hash groupify operator.
     * 
     * @param capacity
     *            The capacity
     */
    @SuppressWarnings("unchecked")
    public Groupify(int capacity) {

        // Set capacity
        capacity = calculateCapacity(capacity);
        this.count = 0;
        this.buckets = new Group[capacity];
        this.threshold = calculateThreshold(buckets.length, loadFactor);
    }

    /**
     * Adds a new element
     * 
     * @param element
     */
    public void add(T element) {

        // Add
        final int hash = element.hashCode();

        // Find or create entry
        int index = hash & (buckets.length - 1);
        Group<T> entry = findEntry(element, index, hash);
        if (entry == null) {
            if (++count > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(element, index, hash);
        }

        // Track size
        entry.count++;
    }

    /**
     * Returns the first entry for iterations
     * 
     * @return
     */
    public Group<T> first() {
        return first;
    }

    /**
     * Returns the matching entry, if any
     * @param element
     * @return
     */
    public Group<T> get(T element) {
        int hash = element.hashCode();
        int index = hash & (buckets.length - 1);
        return findEntry(element, index, hash);
    }

    /**
     * Returns the current number of entries
     * 
     * @return
     */
    public int size() {
        return count;
    }

    /**
     * Calculates a new capacity.
     * 
     * @param x
     *            the parameter
     * @return the capacity
     */
    private int calculateCapacity(int x) {
        if (x >= (1 << 30)) { return 1 << 30; }
        if (x == 0) { return 16; }
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    /**
     * Computes the threshold for rehashing.
     * 
     * @param buckets
     * @param loadFactor
     * @return
     */
    private int calculateThreshold(final int buckets, final float loadFactor) {
        return (int) (buckets * loadFactor);
    }

    /**
     * Creates a new entry
     * 
     * @param element
     * @param index
     * @param hashcode
     * @return
     */
    private Group<T> createEntry(final T element,
                                 final int index,
                                 final int hashcode) {
        final Group<T> entry = new Group<T>(element, hashcode);
        entry.next = buckets[index];
        buckets[index] = entry;
        if (first == null) {
            first = entry;
            last = entry;
        } else {
            last.nextInOrder = entry;
            last = entry;
        }
        return entry;
    }

    /**
     * Returns the according entry, null if there is none
     * 
     * @param element
     * @param index
     * @param hashcode
     * @return
     */
    private final Group<T> findEntry(final T element,
                                     final int index,
                                     final int hashcode) {
        Group<T> m = buckets[index];
        while ((m != null) && ((m.hashcode != hashcode) || !element.equals(m.element))) {
            m = m.next;
        }
        return m;
    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {

        int length = calculateCapacity((buckets.length == 0 ? 1
                : buckets.length << 1));
        @SuppressWarnings("unchecked")
        Group<T>[] newbuckets = new Group[length];
        Group<T> entry = first;
        while (entry != null) {
            int index = entry.hashcode & (length - 1);
            entry.next = newbuckets[index];
            newbuckets[index] = entry;
            entry = entry.nextInOrder;
        }
        buckets = newbuckets;
        threshold = calculateThreshold(buckets.length, loadFactor);
    }
}
