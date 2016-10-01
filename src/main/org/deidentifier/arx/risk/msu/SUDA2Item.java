package org.deidentifier.arx.risk.msu;

import java.util.HashSet;
import java.util.Set;

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
        return ((long)column) << 32 | value & 0xFFFFFFFFL;
    }

    /** Column */
    private final int    column;
    /** Unique id */
    private final long   id;
    /** Value */
    private final int    value;
    /** Hash code */
    private final int    hashCode;
    /** Support rows */
    private Set<Integer> rows = new HashSet<>();

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
    }

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
     * Returns the rows in which this item is located
     * @return
     */
    public Set<Integer> getRows() {
        return this.rows;
    }

    /**
     * Returns the support
     * @return
     */
    public int getSupport() {
        return this.rows.size();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(column).append(",").append(value).append(")");
        return builder.toString();
    }

    /**
     * Returns the id
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Returns whether the item is contained in a given row
     * @param row
     * @return
     */
    public boolean isContained(int[] row) {
        return row[column] == value;
    }
}
