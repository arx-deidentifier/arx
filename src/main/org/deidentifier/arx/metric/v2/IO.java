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
