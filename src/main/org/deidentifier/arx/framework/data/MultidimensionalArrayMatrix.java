package org.deidentifier.arx.framework.data;

/**
 * A multidimensional data matrix implementation
 * 
 * @author Ramon Gonze
 *
 */
public class MultidimensionalArrayMatrix extends Matrix {
	
	/** Multidimensional array */
	private final int[][] array;
			
    /** Iterate */
    private int               iteratorI        = 0;

    /** Iterate */
    private int               iteratorRow	   = 0;
    
	/** Iterate */
    private int               rowIndex	       = 0;
	
	public MultidimensionalArrayMatrix(final int rows, final int columns) {
		super(rows, columns);
		this.array = new int[rows][columns];
	}
	
	public int[] getRow(int row) {
		int[] rowArray = new int[this.columns];
		System.arraycopy(this.array[row], 0, rowArray, 0, this.columns);
		return rowArray;
	}
	
	public void and(int row, int value) {
    	this.array[row][0] &= value;
	}
	
	public MultidimensionalArrayMatrix clone() {
		MultidimensionalArrayMatrix result = new MultidimensionalArrayMatrix(this.rows, this.columns);
		for(int i = 0; i < this.rows; i++) {
			System.arraycopy(this.array[i], 0, result.array[i], 0, this.columns);
		}
		return result;
	}
	
	public void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow) {
		System.arraycopy(sourceMatrix.getMatrix().getRow(sourceRow), 0, this.array[row], 0, this.columns);
	}
	
	public boolean equals(int row, int[] data) {
    	for (int i = 0; i < this.columns; i++) {
    		if (this.array[row][i] != data[i]) { 
    			return false; 
    		}
    	}
        return true;
    }
	
	public boolean equalsIgnore(int row1, int row2, int ignore) {
    	if (1 <= this.columns && this.columns <= 20) {        	
        	for(int i = this.columns; i >= 1; i--) {
        		if ((ignore != i-1) && this.array[row1][i-1] != this.array[row2][i-1]) {
        			return false;
        		}        		
        	}
        } else {
        	if ((ignore != 0) && (this.array[row1][0]) != (this.array[row2][0])) {
        		return false;
        	}
        	for (int i = 1; i < this.columns; i++) {
        		if ((ignore != i) && this.array[row1][i] != this.array[row2][i]) {
        			return false;
        		}
        	}        	
        }
        return true;
    }

    public int get(final int row, final int col) {
        return this.array[row][col];
    }
    
    

    public int getValueAtColumn(int column) {
        return this.array[rowIndex][column];
    }

    public int hashCode(final int row) {
        int result = 23;
        for (int i = 0; i < this.columns; i++) {
            result = (37 * result) + this.array[row][i];
        }
        return result;        
    }
    
    public int hashCodeIgnore(final int row, final int ignore) {
        int result = 23;
        for (int i = 0; i < this.columns; i++) {
            result = (i == ignore) ? result : ((37 * result) + this.array[row][i]);
        }
        return result;        
    }
    
    public void iterator(int row) {
    	this.iteratorRow = row;
        this.iteratorI = 0;
    }
    
    public boolean iterator_hasNext() {
        return this.iteratorI < this.columns;
    }
    
    
    
    

    public int iterator_next() {
        int result = this.array[this.iteratorRow][iteratorI];
        this.iteratorI++;
        return result;
    }
    
    public void iterator_write(int value) {
    	this.array[this.iteratorRow][this.iteratorI] = value;
        this.iteratorI++;
    }
    
    public void or(int row, int value) {
    	this.array[row][0] |= value;
    }
    
    public void set(int row, int column, int value) {
    	this.array[row][column] = value;
    }
    
    public void setRow(int row) {
    	this.rowIndex = row;
    }
    
    public void setRow(int row, int[] data) {
    	System.arraycopy(data, 0, this.array[row], 0, data.length);
    }
    
    public void setValueAtColumn(int column, int value) {
    	this.array[this.rowIndex][column] = value;
    }
    
    public void swap(int row1, int row2) {
		for(int i = 0; i < this.columns; i++) {
		    int temp = this.array[row1][i];
		    this.array[row1][i] = this.array[row2][i];
		    this.array[row2][i] = temp;
		}
    }
    
    protected boolean equals(int row1, int row2, int flag) {
        if(1 <= this.columns && this.columns <= 20) {
        	for(int i = this.columns; i > 1; i--) {
        		if (this.array[row1][i-1] != this.array[row2][i-1]) {
                    return false;
                }
        	}
        	if ((this.array[row1][0] & flag) != (this.array[row2][0] & flag)) {
                return false;
            }
        } else {
        	if ((this.array[row1][0] & flag) != (this.array[row2][0] & flag)) {
                return false;
            }
            for (int i = 1; i < columns; i++) {
                if (this.array[row1][i] != this.array[row2][i]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected MultidimensionalArrayMatrix clone(int[] subset) {
    	MultidimensionalArrayMatrix result = new MultidimensionalArrayMatrix(subset.length, this.columns);
    	for (int i = 0; i < subset.length; i++) {
            System.arraycopy(this.array[subset[i]], 0, result.array[i], 0, this.columns);
        }
        return result;
    }
}
