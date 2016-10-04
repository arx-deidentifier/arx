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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Each item set is a concrete key, i.e. a set of values for a set of attributes.
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemSet {

    /** Items */
    private Set<SUDA2Item>  items = new HashSet<>();
    /** Support rows */
    private Set<Integer>    rows  = new HashSet<>();
    /** Reference item */
    private SUDA2Item       reference = null;

    /**
     * Creates an item set containing a single item
     * @param item
     */
    public SUDA2ItemSet(SUDA2Item item) {
        init1(item);
    }

    /**
     * Creates a merged item set
     * @param item
     * @param set
     */
    public SUDA2ItemSet(SUDA2Item item, SUDA2ItemSet set) {
        init2(item, set);
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

    /**
     * Returns the reference item
     */
    public SUDA2Item getReferenceItem() {
        if (this.reference == null) {
            SUDA2Item reference = null;
            int support = Integer.MAX_VALUE;
            for (SUDA2Item item : this.items) {
                if (item.getSupport() < support) {
                    support = item.getSupport();
                    reference = item;
                }
            }
            this.reference = reference;
        }
        return this.reference;
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemSet[");
        Iterator<SUDA2Item> iter = this.items.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next().toString());
            if (iter.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * For profiling
     * @param item
     */
    private void init1(SUDA2Item item) {
        this.items.add(item);
        this.rows.addAll(item.getRows());
    }

    /**
     * For profiling
     * @param item
     * @param set
     */
    private void init2(SUDA2Item item, SUDA2ItemSet set) {
        this.items.add(item);
        this.rows.addAll(item.getRows());
        for (SUDA2Item setItem : set.items) {
            this.items.add(setItem);
            this.rows.retainAll(setItem.getRows());
        }
    }
}