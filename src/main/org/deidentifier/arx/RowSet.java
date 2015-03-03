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

package org.deidentifier.arx;

import java.io.Serializable;

/**
 * A set of rows.
 *
 * @author Fabian Prasser
 */
public class RowSet implements Serializable, Cloneable {

    /**  TODO */
    private static final long serialVersionUID = 1492499772279795327L;
    
    /**  TODO */
    private static final int   ADDRESS_BITS_PER_UNIT = 6;
    
    /**  TODO */
    private static final int   BIT_INDEX_MASK        = 63;
    
    /**
     * 
     *
     * @param data
     * @return
     */
    public static RowSet create(Data data){
        return new RowSet(data);
    }
    
    /**  TODO */
    private final long[]       array;
    
    /**  TODO */
    private final int          length;

    /**  TODO */
    private int                size;
    
    /**
     * 
     *
     * @param data
     */
    private RowSet(Data data) {
        this.length = data.getHandle().getNumRows();
        int chunks = (int) (Math.ceil((double) this.length / 64d));
        array = new long[chunks];
    }
    
    /**
     * 
     *
     * @param length
     */
    private RowSet(int length) {
        this.length = length;
        int chunks = (int) (Math.ceil((double) this.length / 64d));
        array = new long[chunks];
    }

    /**
     * 
     *
     * @param rowIndex
     */
    public void add(int rowIndex) {
        int offset = rowIndex >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        array[offset] |= 1L << (rowIndex & BIT_INDEX_MASK);
        size += array[offset] != temp ? 1 : 0; 
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public RowSet clone() {
        RowSet set = new RowSet(this.length);
        set.size = this.size;
        System.arraycopy(this.array, 0, set.array, 0, this.array.length);
        return set;
    }

    /**
     * 
     *
     * @param rowIndex
     * @return
     */
    public boolean contains(int rowIndex) {
        return ((array[rowIndex >> ADDRESS_BITS_PER_UNIT] & (1L << (rowIndex & BIT_INDEX_MASK))) != 0);
    }

    /**
     * 
     *
     * @return
     */
    public int length() {
        return this.length;
    }
    
    /**
     * 
     *
     * @param rowIndex
     */
    public void remove(int rowIndex){
        int offset = rowIndex >> ADDRESS_BITS_PER_UNIT;
        long temp = array[offset];
        array[offset] &= ~(1L << (rowIndex & BIT_INDEX_MASK));
        size -= array[offset] != temp ? 1 : 0; 
    }

    /**
     * 
     *
     * @return
     */
    public int size() {
        return this.size;
    }
    
    /**
     * 
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
