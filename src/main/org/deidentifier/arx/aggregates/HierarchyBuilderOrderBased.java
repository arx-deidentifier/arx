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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * by ordering the data items and merging them according to given fanout parameters
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyBuilderOrderBased<T extends DataType<?>> implements Serializable {

    private static final long serialVersionUID = -2749758635401073668L;

    /**
     * This class represents a fanout parameter
     * @author Fabian Prasser
     */
    public static class Fanout<T extends DataType<?>> implements Serializable {
        
        private static final long serialVersionUID = -3702096214015259350L;
        
        private List<Fanout<T>> children = new ArrayList<Fanout<T>>();
        private int fanout;
        private AggregateFunction<T> function;
        
        public Fanout(int fanout, AggregateFunction<T> function, List<Fanout<T>> children) {
            this.fanout = fanout;
            this.function = function;
            this.children = children;
        }

        public Fanout(int fanout, AggregateFunction<T> function) {
            this(fanout, function, new ArrayList<Fanout<T>>());
        }
        
        public void add(Fanout<T> child){
            this.children.add(child);
        }
    }

    public Hierarchy create(String[] data, T type, Fanout<T> fanout){
        return null;
    }
}
