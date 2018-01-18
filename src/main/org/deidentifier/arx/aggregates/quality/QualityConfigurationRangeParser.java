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
package org.deidentifier.arx.aggregates.quality;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parser for ranges
 * @author Fabian Prasser
 */
public abstract class QualityConfigurationRangeParser {
    
    /** Result buffer*/
    private final double[] doubleResult = new double[2];
    
    /** Result buffer*/
    private final String[] stringResult = new String[2];

    /**
     * List of all available parsers
     */
    private static final QualityConfigurationRangeParser[] PARSERS = new QualityConfigurationRangeParser[]{
        new QualityConfigurationRangeParser() {
            @Override protected String[] getRange(String value) {
                
                // Masked values
                if (value.contains("*")) {
                    return pack(value.replace('*', '0'), value.replace('*', '9'));
                }

                // Does not match
                return pack(null, null);
            }
        },
        new QualityConfigurationRangeParser() {
            @Override protected String[] getRange(String value) {
                
                // Common interval formats
                if (value.startsWith("[") && value.endsWith("[") ||
                    value.startsWith("[") && value.endsWith("]") ||
                    value.startsWith("{") && value.endsWith("}") ||
                    value.startsWith("[") && value.endsWith(")")) {
                    int index = value.indexOf(",");
                    if (index == -1) {
                        index = value.indexOf(";");
                    }
                    if (index != -1) {
                        return pack(value.substring(1, index), value.substring(index + 1, value.length() - 1));
                    }
                }
                
                // Does not match
                return pack(null, null);
            }
        },
        new QualityConfigurationRangeParser() {
            @Override protected String[] getRange(String value) {
                
                // Format
                if (value.startsWith("<")) {
                    if (value.startsWith("<=")) {
                        return pack(null, value.substring(2, value.length()));
                    } else {
                        return pack(null, value.substring(1, value.length()));
                    }
                } else if (value.startsWith(">")) {
                    if (value.startsWith(">=")) {
                        return pack(value.substring(2, value.length()), null);
                    } else {
                        return pack(value.substring(1, value.length()), null);
                    }
                } 
                
                // Does not match
                return pack(null, null);
            }
        }
    };
    
    /**
     * Tries to find a matching parser
     * @param valueParser
     * @param values
     * @return
     */
    public static final QualityConfigurationRangeParser getParser(QualityConfigurationValueParser<?> valueParser, 
                                                                  List<String> values) {
        
        // Try all available parsers
        Map<QualityConfigurationRangeParser, Integer> parsers = new HashMap<>();
        for (QualityConfigurationRangeParser parser : PARSERS) {
            int matches = 0;
            for (String value : values) {
                if (parser.accepts(valueParser, value)) {
                    matches++;
                }
            }
            parsers.put(parser, matches);
        }
        
        // Extract best one
        int matches = 0;
        QualityConfigurationRangeParser parser = null;
        for (Entry<QualityConfigurationRangeParser, Integer> entry : parsers.entrySet()) {
            if (entry.getValue() > matches) {
                matches = entry.getValue();
                parser = entry.getKey();
            }
        }
        
        // Return parser
        return parser;
    }
    
    /**
     * Returns whether the parser matches the value
     * @param parser
     * @param value
     * @return
     */
    protected boolean accepts(QualityConfigurationValueParser<?> parser, String value) {
        String[] range = getRange(value.replaceAll("\\s","")); // Trim
        return (range[0] != null && parser.accepts(range[0])) || (range[1] != null && parser.accepts(range[1]));
    }
    
    /**
     * Implement this method to provide a parser
     * @param value
     * @return
     */
    protected abstract String[] getRange(String value);

    /**
     * Pack function
     * @param min
     * @param max
     * @return
     */
    protected final double[] pack(double min, double max) {
        doubleResult[0] = min;
        doubleResult[1] = max;
        return doubleResult;
    }

    /**
     * Pack function
     * @param min
     * @param max
     * @return
     */
    protected final String[] pack(String min, String max) {
        stringResult[0] = min;
        stringResult[1] = max;
        return stringResult;
    }

    /**
     * Parses different forms of transformed (but not suppressed) values
     * @param parser
     * @param value
     * @param minimum
     * @param maximum
     * @return
     * @throws ParseException 
     */
    double[] getRange(QualityConfigurationValueParser<?> parser, String value, double minimum, double maximum) {
        
        // Parse
        String[] range = getRange(value.replaceAll("\\s","")); // Trim
        double min = range[0] == null ? minimum : parser.getDouble(range[0]);
        double max = range[1] == null ? maximum : parser.getDouble(range[1]);

        // Truncate
        max = Math.min(max, maximum);
        min = Math.max(min, minimum);
     
        // Return
        return pack(min, max);
    }
}
