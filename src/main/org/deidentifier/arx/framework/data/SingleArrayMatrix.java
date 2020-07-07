package org.deidentifier.arx.framework.data;

/**
 * A fast implementation of an array of arrays of equal size
 * 
 * @author Fabian Prasser
 */
public class SingleArrayMatrix extends Matrix {
	
	/** Backing array */
    private final int[]       array;
    
    /** Iterate */
    private int               iteratorI        = 0;

    /** Iterate */
    private int               iteratorOffset   = 0;
    
	/** Iterate */
    private int               baseOffset       = 0;
	
	public SingleArrayMatrix(final int rows, final int columns) {
		super(rows, columns);
		int cells = Math.multiplyExact(rows, columns);
        this.array = new int[cells];
	}

	public int[] getRow(int row) {
		int[] rowArray = new int[this.columns];
		int offset = row * this.columns;
		System.arraycopy(this.array, offset, rowArray, 0, this.columns);
		return rowArray;
	}
	
	public void and(int row, int value) {
        this.array[row * this.columns] &= value;
	}

	public SingleArrayMatrix clone() {
    	SingleArrayMatrix result = new SingleArrayMatrix(this.rows, this.columns);
        System.arraycopy(this.array, 0, result.array, 0, this.array.length);
        return result;
    }

	public void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow) {
		int offset = row * this.columns;
		System.arraycopy(sourceMatrix.getMatrix().getRow(sourceRow), 0, this.array, offset, this.columns);
	}

    public boolean equals(int row, int[] data) {
    	int offset = row * this.columns;
    	for (int i = 0; i < this.columns; i++) {
    		if (this.array[offset++] != data[i]) { 
    			return false; 
    		}
    	}
        return true;
    }

    public boolean equalsIgnore(int row1, int row2, int ignore) {
    	int offset1 = row1 * this.columns;
    	int offset2 = row2 * this.columns;
        
    	if (1 <= this.columns && this.columns <= 20) {        	
        	for(int i = this.columns; i >= 1; i--) {
        		if ((ignore != i-1) && this.array[offset1 + i-1] != this.array[offset2 + i-1]) {
        			return false;
        		}        		
        	}
        } else {
        	if ((ignore != 0) && (this.array[offset1]) != (this.array[offset2] )) {
        		return false;
        	}
        	for (int i = 1; i < this.columns; i++) {
        		if ((ignore != i) && this.array[offset1 + i] != this.array[offset2 + i]) {
        			return false;
        		}
        	}        	
        }
        return true;
    }

    public int get(final int row, final int col) {
    	return this.array[row * this.columns + col];
    }

    public int getValueAtColumn(int column) {
        return this.array[this.baseOffset + column];
    }
    
    public int hashCode(final int row) {
    	int offset = row * this.columns;
        int result = 23;
        for (int i = 0; i < this.columns; i++) {
            result = (37 * result) + this.array[offset++];
        }
        return result;        
    }
    
    public int hashCodeIgnore(final int row, final int ignore) {
    	int offset = row * this.columns;
        int result = 23;
        for (int i = 0; i < this.columns; i++) {
            result = (i == ignore) ? result : ((37 * result) + this.array[offset]);
            offset++;
        }
        return result;        
    }
    
    public void iterator(int row) {
        this.iteratorOffset = row * this.columns;
        this.iteratorI = 0;
    }
    
    public boolean iterator_hasNext() {
        return this.iteratorI < this.columns;
    }

    public int iterator_next() {
        int result = this.array[this.iteratorOffset++];
        this.iteratorI++;
        return result;
    }

    public void iterator_write(int value) {
    	this.array[this.iteratorOffset++] = value;
    	this.iteratorI++;
    }
    
    public void or(int row, int value) {
    	this.array[row * this.columns] |= value;
    }
    
    public void set(int row, int column, int value) {
    	this.array[row * this.columns + column] = value;
    }
    
    public void setRow(int row) {
    	this.baseOffset = row * this.columns;
    }
    
    public void setRow(int row, int[] data) {
    	int offset = row * this.columns;
    	System.arraycopy(data, 0, this.array, offset, data.length);
    }
    
    public void setValueAtColumn(int column, int value) {
    	this.array[this.baseOffset + column] = value;
    }
    
    public void swap(int row1, int row2) {
		int offset1 = row1 * this.columns;
		int offset2 = row2 * this.columns;
		for (int i = 0; i < this.columns; i++) {
		    int temp = this.array[offset1];
		    this.array[offset1] = this.array[offset2];
		    this.array[offset2] = temp;
		    offset1 ++;
		    offset2 ++;
		}
    }

    protected boolean equals(int row1, int row2, int flag) {
    	int offset1 = row1 * this.columns;
        int offset2 = row2 * this.columns;
        
        if(1 <= this.columns && this.columns <= 20) {
        	for(int i = this.columns; i > 1; i--) {
        		if (this.array[offset1 + i-1] != this.array[offset2 + i-1]) {
                    return false;
                }
        		if ((this.array[offset1] & flag) != (this.array[offset2] & flag)) {
                    return false;
                }
        	}
        } else {
        	if ((this.array[offset1] & flag) != (this.array[offset2] & flag)) {
                return false;
            }
            for (int i = 1; i < this.columns; i++) {
                if (this.array[offset1 + i] != this.array[offset2 + i]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected SingleArrayMatrix clone(int[] subset) {
    	SingleArrayMatrix result = new SingleArrayMatrix(subset.length, this.columns);
    	int targetOffset = 0;
        for (int source : subset) {
            int sourceOffset = source * this.columns;
            System.arraycopy(this.array, sourceOffset, result.array, targetOffset, this.columns);
            targetOffset += this.columns;
        }
        return result;
    }
}