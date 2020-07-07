package org.deidentifier.arx.framework.data;

/**
 * Abstract matrix representation.
 * 
 * @author Ramon Gonze
 *
 */
public abstract class Matrix {
	/** The number of rows. */
	protected final int rows;
	
	/** The number of columns. */
	protected final int columns;
	
	public Matrix(final int rows, final int columns) {
		this.rows = rows;
		this.columns = columns;
	}
	
	/**
	 * Get a row from the matrix.
	 * @param row
	 * @return
	 */
	public abstract int[] getRow(int row);
	
	/**
     * ANDs the first value of the row with the given value
     * @param row
     * @param value
     */
	public abstract void and(int row, int value);
	
	@Override
	public abstract Matrix clone();
	
	/**
     * Copies a row from the given matrix into this matrix
     * @param row
     * @param sourceMatrix
     * @param sourceRow
     */
	public abstract void copyFrom(int row, DataMatrix sourceMatrix, int sourceRow);
	
	/**
     * Returns whether the given row has the given data
     * @param row
     * @param data
     * @return
     */
    public abstract boolean equals(int row, int[] data);
	
    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param ignore
     * @return
     */
    public abstract boolean equalsIgnore(int row1, int row2, int ignore);
    
    /**
     * Returns the specified value
     * @param row
     * @param col
     * @return
     */
    public abstract int get(final int row, final int col);
    
    /**
     * Gets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     */
    public abstract int getValueAtColumn(int column);
    
    /**
     * Returns an hashcode for the given row
     * @param row
     * @return
     */
    public abstract int hashCode(final int row);
    
    /**
     * Returns an hashcode for the given row
     * @param row
     * @param ignore
     * @return
     */
    public abstract int hashCodeIgnore(final int row, final int ignore);
    
    /**
     * First iterator
     * @param row
     */
    public abstract void iterator(int row);
    
    /**
     * Check if there is a next iteration
     * @return
     */
    public abstract boolean iterator_hasNext();
    
    /**
     * Get the next value in the iteration
     * @return
     */
    public abstract int iterator_next();
    
    /**
     * Write a value in the current iterator position
     * @param value
     * @return
     */
    public abstract void iterator_write(int value);
    
    /**
     * ORs the first value of the row with the given value
     * @param row
     * @param value
     */
    public abstract void or(int row, int value);
    
    /**
     * Sets a value
     * @param row
     * @param column
     * @param value
     */
    public abstract void set(int row, int column, int value);
    
    /**
     * Sets the row index for data access
     * @param row
     */
    public abstract void setRow(int row);
    
    /**
     * Sets the data for one row
     * @param row
     * @param data
     */
    public abstract void setRow(int row, int[] data);
    
    /**
     * Sets the value in the given column for the row which
     * has been set via setRow(row).
     * @param column
     * @param value
     */
    public abstract void setValueAtColumn(int column, int value);
    
    /**
     * Swaps the data in both rows
     * @param row1
     * @param row2
     */
    public abstract void swap(int row1, int row2);
    
    /**
     * Internal equals
     * @param row1
     * @param row2
     * @param flag
     * @return
     */
    protected abstract boolean equals(int row1, int row2, int flag);
    
    /**
     * Clones only a subset of the records
     * @param subset
     * @return
     */
    protected abstract Matrix clone(int[] subset);
}
