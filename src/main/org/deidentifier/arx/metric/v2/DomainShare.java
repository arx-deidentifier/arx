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

import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.LongDoubleOpenHashMap;

/**
 * This class represents a set of domain shares for an attribute
 * @author Fabian Prasser
 */
public class DomainShare implements Serializable {
    
    /** SVUID*/
    private static final long serialVersionUID = -8981924690395236648L;

    /** The size of the domain*/
    private final double size;

    /** One share per attribute*/
    private final double[] shares;
    
    /** Bit-vector indicating whether duplicates exist*/
    private final DomainShareVector vector;
    
    /** 
     * If an attribute exists with different shares on different generalization
     * levels, store the share in this map: 
     * <code>(((long)value) << 32) | (level & 0xffffffffL) -> share </code>
     */
    private final LongDoubleOpenHashMap duplicates;
    
    /**
     * Creates a new set of domain shares derived from the given attribute
     * @param hierarchy
     */
    public DomainShare(GeneralizationHierarchy hierarchy){
        
        // Prepare
        int[][] array = hierarchy.getArray();
        this.size = array.length;
        this.vector = new DomainShareVector(array.length);
        this.duplicates = new LongDoubleOpenHashMap();
        this.shares = new double[array.length];
        Arrays.fill(shares, -1d);
        IntIntOpenHashMap[] maps = new IntIntOpenHashMap[array[0].length];
        for (int level=0; level<maps.length; level++) {
            maps[level] = new IntIntOpenHashMap(hierarchy.getDistinctValues()[level]);
        }
        
        // First, compute the share for each generalization level
        for (int value=0; value<array.length; value++) {
            int[] transformation = array[value];
            for (int level=0; level<transformation.length; level++) {
                maps[level].putOrAdd(transformation[level], 1, 1);
            }
        }
        
        // Now transform into an array representation and handle duplicates
        for (int level=0; level<maps.length; level++) {
            IntIntOpenHashMap map = maps[level];
            boolean[] allocated = map.allocated;
            int[] keys = map.keys;
            int[] values = map.values;
            for (int index=0; index<allocated.length; index++){
                if (allocated[index]) {
                    int key = keys[index];
                    double share = (double)values[index] / size;
                    if (shares[key] != -1 && shares[key] != share) {
                        long dkey = (((long)key) << 32) | (level & 0xffffffffL);
                        duplicates.put(dkey, share);
                        vector.add(key);
                    } else {
                        shares[key] = share;
                    }
                }
            }
        }
    }
    
    /**
     * Returns the size of the domain
     * @return
     */
    public double getDomainSize(){
        return size;
    }
    
    /**
     * Returns the share of the given value
     * @param value
     * @param level
     * @return
     */
    public double getShare(int value, int level){
        if (!vector.contains(value)){
            return shares[value];
        } else {
            long key = (((long)value) << 32) | (level & 0xffffffffL);
            return duplicates.getOrDefault(key, shares[value]);
        }
    }
}
