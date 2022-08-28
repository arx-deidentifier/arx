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
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
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
    
    /**  SVUID */
    private static final long serialVersionUID = 4823242603368546852L;
    
    /**
     * Create a new instance
     *
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(){
        return new HierarchyBuilderPriorityBased<T>();
    }

    /**
     * Create a new instance
     *
     * @param dataType
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(DataType<T> type){
        return new HierarchyBuilderPriorityBased<T>();
    }

    /**
     * Create a new instance
     *
     * @param maxLevels
     * @param dataType
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(DataType<T> type, int maxLevels){
        return new HierarchyBuilderPriorityBased<T>(maxLevels);
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
     * Create a new instance
     *
     * @param maxLevels
     * @param <T>
     * @return
     */
    public static <T> HierarchyBuilderPriorityBased<T> create(int maxLevels){
        return new HierarchyBuilderPriorityBased<T>(maxLevels);
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
    
    /** Result. */
    private transient String[][] result;

    /** Max levels*/
    private int maxLevels = 10;
    
    /**
     * Creates a new priority-based hierarchy builder
     */
    private HierarchyBuilderPriorityBased(){
        super(Type.PRIORITY_BASED);
        this.maxLevels = 10;
    }

    /**
     * Creates a new priority-based hierarchy builder
     * 
     * @param maxLevels
     */
    private HierarchyBuilderPriorityBased(int maxLevels){
        super(Type.PRIORITY_BASED);
        this.maxLevels = maxLevels;
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
     * Gets the maximal number of levels
     * @return the maxLevels
     */
    public int getMaxLevels() {
        return maxLevels;
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
        
        // Prepare
        int levels = Math.min(data.length, maxLevels);
        double offset = (double)data.length / (double)levels;
        
        // Result
        this.result = new String[data.length][];
        
        // Prepare arrays for levels
        for (int i=0; i < result.length; i++) {
            this.result[i] = new String[levels + 1];
        }
        
        // For each level
        for (int level = 0; level <= levels; level ++) {
            for (int i = 0; i < data.length ; i++) {
                // Value level
                int valueLevel = levels - (int)Math.floor((double)(i) / offset);
                // Value
                this.result[i][level] = (valueLevel <= level ? DataType.ANY_VALUE : data[i]);
            }
        }
    }
}