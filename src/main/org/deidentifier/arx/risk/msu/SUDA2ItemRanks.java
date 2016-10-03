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

import com.carrotsearch.hppc.LongIntOpenHashMap;

/**
 * A map associating items with their ranks in the original table.
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemRanks {
    
    /** Ranks */
    private final LongIntOpenHashMap ranks = new LongIntOpenHashMap();
    
    /**
     * Creates a new instance
     * @param _list
     */
    public SUDA2ItemRanks(SUDA2ItemList _list) {
        
        List<SUDA2Item> list = _list.getList();
        
        // Store rank
        for (int rank = 0; rank < list.size(); rank++) {
            ranks.put(list.get(rank).getId(), rank);
        }
    }
    
    /**
     * Returns the rank for a given id
     * @param id
     * @return
     */
    public int getRank(long id) {
        return this.ranks.get(id);
    }
}
