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
    
    private static final long serialVersionUID = 3663874945543082808L;

    private AggregateFunction<T> function = null;
    private List<Interval<T>> intervals = new ArrayList<Interval<T>>();
    private T min;
    private T max;
    private T epsilon;
    private DynamicAdjustment adjustment = DynamicAdjustment.SNAP_TO_BOUNDS;

    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     * @param epsilon
     * @param adjustment
     */
    public HierarchyBuilderIntervalBased(T min, T max, DataType<T> type, T epsilon, DynamicAdjustment adjustment) {
        super(type);
        this.min = min;
        this.max = max;
        this.epsilon = epsilon;
        this.adjustment = adjustment;
    }

    /**
     * Creates a new instance
     * @param min
     * @param max
     * @param type
     */
    public HierarchyBuilderIntervalBased(T min, T max, DataType<T> type) {
        super(type);
        this.min = min;
        this.max = max;
    }
    
    /**
     * Defines an aggregate function to be used by all intervals
     * @param function
     */
    public void setAggregateFunction(AggregateFunction<T> function) {
        this.function = function;
    }

    /**
     * Adds an interval. Min is exclusive, max is inclusive
     * @param min
     * @param max
     * @param function
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max, AggregateFunction<T> function) {
        this.intervals.add(new Interval<T>(min, max, function));
        return this;
    }

    /**
     * Adds an interval. Min is exclusive, max is inclusive. Uses the predefined default aggregate function
     * @param min
     * @param max
     * @return
     */
    public HierarchyBuilderIntervalBased<T> addInterval(T min, T max) {
        if (this.function == null) {
            throw new IllegalStateException("No default aggregate function defined");
        }
        this.intervals.add(new Interval<T>(min, max, this.function));
        return this;
    }

    /**
     * Removes the given fanout
     * @param fanout
     * @return
     */
    public HierarchyBuilderIntervalBased<T> remove(Interval<T> fanout) {
        this.intervals.remove(fanout);
        return this;
    }

    /**
     * This class represents an interval
     * @author Fabian Prasser
     */
    public class Interval<T> implements Serializable {
        
        private static final long serialVersionUID = 5985820929677249525L;
        
        /** Min is exclusive */
        private T min;
        /** Max is inclusive */
        private T max;
        /** The aggregate function*/
        private AggregateFunction<T> function;

        /**
         * Creates a new instance. Min is exclusive, max is inclusive
         * @param min
         * @param max
         * @param function
         */
        public Interval(T min, T max, AggregateFunction<T> function) {
            this.min = min;
            this.max = max;
            this.function = function;
        }

        /**
         * @return the min (exclusive)
         */
        public T getMin() {
            return min;
        }

        /**
         * @return the max (inclusive)
         */
        public T getMax() {
            return max;
        }

        /**
         * @return the function
         */
        public AggregateFunction<T> getFunction() {
            return function;
        }
    }
    
    @Override
    protected int getBaseLevel() {
        return 0;
    }

    @Override
    protected String getBaseLabel(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void prepare() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected String internalIsValid() {
        return null;
//        for (int i=0; i<Interval)
    }
}
