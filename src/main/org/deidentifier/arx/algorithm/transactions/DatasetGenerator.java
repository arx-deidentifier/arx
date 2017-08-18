package org.deidentifier.arx.algorithm.transactions;

import org.apache.commons.math3.distribution.*;

import java.io.FileWriter;
import java.io.IOException;

public class DatasetGenerator {

    public static int[][] generate(int size, IntegerDistribution itemGenerator, RealDistribution transactionLengthGenerator) {
        int[][] db = new int[size][];
        for (int i = 0; i < size; i++) {
            db[i] = new int[(int) Math.abs(transactionLengthGenerator.sample()) + 1];
            for (int j = 0; j < db[i].length; j++) {
                db[i][j] = itemGenerator.sample();
            }
        }
        return db;
    }

    static void save(int[][] db, String file) throws IOException {
        FileWriter fw = new FileWriter(file);

        for (int i = 0; i < db.length; i++) {
            for (int j = 0; j < db[i].length; j++) {
                fw.write(i + "," + db[i][j] + "\n");
            }
        }
        fw.flush();
        fw.close();
    }


    public static void main(String[] args) throws IOException {
        for (int i = 500; i <= 4000; i += 500) {
            int size = i;
            int maxTransactionSize = 120;
            int domainSize = 60;
            String saveFile = String.format("data/dataset_size%d_domainSize%d.csv", size, domainSize);

            save(generate(size, new UniformIntegerDistribution(0, domainSize - 1), new NormalDistribution(10, Math.sqrt(10 + maxTransactionSize))), saveFile);
        }

    }
}
