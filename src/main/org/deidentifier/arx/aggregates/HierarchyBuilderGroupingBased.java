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
package org.deidentifier.arx.aggregates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This abstract base class enables building hierarchies for categorical and non-categorical values.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class HierarchyBuilderGroupingBased<T> extends HierarchyBuilder<T> implements Serializable {

    /**
     * This class represents a fanout parameter.
     *
     * @author Fabian Prasser
     * @param <U>
     */
    public static class Group<U> implements Serializable {
        
        /**  TODO */
        private static final long serialVersionUID = -5767501048737045793L;
        
        /** Fanout. */
        private final int size;
        
        /** Aggregate function. */
        private final AggregateFunction<U> function;
        
        /**
         * Creates a new instance.
         *
         * @param size
         * @param function
         */
        private Group(int size, AggregateFunction<U> function) {
            if (size<=0) {
                throw new IllegalArgumentException("Size must be >= 0");
            }
            if (function==null) {
                throw new IllegalArgumentException("Function must not be null");
            }
            this.size = size;
            this.function = function;
        }

        /**
         * @return the function
         */
        public AggregateFunction<U> getFunction() {
            return function;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString(){
            return "Group[length="+size+", function="+function.toString()+"]";
        }
    }
    
    /**
     * This class represents a level in the hierarchy.
     *
     * @author Fabian Prasser
     * @param <U>
     */
    public static class Level<U> implements Serializable{
        
        /**  TODO */
        private static final long serialVersionUID = 1410005675926162598L;
        
        /** Level. */
        private final int level;
        
        /** List of groups. */
        private final List<Group<U>> list = new ArrayList<Group<U>>();
        
        /** Builder. */
        private final HierarchyBuilderGroupingBased<U> builder;
        
        /**
         * Creates a new instance.
         *
         * @param builder
         * @param level
         */
        private Level(HierarchyBuilderGroupingBased<U> builder, int level) {
            this.level = level;
            this.builder = builder;
        }
        
        /**
         * Adds the given group with the default aggregate function.
         *
         * @param size
         * @return
         */
        public Level<U> addGroup(int size) {
            if (builder.getDefaultFunction() == null) {
                throw new IllegalStateException("No default aggregate function defined");
            }
            this.list.add(new Group<U>(size, builder.getDefaultFunction()));
            builder.setPrepared(false);
            return this;
        }

        /**
         * Adds the given group with the given aggregate function.
         *
         * @param size
         * @param function
         * @return
         */
        public Level<U> addGroup(int size, AggregateFunction<U> function) {
            this.list.add(new Group<U>(size, function));
            builder.setPrepared(false);
            return this;
        }

        /**
         * Adds the given group. The result will be labeled with the given string
         * @param size
         * @param label
         * @return
         */
        public Level<U> addGroup(int size, String label) {
            this.list.add(new Group<U>(size, AggregateFunction.forType(builder.getDataType()).createConstantFunction(label)));
            builder.setPrepared(false);
            return this;
        }

        /**
         * Removes all groups on this level.
         *
         * @return
         */
        public Level<U> clearGroups() {
            this.list.clear();
            builder.setPrepared(false);
            return this;
        }
        
        /**
         * Returns the list.
         *
         * @return
         */
        @SuppressWarnings("unchecked")
        public List<Group<U>> getGroups(){
            return (List<Group<U>>)((ArrayList<Group<U>>)this.list).clone();
        }

        /**
         * @return the level
         */
        public int getLevel() {
            return level;
        }
     
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString(){
            StringBuilder b = new StringBuilder();
            b.append("Level[height="+level+"]\n");
            for (int i=0, length=list.size(); i<length; i++){
                Group<U> fanout = list.get(i);
                b.append("   ").append(fanout.toString());
                if (i<length-1) b.append("\n");
            }
            return b.toString();
        }
    }
    
    /**
     * A group representation to be used by subclasses.
     *
     * @author Fabian Prasser
     */
    protected abstract static class AbstractGroup implements Serializable {
        
        /**  TODO */
        private static final long serialVersionUID = -7657969446040078411L;
        
        /**  TODO */
        private String label;
        
        /**
         * 
         *
         * @param label
         */
        protected AbstractGroup(String label){
            this.label = label;
        }
        
        /**
         * 
         *
         * @return
         */
        protected String getLabel(){
            return label;
        }
    }
    
    /**  TODO */
    private static final long serialVersionUID = 3208791665131141362L;
    
    /** The data array. */
    private transient String[] data;
    
    /** All fanouts for each level. */
    private Map<Integer, Level<T>> groups = new HashMap<Integer, Level<T>>();
    
    /** The groups on the first level. */
    private transient AbstractGroup[][] abstractGroups;
    
    /** Are we ready to go. */
    private transient boolean prepared = false;
    
    /** The data type. */
    private DataType<T> datatype;
    
    /** The default aggregate function, might be null. */
    protected AggregateFunction<T> function;

    /**
     * Creates a new instance for the given data type.
     *
     * @param type
     * @param datatype
     */
    protected HierarchyBuilderGroupingBased(Type type, DataType<T> datatype){
        super(type);
        this.datatype = datatype;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @return
     */
    public Hierarchy build(){
        
        if (!prepared) {
            throw new IllegalStateException("Please call prepare() first");
        }

        // Add input data
        String[][] result = new String[data.length][abstractGroups.length + 1];
        for (int i=0; i<result.length; i++) {
            result[i] = new String[abstractGroups.length + 1];
            result[i][0] = data[i];
        }
        
        // Add levels
        for (int i=0; i<result[0].length - 1; i++){
            Map<String, Map<AbstractGroup, String>> multiplicities = new HashMap<String, Map<AbstractGroup, String>>();
            for (int j=0; j<result.length; j++){
                result[j][i + 1] = getLabel(multiplicities, abstractGroups[i][j]);
            }
        }
        
        
        Hierarchy h = Hierarchy.create(result);
        
        this.prepared = false;
        this.data = null;
        this.abstractGroups = null;
        return h;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @param data
     * @return
     */
    public Hierarchy build(String[] data){
        prepare(data);
        return build();
    }
    
    /**
     * Returns the data type.
     *
     * @return
     */
    public DataType<T> getDataType(){
        return this.datatype;
    }

    /**
     * Returns the default aggregate function.
     *
     * @return
     */
    public AggregateFunction<T> getDefaultFunction(){
        return this.function;
    }

    /**
     * Returns the given level.
     *
     * @param level
     * @return
     */
    public Level<T> getLevel(int level){
        if (!this.groups.containsKey(level)) {
            this.groups.put(level, new Level<T>(this, level));
            this.setPrepared(false);
        }
        return this.groups.get(level);
    }
    
    /**
     * Returns all currently defined levels.
     *
     * @return
     */
    public List<Level<T>> getLevels(){
        List<Level<T>> levels = new ArrayList<Level<T>>();
        levels.addAll(this.groups.values());
        Collections.sort(levels, new Comparator<Level<T>>(){
            @Override
            public int compare(Level<T> o1,
                               Level<T> o2) {
                return new Integer(o1.getLevel()).compareTo(new Integer(o2.getLevel()));
            }
        });
        return levels;
    }
    
    /**
     * Returns whether the current configuration is valid. Returns <code>null</code>, if so, an error message
     * if not.
     * @return
     */
    public String isValid() {
        
        // Check fanouts
        int max = 0;
        for (Entry<Integer, Level<T>> level : this.groups.entrySet()) {
            if (level.getValue().getGroups().isEmpty()) {
                if (level.getKey() < this.groups.size()-1) {
                    return "No group specified on level "+level.getKey();
                }
            }
            max = Math.max(level.getKey(), max);
        }
        for (int i=0; i<max; i++){
            if (!this.groups.containsKey(i)) {
                return "Missing specification for level "+i;
            } else if (this.groups.get(i).getGroups().isEmpty()) {
                return "Missing specification for level "+i;
            }
        }
        
        return null;
    }
    
    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     *
     * @param data
     * @return
     */
    public int[] prepare(String[] data){
        this.data = data;
        String error = this.isValid();
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        this.abstractGroups = prepareGroups();
        this.prepared = true;
        
        // TODO: This assumes that input data does not contain duplicates
        int[] result = new int[this.abstractGroups.length + 1];
        result[0] = data.length; 
        for (int i=0; i<result.length - 1; i++){
            Set<AbstractGroup> set = new HashSet<AbstractGroup>();
            for (int j=0; j<this.abstractGroups[i].length; j++){
                set.add(abstractGroups[i][j]);
            }
            result[i + 1] = set.size();
        }
        return result;
    }
    
    /**
     * Sets the default aggregate function to be used by all fanouts.
     *
     * @param function
     */
    public void setAggregateFunction(AggregateFunction<T> function){
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        this.function = function;
    }
    
    /**
     * Returns the label for a given group. Makes sure that no labels are returned twice
     * @param multiplicities
     * @param group
     * @return
     */
    private String getLabel(Map<String, Map<AbstractGroup, String>> multiplicities, AbstractGroup group) {
        String label = group.getLabel();
        Map<AbstractGroup, String> map = multiplicities.get(label);
        if (map == null) {
            map = new HashMap<AbstractGroup, String>();
            map.put(group, label);
            multiplicities.put(label, map);
            return label;
        } else {
            String storedLabel = map.get(group);
            if (storedLabel != null) {
                return storedLabel;
            } else {
                label +="-"+map.size();
                map.put(group, label);
                return label;
            }
        }
    }
    
    /**
     * Returns the data array.
     *
     * @return
     */
    protected String[] getData(){
        return data;
    }
    
    /**
     * Returns the prepared groups for recursion.
     *
     * @return
     */
    protected AbstractGroup[][] getPreparedGroups(){
        return this.abstractGroups;
    }
    
    /**
     * Tells the implementing class to prepare the generalization process.
     *
     * @return
     */
    protected abstract AbstractGroup[][] prepareGroups();
    
    /**
     * Sets the data array.
     *
     * @param data
     */
    protected void setData(String[] data){
        this.data = data;
    }
    
    /**
     * Sets the groups on higher levels of the hierarchy.
     *
     * @param levels
     */
    protected void setLevels(List<Level<T>> levels) {
        for (Level<T> level : levels) {
            this.groups.put(level.getLevel(), level);
        }
    }

    /**
     * Is this builder prepared allready.
     *
     * @param prepared
     */
    protected void setPrepared(boolean prepared){
        this.prepared = prepared;
        if (prepared == false) {
            this.abstractGroups = null;
        }
    }
}
