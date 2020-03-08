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

import org.apache.commons.math3.fraction.BigFraction;

import com.carrotsearch.hppc.LongDoubleOpenHashMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;

/**
 * This class implements serialization for maps
 * 
 * @author Fabian Prasser
 */
public class IO {

    /**
     * Reads a LongDoubleOpenHashMap from the stream.
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
     * Reads a LongObjectOpenHashMap of the type BigFraction from the stream.
     *
     * @param stream
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static LongObjectOpenHashMap<BigFraction> readLongBigFractionOpenHashMap(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        
        // Read
        boolean[] allocated = (boolean[]) stream.readObject();
        long[] keys = (long[]) stream.readObject();
        Object[] values = (Object[]) stream.readObject();
        
        // Set
        LongObjectOpenHashMap<BigFraction> result = new LongObjectOpenHashMap<BigFraction>();
        for (int i=0; i<allocated.length; i++) {
            if (allocated[i]) {
                result.put(keys[i], (BigFraction)values[i]);
            }
        }
        
        // Return
        return result;
    }

    /**
     * Writes a LongDoubleOpenHashMap to the stream.
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
    
    /**
     * Writes a LongObjectOpenHashMap of the type BigFraction to the stream.
     *
     * @param stream
     * @param hashmap
     * @throws IOException
     */
    public static void writeLongBigFractionOpenHashMap(ObjectOutputStream stream, LongObjectOpenHashMap<BigFraction> hashmap) throws IOException {
        
        // Write
        stream.writeObject(hashmap.allocated);
        stream.writeObject(hashmap.keys);
        stream.writeObject(hashmap.values);
    }
}
