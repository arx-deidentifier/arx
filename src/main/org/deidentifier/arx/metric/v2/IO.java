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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.LongDoubleOpenHashMap;

/**
 * 
 */
public class IO {

    /**
     * Reads a hash map from the stream.
     *
     * @param stream
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static IntIntOpenHashMap readIntIntOpenHashMap(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        
        // Read
        boolean[] allocated = (boolean[]) stream.readObject();
        int[] keys = (int[]) stream.readObject();
        int[] values = (int[]) stream.readObject();
        
        // Set
        IntIntOpenHashMap result = new IntIntOpenHashMap();
        for (int i=0; i<allocated.length; i++) {
            if (allocated[i]) {
                result.put(keys[i], values[i]);
            }
        }
        
        // Return
        return result;
    }
    

    /**
     * Reads a hash map from the stream.
     *
     * @param stream
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static LongDoubleOpenHashMap readLongDoubleOpenHashMap(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        
        // Read
        boolean[] allocated = (boolean[]) stream.readObject();
        long[] keys = (long[]) stream.readObject();
        double[] values = (double[]) stream.readObject();
        
        // Set
        LongDoubleOpenHashMap result = new LongDoubleOpenHashMap();
        for (int i=0; i<allocated.length; i++) {
            if (allocated[i]) {
                result.put(keys[i], values[i]);
            }
        }
        
        // Return
        return result;
    }

    /**
     * Reads a hash map from the stream.
     *
     * @param stream
     * @param hashmap
     * @throws IOException
     */
    public static void writeIntIntOpenHashMap(ObjectOutputStream stream, IntIntOpenHashMap hashmap) throws IOException {
        
        // Write
        stream.writeObject(hashmap.allocated);
        stream.writeObject(hashmap.keys);
        stream.writeObject(hashmap.values);
    }
    

    /**
     * Reads a hash map from the stream.
     *
     * @param stream
     * @param hashmap
     * @throws IOException
     */
    public static void writeLongDoubleOpenHashMap(ObjectOutputStream stream, LongDoubleOpenHashMap hashmap) throws IOException {
        
        // Write
        stream.writeObject(hashmap.allocated);
        stream.writeObject(hashmap.keys);
        stream.writeObject(hashmap.values);
    }
}
