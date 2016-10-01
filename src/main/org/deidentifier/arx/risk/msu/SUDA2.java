package org.deidentifier.arx.risk.msu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.mahout.math.Arrays;

import com.carrotsearch.hppc.LongIntOpenHashMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;

public class SUDA2 {

    /** The data */
    private final int[][]                       data;
    /** Num columns */
    private final int                           columns;
    /** Will be initialized when suda2() is called */
    private LongIntOpenHashMap                  originalRanks;
    /** Will be initialized when suda2() is called */
    private LongObjectOpenHashMap<Set<Integer>> originalSupports;

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
        return suda2(Integer.MAX_VALUE);
    }
        
    /**
     * Executes the SUDA2 algorithm
     * @param maxK
     * @return
     */
    public Set<SUDA2ItemSet> suda2(int maxK) {
        
        // Calculate all items
        LongObjectOpenHashMap<SUDA2Item> items = getItems();

        // Calculate support and rank
        Pair<List<SUDA2Item>, LongIntOpenHashMap> listAndRanks = getItemList(items);
        List<SUDA2Item> list = listAndRanks.first;
        
        // Store original ranks
        this.originalRanks = listAndRanks.second;
        
        // Store original supports
        this.originalSupports = new LongObjectOpenHashMap<>();
        for (SUDA2Item item : list) {
            this.originalSupports.put(item.getId(), item.getRows());
        }
        
        // Execute remainder of SUDA2 algorithm
        return suda2(maxK, list, data.length);
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
     * Returns an item object for the given item
     * @param items
     * @param column
     * @param value
     * @return
     */
    private SUDA2Item getItem(LongObjectOpenHashMap<SUDA2Item> items, int column, int value) {
        long id = SUDA2Item.getId(column, value);
        SUDA2Item item;
        if (items.containsKey(id)) {
            item = items.lget(); 
        } else {
            item = new SUDA2Item(column, value);
            items.put(id, item);
        }
        return item;
    }

    /**
     * Returns a sorted list and stores ranks
     * @param items
     * @return
     */
    private Pair<List<SUDA2Item>, LongIntOpenHashMap> getItemList(LongObjectOpenHashMap<SUDA2Item> items) {

        // Create list and sort by support, move all null values to the end
        List<SUDA2Item> list = new ArrayList<SUDA2Item>();
        Iterator<ObjectCursor<SUDA2Item>> iter = items.values().iterator();
        while (iter.hasNext()) {
            list.add(iter.next().value);
        }
        Collections.sort(list, new Comparator<SUDA2Item>() {
            @Override
            public int compare(SUDA2Item o1, SUDA2Item o2) {
                return o1.getSupport() < o2.getSupport() ? -1 :
                       o1.getSupport() > o2.getSupport() ? +1 : 0;
            }
        });
        
        // Store rank
        LongIntOpenHashMap ranks = new LongIntOpenHashMap();
        for (int rank = 0; rank < list.size(); rank++) {
            ranks.put(list.get(rank).getId(), rank);
        }
        
        // Return
        return new Pair<List<SUDA2Item>, LongIntOpenHashMap>(list, ranks);
    }
    
    /**
     * Returns all items
     * @return
     */
    private LongObjectOpenHashMap<SUDA2Item> getItems() {

        // Collect all items and calculate support
        LongObjectOpenHashMap<SUDA2Item> items = new LongObjectOpenHashMap<>();
        int index = 0;
        for (int[] row : data) {
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                SUDA2Item item = getItem(items, column, value);
                item.addRow(index);
            }
            index++;
        }
        return items;
    }

    /**
     * Returns all items for the given rows
     * @param rows
     * @return
     */
    private LongObjectOpenHashMap<SUDA2Item> getItems(Set<Integer> rows) {

        // Collect all items and calculate support
        LongObjectOpenHashMap<SUDA2Item> items = new LongObjectOpenHashMap<>();
        for (int index : rows) {
            int[] row = data[index];
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                SUDA2Item item = getItem(items, column, value);
                item.addRow(index);
            }
        }
        return items;
    }
    
    /**
     * Clears the list and returns all MSUs
     * @param items
     * @param subsetLength
     * @return
     */
    private Pair<Set<SUDA2ItemSet>, List<SUDA2Item>> getMSUs(List<SUDA2Item> items, int subsetLength) {
        
        // Prepare
        Set<SUDA2ItemSet> msus = new HashSet<>();
        
        // Check the items
        List<SUDA2Item> result = new ArrayList<SUDA2Item>();
        for (SUDA2Item item : items) {

            // All unique items are already MSUs
            if (item.getSupport() == 1) {
                msus.add(new SUDA2ItemSet(item));

            // All items appearing in all rows can be ignored
            } else if (item.getSupport() != subsetLength) {
                result.add(item);
            }
        }

        // Return
        return new Pair<Set<SUDA2ItemSet>, List<SUDA2Item>>(msus, result);
    }

    /**
     * Main part of the SUDA2 algorithm
     * @param maxK
     * @param items
     * @return
     */
    private Set<SUDA2ItemSet> suda2(int maxK, 
                                    List<SUDA2Item> list,
                                    int subsetLength) {
        
        // Find MSUs
        Pair<Set<SUDA2ItemSet>, List<SUDA2Item>> msusAndList = getMSUs(list, subsetLength);
        Set<SUDA2ItemSet> msus = msusAndList.first;
        list = msusAndList.second;

        // Check for maxK
        if (maxK == 1) {
            return msus;
        }

        // For each item i
        for (int index = 0; index < list.size(); index++) {
            
            if (subsetLength == data.length) {
                System.out.println(index+"/"+list.size());
            }
            
            // Obtain item and rank
            SUDA2Item referenceItem = list.get(index);
            int referenceRank = originalRanks.get(referenceItem.getId());

            // Recursive call
            Set<SUDA2ItemSet> msus_i = suda2(referenceItem.getRows(), maxK - 1);

            // For each candidate
            outer: for (SUDA2ItemSet candidate : msus_i) {
                
                // Check if candidate is an MSU
                if (!isMSU(candidate, referenceItem, referenceRank)) {
                    continue outer;
                }

                // Merge them
                SUDA2ItemSet merged = new SUDA2ItemSet(referenceItem, candidate);

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
    
    /**
     * Implements both checks for MSUs described in the paper
     * @param candidate
     * @param referenceItem
     * @param referenceRank
     * @return
     */
    private boolean isMSU(SUDA2ItemSet candidate, SUDA2Item referenceItem, int referenceRank) {

        // All of the k-1 items in the candidate set must have rank > reference rank
        for (SUDA2Item candidateItem : candidate.getItems()) {
            if (originalRanks.get(candidateItem.getId()) <= referenceRank) {
                return false;
            }
        }
        
        // Search for the special row
        if (candidate.getItems().size() == 1) {
            return true;
        }
        
        Set<Integer> Tr = originalSupports.get(referenceItem.getId());
        Set<Integer> T = getSpecialRows(candidate, referenceItem);
        
        // Condition: row contains candidate set but not the referenceItem
        // Such a row R must exist in the original table
        System.out.println(candidate+"->"+new SUDA2ItemSet(referenceItem, candidate)+"->"+Tr+"/"+T);
        
        return true;

//        return (getSpecialRows(candidate, referenceItem).size()==1);
        
    }
    
    /**
     * Returns the special rows
     * @param rows
     * @param set
     * @param reference
     * @return
     */
    private Set<Integer> getSpecialRows(Set<Integer> rows, SUDA2ItemSet set, SUDA2Item reference) {
        
        Set<Integer> result = new HashSet<Integer>();
        for (int index : rows) {
            int[] row = data[index];
            if (reference.isContained(row)) {
                continue;
            }
            for (SUDA2Item item : set.getItems()) {
                if (!item.isContained(row)) {
                    continue;
                }
            }
            result.add(index);
        }
        return result;
    }

    /**
     * Returns the special rows
     * @param set
     * @param reference
     * @return
     */
    private Set<Integer> getSpecialRows(SUDA2ItemSet set, SUDA2Item reference) {
        
        Set<Integer> result = new HashSet<Integer>();
        outer: for (int index = 0; index < data.length; index++) {
            int[] row = data[index];
            if (reference.isContained(row)) {
                continue;
            }
            for (SUDA2Item item : set.getItems()) {
                if (!item.isContained(row)) {
                    continue outer;
                }
            }
            result.add(index);
        }
        return result;
    }

    /**
     * Executes SUDA2 for a given set of rows
     * @param rows
     * @param originalRanks
     * @param maxK
     * @return
     */
    private Set<SUDA2ItemSet> suda2(Set<Integer> rows, int maxK) {

        // Calculate all items
        LongObjectOpenHashMap<SUDA2Item> items = getItems(rows);

        // Calculate support and rank
        Pair<List<SUDA2Item>, LongIntOpenHashMap> listAndRanks = getItemList(items);
        List<SUDA2Item> list = listAndRanks.first;
        
        // Execute remainder of suda2 algorithm
        return suda2(maxK, list, rows.size());
    }
}
