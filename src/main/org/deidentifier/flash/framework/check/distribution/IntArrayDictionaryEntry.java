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

/**
 * Implements an entry.
 * 
 * @author Prasser, Kohlmayer
 */
public class IntArrayDictionaryEntry {

    /** The hashcode of this class. */
    private final int               hashcode;

    /** The key of this class. */
    private final int[]             key;

    /** The value. */
    private final int               value;

    /** The next element in this bucket. */
    private IntArrayDictionaryEntry next;

    /** The reference counter. */
    private int                     refCount;

    /**
     * Creates a new entry.
     * 
     * @param key
     *            the key
     * @param hash
     *            the hash
     * @param value
     *            the value
     */
    public IntArrayDictionaryEntry(final int[] key,
                                   final int hash,
                                   final int value) {
        hashcode = hash;
        this.key = key;
        this.value = value;
        refCount = 1;
        next = null;
    }

    public int decRefCount() {
        refCount--;
        return refCount;
    }

    /**
     * Gets the hashcode of this class.
     * 
     * @return the hashcode of this class
     */
    public int getHashcode() {
        return hashcode;
    }

    /**
     * Gets the key of this class.
     * 
     * @return the key of this class
     */
    public int[] getKey() {
        return key;
    }

    /**
     * Gets the next element in this bucket.
     * 
     * @return the next element in this bucket
     */
    public IntArrayDictionaryEntry getNext() {
        return next;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public int getValue() {
        return value;
    }

    public void incRefCount() {
        refCount++;

    }

    /**
     * Sets the next element in this bucket.
     * 
     * @param next
     *            the new next element in this bucket
     */
    public void setNext(final IntArrayDictionaryEntry next) {
        this.next = next;
    }
}
