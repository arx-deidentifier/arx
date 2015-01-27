/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.DataHandleStatistics.InterruptHandler;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.io.CSVDataOutput;

import cern.colt.Swapper;

/**
 * This class provides access to dictionary encoded data. Furthermore, the data
 * is linked to the associated input or output data. This means that, e.g., if
 * the input data is sorted, the output data will be sorted accordingly. This
 * ensures that original tuples and their generalized counterpart will always
 * have the same row index, which is important for many use cases, e.g., for
 * graphical tools that allow to compare the original dataset to generalized
 * versions.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class DataHandle {

    /** The data types. */
    protected DataType<?>[][]   dataTypes  = null;

    /** The data definition. */
    protected DataDefinition    definition = null;

    /** The header. */
    protected String[]          header     = null;

    /** The node. */
    protected ARXNode           node       = null;

    /** The current registry. */
    protected DataRegistry      registry   = null;

    /** The statistics. */
    protected StatisticsBuilder statistics = null;

    /** The current research subset. */
    protected DataHandle        subset     = null;

    /**
     * Returns the name of the specified column.
     *
     * @param col The column index
     * @return
     */
    public abstract String getAttributeName(int col);
    
    /**
     * Returns the index of the given attribute, -1 if it is not in the header.
     *
     * @param attribute
     * @return
     */
    public int getColumnIndexOf(final String attribute) {
        checkRegistry();
        for (int i = 0; i < header.length; i++) {
            if (header[i].equals(attribute)) { return i; }
        }
        return -1;
    }
    
    /**
     * Returns the according datatype.
     *
     * @param attribute
     * @return
     */
    public DataType<?> getDataType(final String attribute) {
        checkRegistry();
        return definition.getDataType(attribute);
    }
    
    /**
     * Returns a date/time value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return
     * @throws ParseException
     */
    public Date getDate(int row, int col) throws ParseException{
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXDate) {
            return ((ARXDate)type).parse(value);
        } else {
            throw new ParseException("Invalid datatype: "+type.getClass().getSimpleName(), col);
        }
    }
    
    /**
     * Returns the data definition.
     *
     * @return
     */
    public DataDefinition getDefinition() {
        checkRegistry();
        return definition;
    }

    /**
     * Returns an array containing the distinct values in the given column.
     *
     * @param column The column to process
     * @return
     */
    public final String[] getDistinctValues(int column) {
        return getDistinctValues(column, new InterruptHandler(){
            public void checkInterrupt() {
                // Nothing to do
            }
        });
    }

    /**
     * Returns a double value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return
     * @throws ParseException
     */
    public double getDouble(int row, int col) throws ParseException{
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXDecimal) {
            return ((ARXDecimal)type).parse(value);
        } else if (type instanceof ARXInteger) {
            return ((ARXInteger)type).parse(value);
        } else {
            throw new ParseException("Invalid datatype: "+type.getClass().getSimpleName(), col);
        }
    }
    
    /**
     * Returns a float value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return
     * @throws ParseException
     */
    public float getFloat(int row, int col) throws ParseException{
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXDecimal) {
            return ((ARXDecimal)type).parse(value).floatValue();
        } else if (type instanceof ARXInteger) {
            return ((ARXInteger)type).parse(value).floatValue();
        } else {
            throw new ParseException("Invalid datatype: "+type.getClass().getSimpleName(), col);
        }
    }

    /**
     * Returns the generalization level for the attribute.
     *
     * @param attribute
     * @return
     */
    public abstract int getGeneralization(String attribute);

    /**
     * Returns an int value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return
     * @throws ParseException
     */
    public int getInt(int row, int col) throws ParseException{
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXInteger) {
            return ((ARXInteger)type).parse(value).intValue();
        } else {
            throw new ParseException("Invalid datatype: "+type.getClass().getSimpleName(), col);
        }
    }
    
    /**
     * Returns a long value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return
     * @throws ParseException
     */
    public long getLong(int row, int col) throws ParseException{
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXInteger) {
            return ((ARXInteger)type).parse(value);
        } else {
            throw new ParseException("Invalid datatype: "+type.getClass().getSimpleName(), col);
        }
    }

    /**
     * Returns the number of columns in the dataset.
     *
     * @return
     */
    public abstract int getNumColumns();

    /**
     * Returns the number of rows in the dataset.
     *
     * @return
     */
    public abstract int getNumRows();

    /**
     * Returns an object providing access to basic descriptive statistics about the data represented
     * by this handle.
     *
     * @return
     */
    public StatisticsBuilder getStatistics(){
        return statistics;
    }
    
    /**
     * Returns the transformation .
     *
     * @return
     */
    public ARXNode getTransformation(){
        return node;
    }

    /**
     * Returns the value in the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return
     */
    public abstract String getValue(int row, int col);

    /**
     * Returns a new data handle that represents a context specific view on the dataset.
     *
     * @return
     */
    public DataHandle getView(){
        checkRegistry();
        if (this.subset == null){
            return this;
        } else {
            return this.subset;
        }
    }

    /**
     * Determines whether this handle is orphaned, i.e., should not be used anymore
     * @return
     */
    public boolean isOrphaned() {
        return this.registry == null;
    }

    /**
     * Determines whether a given row is an outlier in the currently associated
     * data transformation.
     *
     * @param row
     * @return
     */
    public boolean isOutlier(int row){
        checkRegistry();
        return registry.isOutlier(this, row);
    }
    
    /**
     * Returns an iterator over the data.
     *
     * @return
     */
    public abstract Iterator<String[]> iterator();

    /**
     * Releases this handle and all associated resources. If a input handle is released all associated results are released
     * as well.
     */
    public void release() {
        if (registry != null){
            registry.release(this);
        }
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param file A file
     * @param separator The utilized separator character
     * @throws IOException
     */
    public void save(final File file, final char separator) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(file, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param out Output stream
     * @param separator The utilized separator character
     * @throws IOException
     */
    public void save(final OutputStream out, final char separator) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(out, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param path A path
     * @param separator The utilized separator character
     * @throws IOException
     */
    public void save(final String path, final char separator) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(path, separator);
        output.write(iterator());
    }

    /**
     * Sorts the dataset according to the given columns. Will sort input and
     * output analogously.
     *
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, ascending, columns);
    }
    
    /**
     * Sorts the dataset according to the given columns and the given range.
     * Will sort input and output analogously.
     *
     * @param from The lower bound
     * @param to The upper bound
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(int from, int to, boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, from, to, ascending, columns);
    }

    /**
     * Sorts the dataset according to the given columns. Will sort input and
     * output analogously.
     *
     * @param swapper A swapper
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(Swapper swapper, boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, swapper, ascending, columns);
    }

    /**
     * Sorts the dataset according to the given columns and the given range.
     * Will sort input and output analogously.
     *
     * @param swapper A swapper
     * @param from The lower bound
     * @param to The upper bound
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(Swapper swapper, int from, int to, boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, swapper, from, to, ascending, columns);
    }

    /**
     * Swaps both rows.
     *
     * @param row1
     * @param row2
     */
    public void swap(int row1, int row2){
        checkRegistry();
        registry.swap(this, row1, row2);
    }
    
    /**
     * Checks a column index.
     *
     * @param column1
     */
    protected void checkColumn(final int column1) {
        if ((column1 < 0) || (column1 > (header.length - 1))) { 
            throw new IndexOutOfBoundsException("Column index out of range: "+column1+". Valid: 0 - " + (header.length - 1)); 
        }
    }

    /**
     * Checks the column indexes.
     *
     * @param columns
     */
    protected void checkColumns(final int[] columns) {

        // Check
        if ((columns.length == 0) || (columns.length > header.length)) { 
            throw new IllegalArgumentException("Invalid number of column indices"); 
        }

        // Create a sorted copy of the input columns
        final int[] cols = new int[columns.length];
        System.arraycopy(columns, 0, cols, 0, cols.length);
        Arrays.sort(cols);

        // Check
        for (int i = 0; i < cols.length; i++) {
            checkColumn(cols[i]);
            if ((i > 0) && (cols[i] == cols[i - 1])) { throw new IllegalArgumentException("Duplicate column index"); }
        }
    }

    /**
     * Checks whether a registry is referenced.
     */
    protected void checkRegistry() {
        if (registry == null) {
            throw new RuntimeException("This data handle ("+this.getClass().getSimpleName()+"@"+
                                       this.hashCode()+") is orphaned");
        }
    }
    
    /**
     * Checks a row index.
     *
     * @param row1
     * @param length
     */
    protected void checkRow(final int row1, final int length) {
        if ((row1 < 0) || (row1 > length)) { 
            throw new IndexOutOfBoundsException("Row index (" + row1 + ") out of range (0 <= row <= " + length + ")"); 
        }
    }
    
    /**
     * Releases all resources.
     */
    protected abstract void doRelease();

    /**
     * Returns the base data type without generalization.
     *
     * @param attribute
     * @return
     */
    protected DataType<?> getBaseDataType(final String attribute) {
        checkRegistry();
        return getRegistry().getBaseDataType(attribute);
    }

    /**
     * Generates an array of data types.
     *
     * @return
     */
    protected abstract DataType<?>[][] getDataTypeArray();

    /**
     * Returns the distinct values.
     *
     * @param column
     * @param handler
     * @return
     */
    protected abstract String[] getDistinctValues(int column, InterruptHandler handler);

    /**
     * Returns the registry associated with this handle.
     *
     * @return
     */
    protected DataRegistry getRegistry() {
        return this.registry;
    }

    /**
     * Returns the string inserted for suppressed data items.
     *
     * @return
     */
    protected String getSuppressionString(){
        return null;
    }

    /**
     * A negative integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second. It uses the specified
     * data types for comparison. If no datatype is specified for a specific
     * column it uses string comparison.
     * 
     * @param row1
     * @param row2
     * @param columns
     * @param ascending
     * @return
     */
    protected int internalCompare(final int row1,
                                  final int row2,
                                  final int[] columns,
                                  final boolean ascending) {

        checkRegistry();
        try {
            for (int i=0; i<columns.length; i++) {
                
                int index = columns[i];
                int cmp = dataTypes[0][index].compare(internalGetValue(row1, index),
                                                      internalGetValue(row2, index));
                if (cmp != 0) {
                    return ascending ? cmp : -cmp;
                }
            }
            return 0;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }  
    
    /**
     * Internal representation of get value.
     *
     * @param row
     * @param col
     * @return
     */
    protected abstract String internalGetValue(int row, int col);
    
    /**
     * Updates the registry.
     *
     * @param registry
     */
    protected void setRegistry(DataRegistry registry){
        this.registry = registry;
    }

    /**
     * Sets the subset.
     *
     * @param handle
     */
    protected void setView(DataHandle handle){
        this.subset = handle;
    }
}
