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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExhaustiveSearch {

    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }       
        return sets;
    }

    /** The data */
    private final int[][]  data;
    
    private final int columns;
    
    /**
     * Constructor
     * @param data
     */
    public ExhaustiveSearch(int[][] data) {
        this.data = data;
        this.columns = data[0].length;
    }
    
    public Set<Set<SUDA2Item>> exhaustive() {
        
        Map<Set<SUDA2Item>, Integer> counts = new HashMap<Set<SUDA2Item>, Integer>();
        
        // Collect the powerset of all items in each row and count how often they occur

        System.out.println("Extracting raw elements");
        
        int index = 0;
        for (int[] row : data) {
            
            // Status
            System.out.println(" - " + index++ +"/"+data.length);
            
            // Set of items for this row
            Set<SUDA2Item> items = new HashSet<SUDA2Item>();
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                items.add(new SUDA2Item(column, value));
            }
            
            // Extract power set
            for (Set<SUDA2Item> set : powerSet(items)) {
                
                if (!set.isEmpty()) {
                    
                    // Count number of occurences
                    if (!counts.containsKey(set)) {
                        counts.put(set, 1);
                    } else {
                        counts.put(set, counts.get(set)+1);
                    }
                }
            }
        }

        System.out.println(" - Current size: " + counts.size());
        
        // Create a set and a list containing the items
        Set<Set<SUDA2Item>> result = new HashSet<>();
        result.addAll(counts.keySet());

        System.out.println("Removing elements that are not unique");
        
        // Extract all item sets that occur only once
        Iterator<Set<SUDA2Item>> iter = result.iterator();
        while (iter.hasNext()) {
            if (counts.get(iter.next()) > 1) {
                iter.remove();
            }
        }
        System.out.println(" - Current size: " + result.size());
        
        System.out.println("Removing elements that are not minimal");
        
        // Now remove all item sets which contain any of the other item sets
        List<Set<SUDA2Item>> list = new ArrayList<>();
        list.addAll(result);
        int size = list.size();
        int previous = 0;
        while (size != previous) {
            
            System.out.println(" - Current size: " + size + " previous: " + previous);
            
            // For each itemset, pivot, in this list
            for (Set<SUDA2Item> pivot : list) {
                
                // Remove all itemsets from the set that contain all elements from pivot
                iter = result.iterator();
                while (iter.hasNext()) {
                    Set<SUDA2Item> current = iter.next();
                    if (current != pivot && current.containsAll(pivot)) {
                        iter.remove();
                    }
                }
            }
            previous = size;
            size = result.size();
            list.clear();
            list.addAll(result);
        }

        System.out.println(" - Current size: " + result.size());
       
        DEBUG_printHistogram(result);
    
        // Return
        return result;
}

private void DEBUG_printHistogram(Set<Set<SUDA2Item>> set) {
    
    Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    for (Set<SUDA2Item> itemset : set) {
        int size = itemset.size();
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

private String DEBUG_pad(String s, int width) {
    while (s.length() < width) {
        s = " " + s;
    }
    return s;
}

}
