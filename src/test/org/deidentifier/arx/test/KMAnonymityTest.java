package org.deidentifier.arx.test;

import org.deidentifier.arx.algorithm.transactions.CountTree;
import org.deidentifier.arx.algorithm.transactions.Cut;
import org.deidentifier.arx.algorithm.transactions.Hierarchy;
import org.deidentifier.arx.algorithm.transactions.KMAnonymity;
import org.junit.Test;

import java.io.IOException;

import static org.deidentifier.arx.test.SampleHierarchies.*;
import static org.junit.Assert.assertTrue;

public class KMAnonymityTest {


    @Test
    public void testOAPaperSample() throws IOException {
        int k = 2;
        int m = 2;
        KMAnonymity kk = new KMAnonymity(k, m, paperSampleHierarchy, paperDatabase);
        Cut c = kk.optimalAnonymization();
        assertTrue(isKAnonymous(c.generalize(paperDatabase), paperSampleHierarchy, k, m));
        System.out.println(kk.informationLoss());
    }

    @Test
    public void testDAPaperSample() {
        int k = 2;
        int m = 2;
        KMAnonymity kk = new KMAnonymity(k, m, paperSampleHierarchy, paperDatabase);
        Cut c = kk.directAnonymization();
        assertTrue(isKAnonymous(c.generalize(paperDatabase), paperSampleHierarchy, k, m));
        System.out.println(kk.informationLoss());
    }

    @Test
    public void testAAPaperSample() {
        int k = 2;
        int m = 2;
        KMAnonymity kk = new KMAnonymity(k, m, paperSampleHierarchy, paperDatabase);
        Cut c = kk.aprioriAnonymization();
        assertTrue(isKAnonymous(c.generalize(paperDatabase), paperSampleHierarchy, k, m));
        System.out.println(kk.informationLoss());
    }

    @Test
    public void testOASmallSkewed() throws IOException {
        int k = 7;
        int m = 3;
        KMAnonymity kk = new KMAnonymity(k, m, smallSkewedHierarchy, smallSkewedDatabase);
        Cut c = kk.optimalAnonymization();
        assertTrue(isKAnonymous(c.generalize(smallSkewedDatabase), smallSkewedHierarchy, k, m));
        System.out.println(kk.informationLoss());
    }

    @Test
    public void testDASmallSkewed() {
        int k = 7;
        int m = 3;
        KMAnonymity kk = new KMAnonymity(k, m, smallSkewedHierarchy, smallSkewedDatabase);
        Cut c = kk.directAnonymization();
        assertTrue(isKAnonymous(c.generalize(smallSkewedDatabase), smallSkewedHierarchy, k, m));
        System.out.println(kk.informationLoss());
    }

    @Test
    public void testAASmallSkewed() {
        int k = 7;
        int m = 3;
        KMAnonymity kk = new KMAnonymity(k, m, smallSkewedHierarchy, smallSkewedDatabase);
        Cut c = kk.aprioriAnonymization();
        assertTrue(isKAnonymous(c.generalize(smallSkewedDatabase), smallSkewedHierarchy, k, m));
        System.out.println(kk.informationLoss());
    }

    private boolean isKAnonymous(int[][] db, Hierarchy h, int k, int m) {
        CountTree ct = new CountTree(m, db, h);
        return ct.isKManonymous(k);
    }
}
