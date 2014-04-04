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
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

/**
 * This class enables building hierarchies for non-categorical values by mapping them
 * into given intervals
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyBuilderIntervalBased<T> extends HierarchyBuilderGroupingBased<T> {
    
    /**
     * The dynamic adjustment settings
     * @author Fabian Prasser
     */
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
        private final T             min;
        /** Max is exclusive */
        private final T             max;
        /** Children */
        private final IndexNode[]   children;
        /** Leafs */
        private final Interval<T>[] leafs;
        /** IsLeaf */
        private final boolean       isLeaf;

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
        public IndexNode(T min, T max, Interval<T>[] leafs) {
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
    @SuppressWarnings("hiding")
    public class Interval<T> implements Serializable {
        
        private static final long serialVersionUID = 5985820929677249525L;
        
        /** Min is inclusive */
        private final T min;
        /** Max is exclusive */
        private final T max;
        /** The interval's label*/
        private String label;
        /** The function*/
        private final AggregateFunction<T> function;

        /**
         * Constructor for creating out of bounds labels
         * @param b
         */
        private Interval(boolean b) {
            this.min = null;
            this.max = null;
            this.function = null;
            if (b) {
                label = ">" + getType().format(HierarchyBuilderIntervalBased.this.max);
            } else {
                label = "<" + getType().format(HierarchyBuilderIntervalBased.this.min);
            }
        }

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         * @param min
         * @param max
         * @param function
         */
        private Interval(DataType<T> type, T min, T max, AggregateFunction<T> function) {
            this.min = min;
            this.max = max;
            this.function = function;
            this.label = function.aggregate(new String[]{type.format(min), type.format(max)});
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Interval<T> other = (Interval<T>) obj;
            if (max == null) {
                if (other.max != null) return false;
            } else if (!max.equals(other.max)) return false;
            if (min == null) {
                if (other.min != null) return false;
            } else if (!min.equals(other.min)) return false;
            return true;
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

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((max == null) ? 0 : max.hashCode());
            result = prime * result + ((min == null) ? 0 : min.hashCode());
            return result;
        }

        /**
         * Gets the label
         * @param label
         */
        private String getLabel(){
            return label;
        }
    }

    private static final long serialVersionUID = 3663874945543082808L;
    
    /** OOB label*/
    private final Interval<T> OUT_OF_BOUNDS_MAX;
    /** OOB label*/
    private final Interval<T> OUT_OF_BOUNDS_MIN;
    /** TODO: Is this parameter OK?*/
    private static final int INDEX_FANOUT = 2;
    /** Defined intervals*/
    private List<Interval<T>> intervals = new ArrayList<Interval<T>>();
    /** Min*/
    private T min;
    /** Max*/
    private T max;
    /** Adjustment*/
    private DynamicAdjustment adjustment = DynamicAdjustment.SNAP_TO_BOUNDS;
    /** Index*/
    private IndexNode index = null;
    
    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     */
    public HierarchyBuilderIntervalBased(String[] data, T min, T max, DataType<T> type) {
        super(data, type);
        checkInterval(type, min, max);
        if (!(type instanceof DataTypeWithRatioScale)) {
            throw new IllegalArgumentException("Data type must have a ratio scale");
        }
        this.min = min;
        this.max = max;
        this.OUT_OF_BOUNDS_MIN = new Interval<T>(false);
        this.OUT_OF_BOUNDS_MAX = new Interval<T>(true);
    }

    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     * @param epsilon
     * @param adjustment
     */
    public HierarchyBuilderIntervalBased(String[] data, T min, T max, DataType<T> type, DynamicAdjustment adjustment) {
        super(data, type);
        checkInterval(type, min, max);
        if (!(type instanceof DataTypeWithRatioScale)) {
            throw new IllegalArgumentException("Data type must have a ratio scale");
        }
        this.min = min;
        this.max = max;
        this.OUT_OF_BOUNDS_MIN = new Interval<T>(false);
        this.OUT_OF_BOUNDS_MAX = new Interval<T>(true);
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
        this.intervals.add(new Interval<T>(getType(), min, max, this.function));
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
        this.intervals.add(new Interval<T>(getType(), min, max, function));
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
    
    /**
     * Returns all currently defined intervals
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Interval<T>> getIntervals(){
        return (List<Interval<T>>)((ArrayList<T>)this.intervals).clone();
    }

    /**
     * Performs the index lookup
     * @param value
     * @return
     */
    public Interval<T> query(IndexNode node, T value) {
        @SuppressWarnings("unchecked")
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getType();
        if (node.isLeaf) {
            for (Interval<T> leaf : node.leafs) {
                if (type.compare(leaf.min, value) <= 0 && type.compare(leaf.max, value) > 0) {
                    return leaf;
                }
            }
        } else {
            for (IndexNode child : node.children) {
                if (type.compare(child.min, value) <= 0 && type.compare(child.max, value) > 0) {
                    return query(child, value);
                }
            }
        }
        throw new IllegalStateException("No interval found for: "+type.format(value));
    }

    /**
     * Checks the interval
     * @param type
     * @param min
     * @param max
     */
    private <U> void checkInterval(DataType<U> type, U min, U max){
        try {
            if (type.compare(type.format(min), type.format(max)) > 0) {
                throw new IllegalArgumentException("Min ("+min+") must be lower than max ("+max+")");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid data item "+min+" or "+max);
        }
    }

    @SuppressWarnings("unchecked")
    private Interval<T> getInterval(String sValue) {

        // Init
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getType();
        T tValue = type.parse(sValue);

        // Check if out of bounds
        if (adjustment == DynamicAdjustment.OUT_OF_BOUNDS_LABEL) {
            if (type.compare(tValue, min) < 0) {
                return OUT_OF_BOUNDS_MIN;
            } else if (type.compare(tValue, max) > 0) {
                return OUT_OF_BOUNDS_MAX;
            }
        }
        
        // Find interval offset
        int iindex = (int)Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        T lower; 
        T upper;
        AggregateFunction<T> function;
        
        // Adjust
        if (iindex < 0) {
            if (adjustment != DynamicAdjustment.SNAP_TO_BOUNDS) {
                throw new IllegalStateException("Value out of bounds: "+sValue);
            } else {
                lower = min;
                upper = intervals.get(0).max;
                function = intervals.get(0).function;
            }
        } else {
            
            T offset = type.multiply(type.subtract(index.max, index.min), iindex);
            Interval<T> interval = query(index, type.subtract(tValue, offset));
            
            if (interval == null) {
                throw new IllegalStateException("No interval found for: "+sValue);
            }
            
            function = interval.function;
            if (interval.equals(intervals.get(0)) && iindex==0) {
                lower = min;
                upper = intervals.get(0).max;
            } else {
                lower = type.add(interval.min, offset);
                upper = type.add(interval.max, offset);
            }
            
            if (type.compare(upper, max) > 0) {
                upper = max;
            }
        }
        
        // Return
        return new Interval<T>((DataType<T>)type, lower, upper, function);
    }

    @Override
    protected String[][] create(String[][] result) {

        // Replace each value with label
        String[] data = getData();
        for (int i=0; i<data.length; i++){
            Interval<T> interval = getInterval(data[i]);
            result[i][1] = interval.getLabel();
        }
        return result;
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
        
        // Init
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getType();
        
        // Determine max and min
        if (this.adjustment == DynamicAdjustment.SNAP_TO_BOUNDS) {
            for (String value : getData()){
                T parsedValue = type.parse(value);
                try {
                    if (type.compare(min, parsedValue)>0){
                        min = parsedValue;
                    }
                    if (type.compare(max, parsedValue)<0){
                        max = parsedValue;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid data item: "+value);
                }
            }
        }
        
        // Build leaf level index
        ArrayList<IndexNode> nodes = new ArrayList<IndexNode>();
        for (int i=0, len = intervals.size(); i < len; i+=INDEX_FANOUT) {
            int min = i;
            int max = Math.min(i+INDEX_FANOUT-1, len-1);
            
            List<Interval<T>> leafs = new ArrayList<Interval<T>>();
            for (int j=min; j<=max; j++) {
                leafs.add(intervals.get(j));
            }

            nodes.add(new IndexNode(intervals.get(min).min, 
                                       intervals.get(max).max,
                                       leafs.toArray(new Interval[leafs.size()])));
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
                                           temp.toArray(new Interval[temp.size()])));
            }
        }
        
        // Store index
        this.index = nodes.get(0);
    }

    @Override
    protected int getBaseLevel() {
        return 2;
    }
    
    @Override
    protected String internalIsValid() {
        
        if (!intervals.get(0).getMin().equals(min)) {
            return "Lower bound of first interval must match overall lower bound";
        }
        
        for (int i=1; i<intervals.size(); i++){
            Interval<T> interval1 = intervals.get(i-1);
            Interval<T> interval2 = intervals.get(i);
            
            if (!interval1.getMax().equals(interval2.getMin())) {
                return "Gap between interval"+i+" and interval"+(i+1);
            }
        }
        
        return null;
    }
}
