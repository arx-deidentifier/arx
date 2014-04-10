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
         * @return the labelBound
         */
        public U getLabelBound() {
            return labelBound;
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
         * @param labelBound the labelBound to set
         */
        private void setLabelBound(U labelBound) {
            this.labelBound = labelBound;
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
    public static class Interval<T> extends AbstractGroup {
        
        private static final long serialVersionUID = 5985820929677249525L;

        /** The function*/
        private final AggregateFunction<T> function;
        /** Max is exclusive */
        private final T max;
        /** Min is inclusive */
        private final T min;
        /** The builder*/
        private final HierarchyBuilderGroupingBased<T> builder;
        /** Null for normal intervals, true if <min, false if >max*/
        private final Boolean lower;
        
        /**
         * Constructor for creating out of bounds labels
         * @param b
         */
        private Interval(HierarchyBuilderGroupingBased<T> builder, boolean lower, T value) {
            super(lower ? "<" + ((DataType<T>)builder.getDataType()).format(value) : 
                          ">=" + ((DataType<T>)builder.getDataType()).format(value));
            this.builder = builder;
            this.min = null;
            this.max = null;
            this.function = null;
            this.lower = lower;
        }

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         * @param min
         * @param max
         * @param function
         */
        private Interval(HierarchyBuilderGroupingBased<T> builder, DataType<T> type, T min, T max, AggregateFunction<T> function) {
            super(function.aggregate(new String[]{type.format(min), type.format(max)}));
            this.builder = builder;
            this.min = min;
            this.max = max;
            this.function = function;
            this.lower = false;
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
            if (lower == null) {
                if (other.lower != null) return false;
            } else if (lower != other.lower) return false;
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
            result = prime * result + ((lower == null) ? 0 : lower.hashCode());
            return result;
        }

        @Override
        public String toString(){
            DataType<T> type = (DataType<T>)builder.getDataType();
            return "Interval[min="+type.format(min)+", max="+type.format(max)+", function="+function.toString()+"]";
        }
    }

    /** TODO: Is this parameter OK? */
    private static final int  INDEX_FANOUT     = 2;

    /** SVUID */
    private static final long serialVersionUID = 3663874945543082808L;
    
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
    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderIntervalBased<T> create(String file) throws IOException{
        return create(new File(file));
    }
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
        this.intervals.add(new Interval<T>(this, getDataType(), min, max, 
                           AggregateFunction.forType(getDataType()).createConstantFunction(label)));
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
                return "Gap between " + interval1 + " and " + interval2;
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
    private Interval<T> getInterval(IndexNode index, DataTypeWithRatioScale<T> type, T tValue) {

        // Find interval
        int shift = (int)Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        T offset = type.multiply(type.subtract(index.max, index.min), shift);
        Interval<T> interval = query(index, type.subtract(tValue, offset));

        // Check
        if (interval == null) { throw new IllegalStateException("No interval found for: " + type.format(tValue)); }
        
        // Create first result interval
        T lower = type.add(interval.min, offset);
        T upper = type.add(interval.max, offset);
        return new Interval<T>(this, (DataType<T>)type, lower, upper, interval.function);
    }

    @SuppressWarnings("unchecked")
    private Interval<T> getInterval(IndexNode index, String sValue, 
                                    Adjustment<T> lowerAdjustment, Adjustment<T> upperAdjustment) {

        // Init
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        T tValue = type.parse(sValue);
        Interval<T> interval = getInterval(index, type, tValue);
        
        // Handle < min
        if (type.compare(tValue, intervals.get(0).min) < 0) {
            if (type.compare(tValue, lowerAdjustment.labelBound) < 0){
                throw new IllegalArgumentException("Value out of range: "+sValue + "<" + type.format(lowerAdjustment.labelBound));
            } else if (type.compare(tValue, lowerAdjustment.snapBound) < 0){
                return new Interval<T>(this, true, lowerAdjustment.snapBound);
            } else if (type.compare(tValue, lowerAdjustment.repeatBound) < 0){
                Interval<T> result = getInterval(index, type, lowerAdjustment.repeatBound);
                return new Interval<T>(this, (DataType<T>)type, lowerAdjustment.snapBound, result.max, result.function);
            } 
        }
        
        // Handle > max
        if (type.compare(tValue, intervals.get(intervals.size() - 1).max) >= 0) {
            if (type.compare(tValue, upperAdjustment.labelBound) >= 0){
                throw new IllegalArgumentException("Value out of range: "+sValue + ">" + type.format(upperAdjustment.labelBound));
            } else if (type.compare(tValue, upperAdjustment.snapBound) >= 0){
                return new Interval<T>(this, false, upperAdjustment.snapBound);
            } else if (type.compare(tValue, upperAdjustment.repeatBound) >= 0){
                Interval<T> result = getInterval(index, type, upperAdjustment.repeatBound);
                // Ugly hack to get interval left of the current one
                T point = type.subtract(upperAdjustment.repeatBound, type.multiply(type.subtract(result.max, result.min), 0.5d));
                result = getInterval(index, type, point);
                return new Interval<T>(this, (DataType<T>)type, result.min, upperAdjustment.snapBound, result.function);
            }
        }

        // Adjust
        if (type.compare(interval.min, lowerAdjustment.snapBound) < 0) {
            interval = new Interval<T>(this, (DataType<T>)type, lowerAdjustment.snapBound, interval.max, interval.function);
        }
        if (type.compare(interval.max, upperAdjustment.snapBound) > 0) {
            interval = new Interval<T>(this, (DataType<T>)type, interval.min, upperAdjustment.snapBound, interval.function);
        }
        
        // Return
        return interval;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected AbstractGroup[][] prepareGroups() {

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

        // Prepare
        String[] data = getData();
        List<AbstractGroup[]> result = new ArrayList<AbstractGroup[]>();
        
        // Create first column
        Map<AbstractGroup, AbstractGroup> cache = new HashMap<AbstractGroup, AbstractGroup>();
        AbstractGroup[] first = new AbstractGroup[data.length];
        for (int i=0; i<data.length; i++){
            first[i] = getGroup(cache, getInterval(index, data[i], lowerAdjustment, upperAdjustment));
        }
        result.add(first);
        
        // Create other columns
        List<Group<T>> groups = super.getLevel(0).getGroups();
        if (!groups.isEmpty()) {

            // Prepare
            List<Interval<T>> newIntervals = new ArrayList<Interval<T>>();
            int intervalIndex = 0;
            int multiplier = 0;
            DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>) getDataType();
            T width = type.subtract(intervals.get(intervals.size() - 1).max, intervals.get(0).min);

            // Merge intervals
            for (Group<T> group : groups) {
                
                // Find min and max
                T min = null;
                T max = null;
                for (int i = 0; i < group.getSize(); i++) {
                    Interval<T> current = intervals.get(intervalIndex++);
                    T offset = type.multiply(width, multiplier);
                    T cMin = type.add(current.min, offset);
                    T cMax = type.add(current.max, offset);
                    if (min == null || type.compare(min, cMin) > 0) {
                        min = cMin;
                    }
                    if (max == null || type.compare(max, cMax) < 0) {
                        max = cMax;
                    }
                    if (intervalIndex == intervals.size()) {
                        intervalIndex = 0;
                        multiplier++;
                    }
                }

                // Make sure that max is <= repeat bound
                if (type.compare(max, upperAdjustment.getRepeatBound()) > 0){
                    max = upperAdjustment.getRepeatBound();
                }
                
                // Add interval
                newIntervals.add(new Interval<T>(this, getDataType(), min, max, group.getFunction()));
            }

            // Compute next column
            HierarchyBuilderIntervalBased<T> builder = new HierarchyBuilderIntervalBased<T>(getDataType(),
                                                                                            minAdjustment,
                                                                                            maxAdjustment);
            for (Interval<T> interval : newIntervals) {
                builder.addInterval(interval.min, interval.max, interval.function);
            }

            for (int i=1; i<super.getLevels().size(); i++){
                for (Group<T> sgroup : super.getLevel(i).getGroups()) {
                    builder.getLevel(i-1).addGroup(sgroup.getSize(), sgroup.getFunction());
                }
            }
            
            // Copy data
            builder.prepare(data);
            AbstractGroup[][] columns = builder.prepareGroups();
            for (AbstractGroup[] column : columns) {
                result.add(column);
            }
        } else {
            if (cache.size()>1) {
                AbstractGroup[] column = new AbstractGroup[data.length];
                @SuppressWarnings("serial") AbstractGroup element = new AbstractGroup("*"){};
                for (int i=0; i<column.length; i++){
                    column[i] = element;
                }
                result.add(column);  
            }
        }
        
        // Return
        return result.toArray(new AbstractGroup[0][0]);
    }

    /**
     * Returns the according group from the cache
     * @param cache
     * @param interval
     * @return
     */
    private AbstractGroup getGroup(Map<AbstractGroup, AbstractGroup> cache, Interval<T> interval) {
        AbstractGroup cached = cache.get(interval);
        if (cached != null) {
            return cached;
        } else {
            cache.put(interval, interval);
            return interval;
        }
    }
}
