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

    /** Indicates wheter the matrix is represented as an single array or
      * a multidimensional one
      */
    private final boolean     isMultidimensional;

    /** Backing array */
    private final int[]       array;

    /** Mutidimensional array */
    private final int[][]	  matrix;

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
        
        int cells = Math.multiplyExact(rows, columns);
        if (cells <= Integer.MAX_VALUE - 2) {
        	this.array = new int[cells];
        	this.isMultidimensional = false;
        	this.matrix = null;        	
        } else {
        	// Create an multidimensional array if there are more than 2^31-1 cells
        	this.matrix = new int[rows][columns];
        	this.isMultidimensional = true;
        	this.array = null;        	
        }
    }

    /**
     * Returns true if the matrix is represented with multidimensional
     * array or false otherwise
     * @return
     */
    public boolean isMultidimensionalMatrix() {
        return this.isMultidimensional;
    }

    /**
     * ANDs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void and(int row, int value) {
    	if (this.isMultidimensional) {
    		this.matrix[row][0] &= value;
    	} else {
        	this.array[row * columns] &= value;
    	}
    }

    @Override
    public DataMatrix clone() {
    	DataMatrix result = new DataMatrix(this.rows, this.columns);
    	
    	if(this.isMultidimensional) {
	        for (int i = 0; i < this.rows; i++) {
	        	System.arraycopy(this.matrix[i], 0, result.matrix[i], 0, this.columns);
	        }
    	} else {
	        System.arraycopy(this.array, 0, result.array, 0, this.array.length);
    	}

        return result;
    }
    
    /**
     * Copies a row from the given matrix into this matrix
     * @param row
     * @param sourceMatrix
     * @param sourceRow
     */
    public void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow) {
        if (this.isMultidimensional) {
        	System.arraycopy(sourceMatrix.matrix[sourceRow], 0, this.matrix[row], 0, this.columns);
        } else {
	        int sourceOffset = sourceRow * columns;
	        int thisOffset = row * columns;
	        System.arraycopy(sourceMatrix.array, sourceOffset, this.array, thisOffset, columns);
        }
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
        if (this.isMultidimensional) {
            for (int i = 0; i < columns; i++) {
                if (this.matrix[row][i] != data[i]) {
                    return false;
                }
            }
        } else {
            int offset = row * columns;
            for (int i = 0; i < columns; i++) {
                if (this.array[offset++] != data[i]) { 
                    return false; 
                }
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
        if (columns < 1 || columns > 20){
            if (this.isMultidimensional) {
                if ((ignore != 0) && (this.matrix[row1][0]) != (this.matrix[row2][0] )) {
                    return false;
                }
                for (int i = 1; i < columns; i++) {
                    if ((ignore != i) && this.matrix[row1][i] != this.matrix[row2][i]) {
                        return false;
                    }
                }
                return true;
            } else {
                int offset1 = row1 * columns;
                int offset2 = row2 * columns;

                if ((ignore != 0) && (this.array[offset1]) != (this.array[offset2] )) {
                    return false;
                }
                for (int i = 1; i < columns; i++) {
                    if ((ignore != i) && this.array[offset1 + i] != this.array[offset2 + i]) {
                        return false;
                    }
                }
                return true;
            }
        }

        if (this.isMultidimensional) {
            for (int i = 20; i > 0; i--) {
                if (columns == i && (ignore != i-1) && this.matrix[row1][i-1] != this.matrix[row2][i-1]) {
                    return false;
                }
            }
        } else {
            int offset1 = row1 * columns;
            int offset2 = row2 * columns;

            for (int i = 20; i > 0; i--) {
                if (columns == i && (ignore != i-1) && this.array[offset1 + i-1] != this.array[offset2 + i-1]) {
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
        if (this.isMultidimensional) {
            return this.matrix[row][col];
        } else {
            return this.array[row * columns + col];
        }
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
        if (this.isMultidimensional) {
            return this.matrix[baseOffset][column];
        } else {
            return this.array[baseOffset + column];
        }
    }

    /**
     * Returns an hashcode for the given row
     * @param row
     * @return
     */
    public int hashCode(final int row) {
        int result = 23;
        
        if (this.isMultidimensional) {
            for (int i = 0; i < columns; i++) {
                result = (37 * result) + this.matrix[row][i];
            }
        } else {
            int offset = row * columns;
            for (int i = 0; i < columns; i++) {
                result = (37 * result) + this.array[offset++];
            }
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
        int result = 23;

        if (this.isMultidimensional) {
            for (int i = 0; i < columns; i++) {
                result = (i == ignore) ? result : ((37 * result) + this.matrix[row][i]);
            }
        } else {
            int offset = row * columns;
            for (int i = 0; i < columns; i++) {
                result = (i == ignore) ? result : ((37 * result) + this.array[offset]);
                offset++;
            }
        }
        return result;        
    }
    
    /**
     * First iterator
     * @param row
     */
    public void iterator(int row) {
        if (this.isMultidimensional) {
            iteratorOffset = row;
        } else {
            iteratorOffset = row * columns;
        }
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
        int result;
        if (this.isMultidimensional) {
            result = this.matrix[iteratorOffset][iteratorI];
        } else {
            result = this.array[iteratorOffset++];
        }
        iteratorI++;
        return result;
    }
    
    /**
     * First iterator
     * @param value
     * @return
     */
    public void iterator_write(int value) {
        if (this.isMultidimensional) {
            this.matrix[iteratorOffset][iteratorI] = value;
        } else {
            this.array[iteratorOffset++] = value;
        }
        iteratorI++;
    }

    /**
     * ORs the first value of the row with the given value
     * @param row
     * @param value
     */
    public void or(int row, int value) {
        if (this.isMultidimensional) {
            this.matrix[row][0] |= value;
        } else {
            this.array[row * columns] |= value;
        }
    }

    /**
     * Sets a value
     * @param row
     * @param column
     * @param value
     */
    public void set(int row, int column, int value) {
        if (this.isMultidimensional) {
            this.matrix[row][column] = value;
        } else {
            this.array[row * columns + column] = value;
        }
    }

    /**
     * Sets the row index for data access
     * @param row
     */
    public void setRow(int row) {
        if (this.isMultidimensional) {
            this.baseOffset = row;
        } else {
            this.baseOffset = row * columns;
        }
    }

    /**
     * Sets the data for one row
     * @param row
     * @param data
     */
    public void setRow(int row, int[] data) {
        if (this.isMultidimensional) {
            System.arraycopy(data, 0, this.matrix[row], 0, data.length);
        } else {
            int offset = row * columns;
            System.arraycopy(data, 0, this.array, offset, data.length);
        }
    }

    /**
     * Sets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public void setValueAtColumn(int column, int value) {
        if (this.isMultidimensional) {
            this.matrix[baseOffset][column] = value;
        } else {
            this.array[baseOffset + column] = value;
        }
    }

    /**
     * Swaps the data in both rows
     * @param row1
     * @param row2
     */
    public void swap(int row1, int row2) {
        if (this.isMultidimensional) {
            for(int i = 0; i < this.columns; i++) {
                int temp = this.matrix[row1][i];
                this.matrix[row1][i] = this.matrix[row2][i];
                this.matrix[row2][i] = temp;
            }
        } else {
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
    }

    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param flag
     * @return
     */
    private boolean equals(int row1, int row2, int flag) {

        if (columns < 0 || columns > 20) {
            if (this.isMultidimensional) {
                if ((this.matrix[row1][0] & flag) != (this.matrix[row2][0] & flag)) {
                    return false;
                }
                for (int i = 1; i < columns; i++) {
                    if (this.matrix[row1][i] != this.matrix[row2][i]) {
                        return false;
                    }
                }
                return true;
            } else {
                int offset1 = row1 * columns;
                int offset2 = row2 * columns;
                
                if ((this.array[offset1] & flag) != (this.array[offset2] & flag)) {
                    return false;
                }
                for (int i = 1; i < columns; i++) {
                    if (this.array[offset1 + i] != this.array[offset2 + i]) {
                        return false;
                    }
                }
                return true;
            }
        }

        if (this.isMultidimensional) {
            for (int i = 20; i > 0; i--) {
                if (columns == i && this.matrix[row1][i-1] != this.matrix[row2][i-1]) {
                    return false;
                }
            }
        } else {
            int offset1 = row1 * columns;
            int offset2 = row2 * columns;

            for(int i = 20; i > 0; i--) {
                if (columns == i && this.array[offset1 + i-1] != this.array[offset2 + i-1]) {
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
        if (this.isMultidimensional) {
            for (int i = 0; i < subset.length; i++) {
                System.arraycopy(this.matrix[subset[i]], 0, result.matrix[i], 0, columns);
            }
        } else {
            int targetOffset = 0;
            for (int source : subset) {
                int sourceOffset = source * columns;
                System.arraycopy(this.array, sourceOffset, result.array, targetOffset, columns);
                targetOffset += columns;
            }
        }
        
        // Return
        return result;
    }
}