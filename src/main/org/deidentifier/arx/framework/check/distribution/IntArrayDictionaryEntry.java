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

import org.deidentifier.arx.framework.MemoryManager;

/**
 * Implements an entry.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class IntArrayDictionaryEntry {

    /*
     * Memory layout
     * field-1: 4 bytes: int hashcode
     * field-2: 4 bytes: int value
     * field-3: 4 bytes: int refcount
     * field-4: 8 bytes: long next entry in bucket
     * field-5: 4 bytes: int array-length
     * field-6: 4*x bytes: array: array-length * int 
     */
    private static final int HASHCODE_OFFSET     = 0;
    private static final int VALUE_OFFSET        = 4;
    private static final int REFCOUNT_OFFSET     = 8;
    private static final int NEXT_OFFSET         = 12;
    private static final int ARRAY_LENGTH_OFFSET = 20;
    private static final int ARRAY_OFFSET        = 24;

    /**
     * Allocates a new instance
     * @param size
     * @return
     */
    public static long allocate(final int size) {
        long address = MemoryManager.allocateMemory(ARRAY_OFFSET + (size << 2));
        MemoryManager.putInt(address + ARRAY_LENGTH_OFFSET, size);
        MemoryManager.putInt(address + REFCOUNT_OFFSET, 1);
        return address;
    }
    
    public static int getHashCode(long address) {
        return MemoryManager.getInt(address + HASHCODE_OFFSET);
    }
    
    public static int getValue(long address) {
        return MemoryManager.getInt(address + VALUE_OFFSET);
    }

    public static void setValue(long address, int value) {
        MemoryManager.putInt(address + VALUE_OFFSET, value);
    }
    
    public static void incRefCount(long address) {
        MemoryManager.putInt(address + REFCOUNT_OFFSET, MemoryManager.getInt(address + REFCOUNT_OFFSET) + 1);
    }

    public static int decRefCount(long address) {
        int value = MemoryManager.getInt(address + REFCOUNT_OFFSET) - 1;
        MemoryManager.putInt(address + REFCOUNT_OFFSET, value);
        return value;
    }
    
    public static long getNext(long address) {
        return MemoryManager.getLong(address + NEXT_OFFSET);
    }

    public static void setNext(long address, long value) {
        MemoryManager.putLong(address + NEXT_OFFSET, value);
    }
    
    public static int getArrayLength(long address) {
        return MemoryManager.getInt(address + ARRAY_LENGTH_OFFSET);
    }
    
    public static long getArrayAddress(long address) {
        return address + ARRAY_OFFSET;
    }
    
    public static long setArrayEntry(long address, int value) {
        MemoryManager.putInt(address, value);
        address += 4;
        return address;
    }
    
    public static int getArray(long address, int index) {
        return MemoryManager.getInt(address + ARRAY_OFFSET + (index << 2));
    }
    
    public static void calculateHashCode(long address) {

        int h1 = 0;
        int length = getArrayLength(address);
        long start = address + ARRAY_OFFSET;
        long end = start + (length << 2);

        for (long pointer = start; pointer < end; pointer++) {
            int k1 = MemoryManager.getInt(pointer);
            k1 *= 0xcc9e2d51;
            k1 = (k1 << 15) | (k1 >>> -15);
            k1 *= 0x1b873593;

            h1 ^= k1;
            h1 = (h1 << 13) | (h1 >>> -13);
            h1 = (h1 * 5) + 0xe6546b64;
        }

        h1 ^= (2 * length);
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        MemoryManager.putInt(address + HASHCODE_OFFSET, h1);
    }

    /**
     * Equals
     * @param address1
     * @param address2
     * @return
     */
    public static boolean arrayEquals(long address1, long address2) {
        int length1 = getArrayLength(address1);
        int length2 = getArrayLength(address2);
        if (length1 != length2) {
            return false;
        }
        address1 += ARRAY_OFFSET;
        address2 += ARRAY_OFFSET;
        for (int i = 0; i < length1; i++) {
            if (MemoryManager.getInt(address1) != MemoryManager.getInt(address2)) {
                return false;
            }
            address1 +=4;
            address2 +=4;
        }
        return true;
    }

    /**
     * Free
     * @param address
     */
    public static void free(long address) {
        long size = ARRAY_OFFSET + (getArrayLength(address) << 2);
        MemoryManager.freeMemory(address, size);
    }
}
