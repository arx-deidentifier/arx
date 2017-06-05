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

import java.util.Arrays;

import org.deidentifier.arx.framework.check.groupify.HashTableUtil;

import com.carrotsearch.hppc.LongArrayList;

/**
 * A hash groupify operator.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class IntArrayDictionary {

    /** The entry array. */
    private long[]              buckets;

    /** Current number of elements. */
    private int                 elementCount;

    /** The list. */
    private final LongArrayList list;

    /** Load factor. */
    private final float         loadFactor;

    /** Maximum number of elements that can be put in this map before having to rehash. */
    private int                 threshold;

    /**
     * Constructs a new dictionary.
     *
     * @param capacity the capacity
     */
    public IntArrayDictionary(int capacity) {
        list = new LongArrayList(capacity);
        if ((capacity >= 0) && (0.75f > 0)) {
            capacity = HashTableUtil.calculateCapacity(capacity);
            elementCount = 0;
            buckets = new long[capacity];
            Arrays.fill(buckets, -1);
            loadFactor = 0.75f;
            threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Clears the dictionary.
     */
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            for (long address : buckets) {
                while (address != -1) {
                    long next = IntArrayDictionaryEntry.getNext(address);
                    IntArrayDictionaryEntry.free(address);
                    address = next;
                }
            }
            Arrays.fill(buckets, -1);
            list.clear();
        }
    }

    /**
     * Removes a element from the dictionary.
     *
     * @param index
     */
    public void decrementRefCount(final int index) {

        final long entry = list.get(index);
        final int refCount = IntArrayDictionaryEntry.decRefCount(entry);

        if (refCount == 0) { // entry no longer needed remove

            list.set(index, -1);
            final int bucketIndex = IntArrayDictionaryEntry.getHashCode(entry) & (buckets.length - 1);
            long prev = buckets[bucketIndex];
            long e = prev;
            while (e != -1) {
                final long next = IntArrayDictionaryEntry.getNext(e);
                if (e == entry) { // found element
                    elementCount--;
                    if (prev == e) {
                        buckets[bucketIndex] = next;
                    } else {
                        IntArrayDictionaryEntry.setNext(prev, next);
                    }
                }
                prev = e;
                e = next;
            }
            IntArrayDictionaryEntry.free(entry);
        }
    }

    /**
     * Returns the according entry.
     *
     * @param index
     * @return
     */
    public long get(final int index) {
        return list.get(index);
    }

    /**
     * Probes the dictionary and either inserts a new entry or returns the
     * corresponding entry index.
     *
     * @param address Address of the entry to insert
     * @return
     */
    public int probe(final long address) {

        int hash = IntArrayDictionaryEntry.getHashCode(address);
        int index = hash & (buckets.length - 1);
        long entry = findEntry(address, index, hash);
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(address, index, hash);
        } else {
            IntArrayDictionaryEntry.free(address);
            IntArrayDictionaryEntry.incRefCount(entry);
        }
        return IntArrayDictionaryEntry.getValue(entry);
    }

    /**
     * Returns the element count of the dictionary.
     *
     * @return the int
     */
    public int size() {
        return elementCount;
    }

    /**
     * Creates a new entry.
     *
     * @param address the address
     * @param index the index
     * @param hash the hash
     * @return the hash groupify entry
     */
    private long createEntry(final long address,
                             final int index,
                             final int hash) {
        IntArrayDictionaryEntry.setValue(address, list.size());
        IntArrayDictionaryEntry.setNext(address, buckets[index]);
        buckets[index] = address;
        list.add(address);
        return address;
    }

    /**
     * Returns the according entry.
     * 
     * @param address the address
     * @param index the index
     * @param keyHash the key hash
     * @return the hash entry
     */
    private long findEntry(final long address,
                                 final int index,
                                 final int keyHash) {
        long m = buckets[index];
        while ((m != -1) &&
               ((IntArrayDictionaryEntry.getHashCode(m) != keyHash) ||
               (!IntArrayDictionaryEntry.arrayEquals(m, address)))) {
            m = IntArrayDictionaryEntry.getNext(m);
        }
        return m;
    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {

        final int length = HashTableUtil.calculateCapacity((buckets.length == 0 ? 1 : buckets.length << 1));
        final long[] newData = new long[length];
        Arrays.fill(newData, -1);
        for (int i = 0; i < buckets.length; i++) {
            long entry = buckets[i];
            while (entry != -1) {
                final long next = IntArrayDictionaryEntry.getNext(entry);
                final int index = IntArrayDictionaryEntry.getHashCode(entry) & (length - 1);
                IntArrayDictionaryEntry.setNext(entry, newData[index]);
                newData[index] = entry;
                entry = next;
            }
        }
        buckets = newData;
        threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);
    }
}
