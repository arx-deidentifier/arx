package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntOpenHashSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates subsets of the BMS WebView2 dataset based on parameters "size" and "domainsize". Performs modulo on the
 * items with "size" and then removes duplicate items from the transaction. Randomly samples the original dataset for
 * transactions.
 */
public class BMSGenerator {

    public static void main(String[] args) throws IOException {

        for (int j = 2000; j <= 20000 ; j+=2000)
        for (int i = 25; i <= 70; i += 5) {
            int domainSize = i;
            int dbSize = j;
            String saveFile = String.format("data/BMS2_size%d_domainSize%d.csv", dbSize, domainSize);
            generateBMS(dbSize, domainSize, saveFile);
        }
    }


    public static void generateBMS(int dbSize, int domainSize, String saveFile) throws IOException {
        File f = new File("data/Webview2.csv");

        List<String> lines = Files.readAllLines(f.toPath(), Charset.defaultCharset());
        List<List<String>> aggregatedLines = new ArrayList<>();

        String id = lines.get(0).split(",")[0];
        List<String> tran = new ArrayList<>();
        for (String line : lines) {
            String curId = line.split(",")[0];
            if(!curId.equals(id)){
                aggregatedLines.add(tran);
                tran = new ArrayList<>();
                id = curId;
            }
            tran.add(line.split(",")[1]);
        }

        String[][] fulldb = new String[aggregatedLines.size()][];

        for (int i = 0; i < aggregatedLines.size(); i++) {
            fulldb[i] = aggregatedLines.get(i).toArray(new String[0]);
        }

        int[][] sampledTransactions = new int[dbSize][];

        for (int i = 0; i < dbSize; i++) {
            IntOpenHashSet tra = new IntOpenHashSet();
            for (String string : fulldb[i]) {
                tra.add(Integer.parseInt(string) % domainSize);
            }
            sampledTransactions[i] = tra.toArray();
        }


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
