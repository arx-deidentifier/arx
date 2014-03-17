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
import java.util.Date;

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
	public static class ARXDate extends DataType<Date> {

        SimpleDateFormat format;
        String           string;

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
            if (string == null) {
                if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            return true;
        }

        @Override
        public String toString() {
            return "Date(" + string + ")";
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
        public String toString(Date s){
        	return format.format(s);
        }

        /**
         * Returns the format
         * @return
         */
        public String getFormat() {
            return string;
        }
    }

    /**
     * Base class for numeric types
     * @author Fabian Prasser
     */
    public static class ARXInteger extends DataType<Long> {
        
        private DecimalFormat format;
        
        public ARXInteger(){
            format = null;
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        public ARXInteger(String format){
            this.format = new DecimalFormat(format);
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
            return true;
        }

        @Override
        public String toString() {
            return "Integer";
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
        public String toString(Long s){
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
    public static class ARXDecimal extends DataType<Double> {
        
        private DecimalFormat format;
        
        public ARXDecimal(){
            format = null;
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        public ARXDecimal(String format){
            this.format = new DecimalFormat(format);
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
            return true;
        }

        @Override
        public String toString() {
            return "Decimal";
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
        public String toString(Double s){
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
        public String toString() {
            return "String";
        }
        
        @Override
        public String fromString(String s) {
        	return s;
        }

        @Override
        public String toString(String s){
        	return s;
        }
    }

    /** A generic decimal data type */
    public static final DataType<Double> DECIMAL = new ARXDecimal();

    /** A string data type */
    public static final DataType<String> STRING  = new ARXString();

    /** A date data type with default format dd.mm.yyyy */
    public static final DataType<Date>   DATE    = new ARXDate();

    /** A generic integer data type */
    public static final DataType<Long>   INTEGER = new ARXInteger();

    /**
     * An integer data type with given format
     * 
     * @see SimpleDateFormat
     * @param format
     * @return
     */
    public static final DataType<Long> INTEGER(final String format) {
        return new ARXInteger(format);
    }
    
    /**
     * A decimal data type with given format
     * 
     * @see SimpleDateFormat
     * @param format
     * @return
     */
    public static final DataType<Double> DECIMAL(final String format) {
        return new ARXDecimal(format);
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

    @Override
    public abstract DataType<T> clone();
    
    /**
     * Converts a value into a string
     * @param t
     * @return
     */
    public abstract String toString(T t);
    
    /**
     * Converts a string into a value
     * @param s
     * @return
     */
    public abstract T fromString(String s);

    /**
     * Compares two values
     * @param s1
     * @param s2
     * @return
     * @throws NumberFormatException
     * @throws ParseException
     */
    public abstract int compare(String s1, String s2) throws NumberFormatException, ParseException;

}
