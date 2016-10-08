/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk.msu;

import com.carrotsearch.hppc.IntOpenHashSet;

/**
 * Each itemset is a concrete value for a concrete attribute
 * 
 * @author Fabian Prasser
 */
public class SUDA2Item {

    /**
     * Packs column and value into a long to be used as a key
     * @param column
     * @param value
     * @return
     */
    public static long getId(int column, int value) {
        return ((long)column) << 32 | ((long)value) & 0xFFFFFFFFL;
    }

    /** Column */
    private final int            column;
    /** Unique id */
    private final long           id;
    /** Value */
    private final int            value;
    /** Support rows */
    private final IntOpenHashSet rows;

    /**
     * Creates a new item
     * @param column
     * @param value
     * @param id
     */
    public SUDA2Item(int column, int value, long id) {
        this.column = column;
        this.value = value;
        this.id = id;
        this.rows = new IntOpenHashSet();
    }
    
    /**
     * Clone constructor
     * @param column
     * @param value
     * @param id
     * @param rows
     */
    SUDA2Item(int column, int value, long id, IntOpenHashSet rows) {
        this.column = column;
        this.value = value;
        this.id = id;
        this.rows = rows;
    }

    /**
     * Adds a row
     * @param row
     */
    public void addRow(int row) {
        this.rows.add(row);
    }

    /**
     * Returns this item if it becomes a 1-MSU in the given set of rows,
     * null otherwise
     * @param otherRows
     * @return
     */
    public SUDA2Item get1MSU(IntOpenHashSet otherRows) {

        // Smaller set is rows1
        int size1 = this.rows.size();
        int size2 = otherRows.size();
        IntOpenHashSet rows1 = size1 < size2 ? this.rows : otherRows;
        IntOpenHashSet rows2 = size1 < size2 ? otherRows : this.rows;
        
        // Check if they intersect with exactly one support row
        boolean supportRowFound = false;
        final int [] keys = rows1.keys;
        final boolean [] allocated = rows1.allocated;
        for (int i = 0; i < allocated.length; i++) {
            if (allocated[i]) {
                int row = keys[i];
                if (rows2.contains(row)) {
                    // More than one support row
                    if (supportRowFound) {
                        return null;
                    } else {
                        supportRowFound = true;
                    }
                }
            }
        }
        
        // Check whether the item is a 1-MSU
        return supportRowFound ? this : null;
    }

    /**
     * Returns the column
     * @return
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the id
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Returns an instance of this item projected to the given rows
     * @param otherRows
     * @return
     */
    public SUDA2Item getProjection(IntOpenHashSet otherRows) {

        // Smaller set is set 1
        int size1 = this.rows.size();
        int size2 = otherRows.size();
        IntOpenHashSet rows1 = size1 < size2 ? this.rows : otherRows;
        IntOpenHashSet rows2 = size1 < size2 ? otherRows : this.rows;
        
        // Intersect support rows with those provided
        IntOpenHashSet rows = new IntOpenHashSet();
        final int [] keys = rows1.keys;
        final boolean [] allocated = rows1.allocated;
        for (int i = 0; i < allocated.length; i++) {
            if (allocated[i]) {
                int row = keys[i];
                if (rows2.contains(row)) {
                    rows.add(row);
                }
            }
        }

        // Return
        return rows.isEmpty() ? null : new SUDA2Item(this.column, this.value, this.id, rows);
    }
    
    /**
     * Returns the rows in which this item is located
     * @return
     */
    public IntOpenHashSet getRows() {
        return this.rows;
    }

    /**
     * Returns the support
     * @return
     */
    public int getSupport() {
        return this.rows.size();
    }

    /**
     * Returns the value
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns whether the item is contained in a given row
     * @param row
     * @return
     */
    public boolean isContained(int[] row) {
        return row[column] == value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(column).append(",").append(value).append(")");
        return builder.toString();
    }
}
