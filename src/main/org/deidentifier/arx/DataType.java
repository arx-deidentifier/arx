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

package org.deidentifier.arx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides access to the data types supported by the ARX framework
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class DataType<T> implements Serializable {
    
    private static final long serialVersionUID = -4380267779210935078L;

    /**
     * Base class for date/time types
     * @author Fabian Prasser
     */
	public static class ARXDate extends DataType<Date> implements DataTypeWithFormat, DataTypeWithRatioScale<Date> {
	
        private static final long serialVersionUID = -1658470914184442833L;

        /** The description of the data type*/
        private static final DataTypeDescription<Date> description = new DataTypeDescription<Date>(Date.class, "Date/Time",  true, listDateFormats()){
            /**
             * 
             */
            private static final long serialVersionUID = -1723392257250720908L;
            @Override public DataType<Date> newInstance() { return DATE; }
            @Override public DataType<Date> newInstance(String format) {return DATE(format);}
        };
        
        private SimpleDateFormat format;

        private String           string;
        /**
         * Create a data with a "dd.MM.yyyy" format string
         * for <code>SimpleDateFormat</code>.
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
         */
        private ARXDate() {
            this("Default");
        }

        /**
         * Create a date with a format string. Format strings must be valid formats
         * for <code>SimpleDateFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
         */
        private ARXDate(final String formatString) {
            if (format == null || format.equals("Default")) {
                string = "dd.MM.yyyy";
                format = new SimpleDateFormat(string);
            } else {
                this.format = new SimpleDateFormat(formatString);
                this.string = formatString;
            }
        }

        @Override
        public DataType<Date> clone() {
            return new ARXDate(string);
        }

        @Override
        public int compare(final String s1, final String s2) throws ParseException {
            try {
                return format.parse(s1).compareTo(format.parse(s2));
            } catch (Exception e){
                throw new IllegalArgumentException("Invalid value", e);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ARXDate other = (ARXDate) obj;
            if (string == null) { if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            return true;
        }
        
        @Override
        public Date parse(String s) {
        	try {
				return format.parse(s);
        	} catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ": " + s, e);
            }
        }

        @Override
        public boolean isValid(String s) {
            try {
                parse(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        @Override
        public DataTypeDescription<Date> getDescription(){
            return description;
        }

        @Override
        public String getFormat() {
            return string;
        }
        
        @Override
        public int hashCode() {
            if (string==null) return 0;
            else return string.hashCode();
        }

        @Override
        public String toString() {
            return "Date(" + string + ")";
        }

        @Override
        public String format(Date s){
        	return format.format(s);
        }
        
        @Override
        public String divide(String dividend, String divisor) {
            long d1 = parse(dividend).getTime();
            long d2 = parse(divisor).getTime();
            return format(new Date(d1 / d2));
        }

        @Override
        public String multiply(String multiplicand, String multiplicator) {
            long d1 = parse(multiplicand).getTime();
            long d2 = parse(multiplicator).getTime();
            return format(new Date(d1 * d2));
        }

        @Override
        public Date divide(Date dividend, Date divisor) {
            long d1 = dividend.getTime();
            long d2 = divisor.getTime();
            return new Date(d1 / d2);
        }

        @Override
        public Date multiply(Date multiplicand, Date multiplicator) {
            long d1 = multiplicand.getTime();
            long d2 = multiplicator.getTime();
            return new Date(d1 * d2);
        }

        @Override
        public int compare(Date t1, Date t2) {
            return t1.compareTo(t2);
        }

        @Override
        public Date subtract(Date minuend, Date subtrahend) {
            long d1 = minuend.getTime();
            long d2 = subtrahend.getTime();
            return new Date(d1 - d2);
        }

        @Override
        public Date add(Date augend, Date addend) {
            long d1 = augend.getTime();
            long d2 = addend.getTime();
            return new Date(d1 + d2);
        }
    }

    /**
	 * Base class for numeric types
	 * @author Fabian Prasser
	 */
    public static class ARXDecimal extends DataType<Double> implements DataTypeWithFormat, DataTypeWithRatioScale<Double> {
  
        private static final long serialVersionUID = 7293446977526103610L;

        /** The description of the data type*/
        private static final DataTypeDescription<Double> description = new DataTypeDescription<Double>(Double.class, "Decimal", true, listDecimalFormats()){
            /**
             * 
             */
            private static final long serialVersionUID = -3549629178680030868L;
            @Override public DataType<Double> newInstance() { return DECIMAL; }
            @Override public DataType<Double> newInstance(String format) {return DECIMAL(format);}
        };
        
        private DecimalFormat format;
        
        private String        string;
        
        private ARXDecimal(){
            this("Default");
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXDecimal(String format){
            if (format == null || format.equals("Default")){
                this.format = null;
                this.string = null;
            } else {
                this.format = new DecimalFormat(format);
                this.string = format;
            }
        }
        
        @Override
        public DataType<Double> clone() {
            return this;
        }
        
        @Override
        public int compare(final String s1, final String s2) throws NumberFormatException {
            try {
                return parse(s1).compareTo(parse(s2));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid value: '"+s1+"' or: '"+s2+"'", e);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ARXDecimal other = (ARXDecimal) obj;
            if (string == null) { if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            return true;
        }

        @Override
        public Double parse(String s) {
            try {
                if (format == null) {
                    return Double.valueOf(s);
                } else {
                    return format.parse(s).doubleValue();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ": " + s, e);
            }
        }

        @Override
        public boolean isValid(String s) {
            try {
                parse(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        @Override
        public DataTypeDescription<Double> getDescription(){
            return description;
        }

        @Override
        public String getFormat() {
            return string;
        }

        @Override
        public int hashCode() {
            if (string==null) return 0;
            else return string.hashCode();
        }

        @Override
        public String toString() {
            return "Decimal";
        }

        @Override
        public String format(Double s){
            if (format==null){
                return String.valueOf(s);
            } else {
                return format.format(s);
            }
        }

        @Override
        public String divide(String dividend, String divisor) {
            Double d1 = parse(dividend);
            Double d2 = parse(divisor);
            return format(d1 / d2);
        }

        @Override
        public String multiply(String multiplicand, String multiplicator) {
            Double d1 = parse(multiplicand);
            Double d2 = parse(multiplicator);
            return format(d1 * d2);
        }

        @Override
        public Double divide(Double dividend, Double divisor) {
            return dividend / divisor;
        }

        @Override
        public Double multiply(Double multiplicand, Double multiplicator) {
            return multiplicand * multiplicator;
        }

        @Override
        public int compare(Double t1, Double t2) {
            return t1.compareTo(t2);
        }

        @Override
        public Double subtract(Double minuend, Double subtrahend) {
            return minuend - subtrahend;
        }

        @Override
        public Double add(Double augend, Double addend) {
            return augend + addend;
        }
    }
    
    /**
     * Base class for numeric types
     * @author Fabian Prasser
     */
    public static class ARXInteger extends DataType<Long> implements DataTypeWithFormat, DataTypeWithRatioScale<Long>  {
        
        private static final long serialVersionUID = -631163546929231044L;

        /** The description of the data type*/
        private static final DataTypeDescription<Long> description = new DataTypeDescription<Long>(Long.class, "Integer", false, new ArrayList<String>()){
            /**
             * 
             */
            private static final long serialVersionUID = -4498725217659811835L;
            @Override public DataType<Long> newInstance() { return INTEGER; }
            @Override public DataType<Long> newInstance(String format) {return INTEGER(format);}
        };
        
        private DecimalFormat format;
        
        private String        string;
        
        private ARXInteger(){
            this("Default");
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXInteger(String format){
            if (format == null || format.equals("Default")){
                this.format = null;
                this.string = null;   
            } else {
                this.format = new DecimalFormat(format);
                this.string = format;
            }
        }
        
        @Override
        public DataType<Long> clone() {
            return this;
        }
        
        @Override
        public int compare(final String s1, final String s2) throws NumberFormatException {
            try {
                return parse(s1).compareTo(parse(s2));
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ": " + s1 +" or: " + s2, e);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ARXInteger other = (ARXInteger) obj;
            if (string == null) { if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            return true;
        }

        @Override
        public Long parse(String s) {
            try {
                if (format == null) {
                    return Long.valueOf(s);
                } else {
                    return format.parse(s).longValue();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ": " + s, e);
            }
        }

        @Override
        public DataTypeDescription<Long> getDescription(){
            return description;
        }

        @Override
        public String getFormat() {
            return string;
        }

        @Override
        public int hashCode() {
            if (string==null) return 0;
            else return string.hashCode();
        }
        
        @Override
        public String toString() {
            return "Integer";
        }

        @Override
        public String format(Long s){
            if (format==null){
                return String.valueOf(s);
            } else {
                return format.format(s);
            }
        }
        
        @Override
        public boolean isValid(String s) {
            try {
                parse(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        @Override
        public String divide(String dividend, String divisor) {
            Long d1 = parse(dividend);
            Long d2 = parse(divisor);
            return format(d1 / d2);
        }

        @Override
        public String multiply(String multiplicand, String multiplicator) {
            Long d1 = parse(multiplicand);
            Long d2 = parse(multiplicator);
            return format(d1 * d2);
        }

        @Override
        public Long divide(Long dividend, Long divisor) {
            return (long)Math.round((double)dividend / (double)divisor);
        }

        @Override
        public Long multiply(Long multiplicand, Long multiplicator) {
            return (long)Math.round((double)multiplicand * (double)multiplicator);
        }

        @Override
        public int compare(Long t1, Long t2) {
            return t1.compareTo(t2);
        }

        @Override
        public Long subtract(Long minuend, Long subtrahend) {
            return minuend - subtrahend;
        }

        @Override
        public Long add(Long augend, Long addend) {
            return augend + addend;
        }
    }
    
    /**
     * Base class for string types
     * @author Fabian Prasser
     */
    public static class ARXString extends DataType<String> {
        
        private static final long serialVersionUID = 903334212175979691L;
        
        /** The description of the data type*/
        private static final DataTypeDescription<String> description = new DataTypeDescription<String>(String.class, "String", false, new ArrayList<String>()){
            /**
             * 
             */
            private static final long serialVersionUID = -6679110898204862834L;
            @Override public DataType<String> newInstance() { return STRING; }
            @Override public DataType<String> newInstance(String format) {return STRING;}
        };
        
        @Override
        public DataType<String> clone() {
            return this;
        }
        
        @Override
        public int compare(final String s1, final String s2) {
            return s1.compareTo(s2);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            return true;
        }

        @Override
        public String parse(String s) {
            return s;
        }
        
        @Override
        public DataTypeDescription<String> getDescription(){
            return description;
        }

        @Override
        public int hashCode() {
            return ARXString.class.hashCode();
        }
        
        @Override
        public String toString() {
            return "String";
        }

        @Override
        public String format(String s){
            return s;
        }

        @Override
        public boolean isValid(String s) {
            return true;
        }
    }
    
    /**
     * Base class for ordered string types
     * @author Fabian Prasser
     */
    public static class ARXOrderedString extends DataType<String> implements DataTypeWithFormat {
        
        private static final long serialVersionUID = -830897705078418835L;
        
        private Map<String, Integer> order;
        
        /**
         * Creates a new instance
         */
        private ARXOrderedString(){
            this("Default");
        }
        
        /**
         * Creates a new instance
         * @param format Ordered list of string separated by line feeds
         */
        private ARXOrderedString(String format){
            if (format==null || format.equals("Default") || format.equals("")) {
                this.order = null;
            } else {
                try {
                    this.order = new HashMap<String, Integer>(); 
                    BufferedReader reader = new BufferedReader(new StringReader(format));
                    int index = 0;
                    String line = reader.readLine();
                    while (line != null) {
                        if (this.order.put(line, index) != null) {
                            throw new IllegalArgumentException("Duplicate value '"+line+"'");
                        }
                        line = reader.readLine();
                        index++;
                    }
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalArgumentException("Error reading input data");
                }
            }
        }

        /**
         * Creates a new instance
         * @param format Ordered list of strings
         */
        private ARXOrderedString(String[] format){
            if (format.length == 0) {
                this.order = null;
            } else {
                this.order = new HashMap<String, Integer>(); 
                for (int i=0; i< format.length; i++){
                    if (this.order.put(format[i], i) != null) {
                        throw new IllegalArgumentException("Duplicate value '"+format[i]+"'");
                    }
                }
            }
        }

        /**
         * Creates a new instance
         * @param format Ordered list of strings
         */
        private ARXOrderedString(List<String> format){
            if (format.size()==0) {
                this.order = null;
            } else {
                this.order = new HashMap<String, Integer>(); 
                for (int i=0; i< format.size(); i++){
                    if (this.order.put(format.get(i), i) != null) {
                        throw new IllegalArgumentException("Duplicate value '"+format.get(i)+"'");
                    }
                }
            }
        }
        
        /** The description of the data type*/
        private static final DataTypeDescription<String> description = new DataTypeDescription<String>(String.class, "OrderedString", true, new ArrayList<String>()){
            
            private static final long serialVersionUID = -6300869938311742699L;
            @Override public DataType<String> newInstance() { return ORDERED_STRING; }
            @Override public DataType<String> newInstance(String format) {return ORDERED_STRING(format);}
        };
        
        @Override
        public DataType<String> clone() {
            return this;
        }
        
        @Override
        public int compare(final String s1, final String s2) {
            if (order != null){
                try {
                    return order.get(s1).compareTo(order.get(s2));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid value", e);
                }
            } else {
                return s1.compareTo(s2);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            return true;
        }

        @Override
        public String parse(String s) {
            if (order != null && !order.containsKey(s)) {
                throw new IllegalArgumentException("Unknown string '"+s+"'");
            }
        	return s;
        }
        
        @Override
        public DataTypeDescription<String> getDescription(){
            return description;
        }

        @Override
        public int hashCode() {
            return ARXOrderedString.class.hashCode();
        }
        
        @Override
        public String toString() {
            return "OrderedString";
        }

        @Override
        public String format(String s){
            if (order != null && !order.containsKey(s)) {
                throw new IllegalArgumentException("Unknown string '"+s+"'");
            }
        	return s;
        }

        @Override
        public boolean isValid(String s) {
            if (order != null && !order.containsKey(s)) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String getFormat() {
            if (order == null) return "";
            List<String> list = new ArrayList<String>();
            list.addAll(order.keySet());
            Collections.sort(list, new Comparator<String>(){
                @Override
                public int compare(String arg0, String arg1) {
                    return order.get(arg0).compareTo(order.get(arg1));
                } 
            });
            StringBuilder b = new StringBuilder();
            for (int i=0; i<list.size(); i++) {
                b.append(list.get(i));
                if (i<list.size()-1) {
                    b.append("\n");
                }
            }
            return b.toString();
        }
    }

	/**
     * An entry in the list of available data types
     * @author Fabian Prasser
     * @param <T>
     */
    public static abstract class DataTypeDescription<T> implements Serializable {

        private static final long serialVersionUID = 6369986224526795419L;
        
        /** The wrapped java class*/
        private Class<?> clazz;
        /** If yes, a list of available formats*/
        private List<String> exampleFormats;
        /** Can the type be parameterized with a format string*/
        private boolean hasFormat;
        /** A human readable label*/
        private String label;
        
        /**
         * Internal constructor
         * @param label
         * @param hasFormat
         * @param exampleFormats
         */
        private DataTypeDescription(Class<T> clazz, String label, boolean hasFormat, List<String> exampleFormats) {
            this.clazz = clazz;
            this.label = label;
            this.hasFormat = hasFormat;
            this.exampleFormats = exampleFormats;
        }
        
        /**
         * Returns a list of example formats
         * @return
         */
        public List<String> getExampleFormats() {
            return exampleFormats;
        }
        
        /**
         * Returns a human readable label
         * @return
         */
        public String getLabel() {
            return label;
        }
        
        /**
         * Returns the wrapped java class
         * @return
         */
        public Class<?> getWrappedClass() {
            return clazz;
        }
        
        /**
         * Returns whether the type be parameterized with a format string. Note that every data type
         * can be instantiated without a format string, using a default format.
         * @return
         */
        public boolean hasFormat() {
            return hasFormat;
        }
        
        /**
         * Creates a new instance with default format string
         * @return
         */
        public abstract DataType<T> newInstance();
        
        /**
         * Creates a new instance with the given format string
         * @param format
         * @return
         */
        public abstract DataType<T> newInstance(String format);
    }

    /**
     * An interface for data types with a ratio scale
     * @author Fabian Prasser
     *
     * @param <T>
     */
    public static interface DataTypeWithRatioScale<T> {
        
        public abstract String divide(String dividend, String divisor);

        public abstract String multiply(String multiplicand,
                                        String multiplicator);

        public abstract T divide(T dividend, T divisor);

        public abstract T multiply(T multiplicand, T multiplicator);

        public abstract int compare(T t1, T t2);

        public abstract T subtract(T minuend, T subtrahend);

        public abstract T add(T augend, T addend);

        public abstract int compare(String s1, String s2) throws NumberFormatException,
                                                                 ParseException;

        public abstract T parse(String s);

        public abstract DataTypeDescription<T> getDescription();

        public abstract String format(T t);

        public abstract boolean isValid(String s);
    }
    
    /**
     * An interface for data types with format
     * @author Fabian Prasser
     */
    public static interface DataTypeWithFormat {
        public abstract String getFormat();
    }

    /** A date data type with default format dd.mm.yyyy */
    public static final DataType<Date>               DATE    = new ARXDate();

    /** A generic decimal data type */
    public static final DataType<Double>             DECIMAL = new ARXDecimal();

    /** A generic integer data type */
    public static final DataType<Long>               INTEGER = new ARXInteger();

    /** A string data type */
    public static final DataType<String>             STRING  = new ARXString();

    /** A ordered string data type */
    public static final DataType<String>             ORDERED_STRING  = new ARXOrderedString();

    /** Provides a list of all available data types */
    public static final List<DataTypeDescription<?>> LIST    = listDataTypes();
    
    /**
     * A ordered string type with given format. 
     * 
     * @param format List of ordered strings
     * @return
     */
    public static final DataType<String> ORDERED_STRING(final List<String> format) {
        return new ARXOrderedString(format);
    }
    
    /**
     * A ordered string type with given format. 
     * 
     * @param format List of ordered strings
     * @return
     */
    public static final DataType<String> ORDERED_STRING(final String[] format) {
        return new ARXOrderedString(format);
    }
    
    /**
     * A ordered string type with given format. 
     * 
     * @param format List of ordered strings separated by line feeds
     * @return
     */
    public static final DataType<String> ORDERED_STRING(final String format) {
        return new ARXOrderedString(format);
    }
    
    /**
     * A date data type with given format
     * 
     * @see SimpleDateFormat
     * @param format
     * @return
     */
    public static final DataType<Date> DATE(final String format) {
        return new ARXDate(format);
    }
    
    /**
     * A decimal data type with given format
     * 
     * @see DecimalFormat
     * @param format
     * @return
     */
    public static final DataType<Double> DECIMAL(final String format) {
        return new ARXDecimal(format);
    }
    
    /**
     * An integer data type with given format
     * 
     * @see DecimalFormat
     * @param format
     * @return
     */
    public static final DataType<Long> INTEGER(final String format) {
        return new ARXInteger(format);
    }
    
    /** 
     * Returns a datatype for the given class
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <U> DataTypeDescription<U> LIST(Class<U> clazz){
        for (DataTypeDescription<?> entry : LIST) {
            if (entry.getWrappedClass() == clazz) {
                return (DataTypeDescription<U>)entry;
            }
        }
        return null;
    }

    /**
     * Lists all available data types
     * @return
     */
    private static final List<DataTypeDescription<?>> listDataTypes(){
        List<DataTypeDescription<?>> list = new ArrayList<DataTypeDescription<?>>();
        list.add(STRING.getDescription());
        list.add(ORDERED_STRING.getDescription());
        list.add(DATE.getDescription());
        list.add(DECIMAL.getDescription());
        list.add(INTEGER.getDescription());
        return list;
    }
    
    /**
     * Provides a list of example formats for the <code>Date</code> data type
     * @return
     */
    private static List<String> listDateFormats(){
        List<String> result = new ArrayList<String>();
        result.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
        result.add("yyyy-MM-ddZZ");
        result.add("yyyy-MM-dd'T'HH:mm:ssz");
        result.add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        result.add("EEE MMM d hh:mm:ss z yyyy");
        result.add("EEE MMM dd HH:mm:ss yyyy");
        result.add("EEEE, dd-MMM-yy HH:mm:ss zzz");
        result.add("EEE, dd MMM yyyy HH:mm:ss zzz");
        result.add("EEE, dd MMM yy HH:mm:ss z");
        result.add("EEE, dd MMM yy HH:mm z");
        result.add("EEE, dd MMM yyyy HH:mm:ss z");
        result.add("yyyy-MM-dd'T'HH:mm:ss");
        result.add("EEE, dd MMM yyyy HH:mm:ss Z");
        result.add("dd MMM yy HH:mm:ss z");
        result.add("dd MMM yy HH:mm z");
        result.add("'T'HH:mm:ss");
        result.add("'T'HH:mm:ssZZ");
        result.add("HH:mm:ss");
        result.add("HH:mm:ssZZ");
        result.add("yyyy-MM-dd");
        result.add("yyyy-MM-dd hh:mm:ss");
        result.add("yyyy-MM-dd HH:mm:ss");
        result.add("yyyy-MM-dd'T'HH:mm:ssz");
        result.add("yyyy-MM-dd'T'HH:mm:ss");
        result.add("yyyy-MM-dd'T'HH:mm:ssZZ");
        result.add("yyyy-MM-dd");
        return result;
    }

    /**
     * Provides a list of example formats for the <code>Decimal</code> data type
     * @return
     */
    private static List<String> listDecimalFormats(){
        List<String> result = new ArrayList<String>();
        result.add("#,##0");
        result.add("#,##0.###");
        result.add("#,##0%");
        result.add("¤#,##0.00;(¤#,##0.00)");
        return result;
    }
    
    @Override
    public abstract DataType<T> clone();
    
    /**
     * Compares two values. The result is 0 if both values are equal, 
     * less than 0 if the first value is less than the second argument, 
     * and greater than 0 if the first value is greater than the second argument.
     * @param s1
     * @param s2
     * @return
     * @throws NumberFormatException
     * @throws ParseException
     */
    public abstract int compare(String s1, String s2) throws NumberFormatException, ParseException;
    
    /**
     * Converts a string into a value
     * @param s
     * @return
     */
    public abstract T parse(String s);

    /**
     * Returns a description of the data type
     * @return
     */
    public abstract DataTypeDescription<T> getDescription();
    
    /**
     * Converts a value into a string
     * @param t
     * @return
     */
    public abstract String format(T t);

    /**
     * Checks whether the given string conforms to the data type's format
     * @param s
     * @return
     */
    public abstract boolean isValid(String s);
}
