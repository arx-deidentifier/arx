/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.framework.check.groupify;

/**
 * This class implements several helper methods for hash tables
 * 
 * @author Prasser, Kohlmayer
 * 
 */
public class HashTableUtil {

    /** The Constant ALENGTH. */
    private static final int      ALENGTH = 1000;

    /** The Constant ARRAY. */
    private static final Object[] ARRAY   = new Object[ALENGTH];

    /**
     * Calculates a new capacity.
     * 
     * @param x
     *            the parameter
     * @return the capacity
     */
    public static final int calculateCapacity(int x) {
        if (x >= (1 << 30)) { return 1 << 30; }
        if (x == 0) { return 16; }
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    /**
     * Computes the threshold for rehashing.
     */
    public static final int calculateThreshold(final int buckets,
                                               final float loadFactor) {
        return (int) (buckets * loadFactor);
    }

    /**
     * Equality check for integer arrays.
     * 
     * @param a
     *            an array
     * @param a2
     *            another array
     * @return true, if equal
     */
    public static final boolean equals(final int[] a, final int[] a2) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != a2[i]) { return false; }
        }
        return true;
    }

    /**
     * Computes a hashcode for an integer array.
     * 
     * @param array
     *            the array
     * @return the hashcode
     */
    public static final int hashcode(final int[] array) {
        int result = 23;
        for (int i = 0; i < array.length; i++) {
            result = (37 * result) + array[i];
        }
        return result;
    }

    /**
     * Returns the same result as Arrays.fill(array, null)
     * 
     * @param array
     *            the array
     */
    public static final void nullifyArray(final Object[] array) {
        final int full = array.length / ALENGTH;
        final int part = (full == 0) ? array.length : (array.length % ALENGTH);
        int i = 0;
        for (i = 0; i < (full * ALENGTH); i += ALENGTH) {
            System.arraycopy(ARRAY, 0, array, i, ALENGTH);
        }
        System.arraycopy(ARRAY, 0, array, i, part);
    }

}
