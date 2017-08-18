package org.deidentifier.arx.test;

import org.deidentifier.arx.algorithm.transactions.SubsetIterator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SubsetIteratorTest {
    public static final int SMALL_SET_SIZE = 10;
    public static final int LARGE_SET_SIZE = 100;


    private int countItems(SubsetIterator it) {
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    private long binomial(final int N, final int K) {
        long ret = 1;
        for (int k = 0; k < K; k++) {
            ret = ret * (N - k) / (k + 1);
        }
        return ret;
    }

    public int[] generateSet(int size) {
        int[] k = new int[size];
        for (int i = 0; i < size; i++) {
            k[i] = i;
        }
        return k;
    }

    @Test
    public void testEmptySet() {
        SubsetIterator it = new SubsetIterator(new int[0], 1);
        assertEquals(0, countItems(it));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroSelectedItems() {
        SubsetIterator it = new SubsetIterator(new int[5], 0);
    }

    @Test
    public void testSubsetSizeLessThanSetSize() {
        SubsetIterator it = new SubsetIterator(generateSet(4), 4);
        while (it.hasNext()) {
            int[] next = it.next();
            System.out.println(Arrays.toString(next));
        }
    }


    @Test
    public void testSmallSet() {
        int choose = new Random().nextInt(6) + 1;
        for (int i = 1; i < choose; i++) {
            SubsetIterator it = new SubsetIterator(generateSet(SMALL_SET_SIZE), i);
            assertEquals(binomial(SMALL_SET_SIZE, i), countItems(it));
        }
    }

    @Test
    public void testLargeSet() {
        int choose = new Random().nextInt(5) + 1;
        for (int i = 1; i < choose; i++) {
            SubsetIterator it = new SubsetIterator(generateSet(LARGE_SET_SIZE), i);
            assertEquals(binomial(LARGE_SET_SIZE, i), countItems(it));
        }
    }


}
