/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor.Order;
import org.deidentifier.arx.AttributeType.Hierarchy;

/**
 * This class enables building hierarchies for dates.
 *
 * @author Fabian Prasser
 */
public class HierarchyBuilderDate extends HierarchyBuilder<Date> implements Serializable {

    /**
     * Granularity
     */
    public static enum Granularity {
        
        /**  Granularity */
        SECOND_MINUTE_HOUR_DAY_MONTH_YEAR("yyyy-MM-dd 'at' HH:mm:ss"),
        /**  Granularity */
        MINUTE_HOUR_DAY_MONTH_YEAR("yyyy-MM-dd 'at' HH:mm"),
        /**  Granularity */
        HOUR_DAY_MONTH_YEAR("yyyy-MM-dd 'at' HH"),
        /**  Granularity */
        DAY_MONTH_YEAR("yyyy-MM-dd"),
        /**  Granularity */
        WEEK_MONTH_YEAR("yyyy-MM-W '(week)'"),
        /**  Granularity */
        WEEK_YEAR("yyyy-ww '(week)'"),
        /**  Granularity */
        MONTH_YEAR("yyyy-MM"),
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
    }
    
    public static enum Locale {
        EN,
        EU
    }

    /** SVUID */
    private static final long serialVersionUID = 6294885577802586286L;

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

    /**
     * Creates an hierarchy reflecting the given granularities
     *
     * @param granularities
     * @return
     */
    public static HierarchyBuilder<Date> create(Granularity... granularities){
        return new HierarchyBuilderDate(null, null, granularities);
    }
    
    /**
     * Creates an hierarchy reflecting the given granularities
     *
     * @param outputTimeZone
     * @param outputLocale
     * @param granularities
     * @return
     */
    public static HierarchyBuilder<Date> create(TimeZone outputTimeZone,
                                                Locale outputLocale,
                                                Granularity... granularities){
        return new HierarchyBuilderDate(outputTimeZone, outputLocale, granularities);
    }

    /** Result */
    private transient String[][] result;
    /** Granularities */
    private Granularity[]        granularities;
    /** Timezones */
    private TimeZone             outputTimeZone;
    /** Locales */
    private Locale               outputLocale;

    /**
     * Creates an hierarchy reflecting the given granularities
     * 
     * @param outputTimeZone
     * @param outputLocale
     * @param granularities
     */
    private HierarchyBuilderDate(TimeZone outputTimeZone,
                                 Locale outputLocale,
                                 Granularity... granularities){
        super(Type.DATE_BASED);
        this.granularities = granularities;
        this.outputTimeZone = outputTimeZone;
        this.outputLocale = outputLocale;
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
     * Returns the granularities
     * @return the granularities
     */
    public Granularity[] getGranularities() {
        return granularities;
    }

    /**
     * @return the outputLocale
     */
    public Locale getLocale() {
        return outputLocale;
    }

    /**
     * @return the outputTimeZone
     */
    public TimeZone getTimeZone() {
        return outputTimeZone;
    }

    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     *
     * @param data
     * @return
     */
    public int[] prepare(String[] data){
        
        // Check
        if (this.result == null) {
            prepareResult(data);
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
     * Sets the granularities
     * @param granularities
     */
    public void setGranularities(Granularity[] granularities) {
        this.granularities = granularities;
        Arrays.sort(this.granularities);
    }

    /**
     * @param outputLocale the outputLocale to set
     */
    public void setLocale(Locale outputLocale) {
        this.outputLocale = outputLocale;
    }
    
    /**
     * @param outputTimeZone the outputTimeZone to set
     */
    public void setTimeZone(TimeZone outputTimeZone) {
        this.outputTimeZone = outputTimeZone;
    }

    /**
     * Computes the hierarchy.
     *
     * @param data
     */
    private void prepareResult(String[] data){

        // Build result
        this.result = new String[data.length][granularities.length + 1];
        for (int i=0; i<data.length; i++){
            result[i] = new String[granularities.length + 1];
            result[i][0] = data[i];
            for (int j=0; j<granularities.length; j++){
                String output = generalize(data[i], granularities[j]);
                result[i][j+1] = output;
            }
        }
    }

    /**
     * Applies a generalization function
     * @param input
     * @param granularity
     * @return
     */
    private String generalize(String input, Granularity granularity) {
        String format = granularity.format;
        Integer range = granularity.range;
        return null;
    }
}
