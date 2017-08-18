package org.deidentifier.arx.test;

import org.deidentifier.arx.algorithm.transactions.Cut;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class CutTest {
    @Test
    public void testLeafGeneralization() {
        Cut c = new Cut(SampleHierarchies.smallSkewedHierarchy);
        c.generalizeToLevel(2, 2);
        assertTrue(c.getGeneralization(0) == 11
                && c.getGeneralization(1) == 11
                && c.getGeneralization(2) == 11
                && c.getGeneralization(3) == 11);
    }

    @Test
    public void testInnerNodeGeneralization() {
        Cut c = new Cut(SampleHierarchies.smallSkewedHierarchy);
        c.generalizeToLevel(10, 2);
        assertTrue(c.getGeneralization(4) == 12
                && c.getGeneralization(5) == 12
                && c.getGeneralization(6) == 12
                && c.getGeneralization(7) == 12);
    }

    @Test
    public void testIsGeneralized() {
        Cut c = new Cut(SampleHierarchies.smallSkewedHierarchy);
        c.generalizeToLevel(10, 2);
        assertTrue(c.isGeneralized(5));
        assertTrue(c.isGeneralized(7));
        assertTrue(c.isGeneralized(10));
        assertFalse(c.isGeneralized(0));
        assertFalse(c.isGeneralized(1));

        c.generalizeToLevel(0, 1);
        assertTrue(c.isGeneralized(0));
        assertTrue(c.isGeneralized(1));
        assertFalse(c.isGeneralized(2));
        assertFalse(c.isGeneralized(8));
        assertFalse(c.isGeneralized(12));
    }

    @Test
    public void testAncestors() {
        Cut c = new Cut(SampleHierarchies.paperSampleHierarchy);
        System.out.println(c.ancestors());
     }

}
