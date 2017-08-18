package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.google.common.primitives.Ints;
import org.apache.commons.lang.StringUtils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Generates arbitrary generalization hierarchies based on domainsize and node degree
 */
public class HierarchyGenerator {

    static int[][] generate(int domainSize, int degree) {
        IntArrayList[] p = new IntArrayList[domainSize];
        int[][] ret = new int[domainSize][];

        for (int i = 0; i < domainSize; i++) {
            if (p[i] == null)
                p[i] = new IntArrayList();
            p[i].add(i);
        }

        int level = 1;
        int node = domainSize;
        int nodeCounter = 0;
        while (true) {
            for (int i = 0; i < domainSize; i++) {
                nodeCounter++;
                p[i].add(node);
                if ((nodeCounter) % Math.pow(degree,level) == 0) {
                    nodeCounter = 0;
                    node++;
                }
            }
            if(nodeCounter==domainSize)
                break;
            nodeCounter =0;
            node++;
            level++;
        }

        for (int i = 0; i < domainSize; i++) {
            ret[i] = p[i].toArray();
        }

        return ret;
    }


    public static void main(String[] args) throws IOException {
        generateAndSave(40, 4,String.format("data/Hierarchy_size%s_degree%s.csv", 40, 4));
    }


    static void generateAndSave(int size, int degree, String file) throws IOException {
        int[][] m = generate(size, degree);
        saveHierarchy(file, m);
    }

    private static void saveHierarchy(String path, int[][] hierarchy) throws IOException {
        FileWriter fw = new FileWriter(path);
        for (int[] aHierarchy : hierarchy) {
            fw.write(StringUtils.join(Ints.asList(aHierarchy), ',') + "\n");
        }
        fw.flush();
        fw.close();
    }
}
