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

import java.util.List;

import com.carrotsearch.hppc.LongObjectOpenHashMap;

/**
 * A list of items. It may be indexed dynamically.
 * 
 * @author Fabian Prasser
 *
 */
public class SUDA2ItemList {

    /** The actual list */
    private final List<SUDA2Item>            list;
    /** The index */
    private LongObjectOpenHashMap<SUDA2Item> index = null;
    /** The reference item */
    private final SUDA2Item                  reference;

    /**
     * Creates a new instance
     * @param list
     * @param reference
     */
    public SUDA2ItemList(List<SUDA2Item> list, SUDA2Item reference) {
        this.reference = reference;
        this.list = list;
    }

    /**
     * Returns whether the list is empty
     * @return
     */
    public boolean containsAllItems() {
        return this.list == null;
    }
    
    /**
     * Returns the item with the given id. Indexes the set, if not done already
     * @param id
     * @return
     */
    public SUDA2Item getItem(long id) {
        if (index == null) {
            index = new LongObjectOpenHashMap<>();
            for (SUDA2Item item : list) {
                index.put(item.getId(), item);
            }
        }
        return index.get(id);
    }
    
    /**
     * @return the list
     */
    public List<SUDA2Item> getList() {
        return list;
    }

    /**
     * Returns ranks for this list
     * @return
     */
    public SUDA2ItemRanks getRanks() {
        return new SUDA2ItemRanks(this);
    }

    /**
     * Returns the reference item
     * @return
     */
    public SUDA2Item getReferenceItem() {
        return this.reference;
    }
}
