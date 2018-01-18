/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;

/**
 * This abstract class represents an aggregate function.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class AggregateFunction<T> implements Serializable{
    
    /**
     * A builder for aggregate functions.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class AggregateFunctionBuilder<T> {

        /**  SVUID */
        private DataType<T> type;
        
        /**
         * Creates a new instance.
         *
         * @param type
         */
        private AggregateFunctionBuilder(DataType<T> type){
            this.type = type;
        }

        /**
         * An aggregate function that returns a the arithmetic mean, if it can be computed, NULL otherwise.
         *
         * @return
         */
        public final AggregateFunction<T> createArithmeticMeanFunction() {
            return new GenericArithmeticMean<T>(type);
        }

        /**
         * An aggregate function that returns a the arithmetic mean of min & max, if it can be computed, NULL otherwise.
         *
         * @return
         */
        public final AggregateFunction<T> createArithmeticMeanOfBoundsFunction() {
            return new GenericArithmeticMeanOfBounds<T>(type);
        }
        
        /**
         * An aggregate function that returns an interval consisting of the
         * first and the last element following the predefined order.
         *
         * @return
         */
        public final AggregateFunction<T> createBoundsFunction() {
            return new GenericBounds<T>(type);
        }
        
        /**
         * An aggregate function that returns a constant value.
         *
         * @param value
         * @return
         */
        public final AggregateFunction<T> createConstantFunction(String value) {
            return new GenericConstant<T>(type, value);
        }
        
        /**
         * An aggregate function that returns a the geometric mean, if it can be computed, NULL otherwise.
         *
         * @return
         */
        public final AggregateFunction<T> createGeometricMeanFunction() {
            return new GenericGeometricMean<T>(type);
        }

        /**
         * An aggregate function that returns a the geometric mean of min & max, if it can be computed, NULL otherwise.
         *
         * @return
         */
        public final AggregateFunction<T> createGeometricMeanOfBoundsFunction() {
            return new GenericGeometricMeanOfBounds<T>(type);
        }
        /**
         * An aggregate function that returns an interval [min, max].
         *
         * @return
         */
        public final AggregateFunction<T> createIntervalFunction() {
            return new GenericInterval<T>(type, true, true);
        }
        
        /**
         * An aggregate function that returns an interval [min, max].
         *
         * @param lowerIncluded
         * @param upperIncluded
         * @return
         */
        public final AggregateFunction<T> createIntervalFunction(boolean lowerIncluded, boolean upperIncluded) {
            return new GenericInterval<T>(type, lowerIncluded, upperIncluded);
        }

        /**
         * An aggregate function that returns a common prefix.
         *
         * @return
         */
        public final AggregateFunction<T> createPrefixFunction() {
            return new GenericCommonPrefix<T>(type, null);
        }

        /**
         * An aggregate function that returns a common prefix. The remaining characters will be redacted with
         * the given character
         *
         * @param redaction
         * @return
         */
        public final AggregateFunction<T> createPrefixFunction(Character redaction) {
            return new GenericCommonPrefix<T>(type, redaction);
        }

        /**
         * 
         * An aggregate function that returns a set of all data values .
         *
         * @return
         */
        public final AggregateFunction<T> createSetFunction() {
            return new GenericSet<T>(type);
        }

        /**
         * 
         * An aggregate function that returns a set of the prefixes of the data values. Length is 1
         *
         * @return
         */
        public final AggregateFunction<T> createSetOfPrefixesFunction() {
            return new GenericSetOfPrefixes<T>(type, 1);
        }

        /**
         * 
         * An aggregate function that returns a set of the prefixes of the data values.
         *
         * @param length
         * @return
         */
        public final AggregateFunction<T> createSetOfPrefixesFunction(int length) {
            return new GenericSetOfPrefixes<T>(type, length);
        }
    }

    /**
     * An aggregate function that has a parameter.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static abstract class AggregateFunctionWithParameter<T> extends AggregateFunction<T>{
        
        /**  SVUID */
        private static final long serialVersionUID = 1L;
        
        /**
         * Creates a new instance.
         *
         * @param type
         */
        protected AggregateFunctionWithParameter(DataType<T> type) { super(type); }
        
        /**
         * Returns whether the function accepts this parameter.
         *
         * @param parameter
         * @return
         */
        public abstract boolean acceptsParameter(String parameter);
        
        /**
         * Returns the parameter.
         *
         * @return
         */
        public abstract String getParameter();
        
        /**
         * Creates a new instance with the given parameter.
         *
         * @param parameter
         * @return
         */
        public abstract AggregateFunctionWithParameter<T> newInstance(String parameter);
    }

    /**
     * An aggregate function that returns the arithmetic mean, if it may be computed, "NULL"
     * otherwise.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericArithmeticMean<T> extends AggregateFunction<T> {

        /** SVUID*/
        private static final long serialVersionUID = -901667129625212217L;

        /**
         * Creates a new instance.
         *
         * @param type
         */
        private GenericArithmeticMean(DataType<T> type) {
            super(type);
        }

        @Override
        public String aggregate(String[] values) {
            
            // Count the number of non-null values
            double count = 0;
            for (String value : values) {
                count += value != null && !DataType.isNull(value) ? 1 : 0;
            }
            
            // Data-type specific implementation
            if (super.type.getDescription().getWrappedClass() == Date.class) {
                
                double result = 0d;
                for (String value : values) {
                    Date date = ((ARXDate)type).parse(value);
                    result += date != null ? (double)date.getTime() / count : 0d;
                }
                return ((ARXDate)type).format(new Date((long)result));

            // Data-type specific implementation
            } else if (super.type.getDescription().getWrappedClass() == Long.class) {

                double result = 0d;
                for (String value : values) {
                    Long longValue = ((ARXInteger)type).parse(value);
                    result += longValue != null ? (double)longValue / count : 0d;
                }
                return ((ARXInteger)type).format((long)result);

            // Data-type specific implementation
            } else if (super.type.getDescription().getWrappedClass() == Double.class) {

                double result = 0d;
                for (String value : values) {
                    Double doubleValue = ((ARXDecimal)type).parse(value);
                    result += doubleValue != null ? doubleValue / count : 0d;
                }
                return ((ARXDecimal)type).format(result);
                
            // Data-type specific implementation
            } else {
                return DataType.NULL_VALUE;
            }
        }
        
        @Override
        public String toLabel() {
            return "Arithmetic mean";
        }

        @Override
        public String toString(){
            return "ArithmeticMean";
        }
    }

    /**
     * An aggregate function that returns the arithmetic mean of min & max, if it may be computed, "NULL"
     * otherwise.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericArithmeticMeanOfBounds<T> extends AggregateFunction<T> {

        /** SVUID*/
        private static final long serialVersionUID = 5067728720270473715L;

        /**
         * Creates a new instance.
         *
         * @param type
         */
        private GenericArithmeticMeanOfBounds(DataType<T> type) {
            super(type);
        }

        @Override
        public String aggregate(String[] values) {
            

            String min = null;
            String max = null;
            for (String value : values) {
                try {
                    if (value != null && !DataType.isNull(value) && (min == null || type.compare(min, value) > 0)){
                        min = value;
                    }
                    if (value != null && !DataType.isNull(value) && (max == null || type.compare(max, value) < 0)){
                        max = value;
                    }
                } catch (Exception e) {
                    return DataType.NULL_VALUE;
                }
            }
            
            if (min == null || max == null) {
                return DataType.NULL_VALUE;
            } else {
                return new GenericArithmeticMean<T>(this.type).aggregate(new String[]{min, max});
            }
        }
        
        @Override
        public String toLabel() {
            return "Arithmetic mean of bounds";
        }

        @Override
        public String toString(){
            return "ArithmeticMeanOfBounds";
        }
    }

    /**
     * An aggregate function that returns an interval consisting of the
     * first and the last element following the predefined order .
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericBounds<T> extends AggregateFunction<T> {

        /**  SVUID */
        private static final long serialVersionUID = -8884657842545379206L;

        /**
         * Creates a new instance.
         *
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
     * An aggregate function that returns a common prefix.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericCommonPrefix<T> extends AggregateFunctionWithParameter<T> {
        
        /**  SVUID */
        private static final long serialVersionUID = 526809670467390820L;
        
        /**  SVUID */
        private Character redaction;

        /**
         * Creates a new instance.
         *
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
            outer: while (true) {
                if (values[0].length()==position) break outer;
                char c = values[0].charAt(position);
                for (int i = 1; i < values.length; i++) {
                    if (values[i].charAt(position) != c) {
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
     * An aggregate function that returns a constant value.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericConstant<T> extends AggregateFunctionWithParameter<T> {

        /**  SVUID */
        private static final long serialVersionUID = -8995068916108125096L;
        
        /**  SVUID */
        private String value;
        
        /**
         * Creates a new instance.
         *
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
     * An aggregate function that returns the geometric mean, if it may be computed, "NULL"
     * otherwise.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericGeometricMean<T> extends AggregateFunction<T> {

        /** SVUID*/
        private static final long serialVersionUID = -1756610766270481335L;

        /**
         * Creates a new instance.
         *
         * @param type
         */
        private GenericGeometricMean(DataType<T> type) {
            super(type);
        }

        @Override
        public String aggregate(String[] values) {
            
            // Count the number of non-null values
            double count = 0;
            for (String value : values) {
                count += value != null && !DataType.isNull(value) ? 1 : 0;
            }
            
            // Data-type specific implementation
            if (super.type.getDescription().getWrappedClass() == Date.class) {
                
                double result = 0d;
                for (String value : values) {
                    Date date = ((ARXDate)type).parse(value);
                    result += date != null ? Math.log10((double)date.getTime()) / count : 0d;
                }
                return ((ARXDate)type).format(new Date((long)Math.pow(10d, result)));
               
            // Data-type specific implementation
            } else if (super.type.getDescription().getWrappedClass() == Long.class) {

                double result = 0d;
                for (String value : values) {
                    Long longValue = ((ARXInteger)type).parse(value);
                    result += longValue != null ? Math.log10((double)longValue) / count : 0d;
                }
                return ((ARXInteger)type).format((long)Math.pow(10d, result));
                
            // Data-type specific implementation
            } else if (super.type.getDescription().getWrappedClass() == Double.class) {

                double result = 0d;
                for (String value : values) {
                    Double doubleValue = ((ARXDecimal)type).parse(value);
                    result += doubleValue != null ? Math.log10(doubleValue) / count : 0d;
                }
                return ((ARXDecimal)type).format(Math.pow(10d, result));
                
            // Data-type specific implementation
            } else {
                return DataType.NULL_VALUE;
            }
        }
        
        @Override
        public String toLabel() {
            return "Geometric mean";
        }

        @Override
        public String toString(){
            return "GeometricMean";
        }
    }
    
    /**
     * An aggregate function that returns the geometric mean of min & max, if it may be computed, "NULL"
     * otherwise.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericGeometricMeanOfBounds<T> extends AggregateFunction<T> {

        /** SVUID*/
        private static final long serialVersionUID = 8155390779775522723L;

        /**
         * Creates a new instance.
         *
         * @param type
         */
        private GenericGeometricMeanOfBounds(DataType<T> type) {
            super(type);
        }

        @Override
        public String aggregate(String[] values) {
            

            String min = null;
            String max = null;
            for (String value : values) {
                try {
                    if (value != null && !DataType.isNull(value) && (min == null || type.compare(min, value) > 0)){
                        min = value;
                    }
                    if (value != null && !DataType.isNull(value) && (max == null || type.compare(max, value) < 0)){
                        max = value;
                    }
                } catch (Exception e) {
                    return DataType.NULL_VALUE;
                }
            }
            
            if (min == null || max == null) {
                return DataType.NULL_VALUE;
            } else {
                return new GenericGeometricMean<T>(this.type).aggregate(new String[]{min, max});
            }
        }
        
        @Override
        public String toLabel() {
            return "Geometric mean of bounds";
        }

        @Override
        public String toString(){
            return "GeometricMeanOfBounds";
        }
    }

    /**
     * An aggregate function that returns an interval [min, max] .
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericInterval<T> extends AggregateFunction<T> {
        
        /**  SVUID */
        private static final long serialVersionUID = -5182521036467379023L;
        
        /**  SVUID */
        private final boolean lowerIncluded;
        
        /**  SVUID */
        private final boolean upperIncluded;
        
        /**
         * Creates a new instance.
         *
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
    }

    /**
     * An aggregate function that returns a set of all data values.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericSet<T> extends AggregateFunction<T> {

        /**  SVUID */
        private static final long serialVersionUID = -4029191421720743653L;

        /**
         * Creates a new instance.
         *
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
     * An aggregate function that returns a set of the prefixes of the data values.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class GenericSetOfPrefixes<T> extends AggregateFunctionWithParameter<T> {

        /**  SVUID */
        private static final long serialVersionUID = -4164142474804296433L;
        
        /**  SVUID */
        private int length;

        /**
         * Creates a new instance.
         *
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
    };

    /**  SVUID */
    private static final long serialVersionUID = 3803318906010996154L;
    
    /**
     * Returns a builder for the given data type.
     *
     * @param <T>
     * @param type
     * @return
     */
    public static <T> AggregateFunctionBuilder<T> forType(DataType<T> type){
        return new AggregateFunctionBuilder<T>(type);
    }

    /** The data type. */
    protected DataType<T> type;
    
    /**
     * Constructor.
     *
     * @param type
     */
    protected AggregateFunction(DataType<T> type){
        this.type = type;
    }

    /**
     * 
     * This function returns an aggregate value.
     *
     * @param values
     * @return
     */
    public abstract String aggregate (String[] values);

    /**
     * 
     * Returns whether the function accepts a parameter.
     *
     * @return
     */
    public boolean hasParameter() {
        return (this instanceof AggregateFunctionWithParameter);
    }
    
    /**
     * Returns a label.
     *
     * @return
     */
    public abstract String toLabel();

    @Override
    public abstract String toString ();
}
