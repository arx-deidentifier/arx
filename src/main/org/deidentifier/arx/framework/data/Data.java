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
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.RowSet;

/**
 * Encodes a data object consisting of a dictionary encoded two-dimensional
 * array, an associated dictionary, a header and a mapping to the columns in the
 * input data set.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Data implements Cloneable, Serializable {

    /** SVUID */
    private static final long          serialVersionUID    = 9088882549074658790L;

    /** The outliers mask. */
    public static final int            OUTLIER_MASK        = 1 << 31;

    /** The inverse outliers mask. */
    public static final int            REMOVE_OUTLIER_MASK = ~OUTLIER_MASK;

    /**
     * Creates an object which projects the given data onto the given set of columns
     * 
     * @param data
     * @param header
     * @param columns
     * @param dictionary
     * @return
     */
    public static Data createProjection(final DataMatrix data,
                                        final String[] header,
                                        final int[] columns,
                                        final Dictionary dictionary) {
        
        // Empty object
        if (columns.length == 0) {
            return new Data(null, new String[0], new int[0], new Dictionary(0));
        }

        // Clone matrix
        DataMatrix matrix = new DataMatrix(data.getNumRows(), columns.length);
        for (int row = 0; row < data.getNumRows(); row++) {
            
            // Prepare row
            matrix.setRow(row);
            data.setRow(row);
            
            // Copy each column
            for (int index = 0; index < columns.length; index++) {
                matrix.setValueAtColumn(index, data.getValueAtColumn(columns[index]));
            }
        }
        
        // Prepare header
        String[] newHeader = new String[columns.length];
        int index = 0;
        for (int column : columns) {
            newHeader[index++] = header[column];
        }

        // Return
        return new Data(matrix, newHeader, columns, new Dictionary(dictionary, columns));
    }

    /**
     * Creates an object which simply encapsulates the provided objects
     * @param data
     * @param header
     * @param columns
     * @param dictionary
     * @return
     */
    public static Data createWrapper(final DataMatrix data,
                                     final String[] header,
                                     final int[] columns,
                                     final Dictionary dictionary) {
        
        // Return
        return new Data(data, header, columns, dictionary);
    }

    /** Row, Dimension. */
    private final DataMatrix           data;

    /** The header. */
    private final String[]             header;

    /** The associated dictionary. */
    private final Dictionary           dictionary;

    /** The associated map. */
    private final int[]                columns;

    /** Maps attributes to their index */
    private final Map<String, Integer> map;

    /**
     * Creates a new data object.
     * 
     * @param data The int array
     * @param header The header
     * @param columns The map
     * @param dictionary The dictionary
     */
    private Data(final DataMatrix data,
                 final String[] header,
                 final int[] columns,
                 final Dictionary dictionary) {
        
        this.data = data;
        this.header = header;
        this.dictionary = dictionary;
        this.columns = columns;
        this.map = new HashMap<>();
        for (int index = 0; index < header.length; index++) {
            map.put(header[index], index);
        }
    }

    @Override
    public Data clone(){
        return new Data(data != null ? data.clone() : null, header, columns, dictionary);
    }

    /**
     * Returns the data array.
     *
     * @return
     */
    public DataMatrix getArray() {
        return data;
    }

    /**
     * Returns the set of columns from the input data set stored in this object.
     *
     * @return
     */
    public int[] getColumns() {
        return columns;
    }

    /**
     * Returns the number of rows.
     * 
     * @return the data length
     */
    public int getDataLength() {
        return data.getNumRows();
    }

    /**
     * Returns the dictionary.
     *
     * @return
     */
    public Dictionary getDictionary() {
        return dictionary;
    }
    
    /**
     * Returns the header.
     *
     * @return
     */
    public String[] getHeader() {
        return header;
    }
    
    /**
     * Returns the index of the given attribute. Returns -1 if the attribute is not contained.
     * @param attribute
     * @return
     */
    public int getIndexOf(String attribute) {
        return !map.containsKey(attribute) ? -1 : map.get(attribute);
    }

    /**
     * Returns a new instance that is projected onto the given subset
     * @param rowset
     * @return
     */
    public Data getSubsetInstance(RowSet rowset) {
        int[] rows = new int[rowset.size()];
        int index = 0;
        for (int row = 0; row < rowset.length(); row++) {
            if (rowset.contains(row)) {
                rows[index++] = row;
            }
        }
        return new Data(new DataMatrixSubset(data, rows), header, columns, dictionary);
    }

    /**
     * Returns whether this object is empty
     * @return
     */
    public boolean isEmpty() {
        return header == null || header.length == 0;
    }
}
