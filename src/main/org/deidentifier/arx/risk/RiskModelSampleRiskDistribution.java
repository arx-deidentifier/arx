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

package org.deidentifier.arx.risk;

import java.util.Arrays;

/**
 * Class representing the distribution of risks in the sample
 * 
 * @author Fabian Prasser
 */
public class RiskModelSampleRiskDistribution {

    /** Thresholds */
    private static final double[] thresholds = new double[106];

    /** Thresholds */
    static {
        thresholds[0] = 0d;
        thresholds[1] = 0.0000001d;
        thresholds[2] = 0.000001d;
        thresholds[3] = 0.00001d;
        thresholds[4] = 0.0001d;
        thresholds[5] = 0.001d;
        for (int i=6; i<106; i++) {
            thresholds[i] = (double) (i - 5) / 100d;
        }
    }
    
    /** Risks*/
    private final double[] recordsAtRisk = new double[thresholds.length];
    /** Cumulative risks*/
    private final double[] recordsAtCumulativeRisk = new double[thresholds.length];
    
    /**
     * Creates a new instance
     * 
     * @param histogram
     */
    public RiskModelSampleRiskDistribution(RiskModelHistogram histogram) {

        int[] array = histogram.getHistogram();
        for (int i = 0; i < array.length; i += 2) {
            int size = array[i];
            int count = array[i+1];
            double risk = 1d / (double)size;
            double records = (double)count / histogram.getNumTuples();
            int index = Arrays.binarySearch(thresholds, risk);
            if (index < 0) {
                index = -index - 2;
            }
            recordsAtRisk[index] += records;
        }
        double cumulativeRisk = 0;
        for (int i=0; i<thresholds.length; i++) {
            cumulativeRisk += recordsAtRisk[i];
            recordsAtCumulativeRisk[i] = cumulativeRisk;
        }
    }
    
    /**
     * Returns the fraction of records with a risk lower than or equal
     * to the given threshold.
     * Note: all risks below 10^-6 are mapped to 0% risk.
     * 
     * @param risk
     * @return
     */
    public double getFractionOfRecordsAtCumulativeRisk(double risk) {
        int index = Arrays.binarySearch(thresholds, risk);
        if (index < 0) {
            index = -index - 2;
        }
        return recordsAtCumulativeRisk[index];
    }

    /**
     * Returns the fraction of records with a risk which equals the given threshold.
     * Note: all risks below 10^-6 are mapped to 0% risk.
     * @param risk
     * @return
     */
    public double getFractionOfRecordsAtRisk(double risk) {
        int index = Arrays.binarySearch(thresholds, risk);
        if (index < 0) {
            index = -index - 2;
        }
        return recordsAtRisk[index];
    }

    /**
     * Returns an array of cumulative risks for the risk thresholds 
     * @return
     */
    public double[] getFractionOfRecordsForCumulativeRiskThresholds() {
        return recordsAtRisk;
    }

    /**
     * Returns an array of risks for the risk thresholds 
     * @return
     */
    public double[] getFractionOfRecordsForRiskThresholds() {
        return recordsAtRisk;
    }

    /**
     * Returns a set of risk thresholds for which data is maintained.
     * Note: all risks below 10^-6 are mapped to 0% risk.
     * @return
     */
    public double[] getRiskThresholds() {
        return thresholds;
    }
}
