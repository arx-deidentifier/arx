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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This abstract base class enables building hierarchies for categorical and non-categorical values
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class HierarchyBuilderGroupingBased<T> implements Serializable {

    private static final long serialVersionUID = 3208791665131141362L;
    private DataType<T> type;
    
    /**
     * Creates a new instance for the given data type
     * @param type
     */
    public HierarchyBuilderGroupingBased(DataType<T> type){
        this.type = type;
    }
    
    /**
     * All fanouts for each level
     */
    private Map<Integer, HierarchyBuilderLevel<T>> fanouts = new HashMap<Integer, HierarchyBuilderLevel<T>>();

    /**
     * This class represents a level in the hierarchy
     * @author Fabian Prasser
     */
    public static class HierarchyBuilderLevel<T> {
        
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
        public HierarchyBuilderLevel<T> add(Fanout<T> fanout) {
            this.list.add(fanout);
            return this;
        }

        /**
         * Removes the given fanout
         * @param fanout
         * @return
         */
        public HierarchyBuilderLevel<T> remove(Fanout<T> fanout) {
            this.list.remove(fanout);
            return this;
        }

        /**
         * @return the level
         */
        public int getLevel() {
            return level;
        }
    }

    /**
     * This class represents a fanout parameter
     * @author Fabian Prasser
     */
    public static class Fanout<T> implements Serializable {
        
        private static final long serialVersionUID = -5767501048737045793L;
        
        private int fanout;
        private AggregateFunction<DataType<T>> function;
        
        public Fanout(int fanout, AggregateFunction<DataType<T>> function) {
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
        public AggregateFunction<DataType<T>> getFunction() {
            return function;
        }
    }
    
    /**
     * Returns the given level
     * @param level
     */
    public void getLevel(int level){
        if (!this.fanouts.containsKey(level)) {
            this.fanouts.put(level, new HierarchyBuilderLevel<T>(level));
        }
    }
    
    /**
     * Returns the first level of the grouping instructions to be used. For order-based hierarchies this will be 1,
     * for interval-based hierarchies this will be 0.
     * @return
     */
    protected abstract int getBaseLevel();
    
    /**
     * Returns the label for the value at the given index at the first level of generalization
     * @param index
     * @return
     */
    protected abstract String getBaseLabel(int index);
    
    /**
     * Tells the implementing class to prepare the generalization process, i.e., prepare calls to
     * <code>prepareBaseLevel()</code>
     */
    protected abstract void prepare();
    
    /**
     * Returns whether the current configuration is valid. Returns <code>null</code>, if so, an error message
     * if not.
     * @return
     */
    protected abstract String internalIsValid();
    
    /**
     * Returns whether the current configuration is valid. Returns <code>null</code>, if so, an error message
     * if not.
     * @return
     */
    public String isValid() {
        // Also check: internalIsValid();
        return "Error";
    }
    
    /**
     * Returns the data type
     * @return
     */
    protected DataType<T> getType(){
        return this.type;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @param data
     * @param type
     * @return
     */
    public Hierarchy create(String[] data){
        this.prepare();
        return null;
    }
}
