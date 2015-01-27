/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.aggregates.AggregateFunction.AggregateFunctionBuilder;

/**
 * This class provides access to the data types supported by the ARX framework.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @param <T>
 */
public abstract class DataType<T> implements Serializable, Comparator<T> {
    
    /**
     * Base class for date/time types.
     *
     * @author Fabian Prasser
     */
	public static class ARXDate extends DataType<Date> implements DataTypeWithFormat, DataTypeWithRatioScale<Date> {

        /**  TODO */
        private static final long                      serialVersionUID = -1658470914184442833L;

        /** The description of the data type. */
        private static final DataTypeDescription<Date> description = new DataTypeDescription<Date>(Date.class, "Date/Time",  true, listDateFormats()){
            private static final long serialVersionUID = -1723392257250720908L;
            @Override public DataType<Date> newInstance() { return DATE; }
            @Override public DataType<Date> newInstance(String format) {return createDate(format);}
            @Override public DataType<Date> newInstance(String format, Locale locale) {return createDate(format, locale);}
        };

        /** Format. */
        private final SimpleDateFormat                 format;

        /** Format string. */
        private final String                           string;

        /** Locale. */
        private final Locale                           locale;

        /**
         * Create a date with a "dd.MM.yyyy" format string
         * for <code>SimpleDateFormat</code> and default locale.
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
         */
        private ARXDate() {
            this("Default");
        }

        /**
         * Create a date with a format string and the default locale. Format strings must be valid formats
         * for <code>SimpleDateFormat</code>.
         *
         * @param formatString
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
         */
        private ARXDate(final String formatString) {
            if (formatString == null || formatString.equals("Default")) {
                this.string = "dd.MM.yyyy";
                this.format = new SimpleDateFormat(string);
                this.locale = null;
            } else {
                this.format = new SimpleDateFormat(formatString);
                this.string = formatString;
                this.locale = null;
            }
        }
        
