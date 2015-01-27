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

package org.deidentifier.arx.framework.data;

import java.util.Arrays;

/**
 * Encodes a data object consisting of a dictionary encoded two-dimensional
 * array, an associated dictionary, a header and a mapping to the columns in the
 * input data set.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Data implements Cloneable{

    /** The outliers mask. */
    public static final int  OUTLIER_MASK        = 1 << 31;

    /** The inverse outliers mask. */
    public static final int  REMOVE_OUTLIER_MASK = ~OUTLIER_MASK;

    /** Row, Dimension. */
    private final int[][]    data;

    /** The header. */
    private final String[]   header;

    /** The associated dictionary. */
    private final Dictionary dictionary;

    /** The associated map. */
    private final int[]      map;

    /**
     * Creates a new data object.
     * 
     * @param data
     *            The int array
     * @param header
     *            The header
     * @param map
     *            The map
     * @param dictionary
     *            The dictionary
     */
    public Data(final int[][] data,
                final String[] header,
                final int[] map,
                final Dictionary dictionary) {
        this.data = data;
        this.header = header;
        this.dictionary = dictionary;
        this.map = map;
    }

    /**
     * Returns the data array.
     *
     * @return
     */
    public int[][] getArray() {
        return data;
    }

    /**
     * Returns the data.
     *
     * @return
     */
    public int[][] getData() {
        return data;
    }

    /**
     * Returns the number of rows.
     * 
     * @return the data length
     */
    public int getDataLength() {
        return data.length;
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Data clone(){
        int[][] newData = new int[data.length][];
        for (int i=0; i < data.length; i++){
            newData[i] = Arrays.copyOf(data[i], header.length);
        }
        return new Data(newData, header, map, dictionary);
    }
}
