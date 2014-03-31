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
public class HierarchyBuilderIntervalBased<T extends DataType<?>> extends HierarchyBuilderGroupingBased<T> {
    
    private static final long serialVersionUID = 3663874945543082808L;

    private List<Interval<T>> intervals = new ArrayList<Interval<T>>();
    
    /**
     * Creates a new instance
     * @param type
     */
    public HierarchyBuilderIntervalBased(T type) {
        super(type);
    }

    /**
     * Adds the given fanout
     * @param fanout
     * @return
     */
    public HierarchyBuilderIntervalBased<T> add(Interval<T> fanout) {
        this.intervals.add(fanout);
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
    public static class Interval<T extends DataType<?>> implements Serializable {
        
        private static final long serialVersionUID = 5985820929677249525L;
        
        private String min;
        private String max;
        private AggregateFunction<T> function;

        /**
         * Creates a new instance
         * @param min
         * @param max
         * @param function
         */
        public Interval(String min, String max, AggregateFunction<T> function) {
            this.min = min;
            this.max = max;
            this.function = function;
        }

        /**
         * @return the min
         */
        public String getMin() {
            return min;
        }

        /**
         * @return the max
         */
        public String getMax() {
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
        // TODO Auto-generated method stub
        return null;
    }
}