        /**
         * Create a date with a format string and a given locale. Format strings must be valid formats
         * for <code>SimpleDateFormat</code>.
         *
         * @param formatString
         * @param locale
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
         */
        private ARXDate(String formatString, Locale locale) {
            if (formatString == null || formatString.equals("Default")) {
                this.string = "dd.MM.yyyy";
                this.format = new SimpleDateFormat(string, locale);
                this.locale = locale;
            } else {
                this.format = new SimpleDateFormat(formatString, locale);
                this.string = formatString;
                this.locale = locale;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#add(java.lang.Object, java.lang.Object)
         */
        @Override
        public Date add(Date augend, Date addend) {
            long d1 = augend.getTime();
            long d2 = addend.getTime();
            return new Date(d1 + d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#clone()
         */
        @Override
        public DataType<Date> clone() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Date t1, Date t2) {
            return t1.compareTo(t2);
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.String, java.lang.String)
         */
        @Override
        public int compare(final String s1, final String s2) throws ParseException {
            try {
                return format.parse(s1).compareTo(format.parse(s2));
            } catch (Exception e){
                throw new IllegalArgumentException("Invalid value", e);
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#divide(java.lang.Object, java.lang.Object)
         */
        @Override
        public Date divide(Date dividend, Date divisor) {
            long d1 = dividend.getTime();
            long d2 = divisor.getTime();
            return new Date(d1 / d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#divide(java.lang.String, java.lang.String)
         */
        @Override
        public String divide(String dividend, String divisor) {
            long d1 = parse(dividend).getTime();
            long d2 = parse(divisor).getTime();
            return format(new Date(d1 / d2));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ARXDate other = (ARXDate) obj;
            if (string == null) { if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            if (getLocale() == null) { if (other.getLocale() != null) { return false; }
            } else if (!getLocale().equals(other.getLocale())) { return false; }
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#format(java.lang.Object)
         */
        @Override
        public String format(Date s){
        	return format.format(s);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#getDescription()
         */
        @Override
        public DataTypeDescription<Date> getDescription(){
            return description;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithFormat#getFormat()
         */
        @Override
        public String getFormat() {
            return string;
        }
        
        /**
         * Returns the locale of the format.
         *
         * @return
         */
        public Locale getLocale() {
            if (this.locale == null) {
                return Locale.getDefault();
            } else {
                return locale;
            }
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#getMaximum()
         */
        @Override
        public Date getMaximum() {
            return new Date(Long.MAX_VALUE);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#getMinimum()
         */
        @Override
        public Date getMinimum() {
            return new Date(Long.MIN_VALUE);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#hashCode()
         */
        @Override
        public int hashCode() {
            if (string == null) {
                return getLocale().hashCode();
            }
            else {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((string == null) ? 0 : string.hashCode());
                result = prime * result + ((getLocale() == null) ? 0 : getLocale().hashCode());
                return result;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid(String s) {
            try {
                parse(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, java.lang.Object)
         */
        @Override
        public Date multiply(Date multiplicand, Date multiplicator) {
            long d1 = multiplicand.getTime();
            long d2 = multiplicator.getTime();
            return new Date(d1 * d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, double)
         */
        @Override
        public Date multiply(Date multiplicand, double multiplicator) {
            long d1 = multiplicand.getTime();
            return new Date((long)((double)d1 * multiplicator));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, int)
         */
        @Override
        public Date multiply(Date multiplicand, int multiplicator) {
            long d1 = multiplicand.getTime();
            return new Date(d1 * multiplicator);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.String, java.lang.String)
         */
        @Override
        public String multiply(String multiplicand, String multiplicator) {
            long d1 = parse(multiplicand).getTime();
            long d2 = parse(multiplicator).getTime();
            return format(new Date(d1 * d2));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#parse(java.lang.String)
         */
        @Override
        public Date parse(String s) {
        	try {
				return format.parse(s);
        	} catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ": " + s, e);
            }
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#ratio(java.lang.Object, java.lang.Object)
         */
        @Override
        public double ratio(Date dividend, Date divisor) {
            long d1 = dividend.getTime();
            long d2 = divisor.getTime();
            return (double)d1 / (double)d2;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#subtract(java.lang.Object, java.lang.Object)
         */
        @Override
        public Date subtract(Date minuend, Date subtrahend) {
            long d1 = minuend.getTime();
            long d2 = subtrahend.getTime();
            return new Date(d1 - d2);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Date(" + string + ")";
        }
    }

    /**
     * Base class for numeric types.
     *
     * @author Fabian Prasser
     */
    public static class ARXDecimal extends DataType<Double> implements DataTypeWithFormat, DataTypeWithRatioScale<Double> {

        /**  TODO */
        private static final long                        serialVersionUID = 7293446977526103610L;

        /** The description of the data type. */
        private static final DataTypeDescription<Double> description = new DataTypeDescription<Double>(Double.class, "Decimal", true, listDecimalFormats()){
            private static final long serialVersionUID = -3549629178680030868L;
            @Override public DataType<Double> newInstance() { return DECIMAL; }
            @Override public DataType<Double> newInstance(String format) {return createDecimal(format);}
            @Override public DataType<Double> newInstance(String format, Locale locale) {return createDecimal(format, locale);}
        };

        /** Format. */
        private final DecimalFormat                      format;

        /** Format string. */
        private final String                             string;

        /** Locale. */
        private final Locale                             locale;

        /**
         * Default constructor.
         */
        private ARXDecimal(){
            this("Default");
        }
        
        /**
         * Create a numeric with a format string and default locale. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXDecimal(String format){
            if (format == null || format.equals("Default")){
                this.format = null;
                this.string = null;
                this.locale = null;
            } else {
                this.format = new DecimalFormat(format);
                this.string = format;
                this.locale = null;
            }
        }

        /**
         * Create a numeric with a format string and given locale. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @param locale
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXDecimal(String format, Locale locale) {
            if (format == null || format.equals("Default")){
                this.format = null;
                this.string = null;
                this.locale = locale;
            } else {
                this.format = new DecimalFormat(format, new DecimalFormatSymbols(locale));
                this.string = format;
                this.locale = locale;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#add(java.lang.Object, java.lang.Object)
         */
        @Override
        public Double add(Double augend, Double addend) {
            return parse(format(augend + addend));
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#clone()
         */
        @Override
        public DataType<Double> clone() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Double t1, Double t2) {
            double d1 = parse(format(t1));
            double d2 = parse(format(t2));
            d1 = d1 == -0.0d ? 0d : d1;
            d2 = d2 == -0.0d ? 0d : d2;
            return Double.valueOf(d1).compareTo(Double.valueOf(d2));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.String, java.lang.String)
         */
        @Override
        public int compare(final String s1, final String s2) throws NumberFormatException {
            try {
                double d1 = parse(s1);
                double d2 = parse(s2);
                d1 = d1 == -0.0d ? 0d : d1;
                d2 = d2 == -0.0d ? 0d : d2;
                return Double.valueOf(d1).compareTo(Double.valueOf(d2));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid value: '"+s1+"' or '"+s2+"'", e);
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#divide(java.lang.Object, java.lang.Object)
         */
        @Override
        public Double divide(Double dividend, Double divisor) {
            return parse(format(dividend / divisor));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#divide(java.lang.String, java.lang.String)
         */
        @Override
        public String divide(String dividend, String divisor) {
            Double d1 = parse(dividend);
            Double d2 = parse(divisor);
            return format(d1 / d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ARXDecimal other = (ARXDecimal) obj;
            if (string == null) { if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            if (getLocale() == null) { if (other.getLocale() != null) { return false; }
            } else if (!getLocale().equals(other.getLocale())) { return false; }
            return true;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#format(java.lang.Object)
         */
        @Override
        public String format(Double s){
            if (format==null){
                return String.valueOf(s);
            } else {
                return format.format(s);
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#getDescription()
         */
        @Override
        public DataTypeDescription<Double> getDescription(){
            return description;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithFormat#getFormat()
         */
        @Override
        public String getFormat() {
            return string;
        }
        
        /**
         * Returns the locale of the format.
         *
         * @return
         */
        public Locale getLocale() {
            if (this.locale == null) {
                return Locale.getDefault();
            } else {
                return locale;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#getMaximum()
         */
        @Override
        public Double getMaximum() {
            return Double.MAX_VALUE;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#getMinimum()
         */
        @Override
        public Double getMinimum() {
            return -Double.MAX_VALUE;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#hashCode()
         */
        @Override
        public int hashCode() { 
            if (string==null) {
                return getLocale().hashCode();
            }
            else {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((string == null) ? 0 : string.hashCode());
                result = prime * result + ((getLocale() == null) ? 0 : getLocale().hashCode());
                return result;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid(String s) {
            try {
                parse(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, double)
         */
        @Override
        public Double multiply(Double multiplicand, double multiplicator) {
            return parse(format(multiplicand * multiplicator));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, java.lang.Object)
         */
        @Override
        public Double multiply(Double multiplicand, Double multiplicator) {
            return parse(format(multiplicand * multiplicator));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, int)
         */
        @Override
        public Double multiply(Double multiplicand, int multiplicator) {
            return parse(format(multiplicand* multiplicator));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.String, java.lang.String)
         */
        @Override
        public String multiply(String multiplicand, String multiplicator) {
            Double d1 = parse(multiplicand);
            Double d2 = parse(multiplicator);
            return format(d1 * d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#parse(java.lang.String)
         */
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

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#ratio(java.lang.Object, java.lang.Object)
         */
        @Override
        public double ratio(Double dividend, Double divisor) {
            return parse(format(dividend / divisor));
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#subtract(java.lang.Object, java.lang.Object)
         */
        @Override
        public Double subtract(Double minuend, Double subtrahend) {
            return parse(format(minuend - subtrahend));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Decimal";
        }
    }

    /**
     * Base class for numeric types.
     *
     * @author Fabian Prasser
     */
    public static class ARXInteger extends DataType<Long> implements DataTypeWithFormat, DataTypeWithRatioScale<Long>  {
        
        /**  TODO */
        private static final long serialVersionUID = -631163546929231044L;

        /** The description of the data type. */
        private static final DataTypeDescription<Long> description = new DataTypeDescription<Long>(Long.class, "Integer", false, new ArrayList<String>()){
            private static final long serialVersionUID = -4498725217659811835L;
            @Override public DataType<Long> newInstance() { return INTEGER; }
            @Override public DataType<Long> newInstance(String format) {return createInteger(format);}
            @Override public DataType<Long> newInstance(String format, Locale locale) {return createInteger(format, locale);}
        };

        /** Format. */
        private final DecimalFormat                    format;

        /** Format string. */
        private final String                           string;

        /** Locale. */
        private final Locale                           locale;

        /**
         * Default constructor.
         */
        private ARXInteger(){
            this("Default");
        }
        
        /**
         * Create a numeric with a format string and default locale. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         * @param format
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXInteger(String format){
            if (format == null || format.equals("Default")){
                this.format = null;
                this.string = null;   
                this.locale = null;
            } else {
                this.format = new DecimalFormat(format);
                this.string = format;
                this.locale = null;
            }
        }
        
        /**
         * Create a numeric with a format string. Format strings must be valid formats
         * for <code>DecimalFormat</code>.
         *
         * @param format
         * @param locale
         * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
         */
        private ARXInteger(String format, Locale locale){
            if (format == null || format.equals("Default")){
                this.format = null;
                this.string = null;
                this.locale = locale;
            } else {
                this.format = new DecimalFormat(format, new DecimalFormatSymbols(locale));
                this.string = format;
                this.locale = locale;
            }
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#add(java.lang.Object, java.lang.Object)
         */
        @Override
        public Long add(Long augend, Long addend) {
            return augend + addend;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#clone()
         */
        @Override
        public DataType<Long> clone() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Long t1, Long t2) {
            return t1.compareTo(t2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.String, java.lang.String)
         */
        @Override
        public int compare(final String s1, final String s2) throws NumberFormatException {
            try {
                return parse(s1).compareTo(parse(s2));
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ": " + s1 +" or " + s2, e);
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#divide(java.lang.Object, java.lang.Object)
         */
        @Override
        public Long divide(Long dividend, Long divisor) {
            return (long)Math.round((double)dividend / (double)divisor);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#divide(java.lang.String, java.lang.String)
         */
        @Override
        public String divide(String dividend, String divisor) {
            Long d1 = parse(dividend);
            Long d2 = parse(divisor);
            return format(d1 / d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ARXInteger other = (ARXInteger) obj;
            if (string == null) { if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            if (getLocale() == null) { if (other.getLocale() != null) { return false; }
            } else if (!getLocale().equals(other.getLocale())) { return false; }
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#format(java.lang.Object)
         */
        @Override
        public String format(Long s){
            if (format==null){
                return String.valueOf(s);
            } else {
                return format.format(s);
            }
        }
        
        /**
         * Returns the locale of the format.
         *
         * @return
         */
        public Locale getLocale() {
            if (this.locale == null) {
                return Locale.getDefault();
            } else {
                return locale;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#getDescription()
         */
        @Override
        public DataTypeDescription<Long> getDescription(){
            return description;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithFormat#getFormat()
         */
        @Override
        public String getFormat() {
            return string;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#getMaximum()
         */
        @Override
        public Long getMaximum() {
            return Long.MAX_VALUE;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#getMinimum()
         */
        @Override
        public Long getMinimum() {
            return Long.MIN_VALUE;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#hashCode()
         */
        @Override
        public int hashCode() {
            if (string == null) {
                return getLocale().hashCode();
            }
            else {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((string == null) ? 0 : string.hashCode());
                result = prime * result + ((getLocale() == null) ? 0 : getLocale().hashCode());
                return result;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid(String s) {
            try {
                parse(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, double)
         */
        @Override
        public Long multiply(Long multiplicand, double multiplicator) {
            return (long)((double)multiplicand * multiplicator);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, int)
         */
        @Override
        public Long multiply(Long multiplicand, int multiplicator) {
            return multiplicand * multiplicator;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.Object, java.lang.Object)
         */
        @Override
        public Long multiply(Long multiplicand, Long multiplicator) {
            return (long)Math.round((double)multiplicand * (double)multiplicator);
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#multiply(java.lang.String, java.lang.String)
         */
        @Override
        public String multiply(String multiplicand, String multiplicator) {
            Long d1 = parse(multiplicand);
            Long d2 = parse(multiplicator);
            return format(d1 * d2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#parse(java.lang.String)
         */
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
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#ratio(java.lang.Object, java.lang.Object)
         */
        @Override
        public double ratio(Long dividend, Long divisor) {
            return (double)dividend / (double)divisor;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithRatioScale#subtract(java.lang.Object, java.lang.Object)
         */
        @Override
        public Long subtract(Long minuend, Long subtrahend) {
            return minuend - subtrahend;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Integer";
        }
    }
    
    /**
     * Base class for ordered string types.
     *
     * @author Fabian Prasser
     */
    public static class ARXOrderedString extends DataType<String> implements DataTypeWithFormat {

        /**  TODO */
        private static final long                        serialVersionUID = -830897705078418835L;

        /**  TODO */
        private final Map<String, Integer>               order;

        /** The description of the data type. */
        private static final DataTypeDescription<String> description = new DataTypeDescription<String>(String.class, "OrderedString", true, new ArrayList<String>()){
            private static final long serialVersionUID = -6300869938311742699L;
            @Override public DataType<String> newInstance() { return ORDERED_STRING; }
            @Override public DataType<String> newInstance(String format) {return createOrderedString(format);}
            @Override public DataType<String> newInstance(String format, Locale locale) {return createOrderedString(format);}
        };
        
        /**
         * Creates a new instance.
         */
        private ARXOrderedString(){
            this("Default");
        }

        /**
         * Creates a new instance.
         *
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

        /**
         * Creates a new instance.
         *
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
         * Creates a new instance.
         *
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
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#clone()
         */
        @Override
        public DataType<String> clone() {
            return this;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.String, java.lang.String)
         */
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

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            if (this.order == null) {
                if (((ARXOrderedString)obj).order != null) { 
                    return false;
                }
            } else {
                if (!((ARXOrderedString)obj).order.equals(this.order)) {
                    return false;
                }
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#format(java.lang.Object)
         */
        @Override
        public String format(String s){
            if (order != null && !order.containsKey(s)) {
                throw new IllegalArgumentException("Unknown string '"+s+"'");
            }
        	return s;
        }
        
        /**
         * Returns the locale of the format.
         *
         * @return
         */
        public Locale getLocale() {
            return Locale.getDefault();
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#getDescription()
         */
        @Override
        public DataTypeDescription<String> getDescription(){
            return description;
        }

        /**
         * Returns all elements backing this datatype.
         *
         * @return
         */
        public List<String> getElements() {
            List<String> result = new ArrayList<String>();
            if (order == null) {
                return result;
            }
            result.addAll(order.keySet());
            Collections.sort(result, new Comparator<String>(){
                @Override public int compare(String arg0, String arg1) {
                    return order.get(arg0).compareTo(order.get(arg1));
                }
            });
            return result;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType.DataTypeWithFormat#getFormat()
         */
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

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#hashCode()
         */
        @Override
        public int hashCode() {
            return ARXOrderedString.class.hashCode();
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid(String s) {
            if (order != null && !order.containsKey(s)) {
                return false;
            } else {
                return true;
            }
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#parse(java.lang.String)
         */
        @Override
        public String parse(String s) {
            if (order != null && !order.containsKey(s)) {
                throw new IllegalArgumentException("Unknown string '"+s+"'");
            }
        	return s;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "OrderedString";
        }
    }
    
    /**
     * Base class for string types.
     *
     * @author Fabian Prasser
     */
    public static class ARXString extends DataType<String> {
        
        /**  TODO */
        private static final long serialVersionUID = 903334212175979691L;
        
        /** The description of the data type. */
        private static final DataTypeDescription<String> description = new DataTypeDescription<String>(String.class, "String", false, new ArrayList<String>()){
            private static final long serialVersionUID = -6679110898204862834L;
            @Override public DataType<String> newInstance() { return STRING; }
            @Override public DataType<String> newInstance(String format) {return STRING;}
            @Override public DataType<String> newInstance(String format, Locale locale) {return STRING;}
        };
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#clone()
         */
        @Override
        public DataType<String> clone() {
            return this;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#compare(java.lang.String, java.lang.String)
         */
        @Override
        public int compare(final String s1, final String s2) {
            return s1.compareTo(s2);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            return true;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#format(java.lang.Object)
         */
        @Override
        public String format(String s){
            return s;
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#getDescription()
         */
        @Override
        public DataTypeDescription<String> getDescription(){
            return description;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#hashCode()
         */
        @Override
        public int hashCode() {
            return ARXString.class.hashCode();
        }
        
        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid(String s) {
            return true;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.DataType#parse(java.lang.String)
         */
        @Override
        public String parse(String s) {
            return s;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "String";
        }
    }
    
    /**
     * An entry in the list of available data types.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static abstract class DataTypeDescription<T> implements Serializable {

        /**  TODO */
        private static final long serialVersionUID = 6369986224526795419L;
        
        /** The wrapped java class. */
        private Class<?> clazz;
        
        /** If yes, a list of available formats. */
        private List<String> exampleFormats;
        
        /** Can the type be parameterized with a format string. */
        private boolean hasFormat;
        
        /** A human readable label. */
        private String label;
        
        /**
         * Internal constructor.
         *
         * @param clazz
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
         * Returns a list of example formats.
         *
         * @return
         */
        public List<String> getExampleFormats() {
            return exampleFormats;
        }
        
        /**
         * Returns a human readable label.
         *
         * @return
         */
        public String getLabel() {
            return label;
        }
        
        /**
         * Returns the wrapped java class.
         *
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
         * Creates a new instance with default format string and default locale.
         *
         * @return
         */
        public abstract DataType<T> newInstance();
        
        /**
         * Creates a new instance with the given format string and default locale.
         *
         * @param format
         * @return
         */
        public abstract DataType<T> newInstance(String format);

        /**
         * Creates a new instance with the given format string and the given locale.
         *
         * @param format
         * @param locale
         * @return
         */
        public abstract DataType<T> newInstance(String format, Locale locale);
    }

	/**
     * An interface for data types with format.
     *
     * @author Fabian Prasser
     */
    public static interface DataTypeWithFormat {
        
        /**
         * 
         *
         * @return
         */
        public abstract String getFormat();
        
        /**
         * 
         *
         * @return
         */
        public abstract Locale getLocale();
    }

    /**
     * An interface for data types with a ratio scale.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static interface DataTypeWithRatioScale<T> {
        
        /**
         * 
         *
         * @param augend
         * @param addend
         * @return
         */
        public abstract T add(T augend, T addend);

        /**
         * 
         *
         * @param s1
         * @param s2
         * @return
         * @throws NumberFormatException
         * @throws ParseException
         */
        public abstract int compare(String s1, String s2) throws NumberFormatException,
                                                                 ParseException;

        /**
         * 
         *
         * @param t1
         * @param t2
         * @return
         */
        public abstract int compare(T t1, T t2);

        /**
         * 
         *
         * @param dividend
         * @param divisor
         * @return
         */
        public abstract String divide(String dividend, String divisor);
        
        /**
         * 
         *
         * @param dividend
         * @param divisor
         * @return
         */
        public abstract T divide(T dividend, T divisor);
        
        /**
         * 
         *
         * @param t
         * @return
         */
        public abstract String format(T t);

        /**
         * 
         *
         * @return
         */
        public abstract DataTypeDescription<T> getDescription();

        /**
         * 
         *
         * @return
         */
        public T getMaximum();

        /**
         * 
         *
         * @return
         */
        public T getMinimum();
        
        /**
         * 
         *
         * @param s
         * @return
         */
        public abstract boolean isValid(String s);

        /**
         * 
         *
         * @param multiplicand
         * @param multiplicator
         * @return
         */
        public abstract String multiply(String multiplicand,
                                        String multiplicator);

        /**
         * 
         *
         * @param multiplicand
         * @param multiplicator
         * @return
         */
        public abstract T multiply(T multiplicand, double multiplicator);

        /**
         * 
         *
         * @param multiplicand
         * @param multiplicator
         * @return
         */
        public abstract T multiply(T multiplicand, int multiplicator);

        /**
         * 
         *
         * @param multiplicand
         * @param multiplicator
         * @return
         */
        public abstract T multiply(T multiplicand, T multiplicator);

        /**
         * 
         *
         * @param s
         * @return
         */
        public abstract T parse(String s);
        
        /**
         * 
         *
         * @param dividend
         * @param divisor
         * @return
         */
        public abstract double ratio(T dividend, T divisor);
        
        /**
         * 
         *
         * @param minuend
         * @param subtrahend
         * @return
         */
        public abstract T subtract(T minuend, T subtrahend);
    }
    
    /**  TODO */
    private static final long serialVersionUID = -4380267779210935078L;

    /** A date data type with default format dd.mm.yyyy */
    public static final DataType<Date>               DATE    = new ARXDate();

    /** A generic decimal data type. */
    public static final DataType<Double>             DECIMAL = new ARXDecimal();

    /** A generic integer data type. */
    public static final DataType<Long>               INTEGER = new ARXInteger();

    /** A string data type. */
    public static final DataType<String>             STRING  = new ARXString();

    /** A ordered string data type. */
    public static final DataType<String>             ORDERED_STRING  = new ARXOrderedString();
    
    /**
     * A date data type with given format.
     *
     * @param format
     * @return
     * @see SimpleDateFormat
     */
    public static final DataType<Date> createDate(final String format) {
        return new ARXDate(format);
    }

    /**
     * A date data type with given format.
     *
     * @param format
     * @param locale
     * @return
     * @see SimpleDateFormat
     */
    public static final DataType<Date> createDate(final String format, final Locale locale) {
        return new ARXDate(format, locale);
    }
    
    /**
     * A decimal data type with given format.
     *
     * @param format
     * @return
     * @see DecimalFormat
     */
    public static final DataType<Double> createDecimal(final String format) {
        return new ARXDecimal(format);
    }
    
    /**
     * Creates a decimal data type with a format string from the given locale.
     *
     * @param format
     * @param locale
     * @return
     */
    public static DataType<Double> createDecimal(String format, Locale locale) {
        return new ARXDecimal(format, locale);
    }
    
    /**
     * An integer data type with given format.
     *
     * @param format
     * @return
     * @see DecimalFormat
     */
    public static final DataType<Long> createInteger(final String format) {
        return new ARXInteger(format);
    }
    
    /**
     * An integer data type with given format using the given locale.
     *
     * @param format
     * @param locale
     * @return
     * @see DecimalFormat
     */
    public static final DataType<Long> createInteger(final String format, Locale locale) {
        return new ARXInteger(format, locale);
    }
    
    /**
     * A ordered string type with given format. 
     * 
     * @param format List of ordered strings
     * @return
     */
    public static final DataType<String> createOrderedString(final List<String> format) {
        return new ARXOrderedString(format);
    }
    
    /**
     * A ordered string type with given format. 
     * 
     * @param format List of ordered strings separated by line feeds
     * @return
     */
    public static final DataType<String> createOrderedString(final String format) {
        return new ARXOrderedString(format);
    }
    
    /**
     * A ordered string type with given format. 
     * 
     * @param format List of ordered strings
     * @return
     */
    public static final DataType<String> createOrderedString(final String[] format) {
        return new ARXOrderedString(format);
    }
    
    /**
     * Lists all available data types.
     *
     * @return
     */
    public static final List<DataTypeDescription<?>> list(){
        List<DataTypeDescription<?>> list = new ArrayList<DataTypeDescription<?>>();
        list.add(STRING.getDescription());
        list.add(ORDERED_STRING.getDescription());
        list.add(DATE.getDescription());
        list.add(DECIMAL.getDescription());
        list.add(INTEGER.getDescription());
        return list;
    }

    /**
     * 
     * Returns a datatype for the given class.
     *
     * @param <U>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <U> DataTypeDescription<U> list(Class<U> clazz){
        for (DataTypeDescription<?> entry : list()) {
            if (entry.getWrappedClass() == clazz) {
                return (DataTypeDescription<U>)entry;
            }
        }
        return null;
    }
    
    /**
     * Provides a list of example formats for the <code>Date</code> data type.
     *
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
        return result;
    }

    /**
     * Provides a list of example formats for the <code>Decimal</code> data type.
     *
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
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
     * Compare.
     *
     * @param t1
     * @param t2
     * @return
     */
    public abstract int compare(T t1, T t2);
    
    /**
     * Returns a new function builder.
     *
     * @return
     */
    public AggregateFunctionBuilder<T> createAggregate(){
        return AggregateFunction.forType(this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public abstract boolean equals(Object other);

    /**
     * Converts a value into a string.
     *
     * @param t
     * @return
     */
    public abstract String format(T t);
    
    /**
     * Returns a description of the data type.
     *
     * @return
     */
    public abstract DataTypeDescription<T> getDescription();

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public abstract int hashCode();

    /**
     * Checks whether the given string conforms to the data type's format.
     *
     * @param s
     * @return
     */
    public abstract boolean isValid(String s);
    
    /**
     * Converts a string into a value.
     *
     * @param s
     * @return
     */
    public abstract T parse(String s);
}
