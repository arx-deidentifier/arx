/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.metric.v2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import com.carrotsearch.hppc.LongDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * This class represents a set of domain shares for an attribute. The shares are derived from a materialized
 * generalization hierarchy. It is assumed that the complete domain of the attribute is represented in
 * the hierarchy.
 * 
 * @author Fabian Prasser
 */
public class DomainShareMaterialized implements DomainShare {

    /** SVUID. */
    private static final long           serialVersionUID = -8981924690395236648L;

    /** The value representing a non-existent entry. */
    private static final double         NOT_AVAILABLE    = -Double.MAX_VALUE;

    /** The size of the domain. */
    private final double                size;

    /** One share per attribute. */
    private final double[]              shares;

    /** If an attribute exists with different shares on different generalization levels, store the share in this map: <code>(((long)value) << 32) | (level & 0xffffffffL) -> share </code>. */
    private transient LongDoubleOpenHashMap duplicates;

    /**
     * Creates a new set of domain shares derived from the given attribute.
     *
     * @param rawHierarchy
     * @param encodedValues
     * @param encodedHierarchy
     */
    public DomainShareMaterialized(String[][] rawHierarchy, 
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
     * Returns the size of the domain.
     *
     * @return
     */
    @Override
    public double getDomainSize() {
        return size;
    }

    /**
     * Returns the share of the given value.
     *
     * @param value
     * @param level
     * @return
     */
    @Override
    public double getShare(int value, int level) {
        double share = shares[value];
        if (share >= 0) {
            return share;
        } else {
            long key = (((long) value) << 32) | (level & 0xffffffffL);
            return duplicates.getOrDefault(key, -share);
        }
    }

    /**
     * De-serialization.
     *
     * @param aInputStream
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

        // Default de-serialization
        aInputStream.defaultReadObject();

        // Read map
        duplicates = IO.readLongDoubleOpenHashMap(aInputStream);
    }

    /**
     * Serialization.
     *
     * @param aOutputStream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

        // Default serialization
        aOutputStream.defaultWriteObject();
        
        // Write map
        IO.writeLongDoubleOpenHashMap(aOutputStream, duplicates);
    }

}
