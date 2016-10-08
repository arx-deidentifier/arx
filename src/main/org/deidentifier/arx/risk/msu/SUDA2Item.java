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
    /** Hash code */
    private final int            hashCode;
    /** Support rows */
    private final IntOpenHashSet rows;

    /**
     * Creates a new item
     * @param column
     * @param value
     */
    public SUDA2Item(int column, int value) {
        this.column = column;
        this.value = value;
        this.hashCode = (31 + column) * 31 + value;
        this.id = getId(column, value);
        this.rows = new IntOpenHashSet();
    }
    
    /**
     * Clone constructor
     * @param column
     * @param value
     * @param id
     * @param hashCode
     * @param rows
     */
    SUDA2Item(int column, int value, long id, int hashCode, IntOpenHashSet rows) {
        this.column = column;
        this.value = value;
        this.id = id;
        this.hashCode = hashCode;
        this.rows = rows;
    }

    /**
     * Adds a row
     * @param row
     */
    public void addRow(int row) {
        this.rows.add(row);
    }

    @Override
    public boolean equals(Object obj) {
        SUDA2Item other = (SUDA2Item) obj;
        if (column != other.column || value != other.value) return false;
        return true;
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
     * Returns an instance of this item projected to the given rows
     * @param rows
     * @return
     */
    public SUDA2Item getProjection(IntOpenHashSet rows) {
        return new SUDA2Item(this.column, this.value, this.id, this.hashCode, rows);
    }

    /**
     * Returns the value
     * @return
     */
    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return hashCode;
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
