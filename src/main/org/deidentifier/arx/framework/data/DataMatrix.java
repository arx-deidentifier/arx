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

package org.deidentifier.arx.framework.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import sun.misc.Unsafe;

/**
 * A fast implementation of an array of arrays of equal size
 * 
 * @author Fabian Prasser
 */
public class DataMatrix {
    
    /**
     * An exclusive row iterator
     * 
     * @author Fabian Prasser
     */
    public class ExclusiveRowIterator {
        
        /** Iterate */
        private int                 i       = 0;
        /** Iterate */
        private long                address = 0; 

        /**
         * Creates a new instance
         * @param row
         */
        ExclusiveRowIterator(int row) {
            address = baseAddress + row * rowSizeInBytes;
            i = 0;
        }

        /**
         * First iterator
         * @return
         */
        public boolean hasNext() {
            return i < columns;
        }
        
        /**
         * First iterator
         * @return
         */
        public int next() {
            int result = unsafe.getInt(address);
            address += 4;
            i++;
            return result;
        }
        
        /**
         * First iterator
         * @param value
         * @return
         */
        public void write(int value) {
            unsafe.putInt(address, value);
            address += 4;
            i++;
        }
    }

    private static final List<DataMatrix> MATRICES = Collections.synchronizedList(new ArrayList<DataMatrix>());
    
    /**
     * Returns the total off-heap memory
     * @return
     */
    public static long getTotalOffHeapMemory() {
        long result = 0L;
        synchronized(MATRICES) {
            for (DataMatrix matrix : MATRICES) {
                result += matrix.getOffHeapByteSize();
            }
        }
        return result;
    }

    /** The unsafe. */
    private final Unsafe        unsafe             = getUnsafe();

    /** The base address of the memory field in bytes. */
    private final long          baseAddress;

    /** The size in bytes of one row. */
    private final long          rowSizeInBytes;

    /** The size in longs of one row. */
    private final int           rowSizeInLongs;

    /** The total size of the allocated memory. */
    private final long          size;

    /** Flag to indicate if the allocated memory has been freed. */
    private final AtomicBoolean freed              = new AtomicBoolean(false);

    /** The number of rows. */
    private final int           rows;

    /** The number of columns. */
    private final int           columns;

    /** Iterate */
    private int                 iterator_1_i       = 0;

    /** Iterate */
    private long                iterator_1_address = 0;

    /** Iterate */
    private int                 iterator_2_i       = 0;
    
    /** Iterate */
    private long                iterator_2_address = 0;

    /**
     * Instantiates a new memory.
     *
     * @param rows the num rows
     * @param columns the num columns
     */
    public DataMatrix(final int rows, final int columns) {
        
        // Special case
        if (rows == 0 && columns == 0) {
            freed.set(true);
            this.columns = 0;
            this.rows = 0;
            this.rowSizeInLongs = 0;
            this.rowSizeInBytes = 0;
            this.size = 0;
            this.baseAddress = -1;
            return;
        }
        
        // Register
        MATRICES.add(this);

        // Initialize
        this.columns = columns;
        this.rows = rows;
        this.rowSizeInLongs = (int) (Math.ceil(columns / 2d));
        this.rowSizeInBytes = rowSizeInLongs * 8;
        this.size = rowSizeInBytes * rows;
        this.baseAddress = unsafe.allocateMemory(size);
        this.unsafe.setMemory(baseAddress, size, (byte) 0);
    }

