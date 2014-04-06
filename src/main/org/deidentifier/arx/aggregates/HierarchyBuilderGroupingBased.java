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
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This abstract base class enables building hierarchies for categorical and non-categorical values
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class HierarchyBuilderGroupingBased<T> extends HierarchyBuilder<T> implements Serializable {

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
        
        @Override
        public String toString(){
            return "Fanout[length="+fanout+", function="+function.toString()+"]";
        }
    }
    
    /**
     * This class represents a level in the hierarchy
     * @author Fabian Prasser
     */
    public class Level {
        
        private final int level;
        private final List<Fanout<T>> list = new ArrayList<Fanout<T>>();
        
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
         * @param function
         * @return
         */
        public Level addFanout(int fanout, AggregateFunction<T> function) {
            this.list.add(new Fanout<T>(fanout, function));
            setPrepared(false);
            return this;
        }

        /**
         * Adds the given fanout. The result will be labeled with the given string
         * @param fanout
         * @param label
         * @return
         */
        public Level addFanout(int fanout, String label) {
            this.list.add(new Fanout<T>(fanout, AggregateFunction.CONSTANT(getDataType(), label)));
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
         * Returns the list
         * @return
         */
        @SuppressWarnings("unchecked")
        public List<Fanout<T>> getFanouts(){
            return (List<Fanout<T>>)((ArrayList<Fanout<T>>)this.list).clone();
        }

        /**
         * @return the level
         */
        public int getLevel() {
            return level;
        }
     
        @Override
        public String toString(){
            StringBuilder b = new StringBuilder();
            b.append("Level[height="+level+"]\n");
            for (int i=0, length=list.size(); i<length; i++){
                Fanout<T> fanout = list.get(i);
                b.append("   ").append(fanout.toString());
                if (i<length-1) b.append("\n");
            }
            return b.toString();
        }
    }
    /**
     * A group representation to be used by subclasses
     * @author Fabian Prasser
     */
    protected abstract class Group implements Serializable, Comparable<Group> {
        
        private static final long serialVersionUID = -7657969446040078411L;
        
        private String label;
        
        protected Group(String label){
            this.label = label;
        }
        @SuppressWarnings("rawtypes") 
        protected abstract String getGroupLabel(Set<Group> groups, Fanout fanout);
        
        protected String getLabel(){
            return label;
        }
        
        protected abstract boolean isOutOfBounds();
    }
    private static final long serialVersionUID = 3208791665131141362L;
    private transient String[] data;
    /**
     * All fanouts for each level
     */
    private Map<Integer, Level> fanouts = new HashMap<Integer, Level>();
    private transient List<Group> groups;
    private transient boolean prepared = false;
    private DataType<T> datatype;
    
    protected AggregateFunction<T> function;

    /**
     * Creates a new instance for the given data type
     * @param type
     */
    protected HierarchyBuilderGroupingBased(Type type, DataType<T> datatype){
        super(type);
        this.datatype = datatype;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @return
     */
    public Hierarchy create(){
        if (!prepared) {
            throw new IllegalStateException("Please call prepare() first");
        }

        Collections.sort(groups);
        
        String[][] result = new String[data.length][2];
        for (int i=0; i<result.length; i++) {
            result[i] = new String[2];
            result[i][0] = data[i];
            result[i][1] = groups.get(i).getLabel();
        }
        Hierarchy h = Hierarchy.create(result);
        
        // 1. Obtain list of unique groups
        // 2. Sort them (they implement comparable)
        // 3. Handle duplicate labels, two options
        // 3. Perform grouping, but exclude OutOfBounds groups
        
        this.prepared = false;
        this.data = null;
        this.groups = null;
        return h;
    }

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
    public int[] prepare(String[] data){
        this.data = data;
        String error = this.isValid();
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        this.groups = this.prepareGroups();
        this.prepared = true;
        return new int[]{1};
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
     * First level to start aggregating
     * @return
     */
    protected abstract int getBaseLevel();
    
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
    protected DataType<T> getDataType(){
        return this.datatype;
    }
    
    /**
     * Returns whether the current configuration is valid. Returns <code>null</code>, if so, an error message
     * if not.
     * @return
     */
    protected abstract String internalIsValid();
    
    /**
     * Tells the implementing class to prepare the generalization process
     */
    protected abstract List<Group> prepareGroups();
    
    /**
     * Is this builder prepared allready
     * @param prepared
     */
    protected void setPrepared(boolean prepared){
        this.prepared = prepared;
        if (prepared == false) {
            this.groups = null;
        }
    }
}
