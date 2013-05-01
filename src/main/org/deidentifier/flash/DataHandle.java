/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.deidentifier.flash.io.CSVDataOutput;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * This class provides access to dictionary encoded data. Furthermore, the data
 * is paired to the associated input or output data. This means that, e.g., if
 * the input data is sorted, the output data will be sorted accordingly. This
 * ensures that original tuples and their generalized counterpart will always
 * have the same row index, which is important for many use cases, e.g., for
 * graphical tools that allow to compare the original dataset to generalized
 * versions.
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class DataHandle {

    /** The header */
    protected String[]       header     = null;

    /** The other handle */
    protected DataHandle     other      = null;

    /** The data defintion */
    protected DataDefinition definition = null;

    /** The data types */
    protected DataType[][]   dataTypes  = null;

    /**
     * Associates this handle to another handle
     * 
     * @param other
     */
    protected void associate(final DataHandle other) {
        this.other = other;
    }

    /**
     * Checks a column index
     * 
     * @param column1
     * @param length
     */
    protected void checkColumn(final int column1) {
        if ((column1 < 0) || (column1 > (header.length - 1))) { throw new IndexOutOfBoundsException("Column index out of range!"); }
    }

    /**
     * Checks the column indexes
     * 
     * @param columns
     * @return
     */
    protected void checkColumns(final int[] columns) {

        // Check
        if ((columns.length == 0) || (columns.length > header.length)) { throw new IllegalArgumentException("Invalid column indices provided!"); }

        // Create a sorted copy of the input columns
        final int[] cols = new int[columns.length];
        System.arraycopy(columns, 0, cols, 0, cols.length);
        Arrays.sort(cols);

        // Check
        for (int i = 0; i < cols.length; i++) {
            checkColumn(cols[i]);
            if ((i > 0) && (cols[i] == cols[i - 1])) { throw new IllegalArgumentException("Duplicate column index provided!"); }
        }
    }

    /**
     * Checks a row index
     * 
     * @param row1
     * @param length
     */
    protected void checkRow(final int row1, final int length) {
        if ((row1 < 0) || (row1 > length)) { throw new IndexOutOfBoundsException("Row index (" +
                                                                                 row1 +
                                                                                 ") out of range (0<=row<=" +
                                                                                 (length) +
                                                                                 ")!"); }
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
    protected int compare(final int row1,
                          final int row2,
                          final int[] columns,
                          final boolean ascending) {

        for (final int index : columns) {

            int cmp = 0;
            try {
                cmp = dataTypes[0][index].compare(getValueInternal(row1, index),
                                                  getValueInternal(row2, index));
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            if (cmp != 0) {
                if (ascending) {
                    return -cmp;
                } else {
                    return cmp;
                }
            }
        }
        return 0;
    }

    /**
     * generate datatypeArray for compare
     * 
     * @return
     */
    protected abstract void createDataTypeArray();

    /**
     * Returns the name of the specified column
     * 
     * @param col
     *            The column index
     * @return
     */
    public abstract String getAttributeName(int col);

    /**
     * Returns the index of the given attribute, -1 if it is not in the header
     * 
     * @param attribute
     * @return
     */
    public int getColumnIndexOf(final String attribute) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equals(attribute)) { return i; }
        }
        return -1;
    }

    /**
     * Returns the according datatype
     * 
     * @param attribute
     * @return
     */
    public DataType getDataType(final String attribute) {
        return definition.getDataType(attribute);
    }

    protected Map<String, DataType> getDataTypes() {
        return definition.getDataTypes();
    }

    public DataDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns an array containing the distinct values in the given column
     * 
     * @param column
     *            The column to process
     * @return
     */
    public abstract String[] getDistinctValues(int column);

    /**
     * Returns the generalization level for the attribute
     * 
     * @param attribute
     * @return
     */
    public abstract int getGeneralization(String attribute);

    /** Returns the number of columns in the dataset */
    public abstract int getNumColumns();

    /** Returns the number of rows in the dataset */
    public abstract int getNumRows();

    /**
     * Returns the value in the specified cell
     * 
     * @param row
     *            The cell's row index
     * @param col
     *            The cell's column index
     * @return
     */
    public abstract String getValue(int row, int col);

    /**
     * Internal representation of get value
     * 
     * @param row
     * @param col
     * @return
     */
    protected abstract String getValueInternal(int row, int col);

    /**
     * Determines whether a given row is an outlier in the currently associated
     * data transformation
     * 
     * @param row
     */
    public boolean isOutlier(final int row) {
        boolean result = isOutlierInternal(row);
        if (other != null) {
            result |= other.isOutlierInternal(row);
        }
        return result;
    }

    /**
     * Internal method for determining whether a row is an outlier
     * 
     * @param row
     * @return
     */
    protected abstract boolean isOutlierInternal(int row);

    /**
     * Returns an iterator over the data
     * 
     * @return
     */
    public abstract Iterator<String[]> iterator();

    /**
     * Writes the data to a CSV file
     * 
     * @param file
     *            A file
     * @param separator
     *            The utilized separator character
     * @throws IOException
     */
    public void save(final File file, final char separator) throws IOException {
        final CSVDataOutput output = new CSVDataOutput(file, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file
     * 
     * @param out
     *            Output stream
     * @param separator
     *            The utilized separator character
     * @throws IOException
     */
    public void
            save(final OutputStream out, final char separator) throws IOException {
        final CSVDataOutput output = new CSVDataOutput(out, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file
     * 
     * @param path
     *            A path
     * @param separator
     *            The utilized separator character
     * @throws IOException
     */
    public void
            save(final String path, final char separator) throws IOException {
        final CSVDataOutput output = new CSVDataOutput(path, separator);
        output.write(iterator());
    }

    /**
     * Sorts the dataset according to the given columns. Will sort input and
     * output analogously.
     * 
     * @param columns
     *            An integer array containing column indicides
     * @param ascending
     *            Sort ascending or descending
     */
    public void sort(final boolean ascending, final int... columns) {
        sort(0, getNumRows(), ascending, columns);
    }

    /**
     * Sorts the dataset according to the given columns and the given range.
     * Will sort input and output analogously.
     * 
     * @param from
     *            The lower bound
     * @param to
     *            The upper bound
     * @param columns
     *            An integer array containing column indicides
     * @param ascending
     *            Sort ascending or descending
     */
    public void sort(final int from,
                     final int to,
                     final boolean ascending,
                     final int... columns) {
        checkColumns(columns);
        checkRow(from, getNumRows());
        checkRow(to, getNumRows());

        final DataHandle outer = this;
        final IntComparator c = new IntComparator() {
            @Override
            public int compare(final int arg0, final int arg1) {
                return outer.compare(arg0, arg1, columns, ascending);
            }
        };
        final Swapper s = new Swapper() {
            @Override
            public void swap(final int arg0, final int arg1) {
                outer.swapBoth(arg0, arg1);
            }
        };
        GenericSorting.mergeSort(from, to, c, s);
    }

    /**
     * Swaps the data in the provided rows
     * 
     * @param row1
     *            The first row to swap
     * @param row2
     *            The second row to swap
     */
    public abstract void swap(int row1, int row2);

    /**
     * Swaps both representations
     * 
     * @param row1
     * @param row2
     */
    private void swapBoth(final int row1, final int row2) {
        swapInternal(row1, row2);
        if (other != null) {
            other.swapInternal(row1, row2);
        }
    }

    /**
     * Internal representation of the swap method
     * 
     * @param row1
     * @param row2
     */
    protected abstract void swapInternal(int row1, int row2);

}
