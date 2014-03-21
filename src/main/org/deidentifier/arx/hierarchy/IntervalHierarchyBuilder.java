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
package org.deidentifier.arx.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies for non-categorical values by mapping them
 * into given intervals
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class IntervalHierarchyBuilder<T extends DataType<?>> implements Serializable {
    
    private static final long serialVersionUID = 3663874945543082808L;

    /**
     * This class represents an interval
     * @author Fabian Prasser
     */
    public static class Interval<T extends DataType<?>> implements Serializable {
        
        private static final long serialVersionUID = 5985820929677249525L;
        
        private List<Interval<T>> children;
        private T type;
        private String min;
        private String max;
        private AggregateFunction<T> function;

        public Interval(String min, String max, T type, AggregateFunction<T> function, List<Interval<T>> children) {
            this.min = min;
            this.max = max;
            this.type = type;
            this.function = function;
            this.children = children;
        }

        public Interval(String min, String max, T type, AggregateFunction<T> function) {
            this(min, max, type, function, new ArrayList<Interval<T>>());
        }
        
        public void add(Interval<T> child){
            this.children.add(child);
        }
    }
    
    public Hierarchy create(String[] data, T type, Interval<T> interval){
        return null;
    }
}
