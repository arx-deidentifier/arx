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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.mahout.math.Arrays;

public class SUDA2 {

    /** Debug flag*/
    private static final boolean DEBUG = false;

    /** The data */
    private final int[][]        data;
    /** Number of columns */
    private final int            columns;

    /**
     * Constructor
     * @param data
     */
    public SUDA2(int[][] data) {
        this.check(data);
        this.data = data;
        this.columns = data[0].length;
    }

    /**
     * Executes the SUDA2 algorithm
     * @return
     */
    public Set<SUDA2ItemSet> suda2() {
        return suda2(columns);
    }
        
    /**
     * Executes the SUDA2 algorithm
     * @param maxK
     * @return
     */
    public Set<SUDA2ItemSet> suda2(int maxK) {

        // Obtain sorted item list
        SUDA2ItemList list = getItems().getItemList();
        
        // Obtain map with ranks
        SUDA2ItemRanks ranks = list.getRanks();

        // Execute remainder of SUDA2 algorithm
        return suda2(ranks, list, data.length, maxK);
    }

    /**
     * Check argument
     * @param data
     */
    private void check(int[][] data) {
        if (data == null) {
            throw new NullPointerException("Data must not be null");
        }
        if (data.length == 0 || data[0] == null || data[0].length == 0) {
            throw new IllegalArgumentException("Data must not be empty");
        }
    }

    /**
     * Debugging stuff
     * @param currentItem
     * @param currentList
     * @param maxK
     */
    private void DEBUG_print(SUDA2ItemList list, int maxK) {
        if (!DEBUG) return;
        
        if (list.containsAllItems()) {
            int index = 0;
            for (int[] row : data) {
               DEBUG_println(Arrays.toString(row)+"-"+index++, maxK);
            }
        } else {
            Set<Integer> rows = new HashSet<Integer>();
            for (SUDA2Item item : list.getList()) {
                rows.addAll(item.getRows());
            }
            
            for (int index : rows) {
                DEBUG_println(Arrays.toString(data[index])+"-"+index, maxK);
            }
        }
    }

    /**
     * Debugging stuff
     * @param string
     * @param maxK
     * @return
     */
    private void DEBUG_println(String string, int maxK) {
        if (!DEBUG) return;
        String intent = "";
        for (int i=maxK; i<columns; i++) {
            intent +="   ";
        }
        System.out.println(intent+string);
    }
    
