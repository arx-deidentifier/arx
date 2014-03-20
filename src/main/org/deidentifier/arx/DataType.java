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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides access to the data types supported by the ARX framework
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class DataType<T> {
    
    /**
     * Base class for date/time types
     * @author Fabian Prasser
     */
	public static class ARXDate extends DataType<Date> implements DataTypeWithFormat {
	    
        /** The description of the data type*/
        private static final DataTypeDescription<Date> description = new DataTypeDescription<Date>(Date.class, "Date/Time",  true, listDateFormats()){
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
            string = "dd.MM.yyyy";
            format = new SimpleDateFormat(string);
        }

        /**
         * Create a data with a format string. Format strings must be valid formats
         * for <code>SimpleDateFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
         */
        private ARXDate(final String format) {
            this.format = new SimpleDateFormat(format);
            this.string = format;
        }


        @Override
        public DataType<Date> clone() {
            return new ARXDate(string);
        }

        @Override
        public int compare(final String s1, final String s2) throws ParseException {
            return format.parse(s1).compareTo(format.parse(s2));
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
        public Date fromString(String s) {
        	try {
				return format.parse(s);
			} catch (ParseException e) {
				throw new RuntimeException(e);
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
        public String toString(Date s){
        	return format.format(s);
        }
    }

    /**
	 * Base class for numeric types
	 * @author Fabian Prasser
	 */
    public static class ARXDecimal extends DataType<Double> implements DataTypeWithFormat {
        
        /** The description of the data type*/
        private static final DataTypeDescription<Double> description = new DataTypeDescription<Double>(Double.class, "Decimal", true, listDecimalFormats()){
            @Override public DataType<Double> newInstance() { return DECIMAL; }
            @Override public DataType<Double> newInstance(String format) {return DECIMAL(format);}
        };
        
        private DecimalFormat format;
        
        private String        string;
        
        private ARXDecimal(){
            this.format = null;
            this.string = null;
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXDecimal(String format){
            if (format != null){
                this.format = new DecimalFormat(format);
                this.string = format;
            } else {
                this.format = null;
                this.string = null;
            }
        }
        
        @Override
        public DataType<Double> clone() {
            return this;
        }
        
        @Override
        public int compare(final String s1, final String s2) throws NumberFormatException {
            return fromString(s1).compareTo(fromString(s2));
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
        public Double fromString(String s) {
            if (format==null){
                return Double.valueOf(s);
            } else {
                try {
                    return format.parse(s).doubleValue();
                } catch (ParseException e) {
                    throw new NumberFormatException(e.getMessage());
                }
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
        public String toString(Double s){
            if (format==null){
                return String.valueOf(s);
            } else {
                return format.format(s);
            }
        }
    }
    
    /**
     * Base class for numeric types
     * @author Fabian Prasser
     */
    public static class ARXInteger extends DataType<Long> implements DataTypeWithFormat {
        
        /** The description of the data type*/
        private static final DataTypeDescription<Long> description = new DataTypeDescription<Long>(Long.class, "Integer", false, new ArrayList<String>()){
            @Override public DataType<Long> newInstance() { return INTEGER; }
            @Override public DataType<Long> newInstance(String format) {return INTEGER(format);}
        };
        
        private DecimalFormat format;
        
        private String        string;
        
        private ARXInteger(){
            this.format = null;
            this.string = null;
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXInteger(String format){
            if (format != null){
                this.format = new DecimalFormat(format);
                this.string = format;
            } else {
                this.format = null;
                this.string = null;   
            }
        }
        
        @Override
        public DataType<Long> clone() {
            return this;
        }
        
        @Override
        public int compare(final String s1, final String s2) throws NumberFormatException {
            return fromString(s1).compareTo(fromString(s2));
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
        public Long fromString(String s) {
            if (format==null){
                return Long.valueOf(s);
            } else {
                try {
                    return format.parse(s).longValue();
                } catch (ParseException e) {
                    throw new NumberFormatException(e.getMessage());
                }
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
        public String toString(Long s){
            if (format==null){
                return String.valueOf(s);
            } else {
                return format.format(s);
            }
        }
    }

    /**
     * Base class for string types
     * @author Fabian Prasser
     */
    public static class ARXString extends DataType<String> {
        
        /** The description of the data type*/
        private static final DataTypeDescription<String> description = new DataTypeDescription<String>(String.class, "String", false, new ArrayList<String>()){
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
        public String fromString(String s) {
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
        public String toString(String s){
        	return s;
        }
    }

	/**
     * An entry in the list of available data types
     * @author Fabian Prasser
     * @param <T>
     */
    public static abstract class DataTypeDescription<T> {

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

    /** Provides a list of all available data types */
    public static final List<DataTypeDescription<?>> LIST    = listDataTypes();

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
     * Compares two values
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
    public abstract T fromString(String s);

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
    public abstract String toString(T t);
}
