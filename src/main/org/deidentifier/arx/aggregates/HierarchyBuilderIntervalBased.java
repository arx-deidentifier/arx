/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

/**
 * This class enables building hierarchies for non-categorical values by mapping them
 * into given intervals.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyBuilderIntervalBased<T> extends HierarchyBuilderGroupingBased<T> { // NO_UCD
    
    /**
     * A constructor to use the load function, see example 24.
     */
    public HierarchyBuilderIntervalBased(){
        super();
    }
    
    /**
     * This class represents an node.
     *
     * @author Fabian Prasser
     */
    public class IndexNode implements Serializable {

        /** SVUID */
        private static final long   serialVersionUID = 5985820929677249525L;

        /** Children. */
        private final IndexNode[]   children;

        /** IsLeaf. */
        private final boolean       isLeaf;

        /** Leafs. */
        private final Interval<T>[] leafs;

        /** Max is exclusive. */
        private final T             max;

        /** Min is inclusive. */
        private final T             min;

        /**
         * Creates a new instance. Min is inclusive, max is exclusive
         *
         * @param min
         * @param max
         * @param children
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
         *
         * @param min
         * @param max
         * @param leafs
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
        
        /**
         * Convert to string.
         *
         * @param prefix
         * @return
         */
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
     * This class represents an interval.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class Interval<T> extends AbstractGroup {

        /** SVUID */
        private static final long                      serialVersionUID = 5985820929677249525L;

        /** The function. */
        private final AggregateFunction<T>             function;

        /** Max is exclusive. */
        private final T                                max;

        /** Min is inclusive. */
        private final T                                min;

        /** The builder. */
        private final HierarchyBuilderGroupingBased<T> builder;

        /** Null for normal intervals, true if <min, false if >max. */
        private final Boolean                          lower;
        
        /**
         * Constructor for creating label for null values
         *
         * @param builder
         */
        private Interval(HierarchyBuilderGroupingBased<T> builder) {
            super(DataType.NULL_VALUE);
            this.builder = builder;
            this.min = null;
            this.max = null;
            this.function = null;
            this.lower = null;
        }
        
        /**
         * Constructor for creating out of bounds labels.
         *
         * @param builder
         * @param lower
         * @param value
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
         *
         * @param builder
         * @param type
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
            this.lower = null;
        }
        
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
         * Returns the function.
         * 
         * @return the function
         */
        public AggregateFunction<T> getFunction() {
            return function;
        }

        /**
         * Returns the maximum.
         * 
         * @return the max (inclusive)
         */
        public T getMax() {
            return max;
        }

        /**
         * Returns the minimum.
         * 
         * @return the min (exclusive)
         */
        public T getMin() {
            return min;
        }

        /**
         * Returns an instance shifted by the given offset
         * @param offset
         * @param type
         * @return
         */
        @SuppressWarnings("unchecked")
        public Interval<T> getShiftedInstance(T offset, DataTypeWithRatioScale<T> type) {
            return new Interval<T>(this.builder,
                                   (DataType<T>) type,
                                   offset == null ? this.min : type.add(this.min, offset),
                                   offset == null ? this.max : type.add(this.max, offset),
                                   function);

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((max == null) ? 0 : max.hashCode());
            result = prime * result + ((min == null) ? 0 : min.hashCode());
            result = prime * result + ((lower == null) ? 0 : lower.hashCode());
            return result;
        }
        
        /**
         * Is this an interval for null values
         * @return
         */
        public boolean isNullInterval() {
            return this.lower == null && this.min == null && this.max == null;
        }
        
        /**
         * Is this an interval representing values that are out of bounds
         * @return
         */
        public boolean isOutOfBound() {
            return lower != null;
        }
        
        /**
         * Is this an interval representing values that are out of the lower bound
         * @return
         */
        public boolean isOutOfLowerBound() {
            if (this.lower == null) {
                throw new IllegalStateException("You may only call this on intervals that represent values that are out of bounds");
            }
            return lower;
        }

        @Override
        public String toString(){
            DataType<T> type = (DataType<T>)builder.getDataType();
            return "Interval[min="+type.format(min)+", max="+type.format(max)+", function="+function.toString()+"]";
        }
    }
    
    /**
     * For each direction, this class encapsulates three bounds. Intervals will be repeated until the
     * repeat-bound is reached. The outmost intervals will than be extended to the snap-bound. Values between
     * the snap-bound and the label-bound will be replaced by an out-of-bounds-label. For values larger than
     * the label-bound exceptions will be raised.
     *
     * @author Fabian Prasser
     * @param <U>
     */
    public static class Range<U> implements Serializable {
        
        /**  SVUID */
        private static final long serialVersionUID = -5385139177770612960L;
        
        /** Snap from. */
        private U repeatBound;
        
        /** Bottom/top coding from. */
        private U snapBound;
        
        /** Minimum / maximum value. */
        private U labelBound;
            
        /**
         * Creates a new instance.
         *
         * @param snapFrom
         * @param bottomTopCodingFrom
         * @param minMaxValue
         */
        public Range(U snapFrom, U bottomTopCodingFrom, U minMaxValue) {

            if (!(snapFrom == null && bottomTopCodingFrom == null && minMaxValue == null)) {
                if (snapFrom == null || bottomTopCodingFrom == null || minMaxValue == null) { 
                    throw new IllegalArgumentException("Value must not be null"); 
                }
            }
            
            this.repeatBound = snapFrom;
            this.snapBound = bottomTopCodingFrom;
            this.labelBound = minMaxValue;
        }

        /**
         * Bottom/top coding will start from this value.
         * 
         * @return
         */
        public U getBottomTopCodingFrom() {
            return snapBound;
        }

        /**
         * If a value is discovered which is smaller/larger than this value
         * an exception will be raised.
         * 
         * @return
         */
        public U getMinMaxValue() {
            return labelBound;
        }

        /**
         * Intervals will snap to lower/higher values from this value.
         * 
         * @return
         */
        public U getSnapFrom() {
            return repeatBound;
        }

        @Override
        public String toString() {
            return "Range [snap=" + repeatBound + ", coding=" + snapBound + ", extreme=" + labelBound + "]";
        }

        /**
         * Bottom/top coding will start from this value.
         * 
         * @param bottomTopCodingValue
         */
        private void setBottomTopCodingFrom(U bottomTopCodingValue) {
            this.snapBound = bottomTopCodingValue;
        }

        /**
         * If a value is discovered which is smaller/larger than this value
         * an exception will be raised.
         * 
         * @param minMaxValue
         */
        private void setMinMaxValue(U minMaxValue) {
            this.labelBound = minMaxValue;
        }

        /**
         * Intervals will snap to lower/higher values from this value.
         * 
         * @param snapValue
         */
        private void setSnapFrom(U snapValue) {
            this.repeatBound = snapValue;
        }
    }

    /** Fanout */
    private static final int               INDEX_FANOUT     = 2;

    /** SVUID. */
    private static final long              serialVersionUID = 3663874945543082808L;
    
    /**
     * Creates a new instance. Snapping is disabled. Repetition is disabled. Bound is determined dynamically.
     *
     * @param <T>
     * @param type
     * @return
     */
    public static <T> HierarchyBuilderIntervalBased<T> create(DataType<T> type) {
        return new HierarchyBuilderIntervalBased<T>(type);
    }
    
    /**
     * Creates a new instance. Data points that are out of range are handled according to the given settings.
     *
     * @param <T>
     * @param type
     * @param lowerRange
     * @param upperRange
     * @return
     */
    public static <T> HierarchyBuilderIntervalBased<T> create(DataType<T> type, Range<T> lowerRange, Range<T> upperRange) {
        return new HierarchyBuilderIntervalBased<T>(type, lowerRange, upperRange);
    }
    
    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
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
            if (ois != null) {
                ois.close();
            }
        }
    }
    
    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderIntervalBased<T> create(String file) throws IOException{
        return create(new File(file));
    }

    /** Adjustment. */
    private Range<T>          lowerRange;

    /** Adjustment. */
    private Range<T>          upperRange;

    /** Defined intervals. */
    private List<Interval<T>> intervals = new ArrayList<Interval<T>>();

    /**
     * Creates a new instance. Snapping is disabled. Repetition is disabled. Bound is determined dynamically.
     * @param type
     */
    protected HierarchyBuilderIntervalBased(DataType<T> type) {
        super(Type.INTERVAL_BASED, type);
        if (!(type instanceof DataTypeWithRatioScale)) {
            throw new IllegalArgumentException("Data type must have a ratio scale");
        }
        this.lowerRange = new Range<T>(null, null, null);
        this.upperRange = new Range<T>(null, null, null);
        this.function = AggregateFunction.forType(type).createIntervalFunction();
    }
    
    /**
     * Creates a new instance. Data points that are out of range are handled according to the given settings.
     * @param type
     * @param lowerRange
     * @param upperRange
     */
    protected HierarchyBuilderIntervalBased(DataType<T> type, Range<T> lowerRange, Range<T> upperRange) {
        super(Type.INTERVAL_BASED, type);
        if (!(type instanceof DataTypeWithRatioScale)) {
            throw new IllegalArgumentException("Data type must have a ratio scale");
        }
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.function = AggregateFunction.forType(type).createIntervalFunction();
    }

    /**
     * Adds an interval. Min is inclusive, max is exclusive. Uses the predefined default aggregate function
     * @param min
     * @param max
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max) {
        if (this.getDefaultFunction() == null) {
            throw new IllegalStateException("No default aggregate function defined");
        }
        checkInterval(getDataType(), min, max);
        this.intervals.add(new Interval<T>(this, getDataType(), min, max, this.getDefaultFunction()));
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
     *
     * @return
     */
    public HierarchyBuilderIntervalBased<T> clearIntervals() {
        this.intervals.clear();
        this.setPrepared(false);
        return this;
    }

    /**
     * Returns all currently defined intervals.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Interval<T>> getIntervals(){
        return (List<Interval<T>>)((ArrayList<T>)this.intervals).clone();
    }

    /**
     * Returns the lower range.
     *
     * @return
     */
    public Range<T> getLowerRange() {
        return this.lowerRange;
    }

    /**
     * Returns the upper range.
     *
     * @return
     */
    public Range<T> getUpperRange() {
        return this.upperRange;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String isValid() {

        String superIsValid = super.isValid();
        if (superIsValid != null) return superIsValid;
        
        if (intervals.isEmpty()) {
            return "No intervals specified";
        }

        for (int i = 1; i < intervals.size(); i++) {
            Interval<T> interval1 = intervals.get(i - 1);
            Interval<T> interval2 = intervals.get(i);

            if (!interval1.getMax().equals(interval2.getMin())) {
                return "Gap between " + interval1 + " and " + interval2;
            }
            
            if (interval1.getMin().equals(interval2.getMin()) &&
                interval1.getMax().equals(interval2.getMax())) {
                return "Repeating intervals " + interval1 + " and " + interval2;
            }
        }
        
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        if (lowerRange.getSnapFrom() != null && 
            upperRange.getSnapFrom() != null && 
            type.compare(lowerRange.getSnapFrom(), upperRange.getSnapFrom()) > 0){
            return "Lower snap bound (" + lowerRange.getSnapFrom() + ") must be < upper snap bound (" + upperRange.getSnapFrom() + ")"; 
        }
        
        if (lowerRange.getBottomTopCodingFrom() != null &&
            lowerRange.getSnapFrom() != null &&
            type.compare(lowerRange.getBottomTopCodingFrom(), lowerRange.getSnapFrom()) > 0){
            return "Bottom coding bound (" + lowerRange.getBottomTopCodingFrom() + ") must be <= lower snap bound (" + lowerRange.getSnapFrom() + ")"; 
        }
        
        if (lowerRange.getMinMaxValue() != null &&
            lowerRange.getBottomTopCodingFrom() != null && 
            type.compare(lowerRange.getMinMaxValue(), lowerRange.getBottomTopCodingFrom()) > 0){
            return "Minimal value (" + lowerRange.getMinMaxValue() + ") must be <= bottom coding bound (" + lowerRange.getBottomTopCodingFrom() + ")"; 
        }
        
        if (upperRange.getSnapFrom() != null &&
            upperRange.getBottomTopCodingFrom() != null && 
            type.compare(upperRange.getBottomTopCodingFrom(), upperRange.getSnapFrom()) < 0){
            return "Upper snap bound (" + upperRange.getBottomTopCodingFrom() + ") must be <= top coding bound (" + upperRange.getSnapFrom() + ")"; 
        }
        
        if (lowerRange.getMinMaxValue() != null && 
            upperRange.getBottomTopCodingFrom() != null && 
            type.compare(upperRange.getMinMaxValue(), upperRange.getBottomTopCodingFrom()) < 0){
            return "Maximum value (" + upperRange.getMinMaxValue() + ") must be >= top coding bound (" + upperRange.getBottomTopCodingFrom() + ")"; 
        }

        return null;
    }

    /**
     * Checks the interval.
     *
     * @param <U>
     * @param type
     * @param min
     * @param max
     */
    private <U> void checkInterval(DataType<U> type, U min, U max){
         
        int cmp = 0;
        try {
            cmp = type.compare(type.format(min), type.format(max));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid data item " + min + " or " + max);
        }
        if (cmp >= 0) throw new IllegalArgumentException("Min (" + min + ") must be lower than max (" + max + ")");
    }

    /**
     * Returns the according group from the cache.
     *
     * @param cache
     * @param interval
     * @return
     */
    @SuppressWarnings("unchecked")
    private Interval<T> getGroup(Map<AbstractGroup, AbstractGroup> cache, Interval<T> interval) {
        AbstractGroup cached = cache.get(interval);
        if (cached != null) {
            return (Interval<T>)cached;
        } else {
            cache.put(interval, interval);
            return interval;
        }
    }

    /**
     * Returns the matching interval.
     *
     * @param index
     * @param type
     * @param tValue
     * @return
     */
    @SuppressWarnings("unchecked")
    private Interval<T> getInterval(IndexNode index, DataTypeWithRatioScale<T> type, T tValue) {

        // Find interval
        int shift = (int)Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        T offset = type.multiply(type.subtract(index.max, index.min), shift);
        T shifted = type.subtract(tValue, offset);
        
        // Fix case when shifted value equals interval-max
        if (type.compare(shifted, index.max) == 0) {
            offset = type.multiply(type.subtract(index.max, index.min), shift + 1);
            shifted = index.min;
        }
        Interval<T> interval = getInterval(index, shifted);

        // Check
        if (interval == null) {
            throw new IllegalStateException("No interval found for: " + type.format(tValue));
        }
        
        // Create first result interval
        T lower = type.add(interval.min, offset);
        T upper = type.add(interval.max, offset);
        return new Interval<T>(this, (DataType<T>)type, lower, upper, interval.function);
    }
    
    /**
     * Performs the index lookup.
     *
     * @param node
     * @param value
     * @return
     */
    private Interval<T> getInterval(IndexNode node, T value) {
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
                    return getInterval(child, value);
                }
            }
        }
        return null;
    }

    /**
     * Returns the matching interval.
     *
     * @param index
     * @param type
     * @param tValue
     * @return
     */
    @SuppressWarnings("unchecked")
    private Interval<T> getIntervalUpperSnap(IndexNode index, DataTypeWithRatioScale<T> type, T tValue) {

        // Find interval
        double shift = Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        T offset = type.multiply(type.subtract(index.max, index.min), shift);
        T value = type.subtract(tValue, offset);
        Interval<T> interval = null;

        for (int j = 0; j < intervals.size(); j++) {
            Interval<T> i = intervals.get(j);
            if (type.compare(i.min, value) <= 0 &&
                type.compare(i.max, value) > 0) {

                // If on lower bound, use next-lower interval
                if (type.compare(value, i.min) == 0) {
                    if (j>0) {
                        
                        // Simply use the next one
                        interval = intervals.get(j-1);
                        break;
                    } else {
                        
                        // Wrap around
                        interval = intervals.get(intervals.size()-1);
                        offset = type.multiply(type.subtract(index.max, index.min), shift-1);
                        break;
                    }
                } else {
                    interval = i;
                    break;
                }
            }
        }
        
        if (interval == null && intervals.size()==1){
            interval = intervals.get(0);
        }

        // Check
        if (interval == null) { 
            throw new IllegalStateException("Internal error. Sorry for that!"); 
        }
        
        
        // Create first result interval
        T lower = type.add(interval.min, offset);
        T upper = type.add(interval.max, offset);
        return new Interval<T>(this, (DataType<T>)type, lower, upper, interval.function);
    }

    /**
     * Adds an interval.
     *
     * @param interval
     */
    protected void addInterval(Interval<T> interval) {
        this.intervals.add(interval);
    }

    /**
     * Returns adjusted ranges.
     *
     * @return Array containing {lower, upper}
     */
    @SuppressWarnings("unchecked")
    protected Range<T>[] getAdjustedRanges() {
        // Create adjustments
        Range<T> tempLower = new Range<T>(null, null, null);
        Range<T> tempUpper = new Range<T>(null, null, null);
        if (lowerRange.getSnapFrom() != null) {
            tempLower.setSnapFrom(lowerRange.getSnapFrom());
        } else {
            tempLower.setSnapFrom(intervals.get(0).min);
        }
        if (lowerRange.getBottomTopCodingFrom() != null) {
            tempLower.setBottomTopCodingFrom(lowerRange.getBottomTopCodingFrom());
        } else {
            tempLower.setBottomTopCodingFrom(tempLower.getSnapFrom());
        }
        if (lowerRange.getMinMaxValue() != null) {
            tempLower.setMinMaxValue(lowerRange.getMinMaxValue());
        } else {
            tempLower.setMinMaxValue(tempLower.getBottomTopCodingFrom());
        }
        if (upperRange.getSnapFrom() != null) {
            tempUpper.setSnapFrom(upperRange.getSnapFrom());
        } else {
            tempUpper.setSnapFrom(intervals.get(intervals.size()-1).max);
        }
        if (upperRange.getBottomTopCodingFrom() != null) {
            tempUpper.setBottomTopCodingFrom(upperRange.getBottomTopCodingFrom());
        } else {
            tempUpper.setBottomTopCodingFrom(tempUpper.getSnapFrom());
        }
        if (upperRange.getMinMaxValue() != null) {
            tempUpper.setMinMaxValue(upperRange.getMinMaxValue());
        } else {
            tempUpper.setMinMaxValue(tempUpper.getBottomTopCodingFrom());
        }
        return new Range[]{tempLower, tempUpper};
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected AbstractGroup[][] prepareGroups() {

        // Check
        String valid = isValid();
        if (valid != null) {
            throw new IllegalArgumentException(valid);
        }
        
        // *******************************************************************************
        // Step 1: Build an index for efficiently mapping a value to its matching interval
        // *******************************************************************************
        
        // Build leaf level index
        ArrayList<IndexNode> nodes = new ArrayList<IndexNode>();
        for (int i = 0, len = intervals.size(); i < len; i += INDEX_FANOUT) {
            int min = i;
            int max = Math.min(i + INDEX_FANOUT - 1, len - 1);

            List<Interval<T>> leafs = new ArrayList<Interval<T>>();
            for (int j = min; j <= max; j++) {
                leafs.add(intervals.get(j));
            }

            nodes.add(new IndexNode(intervals.get(min).min,
                                    intervals.get(max).max,
                                    leafs.toArray(new Interval[leafs.size()])));
        }
        
        // Builder inner nodes
        while (nodes.size() > 1) {
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

        // ****************************************************
        // Step 2: Prepare structures holding relevant data
        // ****************************************************
        
        // Prepare
        String[] data = getData();
        IndexNode index = nodes.get(0);
        List<AbstractGroup[]> result = new ArrayList<AbstractGroup[]>();
        DataTypeWithRatioScale<T> type = (DataTypeWithRatioScale<T>)getDataType();
        Map<AbstractGroup, AbstractGroup> cache = new HashMap<AbstractGroup, AbstractGroup>();
        
        // ****************************************************
        // Step 3: Calculate relevant ranges (snap, repeat etc.
        // ****************************************************
        
        // Create adjustments
        Range<T>[] ranges = getAdjustedRanges();
        Range<T> tempLower = ranges[0];
        Range<T> tempUpper = ranges[1];
        
        // Create snap intervals
        Interval<T> lowerSnap = getInterval(index, type, tempLower.repeatBound);
        lowerSnap = new Interval<T>(this, getDataType(), tempLower.snapBound, lowerSnap.max, lowerSnap.function);
        
        Interval<T> upperSnap = getIntervalUpperSnap(index, type, tempUpper.repeatBound);
        upperSnap = new Interval<T>(this, getDataType(), upperSnap.min, tempUpper.snapBound, upperSnap.function);
        
        // Overlapping snaps -> one interval
        if (type.compare(lowerSnap.max, upperSnap.min)>0) {
            // We could use lowerSnap.function or upperSnap.function
            lowerSnap = new Interval<T>(this, getDataType(), lowerSnap.min, upperSnap.max, lowerSnap.function);
            upperSnap = lowerSnap;
        }

        // ****************************************************
        // Step 4: Create first column of hierarchy
        // ****************************************************
        Interval<T>[] first = new Interval[data.length];
        for (int i = 0; i < data.length; i++) {
            
            // Step 4.1 Parse value
            T value = type.parse(data[i]);
            Interval<T> interval;
            
            // Step 4.2 Empty value
            if (value == null) {
                interval = new Interval<T>(this);
                
            // Step 4.3 Error if < lower
            } else if (type.compare(value, tempLower.labelBound) < 0) {
                throw new IllegalArgumentException("Data item " + type.format(value) + " is < minim value (" + type.format(tempLower.labelBound) + ")");
                
            // Step 4.4 Bottom coding    
            } else if (type.compare(value, tempLower.snapBound) < 0) {
                interval = new Interval<T>(this, true, tempLower.snapBound);
                
            // Step 4.5 Error if > upper
            } else if (type.compare(value, tempUpper.labelBound) >= 0) {
                throw new IllegalArgumentException("Data item " + type.format(value)+ " is >= maximum value (" + type.format(tempUpper.labelBound) + ")");
                
            // Step 4.6 Top coding
            } else if (type.compare(value, tempUpper.snapBound) >= 0) {
                interval = new Interval<T>(this, false, tempUpper.snapBound);
            
            // Step 4.7 Find interval using index
            } else {
                interval = getInterval(index, type, value);    
            }
            
            // If found
            if (interval.min != null && interval.max != null){
                
                // Step 4.8 Snap to lower
                if (type.compare(interval.min, lowerSnap.max) < 0){
                    interval = lowerSnap;
                    
                // Step 4.9 Snap to upper
                } else if (type.compare(interval.max, upperSnap.min) > 0){
                    interval = upperSnap;
                }
            }
            
            // Make sure that identical intervals are represented by the same object
            first[i] = getGroup(cache, interval);
        }
        result.add(first);
        
        // Clean
        index = null;
        
        // ****************************************************
        // Step 5: Prepare recursion to generate other columns
        // ****************************************************
        
        // Create other columns
        List<Group<T>> groups = new ArrayList<Group<T>>();
        if (!super.getLevels().isEmpty()) groups = super.getLevels().get(0).getGroups();
        if (cache.size() > 1 && !groups.isEmpty()) {

            // Prepare
            List<Interval<T>> newIntervals = new ArrayList<>();
            
            // Calculate number of intervals grouped together
            int numGroupedIntervals = 0;
            for (Group<T> group : groups) {
                numGroupedIntervals += group.getSize();
            }
            
            // ---------------------------------------------------------------------------------
            // Case 1: More grouped intervals or same number of grouped intervals than intervals
            // ---------------------------------------------------------------------------------
            if (numGroupedIntervals >= intervals.size()) {
            
                // In this case we need to shift intervals to cover the complete range
                T width = type.subtract(intervals.get(intervals.size() - 1).max, intervals.get(0).min);
                T offset = null;
                
                // Indices
                int iInterval = 0; // Index into intervals
                for (Group<T> group : groups) { // For each group
                    
                    // Collect intervals to merge for this group
                    Set<Interval<T>> toMerge = new HashSet<>();
                    while (toMerge.size() < group.getSize()) {
                        
                        // Add interval
                        toMerge.add(intervals.get(iInterval).getShiftedInstance(offset, type));
                        
                        // Next interval round robin
                        iInterval++;
                        if (iInterval == intervals.size()) {
                            iInterval = 0;
                            offset = offset == null ? width : type.add(offset, width);
                        }
                    }
                    
                    // Merge intervals
                    if (!toMerge.isEmpty()) {                    
                        T min = null;
                        T max = null;
                        for (Interval<T> interval : toMerge) {
                            if (min == null || type.compare(min, interval.min) > 0) {
                                min = interval.min;
                            }
                            if (max == null || type.compare(max, interval.max) < 0) {
                                max = interval.max;
                            }
                        }
                        
                        // Add merged interval
                        newIntervals.add(new Interval<T>(this, getDataType(), min, max, group.getFunction()));
                    }
                }
            
            // ---------------------------------------------
            // Case 2: Less grouped intervals than intervals
            // ---------------------------------------------
            } else {
                
                // Index into intervals
                int iInterval = 0;
                
                // Until we have processed all intervals
                while (iInterval < intervals.size()) {
                    
                    // For each group
                    for (Group<T> group : groups) {
                        
                        // Collect intervals to merge for this group
                        Set<Interval<T>> toMerge = new HashSet<>();
                        while (toMerge.size() < group.getSize()) {
                            
                            // Add interval
                            toMerge.add(intervals.get(iInterval));
                            
                            // Next interval
                            iInterval++;
                            if (iInterval == intervals.size()) {
                                break;
                            }
                        }
                        
                        // Merge intervals
                        if (!toMerge.isEmpty()) {                    
                            T min = null;
                            T max = null;
                            for (Interval<T> interval : toMerge) {
                                if (min == null || type.compare(min, interval.min) > 0) {
                                    min = interval.min;
                                }
                                if (max == null || type.compare(max, interval.max) < 0) {
                                    max = interval.max;
                                }
                            }
                            
                            // Add merged interval
                            newIntervals.add(new Interval<T>(this, getDataType(), min, max, group.getFunction()));
                        }
                    }
                }
            }
            
            // ****************************************************
            // Step 6: Recursive call to separate builder
            // ****************************************************

            // Compute next column
            HierarchyBuilderIntervalBased<T> builder = new HierarchyBuilderIntervalBased<T>(getDataType(),
                                                                                            tempLower,
                                                                                            tempUpper);
            for (Interval<T> interval : newIntervals) {
                builder.addInterval(interval.min, interval.max, interval.function);
            }

            for (int i = 1; i < super.getLevels().size(); i++) {
                for (Group<T> sgroup : super.getLevel(i).getGroups()) {
                    builder.getLevel(i-1).addGroup(sgroup.getSize(), sgroup.getFunction());
                }
            }
            
            // Copy data
            builder.prepare(data);

            // ****************************************************
            // Step 7: Copy data over
            // ****************************************************
            AbstractGroup[][] columns = builder.getPreparedGroups();
            for (AbstractGroup[] column : columns) {
                result.add(column);
            }
        
        // Add stars if out of levels but still no root node
        } else {
            
            if (cache.size() > 1) {
                AbstractGroup[] column = new AbstractGroup[data.length];
                @SuppressWarnings("serial")
                AbstractGroup element = new AbstractGroup(DataType.ANY_VALUE) {};
                for (int i = 0; i < column.length; i++) {
                    column[i] = element;
                }
                result.add(column);
            }
        }
        
        // Return
        return result.toArray(new AbstractGroup[0][0]);
    }

    /**
     * Sets the data array.
     *
     * @param data
     */
    protected void setData(String[] data){
        super.setData(data);
    }
    
    /**
     * Sets the groups on higher levels of the hierarchy.
     *
     * @param levels
     */
    protected void setLevels(List<Level<T>> levels) {
        super.setLevels(levels);
    }
}
