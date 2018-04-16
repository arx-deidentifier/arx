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

package org.deidentifier.arx.risk;

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;

/**
 * Class representing the distribution of risks in the sample
 * 
 * @author Fabian Prasser
 */
public class RiskModelSampleRiskDistribution {
    
    /** Thresholds */
    private static final double[] thresholdsLow           = new double[] {  0d,
                                                                            0.00000001d,
                                                                            0.0000001d,
                                                                            0.000001d,
                                                                            0.00001d,
                                                                            0.0001d,
                                                                            0.001d,
                                                                            0.01d,
                                                                            0.02d,
                                                                            0.03d,
                                                                            0.04d,
                                                                            0.05d,
                                                                            0.06d,
                                                                            0.07d,
                                                                            0.08d,
                                                                            0.09d,
                                                                            0.1d,
                                                                            0.125d,
                                                                            0.143d,
                                                                            0.167d,
                                                                            0.2d,
                                                                            0.25d,
                                                                            0.334d,
                                                                            0.5d};
    /** Thresholds */
    private static final double[] thresholdsHigh          = new double[] {  0.00000001d,
                                                                            0.0000001d,
                                                                            0.000001d,
                                                                            0.00001d,
                                                                            0.0001d,
                                                                            0.001d,
                                                                            0.01d,
                                                                            0.02d,
                                                                            0.03d,
                                                                            0.04d,
                                                                            0.05d,
                                                                            0.06d,
                                                                            0.07d,
                                                                            0.08d,
                                                                            0.09d,
                                                                            0.1d,
                                                                            0.125d,
                                                                            0.143d,
                                                                            0.167d,
                                                                            0.2d,
                                                                            0.25d,
                                                                            0.334d,
                                                                            0.5d,
                                                                            1d };

    /** Risks */
    private final double[]        recordsAtRisk           = new double[thresholdsLow.length];
    /** Cumulative risks */
    private final double[]        recordsAtCumulativeRisk = new double[thresholdsLow.length];
    /** Threshold risk */
    private final double          threshold;
    
    /**
     * Creates a new instance
     * 
     * @param histogram
     * @param config
     * @param anonymous
     */
    public RiskModelSampleRiskDistribution(RiskModelHistogram histogram,
                                           ARXConfiguration config,
                                           boolean anonymous) {

        int[] array = histogram.getHistogram();
        for (int i = 0; i < array.length; i += 2) {
            int size = array[i];
            int count = array[i+1];
            double risk = 1d / (double)size;
            double records = (double)(count * size)/ histogram.getNumRecords();
            int index = Arrays.binarySearch(thresholdsHigh, risk);
            if (index < 0) {
                index = -index - 1;
            }
            this.recordsAtRisk[index] += records;
        }
        double cumulativeRisk = 0;
        for (int i=0; i<thresholdsHigh.length; i++) {
            cumulativeRisk += this.recordsAtRisk[i];
            this.recordsAtCumulativeRisk[i] = cumulativeRisk;
        }
        this.threshold = Math.min(1.0d / (double)histogram.getHistogram()[0], config != null && anonymous ? config.getRiskThresholdProsecutor() : 1d);
    }
    
    /**
     * Returns the lower bounds (exclusive) of the intervals for the according data points.
     * 
     * @return
     */
    public double[] getAvailableLowerRiskThresholds() {
        return thresholdsLow;
    }

    /**
     * Returns the lower bounds (inclusive) of the intervals for the according data points.
     * 
     * @return
     */
    public double[] getAvailableUpperRiskThresholds() {
        return thresholdsHigh;
    }
    
    /**
     * Returns the fraction of records with a risk lower than or equal
     * to the given threshold.
     * 
     * @param risk
     * @return
     */
    public double getFractionOfRecordsAtCumulativeRisk(double risk) {
        if (risk < 0d || risk >1d) {
            throw new IllegalArgumentException("Parameter out of range [0, 1]: " + risk);
        }
        int index = Arrays.binarySearch(thresholdsHigh, risk);
        if (index < 0) {
            index = -index - 1;
        }
        return recordsAtCumulativeRisk[index];
    }

    /**
     * Returns the fraction of records with a risk which equals the given threshold.
     * 
     * @param risk
     * @return
     */
    public double getFractionOfRecordsAtRisk(double risk) {
        if (risk < 0d || risk >1d) {
            throw new IllegalArgumentException("Parameter out of range [0, 1]: " + risk);
        }
        int index = Arrays.binarySearch(thresholdsHigh, risk);
        if (index < 0) {
            index = -index - 1;
        }
        return recordsAtRisk[index];
    }

    /**
     * Returns an array of cumulative risks for the risk thresholds 
     * @return
     */
    public double[] getFractionOfRecordsForCumulativeRiskThresholds() {
        return recordsAtCumulativeRisk;
    }

    /**
     * Returns an array of risks for the risk thresholds 
     * @return
     */
    public double[] getFractionOfRecordsForRiskThresholds() {
        return recordsAtRisk;
    }

    /**
     * Returns the threshold which is available on prosecutor risks
     * @return
     */
    public double getRiskThreshold() {
        return this.threshold;
    }
}
