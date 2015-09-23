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

package org.deidentifier.arx.risk.hipaa;

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
 * @author David Gaﬂmann
 */
public interface ValueMatcher {
    /**
     * Pattern which matches Dates and years older than 89
     * @author David Gaﬂmann
     */
    public class DatePattern implements ValueMatcher {
        
        @Override
        public boolean matches(String value) {
            if (value.isEmpty()) {
                return false;
            }
            
            value = value.toLowerCase().trim();
            boolean isDate = isDate(value);
            if (isDate) {
                return true;
            }
            
            return isYearOlderThan89(value);
        }
        
        /**
         * @param value Cell content
         * @return True if input is a date
         */
        protected boolean isDate(String value) {
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
        protected boolean isYearOlderThan89(String value) {
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
     * @author David Gaﬂmann
     */
    public class EMailPattern implements ValueMatcher {
        @Override
        public boolean matches(String value) {
            EmailValidator validator = EmailValidator.getInstance();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which matches IBAN account numbers
     * @author David Gaﬂmann
     */
    public class IBANPattern extends StringPattern {
        public IBANPattern() {
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
     * @author David Gaﬂmann
     */
    public class IPPattern implements ValueMatcher {
        @Override
        public boolean matches(String value) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which matches names with a predefined list of names
     * @author David Gaﬂmann
     */
    public class NamePattern implements ValueMatcher {
        Set<String> names = new HashSet<String>();
        
        public NamePattern() {
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
     * @author David Gaﬂmann
     */
    public class SSNPattern extends StringPattern {
        public SSNPattern() {
            super("[0-9]{3}-[0-9]{2}-[0-9]{4}|[0-9]{9}");
        }
    }
    
    /**
     * Pattern which matches a string with the provided regular expression
     * @author David Gaﬂmann
     */
    public class StringPattern implements ValueMatcher {
        Matcher matcher;
        
        public StringPattern(String regex) {
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
     * @author David Gaﬂmann
     */
    public class URLPattern implements ValueMatcher {
        @Override
        public boolean matches(String value) {
            UrlValidator validator = UrlValidator.getInstance();
            return validator.isValid(value);
        }
    }
    
    /**
     * Pattern which matches names a vehicle identification number
     * @author David Gaﬂmann
     */
    public class VINPattern extends StringPattern {
        public VINPattern() {
            super("[0-9A-Z]{17}");
        }
        
        @Override
        public boolean matches(String value) {
            value = value.replaceAll("\\s+", "").replaceAll("-", "");
            boolean match = super.matches(value);
            return match;
        }
    }
    
    /**
     * Pattern which matches a ZIP code
     * @author David Gaﬂmann
     */
    public class ZIPPattern implements ValueMatcher {
        private Set<String> zipCodes;
        
        public ZIPPattern() {
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
