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
package org.deidentifier.arx.gui.resources;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for managing charsets
 * @author Fabian Prasser
 */
public class Charsets {

    /**
     * Class representing an available charset
     * @author Fabian Prasser
     */
    public static class AvailableCharset {

        /** Name */
        public final String  name;
        /** Name */
        public final Charset charset;
        /** System default */
        public final boolean systemDefault;

        /**
         * Creates a new instance
         * @param name
         * @param charset
         */
        AvailableCharset(String name, Charset charset) {

            // Format name of the charset
            name = name.toUpperCase().replace('_', '-').replace(' ', '-');
            char[] array = name.toCharArray();
            StringBuilder builder = new StringBuilder();
            builder.append(array[0]);
            for (int i = 1; i < array.length; i++) {
                if ((array[i - 1] != '-' && !Character.isDigit(array[i - 1])) && Character.isDigit(array[i])) {
                    builder.append("-");
                }
                builder.append(array[i]);
            }

            // Store
            this.charset = charset;
            this.systemDefault = charset.equals(Charset.defaultCharset());
            if (this.systemDefault) {
                builder.append(" (").append(Resources.getMessage("MainWindow.21")).append(")");
                DEFAULT_CHARSET = this;
            }
            this.name = builder.toString();
        }
    }

    /** Default charset*/
    private static AvailableCharset         DEFAULT_CHARSET;

    /** List of names of the most commonly used charsets*/
    private static String[]                 MOST_USED_CHARSETS = { "UTF-8",
                                                               "ISO-8859-1",
                                                               "latin1",
                                                               "Windows-1251",
                                                               "US-ASCII",
                                                               "Shift_JIS",
                                                               "Windows-1252",
                                                               "GB2312",
                                                               "EUC-KR",
                                                               "EUC-JP",
                                                               "GBK",
                                                               "ISO-8859-2",
                                                               "ISO-8859-15",
                                                               "Windows-1250",
                                                               "Windows-1256",
                                                               "ISO-8859-9",
                                                               "Big5",
                                                               "Windows-1254",
                                                               "Windows-874",
                                                               "US-ASCII",
                                                               "TIS-620",
                                                               "Windows-1255",
                                                               "ISO-8859-7",
                                                               "Windows-1253",
                                                               "KOI8-R",
                                                               "Windows-1257",
                                                               "UTF-16",
                                                               "UTF-16LE",
                                                               "UTF-16BE",
                                                               "ksc_5601",
                                                               "GB18030",
                                                               "Windows-31J",
                                                               "ISO-8859-5",
                                                               "ISO-8859-8",
                                                               "ISO-8859-4",
                                                               "ISO-8859-6",
                                                               "KOI8-U",
                                                               "ISO-2022-JP",
                                                               "ISO-8859-13",
                                                               "ISO-8859-3",
                                                               "Windows-949",
                                                               "Big5-HKSCS",
                                                               "Windows-1258",
                                                               "ISO-8859-11",
                                                               "IBM850" };

    /** List of objects representing the most commonly used charsets*/
    private static final AvailableCharset[] AVAILABLE_CHARSETS = getAvailableCharsets(MOST_USED_CHARSETS);

    /**
     * Returns a list of available charsets. The list is restricted to the most common charsets.
     * @return
     */
    public static AvailableCharset[] getAvailableCharsets() {
        return AVAILABLE_CHARSETS;
    }
    
    /**
     * Returns the system's default charset
     * @return
     */
    public static AvailableCharset getDefaultCharset() {
        return DEFAULT_CHARSET;
    }
    
    /**
     * Returns all available charsets from the given list of names
     * @param charsetsNames
     * @return
     */
    private static AvailableCharset[] getAvailableCharsets(String[] charsetsNames) {
        List<AvailableCharset> result = new ArrayList<>();
        for (String name : charsetsNames) {
            Charset charset = null;
            try {
                charset = Charset.forName(name);
            } catch (Exception e) {
                // We can live with this
            }
            if (charset != null) {
                result.add(new AvailableCharset(name, charset));
            } else {
                System.out.println(name);
            }
        }
        return result.toArray(new AvailableCharset[result.size()]);
    }
}
