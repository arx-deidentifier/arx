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

import org.apache.commons.validator.routines.CalendarValidator;
import org.apache.commons.validator.routines.DateValidator;

import java.util.Calendar;

/**
 * Pattern which matches Dates and years older than 89
 * @author David Gaßmann
 */
public class DatePattern implements ValuePattern {

    @Override
    public boolean matches(String value){
        if(value.isEmpty())
            return false;

        value = value.toLowerCase().trim();
        boolean isDate = this.isDate(value);
        if(isDate)
            return true;

        return this.isYearOlderThan89(value);
    }


    /**
     * @param value Cell content
     * @return True if input is a date
     */
    protected boolean isDate(String value){
        DateValidator validator = DateValidator.getInstance();

        String[] formats = new String[] {
                "yyyy-MM-dd",
                "mm/dd/yyyy",
                "mm/dd/yy"
        };
        for (String format : formats){
            if(validator.isValid(value, format))
                return true;
        }
        return false;
    }

    /**
     * @param value Cell content
     * @return True if input is a year and older than 89
     */
    protected boolean isYearOlderThan89(String value){
        if(value.length() != 4) //If it is not a year
            return false;

        CalendarValidator validator = CalendarValidator.getInstance();
        Calendar date = validator.validate(value, "yyyy");
        if(date == null)
            return false;
        int birthYear = validator.validate(value, "yyyy").get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return currentYear - birthYear > 89 && currentYear - birthYear < 130; //Filter out differences above 130, as humans do not get older than that
    }
}
