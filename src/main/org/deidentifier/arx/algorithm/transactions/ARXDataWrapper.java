package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntOpenHashSet;
import org.deidentifier.arx.framework.data.Data;

import java.util.Vector;

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
}
