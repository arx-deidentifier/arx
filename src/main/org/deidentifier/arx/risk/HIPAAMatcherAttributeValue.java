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

package org.deidentifier.arx.risk;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.CalendarValidator;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

/**
 * Interfaces the patterns
 * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
 */
interface HIPAAMatcherAttributeValue {
    
    /**
     * Pattern which matches Dates and years older than 89
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherDate implements HIPAAMatcherAttributeValue {
        
        @Override
        public boolean matches(String value) {
            if (value.isEmpty()) {
                return false;
            }
            
            value = value.toLowerCase().trim();

            if (isDate(value)) {
                return true;
            }
            
            return isYearOlderThan89(value);
        }
        
        /**
         * @param value Cell content
         * @return True if input is a date
         */
        private boolean isDate(String value) {
            DateValidator validator = DateValidator.getInstance();
            
            String[] formats = new String[] {
                                              "yyyy-MM-dd",
                                              "mm/dd/yyyy",
                                              "mm/dd/yy"
            };
            for (String format : formats) {
                if (validator.isValid(value, format)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * @param value Cell content
         * @return True if input is a year and older than 89
         */
        private boolean isYearOlderThan89(String value) {
            if (value.length() != 4) {
                return false;
            }
            
            CalendarValidator validator = CalendarValidator.getInstance();
            Calendar date = validator.validate(value, "yyyy");
            if (date == null) {
                return false;
            }
            int birthYear = validator.validate(value, "yyyy").get(Calendar.YEAR);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return ((currentYear - birthYear) > 89) && ((currentYear - birthYear) < 130); // Filter out differences above 130, as humans do not get older than that
        }
    }
    
    /**
     * Pattern which matches email addresses
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherEMail implements HIPAAMatcherAttributeValue {
        @Override
        public boolean matches(String value) {
            EmailValidator validator = EmailValidator.getInstance();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which matches IBAN account numbers
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherIBAN extends HIPAAMatcherString {
        HIPAAMatcherIBAN() {
            super("[a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]{7}([a-zA-Z0-9]?){0,16}");
        }
        
        @Override
        public boolean matches(String value) {
            if (!super.matches(value)) {
                return false;
            }
            
            value = value.replaceAll("\\s+", "");
            IBANCheckDigit validator = new IBANCheckDigit();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which maches IPv4 and IPv6 addresses
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherIP implements HIPAAMatcherAttributeValue {
        @Override
        public boolean matches(String value) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which matches names with a predefined list of names
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherName implements HIPAAMatcherAttributeValue {
        Set<String> names = new HashSet<String>();
        
        HIPAAMatcherName() {
            names.add("john");
            names.add("doe");
            names.add("max");
        }
        
        @Override
        public boolean matches(String value) {
            value = value.trim().toLowerCase();
            return names.contains(value);
        }
    }
    
    /**
     * Pattern which matches the social security numbers
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherSSN extends HIPAAMatcherString {
        HIPAAMatcherSSN() {
            super("[0-9]{3}-[0-9]{2}-[0-9]{4}|[0-9]{9}");
        }
    }
    
    /**
     * Pattern which matches a string with the provided regular expression
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    abstract class HIPAAMatcherString implements HIPAAMatcherAttributeValue {
        Matcher matcher;
        
        HIPAAMatcherString(String regex) {
            Pattern pattern = Pattern.compile(regex);
            matcher = pattern.matcher("");
        }
        
        @Override
        public boolean matches(String value) {
            return matcher.reset(value).matches();
            
        }
    }
    
    /**
     * Pattern which matches an URL
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherURL implements HIPAAMatcherAttributeValue {
        @Override
        public boolean matches(String value) {
            UrlValidator validator = UrlValidator.getInstance();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which matches names a vehicle identification number
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherVIN extends HIPAAMatcherString {
        HIPAAMatcherVIN() {
            super("[0-9A-Z]{17}");
        }
        
        @Override
        public boolean matches(String value) {
            value = value.replaceAll("\\s+", "").replaceAll("-", "");
            return super.matches(value);
        }
    }
    
    /**
     * Pattern which matches a ZIP code
     * @author Florian Kohlmayer, Fabian Prasser, David Gaﬂmann
     */
    class HIPAAMatcherZIP implements HIPAAMatcherAttributeValue {
        private Set<String> zipCodes;
        
        HIPAAMatcherZIP() {
            zipCodes = new HashSet<>();
            
            zipCodes.add("036");
            zipCodes.add("059");
            zipCodes.add("063");
            zipCodes.add("102");
            zipCodes.add("203");
            zipCodes.add("556");
            
            zipCodes.add("692");
            zipCodes.add("790");
            zipCodes.add("821");
            zipCodes.add("823");
            zipCodes.add("830");
            zipCodes.add("831");
            
            zipCodes.add("878");
            zipCodes.add("879");
            zipCodes.add("884");
            zipCodes.add("890");
            zipCodes.add("893");
        }
        
        @Override
        public boolean matches(String value) {
            if (!value.contains("-")) {
                return false;
            }
            
            value = value.replaceAll("\\s+", "").replaceAll("-", "");
            
            if (value.length() < 3) {
                return false;
            }
            
            String zipCode = value.substring(0, 3);
            return zipCodes.contains(zipCode);
        }
    }
    
    /**
     * Returns true if the value matches the given Pattern.
     * @param value
     * @return
     */
    public boolean matches(String value);
}
