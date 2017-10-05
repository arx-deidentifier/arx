/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
    private static final long serialVersionUID    = 9088882549074658790L;

    /** The outliers mask. */
    public static final int   OUTLIER_MASK        = 1 << 31;

    /** The inverse outliers mask. */
    public static final int   REMOVE_OUTLIER_MASK = ~OUTLIER_MASK;

    /** Row, Dimension. */
    private final DataMatrix  data;

    /** The header. */
    private final String[]    header;

    /** The associated dictionary. */
    private final Dictionary  dictionary;

    /** The associated map. */
    private final int[]       map;

    /**
     * Creates a new data object.
     * 
     * @param data The int array
     * @param header The header
     * @param map The map
     * @param dictionary The dictionary
     */
    public Data(final DataMatrix data,
                final String[] header,
                final int[] map,
                final Dictionary dictionary) {
        this.data = data;
        this.header = header;
        this.dictionary = dictionary;
        this.map = map;
    }

    @Override
    public Data clone(){
        return new Data(data.clone(), header, map, dictionary);
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
     * Returns the map.
     *
     * @return
     */
    public int[] getMap() {
        return map;
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
        return new Data(new DataMatrixSubset(data, rows), header, map, dictionary);
    }

    /**
     * Returns whether this object is empty
     * @return
     */
    public boolean isEmpty() {
        return header == null || header.length == 0;
    }
}
