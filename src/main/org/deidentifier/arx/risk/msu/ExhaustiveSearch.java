package org.deidentifier.arx.risk.msu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ExhaustiveSearch {

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
        
        for (int[] row : data) {
            Set<SUDA2Item> items = new HashSet<SUDA2Item>();
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                items.add(new SUDA2Item(column, value));
            }
            for (Set<SUDA2Item> set : powerSet(items)) {
                if (!counts.containsKey(set)) {
                    counts.put(set, 1);
                } else {
                    counts.put(set, counts.get(set)+1);
                }
            }
        }
        List<Set<SUDA2Item>> list = new ArrayList<Set<SUDA2Item>>();
        
        for (Entry<Set<SUDA2Item>, Integer> entry : counts.entrySet()) {
            if (entry.getValue()==1) {
                list.add(entry.getKey());
            }
        }
        
        int size = list.size();
        int previous = 0;
        while (size != previous) {
            
            for (int i=0; i<list.size(); i++) {
                
                Set<SUDA2Item> pivot = list.get(i);
                
                Iterator<Set<SUDA2Item>> iter = list.iterator();
                while (iter.hasNext()) {
                    Set<SUDA2Item> current = iter.next();
                    if (current != pivot && current.containsAll(pivot)) {
                        iter.remove();
                    }
                }
            }
            previous = size;
            size = list.size();
        }
        return new HashSet<Set<SUDA2Item>>(list);
    }
    
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
}
