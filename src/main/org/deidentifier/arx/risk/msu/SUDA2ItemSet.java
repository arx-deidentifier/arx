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

import java.util.Arrays;

/**
 * Each item set is a concrete key, i.e. a set of values for a set of attributes.
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemSet {

    /** Items */
    private SUDA2Item[] items;
    /** Size */
    private int         size = 1;

    /**
     * Creates a merged item set
     * @param item
     * @param set
     */
    public SUDA2ItemSet(SUDA2Item item) {
        this.items = new SUDA2Item[10];
        this.items[0] = item;
    }
    
    /**
     * Adds an item
     * @param item
     */
    public void add(SUDA2Item item) {
        this.increment();
        this.items[this.size - 1] = item;
    }
    
    /**
     * Returns the item at the given index, in reverse order. For some reason this
     * makes the implementation much more efficient.
     * 
     * @param index
     * @return
     */
    public SUDA2Item get(int index) {
        return this.items[size - 1 - index];
    }

    /**
     * Increases the capacity of this set.
     */
    public void increment() {
        this.size++;
        int capacity = this.items.length;
        if (this.size > capacity) {
            int newCapacity = (capacity * 3) / 2 + 1;
            if (newCapacity < size) {
                newCapacity = size;
            }
            this.items = Arrays.copyOf(this.items, newCapacity);
        }
    }
    
    /**
     * Returns the size of the set
     * @return
     */
    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemSet[");
        for (int i = 0; i < size; i++) {
            SUDA2Item item = this.items[i];
            builder.append(item);
            if (i < size - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}