    /**
     * Compares two rows for equality
     * @param row1
     * @param row2
     * @return
     */
    public boolean equals(final int row1, final int row2) {
        
        final long base1 = baseAddress + (row1 * rowSizeInBytes);
        final long base2 = baseAddress + (row2 * rowSizeInBytes);

        switch (rowSizeInLongs) {
        case 10:
            if (unsafe.getLong(base1 + 72) != unsafe.getLong(base2 + 72)) {
                return false;
            }
        case 9:
            if (unsafe.getLong(base1 + 64) != unsafe.getLong(base2 + 64)) {
                return false;
            }
        case 8:
            if (unsafe.getLong(base1 + 56) != unsafe.getLong(base2 + 56)) {
                return false;
            }
        case 7:
            if (unsafe.getLong(base1 + 48) != unsafe.getLong(base2 + 48)) {
                return false;
            }
        case 6:
            if (unsafe.getLong(base1 + 40) != unsafe.getLong(base2 + 40)) {
                return false;
            }
        case 5:
            if (unsafe.getLong(base1 + 32) != unsafe.getLong(base2 + 32)) {
                return false;
            }
        case 4:
            if (unsafe.getLong(base1 + 24) != unsafe.getLong(base2 + 24)) {
                return false;
            }
        case 3:
            if (unsafe.getLong(base1 + 16) != unsafe.getLong(base2 + 16)) {
                return false;
            }
        case 2:
            if (unsafe.getLong(base1 + 8) != unsafe.getLong(base2 + 8)) {
                return false;
            }
        case 1:
            if (unsafe.getLong(base1) != unsafe.getLong(base2)) {
                return false;
            }
            break;
        default:
            final long end1 = base1 + rowSizeInBytes;
            long address2 = base2;

            for (long address = base1; address < end1; address += 8) {
                if (unsafe.getLong(address) != unsafe.getLong(address2)) {
                    return false;
                }
                address2 += 8;
            }
        }
        return true;
    }

    /**
     * Frees the backing off-heap memory
     */
    public void free() {
        if (!freed.compareAndSet(false, true)) return;
        unsafe.freeMemory(baseAddress);
        MATRICES.remove(this);
    }
    
    /**
     * Returns the specified value
     * @param row
     * @param col
     * @return
     */
    public int get(final int row, final int col) {
        return unsafe.getInt((row * rowSizeInBytes) + col * 4);
    }

    /**
     * This returns an iterator for parallel access, e.g. in
     * multithreaded environments
     * 
     * @param row
     * @return
     */
    public ExclusiveRowIterator getExclusiveIterator(int row) {
       return new ExclusiveRowIterator(row); 
    }

    /**
     * Returns the number of columns
     * @return
     */
    public int getNumColumns() {
        return columns;
    }

    /**
     * Returns the number of rows
     * @return
     */
    public int getNumRows() {
        return rows;
    }
    
    /**
     * Returns the size in bytes
     * @return
     */
    public long getOffHeapByteSize() {
        return this.size;
    }

    /**
     * Returns an hashcode for the given row
     * @param row
     * @return
     */
    public int hashCode(final int row) {
        long address = baseAddress + row * rowSizeInBytes;
        int result = 23;
        
        for (int i = 0; i < columns; i++) {
            result = (37 * result) + unsafe.getInt(address);
            address += 4;
        }
        return result;        
    }

    /**
     * First iterator
     * @param row
     */
    public void iterator1(int row) {
        iterator_1_address = baseAddress + row * rowSizeInBytes;
        iterator_1_i = 0;
    }

    /**
     * First iterator
     * @return
     */
    public boolean iterator1_hasNext() {
        return iterator_1_i < columns;
    }
    
    /**
     * First iterator
     * @return
     */
    public int iterator1_next() {
        int result = unsafe.getInt(iterator_1_address);
        iterator_1_address += 4;
        iterator_1_i++;
        return result;
    }

    /**
     * First iterator
     * @param value
     * @return
     */
    public void iterator1_write(int value) {
        unsafe.putInt(iterator_1_address, value);
        iterator_1_address += 4;
        iterator_1_i++;
    }

    /**
     * First iterator
     * @param row
     */
    public void iterator2(int row) {
        iterator_2_address = baseAddress + row * rowSizeInBytes;
        iterator_2_i = 0;
    }

    /**
     * First iterator
     * @return
     */
    public boolean iterator2_hasNext() {
        return iterator_2_i < columns;
    }
    
    /**
     * First iterator
     * @return
     */
    public int iterator2_next() {
        int result = unsafe.getInt(iterator_2_address);
        iterator_2_address += 4;
        iterator_2_i++;
        return result;
    }
    
    /**
     * First iterator
     * @param value
     * @return
     */
    public void iterator2_write(int value) {
        unsafe.putInt(iterator_2_address, value);
        iterator_2_address += 4;
        iterator_2_i++;
    }

    /**
     * Access unsafe
     * @return
     */
    private Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Error accessing off-heap memory!", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        free();
    }
}