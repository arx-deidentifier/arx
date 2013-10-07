/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.deidentifier.arx.framework.data.Dictionary;

/**
 * An implementation of the DataHandle interface for input data
 * 
 * @author Prasser, Kohlmayer
 */
public class DataHandleInput extends DataHandle {

    /** The data */
    protected int[][]    data         = null;

    /** The dictionary */
    protected Dictionary dictionary   = null;

    /** The data subset */
    private RowSet       subsetBitset = null;

    /** The data subset */
    private int[]        subsetArray  = null;

    /**
     * Creates a new data handle
     * 
     * @param data
     */
    protected DataHandleInput(final Data data) {

        definition = data.getDefinition();

        // Obtain iterator
        final Iterator<String[]> iterator = data.iterator();

        if (!iterator.hasNext()) { throw new IllegalArgumentException("Data object is empty!"); }

        // Obtain header
        final String[] columns = iterator.next();
        super.header = Arrays.copyOf(columns, columns.length);

        // Init dictionary
        dictionary = new Dictionary(header.length);

        // Encode data
        final LinkedList<int[]> vals = new LinkedList<int[]>();
        while (iterator.hasNext()) {

            // Process a tuple
            final String[] strings = iterator.next();
            final int[] tuple = new int[header.length];
            for (int i = 0; i < strings.length; i++) {
                tuple[i] = dictionary.register(i, strings[i]);
            }
            vals.add(tuple);
        }

        // Build array
        this.data = new int[vals.size()][];
        final Iterator<int[]> i = vals.iterator();
        int index = 0;
        while (i.hasNext()) {
            this.data[index++] = i.next();
        }

        // finalize dictionary
        dictionary.finalizeAll();

        // Create datatype array
        createDataTypeArray();
    }

    protected DataHandleInput(final DataHandleInput other,
                              final DataDefinition definition) {

        this.definition = definition;

        // Obtain header
        super.header = other.header;
        dictionary = other.dictionary;
        data = other.data;

        // Create datatype array
        createDataTypeArray();

    }

    @Override
    public String getAttributeName(final int column) {
        checkColumn(column);
        return header[column];
    }

    @Override
    public String[] getDistinctValues(final int column) {
        checkColumn(column);
        final String[] dict = dictionary.getMapping()[column];
        final String[] vals = new String[dict.length];
        System.arraycopy(dict, 0, vals, 0, vals.length);
        return vals;
    }

    @Override
    public int getGeneralization(final String attribute) {
        return 0;
    }

    @Override
    public int getNumColumns() {
        return header.length;
    }

    @Override
    public int getNumRows() {
        return data.length;
    }

    /**
     * Returns the associated research subset
     */
    public RowSet getSubset() {
        return this.subsetBitset;
    }

    @Override
    public String getValue(final int row, final int column) {
        checkColumn(column);
        checkRow(row, data.length);
        return getValueInternal(row, column);
    }

    @Override
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {

            int index = -1;

            @Override
            public boolean hasNext() {
                return (index < data.length);
            }

            @Override
            public String[] next() {
                if (index == -1) {
                    index++;
                    return header;
                } else {
                    final String[] result = new String[header.length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = getValue(index, i);
                    }
                    index++;
                    return result;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is unsupported!");
            }
        };
    }

    /**
     * Sets the research subset
     * @param bitset
     * @param array
     */
    public void setSubset(RowSet bitset, int[] array) {
        this.subsetBitset = bitset;
        this.subsetArray = array;
    }

    @Override
    public void swap(final int row1, final int row2) {

        // Check
        checkRow(row1, data.length);
        checkRow(row2, data.length);

        // Swap output data
        if (other != null) {
            other.swap(row1, row2);
        }

        // Swap
        swapInternal(row1, row2);
        
        // Swap subset
        if (this.subsetBitset != null){
            
            this.subsetBitset.swap(row1, row2);

            // TODO: Super inefficient
            int index = 0;
            for (int i = 0; i < data.length; i++){
                if (this.subsetBitset.contains(i)){
                    this.subsetArray[index++] = i;
                }
            }
        }
    }

    @Override
    protected void afterSorting() {

        // Rebuild array representation of subset
        if (this.subsetBitset != null){
        	
            int index = 0;
            for (int i = 0; i < data.length; i++){
                if (this.subsetBitset.contains(i)){
                    this.subsetArray[index++] = i;
                }
            }
        }
    }

    @Override
    protected void createDataTypeArray() {
        dataTypes = new DataType[1][header.length];
        for (int i = 0; i < header.length; i++) {
            final DataType<?> type = definition.getDataType(header[i]);
            if (type != null) {
                dataTypes[0][i] = type;
            } else {
                dataTypes[0][i] = DataType.STRING;
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.ARXDataHandle#getValueInternal(int, int)
     */
    @Override
    protected String getValueInternal(final int row, final int column) {
        return dictionary.getMapping()[column][data[row][column]];
    }
    
    @Override
    protected boolean isOutlierInternal(final int row) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.ARXDataHandle#swapInternal(int, int)
     */
    @Override
    protected void swapInternal(final int row1, final int row2) {
        final int[] temp = data[row1];
        data[row1] = data[row2];
        data[row2] = temp;
        
        if (this.subsetBitset != null){
            this.subsetBitset.swap(row1, row2);
        }
    }
}
