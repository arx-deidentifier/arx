/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2023 Fabian Prasser and contributors
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
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies mostly for categorical variables
 * by iteratively removing the value with lowest priority
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyBuilderPriorityBased<T> extends HierarchyBuilder<T> implements Serializable {
    
    /**
     * For priorities
     * @author Fabian Prasser
     */
    public static enum Priority {
        /** Highest to lowest*/
        HIGHEST_TO_LOWEST,
        /** Lowest to highest*/
        LOWEST_TO_HIGHEST
    }
    
    /**  SVUID */
    private static final long serialVersionUID = 4823242603368546852L;

    /**
     * Create a new instance, prioritized by the order implied by the data type in
     * order highest to lowest and a maximum of 10 levels.
     *
     * @param dataType
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(DataType<T> type){
        return create(type, Priority.HIGHEST_TO_LOWEST);
    }
    
    /**
     * Create a new instance, prioritized by the order implied by the data type
     * and a maximum of 10 levels.
     *
     * @param dataType
     * @param priority
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(DataType<T> type, Priority priority){
        return create(type, priority, 10);
    }
    
    /**
     * Create a new instance, prioritized by the order implied by the data type.
     *
     * @param maxLevels
     * @param dataType
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(DataType<T> type, Priority priority, int maxLevels){
        return new HierarchyBuilderPriorityBased<T>(type, priority, maxLevels);
    }
    
    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> HierarchyBuilderPriorityBased<T> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilderPriorityBased<T> result = (HierarchyBuilderPriorityBased<T>)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }
    

    /**
     * Create a new instance, prioritized by the order provided in the given array
     * (highest to lowest) with a maximum of 10 levels.
     *
     * @param priorities
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(Map<String, Integer> priorities){
        return create(priorities, Priority.HIGHEST_TO_LOWEST, 10);
    }

    /**
     * Create a new instance, prioritized by the order provided in the given array
     * with the given order with a maximum of 10 levels.
     *
     * @param priorities
     * @param priority
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(Map<String, Integer> priorities, Priority priority){
        return create(priorities, priority, 10);
    }

    /**
     * Create a new instance, prioritized by the order provided in the given array
     * (highest to lowest)
     *
     * @param priorities
     * @param priority
     * @param maxLevels
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(Map<String, Integer> priorities, Priority priority, int maxLevels){
        return new HierarchyBuilderPriorityBased<T>(priorities, priority, maxLevels);
    }
    
    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(String file) throws IOException{
        return create(new File(file));
    }
    
    
    /** Result */
    private transient String[][] result;

    /** Max levels */
    private int                  maxLevels  = 10;

    /** Priority */
    private Priority             priority   = null;

    /** Type */
    private DataType<T>          type       = null;

    /** Priorities */
    private Map<String, Integer> priorities = null;
    
    /**
     * Creates a new instance
     * @param type
     * @param priority
     * @param maxLevels
     */
    private HierarchyBuilderPriorityBased(DataType<T> type, Priority priority, int maxLevels) {
        super(Type.PRIORITY_BASED);
        this.maxLevels = maxLevels;
        this.type = type;
        this.priority = priority;
        this.priorities = null;
    }
    
    /**
     * Creates a new instance
     * @param priorities
     * @param priority
     * @param maxLevels
     */
    private HierarchyBuilderPriorityBased(Map<String, Integer> priorities, Priority priority, int maxLevels) {
        super(Type.PRIORITY_BASED);
        this.maxLevels = maxLevels;
        this.priorities = priorities;
        this.priority = priority;
        this.type = null;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @return
     */
    public Hierarchy build(){
        
        // Check
        if (result == null) {
            throw new IllegalArgumentException("Please call prepare() first");
        }
        
        // Return
        Hierarchy h = Hierarchy.create(result);
        this.result = null;
        return h;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @param data - Prioritized from highest to lowest
     * @return
     */
    public Hierarchy build(String[] data){
        prepare(data);
        return build();
    }
    
    /**
     * @return the type
     */
    public DataType<T> getDataType() {
        return type;
    }

    /**
     * Gets the maximal number of levels
     * @return the maxLevels
     */
    public int getMaxLevels() {
        return maxLevels;
    }
    
    /**
     * @return the priorities
     */
    public Map<String, Integer> getPriorities() {
        return priorities;
    }
    
    /**
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }
    
    /**
     * Returns whether domain-properties are available for this builder. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies.
     * @return
     */
    public boolean isDomainPropertiesAvailable() {
        return false;
    }

    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     *
     * @param data In prioritized order, with highest priority to lowest priority
     * @return
     */
    public int[] prepare(String[] data){
        
        // Check
        prepareResult(data);
        
        // Compute
        int[] sizes = new int[this.result[0].length];
        for (int i=0; i < sizes.length; i++){
            Set<String> set = new HashSet<String>();
            for (int j=0; j<this.result.length; j++) {
                set.add(result[j][i]);
            }
            sizes[i] = set.size();
        }
        
        // Return
        return sizes;
    }

    /**
     * Sets the maximal number of levels
     * @param maxLevels the maxLevels to set
     */
    public void setMaxLevels(int maxLevels) {
        this.maxLevels = maxLevels;
    }
    
    /**
     * Computes the hierarchy.
     *
     * @param data
     */
    private void prepareResult(String[] data){
        
        String[] values = null;
        
        // Based on data type: prepare sorted list
        if (priority != null && type != null) {
            values = data.clone();
            Arrays.sort(values, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    try {
                        return (priority == Priority.HIGHEST_TO_LOWEST) ? -type.compare(o1, o2) : type.compare(o1, o2);
                    } catch (NumberFormatException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        // Based on provided priorities
        } else {
            
            // Create ordered values for this case
            List<String> order = new ArrayList<String>();
            
            // Create sorted list of priorities
            String[] priorities = data.clone();
            Arrays.sort(priorities, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int prio1 = HierarchyBuilderPriorityBased.this.priorities.getOrDefault(o1, 0);
                    int prio2 = HierarchyBuilderPriorityBased.this.priorities.getOrDefault(o2, 0);
                    return (priority == Priority.HIGHEST_TO_LOWEST) ? -Integer.compare(prio1, prio2) : Integer.compare(prio1, prio2); 
                }
            });
            
            // Prepare sets
            Set<String> setOfPriorities = new HashSet<String>();
            setOfPriorities.addAll(Arrays.asList(priorities));
            Set<String> setOfData = new HashSet<String>();
            setOfData.addAll(Arrays.asList(data));
            
            // Add elements
            for (String element : priorities) {
                if (setOfData.contains(element)) {
                    order.add(element);
                }
            }
            
            // Perform check
            if (!setOfPriorities.containsAll(setOfData)) {
                throw new IllegalArgumentException("Data contains elements that haven't been prioritized");
            }
            
            // Done
            values = order.toArray(new String[order.size()]);
        }
        
        // Prepare
        int levels = Math.min(values.length, maxLevels);
        double offset = (double)values.length / (double)levels;
        
        // Result
        this.result = new String[values.length][];
        
        // Prepare arrays for levels
        for (int i=0; i < result.length; i++) {
            this.result[i] = new String[levels + 1];
        }
        
        // For each level
        for (int level = 0; level <= levels; level ++) {
            for (int i = 0; i < values.length ; i++) {
                // Value level
                int valueLevel = levels - (int)Math.floor((double)(i) / offset);
                // Value
                this.result[i][level] = (valueLevel <= level ? DataType.ANY_VALUE : values[i]);
            }
        }
    }
}