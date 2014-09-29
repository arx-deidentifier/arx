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

package org.deidentifier.arx.metric.v2;

import java.io.Serializable;
import java.util.Arrays;

import com.carrotsearch.hppc.LongDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * This class represents a set of domain shares for an attribute
 * @author Fabian Prasser
 */
public class DomainShare implements Serializable {

    /** SVUID */
    private static final long           serialVersionUID = -8981924690395236648L;

    /** The value representing a non-existent entry */
    private static final double         NOT_AVAILABLE    = -Double.MAX_VALUE;

    /** The size of the domain */
    private final double                size;

    /** One share per attribute */
    private final double[]              shares;

    /**
     * If an attribute exists with different shares on different generalization
     * levels, store the share in this map: <code>(((long)value) << 32) | (level & 0xffffffffL) -> share </code>
     */
    private final LongDoubleOpenHashMap duplicates;

    /**
     * Creates a new set of domain shares derived from the given attribute
     * @param rawHierarchy
     * @param encodedValues 
     * @param encodedHierarchy 
     */
    public DomainShare(String[][] rawHierarchy, 
                       String[] encodedValues, 
                       int[][] encodedHierarchy) {

        this.size = rawHierarchy.length;
        this.duplicates = new LongDoubleOpenHashMap();
        this.shares = new double[encodedValues.length];
        Arrays.fill(shares, NOT_AVAILABLE);
        @SuppressWarnings("unchecked")
        ObjectIntOpenHashMap<String>[] maps = new ObjectIntOpenHashMap[rawHierarchy[0].length];
        for (int level = 0; level < maps.length; level++) {
            maps[level] = new ObjectIntOpenHashMap<String>();
        }

        // First, compute the share for each generalization strategy
        for (int value = 0; value < rawHierarchy.length; value++) {
            String[] transformation = rawHierarchy[value];
            for (int level = 0; level < transformation.length; level++) {
                ObjectIntOpenHashMap<String> map = maps[level];
                String key = transformation[level];
                if (!map.containsKey(key)) {
                    map.put(key, 0);
                }
                map.put(key, map.get(key) + 1);
            }
        }

        // Now transform into an array representation and handle duplicates
        for (int row = 0; row < encodedHierarchy.length; row++) {
            
            int[] strategy = encodedHierarchy[row];
            
            for (int level = 0; level < strategy.length; level++){
                
                ObjectIntOpenHashMap<String> map = maps[level];
                int value = strategy[level];
                String keyString = encodedValues[value];
                double share = (double) map.get(keyString) / size;
                double stored = shares[value];

                // If duplicate
                if (stored != NOT_AVAILABLE) {

                    // If same share, simply continue
                    if (stored == share) {
                        continue;
                    }

                    // Mark as duplicate, if not already marked
                    if (stored >= 0d) {
                        shares[value] = -shares[value];
                    }

                    // Store duplicate value
                    long dkey = (((long) value) << 32) | (level & 0xffffffffL);
                    duplicates.put(dkey, share);

                    // If its not a duplicate, simply store
                } else {
                    shares[value] = share;
                }
            }
        }
    }

    /**
     * Returns the size of the domain
     * @return
     */
    public double getDomainSize() {
        return size;
    }

    /**
     * Returns the share of the given value
     * @param value
     * @param level
     * @return
     */
    public double getShare(int value, int level) {
        double share = shares[value];
        if (share >= 0) {
            return share;
        } else {
            long key = (((long) value) << 32) | (level & 0xffffffffL);
            return duplicates.getOrDefault(key, -share);
        }
    }
}
