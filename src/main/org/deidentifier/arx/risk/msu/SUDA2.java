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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.Arrays;

public class SUDA2 {

    /** Debug flag */
    private static final boolean      DEBUG            = false;
    /** Debug data */
    private int                       DEBUG_CALLS      = 0;

    /** The data */
    private final int[][]             data;
    /** Number of columns */
    private final int                 columns;

    /**
     * Constructor
     * @param data
     */
    public SUDA2(int[][] data) {
        
        // Check
        this.check(data);
        
        // Init
        this.data = data;
        this.columns = data[0].length;
    }

    /**
     * Executes the SUDA2 algorithm
     * 
     * @param maxK If maxK <= 0, maxK will be set to the number of columns
     * @return
     */
    public Set<SUDA2ItemSet> suda2(int maxK) {
        
        // If maxK <= 0, maxK will be set to the number of columns
        maxK = maxK > 0 ? maxK : columns;
        
        // Reset
        DEBUG_CALLS = 0;

        // Execute remainder of SUDA2 algorithm
        Set<SUDA2ItemSet> set = suda2(maxK, this.getItems().getItemList(), data.length);
        
        System.out.println("Calls: " + DEBUG_CALLS);
        
        DEBUG_printHistogram(set);
        
        // Return
        return set;
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
    
    private String DEBUG_pad(String s, int width) {
        while (s.length() < width) {
            s = " " + s;
        }
        return s;
    }

    /**
     * Debugging stuff
     * @param currentItem
     * @param currentList
     * @param depth
     */
    private void DEBUG_print(SUDA2ItemList list, int depth) {
        if (!DEBUG) return;
        
        if (list.containsAllItems()) {
            int index = 0;
            for (int[] row : data) {
               DEBUG_println(Arrays.toString(row)+"-"+index++, depth);
            }
        } else {
            Set<Integer> rows = new HashSet<Integer>();
            for (SUDA2Item item : list.getList()) {
                rows.addAll(item.getRows());
            }
            
            for (int index : rows) {
                DEBUG_println(Arrays.toString(data[index])+"-"+index, depth);
            }
        }
    }

    private void DEBUG_printHistogram(Set<SUDA2ItemSet> set) {
        
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (SUDA2ItemSet itemset : set) {
            int size = itemset.getItems().size();
            Integer count = map.get(size);
            count = count == null ? 1 : count +1;
            map.put(size, count);
        }
        
        
        List<Integer> sizes = new ArrayList<Integer>();
        sizes.addAll(map.keySet());
        Collections.sort(sizes);
        
        for (int size : sizes) {
            System.out.print(DEBUG_pad(String.valueOf(size), 5) + "|");
        }
        System.out.println("");
        for (int size : sizes) {
            System.out.print(DEBUG_pad(String.valueOf(map.get(size)), 5) + "|");
        }
        System.out.println("");
        System.out.println("MSUs: " +set.size());
    }

    /**
     * Debugging stuff
     * @param string
     * @param depth
     * @return
     */
    private void DEBUG_println(String string, int depth) {
        if (!DEBUG) return;
        String intent = "";
        for (int i = 0; i < columns - depth; i++) {
            intent += "   ";
        }
        System.out.println(intent + string);
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
                SUDA2Item item = items.getOrCreateItem(column, value);
                item.addRow(index);
            }
            index++;
        }
        return items;
    }

    /**
     * Returns all 1-MSUs from the table for the given reference item
     * @param list
     * @param reference
     * @return
     */
    private Set<SUDA2ItemSet> getMSUs(SUDA2ItemList list, SUDA2Item reference) {
        // Collect items within the given range and their support rows
        Set<SUDA2ItemSet> items = new HashSet<>();
        for (SUDA2Item _item : list.getList()) {
            Set<Integer> rows = new HashSet<>();
            rows.addAll(_item.getRows());
            rows.retainAll(reference.getRows());
            if (rows.size() == 1) {
                items.add(new SUDA2ItemSet(new SUDA2Item(_item.getColumn(), _item.getValue(), rows)));
            }
        }
        return items;
    }

    /**
     * Returns all items for the given reference item from the given list.
     * This means that all 1-MSUs can be removed beforehand.
     * @param list
     * @param reference
     * @return
     */
    private SUDA2IndexedItemSet getItems(SUDA2ItemList list, SUDA2Item reference) {

        // Collect items within the given range and their support rows
        SUDA2IndexedItemSet items = new SUDA2IndexedItemSet(reference);
        for (SUDA2Item _item : list.getList()) {
            Set<Integer> rows = new HashSet<>();
            rows.addAll(_item.getRows());
            rows.retainAll(reference.getRows());
            if (!rows.isEmpty()) {
                SUDA2Item item = items.getOrCreateItem(_item.getColumn(), _item.getValue());
                item.addRows(rows);
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
//
//    /**
//     * Returns all 1-MSUs for the given item
//     * @param item
//     * @return
//     */
//    private Set<SUDA2ItemSet> getOneMSUs(SUDA2Item item) {
//        return getItems(item).getOneMSUs();
//    }

    /**
     * Implements both checks for MSUs described in the paper
     * @param currentList
     * @param currentRanks 
     * @param candidate
     * @param referenceItem
     * @param referenceRank
     * @return
     */

    private boolean isMSU(SUDA2ItemList currentList,
                          SUDA2ItemRanks currentRanks, 
                          SUDA2ItemSet candidate,
                          SUDA2Item referenceItem,
                          int referenceRank) {

        // All of the k-1 items in the candidate set must have rank > reference rank
        for (SUDA2Item candidateItem : candidate.getItems()) {
            if (currentRanks.getRank(candidateItem.getId()) <= referenceRank) {
                return false;
            }
        }
        
        // Search for the special row
        if (candidate.getItems().size() <= 1) {
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
        
        // Obtain reference item from the candidate set
        SUDA2Item candidateReferenceItem = candidate.getReferenceItem();

        // Obtain according item in the current list
        SUDA2Item candidateReferenceItemInCurrentList = currentList.getItem(candidateReferenceItem.getId());

        // If the item is not contained in the current list it must have been
        // a singleton MSU. This implies that the special row cannot exist
        if (candidateReferenceItemInCurrentList == null) { return false; }

        // Else obtain the relevant set of rows
        Set<Integer> rows = candidateReferenceItemInCurrentList.getRows();

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
     * @param maxK
     * @param currentList
     * @param numRecords
     * @return
     */
    private Set<SUDA2ItemSet> suda2(int maxK,
                                    SUDA2ItemList currentList,
                                    int numRecords) {

        DEBUG_CALLS++;

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
        if (maxK <= 1) {
            return msus;
        }

        // For each item i
        int index = 0;
        SUDA2ItemRanks currentRanks = currentList.getRanks();
        for (SUDA2Item referenceItem : currentList.getList()) {
            
            // Track
            index++;
            
            // Progress information
            if (numRecords == data.length) {
                System.out.println(index + "/" + currentList.size() + " -> " + DEBUG_CALLS);
            }

            // Obtain rank
            int referenceRank = currentRanks.getRank(referenceItem.getId());

            // Debug
            DEBUG_println("Reference: " + referenceItem, maxK);

            // Recursive call
            Set<SUDA2ItemSet> msus_i;
            int upperLimit = Math.min(maxK - 1, currentList.size() - index + 1);
            if (upperLimit == 1) {
                msus_i = getMSUs(currentList, referenceItem);
            } else {
                msus_i = suda2(upperLimit, // Pruning strategy 2
                               getItems(currentList, referenceItem).getItemList(),
                               referenceItem.getRows().size());
            }

            // For each candidate
            outer: for (SUDA2ItemSet candidate : msus_i) {
                
                // Check if candidate is an MSU
                if (!isMSU(currentList, currentRanks, candidate, referenceItem, referenceRank)) {
                    continue outer;
                }
           
                // Merge them
                SUDA2ItemSet merged = new SUDA2ItemSet(referenceItem, candidate);

                // Debug
                DEBUG_println("Combined: " + merged, maxK);

                // TODO: Just a sanity check
//                if (maxK == 1) {
//                    for (SUDA2ItemSet existing : msus) {
//                        if (existing.intersectsWith(merged)) {
//                            throw new IllegalStateException("Non-minimal result");
//                        }
//                    }
//                    Set<Integer> rows = new HashSet<Integer>();
//                    Iterator<SUDA2Item> iter = merged.getItems().iterator();
//                    rows.addAll(this.set.getItem(iter.next().getId()).getRows());
//                    while (iter.hasNext()) {
//                        rows.retainAll(this.set.getItem(iter.next().getId()).getRows());
//                    }
//                    if (rows.size() != 1) {
//                        throw new IllegalStateException("Non-unique result");
//                    }
//                }

                // Add MSU
                msus.add(merged);
            }
        }
        
        // Return
        return msus;
    }
}
