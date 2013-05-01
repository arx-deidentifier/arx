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

package org.deidentifier.flash.framework.data;

/**
 * Encodes a data object consisting of a dictionary encoded two-dimensional
 * array, an associated dictionary, a header and a mapping to the columns in the
 * input data set
 * 
 * @author Prasser, Kohlmayer
 */
public class Data {

    /** The outliers mask */
    public static final int  OUTLIER_MASK        = 1 << 31;

    /** The inverse outliers mask */
    public static final int  REMOVE_OUTLIER_MASK = ~OUTLIER_MASK;

    /** Row, Dimension. */
    private final int[][]    data;

    /** The header */
    private final String[]   header;

    /** The associated dictionary */
    private final Dictionary dictionary;

    /** The associated map */
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
     * Returns the data array
     * 
     * @return
     */
    public int[][] getArray() {
        return data;
    }

    /**
     * Returns the data
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
     * Returns the dictionary
     * 
     * @return
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Returns the header
     * 
     * @return
     */
    public String[] getHeader() {
        return header;
    }

    /**
     * Returns the map
     * 
     * @return
     */
    public int[] getMap() {
        return map;
    }
}
