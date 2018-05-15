/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for managing charsets. In comparison to the class <code>Charset</code> provided by the Java libraries,
 * this class restricts the set of available charset to the most common ones (ordered by frequency of use) 
 * and it provides consistent and beautified labels.
 * 
 * @author Fabian Prasser
 */
public class Charsets {

    /** Name to charset */
    private static Map<String, Charset> NAME_TO_CHARSET    = new HashMap<>();

    /** Default charset */
    private static String               DEFAULT_CHARSET;

    /** List of names of the most commonly used charsets*/
    private static String[]             MOST_USED_CHARSETS = { "UTF-8",
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

    /** List of objects representing the most commonly used charsets */
    private static final String[]       AVAILABLE_CHARSETS;

    static {

        // Prepare
        List<String> availableCharsets = new ArrayList<>();
        String defaultCharset = null;
        
        // For each name
        for (String name : MOST_USED_CHARSETS) {
            
            // Check
            Charset charset = null;
            try {
                charset = Charset.forName(name);
            } catch (Exception e) {
                // We can live with this
            }
            
            // If it exists
            if (charset != null) {

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
                boolean systemDefault = charset.equals(Charset.defaultCharset());
                if (systemDefault) {
                    builder.append(" (").append(Resources.getMessage("Charset.1")).append(")");
                    defaultCharset = builder.toString();
                }
                availableCharsets.add(builder.toString());
                NAME_TO_CHARSET.put(builder.toString(), charset);
            }
        }
        
        // Store
        AVAILABLE_CHARSETS = availableCharsets.toArray(new String[availableCharsets.size()]);
        if (defaultCharset != null) {
            DEFAULT_CHARSET = defaultCharset;
        } else {
            DEFAULT_CHARSET = AVAILABLE_CHARSETS[0];
        }
    }

    /**
     * Returns a charset for the given name
     * @param name
     * @return
     */
    public static Charset getCharsetForName(String name) {
        return NAME_TO_CHARSET.get(name);
    }
    
    /**
     * Returns the system's default charset
     * @return
     */
    public static String getNameOfDefaultCharset() {
        return DEFAULT_CHARSET;
    }
    
    /**
     * Returns a list of available charsets. The list is restricted to the most common charsets.
     * @return
     */
    public static String[] getNamesOfAvailableCharsets() {
        return AVAILABLE_CHARSETS;
    }
}
