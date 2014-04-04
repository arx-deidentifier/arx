/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
package org.deidentifier.arx.aggregates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This abstract base class enables building hierarchies for categorical and non-categorical values
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class HierarchyBuilderGroupingBased<T> implements Serializable, HierarchyBuilder {

    /**
     * This class represents a fanout parameter
     * @author Fabian Prasser
     */
    public static class Fanout<T> implements Serializable {
        
        private static final long serialVersionUID = -5767501048737045793L;
        
        private int fanout;
        private AggregateFunction<T> function;
        
        public Fanout(int fanout, AggregateFunction<T> function) {
            if (fanout<=0) {
                throw new IllegalArgumentException("Fanout must be >= 0");
            }
            if (function==null) {
                throw new IllegalArgumentException("Function must not be null");
            }
            this.fanout = fanout;
            this.function = function;
        }

        /**
         * @return the fanout
         */
        public int getFanout() {
            return fanout;
        }

        /**
         * @return the function
         */
        public AggregateFunction<T> getFunction() {
            return function;
        }
    }
    /**
     * This class represents a level in the hierarchy
     * @author Fabian Prasser
     */
    public class HierarchyBuilderLevel {
        
        private List<Fanout<T>> list = new ArrayList<Fanout<T>>();
        private int level;
        
        /**
         * Creates a new instance
         * @param level
         */
        private HierarchyBuilderLevel(int level) {
            this.level = level;
        }

        /**
         * Adds the given fanout
         * @param fanout
         * @return
         */
        public HierarchyBuilderLevel add(Fanout<T> fanout) {
            this.list.add(fanout);
            setPrepared(false);
            return this;
        }

        /**
         * Removes all fanouts on this level
         * @return
         */
        public HierarchyBuilderLevel clear() {
            this.list.clear();
            setPrepared(false);
            return this;
        }
        
        /**
         * @return the level
         */
        public int getLevel() {
            return level;
        }

        /**
         * Removes the given fanout
         * @param fanout
         * @return
         */
        public HierarchyBuilderLevel remove(Fanout<T> fanout) {
            this.list.remove(fanout);
            setPrepared(false);
            return this;
        }
        
        /**
         * Returns the list
         * @return
         */
        private List<Fanout<T>> getFanouts(){
            return this.list;
        }
    }
    private static final long serialVersionUID = 3208791665131141362L;
    private DataType<T> type;
    
    private String[] data;
    
    private boolean prepared = false;
    
    /**
     * All fanouts for each level
     */
    private Map<Integer, HierarchyBuilderLevel> fanouts = new HashMap<Integer, HierarchyBuilderLevel>();

    /**
     * Creates a new instance for the given data type
     * @param type
     */
    public HierarchyBuilderGroupingBased(String[] data, DataType<T> type){
        this.type = type;
        this.data = data;
    }

    /**
     * Creates a new hierarchy, based on the predefined specification
     * @return
     */
    public Hierarchy create(){
        if (!prepared) prepare();
        return null;
    }
    
    /**
     * Return the first level on which grouping is to be performed. Returns 0 for order-based hierarchies and
     * 1 for interval-based hierarchies
     * @return
     */
    public abstract int getBaseLevel();
    
    /**
     * Returns the given level
     * @param level
     * @return 
     */
    public HierarchyBuilderLevel getLevel(int level){
        if (!this.fanouts.containsKey(level)) {
            this.fanouts.put(level, new HierarchyBuilderLevel(level));
            this.setPrepared(false);
        }
        return this.fanouts.get(level);
    }
    
    /**
     * Returns whether the current configuration is valid. Returns <code>null</code>, if so, an error message
     * if not.
     * @return
     */
    public String isValid() {
        
        // Check subclass
        String error = internalIsValid();
        if (error != null) return error;

        // Check fanouts
        int max = 0;
        for (Entry<Integer, HierarchyBuilderLevel> level : this.fanouts.entrySet()) {
            if (level.getValue().getFanouts().isEmpty()) {
                return "No fanout specified on level "+level.getKey();
            }
            max = Math.max(level.getKey(), max);
        }
        for (int i=0; i<max; i++){
            if (!this.fanouts.containsKey(i)) {
                return "Missing specification for level "+i;
            }
        }
        
        return null;
    }
    
    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     * @return
     */
    public int[] prepare(){
        String error = this.isValid();
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        this.doPrepare();
        return null;
    }
    
    /**
     * Tells the implementing class to prepare the generalization process
     */
    protected abstract void doPrepare();
    
    /**
     * Returns the data array
     * @return
     */
    protected String[] getData(){
        return data;
    }
    
    /**
     * Returns the data type
     * @return
     */
    protected DataType<T> getType(){
        return this.type;
    }
    
    /**
     * Returns whether the current configuration is valid. Returns <code>null</code>, if so, an error message
     * if not.
     * @return
     */
    protected abstract String internalIsValid();
    
    /**
     * Is this builder prepared allready
     * @param prepared
     */
    protected void setPrepared(boolean prepared){
        this.prepared = prepared;
    }
}
