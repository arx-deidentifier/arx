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

package org.deidentifier.arx.metric.v2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.math3.fraction.BigFraction;

import com.carrotsearch.hppc.LongObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * This class represents a reliable set of domain shares for an attribute. The shares are derived from a materialized
 * generalization hierarchy. It is assumed that the complete domain of the attribute is represented in
 * the hierarchy.
 * 
 * @author Raffael Bild
 */
public class DomainShareReliable implements Serializable {

    /** SVUID. */
    private static final long                            serialVersionUID = -396317436976075163L;

    /** The value representing a non-existent entry. */
    private static final BigFraction                     NOT_AVAILABLE    = null;

    /** One share per attribute. */
    private final BigFraction[]                          shares;

    /** If an attribute exists with different shares on different generalization levels, store the share in this map: <code>(((long)value) << 32) | (level & 0xffffffffL) -> share </code>. */
    private transient LongObjectOpenHashMap<BigFraction> duplicates;

    /**
     * Creates a new set of domain shares derived from the given attribute.
     *
     * @param rawHierarchy
     * @param encodedValues
     * @param encodedHierarchy
     */
    public DomainShareReliable(String[][] rawHierarchy, 
                                   String[] encodedValues, 
                                   int[][] encodedHierarchy) {

        this.duplicates = new LongObjectOpenHashMap<BigFraction>();
        this.shares = new BigFraction[encodedValues.length];
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
                BigFraction share = new BigFraction(map.get(keyString), rawHierarchy.length);
                BigFraction stored = shares[value];

                // If duplicate
                if (stored != NOT_AVAILABLE) {

                    // If same share, simply continue
                    if (stored.equals(share)) {
                        continue;
                    }

                    // Mark as duplicate, if not already marked
                    if (stored.compareTo(BigFraction.ZERO) >= 0) {
                        shares[value] = shares[value].multiply(BigFraction.MINUS_ONE);
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
     * Clone constructor
     * @param shares
     * @param duplicates
     */
    private DomainShareReliable(BigFraction[] shares, LongObjectOpenHashMap<BigFraction> duplicates) {
        this.shares = shares;
        this.duplicates = duplicates;
    }

    @Override
    public DomainShareReliable clone() {
        return new DomainShareReliable(this.shares.clone(), this.duplicates.clone());
    }

    /**
     * Returns the share of the given value.
     *
     * @param value
     * @param level
     * @return
     */
    public BigFraction getShare(int value, int level) {
        BigFraction share = shares[value];
        if (share.compareTo(BigFraction.ZERO) >= 0) {
            return share;
        } else {
            long key = (((long) value) << 32) | (level & 0xffffffffL);
            return duplicates.getOrDefault(key, share.multiply(BigFraction.MINUS_ONE));
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
        duplicates = IO.readLongBigFractionOpenHashMap(aInputStream);
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
        IO.writeLongBigFractionOpenHashMap(aOutputStream, duplicates);
    }
}
