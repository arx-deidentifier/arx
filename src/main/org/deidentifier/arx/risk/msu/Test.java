package org.deidentifier.arx.risk.msu;

import java.util.HashSet;
import java.util.Set;

public class Test {

    public static void main(String[] args) {

        // (1,3), (2,0), (4,2)
        int[][] data = new int[][] {
                new int[] { 0, 3, 0, 1, 1 }, // 0 - SPECIAL
                new int[] { 0, 3, 0, 0, 1 }, // 1 - SPECIAL
                new int[] { 0, 3, 1, 1, 1 }, // 2
                new int[] { 1, 3, 0, 1, 2 }, // 3 - *
                new int[] { 0, 2, 0, 1, 2 }, // 4 
                new int[] { 1, 2, 1, 0, 2 }  // 5
        };
        
        System.out.println("26 is correct");

        SUDA2 suda2 = new SUDA2(data);
        Set<SUDA2ItemSet> result = suda2.suda2(5);
        System.out.println("Found: " + result.size() + " MSUs");
        for (SUDA2ItemSet set : result) {
            System.out.println(set + "-" + set.getRows());
        }

        ExhaustiveSearch exhaustive = new ExhaustiveSearch(data);
        Set<Set<SUDA2Item>> result2 = exhaustive.exhaustive();
        System.out.println("Found: " + result2.size() + " MSUs");
        for (Set<SUDA2Item> set : result2) {
            System.out.println(set);
        }
        
        analyze(result, result2);

    }

    private static void analyze(Set<SUDA2ItemSet> result, Set<Set<SUDA2Item>> result2) {
        System.out.println("Found by SUDA2: " + result.size());
        System.out.println("Found by exhaustive: " + result2.size());
        
        Set<Set<SUDA2Item>> set = new HashSet<>();
        for (SUDA2ItemSet itemset : result) {
            set.add(itemset.getItems());
        }
        Set<Set<SUDA2Item>> set2 = new HashSet<>();
        set2.addAll(set);
        set2.removeAll(result2);
        result2.removeAll(set);
        
        
        System.out.println("In SUDA2 but not in exhaustive: " + set2);
        System.out.println("In exhaustive nut not in SUDA2: " + result2);
    }
       
}
