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

package org.deidentifier.arx.framework.data;

import java.io.Serializable;


/**
 * A fast implementation of an array of arrays of equal size
 * 
 * @author Fabian Prasser
 */
public class DataMatrix implements Serializable {

    /** SVUID */
    private static final long serialVersionUID = 1626391500373995527L;

    /** Backing array */
    private final int[]       array;

    /** The number of rows. */
    private final int         rows;

    /** The number of columns. */
    private final int         columns;

    /** Iterate */
    private int               iteratorI        = 0;

    /** Iterate */
    private int               iteratorOffset   = 0;

    /** Iterate */
    private int               baseOffset       = 0;

    /**
     * Instantiates a new memory block.
     *
     * @param rows the num rows
     * @param columns the num columns
     */
    public DataMatrix(final int rows, final int columns) {
        this.columns = columns;
        this.rows = rows;
        this.array = new int[columns * rows];
    }

    /**
     * ANDs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void and(int row, int value) {
        array[row * columns] &= value;
    }

    @Override
    public DataMatrix clone() {
        DataMatrix result = new DataMatrix(this.rows, this.columns);
        System.arraycopy(this.array, 0, result.array, 0, this.array.length);
        return result;
    }
    
    /**
     * Copies a row from the given matrix into this matrix
     * @param row
     * @param sourceMatrix
     * @param sourceRow
     */
    public void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow) {
        int sourceOffset = sourceRow * columns;
        int thisOffset = row * columns;
        System.arraycopy(sourceMatrix.array, sourceOffset, this.array, thisOffset, columns);
    }

    /**
     * Compares two rows for equality
     * @param row1
     * @param row2
     * @return
     */
    public boolean equals(final int row1, final int row2) {
        return equals(row1, row2, ~0);
    }

    /**
     * Returns whether the given row has the given data
     * @param row
     * @param data
     * @return
     */
    public boolean equals(int row, int[] data) {
        int offset = row * columns;
        for (int i = 0; i < columns; i++) {
            if (this.array[offset++] != data[i]) { 
                return false; 
            }
        }
        return true;
    }

    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param ignore
     * @return
     */
    public boolean equalsIgnore(int row1, int row2, int ignore) {

        int offset1 = row1 * columns;
        int offset2 = row2 * columns;

        switch (columns) {
        case 20:
            if ((ignore != 19) && this.array[offset1 + 19] != this.array[offset2 + 19]) {
                return false;
            }
        case 19:
            if ((ignore != 18) && this.array[offset1 + 18] != this.array[offset2 + 18]) {
                return false;
            }
        case 18:
            if ((ignore != 17) && this.array[offset1 + 17] != this.array[offset2 + 17]) {
                return false;
            }
        case 17:
            if ((ignore != 16) && this.array[offset1 + 16] != this.array[offset2 + 16]) {
                return false;
            }
        case 16:
            if ((ignore != 15) && this.array[offset1 + 15] != this.array[offset2 + 15]) {
                return false;
            }
        case 15:
            if ((ignore != 14) && this.array[offset1 + 14] != this.array[offset2 + 14]) {
                return false;
            }
        case 14:
            if ((ignore != 13) && this.array[offset1 + 13] != this.array[offset2 + 13]) {
                return false;
            }
        case 13:
            if ((ignore != 12) && this.array[offset1 + 12] != this.array[offset2 + 12]) {
                return false;
            }
        case 12:
            if ((ignore != 11) && this.array[offset1 + 11] != this.array[offset2 + 11]) {
                return false;
            }
        case 11:
            if ((ignore != 10) && this.array[offset1 + 10] != this.array[offset2 + 10]) {
                return false;
            }
        case 10:
            if ((ignore != 9) && this.array[offset1 + 9] != this.array[offset2 + 9]) {
                return false;
            }
        case 9:
            if ((ignore != 8) && this.array[offset1 + 8] != this.array[offset2 + 8]) {
                return false;
            }
        case 8:
            if ((ignore != 7) && this.array[offset1 + 7] != this.array[offset2 + 7]) {
                return false;
            }
        case 7:
            if ((ignore != 6) && this.array[offset1 + 6] != this.array[offset2 + 6]) {
                return false;
            }
        case 6:
            if ((ignore != 5) && this.array[offset1 + 5] != this.array[offset2 + 5]) {
                return false;
            }
        case 5:
            if ((ignore != 4) && this.array[offset1 + 4] != this.array[offset2 + 4]) {
                return false;
            }
        case 4:
            if ((ignore != 3) && this.array[offset1 + 3] != this.array[offset2 + 3]) {
                return false;
            }
        case 3:
            if ((ignore != 2) && this.array[offset1 + 2] != this.array[offset2 + 2]) {
                return false;
            }
        case 2:
            if ((ignore != 1) && this.array[offset1 + 1] != this.array[offset2 + 1]) {
                return false;
            }
        case 1:
            if ((ignore != 0) && (this.array[offset1 + 0]) != (this.array[offset2 + 0])) {
                return false;
            }
            break;
        default:
            if ((ignore != 0) && (this.array[offset1]) != (this.array[offset2] )) {
                return false;
            }
            for (int i = 1; i < columns; i++) {
                if ((ignore != i) && this.array[offset1 + i] != this.array[offset2 + i]) {
                    return false;
                }
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
        return this.equals(row1, row2, Data.REMOVE_OUTLIER_MASK);
    }

    /**
     * Returns the specified value
     * @param row
     * @param col
     * @return
     */
    public int get(final int row, final int col) {
        return this.array[row * columns + col];
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
     * Gets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public int getValueAtColumn(int column) {
        return this.array[baseOffset + column];
    }

    /**
     * Returns an hashcode for the given row
     * @param row
     * @return
     */
    public int hashCode(final int row) {
        int offset = row * columns;
        int result = 23;
        for (int i = 0; i < columns; i++) {
            result = (37 * result) + this.array[offset++];
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
     * Returns an hashcode for the given row
     * @param row
     * @param ignore
     * @return
     */
    public int hashCodeIgnore(final int row, final int ignore) {
        int offset = row * columns;
        int result = 23;
        for (int i = 0; i < columns; i++) {
            result = (i == ignore) ? result : ((37 * result) + this.array[offset]);
            offset++;
        }
        return result;        
    }
    
    /**
     * First iterator
     * @param row
     */
    public void iterator(int row) {
        iteratorOffset = row * columns;
        iteratorI = 0;
    }

    /**
     * First iterator
     * @return
     */
    public boolean iterator_hasNext() {
        return iteratorI < columns;
    }

    /**
     * First iterator
     * @return
     */
    public int iterator_next() {
        int result = this.array[iteratorOffset++];
        iteratorI++;
        return result;
    }
    
    /**
     * First iterator
     * @param value
     * @return
     */
    public void iterator_write(int value) {
        this.array[iteratorOffset++] = value;
        iteratorI++;
    }

    /**
     * ORs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void or(int row, int value) {
        array[row * columns] |= value;
    }

    /**
     * Sets a value
     * @param row
     * @param column
     * @param value
     */
    public void set(int row, int column, int value) {
        this.array[row * columns + column] = value;
    }

    /**
     * Sets the row index for data access
     * @param row
     */
    public void setRow(int row) {
        this.baseOffset = row * columns;
    }

    /**
     * Sets the data for one row
     * @param row
     * @param data
     */
    public void setRow(int row, int[] data) {
        int offset = row * columns;
        System.arraycopy(data, 0, this.array, offset, data.length);
    }

    /**
     * Sets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public void setValueAtColumn(int column, int value) {
        this.array[baseOffset + column] = value;
    }

    /**
     * Swaps the data in both rows
     * @param row1
     * @param row2
     */
    public void swap(int row1, int row2) {
        int offset1 = row1 * columns;
        int offset2 = row2 * columns;
        for (int i = 0; i < this.columns; i++) {
            int temp = this.array[offset1];
            this.array[offset1] = this.array[offset2];
            this.array[offset2] = temp;
            offset1 ++;
            offset2 ++;
        }
    }

    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param flag
     * @return
     */
    private boolean equals(int row1, int row2, int flag) {

        int offset1 = row1 * columns;
        int offset2 = row2 * columns;

        switch (columns) {
        case 20:
            if (this.array[offset1 + 19] != this.array[offset2 + 19]) {
                return false;
            }
        case 19:
            if (this.array[offset1 + 18] != this.array[offset2 + 18]) {
                return false;
            }
        case 18:
            if (this.array[offset1 + 17] != this.array[offset2 + 17]) {
                return false;
            }
        case 17:
            if (this.array[offset1 + 16] != this.array[offset2 + 16]) {
                return false;
            }
        case 16:
            if (this.array[offset1 + 15] != this.array[offset2 + 15]) {
                return false;
            }
        case 15:
            if (this.array[offset1 + 14] != this.array[offset2 + 14]) {
                return false;
            }
        case 14:
            if (this.array[offset1 + 13] != this.array[offset2 + 13]) {
                return false;
            }
        case 13:
            if (this.array[offset1 + 12] != this.array[offset2 + 12]) {
                return false;
            }
        case 12:
            if (this.array[offset1 + 11] != this.array[offset2 + 11]) {
                return false;
            }
        case 11:
            if (this.array[offset1 + 10] != this.array[offset2 + 10]) {
                return false;
            }
        case 10:
            if (this.array[offset1 + 9] != this.array[offset2 + 9]) {
                return false;
            }
        case 9:
            if (this.array[offset1 + 8] != this.array[offset2 + 8]) {
                return false;
            }
        case 8:
            if (this.array[offset1 + 7] != this.array[offset2 + 7]) {
                return false;
            }
        case 7:
            if (this.array[offset1 + 6] != this.array[offset2 + 6]) {
                return false;
            }
        case 6:
            if (this.array[offset1 + 5] != this.array[offset2 + 5]) {
                return false;
            }
        case 5:
            if (this.array[offset1 + 4] != this.array[offset2 + 4]) {
                return false;
            }
        case 4:
            if (this.array[offset1 + 3] != this.array[offset2 + 3]) {
                return false;
            }
        case 3:
            if (this.array[offset1 + 2] != this.array[offset2 + 2]) {
                return false;
            }
        case 2:
            if (this.array[offset1 + 1] != this.array[offset2 + 1]) {
                return false;
            }
        case 1:
            if ((this.array[offset1 + 0] & flag) != (this.array[offset2 + 0] & flag)) {
                return false;
            }
            break;
        default:
            if ((this.array[offset1] & flag) != (this.array[offset2] & flag)) {
                return false;
            }
            for (int i = 1; i < columns; i++) {
                if (this.array[offset1 + i] != this.array[offset2 + i]) {
                    return false;
                }
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
        int targetOffset = 0;
        for (int source : subset) {
            int sourceOffset = source * columns;
            System.arraycopy(this.array, sourceOffset, result.array, targetOffset, columns);
            targetOffset += columns;
        }
        
        // Return
        return result;
    }
}