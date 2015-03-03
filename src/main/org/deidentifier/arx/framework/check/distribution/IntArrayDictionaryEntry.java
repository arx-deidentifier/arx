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

/**
 * Implements an entry.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
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

    /**
     * 
     *
     * @return
     */
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

    /**
     * 
     */
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
