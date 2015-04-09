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
public class StatisticsSummary<T> {
    
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
         * @param comparator
         */
        StatisticsSummaryOrdinal(final Comparator<String> comparator) {
            this.comparator = comparator;
        }

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

    /** The associated scale of measure */
    private final ScaleOfMeasure scale;

    /** The number of measures */
    private final int            numberOfMeasures;

    /* ******************************************************************** 
     * ARXString, ARXOrderedString, ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/

    /** Mode */
    private final String         mode;
    /** Mode */
    private final T              modeT;

    /* ******************************************************************** 
     * ARXOrderedString, ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/

    /** Median, may be null */
    private final String         median;
    /** Median, may be null */
    private final T              medianT;
    /** Min, may be null */
    private final String         min;
    /** Min, may be null */
    private final T              minT;
    /** Max, may be null */
    private final String         max;
    /** Max, may be null */
    private final T              maxT;
    
    /* ********************************************************************  
     * ARXDate, ARXInteger, ARXDecimal 
     **********************************************************************/

    /** Arithmetic mean, may be null */
    private final String         arithmeticMean;
    /** Arithmetic mean, may be null */
    private final T              arithmeticMeanT;
    /** Arithmetic mean, may be null */
    private final double         arithmeticMeanD;
    /** Sample variance, may be null */
    private final String         sampleVariance;
    /** Sample variance, may be null */
    private final T              sampleVarianceT;
    /** Sample variance, may be null */
    private final double         sampleVarianceD;
    /** Population variance, may be null */
    private final String         populationVariance;
    /** Population variance, may be null */
    private final T              populationVarianceT;
    /** Population variance, may be null */
    private final double         populationVarianceD;
    /** Std.dev, may be null */
    private final String         stdDev;
    /** Std.dev, may be null */
    private final T              stdDevT;
    /** Std.dev, may be null */
    private final double         stdDevD;
    /** Range, may be null */
    private final String         range;
    /** Range, may be null */
    private final T              rangeT;
    /** Range, may be null */
    private final double         rangeD;
    /** Kurtosis, may be null */
    private final String         kurtosis;
    /** Kurtosis, may be null */
    private final T              kurtosisT;
    /** Kurtosis, may be null */
    private final double         kurtosisD;

    /* ********************************************************************  
     * ARXInteger, ARXDecimal 
     ********************************************************************* */

    /** Geometric mean, may be null */
    private final String         geometricMean;
    /** Geometric mean, may be null */
    private final T              geometricMeanT;
    /** Geometric mean, may be null */
    private final double         geometricMeanD;

