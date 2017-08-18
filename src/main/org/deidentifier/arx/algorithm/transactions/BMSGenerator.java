package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntOpenHashSet;
import org.deidentifier.arx.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Generates subsets of the BMS WebView2 dataset based on parameters "size" and "domainsize". Performs modulo on the
 * items with "size" and then removes duplicate items from the transaction. Randomly samples the original dataset for
 * transactions.
 */
public class BMSGenerator {

    public static void main(String[] args) throws IOException {


        for (int i = 5000; i < 30000; i += 5000) {
            int domainSize = 40;
            String saveFile = String.format("data/BMS2_size%d_domainSize%d.csv", i, domainSize);
            generateBMS(i, domainSize, saveFile);
        }

    }


    public static void generateBMS(int dbSize, int domainSize, String saveFile) throws IOException {
        File f = new File("data/Webview2.csv");
        Data data = Data.create(f, Charset.defaultCharset(), ',');


        List<IntOpenHashSet> transactions = new ArrayList<>();
        Iterator<String[]> it = data.getHandle().iterator();

        while (it.hasNext()) {
            String[] next = it.next();
            IntOpenHashSet i = new IntOpenHashSet();
            transactions.add(i);
            for (String s : next) {
                i.add(Integer.parseInt(s) % domainSize);
            }
        }

        int[][] moduloDB = new int[transactions.size()][];
        for (int i = 0; i < moduloDB.length; i++) {
            moduloDB[i] = transactions.get(i).toArray();
        }

        List<int[]> v = new ArrayList<>(moduloDB.length);
        v.addAll(Arrays.asList(moduloDB));

        Random r = new Random();

        for (int i = 0; i < moduloDB.length; i++) {
            moduloDB[i] = v.get(r.nextInt(v.size()));
        }

        int[][] sampledTransactions = new int[dbSize][];
        System.arraycopy(moduloDB, 0, sampledTransactions, 0, dbSize);

        FileWriter fw = new FileWriter(saveFile);

        for (int i = 0; i < sampledTransactions.length; i++) {
            for (int j = 0; j < sampledTransactions[i].length; j++) {
                fw.write(i + "," + sampledTransactions[i][j] + "\n");
            }
        }
        fw.flush();
        fw.close();
    }
}
