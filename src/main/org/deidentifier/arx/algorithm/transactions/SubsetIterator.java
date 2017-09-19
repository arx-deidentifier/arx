package org.deidentifier.arx.algorithm.transactions;

import java.math.BigInteger;
import java.util.Iterator;

/**
 * An Iterator implementation that iterates over the subsets of size n of a given set.
 */
public class SubsetIterator implements Iterator {
    // true if the set is too big to be represented by a long
    private boolean needsBigInt;
    // the set that of which the subsets should be
    private int[] set;

    // The variables needed for the iteration
    private BigInteger counter;
    private BigInteger limit;

    private long longCounter;
    private long longLimit;

    public SubsetIterator(int[] set, int selectedItems) {
        if(selectedItems <= 0)
            throw new IllegalArgumentException("Subset size must not be less or equal to zero");

        if (set.length >= 63)
            needsBigInt = true;
        this.set = set;

        if (needsBigInt) {
            counter = new BigInteger("1").shiftLeft(selectedItems).subtract(new BigInteger("1"));
            limit = new BigInteger("1").shiftLeft(set.length);
        } else {
            longCounter = (1 << selectedItems) - 1;
            longLimit = (1L << set.length);
        }
    }

    /**
     *
     * @return a subset of set
     */
    private int[] sub() {
        int[] ret = pick(counter); // creates a subset
        BigInteger c = counter.and(counter.negate()); // and advances the iteration
        BigInteger r = counter.add(c);
        counter = (((r.xor(counter)).shiftRight(2)).divide(c)).or(r);
        return ret;
    }

    /**
     * Same semantics as {@link SubsetIterator#sub()}, but implemented with long. Used when set is smaller than 64 items.
     * @return a subset of set
     */
    private int[] lsub() {
        int[] ret = pick(longCounter); // creates a subset
        long c = longCounter & -longCounter; // and advances the iteration
        long r = longCounter + c;
        longCounter = (((r ^ longCounter) >>> 2) / c) | r;
        return ret;
    }

    @Override
    public boolean hasNext() {
        return (needsBigInt && counter.compareTo(limit) < 0) || (!needsBigInt && longCounter < longLimit);
    }

    @Override
    public int[] next() {
        return needsBigInt ? sub() : lsub();
    }

    /**
     * Each bit in the long represents one item of set.
     *
     * @param longSet the long that should be converted to a subset
     * @return a subset containing the items a_i if bit i is set in longSet
     */
    private int[] pick(long longSet) {
        int[] a = new int[Long.bitCount(longSet)];
        int k = 0;
        int p = 0;
        for (int j = 0; j < 64; j++) {
            if ((longSet & 1) == 1) {
                a[k++] = set[p];
            }
            p++;
            longSet = longSet >>> 1;
        }

        return a;
    }

    /**
     * Each bit in the BigInteger represents one item of set.
     *
     * @param bigIntegerSet the long that should be converted to a subset
     * @return a subset containing the items a_i if bit i is set in bigIntegerSet
     */
    private int[] pick(BigInteger bigIntegerSet) {
        int[] a = new int[bigIntegerSet.bitCount()];
        int k = 0;
        for (int j = 0; j < set.length; j++) {
            if (bigIntegerSet.testBit(j)) {
                a[k++] = set[j];
            }
        }
        return a;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not implemented!");
    }
}