    /**
     * Constructor for ARXString
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param modeT
     */
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      T modeT) {

        this(scale, numberOfMeasures, 
             mode, modeT,
             null, null, 
             null, null, 
             null, null, 
             null, null, Double.NaN,
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN);
    }

    /**
     * Constructor for ARXOrderedString
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param modeT
     * @param median
     * @param medianT
     * @param min
     * @param minT
     * @param max
     * @param maxT
     */
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      T modeT,
                      String median,
                      T medianT,
                      String min,
                      T minT,
                      String max,
                      T maxT) {

        this(scale, numberOfMeasures, 
             mode, modeT,
             median, medianT, 
             min, minT,
             max, maxT,
             null, null, Double.NaN,
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN, 
             null, null, Double.NaN);
    }

    /**
     * Constructor for ARXDate 
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param modeT
     * @param median
     * @param medianT
     * @param min
     * @param minT
     * @param max
     * @param maxT
     * @param arithmeticMean
     * @param arithmeticMeanT
     * @param arithmeticMeanD
     * @param sampleVariance
     * @param sampleVarianceT
     * @param sampleVarianceD
     * @param populationVariance
     * @param populationVarianceT
     * @param populationVarianceD
     * @param stdDev
     * @param stdDevT
     * @param stdDevD
     * @param range
     * @param rangeT
     * @param rangeD
     * @param kurtosis
     * @param kurtosisT
     * @param kurtosisD
     */
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      T modeT,
                      String median,
                      T medianT,
                      String min,
                      T minT,
                      String max,
                      T maxT,
                      String arithmeticMean,
                      T arithmeticMeanT,
                      double arithmeticMeanD,
                      String sampleVariance,
                      T sampleVarianceT,
                      double sampleVarianceD,
                      String populationVariance,
                      T populationVarianceT,
                      double populationVarianceD,
                      String stdDev,
                      T stdDevT,
                      double stdDevD,
                      String range,
                      T rangeT,
                      double rangeD,
                      String kurtosis,
                      T kurtosisT,
                      double kurtosisD) {
        
        
        this(scale, numberOfMeasures, 
             mode, modeT,
             median, medianT, 
             min, minT,
             max, maxT,
             arithmeticMean, arithmeticMeanT, arithmeticMeanD,
             sampleVariance, sampleVarianceT, sampleVarianceD, 
             populationVariance, populationVarianceT, populationVarianceD, 
             stdDev, stdDevT, stdDevD, 
             range, rangeT, rangeD, 
             kurtosis, kurtosisT, kurtosisD, 
             null, null, Double.NaN);
    }

    /**
     * Constructor for ARXInteger and ARXDecimal 
     * @param scale
     * @param numberOfMeasures
     * @param mode
     * @param modeT
     * @param median
     * @param medianT
     * @param min
     * @param minT
     * @param max
     * @param maxT
     * @param arithmeticMean
     * @param arithmeticMeanT
     * @param arithmeticMeanD
     * @param sampleVariance
     * @param sampleVarianceT
     * @param sampleVarianceD
     * @param populationVariance
     * @param populationVarianceT
     * @param populationVarianceD
     * @param stdDev
     * @param stdDevT
     * @param stdDevD
     * @param range
     * @param rangeT
     * @param rangeD
     * @param kurtosis
     * @param kurtosisT
     * @param kurtosisD
     * @param geometricMean
     * @param geometricMeanT
     * @param geometricMeanD
     */
    StatisticsSummary(ScaleOfMeasure scale,
                      int numberOfMeasures,
                      String mode,
                      T modeT,
                      String median,
                      T medianT,
                      String min,
                      T minT,
                      String max,
                      T maxT,
                      String arithmeticMean,
                      T arithmeticMeanT,
                      double arithmeticMeanD,
                      String sampleVariance,
                      T sampleVarianceT,
                      double sampleVarianceD,
                      String populationVariance,
                      T populationVarianceT,
                      double populationVarianceD,
                      String stdDev,
                      T stdDevT,
                      double stdDevD,
                      String range,
                      T rangeT,
                      double rangeD,
                      String kurtosis,
                      T kurtosisT,
                      double kurtosisD,
                      String geometricMean,
                      T geometricMeanT,
                      double geometricMeanD) {
        this.numberOfMeasures = numberOfMeasures;
        this.scale = scale;
        this.mode = mode;
        this.modeT = modeT;
        this.median = median;
        this.medianT = medianT;
        this.min = min;
        this.minT = minT;
        this.max = max;
        this.maxT = maxT;
        this.arithmeticMean = arithmeticMean;
        this.arithmeticMeanT = arithmeticMeanT;
        this.arithmeticMeanD = arithmeticMeanD;
        this.sampleVariance = sampleVariance;
        this.sampleVarianceT = sampleVarianceT;
        this.sampleVarianceD = sampleVarianceD;
        this.populationVariance = populationVariance;
        this.populationVarianceT = populationVarianceT;
        this.populationVarianceD = populationVarianceD;
        this.range = range;
        this.rangeT = rangeT;
        this.rangeD = rangeD;
        this.kurtosis = kurtosis;
        this.kurtosisT = kurtosisT;
        this.kurtosisD = kurtosisD;
        this.geometricMean = geometricMean;
        this.geometricMeanT = geometricMeanT;
        this.geometricMeanD = geometricMeanD;
        this.stdDev = stdDev;
        this.stdDevT = stdDevT;
        this.stdDevD = stdDevD;
    }


    /**
     * Returns the mean
     * @return
     */
    public double getArithmeticMeanAsDouble() {
        return arithmeticMeanD;
    }

    /**
     * Returns the mean
     * @return
     */
    public String getArithmeticMeanAsString() {
        return arithmeticMean;
    }

    /**
     * Returns the mean
     * @return
     */
    public T getArithmeticMeanAsValue() {
        return arithmeticMeanT;
    }

    /**
     * Returns the geometric mean
     * @return
     */
    public double getGeometricMeanAsDouble() {
        return geometricMeanD;
    }

    /**
     * Returns the geometric mean
     * @return
     */
    public String getGeometricMeanAsString() {
        return geometricMean;
    }

    /**
     * Returns the geometric mean
     * @return
     */
    public T getGeometricMeanAsValue() {
        return geometricMeanT;
    }

    /**
     * Returns the kurtosis
     * @return
     */
    public double getKurtosisAsDouble() {
        return kurtosisD;
    }

    /**
     * Returns the kurtosis
     * @return
     */
    public String getKurtosisAsString() {
        return kurtosis;
    }

    /**
     * Returns the kurtosis
     * @return
     */
    public T getKurtosisAsValue() {
        return kurtosisT;
    }

    

    /**
     * Returns the max
     * @return
     */
    public String getMaxAsString() {
        return max;
    }

    /**
     * Returns the max
     * @return
     */
    public T getMaxAsValue() {
        return maxT;
    }

    /**
     * Returns the median
     * @return
     */
    public String getMedianAsString() {
        return median;
    }
    
    /**
     * Returns the median
     * @return
     */
    public T getMedianAsValue() {
        return medianT;
    }

    /**
     * Returns the min
     * @return
     */
    public String getMinAsString() {
        return min;
    }

    /**
     * Returns the min
     * @return
     */
    public T getMinAsValue() {
        return minT;
    }

    /**
     * Returns the mode
     * @return
     */
    public String getModeAsString() {
        return mode;
    }

    /**
     * Returns the mode
     * @return
     */
    public T getModeAsValue() {
        return modeT;
    }

    /**
     * Returns the number of measures
     * @return
     */
    public int getNumberOfMeasuresAsString() {
        return numberOfMeasures;
    }

    /**
     * Returns the population variance
     * @return
     */
    public double getPopulationVarianceAsDouble() {
        return populationVarianceD;
    }
    
    /**
     * Returns the population variance
     * @return
     */
    public String getPopulationVarianceAsString() {
        return populationVariance;
    }

    /**
     * Returns the population variance
     * @return
     */
    public T getPopulationVarianceAsValue() {
        return populationVarianceT;
    }

    /**
     * Returns the range
     * @return
     */
    public double getRangeAsDouble() {
        return rangeD;
    }

    /**
     * Returns the range
     * @return
     */
    public String getRangeAsString() {
        return range;
    }

    /**
     * Returns the range
     * @return
     */
    public T getRangeAsValue() {
        return rangeT;
    }

    /**
     * Returns the sample variance
     * @return
     */
    public double getSampleVarianceAsDouble() {
        return sampleVarianceD;
    }

    /**
     * Returns the sample variance
     * @return
     */
    public String getSampleVarianceAsString() {
        return sampleVariance;
    }
    
    /**
     * Returns the sample variance
     * @return
     */
    public T getSampleVarianceAsValue() {
        return sampleVarianceT;
    }

    /**
     * Returns the scale of measure
     * @return
     */
    public ScaleOfMeasure getScale() {
        return scale;
    }

    /**
     * Returns the standard deviation
     * @return
     */
    public double getStdDevAsDouble() {
        return stdDevD;
    }

    /**
     * Returns the standard deviation
     * @return
     */
    public String getStdDevAsString() {
        return stdDev;
    }

    /**
     * Returns the standard deviation
     * @return
     */
    public T getStdDevAsValue() {
        return stdDevT;
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
