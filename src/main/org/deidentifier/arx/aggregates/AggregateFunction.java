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
import java.text.ParseException;
import java.util.Arrays;

import org.deidentifier.arx.DataType;

/**
 * This abstract class represents an aggregate function
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class AggregateFunction<T> implements Serializable{
    
    private static final long serialVersionUID = 3803318906010996154L;

    /**
     * A builder for aggregate functions
     * 
     * @author Fabian Prasser
     *
     */
    public static class AggregateFunctionBuilder<T> {

        private DataType<T> type;
        
        /**
         * Creates a new instance
         * @param type
         */
        private AggregateFunctionBuilder(DataType<T> type){
            this.type = type;
        }
        

        /**
         * An aggregate function that returns an interval consisting of the 
         * first and the last element following the predefined order 
         */
        public final AggregateFunction<T> createBoundsFunction() {
            return new GenericBounds<T>(type);
        }
        
        /**
         * An aggregate function that returns a constant value
         */
        public final AggregateFunction<T> createConstantFunction(String value) {
            return new GenericConstant<T>(type, value);
        }

        /**
         * An aggregate function that returns an interval [min, max] 
         */
        public final AggregateFunction<T> createIntervalFunction() {
            return new GenericInterval<T>(type, true, true);
        }
        
        /**
         * An aggregate function that returns an interval [min, max] 
         */
        public final AggregateFunction<T> createIntervalFunction(boolean lowerIncluded, boolean upperIncluded) {
            return new GenericInterval<T>(type, lowerIncluded, upperIncluded);
        }

        /**
         * An aggregate function that returns a common prefix.
         * 
         */
        public final AggregateFunction<T> createPrefixFunction() {
            return new GenericCommonPrefix<T>(type, null);
        }

        /**
         * An aggregate function that returns a common prefix. The remaining characters will be redacted with
         * the given character
         * 
         * @param redaction
         */
        public final AggregateFunction<T> createPrefixFunction(Character redaction) {
            return new GenericCommonPrefix<T>(type, redaction);
        }

        /** 
         * An aggregate function that returns a set of all data values 
         * @param <V>
         */
        public final AggregateFunction<T> createSetFunction() {
            return new GenericSet<T>(type);
        }

        /** 
         * An aggregate function that returns a set of the prefixes of the data values. Length is 1
         * @param <T>
         */
        public final AggregateFunction<T> createSetOfPrefixesFunction() {
            return new GenericSetOfPrefixes<T>(type, 1);
        }

        /** 
         * An aggregate function that returns a set of the prefixes of the data values
         * @param <T>
         * @param length
         */
        public final AggregateFunction<T> createSetOfPrefixesFunction(int length) {
            return new GenericSetOfPrefixes<T>(type, length);
        }
    }
    
    /**
     * An aggregate function that has a parameter
     * 
     * @author Fabian Prasser
     */
    public static abstract class AggregateFunctionWithParameter<T> extends AggregateFunction<T>{
        
        private static final long serialVersionUID = 1L;
        /** Creates a new instance*/
        protected AggregateFunctionWithParameter(DataType<T> type) { super(type); }
        /** Returns whether the function accepts this parameter*/
        public abstract boolean acceptsParameter(String parameter);
        /** Returns the parameter*/
        public abstract String getParameter();
        /** Creates a new instance with the given parameter*/
        public abstract AggregateFunctionWithParameter<T> newInstance(String parameter);
    }
    
    /**
     * An aggregate function that returns an interval consisting of the 
     * first and the last element following the predefined order 
     * 
     * @author Fabian Prasser
     */
    public static class GenericBounds<T> extends AggregateFunction<T> {

        private static final long serialVersionUID = -8884657842545379206L;

        /**
         * Creates a new instance
         * @param type
         */
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
        public String toLabel() {
            return "Bounding values";
        }

        @Override
        public String toString(){
            return "Bounds";
        }
    }
    
    /**
     * An aggregate function that returns a common prefix
     * 
     * @author Fabian Prasser
     */
    public static class GenericCommonPrefix<T> extends AggregateFunctionWithParameter<T> {
        
        private static final long serialVersionUID = 526809670467390820L;
        private Character redaction;

        /**
         * Creates a new instance
         * @param type
         * @param redaction
         */
        private GenericCommonPrefix(DataType<T> type, final Character redaction) {
            super(type);
            this.redaction = redaction;
        }

        @Override
        public boolean acceptsParameter(String parameter) {
            return parameter == null || parameter.length()<=1;
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
        public String getParameter() {
            if (redaction == null) return null;
            else return String.valueOf(redaction);
        }

        @Override
        public AggregateFunctionWithParameter<T> newInstance(String parameter) {
            if (parameter == null || parameter.length()==0) return new GenericCommonPrefix<T>(this.type, null);
            else return new GenericCommonPrefix<T>(this.type, parameter.toCharArray()[0]);
        }

        @Override
        public String toLabel() {
            return "Common prefix";
        }

        @Override
        public String toString(){
            if (redaction == null){
                return "CommonPrefix";
            } else {
                return "CommonPrefix[redaction="+redaction+"]";
            }
        }
    }

    /**
     * An aggregate function that returns a constant value
     * 
     * @author Fabian Prasser
     */
    public static class GenericConstant<T> extends AggregateFunctionWithParameter<T> {

        private static final long serialVersionUID = -8995068916108125096L;
        private String value;
        
        /**
         * Creates a new instance
         * @param type
         * @param value
         */
        private GenericConstant(DataType<T> type, String value) {
            super(type);
            this.value = value;
        }

        @Override
        public boolean acceptsParameter(String parameter) {
            return parameter != null;
        }
        
        @Override
        public String aggregate(String[] values) {
            return value;
        }

        @Override
        public String getParameter() {
            return value;
        }

        @Override
        public AggregateFunctionWithParameter<T> newInstance(String parameter) {
            return new GenericConstant<T>(this.type, parameter);
        }

        @Override
        public String toLabel() {
            return "Constant value";
        }

        @Override
        public String toString(){
            return "Constant[value="+value+"]";
        }
    }

    /**
     * An aggregate function that returns an interval [min, max] 
     * @author Fabian Prasser
     *
     * @param <T>
     */
    public static class GenericInterval<T> extends AggregateFunction<T> {
        
        private static final long serialVersionUID = -5182521036467379023L;
        private final boolean lowerIncluded;
        private final boolean upperIncluded;
        
        /**
         * Creates a new instance
         * @param type
         * @param lowerIncluded
         * @param upperIncluded
         */
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
        public String toLabel() {
            return "Interval";
        }

        @Override
        public String toString(){
            return "Interval";
        }
    };
    
    /**
     * An aggregate function that returns a set of all data values
     * 
     * @author Fabian Prasser
     */
    public static class GenericSet<T> extends AggregateFunction<T> {

        private static final long serialVersionUID = -4029191421720743653L;

        /**
         * Creates a new instance
         * @param type
         */
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
        public String toLabel() {
            return "Set of values";
        }

        @Override
        public String toString(){
            return "Set";
        }
    };

    /**
     * An aggregate function that returns a set of the prefixes of the data values
     * 
     * @author Fabian Prasser
     */
    public static class GenericSetOfPrefixes<T> extends AggregateFunctionWithParameter<T> {

        private static final long serialVersionUID = -4164142474804296433L;
        private int length;

        /**
         * Creates a new instance
         * @param type
         * @param length
         */
        private GenericSetOfPrefixes(DataType<T> type, int length) {
            super(type);
            this.length = length;
        }

        @Override
        public boolean acceptsParameter(String parameter) {
            try {
                return Integer.parseInt(parameter) > 0;
            } catch (Exception e) {
                return false;
            }
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
        public String getParameter() {
            return String.valueOf(length);
        }

        @Override
        public AggregateFunctionWithParameter<T> newInstance(String parameter) {
            return new GenericSetOfPrefixes<T>(this.type, Integer.parseInt(parameter));
        }

        @Override
        public String toLabel() {
            return "Set of prefixes";
        }

        @Override
        public String toString(){
            return "SetOfPrefixes[length="+length+"]";
        }
    }
    
    /**
     * Returns a builder for the given data type
     * @param type
     * @return
     */
    public static <T> AggregateFunctionBuilder<T> forType(DataType<T> type){
        return new AggregateFunctionBuilder<T>(type);
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

    /** 
     * Returns whether the function accepts a parameter
     * 
     * @param values
     * @param type
     * @return
     */
    public boolean hasParameter() {
        return (this instanceof AggregateFunctionWithParameter);
    }
    
    /**
     * Returns a label
     * @return
     */
    public abstract String toLabel();

    @Override
    public abstract String toString ();
}
