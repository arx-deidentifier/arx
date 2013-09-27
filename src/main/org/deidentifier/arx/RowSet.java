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


/**
 * A set of rows
 * 
 * @author Prasser, Kohlmayer
 */
public class RowSet {

    private final long[]       array;
    private final int          length;
    private static final int   ADDRESS_BITS_PER_UNIT = 6;
    protected static final int BITS_PER_UNIT         = 64;
    private static final int   BIT_INDEX_MASK        = 63;
    private int size;

    public static RowSet create(Data data){
        return new RowSet(data);
    }
    
    private RowSet(Data data) {
        this.length = data.getHandle().getNumRows();
        int chunks = (int) (Math.ceil((double) this.length / 64d));
        array = new long[chunks];
    }

    public void add(int bit) {
        int offset = bit >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        array[offset] |= 1L << (bit & BIT_INDEX_MASK);
        size += array[offset] != temp ? 1 : 0; 
    }

    public boolean contains(int bit) {
        return ((array[bit >> ADDRESS_BITS_PER_UNIT] & (1L << (bit & BIT_INDEX_MASK))) != 0);
    }

    public int length() {
        return this.length;
    }
    
    public int size() {
        return this.size;
    }
}
