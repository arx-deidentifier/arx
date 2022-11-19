/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

package org.deidentifier.arx.distributed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;

import cern.colt.Arrays;

/**
 * Class providing operations for partitions
 * @author Fabian Prasser
 *
 */
public class ARXPartition {

    /** Random */
    private static final Random random = new Random(0xDEADBEEF);

    /**
     * Converts handle to data
     * @param handle
     * @return
     */
    public static Data getData(DataHandle handle) {
        // TODO: Ugly that this is needed, because it is costly
        Data data = Data.create(handle.iterator());
        data.getDefinition().read(handle.getDefinition());
        return data;
    }

    /**
     * Partitions the dataset making sure that records from one equivalence
     * class are assigned to exactly one partition. Will also remove all hierarchies.
     * @param data
     * @param number
     * @return
     */
    public static List<DataHandle> getPartitionsByClass(Data data, int number) {

        // Prepare
        List<DataHandle> result = new ArrayList<>();
        DataDefinition definition = data.getDefinition().clone();
        DataHandle handle = data.getHandle();
        Set<String> qi = handle.getDefinition().getQuasiIdentifyingAttributes();
        for (String attribute : qi) {
            definition.resetHierarchy(attribute);
        }
        
        // Collect indices to use for sorting
        int[] indices = new int[qi.size()];
        int num = 0;
        for (int column = 0; column < handle.getNumColumns(); column++) {
            if (qi.contains(handle.getAttributeName(column))) {
                indices[num++] = column;
            }
        }
        
        // Sort
        handle.sort(true, indices);
        
        // Split
        Iterator<String[]> iter = handle.iterator();
        String[] header = iter.next();
        String[] current = iter.next();
        String[] next = iter.next();
        int size = (int)Math.floor((double)handle.getNumRows() / (double)number);
        for (int i = 0; i < number && current != null; i++) {
            
            // Build this partition
            List<String[]> _list = new ArrayList<>();
            _list.add(header);
            
            // Loop while too small or in same equivalence class
            while (current != null && (_list.size() < size + 1 || equals(current, next, indices))) {
                
                // Add
                _list.add(current);
                
                // Proceed
                current = next;
                next = iter.hasNext() ? iter.next() : null;
            }
            
            // Add to partitions
            Data _data = Data.create(_list);
            _data.getDefinition().read(definition.clone());
            result.add(_data.getHandle());
        }
        
        // Done
        return result;
    }

    /**
     * Partitions the dataset randomly
     * @param data
     * @param number
     * @return
     */
    public static List<DataHandle> getPartitionsRandom(Data data, int number) {
        
        // Copy definition
        DataDefinition definition = data.getDefinition();
        
        // Randomly partition
        DataHandle handle = data.getHandle();
        Iterator<String[]> iter = handle.iterator();
        String[] header = iter.next();

        // Lists
        List<List<String[]>> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            List<String[]> _list = new ArrayList<>();
            _list.add(header);
            list.add(_list);
        }
        
        // Distributed records
        while (iter.hasNext()) {
            list.get(random.nextInt(number)).add(iter.next());
        }
        
        // Convert to data
        List<DataHandle> result = new ArrayList<>();
        for (List<String[]> partition : list) {
            Data _data = Data.create(partition);
            _data.getDefinition().read(definition.clone());
            result.add(_data.getHandle());
        }
        
        // Done
        return result;
    }
    
    /**
     * Partitions the dataset using ordering
     * @param data
     * @param number
     * @return
     */
    public static List<DataHandle> getPartitionsSorted(Data data, int number) {

        // Copy definition
        DataDefinition definition = data.getDefinition();
        
        // Prepare
        List<DataHandle> result = new ArrayList<>();
        DataHandle handle = data.getHandle();

        // Collect indices to use for sorting
        Set<String> qi = handle.getDefinition().getQuasiIdentifyingAttributes();
        int[] indices = new int[qi.size()];
        int num = 0;
        for (int column = 0; column < handle.getNumColumns(); column++) {
            if (qi.contains(handle.getAttributeName(column))) {
                indices[num++] = column;
            }
        }
        
        // Sort
        handle.sort(true, indices);
        
        // Convert
        List<String[]> rows = new ArrayList<>();
        Iterator<String[]> iter = handle.iterator();
        String[] header = iter.next();
        while (iter.hasNext()) {
            rows.add(iter.next());
        }
        
        // Split
        // TODO: Check for correctness
        double size = (double)handle.getNumRows() / (double)number;
        double start = 0d;
        double end = size;
        for (int i = 0; i < number; i++) {
            List<String[]> _list = new ArrayList<>();
            _list.add(header);
            _list.addAll(rows.subList((int)Math.round(start), (int)Math.round(end)));
            Data _data = Data.create(_list);
            _data.getDefinition().read(definition.clone());
            result.add(_data.getHandle());
            start = end;
            end = end + size;
        }
        
        // Done
        return result;
    }

    /**
     * Checks equality of strings regarding the given indices
     * @param array1
     * @param array2
     * @param indices
     * @return
     */
    private static boolean equals(String[] array1, String[] array2, int[] indices) {
        if (array1 == null) {
            return (array2 == null);
        }
        if (array2 == null) {
            return (array1 == null);
        }
        for (int index : indices) {
            if (!array1[index].equals(array2[index])) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Print
     * @param handle
     */
    public static void print(DataHandle handle) {
        Iterator<String[]> iterator = handle.iterator();
        System.out.println(Arrays.toString(iterator.next()));
        System.out.println(Arrays.toString(iterator.next()));
        System.out.println("- Records: " + handle.getNumRows());
    }
}
