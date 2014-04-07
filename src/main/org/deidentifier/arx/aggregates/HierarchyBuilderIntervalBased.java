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
     * For each direction, this class encapsulates three bounds. Intervals will be repeated until the
     * repeat-bound is reached. The outmost intervals will than be exptended to the snap-bound. Values between
     * the snap-bound and the label-bound will be replaced by an out-of-bounds-label. For values larger than
     * the label-bound exceptions will be raised.
     * 
     * @author Fabian Prasser
     */
    public static class Adjustment<U> implements Serializable {
        
        private static final long serialVersionUID = -5385139177770612960L;
        
        /** Bound*/
        private U repeatBound;
        /** Bound*/
        private U snapBound;
        /** Bound*/
        private U labelBound;

        /**
         * Creates a new instance
         * @param repeatBound
         * @param snapBound
         * @param labelBound
         */
        public Adjustment(U repeatBound,
                          U snapBound,
                          U labelBound) {
            
            this.repeatBound = repeatBound;
            this.snapBound = snapBound;
            this.labelBound = labelBound;
        }

        /**
         * @return the repeatBound
         */
        public U getRepeatBound() {
            return repeatBound;
        }

        /**
         * @return the snapBound
         */
        public U getSnapBound() {
            return snapBound;
        }

        /**
         * @return the labelBound
         */
        public U getLabelBound() {
            return labelBound;
        }

        /**
         * @param repeatBound the repeatBound to set
         */
        private void setRepeatBound(U repeatBound) {
            this.repeatBound = repeatBound;
        }

        /**
         * @param snapBound the snapBound to set
         */
        private void setSnapBound(U snapBound) {
            this.snapBound = snapBound;
        }

        /**
         * @param labelBound the labelBound to set
         */
        private void setLabelBound(U labelBound) {
            this.labelBound = labelBound;
        }
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
    public static class Interval<T> extends Group {
        
        private static final long serialVersionUID = 5985820929677249525L;

        /** The function*/
        private final AggregateFunction<T> function;
        /** Max is exclusive */
        private final T max;
        /** Min is inclusive */
        private final T min;
        /** The builder*/
        private final HierarchyBuilderGroupingBased<T> builder;

        /**
         * Constructor for creating out of bounds labels
         * @param b
         */
        private Interval(HierarchyBuilderGroupingBased<T> builder, boolean lower, T value) {
            super(lower ? "<" + ((DataType<T>)builder.getDataType()).format(value) : 
                          ">" + ((DataType<T>)builder.getDataType()).format(value));
            this.builder = builder;
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
        private Interval(HierarchyBuilderGroupingBased<T> builder,DataType<T> type, T min, T max, AggregateFunction<T> function) {
            super(function.aggregate(new String[]{type.format(min), type.format(max)}));
            this.builder = builder;
            this.min = min;
            this.max = max;
            this.function = function;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(Group arg0) {
            DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)builder.getDataType();
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
            DataType<T> type = (DataType<T>)builder.getDataType();
            return "Interval[min="+type.format(min)+", max="+type.format(max)+", function="+function.toString()+"]";
        }
        
        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected String getGroupLabel(Set<Group> groups, Fanout fanout) {
            DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)builder.getDataType();
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

    /** TODO: Is this parameter OK? */
    private static final int  INDEX_FANOUT     = 2;

    /** SVUID */
    private static final long serialVersionUID = 3663874945543082808L;
    
    /** Adjustment */
    private Adjustment<T>     minAdjustment;
    /** Adjustment */
    private Adjustment<T>     maxAdjustment;
    /** Defined intervals */
    private List<Interval<T>> intervals        = new ArrayList<Interval<T>>();

    /**
     * Creates a new instance. Snapping is disabled. Repetition is disabled. Bound is determined dynamically.
     * @param type
     */
    public HierarchyBuilderIntervalBased(DataType<T> type) {
        super(Type.INTERVAL_BASED, type);
        if (!(type instanceof DataTypeWithRatioScale)) {
            throw new IllegalArgumentException("Data type must have a ratio scale");
        }
        this.minAdjustment = new Adjustment<T>(null, null, null);
        this.maxAdjustment = new Adjustment<T>(null, null, null);
    }

    /**
     * Creates a new instance. Data points that are out of range are handled according to the given settings.
     * @param type
     * @param lowerAdjustment
     * @param upperAdjustment
     */
    public HierarchyBuilderIntervalBased(DataType<T> type, Adjustment<T> lowerAdjustment, Adjustment<T> upperAdjustment) {
        super(Type.INTERVAL_BASED, type);
        if (!(type instanceof DataTypeWithRatioScale)) {
            throw new IllegalArgumentException("Data type must have a ratio scale");
        }
        this.minAdjustment = lowerAdjustment;
        this.maxAdjustment = upperAdjustment;
    }

    /**
     * Adds an interval. Min is inclusive, max is exclusive. Uses the predefined default aggregate function
     * @param min
     * @param max
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max) {
        if (this.getDefaultAggregateFunction() == null) {
            throw new IllegalStateException("No default aggregate function defined");
        }
        checkInterval(getDataType(), min, max);
        this.intervals.add(new Interval<T>(this, getDataType(), min, max, this.getDefaultAggregateFunction()));
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
        this.intervals.add(new Interval<T>(this, getDataType(), min, max, function));
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
        this.intervals.add(new Interval<T>(this, getDataType(), min, max, AggregateFunction.CONSTANT(getDataType(), label)));
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
    private Interval<T> getInterval(IndexNode index, String sValue, 
                                    Adjustment<T> lowerAdjustment, Adjustment<T> upperAdjustment) {

        // Init
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        T tValue = type.parse(sValue);

        // Find interval
        int shift = (int)Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        T offset = type.multiply(type.subtract(index.max, index.min), shift);
        Interval<T> interval = query(index, type.subtract(tValue, offset));

        // Check
        if (interval == null) { throw new IllegalStateException("No interval found for: " + sValue); }
        
        T lower = type.add(interval.min, offset);
        T upper = type.add(interval.max, offset);
        return new Interval<T>(this, (DataType<T>)type, lower, upper, interval.function);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String isValid() {

        String superIsValid = super.isValid();
        if (superIsValid != null) return superIsValid;
        
        if (intervals.isEmpty()) {
            return "No intervals specified";
        }
        
        for (int i=1; i<intervals.size(); i++){
            Interval<T> interval1 = intervals.get(i-1);
            Interval<T> interval2 = intervals.get(i);
            
            if (!interval1.getMax().equals(interval2.getMin())) {
                return "Gap between interval"+i+" and interval"+(i+1);
            }
        }
        
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        if (minAdjustment.getRepeatBound() != null){
            if (type.compare(minAdjustment.getRepeatBound(), intervals.get(0).min) > 0){
                return "Lower repeat bound must be <= lower bound of first interval"; 
            }
            if (minAdjustment.getSnapBound() != null){
                if (type.compare(minAdjustment.getSnapBound(), minAdjustment.getRepeatBound()) > 0){
                    return "Lower snap bound must be <= lower repeat bound"; 
                }
                if (minAdjustment.getLabelBound() != null){
                    if (type.compare(minAdjustment.getLabelBound(), minAdjustment.getSnapBound()) > 0){
                        return "Lower label bound must be <= lower snap bound"; 
                    }
                }
            }
        }
        if (maxAdjustment.getRepeatBound() != null){
            if (type.compare(maxAdjustment.getRepeatBound(), intervals.get(intervals.size()-1).max) < 0){
                return "Upper repeat bound must be >= upper bound of first interval"; 
            }
            if (maxAdjustment.getSnapBound() != null){
                if (type.compare(maxAdjustment.getSnapBound(), maxAdjustment.getRepeatBound()) < 0){
                    return "Upper snap bound must be >= upper repeat bound"; 
                }
                if (minAdjustment.getLabelBound() != null){
                    if (type.compare(maxAdjustment.getLabelBound(), maxAdjustment.getSnapBound()) < 0){
                        return "Upper label bound must be >= upper snap bound"; 
                    }
                }
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Group[][] prepareGroups() {

        // Check
        String valid = isValid();
        if (valid != null) {
            throw new IllegalArgumentException(valid);
        }
        
        // Create adjustments
        Adjustment<T> lowerAdjustment = new Adjustment<T>(null, null, null);
        Adjustment<T> upperAdjustment = new Adjustment<T>(null, null, null);
        if (minAdjustment.getRepeatBound() != null) {
            lowerAdjustment.setRepeatBound(minAdjustment.getRepeatBound());
        } else {
            lowerAdjustment.setRepeatBound(intervals.get(0).min);
        }
        if (minAdjustment.getSnapBound() != null) {
            lowerAdjustment.setSnapBound(minAdjustment.getSnapBound());
        } else {
            lowerAdjustment.setSnapBound(lowerAdjustment.getRepeatBound());
        }
        if (minAdjustment.getLabelBound() != null) {
            lowerAdjustment.setLabelBound(minAdjustment.getLabelBound());
        } else {
            lowerAdjustment.setLabelBound(lowerAdjustment.getSnapBound());
        }
        if (maxAdjustment.getRepeatBound() != null) {
            upperAdjustment.setRepeatBound(maxAdjustment.getRepeatBound());
        } else {
            upperAdjustment.setRepeatBound(intervals.get(intervals.size()-1).max);
        }
        if (maxAdjustment.getSnapBound() != null) {
            upperAdjustment.setSnapBound(maxAdjustment.getSnapBound());
        } else {
            upperAdjustment.setSnapBound(upperAdjustment.getRepeatBound());
        }
        if (maxAdjustment.getLabelBound() != null) {
            upperAdjustment.setLabelBound(maxAdjustment.getLabelBound());
        } else {
            upperAdjustment.setLabelBound(upperAdjustment.getSnapBound());
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
        Group[][] result = new Group[data.length][1];
        Map<Group, Group> cache = new HashMap<Group, Group>();
        for (int i=0; i<data.length; i++){

            Group group = getInterval(index, data[i], lowerAdjustment, upperAdjustment);
            
            Group cached = cache.get(group);
            if (cached != null) {
                result[i] = new Group[]{cached};
            }
            else {
                cache.put(group, group);
                result[i] = new Group[]{group};
            }
        }
        return result;
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
