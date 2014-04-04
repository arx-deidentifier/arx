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
import java.util.Collections;
import java.util.Comparator;
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
        
        /** Fanout*/
        private final int fanout;
        /** Aggregate function*/
        private final AggregateFunction<T> function;
        
        /**
         * Creates a new instance
         * @param fanout
         * @param function
         */
        private Fanout(int fanout, AggregateFunction<T> function) {
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
    public class Level {
        
        private final List<Fanout<T>> list = new ArrayList<Fanout<T>>();
        private final int level;
        
        /**
         * Creates a new instance
         * @param level
         */
        private Level(int level) {
            this.level = level;
        }
        
        /**
         * Adds the given fanout with the default aggregate function
         * @param fanout
         * @return
         */
        public Level addFanout(int fanout) {
            if (function == null) {
                throw new IllegalStateException("No default aggregate function defined");
            }
            this.list.add(new Fanout<T>(fanout, function));
            setPrepared(false);
            return this;
        }

        /**
         * Adds the given fanout with the given aggregate function
         * @param fanout
         * @return
         */
        public Level addFanout(int fanout, AggregateFunction<T> function) {
            this.list.add(new Fanout<T>(fanout, function));
            setPrepared(false);
            return this;
        }

        /**
         * Removes all fanouts on this level
         * @return
         */
        public Level clearFanouts() {
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
         * Returns the list
         * @return
         */
        @SuppressWarnings("unchecked")
        public List<Fanout<T>> getFanouts(){
            return (List<Fanout<T>>)((ArrayList<Fanout<T>>)this.list).clone();
        }
    }
    private static final long serialVersionUID = 3208791665131141362L;
    private DataType<T> type;
    protected AggregateFunction<T> function;
    private String[] data;
    private boolean prepared = false;
    
    /**
     * All fanouts for each level
     */
    private Map<Integer, Level> fanouts = new HashMap<Integer, Level>();

    /**
     * Creates a new instance for the given data type
     * @param type
     */
    protected HierarchyBuilderGroupingBased(String[] data, DataType<T> type){
        this.type = type;
        this.data = data;
    }
    
    /**
     * Sets the default aggregate function to be used by all fanouts
     * @param function
     */
    public void setAggregateFunction(AggregateFunction<T> function){
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        this.function = function;
    }

    /**
     * Creates a new hierarchy, based on the predefined specification
     * @return
     */
    public Hierarchy create(){
        if (!prepared) prepare();
        return Hierarchy.create(create(3));
    }
    
    /**
     * Return the first level on which grouping is to be performed. Returns 1 for order-based hierarchies and
     * 2 for interval-based hierarchies
     * @return
     */
    protected abstract int getBaseLevel();
    
    /**
     * Returns the given level
     * @param level
     * @return 
     */
    public Level getLevel(int level){
        if (!this.fanouts.containsKey(level)) {
            this.fanouts.put(level, new Level(level));
            this.setPrepared(false);
        }
        return this.fanouts.get(level);
    }
    
    /**
     * Returns all currently defined levels
     * @return
     */
    public List<Level> getLevels(){
        List<Level> levels = new ArrayList<Level>();
        levels.addAll(this.fanouts.values());
        Collections.sort(levels, new Comparator<Level>(){
            @Override
            public int compare(Level o1,
                               Level o2) {
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
        
        // Check subclass
        String error = internalIsValid();
        if (error != null) return error;
        
        // Check values
        for (String value : data) {
            if (!type.isValid(value)) {
                return "Invalid data item: "+value;
            }
        }

        // Check fanouts
        int max = 0;
        for (Entry<Integer, Level> level : this.fanouts.entrySet()) {
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
    
    /**
     * To be implement by subclasses. Prepare any level in the output, if needed.
     * @return
     */
    protected abstract String[][] create(String[][] result);
    
    /**
     * Returns an initial multi-dimensional array to represent the hierarchy
     * @param levels
     * @return
     */
    private String[][] create(int levels){
        String[][] result = new String[data.length][levels];
        for (int i=0; i<result.length; i++) {
            result[i] = new String[levels];
            result[i][0] = data[i];
        }
        result = create(result);
        return result;
    }
}
