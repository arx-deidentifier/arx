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

import java.text.ParseException;

import org.deidentifier.arx.DataType;

/**
 * This abstract class represents an aggregate function
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class AggregateFunction<T extends DataType<?>> {

    /** 
     * This function returns an aggregate value
     * 
     * @param values
     * @param type
     * @return
     */
    public abstract String aggregate (String[] values, T type);
    
    /** 
     * An aggregate function that returns a set of all data values 
     */
    public static final AggregateFunction<DataType<?>> SET = new AggregateFunction<DataType<?>>(){
        @Override
        public String aggregate(String[] values, DataType<?> type) {
            StringBuilder b = new StringBuilder();
            b.append("{");
            for (int i=0; i<values.length; i++){
                b.append(values[i]);
                if (i < values.length) {
                    b.append(", ");
                }
            }
            b.append("}");
            return b.toString();
        }
    };

    /** 
     * An aggregate function that returns a set of the prefixes of the data values 
     */
    public static final AggregateFunction<DataType<?>> PREFIX(final int length){
        
        return new AggregateFunction<DataType<?>>(){
            @Override
            public String aggregate(String[] values, DataType<?> type) {
                StringBuilder b = new StringBuilder();
                for (int i=0; i<values.length; i++){
                    int size = Math.min(length, values[i].length());
                    b.append(values[i].substring(0, size));
                    if (i < values.length) {
                        b.append("-");
                    }
                }
                return b.toString();
            }
        };
    }
    
    /** 
     * An aggregate function that returns a set of the prefixes 
     * of length 1 of the data values 
     */
    public static final AggregateFunction<DataType<?>> PREFIX = PREFIX(1);

    /** 
     * An aggregate function that returns an interval [min, max] 
     */
    public static final AggregateFunction<DataType<?>> INTERVAL = new AggregateFunction<DataType<?>>(){
        @Override
        public String aggregate(String[] values, DataType<?> type) {
            String min = null;
            String max = null;
            for (String value : values) {
                try {
                    if (min == null || type.compare(min, value) < 0){
                        min = value;
                    }
                    if (max == null || type.compare(max, value) > 0){
                        max = value;
                    }
                } catch (NumberFormatException | ParseException e) {
                    throw new RuntimeException(e);
                }
            }
          return new StringBuilder().append("[")
                                    .append(min)
                                    .append(", ")
                                    .append(max)
                                    .append("]")
                                    .toString();
        }
    };

    /** 
     * An aggregate function that returns an interval consisting of the 
     * first and the last element following the predefined order 
     */
    public static final AggregateFunction<DataType<?>> BOUNDS = new AggregateFunction<DataType<?>>(){
        @Override
        public String aggregate(String[] values, DataType<?> type) {
              return new StringBuilder().append("[")
                                        .append(values[0])
                                        .append(", ")
                                        .append(values[values.length - 1])
                                        .append("]")
                                        .toString();
        }
    };
}
