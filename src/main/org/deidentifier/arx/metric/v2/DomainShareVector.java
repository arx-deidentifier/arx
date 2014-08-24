/*
 * ARX: Powerful Data Anonymization
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

package org.deidentifier.arx.metric.v2;

import java.io.Serializable;

/**
 * A bit vector
 * 
 * @author Fabian Prasser
 */
class DomainShareVector implements Serializable {

    /** SVUID*/
    private static final long serialVersionUID      = -3960376301235981125L;
    /** Static*/
    private static final int  ADDRESS_BITS_PER_UNIT = 6;
    /** Static*/
    private static final int  BIT_INDEX_MASK        = 63;
    
    /** Vector*/
    private final long[]      array;
    /** Vector*/
    private final int         length;
    /** Vector*/
    private int               size;

    /**
     * Creates a new instance
     * @param length
     */
    DomainShareVector(int length) {
        this.length = length;
        int chunks = (int) (Math.ceil((double) this.length / 64d));
        this.array = new long[chunks];
    }

    /**
     * Adds an index
     * @param idx
     */
    public void add(int idx) {
        int offset = idx >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        array[offset] |= 1L << (idx & BIT_INDEX_MASK);
        size += array[offset] != temp ? 1 : 0; 
    }

    /**
     * Returns whether the index is contained
     * @param idx
     * @return
     */
    public boolean contains(int idx) {
        return ((array[idx >> ADDRESS_BITS_PER_UNIT] & (1L << (idx & BIT_INDEX_MASK))) != 0);
    }

    /**
     * Returns the length of the vector
     */
    public int length() {
        return this.length;
    }
    
    /**
     * Removes an index
     * @param idx
     */
    public void remove(int idx){
        int offset = idx >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        array[offset] &= ~(1L << (idx & BIT_INDEX_MASK));
        size -= array[offset] != temp ? 1 : 0; 
    }

    /**
     * Returns the number of elements in the vector
     * @return
     */
    public int size() {
        return this.size;
    }
}
