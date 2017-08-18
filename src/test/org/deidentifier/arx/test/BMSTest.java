package org.deidentifier.arx.test;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.algorithm.transactions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BMSTest {


    public static void main(String[] args) throws IOException {
        File f = new File("data/Webview2.csv");
        File hierarchyFile = new File("data/BMS_webview2_hierarchy.txt");
        Data data = Data.create(f, Charset.defaultCharset(), ',');

        AttributeType.Hierarchy h = AttributeType.Hierarchy.create(hierarchyFile, Charset.defaultCharset(), ',');
        Hierarchy hierarchy = ARXHierarchyWrapper.convert(h);
        Dict d = new Dict(h.getHierarchy());

        List<String[]> transactions = new ArrayList<>();
        Iterator<String[]> it = data.getHandle().iterator();

        while (it.hasNext()) {
            String[] next = it.next();
            transactions.add(next);
        }

        int[][] intTran = d.convertTransactions(ARXDataWrapper.aggregate(transactions.toArray(new String[0][]), 0, 1));

        int k = 50;
        int m = 3;

        System.out.println("Testing with domainsize: " + hierarchy.getDomainItems().length + "\n" + "transactions: " + intTran.length);

        Cut c;
        long start = System.nanoTime();
        KMAnonymity anon = new KMAnonymity(k, m, hierarchy, intTran);
/*
       c = anon.directAnonymization();
        System.out.println(new CountTree(m, c.generalize(sampledTransactions), hierarchy).isKManonymous(k));
        System.out.println(c);
        System.out.println(System.nanoTime() - start);

         System.out.println("DA IL: " + anon.informationLoss());
*/

        start = System.nanoTime();

        c = anon.aprioriAnonymization();
        System.out.println(System.nanoTime() - start);
        System.out.println(new CountTree(m, c.generalize(intTran), hierarchy).isKManonymous(k));
        System.out.println(c);

        System.out.println("AA IL: " + anon.informationLoss());
    }

}
