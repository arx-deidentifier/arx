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

package org.deidentifier.arx;

import java.io.Serializable;

/**
 * A set of rows.
 *
 * @author Fabian Prasser
 */
public class RowSet implements Serializable, Cloneable {

    /**  SVUID */
    private static final long serialVersionUID = 1492499772279795327L;
    
    /**  Bits per unit */
    private static final int   ADDRESS_BITS_PER_UNIT = 6;
    
    /**  Index mask */
    private static final int   BIT_INDEX_MASK        = 63;
    
    /**
     * Creates a new instance
     *
     * @param data
     * @return
     */
    public static RowSet create(Data data){
        return new RowSet(data);
    }

    /**
     * Creates a new instance
     *
     * @param length
     * @return
     */
    public static RowSet create(int length){
        return new RowSet(length);
    }
    
    /**  Array */
    private final long[]       array;
    
    /**  Length of array */
    private final int          length;

    /**  Number of bits set */
    private int                size;
    
    /**
     * Creates a new instance
     *
     * @param data
     */
    private RowSet(Data data) {
        this.length = data.getHandle().getNumRows();
        int chunks = (int) (Math.ceil((double) this.length / 64d));
        this.array = new long[chunks];
    }
    
    /**
     * Creates a new instance
     *
     * @param length
     */
    private RowSet(int length) {
        this.length = length;
        int chunks = (int) (Math.ceil((double) this.length / 64d));
        this.array = new long[chunks];
    }

    /**
     * Sets a bit
     *
     * @param rowIndex
     */
    public void add(int rowIndex) {
        int offset = rowIndex >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        this.array[offset] |= 1L << (rowIndex & BIT_INDEX_MASK);
        this.size += array[offset] != temp ? 1 : 0; 
    }
    
    @Override
    public RowSet clone() {
        RowSet set = new RowSet(this.length);
        set.size = this.size;
        System.arraycopy(this.array, 0, set.array, 0, this.array.length);
        return set;
    }

    /**
     * Checks whether the bit is set
     *
     * @param rowIndex
     * @return
     */
    public boolean contains(int rowIndex) {
        return ((array[rowIndex >> ADDRESS_BITS_PER_UNIT] & (1L << (rowIndex & BIT_INDEX_MASK))) != 0);
    }

    /**
     * Returns the number of available bits
     *
     * @return
     */
    public int length() {
        return this.length;
    }
    
    /**
     * Unsets a bit
     *
     * @param rowIndex
     */
    public void remove(int rowIndex){
        int offset = rowIndex >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        this.array[offset] &= ~(1L << (rowIndex & BIT_INDEX_MASK));
        this.size -= array[offset] != temp ? 1 : 0; 
    }

    /**
     * Returns the number of bits set
     *
     * @return
     */
    public int size() {
        return this.size;
    }
    
    /**
     * Swaps two bits
     *
     * @param rowIndex1
     * @param rowIndex2
     */
    public void swap(int rowIndex1, int rowIndex2) {
        
        final boolean temp1 = contains(rowIndex1);
        final boolean temp2 = contains(rowIndex2);
        
        if (temp2) {
            add(rowIndex1);
        } else {
            remove(rowIndex1);
        }
        if (temp1) {
            add(rowIndex2);
        } else {
            remove(rowIndex2);
        }
    }
}
