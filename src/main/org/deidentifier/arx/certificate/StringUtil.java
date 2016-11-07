/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.certificate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * This class provides some utility methods for working with strings.
 *
 * @author Fabian Prasser
 */
public class StringUtil {

    /** Constant */
    private static final double LN2 = Math.log(2);      // = new Log().value(2);

    /** Constant */
    private static final double LN3 = Math.log(3);      //  = new Log().value(3);

    /**
     * Returns a pretty string representing the given double
     * @param value
     * @return
     */
    public static String getPrettyString(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        if (value == LN2) {
            return "ln(2)";
        } else if (value == LN3) {
            return "ln(3)";
        } else if (value == 0) {
            return "0";
        } else if (Math.abs(value) < 0.00001) {
            return new DecimalFormat("#.#####E0", symbols).format(value).replace('E', 'e');
        } else if (Math.abs(value) < 1) {
            return new DecimalFormat("#.#####", symbols).format(value);
        } else if (Math.abs(value) < 100000) {
            return new DecimalFormat("######.#####", symbols).format(value);
        } else {
            return String.valueOf(value).replace('E', 'e');
        }
    }

    /**
     * Returns a pretty string representing the given value
     * @param value
     * @return
     */
    public static String getPrettyString(int value) {
        return String.valueOf(value);
    }

    /**
     * Returns a pretty string representing the given value
     * @param value
     * @return
     */
    public static String getPrettyString(long value) {
        return String.valueOf(value);
    }
}