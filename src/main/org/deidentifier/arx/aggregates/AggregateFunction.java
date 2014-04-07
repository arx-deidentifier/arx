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

import java.text.ParseException;
import java.util.Arrays;

import org.deidentifier.arx.DataType;

/**
 * This abstract class represents an aggregate function
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class AggregateFunction<T> {
    /**
     * An aggregate function that returns a constant value
     * 
     * @author Fabian Prasser
     */
    public static class GenericConstant<T> extends AggregateFunction<T> {

        private String value;
        
        private GenericConstant(DataType<T> type, String value) {
            super(type);
            this.value = value;
        }

        @Override
        public String aggregate(String[] values) {
            return value;
        }
        
        @Override
        public String toString(){
            return "Constant[value="+value+"]";
        }
    }

    /**
     * An aggregate function that returns a set of all data values
     * 
     * @author Fabian Prasser
     */
    public static class GenericSet<T> extends AggregateFunction<T> {

        private GenericSet(DataType<T> type) {
            super(type);
        }

        @Override
        public String aggregate(String[] values) {
            StringBuilder b = new StringBuilder();
            b.append("{");
            for (int i = 0; i < values.length; i++) {
                b.append(values[i]);
                if (i < values.length - 1) {
                    b.append(", ");
                }
            }
            b.append("}");
            return b.toString();
        }
        
        @Override
        public String toString(){
            return "Set";
        }
    }

    /**
     * An aggregate function that returns a set of the prefixes of the data values
     * 
     * @author Fabian Prasser
     */
    public static class GenericSetOfPrefixes<T> extends AggregateFunction<T> {

        private int length;

        private GenericSetOfPrefixes(DataType<T> type, int length) {
            super(type);
            this.length = length;
        }

        @Override
        public String aggregate(String[] values) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                int size = Math.min(length, values[i].length());
                b.append(values[i].substring(0, size));
                if (i < values.length - 1) {
                    b.append("-");
                }
            }
            return b.toString();
        }
        
        @Override
        public String toString(){
            return "SetOfPrefixes[length="+length+"]";
        }
    };
    
    /**
     * An aggregate function that returns an interval consisting of the 
     * first and the last element following the predefined order 
     * 
     * @author Fabian Prasser
     */
    public static class GenericBounds<T> extends AggregateFunction<T> {

        private GenericBounds(DataType<T> type) {
            super(type);
        }

        @Override
        public String aggregate(String[] values) {
            return new StringBuilder().append("[")
                    .append(values[0])
                    .append(", ")
                    .append(values[values.length - 1])
                    .append("]")
                    .toString();
        }
        
        @Override
        public String toString(){
            return "Bounds";
        }
    };

    /**
     * An aggregate function that returns a common prefix
     * 
     * @author Fabian Prasser
     */
    public static class GenericCommonPrefix<T> extends AggregateFunction<T> {
        
        private Character redaction;

        private GenericCommonPrefix(DataType<T> type, final Character redaction) {
            super(type);
            this.redaction = redaction;
        }

        @Override
        public String aggregate(String[] values) {

            // Determine length
            int length = Integer.MIN_VALUE;
            if (redaction != null) {
                for (String s : values) {
                    length = Math.max(length, s.length());
                }
            }

            // Determine largest common prefix
            int position = 0;
            boolean found = true;
            outer: while (found) {
                char c = values[0].charAt(position);
                for (int i = 1; i < values.length; i++) {
                    if (values[i].charAt(position) != c) {
                        found = false;
                        break outer;
                    }
                }
                position++;
            }
            position--;
            char[] result;
            if (redaction != null) {
                result = new char[length];
                Arrays.fill(result, position + 1, length, redaction);
            } else {
                result = new char[position + 1];
            }
            for (int i = 0; i <= position; i++) {
                result[i] = values[0].charAt(i);
            }
            return new String(result);
        }
        
        @Override
        public String toString(){
            return "CommonPrefix";
        }
    }
    
    /**
     * An aggregate function that returns an interval [min, max] 
     * @author Fabian Prasser
     *
     * @param <T>
     */
    public static class GenericInterval<T> extends AggregateFunction<T> {
        
        private final boolean lowerIncluded;
        private final boolean upperIncluded;
        
        private GenericInterval(DataType<T> type, boolean lowerIncluded, boolean upperIncluded) {
            super(type);
            this.lowerIncluded = lowerIncluded;
            this.upperIncluded = upperIncluded;
        }

        @Override
        public String aggregate(String[] values) {

            String min = null;
            String max = null;
            for (String value : values) {
                try {
                    if (min == null || type.compare(min, value) > 0){
                        min = value;
                    }
                    if (max == null || type.compare(max, value) < 0){
                        max = value;
                    }
                } catch (NumberFormatException | ParseException e) {
                    throw new RuntimeException(e);
                }
            }
          return new StringBuilder().append(lowerIncluded ? "[" : "]")
                                    .append(min)
                                    .append(", ")
                                    .append(max)
                                    .append(upperIncluded ? "]" : "[")
                                    .toString();
        }
        
        @Override
        public String toString(){
            return "Interval";
        }
    }

    /** The data type*/
    protected DataType<T> type;
    
    /**
     * Constructor
     * @param type
     */
    protected AggregateFunction(DataType<T> type){
        this.type = type;
    }

    /** 
     * This function returns an aggregate value
     * 
     * @param values
     * @param type
     * @return
     */
    public abstract String aggregate (String[] values);
    
    @Override
    public abstract String toString ();

    /** 
     * An aggregate function that returns a set of all data values 
     * @param <V>
     */
    public static final <V> AggregateFunction<V> SET(DataType<V> type) {
        return new GenericSet<V>(type);
    }
    
    /** 
     * An aggregate function that returns a set of the prefixes of the data values. Length is 1
     * @param <V>
     */
    public static final <V> AggregateFunction<V> SET_OF_PREFIXES(DataType<V> type) {
        return new GenericSetOfPrefixes<V>(type, 1);
    }

    /** 
     * An aggregate function that returns a set of the prefixes of the data values
     * @param <V>
     * @param length
     */
    public static final <V> AggregateFunction<V> SET_OF_PREFIXES(DataType<V> type, int length) {
        return new GenericSetOfPrefixes<V>(type, length);
    }
    
    /**
     * An aggregate function that returns a common prefix. The remaining characters will be redacted with
     * the given character
     * 
     * @param redaction
     */
    public static final <V> AggregateFunction<V> COMMON_PREFIX(DataType<V> type, Character redaction) {
        return new GenericCommonPrefix<V>(type, redaction);
    }

    /**
     * An aggregate function that returns a common prefix.
     * 
     */
    public static final <V> AggregateFunction<V> COMMON_PREFIX(DataType<V> type) {
        return new GenericCommonPrefix<V>(type, null);
    }

    /**
     * An aggregate function that returns an interval consisting of the 
     * first and the last element following the predefined order 
     */
    public static final <V> AggregateFunction<V> BOUNDS(DataType<V> type) {
        return new GenericBounds<V>(type);
    }

    /**
     * An aggregate function that returns an interval [min, max] 
     */
    public static final <V> AggregateFunction<V> INTERVAL(DataType<V> type) {
        return new GenericInterval<V>(type, true, true);
    }

    /**
     * An aggregate function that returns an interval [min, max] 
     */
    public static final <V> AggregateFunction<V> INTERVAL(DataType<V> type, boolean lowerIncluded, boolean upperIncluded) {
        return new GenericInterval<V>(type, lowerIncluded, upperIncluded);
    }

    /**
     * An aggregate function that returns a constant value
     */
    public static final <V> AggregateFunction<V> CONSTANT(DataType<V> type, String value) {
        return new GenericConstant<V>(type, value);
    }
}
