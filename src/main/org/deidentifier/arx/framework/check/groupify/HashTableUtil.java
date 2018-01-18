/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        if (x >= (1 << 30)) {
            return 1 << 30;
        }
        if (x == 0) {
            return 16;
        }
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
        
        // Make sure that this method can be inlined by keeping
        // its size under 325 bytes
        switch (a.length) {
        case 17:
            if (a[16] != a2[16]) {
                return false;
            }
        case 16:
            if (a[15] != a2[15]) {
                return false;
            }
        case 15:
            if (a[14] != a2[14]) {
                return false;
            }
        case 14:
            if (a[13] != a2[13]) {
                return false;
            }
        case 13:
            if (a[12] != a2[12]) {
                return false;
            }
        case 12:
            if (a[11] != a2[11]) {
                return false;
            }
        case 11:
            if (a[10] != a2[10]) {
                return false;
            }
        case 10:
            if (a[9] != a2[9]) {
                return false;
            }
        case 9:
            if (a[8] != a2[8]) {
                return false;
            }
        case 8:
            if (a[7] != a2[7]) {
                return false;
            }
        case 7:
            if (a[6] != a2[6]) {
                return false;
            }
        case 6:
            if (a[5] != a2[5]) {
                return false;
            }
        case 5:
            if (a[4] != a2[4]) {
                return false;
            }
        case 4:
            if (a[3] != a2[3]) {
                return false;
            }
        case 3:
            if (a[2] != a2[2]) {
                return false;
            }
        case 2:
            if (a[1] != a2[1]) {
                return false;
            }
        case 1:
            if (a[0] != a2[0]) {
                return false;
            }
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
     * Computes a hashcode for an integer array, partially unrolled.
     * 
     * @param array
     * @return the hashcode
     */
    public static final int hashcode(final int[] array) {
        final int len = array.length;
        int result = 23;
        int i = 0;
        // Do blocks of four ints unrolled.
        for (; (i + 3) < len; i += 4) {
            result = (1874161 * result) + // 37 * 37 * 37 * 37 
                     (50653 * array[i]) + // 37 * 37 * 37
                     (1369 * array[i + 1]) + // 37 * 37
                     (37 * array[i + 2]) +
                     array[i + 3];
        }
        // Do the rest
        for (; i < len; i++) {
            result = (37 * result) + array[i];
        }
        return result;
    }
    
    /**
     * Computes a hashcode for an integer array.
     * 
     * @param array
     *            the array
     * @return the hashcode
     */
    public static final int hashcode_old(final int[] array) {
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
