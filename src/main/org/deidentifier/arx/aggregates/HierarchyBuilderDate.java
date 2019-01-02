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
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;

/**
 * This class enables building hierarchies for dates.
 *
 * @author Fabian Prasser
 */
public class HierarchyBuilderDate extends HierarchyBuilder<Date> implements Serializable { // NO_UCD

    /**
     * A format-class for localization
     * 
     * @author Fabian Prasser
     */
    public static class Format implements Serializable {

        /** SVUID */
        private static final long        serialVersionUID = -4412882420968107563L;

        /** Map */
        private Map<Granularity, String> map              = new HashMap<Granularity, String>();
        
        /**
         * Default format
         */
        public Format() {
            map.put(Granularity.SECOND_MINUTE_HOUR_DAY_MONTH_YEAR, "dd.MM.yyyy-HH:mm:ss");
            map.put(Granularity.MINUTE_HOUR_DAY_MONTH_YEAR, "dd.MM.yyyy-HH:mm");
            map.put(Granularity.HOUR_DAY_MONTH_YEAR, "dd.MM.yyyy-HH:00");
            map.put(Granularity.DAY_MONTH_YEAR, "dd.MM.yyyy");
            map.put(Granularity.WEEK_MONTH_YEAR, "W/MM.yyyy");
            map.put(Granularity.WEEK_YEAR, "ww/yyyy");
            map.put(Granularity.MONTH_YEAR, "MM/yyyy");
        }

        /**
         * Returns whether a format for the given granularity is set
         * @param granularity
         * @return
         */
        public boolean contains(Granularity granularity) {
            return map.containsKey(granularity);
        }

        /**
         * Returns the format for the given granularity
         * @param granularity
         * @return
         */
        public String get(Granularity granularity) {
            return map.get(granularity);
        }

        /**
         * Checks whether the input string adheres to the pattern
         * @param input
         * @param pattern
         */
        public boolean isValid(String input, String pattern) {
            
            // Check for null
            if (input == null) {
                return false;
            }
            
            // Lists
            List<Character> listInput = patternAsList(input);
            List<Character> listPattern = patternAsList(pattern);
            
            
            // Compare
            if (!listInput.equals(listPattern)) {
                return false;
            }
            
            // Check if valid
            try {
                new SimpleDateFormat(input);
            } catch (Exception e) {
                return false;
            }
            
            // Return
            return true;
        }

        /**
         * Sets a form
         * @param granularity
         * @param format
         */
        public void set(Granularity granularity, String format) {
            if (granularity == null || format == null) {
                throw new IllegalArgumentException("Argument must not be null");
            }
            if (!granularity.isFormatSupported()) {
                throw new IllegalArgumentException("Format not supported for this granularity");
            }
            if (!isValid(format, granularity.format)) {
                throw new IllegalArgumentException("Illegal format string: '" + format + "'");
            }
            map.put(granularity, format);
        }

        /**
         * Helper function
         * @param input
         * @return
         */
        private List<Character> patternAsList(String input) {
            // Init
            List<Character> list = new ArrayList<>(); 
            
            // Input and pattern
            boolean ignore = false;
            for (char c : input.toCharArray()) {
                if (c == '\'') {
                    ignore = !ignore;
                } else if (!ignore && Character.isLetter(c)) {
                    list.add(new Character(c));
                }
            }
            
            // Sort and return
            Collections.sort(list);
            return list;
        }
    }
    
    /**
     * Granularity
     */
    public static enum Granularity {
        
        /**  Granularity */
        SECOND_MINUTE_HOUR_DAY_MONTH_YEAR("dd.MM.yyyy-HH:mm:ss"),
        /**  Granularity */
        MINUTE_HOUR_DAY_MONTH_YEAR("dd.MM.yyyy-HH:mm"),
        /**  Granularity */
        HOUR_DAY_MONTH_YEAR("dd.MM.yyyy-HH:00"),
        /**  Granularity */
        DAY_MONTH_YEAR("dd.MM.yyyy"),
        /**  Granularity */
        WEEK_MONTH_YEAR("W/MM.yyyy"),
        /**  Granularity */
        WEEK_YEAR("ww/yyyy"),
        /**  Granularity */
        MONTH_YEAR("MM/yyyy"),
        /**  Granularity */
        WEEKDAY("u"),
        /**  Granularity */
        WEEK("W"),
        /**  Granularity */
        QUARTER("MM", 4),
        /**  Granularity */
        YEAR("yyyy"),
        /**  Granularity */
        DECADE("yyyy", 10),
        /**  Granularity */
        CENTURY("yyyy", 100),
        /**  Granularity */
        MILLENIUM("yyyy", 1000);

        /** Format string */
        private String  format;
        /** Range */
        private Integer range;

        /**
         * Creates a new instance
         * @param format
         */
        private Granularity(String format) {
            this(format, null);
        }
        
        /**
         * Creates a new instance
         * @param format
         * @param range
         */
        private Granularity(String format, Integer range) {
            this.format = format;
            this.range = range;
        }
        
        /**
         * Returns the default format
         * @return
         */
        public String getDefaultFormat() {
            return this.format;
        }
        
        /**
         * Returns whether a format-string is supported by this granularity
         * @return
         */
        public boolean isFormatSupported() {
            return range == null;
        }
    }

    /** SVUID */
    private static final long    serialVersionUID = 6294885577802586286L;

    /**
     * Creates an hierarchy reflecting the given granularities
     *
     * @param type
     * @param granularities
     * @return
     */
    public static HierarchyBuilder<Date> create(DataType<Date> type, Granularity... granularities){
        return create(type, null, new Format(), granularities);
    }
    
