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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        /** Children */
        private final IndexNode[]   children;
        /** IsLeaf */
        private final boolean       isLeaf;
        /** Leafs */
        private final Interval<T>[] leafs;
        /** Max is exclusive */
        private final T             max;
        /** Min is inclusive */
        private final T             min;

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
        
        @Override
        public String toString(){
            return toString("");
        }
        
        private String toString(String prefix){
            final String INTEND = "   ";
            StringBuilder b = new StringBuilder();
            DataType<T> type = getDataType();
            if (this.isLeaf) {
                b.append(prefix).append("Leafs[min=");
                b.append(type.format(min)).append(", max=");
                b.append(type.format(max)).append("]\n");
                for (Interval<T> leaf : leafs) {
                    b.append(prefix).append(INTEND).append("Leaf[min=");
                    b.append(type.format(leaf.min)).append(", max=");
                    b.append(type.format(leaf.max)).append(", function=");
                    b.append(leaf.function).append("]\n");
                }
                return b.toString();
            } else {
                b.append(prefix).append("Inner[min=");
                b.append(type.format(min)).append(", max=");
                b.append(type.format(max)).append("]\n");
                for (IndexNode child : children) {
                    b.append(child.toString(prefix+INTEND));
                }
                return b.toString();
            }
        }
    }
    
    /**
     * This class represents an interval
     * @author Fabian Prasser
     */
    @SuppressWarnings("hiding")
    public class Interval<T> extends Group {
        
        private static final long serialVersionUID = 5985820929677249525L;

        /** The function*/
        private final AggregateFunction<T> function;
        /** Max is exclusive */
        private final T max;
        /** Min is inclusive */
        private final T min;

        /**
         * Constructor for creating out of bounds labels
         * @param b
         */
        private Interval(boolean b) {
            super(b ? ">" + getDataType().format(HierarchyBuilderIntervalBased.this.max) : 
                      "<" + getDataType().format(HierarchyBuilderIntervalBased.this.min));
            this.min = null;
            this.max = null;
            this.function = null;
        }

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         * @param min
         * @param max
         * @param function
         */
        private Interval(DataType<T> type, T min, T max, AggregateFunction<T> function) {
            super(function.aggregate(new String[]{type.format(min), type.format(max)}));
            this.min = min;
            this.max = max;
            this.function = function;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(Group arg0) {
            DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
            T myMin = min;
            T otherMin = ((Interval<T>)arg0).min;
            if (myMin == null && otherMin != null) return -1;
            else if (myMin != null && otherMin == null) return +1;
            else if (myMin == null && otherMin == null) return 0;
            else return type.compare(min, ((Interval<T>)arg0).min);
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

        @Override
        public String toString(){
            @SuppressWarnings("unchecked")
            DataType<T> type = (DataType<T>)getDataType();
            return "Interval[min="+type.format(min)+", max="+type.format(max)+", function="+function.toString()+"]";
        }
        
        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected String getGroupLabel(Set<Group> groups, Fanout fanout) {
            DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
            T min = null;
            T max = null;
            for (Group group : groups){
                Interval<T> interval = (Interval<T>)group;
                if (min == null) min = interval.min;
                else  min = type.compare(min, interval.min) > 0 ? interval.min : min;
                if (max == null) max = interval.max;
                else  max = type.compare(max, interval.max) < 0 ? interval.max : max;
            }
            return fanout.getFunction().aggregate(new String[]{type.format(min), type.format(max)});
        }

        /**
         * Is this group out of bounds
         */
        protected boolean isOutOfBounds(){
            return min == null && max == null && function == null;
        }
    }

    /** TODO: Is this parameter OK?*/
    private static final int INDEX_FANOUT = 2;
    
    private static final long serialVersionUID = 3663874945543082808L;
    /** Adjustment*/
    private DynamicAdjustment adjustment = DynamicAdjustment.SNAP_TO_BOUNDS;
    /** Defined intervals*/
    private List<Interval<T>> intervals = new ArrayList<Interval<T>>();
    /** Max*/
    private T max;
    /** Min*/
    private T min;
    /** OOB label*/
    private final Interval<T> OUT_OF_BOUNDS_MAX;
    /** OOB label*/
    private final Interval<T> OUT_OF_BOUNDS_MIN;
    
    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     */
    public HierarchyBuilderIntervalBased(T min, T max, DataType<T> type) {
        super(Type.INTERVAL_BASED, type);
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
    public HierarchyBuilderIntervalBased(T min, T max, DataType<T> type, DynamicAdjustment adjustment) {
        super(Type.INTERVAL_BASED, type);
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
        checkInterval(getDataType(), min, max);
        this.intervals.add(new Interval<T>(getDataType(), min, max, this.function));
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
        checkInterval(getDataType(), min, max);
        this.intervals.add(new Interval<T>(getDataType(), min, max, function));
        this.setPrepared(false);
        return this;
    }

    /**
     * Adds an interval. Min is inclusive, max is exclusive. Interval is labeled
     * with the given string
     * @param min
     * @param max
     * @param label
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max, String label) {
        if (label==null) {
            throw new IllegalArgumentException("Label must not be null");
        }
        checkInterval(getDataType(), min, max);
        this.intervals.add(new Interval<T>(getDataType(), min, max, AggregateFunction.CONSTANT(getDataType(), label)));
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
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
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
    private Interval<T> getInterval(IndexNode index, T actualMax, String sValue) {

        // Init
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        T tValue = type.parse(sValue);

        // Check if out of bounds
        if (adjustment == DynamicAdjustment.OUT_OF_BOUNDS_LABEL) {
            if (type.compare(tValue, min) < 0) {
                return OUT_OF_BOUNDS_MIN;
            } else if (type.compare(tValue, max) > 0) {
                return OUT_OF_BOUNDS_MAX;
            }
        }
        
        // Find interval
        int iindex = (int)Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        T offset = type.multiply(type.subtract(index.max, index.min), iindex);
        Interval<T> interval = query(index, type.subtract(tValue, offset));

        // Check
        if (interval == null) { throw new IllegalStateException("No interval found for: " + sValue); }

        // Extract values
        AggregateFunction<T> function = interval.function;
        T lower = type.add(interval.min, offset);
        T upper = type.add(interval.max, offset);

        // Adjust largest interval
        if (type.compare(tValue, max) > 0) {
            if (adjustment != DynamicAdjustment.SNAP_TO_BOUNDS) {
                throw new IllegalArgumentException("Value out of bounds: "+sValue);
            } else {
                if (type.compare(lower, tValue) <=0 &&
                    type.compare(upper, tValue) >0) {
                    upper = actualMax;
                }
            }
        }
   
        // Return
        return new Interval<T>((DataType<T>)type, lower, upper, function);
    }
    
    @Override
    protected int getBaseLevel() {
        return 0;
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

    @Override
    @SuppressWarnings("unchecked")
    protected List<Group> prepareGroups() {

        // Check
        String valid = internalIsValid();
        if (valid != null) {
            throw new IllegalArgumentException(valid);
        }
        
        // Init
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        
        // Determine max and min
        T actualMin = min;
        T actualMax = max;
        if (this.adjustment == DynamicAdjustment.SNAP_TO_BOUNDS) {
            for (String value : getData()){
                T parsedValue = type.parse(value);
                try {
                    if (type.compare(actualMin, parsedValue)>0){
                        actualMin = parsedValue;
                    }
                    if (type.compare(actualMax, parsedValue)<0){
                        actualMax = parsedValue;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid data item: "+value);
                }
            }
        }
        
        // Create a reference to the intervals, in which the leftmost might be replaced
        List<Interval<T>> actualIntervals = this.intervals;
        if (actualMin != min) {
            actualIntervals = (ArrayList<Interval<T>>)
                              ((ArrayList<Interval<T>>)this.intervals).clone();
            Interval<T> first = intervals.get(0);
            actualIntervals.set(0 ,new Interval<T>((DataType<T>)type,
                                                   actualMin,
                                                   first.max,
                                                   first.function));
            
        }
        
        // Build leaf level index
        ArrayList<IndexNode> nodes = new ArrayList<IndexNode>();
        for (int i=0, len = actualIntervals.size(); i < len; i+=INDEX_FANOUT) {
            int min = i;
            int max = Math.min(i+INDEX_FANOUT-1, len-1);
            
            List<Interval<T>> leafs = new ArrayList<Interval<T>>();
            for (int j=min; j<=max; j++) {
                leafs.add(actualIntervals.get(j));
            }

            nodes.add(new IndexNode(actualIntervals.get(min).min, 
                                    actualIntervals.get(max).max,
                                    leafs.toArray(new Interval[leafs.size()])));
        }

        // Builder inner nodes
        while (nodes.size()>1) {
            List<IndexNode> current = (List<IndexNode>)nodes.clone();
            nodes.clear();
            for (int i=0, len = current.size(); i < len; i+=INDEX_FANOUT) {
                int min = i;
                int max = Math.min(i+INDEX_FANOUT-1, len-1);
                List<IndexNode> temp = new ArrayList<IndexNode>();
                for (int j=min; j<=max; j++) {
                    temp.add(current.get(j));
                }

                nodes.add(new IndexNode(current.get(min).min, 
                                        current.get(max).max,
                                        temp.toArray(new HierarchyBuilderIntervalBased.IndexNode[temp.size()])));
            }
        }
        
        // Store index
        IndexNode index = nodes.get(0);
        
        // Replace each value with label
        String[] data = getData();
        List<Group> groups = new ArrayList<Group>();
        Map<Group, Group> cache = new HashMap<Group, Group>();
        for (int i=0; i<data.length; i++){

            Group group = getInterval(index, actualMax, data[i]);
            Group cached = cache.get(group);
            if (cached != null) {
                groups.add(cached);
            }
            else {
                cache.put(group, group);
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderIntervalBased<T> create(String file) throws IOException{
        return create(new File(file));
    }
    
    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> HierarchyBuilderIntervalBased<T> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilderIntervalBased<T> result = (HierarchyBuilderIntervalBased<T>)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }
}
