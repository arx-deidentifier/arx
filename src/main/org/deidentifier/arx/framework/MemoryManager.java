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
package org.deidentifier.arx.framework;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Class for direct memory access
 * 
 * @author Fabian Prasser
 */
public class MemoryManager {

    /** The unsafe instance */
    private static final Unsafe unsafe       = getUnsafe();

    /** Monitor amount of off-heat memory */
    private static long         offHeapBytes = 0;
    
    /**
     * Allocate memory
     * @param size
     * @return
     */
    public static long allocateMemory(long size) {
        offHeapBytes += size;
        return unsafe.allocateMemory(size);
    }
    
    /**
     * Copy memory
     * @param from
     * @param to
     * @param size
     */
    public static void copyMemory(long from, long to, long size) {
        unsafe.copyMemory(from, to, size);
    }

    /**
     * Free memory
     * @param address
     * @param size 
     */
    public static void freeMemory(long address, long size) {
        unsafe.freeMemory(address);
        offHeapBytes -= size;
    }

    /**
     * Returns the amount of allocated off-heap bytes
     * @return
     */
    public static final long getAllocatedOffHeapMemory() {
        return offHeapBytes;
    }

    /**
     * Get int
     * @param address
     * @return
     */
    public static int getInt(long address) {
        return unsafe.getInt(address);
    }

    /**
     * Get long
     * @param address
     * @return
     */
    public static long getLong(long address) {
        return unsafe.getLong(address);
    }

    /**
     * Put int
     * @param address
     * @param value
     */
    public static void putInt(long address, int value) {
        unsafe.putInt(address, value);
    }

    /**
     * Put long
     * @param address
     * @param value
     */
    public static void putLong(long address, long value) {
        unsafe.putLong(address, value);
    }

    /**
     * Set memory
     * @param address
     * @param size
     * @param value
     */
    public static void setMemory(long address, long size, byte value) {
        unsafe.setMemory(address, size, value);
    }

    /**
     * Access unsafe
     * @return
     */
    private static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Error accessing off-heap memory!", e);
        }
    }
}