    /**
     * Creates an hierarchy reflecting the given granularities
     *
     * @param type
     * @param timeZone
     * @param granularities
     * @return
     */
    public static HierarchyBuilder<Date> create(DataType<Date> type,
                                                TimeZone timeZone,
                                                Format format,
                                                Granularity... granularities){
        return new HierarchyBuilderDate(type, timeZone, format, null, null, granularities);
    }

    /**
     * Creates an hierarchy reflecting the given granularities
     *
     * @param type
     * @param timeZone
     * @param bottomCoding
     * @param topCoding
     * @param granularities
     * @return
     */
    public static HierarchyBuilder<Date> create(DataType<Date> type,
                                                TimeZone timeZone,
                                                Format format,
                                                Date bottomCoding,
                                                Date topCoding,
                                                Granularity... granularities){
        return new HierarchyBuilderDate(type, timeZone, format, bottomCoding, topCoding, granularities);
    }

    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static HierarchyBuilder<Date> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilderDate result = (HierarchyBuilderDate)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }

    /** Result */
    private transient String[][] result;
    /** Granularities */
    private Granularity[]        granularities;
    /** Timezones */
    private TimeZone             timeZone     = TimeZone.getDefault();
    /** Format */
    private Format               format       = new Format();
    /** Type */
    private final ARXDate        datatype;
    /** Top coding */
    private Date                 topCoding    = null;
    /** Bottom coding */
    private Date                 bottomCoding = null;

    /**
     * Creates an hierarchy reflecting the given granularities
     * 
     * @param type
     * @param timeZone
     * @param format
     * @param bottomCoding
     * @param topCoding
     * @param granularities
     */
    private HierarchyBuilderDate(DataType<Date> type,
                                 TimeZone timeZone,
                                 Format format,
                                 Date bottomCoding,
                                 Date topCoding,
                                 Granularity... granularities){
        super(Type.DATE_BASED);
        this.datatype = (ARXDate)type;
        this.granularities = granularities;
        this.timeZone = timeZone;
        this.format = format;
        this.bottomCoding = bottomCoding;
        this.topCoding = topCoding;
        Arrays.sort(this.granularities);
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @return
     */
    public Hierarchy build(){
        
        // Check
        if (result == null) {
            throw new IllegalArgumentException("Please call prepare() first");
        }
        
        // Return
        Hierarchy h = Hierarchy.create(result);
        this.result = null;
        return h;
    }

    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @param data
     * @return
     */
    public Hierarchy build(String[] data){
        prepare(data);
        return build();
    }

    /**
     * @return the bottomCoding
     */
    public Date getBottomCodingBound() {
        return bottomCoding;
    }

    /**
     * @return the format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Returns the granularities
     * @return the granularities
     */
    public Granularity[] getGranularities() {
        return granularities;
    }

    /**
     * @return the outputTimeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @return the topCoding
     */
    public Date getTopCodingBound() {
        return topCoding;
    }

    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     *
     * @param data
     * @return
     */
    public int[] prepare(String[] data){
        
        // Check
        if (this.bottomCoding != null && this.topCoding != null) {
            if (!this.bottomCoding.before(this.topCoding)) {
                throw new IllegalArgumentException("Bottom coding bound must be lower than top coding bound");
            }
        }
        
        // Build result
        this.result = new String[data.length][granularities.length + 1];
        for (int i = 0; i < data.length; i++) {
            result[i] = new String[granularities.length + 1];
            result[i][0] = data[i];
            for (int j = 0; j < granularities.length; j++) {
                String output = generalize(data[i], granularities[j]);
                result[i][j + 1] = output;
            }
        }

        // Compute
        int[] sizes = new int[this.result[0].length];
        for (int i=0; i < sizes.length; i++){
            Set<String> set = new HashSet<String>();
            for (int j=0; j<this.result.length; j++) {
                set.add(result[j][i]);
            }
            sizes[i] = set.size();
        }
        
        // Return
        return sizes;
    }

    /**
     * @param bottomCoding the bottomCoding to set
     */
    public void setBottomCodingBound(Date bottomCoding) {
        this.bottomCoding = bottomCoding;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * Sets the granularities
     * @param granularities
     */
    public void setGranularities(Granularity[] granularities) {
        this.granularities = granularities;
        Arrays.sort(this.granularities);
    }

    /**
     * @param timeZone the time zone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    /**
     * @param topCoding the topCoding to set
     */
    public void setTopCodingBound(Date topCoding) {
        this.topCoding = topCoding;
    }

    /**
     * Applies a generalization function
     * @param input
     * @param granularity
     * @return
     */
    private String generalize(String input, Granularity granularity) {
        
        // Null to null
        if (ARXDate.isNull(input)) {
            return ARXDate.NULL_VALUE;
        }
        
        // Format
        String _format = (format != null && format.contains(granularity)) ? format.get(granularity) : granularity.format;
        Integer _range = granularity.range;
        
        // Init
        SimpleDateFormat sdf = new SimpleDateFormat(_format);
        if (this.timeZone != null) {
            sdf.setTimeZone(this.timeZone);
        }
        
        Date date = datatype.parse(input);

        // Bottom coding
        if (bottomCoding != null) {
            if (date.before(bottomCoding)) {
                return "<" + datatype.format(bottomCoding, this.timeZone);
            }
        }

        // Top coding
        if (topCoding != null) {
            if (date.after(topCoding) || date.equals(topCoding)) {
                return ">=" + datatype.format(topCoding, this.timeZone);
            }
        }
        
        // Range mapping
        if (_range == null) {
            return sdf.format(date);
        } else {
            int year = Integer.valueOf(sdf.format(date));
            int lower = (year / _range) * _range;
            int upper = ((year / _range) + 1) * _range;
            return "[" + lower + ", " + upper + "[";
        }
    }
}