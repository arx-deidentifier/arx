/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.check.groupify;

/**
 * This class implements several helper methods for hash tables.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
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
     *
     * @param buckets
     * @param loadFactor
     * @return
     */
    public static final int calculateThreshold(final int buckets,
                                               final float loadFactor) {
        return (int) (buckets * loadFactor);
    }

    /**
     * Equality check for integer arrays.
     * 
     * @param a an array
     * @param a2 another array
     * @return true, if equal
     */
    public static final boolean equals(final int[] a, final int[] a2) {
        switch (a.length) {
        case 20:
            if (a[19] != a2[19]) return false;
        case 19:
            if (a[18] != a2[18]) return false;
        case 18:
            if (a[17] != a2[17]) return false;
        case 17:
            if (a[16] != a2[16]) return false;
        case 16:
            if (a[15] != a2[15]) return false;
        case 15:
            if (a[14] != a2[14]) return false;
        case 14:
            if (a[13] != a2[13]) return false;
        case 13:
            if (a[12] != a2[12]) return false;
        case 12:
            if (a[11] != a2[11]) return false;
        case 11:
            if (a[10] != a2[10]) return false;
        case 10:
            if (a[9] != a2[9]) return false;
        case 9:
            if (a[8] != a2[8]) return false;
        case 8:
            if (a[7] != a2[7]) return false;
        case 7:
            if (a[6] != a2[6]) return false;
        case 6:
            if (a[5] != a2[5]) return false;
        case 5:
            if (a[4] != a2[4]) return false;
        case 4:
            if (a[3] != a2[3]) return false;
        case 3:
            if (a[2] != a2[2]) return false;
        case 2:
            if (a[1] != a2[1]) return false;
        case 1:
            if (a[0] != a2[0]) return false;
            break;
        default:
            for (int i = 0; i < a.length; i++) {
                if (a[i] != a2[i]) {
                    return false;
                }
            }
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
