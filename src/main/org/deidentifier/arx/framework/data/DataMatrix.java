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
    private Matrix			  matrix;
    
    /** The number of rows. */
    private final int         rows;

    /** The number of columns. */
    private final int         columns;

    /**
     * Instantiates a new memory block.
     *
     * @param rows the num rows
     * @param columns the num columns
     */
    public DataMatrix(final int rows, final int columns) {
        this.columns = columns;
        this.rows = rows;

        try{
        	/** Creates a single array if there are 2^31-1 cells or less than that */
            Math.multiplyExact(rows, columns);
            this.matrix = new SingleArrayMatrix(rows, columns);
        } catch (ArithmeticException e) {
            /** Creates a multidimensional array if there are more than 2^31-1 cells */
            this.matrix = new MultidimensionalArrayMatrix(rows, columns);
        }
    }
    
    /**
     * Get the matrix object
     * @return
     */
    public Matrix getMatrix() {
    	return this.matrix;
    }
    
    /**
     * Set the matrix object
     * @return
     */
    public void setMatrix(Matrix matrix) {
    	this.matrix = matrix;
    }
    
    /**
     * ANDs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void and(int row, int value) {
    	this.matrix.and(row, value);
    }
    
    @Override
    public DataMatrix clone() {
    	DataMatrix result = new DataMatrix(this.rows, this.columns);
    	result.matrix = this.matrix.clone();
        return result;
    }
    
    /**
     * Copies a row from the given matrix into this matrix
     * @param row
     * @param sourceMatrix
     * @param sourceRow
     */
    public void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow) {
        this.matrix.copyFrom(row, sourceMatrix, sourceRow);
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
        return this.matrix.equals(row, data);
    }

    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param ignore
     * @return
     */
    public boolean equalsIgnore(int row1, int row2, int ignore) {
        return this.matrix.equalsIgnore(row1, row2, ignore);
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
        return this.matrix.get(row, col);
    }
    
    /**
     * Returns the number of columns
     * @return
     */
    public int getNumColumns() {
        return this.columns;
    }

    /**
     * Returns the number of rows
     * @return
     */
    public int getNumRows() {
        return this.rows;
    }

    /**
     * Gets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     */
    public int getValueAtColumn(int column) {
        return this.matrix.getValueAtColumn(column);
    }

    /**
     * Returns an hashcode for the given row
     * @param row
     * @return
     */
    public int hashCode(final int row) {
        return this.matrix.hashCode(row);       
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
        return this.matrix.hashCodeIgnore(row, ignore);     
    }
    
    /**
     * First iterator
     * @param row
     */
    public void iterator(int row) {
        this.matrix.iterator(row);
    }

    /**
     * First iterator
     * @return
     */
    public boolean iterator_hasNext() {
        return this.matrix.iterator_hasNext();
    }

    /**
     * Get the next value in the iteration
     * @return
     */
    public int iterator_next() {
    	return this.matrix.iterator_next();
    }
    
    /**
     * Write a value in the current iterator position
     * @param value
     * @return
     */
    public void iterator_write(int value) {
        this.matrix.iterator_write(value);
    }

    /**
     * ORs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void or(int row, int value) {
        this.matrix.or(row, value);
    }

    /**
     * Sets a value
     * @param row
     * @param column
     * @param value
     */
    public void set(int row, int column, int value) {
        this.matrix.set(row, column, value);
    }

    /**
     * Sets the row index for data access
     * @param row
     */
    public void setRow(int row) {
        this.matrix.setRow(row);
    }

    /**
     * Sets the data for one row
     * @param row
     * @param data
     */
    public void setRow(int row, int[] data) {
        this.matrix.setRow(row, data);
    }

    /**
     * Sets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public void setValueAtColumn(int column, int value) {
        this.matrix.setValueAtColumn(column, value);
    }

    /**
     * Swaps the data in both rows
     * @param row1
     * @param row2
     */
    public void swap(int row1, int row2) {
        this.matrix.swap(row1, row2);
    }

    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param flag
     * @return
     */
    private boolean equals(int row1, int row2, int flag) {
    	return this.matrix.equals(row1, row2, flag);
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
        result.setMatrix(this.matrix.clone(subset)); 
        
        // Return
        return result;
    }
}