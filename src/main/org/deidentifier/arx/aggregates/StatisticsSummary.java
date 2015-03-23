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
package org.deidentifier.arx.aggregates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * A base class for summary statistics
 * @author Fabian Prasser
 *
 */
public class StatisticsSummary {
    
    /**
     * Summary statistics for variables with ordinal scale
     * @author Fabian Prasser
     *
     */
    static final class SummaryStatisticsOrdinal {

        /** Var */
        private final DataType<?>  type;
        /** Var */
        private final List<String> values = new ArrayList<String>();
        /** Var */
        private String             mode;
        /** Var */
        private String             median;
        /** Var */
        private String             min;
        /** Var */
        private String             max;
        /** Var */
        private int                numberOfMeasures;

        /**
         * Constructor
         * @param type
         */
        SummaryStatisticsOrdinal(DataType<?> type) {
            this.type = type;
        }
        
        /**
         * Adds a value
         * @param value
         */
        void addValue(String value) {
            this.values.add(value);
        }
        
        void analyze() {
            Collections.sort(values, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    try {
                        return type.compare(o1, o2);
                    } catch (NumberFormatException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            
            if (values.size() == 0) {
                min = null;
                max = null;
                mode = null;
                median = null;
                numberOfMeasures = 0;
            } else {
                
                // Determine simple things
                min = values.get(0);
                max = values.get(values.size() - 1);
                median = values.get(values.size() / 2);
                numberOfMeasures = values.size();
                
                // Determine mode
                int count = 0;
                int index = 0;
                mode = values.get(0);
                while (index < values.size()) {
                    int nIndex = moveWhileEqual(index, values);
                    int nCount = nIndex - index;
                    if (nCount > count) {
                        mode = values.get(index);
                        count = nCount;
                    }
                    index = nIndex;
                }
            }
            
            // Clear
            values.clear();
        }

        /** 
         * Returns a summary
         * @return
         */
        public String getMode() {
            return mode;
        }

        /**
         * Returns a summary
         * @return
         */
        public String getMedian() {
            return median;
        }

        /**
         * Returns a summary
         * @return
         */
        public String getMin() {
            return min;
        }

        /**
         * Returns a summary
         * @return
         */
        public String getMax() {
            return max;
        }

        /**
         * Returns the number of measurements
         * @return
         */
        public int getNumberOfMeasures() {
            return numberOfMeasures;
        }

        /**
         * Returns the index of the next element that does not equal the element at the given index
         * @param index
         * @param values
         * @return
         */
        private int moveWhileEqual(int index, List<String> values) {
            String element = values.get(index);
            // We can do == because of dictionary compression
            while (values.get(index) == element) { 
                index++;
            }
            return index;
        }
    }
    
    /**
     * An enum for scales of measure
     * @author Fabian Prasser
     */
    public static enum ScaleOfMeasure {
        NOMINAL,
        ORDINAL,
        INTERVAL,
        RATIO
    }
    
    /** The associated scale of measure*/
    public final ScaleOfMeasure scale;
    
    /** The number of measures*/
    public final int numberOfMeasures;

    /* ******************************************************************** 
     * ARXString, ARXOrderedString, ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/
    
    /** Mode*/
    public final String mode;
    
    /* ******************************************************************** 
     * ARXOrderedString, ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/
    
    /** Median, may be null*/
    public final String median;
    /** Min, may be null*/
    public final String min;
    /** Max, may be null*/
    public final String max;
    
    /* ********************************************************************  
     * ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/
    
    /** Arithmetic mean, may be null*/
    public final String arithmeticMean;
    /** Sample variance, may be null*/
    public final String sampleVariance;
    /** Population variance, may be null*/
    public final String populationVariance;
    /** Range, may be null*/
    public final String range;
    /** Kurtosis, may be null*/
    public final String kurtosis;
    
    /* ********************************************************************  
     * ARXInteger, ARXDecimal 
     ********************************************************************* */
    
    /** Geometric mean, may be null*/
    public final String geometricMean;

    /**
     * Constructor for ARXInteger and ARXDecimal 
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param median
     * @param min
     * @param max
     * @param arithmeticMean
     * @param sampleVariance
     * @param populationVariance
     * @param range
     * @param kurtosis
     * @param geometricMean
     */
    StatisticsSummary(    ScaleOfMeasure scale,
                                  int numberOfMeasures,
                                  String mode,
                                  String median,
                                  String min,
                                  String max,
                                  String arithmeticMean,
                                  String sampleVariance,
                                  String populationVariance,
                                  String range,
                                  String kurtosis,
                                  String geometricMean) {
        this.numberOfMeasures = numberOfMeasures;
        this.scale = scale;
        this.mode = mode;
        this.median = median;
        this.min = min;
        this.max = max;
        this.arithmeticMean = arithmeticMean;
        this.sampleVariance = sampleVariance;
        this.populationVariance = populationVariance;
        this.range = range;
        this.kurtosis = kurtosis;
        this.geometricMean = geometricMean;
    }

    /**
     * Constructor for ARXDate 
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param median
     * @param min
     * @param max
     * @param arithmeticMean
     * @param sampleVariance
     * @param populationVariance
     * @param range
     * @param kurtosis
     */
    StatisticsSummary(    ScaleOfMeasure scale,
                                  int numberOfMeasures,
                                  String mode,
                                  String median,
                                  String min,
                                  String max,
                                  String arithmeticMean,
                                  String sampleVariance,
                                  String populationVariance,
                                  String range,
                                  String kurtosis) {
        this(scale, numberOfMeasures, mode, median, min, max, arithmeticMean, sampleVariance, populationVariance, range, kurtosis, null);
    }

    /**
     * Constructor for ARXOrderedString
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param median
     * @param min
     * @param max
     */
    StatisticsSummary(    ScaleOfMeasure scale,
                                  int numberOfMeasures,
                                  String mode,
                                  String median,
                                  String min,
                                  String max) {
        this(scale, numberOfMeasures, mode, median, min, max, null, null, null, null, null, null);
    }

    /**
     * Constructor for ARXString
     * @param scale
     * @param numberOfMeasures
     * @param mode
     */
    StatisticsSummary(ScaleOfMeasure scale, int numberOfMeasures, String mode) {
        this(scale, numberOfMeasures, mode, null, null, null, null, null, null, null, null, null);
    }
}
