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

import java.util.concurrent.atomic.AtomicBoolean;

import org.deidentifier.arx.framework.MemoryManager;

/**
 * A fast implementation of an array of arrays of equal size
 * 
 * @author Fabian Prasser
 */
public class DataMatrix {
    
    /** Debugging flag*/
    private static final boolean DEBUG = false;

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

    /** Write access */
    private long                writeBaseAddress   = 0;

    /**
     * Instantiates a new memory block. The block will *not* be initialized.
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
        
        // Initialize
        this.columns = columns;
        this.rows = rows;
        this.rowSizeInLongs = (int) (Math.ceil(columns / 2d));
        this.rowSizeInBytes = rowSizeInLongs * 8;
        this.size = rowSizeInBytes * rows;
        this.baseAddress = MemoryManager.allocateMemory(size);
    }

    /**
     * ANDs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void and(int row, long value) {
        checkRow(row);
        long address = this.baseAddress + row * this.rowSizeInBytes;
        MemoryManager.putLong(address, MemoryManager.getLong(address) & value);
    }

    @Override
    public DataMatrix clone() {
        DataMatrix result = new DataMatrix(this.rows, this.columns);
        MemoryManager.copyMemory(this.baseAddress, result.baseAddress, this.size);
        return result;
    }
    
    /**
     * Copies a row from the given matrix into this matrix
     * @param row
     * @param sourceMatrix
     * @param sourceRow
     */
    public void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow) {
        checkRow(row);
        checkRow(sourceRow);
        MemoryManager.copyMemory(sourceMatrix.baseAddress + sourceRow * this.rowSizeInBytes, 
                          this.baseAddress + row * this.rowSizeInBytes, 
                          this.rowSizeInBytes);
    }

    /**
     * Compares two rows for equality
     * @param row1
     * @param row2
     * @return
     */
    public boolean equals(final int row1, final int row2) {
        checkRow(row1);
        checkRow(row2);
        return equals(row1, row2, ~0L);
    }

    /**
     * Returns whether the given row has the given data
     * @param row
     * @param data
     * @return
     */
    public boolean equals(int row, int[] data) {
        checkRow(row);
        checkColumn(data.length - 1);
        long address = this.baseAddress + row * this.rowSizeInBytes;
        for (int i = 0; i < data.length; i++) {
            if (MemoryManager.getInt(address) != data[i]) {
                return false;
            } else {
                address += 4;
            }
        }
        return true;
    }

    /**
     * Equals ignoring outliers
     * @param row1
     * @param row2
     * @return
     */
    public boolean equalsIgnoringOutliers(int row1, int row2) {
        checkRow(row1);
        checkRow(row2);
        return this.equals(row1, row2, Data.REMOVE_OUTLIER_MASK_LONG);
    }
    
    /**
     * Frees the backing off-heap memory
     */
    public void free() {
        if (!freed.compareAndSet(false, true)) return;
        MemoryManager.freeMemory(baseAddress, size);
    }

    /**
     * Returns the specified value
     * @param row
     * @param col
     * @return
     */
    public int get(final int row, final int col) {
        checkRow(row);
        checkColumn(col);
        return MemoryManager.getInt(this.baseAddress + (row * this.rowSizeInBytes) + (col << 2));
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
     * Sets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public int getValueAtColumn(int column) {
        checkColumn(column);
        return MemoryManager.getInt(this.writeBaseAddress + (column << 2));
    }

    /**
     * Returns an hashcode for the given row
     * @param row
     * @return
     */
    public int hashCode(final int row) {
        checkRow(row);
        long address = baseAddress + row * rowSizeInBytes;
        int result = 23;
        
        for (int i = 0; i < columns; i++) {
            result = (37 * result) + MemoryManager.getInt(address);
            address += 4;
        }
        return result;        
    }
    
    /**
     * Computes a hashcode for an integer array, partially unrolled.
     * 
     * @param array
     * @return the hashcode
     */
    public final int hashCode(final int[] array) {
        final int len = array.length;
        int result = 23;
        int i = 0;
        // Do blocks of four ints unrolled.
        for (; (i + 3) < len; i += 4) {
            result = (1874161 * result) + // 37 * 37 * 37 * 37 
                     (50653 * array[i]) + // 37 * 37 * 37
                     (1369 * array[i + 1]) + // 37 * 37
                     (37 * array[i + 2]) +
                     array[i + 3];
        }
        // Do the rest
        for (; i < len; i++) {
            result = (37 * result) + array[i];
        }
        return result;
    }
    
    /**
     * First iterator
     * @param row
     */
    public void iterator1(int row) {
        checkRow(row);
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
        int result = MemoryManager.getInt(iterator_1_address);
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
        MemoryManager.putInt(iterator_1_address, value);
        iterator_1_address += 4;
        iterator_1_i++;
    }

    /**
     * First iterator
     * @param row
     */
    public void iterator2(int row) {
        checkRow(row);
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
        int result = MemoryManager.getInt(iterator_2_address);
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
        MemoryManager.putInt(iterator_2_address, value);
        iterator_2_address += 4;
        iterator_2_i++;
    }

    /**
     * ORs the first value of the row with the given value
     * @param row
     * @param removeOutlierMaskLong
     */
    public void or(int row, long value) {
        checkRow(row);
        long address = this.baseAddress + row * this.rowSizeInBytes;
        MemoryManager.putLong(address, MemoryManager.getLong(address) | value);
    }

    /**
     * Sets a value
     * @param row
     * @param column
     * @param value
     */
    public void set(int row, int column, int value) {
        checkRow(row);
        checkColumn(column);
        long address = this.baseAddress + row * this.rowSizeInBytes + column * 4;
        MemoryManager.putInt(address, value);
    }

    /**
     * Sets the row index for data access
     * @param row
     */
    public void setRow(int row) {
        checkRow(row);
        this.writeBaseAddress = this.baseAddress + row * this.rowSizeInBytes;
    }

    /**
     * Sets the data for one row
     * @param row
     * @param data
     */
    public void setRow(int row, int[] data) {
        checkRow(row);
        long address = this.baseAddress + row * this.rowSizeInBytes;
        for (int i = 0; i < data.length; i++) {
            MemoryManager.putInt(address, data[i]);
            address += 4;
        }
    }

    /**
     * Sets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public void setValueAtColumn(int column, int value) {
        checkColumn(column);
        MemoryManager.putInt(this.writeBaseAddress + (column << 2), value);
    }

    /**
     * Swaps the data in both rows
     * @param row1
     * @param row2
     */
    public void swap(int row1, int row2) {
        checkRow(row1);
        checkRow(row2);
        long address1 = this.baseAddress + row1 * this.rowSizeInBytes;
        long address2 = this.baseAddress + row2 * this.rowSizeInBytes;
        for (int i = 0; i < this.rowSizeInLongs; i++) {
            long temp = MemoryManager.getLong(address1);
            MemoryManager.putLong(address1, MemoryManager.getLong(address2));
            MemoryManager.putLong(address2, temp);
            address1 += 8;
            address2 += 8;
        }
    }

    /**
     * Parameter check
     * @param column
     */
    @SuppressWarnings("unused")
    private void checkColumn(int column) {
        if (DEBUG && (column < 0 || column > columns -1)) {
            throw new IllegalArgumentException("Column out of bounds: " + column + " max: " + columns);
        }    
    }

    /**
     * Parameter check
     * @param row
     */
    @SuppressWarnings("unused")
    private void checkRow(int row) {
        if (DEBUG && (row < 0 || row > rows -1)) {
            throw new IllegalArgumentException("Row out of bounds: " + row + " max: " + rows);
        }    
    }
 
    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param flag
     * @return
     */
    private boolean equals(int row1, int row2, long flag) {

        long base1 = baseAddress + (row1 * rowSizeInBytes);
        long base2 = baseAddress + (row2 * rowSizeInBytes);

        switch (rowSizeInLongs) {
        case 10:
            if (MemoryManager.getLong(base1 + 72) != MemoryManager.getLong(base2 + 72)) {
                return false;
            }
        case 9:
            if (MemoryManager.getLong(base1 + 64) != MemoryManager.getLong(base2 + 64)) {
                return false;
            }
        case 8:
            if (MemoryManager.getLong(base1 + 56) != MemoryManager.getLong(base2 + 56)) {
                return false;
            }
        case 7:
            if (MemoryManager.getLong(base1 + 48) != MemoryManager.getLong(base2 + 48)) {
                return false;
            }
        case 6:
            if (MemoryManager.getLong(base1 + 40) != MemoryManager.getLong(base2 + 40)) {
                return false;
            }
        case 5:
            if (MemoryManager.getLong(base1 + 32) != MemoryManager.getLong(base2 + 32)) {
                return false;
            }
        case 4:
            if (MemoryManager.getLong(base1 + 24) != MemoryManager.getLong(base2 + 24)) {
                return false;
            }
        case 3:
            if (MemoryManager.getLong(base1 + 16) != MemoryManager.getLong(base2 + 16)) {
                return false;
            }
        case 2:
            if (MemoryManager.getLong(base1 + 8) != MemoryManager.getLong(base2 + 8)) {
                return false;
            }
        case 1:
            if ((MemoryManager.getLong(base1) & flag) != (MemoryManager.getLong(base2) & flag)) {
                return false;
            }
            break;
        default:
            final long end1 = base1 + rowSizeInBytes;

            // Check with flag
            if ((MemoryManager.getLong(base1) & flag) != (MemoryManager.getLong(base2) & flag)) {
                return false;
            }
            base1 += 8;
            base2 += 8;
            
            // Check without flag
            long address2 = base2;
            for (long address = base1; address < end1; address += 8) {
                if (MemoryManager.getLong(address) != MemoryManager.getLong(address2)) {
                    return false;
                }
                address2 += 8;
            }
        }
        return true;
    }

    /**
     * Clones only a subset of the records
     * @param subset
     * @return
     */
    protected DataMatrix clone(int[] subset) {
        
        // Create instance
        DataMatrix result = new DataMatrix(subset.length, this.columns);
        
        // Copy subset
        long targetAddress = result.baseAddress;
        for (int source : subset) {
            long sourceAddress = this.baseAddress + source * rowSizeInBytes;
            MemoryManager.copyMemory(sourceAddress, targetAddress, rowSizeInBytes);
            targetAddress += rowSizeInBytes;
        }
        
        // Return
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        free();
    }
}