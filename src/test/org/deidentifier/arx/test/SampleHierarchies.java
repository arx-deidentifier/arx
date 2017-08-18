package org.deidentifier.arx.test;

import com.carrotsearch.hppc.IntOpenHashSet;
import org.deidentifier.arx.algorithm.transactions.Dict;
import org.deidentifier.arx.algorithm.transactions.Hierarchy;

import java.util.Random;


/**
 * Some sample hierarchies for testing
 */
public class SampleHierarchies {

    public static Hierarchy paperSampleHierarchy;
    public static Hierarchy smallSkewedHierarchy;
    public static Hierarchy medUniform;
    public static Hierarchy medSkewed;
    public static Hierarchy bms;
    public static Dict bmsDict;

    public static int[][] paperDatabase;
    public static int[][] smallSkewedDatabase;
    public static Dict paperSampleDict;
    public static Dict smallSkewedDict;

    static {
        String[][] h = {{"a1", "A", "*"}, {"a2", "A", "*"}, {"b1", "B", "*"}, {"b2", "B", "*"}};
        Dict d = new Dict(h);
        paperSampleDict = d;
        paperDatabase = d.convertTransactions(new String[][]{{"a1", "b1", "b2",}, {"a2", "b1",}, {"a2", "b1", "b2"}, {"a1", "a2", "b2"}});
        paperSampleHierarchy = new Hierarchy(h, d);

        h = new String[][]{
                {"a", "A", "D", "F"},
                {"b", "A", "D", "F"},
                {"c", "B", "D", "F"},
                {"d", "B", "D", "F"},
                {"e", "C", "E", "F"},
                {"f", "C", "E", "F"},
                {"g", "C", "E", "F"},
                {"h", "C", "E", "F"},
        };
        d = new Dict(h);
        smallSkewedDict = d;
        smallSkewedHierarchy = new Hierarchy(h, d);

        int n = 100;
        generateSmallSkewedDatabase(n);
    }

    static void generateSmallSkewedDatabase(int n, long seed) {
        Random r = new Random(seed);
        smallSkewedDatabase = new int[n][];
        for (int i = 0; i < n; i++) {
            IntOpenHashSet s = new IntOpenHashSet();
            for (int j = 0; j < r.nextInt(10) + 1; j++) {
                s.add(r.nextInt(8));
            }
            smallSkewedDatabase[i] = s.toArray();
        }
    }

    static void generateSmallSkewedDatabase(int n) {
        generateSmallSkewedDatabase(n, new Random().nextInt());
    }

}
