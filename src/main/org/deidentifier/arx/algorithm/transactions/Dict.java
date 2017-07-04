package org.deidentifier.arx.algorithm.transactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//Class that manages the conversion from string labels to integer representations
public class Dict {

    private Map<String, Integer> m = new HashMap<>();
    private String[] reps;

    /*
    builds the dictionary based on a string hierarchy. Works in a breadth first fashion, so the labels that are less
    generalized have smaller ids. Not really cache-friendly but neccessary right now for a more compact count-tree
    */
    public Dict(String[][] hierarchy) {
        ArrayList<String> reps = new ArrayList<>(hierarchy[0].length);

        int idcounter = 0;
        int generalizationLevel = 0;
        int biggestTran = hierarchy[0].length;
        for (int i = 0; i < biggestTran; i++) {
            for (String[] item : hierarchy) {
                if (item.length > biggestTran)
                    biggestTran = item.length;
                if (item.length > generalizationLevel && !m.containsKey(item[generalizationLevel])) {
                    m.put(item[generalizationLevel], idcounter++);
                    reps.add(idcounter - 1, item[generalizationLevel]);
                }
            }
            generalizationLevel++;
        }
        this.reps = reps.toArray(new String[0]);
    }

    public int getRepresentation(String s) {
        return m.get(s);
    }

    public String getString(int i) {
        return reps[i];
    }

    /**
     * @param t a database with string items
     * @return the database converted with items in integer representation
     */
    public int[][] convertTransactions(String[][] t) {
        int[][] it = new int[t.length][];
        for (int i = 0; i < t.length; i++) {
            it[i] = new int[t[i].length];
            for (int j = 0; j < t[i].length; j++) {
                it[i][j] = m.get(t[i][j]);
            }
        }
        return it;
    }
}


