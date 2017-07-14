package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntOpenHashSet;
import org.deidentifier.arx.framework.data.Data;

import java.util.*;

public class ARXDataWrapper {

    public static int[][] convert(Data data, int pkey, int aggregateOn) {
        int[][] table = data.getData();
        Vector<IntOpenHashSet> aggregates = new Vector<>();
        for (int[] row : table) {
            IntOpenHashSet l;
            if (aggregates.size() <= row[pkey] || aggregates.get(row[pkey]) == null) {
                l = new IntOpenHashSet();
                aggregates.add(row[pkey], l);
                l.add(row[aggregateOn]);
            } else {
                aggregates.get(row[pkey]).add(row[aggregateOn]);
            }
        }
        int[][] res = new int[aggregates.size()][];
        for (int i = 0; i < res.length; i++) {
            res[i] = aggregates.get(i).toArray();
        }
        return res;
    }

    // ignore this, for testing right now only
    public static String[][] aggregate(String[][] table, int pkey, int aggregateOn) {
        Map<String, Set<String>> aggregates = new HashMap<>();
        for (String[] row : table) {
            HashSet<String> l;
            if (aggregates.get(row[pkey]) == null) {
                l = new HashSet<>();
                aggregates.put(row[pkey], l);
                l.add(row[aggregateOn]);
            } else {
                aggregates.get(row[pkey]).add(row[aggregateOn]);
            }
        }
        String[][] res = new String[aggregates.size()][];
        int i = 0;
        for (Set<String> s : aggregates.values()) {
            res[i] = s.toArray(new String[0]);
            i++;
        }
        return res;
    }
}
