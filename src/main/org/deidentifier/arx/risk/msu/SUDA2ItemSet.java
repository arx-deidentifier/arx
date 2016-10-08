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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Each item set is a concrete key, i.e. a set of values for a set of attributes.
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemSet {

    /** Items */
    private List<SUDA2Item>  items = new ArrayList<>();

    /**
     * Creates an item set containing a single item
     * @param item
     */
    public SUDA2ItemSet(SUDA2Item item) {
        this.items.add(item);
    }

    /**
     * Creates a merged item set
     * @param item
     * @param set
     */
    public SUDA2ItemSet(SUDA2Item item, SUDA2ItemSet set) {
        this.items.add(item);
        this.items.addAll(set.items);
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }
    
    public List<SUDA2Item> getItems() {
        return items;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether this set is a proper super- or a proper sub-set of the given set
     * @param other
     * @return
     */
    public boolean intersectsWith(SUDA2ItemSet other) {
        return !this.equals(other) && (this.items.containsAll(other.items) || other.items.containsAll(this.items));
    }

    /**
     * Returns the size of the set
     * @return
     */
    public int size() {
        return this.items.size();
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
}