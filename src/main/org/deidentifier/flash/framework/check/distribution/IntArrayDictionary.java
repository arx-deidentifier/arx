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

import java.util.ArrayList;

import org.deidentifier.flash.framework.check.groupify.HashTableUtil;

/**
 * A hash groupify operator.
 * 
 * @author Prasser, Kohlmayer
 */
public class IntArrayDictionary {

    /**
     * Calculates the MURMUR v3 hashcode
     * 
     * @param key
     * @return
     */
    private static final int hashCodeMURMUR(final int[] key) {

        int h1 = 0;

        for (int i = 0; i < key.length; i++) {
            int k1 = key[i];
            k1 *= 0xcc9e2d51;
            k1 = (k1 << 15) | (k1 >>> -15);
            k1 *= 0x1b873593;

            h1 ^= k1;
            h1 = (h1 << 13) | (h1 >>> -13);
            h1 = (h1 * 5) + 0xe6546b64;
        }

        h1 ^= (2 * key.length);
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }

    /** Current number of elements. */
    private int                                      elementCount;

    /** The entry array. */
    private IntArrayDictionaryEntry[]                buckets;

    /** Load factor. */
    private final float                              loadFactor;

    /**
     * maximum number of elements that can be put in this map before having to
     * rehash.
     */
    private int                                      threshold;

    /** The list */
    private final ArrayList<IntArrayDictionaryEntry> list;

    /**
     * Constructs a new dictionary
     * 
     * @param capacity
     *            the capacity
     */
    public IntArrayDictionary(int capacity) {
        list = new ArrayList<IntArrayDictionaryEntry>();
        if ((capacity >= 0) && (0.75f > 0)) {
            capacity = HashTableUtil.calculateCapacity(capacity);
            elementCount = 0;
            buckets = new IntArrayDictionaryEntry[capacity];
            loadFactor = 0.75f;
            threshold = HashTableUtil.calculateThreshold(buckets.length,
                                                         loadFactor);
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
            HashTableUtil.nullifyArray(buckets);
            list.clear();
        }
    }

    /**
     * Creates a new entry.
     * 
     * @param key
     *            the key
     * @param index
     *            the index
     * @param hash
     *            the hash
     * @param value
     *            the value
     * @return the hash groupify entry
     */
    private IntArrayDictionaryEntry createEntry(final int[] key,
                                                final int index,
                                                final int hash) {
        final IntArrayDictionaryEntry entry = new IntArrayDictionaryEntry(key,
                                                                          hash,
                                                                          list.size());
        entry.setNext(buckets[index]);
        buckets[index] = entry;
        list.add(entry);
        return entry;
    }

    /**
     * Removes a element from the dictionary
     * 
     * @param element
     */
    public void decrementRefCount(final int index) {

        final IntArrayDictionaryEntry entry = list.get(index);
        final int refCount = entry.decRefCount();

        if (refCount == 0) { // entry no longer needed remove

            list.set(index, null);

            final int bucketIndex = entry.getHashcode() & (buckets.length - 1);
            IntArrayDictionaryEntry prev = buckets[bucketIndex];
            IntArrayDictionaryEntry e = prev;

            while (e != null) {
                final IntArrayDictionaryEntry next = e.getNext();
                if (e == entry) { // found element
                    elementCount--;
                    if (prev == e) {
                        buckets[bucketIndex] = next;
                    } else {
                        prev.setNext(next);
                    }
                }
                prev = e;
                e = next;
            }

        }
    }

    /**
     * Returns the according entry.
     * 
     * @param key
     *            the key
     * @param index
     *            the index
     * @param keyHash
     *            the key hash
     * @return the hash entry
     */
    private final IntArrayDictionaryEntry findEntry(final int[] key,
                                                    final int index,
                                                    final int keyHash) {
        IntArrayDictionaryEntry m = buckets[index];
        while ((m != null) &&
               ((m.getHashcode() != keyHash) ||
                (key.length != m.getKey().length) || !HashTableUtil.equals(key,
                                                                           m.getKey()))) {
            m = m.getNext();
        }
        return m;
    }

    /**
     * Returns the according entry
     * 
     * @param index
     * @return
     */
    public int[] get(final int index) {
        return list.get(index).getKey();
    }

    /**
     * Probes the dictionary and either inserts a new entry index or returns the
     * corresponding entry index.
     * 
     * @param key
     *            the key
     */
    public int probe(final int[] key) {

        final int hash = hashCodeMURMUR(key);

        int index = hash & (buckets.length - 1);
        IntArrayDictionaryEntry entry = findEntry(key, index, hash);
        if (entry == null) {
            if (++elementCount > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(key, index, hash);
        } else {
            entry.incRefCount();
        }
        return entry.getValue();

    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {

        final int length = HashTableUtil.calculateCapacity((buckets.length == 0 ? 1
                : buckets.length << 1));
        final IntArrayDictionaryEntry[] newData = new IntArrayDictionaryEntry[length];
        for (int i = 0; i < buckets.length; i++) {
            IntArrayDictionaryEntry entry = buckets[i];
            while (entry != null) {
                final IntArrayDictionaryEntry next = entry.getNext();
                final int index = entry.getHashcode() & (length - 1);
                entry.setNext(newData[index]);
                newData[index] = entry;
                entry = next;
            }
        }
        buckets = newData;
        threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);
    }

    /**
     * Returns the element count of the dictionary
     * 
     * @return the int
     */
    public int size() {
        return elementCount;
    }
}
