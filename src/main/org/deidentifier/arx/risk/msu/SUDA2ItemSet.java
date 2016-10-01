package org.deidentifier.arx.risk.msu;

import java.util.HashSet;
import java.util.Set;

/**
 * Each itemset is a concrete key, i.e. a set of values for a set of attributes.
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemSet {

    /** Items */
    private Set<SUDA2Item> items = new HashSet<>();
    /** Support rows */
    private Set<Integer>   rows  = new HashSet<>();

    /**
     * Creates an item set containing a single item
     * @param item
     */
    public SUDA2ItemSet(SUDA2Item item) {
        this.items.add(item);
        this.rows.addAll(item.getRows());
    }

    /**
     * Creates a merged item set
     * @param item
     * @param set
     */
    public SUDA2ItemSet(SUDA2Item item, SUDA2ItemSet set) {
        this.items.add(item);
        this.rows.addAll(item.getRows());
        for (SUDA2Item setItem : set.items) {
            this.items.add(setItem);
            this.rows.retainAll(setItem.getRows());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemSet[");
        for (SUDA2Item item : this.items) {
            builder.append(item.toString());
            builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Returns the support rows
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SUDA2ItemSet other = (SUDA2ItemSet) obj;
        if (items == null) {
            if (other.items != null) return false;
        } else if (!items.equals(other.items)) return false;
        return true;
    }

    public Set<SUDA2Item> getItems() {
        return items;
    }
}