    /**
     * Returns all items
     * @return
     */
    private SUDA2IndexedItemSet getItems() {

        // Collect all items and their support rows
        SUDA2IndexedItemSet items = new SUDA2IndexedItemSet();
        int index = 0;
        for (int[] row : data) {
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                SUDA2Item item = items.getOrCreate(column, value);
                item.addRow(index);
            }
            index++;
        }
        return items;
    }
    
    /**
     * Returns all items for the given reference item
     * @param reference
     * @return
     */
    private SUDA2IndexedItemSet getItems(SUDA2Item reference) {

        // Collect items within the given range and their support rows
        SUDA2IndexedItemSet items = new SUDA2IndexedItemSet(reference);
        for (int index : reference.getRows()) {
            int[] row = data[index];
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                SUDA2Item item = items.getOrCreate(column, value);
                item.addRow(index);
            }
        }
        return items;
    }

    /**
     * Clears the list and returns all MSUs
     * @param list
     * @param numRecords
     * @return
     */
    private Pair<Set<SUDA2ItemSet>, SUDA2ItemList> getMSUs(SUDA2ItemList list, int numRecords) {
        
        // Prepare
        Set<SUDA2ItemSet> msus = new HashSet<>();
        
        // Check the items
        List<SUDA2Item> result = new ArrayList<SUDA2Item>();
        for (SUDA2Item item : list.getList()) {

            // All unique items are already MSUs
            if (item.getSupport() == 1) {
                msus.add(new SUDA2ItemSet(item));

            // All items appearing in all rows can be ignored
            } else if (item.getSupport() != numRecords) {
                result.add(item);
            }
        }

        // Return
        return new Pair<Set<SUDA2ItemSet>, SUDA2ItemList>(msus, new SUDA2ItemList(result, list.getReferenceItem()));
    }

    /**
     * Implements both checks for MSUs described in the paper
     * @param candidate
     * @param referenceItem
     * @param referenceRank
     * @return
     */

    private boolean isMSU(SUDA2ItemRanks ranks,
                          SUDA2ItemList currentList,
                          SUDA2ItemSet candidate,
                          SUDA2Item referenceItem,
                          int referenceRank) {

        // All of the k-1 items in the candidate set must have rank > reference rank
        for (SUDA2Item candidateItem : candidate.getItems()) {
            if (ranks.getRank(candidateItem.getId()) <= referenceRank) {
                return false;
            }
        }
        
        // Search for the special row
        if (candidate.getItems().size() == 2) {
            return true;
        } else {
            return isSpecialRowAvailable(currentList, referenceItem, candidate);
        }
    }

    /**
     * Searches for the special row
     * @param rows
     * @param referenceItem
     * @param candidate
     * @return
     */
    private boolean isSpecialRowAvailable(SUDA2ItemList currentList, SUDA2Item referenceItem, SUDA2ItemSet candidate) {
        
        // TODO: The paper recommends the first branch for all searches
        Set<Integer> rows = null;
//        if (currentList.getReferenceItem() == null) {
                
            // Obtain reference item from the candidate set
            SUDA2Item candidateReferenceItem = candidate.getReferenceItem();
            
            // Obtain according item in the current list
            SUDA2Item candidateReferenceItemInCurrentList = currentList.getItem(candidateReferenceItem.getId());
            
            // If the item is not contained in the current list it must have been
            // a singleton MSU. This implies that the special row cannot exist
            if (candidateReferenceItemInCurrentList == null) {
                return false;
            }
            
            // Else obtain the relevant set of rows
            rows = candidateReferenceItemInCurrentList.getRows();
//        } else {
//            rows = currentList.getReferenceItem().getRows();
//        }
            
        // And search them for the special row
        outer: for (int index : rows) {
            int[] row = data[index];
            if (referenceItem.isContained(row)) {
                continue;
            }
            for (SUDA2Item item : candidate.getItems()) {
                if (!item.isContained(row)) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * SUDA2
     * @param ranks
     * @param currentList
     * @param numRecords
     * @return
     */
    private Set<SUDA2ItemSet> suda2(SUDA2ItemRanks ranks,
                                    SUDA2ItemList currentList,
                                    int numRecords,
                                    int maxK) {
        
        // Debug
        DEBUG_print(currentList, maxK);

        // Find MSUs and clear list
        Pair<Set<SUDA2ItemSet>, SUDA2ItemList> msusAndList = getMSUs(currentList, numRecords);
        Set<SUDA2ItemSet> msus = msusAndList.first;
        currentList = msusAndList.second;
        
        // Debug
        for (SUDA2ItemSet msu : msus) {
            DEBUG_println("Singleton: " + msu, maxK);
        }

        // Check for maxK
        if (maxK == 1) {
            return msus;
        }

        // For each item i
        List<SUDA2Item> list = currentList.getList();
        for (int index = 0; index < list.size(); index++) {
            
            // Progress information
            if (numRecords == data.length) {
                System.out.println(index+"/"+list.size());
            }
            
            // Obtain item and rank
            SUDA2Item referenceItem = list.get(index);
            int referenceRank = ranks.getRank(referenceItem.getId());

            // Debug
            DEBUG_println("Reference: " + referenceItem, maxK - 1);

            // Recursive call
            SUDA2ItemList nextList = getItems(referenceItem).getItemList();
            Set<SUDA2ItemSet> msus_i = suda2(ranks,
                                             nextList,
                                             referenceItem.getRows().size(),
                                             maxK - 1);

            // For each candidate
            outer: for (SUDA2ItemSet candidate : msus_i) {
                
                // Check if candidate is an MSU
                if (!isMSU(ranks, currentList, candidate, referenceItem, referenceRank)) {
                    continue outer;
                }
           
                // Merge them
                SUDA2ItemSet merged = new SUDA2ItemSet(referenceItem, candidate);

                // Debug
                DEBUG_println("Combined: " + merged, maxK);

                // TODO: Just a sanity check
                if (merged.getSupport() != 1) {
                    throw new IllegalStateException("Invalid support count");
                }
                
                // Add MSU
                msus.add(merged);
            }
        }
        
        // Return
        return msus;
    }
}
