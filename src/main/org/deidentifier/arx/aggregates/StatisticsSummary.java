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
     * An enum for scales of measure
     * @author Fabian Prasser
     */
    public static enum ScaleOfMeasure {
        NOMINAL("Nominal scale"),
        ORDINAL("Ordinal scale"),
        INTERVAL("Interval scale"),
        RATIO("Ratio scale");
        
        /** Label*/
        private final String label;
        
        /**
         * Construct
         * @param label
         */
        private ScaleOfMeasure(String label) {
            this.label = label;
        }
        
         @Override
        public String toString() {
            return label;
        }
    }
    
    /**
     * Summary statistics for variables with ordinal scale
     * @author Fabian Prasser
     *
     */
    static final class StatisticsSummaryOrdinal {

        /** Var */
        private final Comparator<String> comparator;
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
        StatisticsSummaryOrdinal(final DataType<?> type) {
            this.comparator = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    try {
                        return type.compare(o1, o2);
                    } catch (NumberFormatException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        /**
         * Constructor
         * @param comparator
         */
        StatisticsSummaryOrdinal(final Comparator<String> comparator) {
            this.comparator = comparator;
        }
        
        /**
         * Returns a summary
         * @return
         */
        public String getMax() {
            return max;
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
        public String getMode() {
            return mode;
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
            while (index < values.size() && values.get(index) == element) { 
                index++;
            }
            return index;
        }

        /**
         * Adds a value
         * @param value
         */
        void addValue(String value) {
            this.values.add(value);
        }

        void analyze() {
            Collections.sort(values, comparator);
            
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
    }
    
    /** The associated scale of measure*/
    private final ScaleOfMeasure scale;
    
    /** The number of measures*/
    private final int numberOfMeasures;

    /* ******************************************************************** 
     * ARXString, ARXOrderedString, ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/
    
    /** Mode*/
    private final String mode;
    
    /* ******************************************************************** 
     * ARXOrderedString, ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/
    
    /** Median, may be null*/
    private final String median;
    /** Min, may be null*/
    private final String min;
    /** Max, may be null*/
    private final String max;
    
    /* ********************************************************************  
     * ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/
    
    /** Arithmetic mean, may be null*/
    private final String arithmeticMean;
    /** Sample variance, may be null*/
    private final String sampleVariance;
    /** Population variance, may be null*/
    private final String populationVariance;
    /** Std.dev, may be null*/
    private final String stdDev;
    /** Range, may be null*/
    private final String range;
    /** Kurtosis, may be null*/
    private final String kurtosis;
    
    /* ********************************************************************  
     * ARXInteger, ARXDecimal 
     ********************************************************************* */
    
    /** Geometric mean, may be null*/
    private final String geometricMean;

    /**
     * Constructor for ARXString
     * @param scale
     * @param numberOfMeasures
     * @param mode
     */
    StatisticsSummary(ScaleOfMeasure scale, 
                      int numberOfMeasures, 
                      String mode) {
        this(scale, numberOfMeasures, mode, null, null, null, null, null, null, null, null, null, null);
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
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      String median,
                      String min,
                      String max) {
        this(scale, numberOfMeasures, mode, median, min, max, null, null, null, null, null, null, null);
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
     * @param stdDev
     * @param range
     * @param kurtosis
     */
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      String median,
                      String min,
                      String max,
                      String arithmeticMean,
                      String sampleVariance,
                      String populationVariance,
                      String stdDev,
                      String range,
                      String kurtosis) {
        this(scale, numberOfMeasures, mode, median, min, max, arithmeticMean, sampleVariance, populationVariance, stdDev, range, kurtosis, null);
    }

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
     * @param stdDev
     * @param range
     * @param kurtosis
     * @param geometricMean
     */
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      String median,
                      String min,
                      String max,
                      String arithmeticMean,
                      String sampleVariance,
                      String populationVariance,
                      String stdDev,
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
        this.stdDev = stdDev;
    }

    /**
     * Returns the mean
     * @return
     */
    public String getArithmeticMean() {
        return arithmeticMean;
    }

    /**
     * Returns the geometric mean
     * @return
     */
    public String getGeometricMean() {
        return geometricMean;
    }

    /**
     * Returns the kurtosis
     * @return
     */
    public String getKurtosis() {
        return kurtosis;
    }

    /**
     * Returns the max
     * @return
     */
    public String getMax() {
        return max;
    }

    /**
     * Returns the median
     * @return
     */
    public String getMedian() {
        return median;
    }

    /**
     * Returns the min
     * @return
     */
    public String getMin() {
        return min;
    }

    /**
     * Returns the mode
     * @return
     */
    public String getMode() {
        return mode;
    }

    /**
     * Returns the number of measures
     * @return
     */
    public int getNumberOfMeasures() {
        return numberOfMeasures;
    }

    /**
     * Returns the population variance
     * @return
     */
    public String getPopulationVariance() {
        return populationVariance;
    }

    /**
     * Returns the range
     * @return
     */
    public String getRange() {
        return range;
    }

    /**
     * Returns the sample variance
     * @return
     */
    public String getSampleVariance() {
        return sampleVariance;
    }

    /**
     * Returns the standard deviation
     * @return
     */
    public String getStdDev() {
        return stdDev;
    }

    /**
     * Returns the scale of measure
     * @return
     */
    public ScaleOfMeasure getScale() {
        return scale;
    }
    
    /**
     * Returns whether the following measure is available: mean
     * @return
     */
    public boolean isArithmeticMeanAvailable() {
        return null != arithmeticMean;
    }

    /**
     * Returns whether the following measure is available: geometric mean
     * @return
     */
    public boolean isGeometricMeanAvailable() {
        return null != geometricMean;
    }

    /**
     * Returns whether the following measure is available: kurtosis
     * @return
     */
    public boolean isKurtosisAvailable() {
        return null != kurtosis;
    }

    /**
     * Returns whether the following measure is available: max
     * @return
     */
    public boolean isMaxAvailable() {
        return null != max;
    }

    /**
     * Returns whether the following measure is available: median
     * @return
     */
    public boolean isMedianAvailable() {
        return null != median;
    }

    /**
     * Returns whether the following measure is available: min
     * @return
     */
    public boolean isMinAvailable() {
        return null != min;
    }

    /**
     * Returns whether the following measure is available: mode
     * @return
     */
    public boolean isModeAvailable() {
        return null != mode;
    }

    /**
     * Returns whether the following measure is available: population variance
     * @return
     */
    public boolean isPopulationVarianceAvailable() {
        return null != populationVariance;
    }

    /**
     * Returns whether the following measure is available: range
     * @return
     */
    public boolean isRangeAvailable() {
        return null != range;
    }

    /**
     * Returns whether the following measure is available: sample variance
     * @return
     */
    public boolean isSampleVarianceAvailable() {
        return null != sampleVariance;
    }

    /**
     * Returns whether the following measure is available: std. dev
     * @return
     */
    public boolean isStdDevAvailable() {
        return null != stdDev;
    }
    
    @Override
    public String toString() {
        return "StatisticsSummary [\n" + 
                                   " - scale=" + scale + "\n" + 
                                   " - numberOfMeasures=" + numberOfMeasures + "\n" + 
                                   (isModeAvailable() ? " - mode=" + mode + "\n" : "") + 
                                   (isMedianAvailable() ?  " - median=" + median + "\n" : "") + 
                                   (isMinAvailable() ?  " - min=" + min + "\n" : "") + 
                                   (isMaxAvailable() ?  " - max=" + max + "\n" : "") + 
                                   (isArithmeticMeanAvailable() ?  " - arithmeticMean=" + arithmeticMean + "\n" : "") + 
                                   (isSampleVarianceAvailable() ?  " - sampleVariance=" + sampleVariance + "\n" : "") + 
                                   (isPopulationVarianceAvailable() ?  " - populationVariance=" + populationVariance + "\n" : "") + 
                                   (isRangeAvailable() ? " - range=" + range + "\n" : "") + 
                                   (isKurtosisAvailable() ?  " - kurtosis=" + kurtosis + "\n" : "") + 
                                   (isGeometricMeanAvailable() ?  " - geometricMean=" + geometricMean + "\n" : "") + 
                                   "]";
    }
}
