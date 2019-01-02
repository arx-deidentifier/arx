/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.common;

/**
 * A very basic map using golden ratio hashing and linear probing.
 * 
 * @author Fabian Prasser
 */
public class FastIntDoubleMap {

    /** The entry array. */
    private final int[]    keys;

    /** The entry array. */
    private final double[] values;

    /** The mask */
    private final int      mask;

    /** The min */
    private int            min;

    /** The min */
    private int            max;
    
    /**
     * Creates a new instance
     * @param size
     */
    public FastIntDoubleMap(int size) {
        
        // Calculate capacity needed for a set of given size
        int capacity = (int) (Math.round((double) size * 1.25d)) - 1;
        capacity |= capacity >> 1;
        capacity |= capacity >> 2;
        capacity |= capacity >> 4;
        capacity |= capacity >> 8;
        capacity |= capacity >> 16;
        capacity++;
        
        // Prepare
        this.keys = new int[capacity];
        this.values = new double[capacity];
        this.mask = keys.length - 1;
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
    }
    
    /**
     * Puts a value into this map
     * @param key
     * @param value
     */
    public void put(int key, double value) {

        key++;
        this.min = Math.min(this.min, key);
        this.max = Math.max(this.max, key);
        int slot = hashcode(key) & mask;        
        while (keys[slot] != 0) {
            slot = (slot + 1) & mask;
        }
        keys[slot] = key;
        values[slot] = value;
    }

    /**
     * Returns the associated value, default if not found
     * @param key
     * @param _default
     * @return
     */
    public double get(int key, double _default) {

        key++;
        if (key < min || key > max) {
            return _default;
        }
        int slot = hashcode(key) & mask;        
        while (keys[slot] != 0) {
            if (keys[slot] == key) {
                return values[slot];
            }
            slot = (slot + 1) & mask;
        }
        return _default;
    }
    
    /**
     * Golden ratio hash
     * @param value
     * @return
     */
    private int hashcode(int value) {
        value = value * 0x9E3779B9;
        return (value ^ (value >> 16));
    }
}