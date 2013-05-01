/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class provides access to the data types supported by the FLASH framework
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class DataType {

    private static class FLASHDate extends DataType {

        SimpleDateFormat format;
        String           string;

        private FLASHDate() {
            string = "dd.MM.yyyy";
            format = new SimpleDateFormat(string);
        }

        private FLASHDate(final String format) {
            this.format = new SimpleDateFormat(format);
            string = format;
        }

        @Override
        public DataType clone() {
            return new FLASHDate(string);
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
            final FLASHDate other = (FLASHDate) obj;
            if (string == null) {
                if (other.string != null) { return false; }
            } else if (!string.equals(other.string)) { return false; }
            return true;
        }

        @Override
        public String toString() {
            return "Date(" + string + ")";
        }
    }

    private static class FLASHDecimal extends DataType {
        @Override
        public DataType clone() {
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
    }

    private static class FLASHString extends DataType {
        @Override
        public DataType clone() {
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
    }

    /** A decimal datatype */
    public static final DataType DECIMAL = new FLASHDecimal();

    /** A string datatype */
    public static final DataType STRING  = new FLASHString();

    /** A date datatype with default fomat dd.mm.yyyy */
    public static final DataType DATE    = new FLASHDate();

    /**
     * A date datatype with given format
     * 
     * @see SimpleDateFormat
     * @param format
     * @return
     */
    public static final DataType DATE(final String format) {
        return new FLASHDate(format);
    }

    @Override
    public abstract DataType clone();

    public abstract int
            compare(String s1, String s2) throws NumberFormatException,
                                         ParseException;

}
