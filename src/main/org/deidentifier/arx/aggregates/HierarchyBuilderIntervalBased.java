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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies for non-categorical values by mapping them
 * into given intervals
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyBuilderIntervalBased<T> extends HierarchyBuilderGroupingBased<T> {
    
    public static enum DynamicAdjustment{
        OUT_OF_BOUNDS_LABEL,
        SNAP_TO_BOUNDS
    }
    
    /**
     * This class represents an node
     * @author Fabian Prasser
     */
    public class IndexNode implements Serializable {
        
        private static final long serialVersionUID = 5985820929677249525L;

        /** Min is inclusive */
        private T                 min;
        /** Max is exclusive */
        private T                 max;
        /** Children */
        private IndexNode[]       children;
        /** Leafs */
        private Interval[]        leafs;
        /** IsLeaf */
        private boolean           isLeaf;

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         * @param min
         * @param max
         * @param function
         */
        public IndexNode(T min, T max, IndexNode[] children) {
            this.min = min;
            this.max = max;
            this.children = children;
            this.leafs = null;
            this.isLeaf = false;
        }

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         * @param min
         * @param max
         * @param function
         */
        public IndexNode(T min, T max, Interval[] leafs) {
            this.min = min;
            this.max = max;
            this.children = null;
            this.leafs = leafs;
            this.isLeaf = true;
        }
    }
    
    /**
     * This class represents an interval
     * @author Fabian Prasser
     */
    public class Interval implements Serializable {
        
        private static final long serialVersionUID = 5985820929677249525L;
        
        /** Min is inclusive */
        private T min;
        /** Max is exclusive */
        private T max;
        /** The aggregate function*/
        private AggregateFunction<T> function;

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         * @param min
         * @param max
         * @param function
         */
        public Interval(T min, T max, AggregateFunction<T> function) {
            if (function==null) {
                throw new IllegalArgumentException("Function must not be null");
            }
            checkInterval(getType(), min, max);
            this.min = min;
            this.max = max;
            this.function = function;
        }

        /**
         * @return the function
         */
        public AggregateFunction<T> getFunction() {
            return function;
        }

        /**
         * @return the max (inclusive)
         */
        public T getMax() {
            return max;
        }

        /**
         * @return the min (exclusive)
         */
        public T getMin() {
            return min;
        }
    }

    private static final long serialVersionUID = 3663874945543082808L;
    /** TODO: Is this parameter OK?*/
    private static final int INDEX_FANOUT = 2;
    private AggregateFunction<T> function = null;
    private List<Interval> intervals = new ArrayList<Interval>();
    private T min;
    private T max;
    private T epsilon; 
    
    private DynamicAdjustment adjustment = DynamicAdjustment.SNAP_TO_BOUNDS;

    private IndexNode index = null;
    
    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     */
    public HierarchyBuilderIntervalBased(String[] data, T min, T max, DataType<T> type) {
        super(data, type);
        if (function==null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        checkInterval(type, min, max);
        this.min = min;
        this.max = max;
    }

    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     * @param epsilon
     * @param adjustment
     */
    public HierarchyBuilderIntervalBased(String[] data, T min, T max, DataType<T> type, T epsilon, DynamicAdjustment adjustment) {
        super(data, type);
        if (function==null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        checkInterval(type, min, max);
        this.min = min;
        this.max = max;
        this.epsilon = epsilon;
        this.adjustment = adjustment;
    }

    /**
     * Adds an interval. Min is inclusive, max is exclusive. Uses the predefined default aggregate function
     * @param min
     * @param max
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max) {
        if (this.function == null) {
            throw new IllegalStateException("No default aggregate function defined");
        }
        checkInterval(getType(), min, max);
        this.intervals.add(new Interval(min, max, this.function));
        this.setPrepared(false);
        return this;
    }
    
    /**
     * Adds an interval. Min is inclusive, max is exclusive
     * @param min
     * @param max
     * @param function
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max, AggregateFunction<T> function) {
        if (function==null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        checkInterval(getType(), min, max);
        this.intervals.add(new Interval(min, max, function));
        this.setPrepared(false);
        return this;
    }

    /**
     * Adds an interval. Min is inclusive, max is exclusive. Uses the predefined default aggregate function
     * @param min
     * @param max
     * @return
     */
    public HierarchyBuilderIntervalBased<T> clearIntervals() {
        this.intervals.clear();
        this.setPrepared(false);
        return this;
    }

    @Override
    public int getBaseLevel() {
        return 1;
    }

    /**
     * Removes the given fanout
     * @param fanout
     * @return
     */
    public HierarchyBuilderIntervalBased<T> remove(Interval fanout) {
        this.intervals.remove(fanout);
        this.setPrepared(false);
        return this;
    }

    /**
     * Defines an aggregate function to be used by all intervals
     * @param function
     */
    public void setAggregateFunction(AggregateFunction<T> function) {
        if (function==null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        this.function = function;
        this.setPrepared(false);
    }
    
    /**
     * Checks the interval
     * @param type
     * @param min
     * @param max
     */
    private void checkInterval(DataType<T> type, T min, T max){
        try {
            if (type.compare(type.format(min), type.format(max)) >= 0) {
                throw new IllegalArgumentException("Min ("+min+") must be lower than max ("+max+")");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPrepare() {

        // Check
        index = null;
        String valid = internalIsValid();
        if (valid != null) {
            throw new IllegalArgumentException(valid);
        }
        
        // Build leaf level index
        ArrayList<IndexNode> nodes = new ArrayList<IndexNode>();
        for (int i=0, len = intervals.size(); i < len; i+=INDEX_FANOUT) {
            int min = i;
            int max = Math.min(i+INDEX_FANOUT-1, len-1);
            
            List<Interval> leafs = new ArrayList<Interval>();
            for (int j=min; j<=max; j++) {
                leafs.add(intervals.get(j));
            }

            nodes.add(new IndexNode(intervals.get(min).min, 
                                       intervals.get(max).max,
                                       (Interval[])leafs.toArray()));
        }

        // Builder inner nodes
        while (nodes.size()>1) {
            nodes.clear();
            List<IndexNode> current = (List<IndexNode>)nodes.clone();
            for (int i=0, len = current.size(); i < len; i+=INDEX_FANOUT) {
                int min = i;
                int max = Math.min(i+INDEX_FANOUT-1, len-1);
                
                List<IndexNode> temp = new ArrayList<IndexNode>();
                for (int j=min; j<=max; j++) {
                    temp.add(current.get(j));
                }

                nodes.add(new IndexNode(intervals.get(min).min, 
                                           intervals.get(max).max,
                                           (Interval[])temp.toArray()));
            }
        }
    }

    @Override
    protected String internalIsValid() {
        
        if (!intervals.get(0).getMin().equals(min)) {
            return "Lower bound of first interval must match overall lower bound";
        }
        
        for (int i=1; i<intervals.size(); i++){
            Interval interval1 = intervals.get(i-1);
            Interval interval2 = intervals.get(i);
            
            if (!interval1.getMax().equals(interval2.getMin())) {
                return "Gap between interval"+i+" and interval"+(i+1);
            }
        }
        
        return null;
    }
}
