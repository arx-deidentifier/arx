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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class provides access to the data types supported by the ARX framework
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class DataType<T> {
	
	/*
	 * TODO: Implement Boolean, Integer, Float
	 */

	public static class ARXDate extends DataType<Date> {

        SimpleDateFormat format;
        String           string;

        private ARXDate() {
            string = "dd.MM.yyyy";
            format = new SimpleDateFormat(string);
        }

        private ARXDate(final String format) {
            this.format = new SimpleDateFormat(format);
            this.string = format;
        }

        @Override
        public DataType<Date> clone() {
            return new ARXDate(string);
        }

        @Override
        public int
                compare(final String s1, final String s2) throws ParseException {
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

    public static class ARXNumeric extends DataType<Double> {
        @Override
        public DataType<Double> clone() {
            return this;
        }

        @Override
        public int
                compare(final String s1, final String s2) throws NumberFormatException {
            return Double.valueOf(s1).compareTo(Double.valueOf(s2));
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
        	return Double.valueOf(s);
        }

        @Override
        public String toString(Double s){
        	return String.valueOf(s);
        }
    }

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

    /** A decimal datatype */
    public static final DataType<Double> NUMERIC = new ARXNumeric();

    /** A string datatype */
    public static final DataType<String> STRING  = new ARXString();

    /** A date datatype with default fomat dd.mm.yyyy */
    public static final DataType<Date> DATE    = new ARXDate();

    /**
     * A date datatype with given format
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
    
    public abstract String toString(T t);
    
    public abstract T fromString(String s);

    public abstract int
            compare(String s1, String s2) throws NumberFormatException,
                                         ParseException;

}
