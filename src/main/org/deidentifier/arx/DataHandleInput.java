/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    /**
     * Creates a new data handle
     * 
     * @param data
     */
    protected DataHandleInput(final Data data) {

        // Obtain and check iterator
        final Iterator<String[]> iterator = data.iterator();
        if (!iterator.hasNext()) { 
            throw new IllegalArgumentException("Data object is empty!"); 
        }

        // Register
        this.setRegistry(new DataRegistry());
        this.getRegistry().updateInput(this);
        this.definition = data.getDefinition();

        // Obtain header
        final String[] columns = iterator.next();
        super.header = Arrays.copyOf(columns, columns.length);

        // Init dictionary
        dictionary = new Dictionary(header.length);

        // Encode data
        List<int[]> vals = new ArrayList<int[]>();
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
        this.data = vals.toArray(new int[vals.size()][]);

        // finalize dictionary
        this.dictionary.finalizeAll();

        // Create datatype array
        createDataTypeArray();
    }

    /**
     * Alternative constructor for deserialization
     * @param other
     * @param definition
     */
    protected DataHandleInput(final DataHandleInput other,
                              final DataDefinition definition) {

        // TODO: This is a clone constructor. Share or clone registry?
        this.definition = definition;
        this.setRegistry(new DataRegistry(other.getRegistry()));
        this.getRegistry().updateInput(this);

        // Obtain header
        this.header = other.header;
        this.dictionary = other.dictionary;
        this.data = other.data;

        // Create datatype array
        createDataTypeArray();

    }

    @Override
    public String getAttributeName(final int column) {
        checkRegistry();
        checkColumn(column);
        return header[column];
    }

    @Override
    public String[] getDistinctValues(final int column) {
        checkRegistry();
        checkColumn(column);
        final String[] dict = dictionary.getMapping()[column];
        final String[] vals = new String[dict.length];
        System.arraycopy(dict, 0, vals, 0, vals.length);
        return vals;
    }

    @Override
    public int getGeneralization(final String attribute) {
        checkRegistry();
        return 0;
    }

    @Override
    public int getNumColumns() {
        checkRegistry();
        return header.length;
    }

    @Override
    public int getNumRows() {
        checkRegistry();
        return data.length;
    }

    @Override
    public String getValue(final int row, final int column) {
        checkRegistry();
        checkColumn(column);
        checkRow(row, data.length);
        return internalGetValue(row, column);
    }

    @Override
    public Iterator<String[]> iterator() {
        checkRegistry();
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

    @Override
    protected void createDataTypeArray() {
        checkRegistry();
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
    protected String internalGetValue(final int row, final int column) {
        return dictionary.getMapping()[column][data[row][column]];
    }

    /**
     * Swaps the rows
     * @param row1
     * @param row2
     */
    protected void internalSwap(final int row1, final int row2) {

        // Check
        checkRow(row1, data.length);
        checkRow(row2, data.length);

        // Swap
        final int[] temp = data[row1];
        data[row1] = data[row2];
        data[row2] = temp;
    }

    @Override
    protected DataType<?> getBaseDataType(final String attribute) {
        return this.getDataType(attribute);
    }
}
