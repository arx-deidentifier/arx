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

/**
 * Class for analyzing re-identification risks of the current sample
 * 
 * @author Fabian Prasser
 */
public class RiskModelSampleRisks extends RiskModelSample {

    /**
     * Creates a new instance
     * 
     * @param histogram
     */
    public RiskModelSampleRisks(RiskModelHistogram histogram) {
        super(histogram);
    }

    /**
     * Returns the average re-identification risk
     * 
     * @return
     */
    public double getAverageRisk() {
        return 1.0d / getHistogram().getAvgClassSize();
    }

    /**
     * Returns the fraction of tuples affected by the highest re-identification
     * risk
     * 
     * @return
     */
    public double getFractionOfTuplesAffectedByHighestRisk() {
        return getNumTuplesAffectedByHighestRisk() /
               getHistogram().getNumTuples();
    }

    /**
     * Returns the fraction of tuples affected by the lowest re-identification
     * risk
     * 
     * @return
     */
    public double getFractionOfTuplesAffectedByLowestRisk() {
        return getNumTuplesAffectedByLowestRisk() /
               getHistogram().getNumTuples();
    }

    /**
     * Returns the highest re-identification risk of any tuple in the data set
     * 
     * @return
     */
    public double getHighestRisk() {
        int[] classes = getHistogram().getHistogram();
        return 1d / (double) classes[0];
    }

    /**
     * Returns the lowest re-identification risk of any tuple in the data set
     * 
     * @return
     */
    public double getLowestRisk() {
        int[] classes = getHistogram().getHistogram();
        int index = classes.length - 2;
        return 1d / (double) classes[index];
    }

    /**
     * Returns the number of tuples affected by the highest re-identification
     * risk
     * 
     * @return
     */
    public double getNumTuplesAffectedByHighestRisk() {
        int[] classes = getHistogram().getHistogram();
        return (double) classes[0] * (double) classes[1];
    }

    /**
     * Returns the number of tuples affected by the lowest re-identification
     * risk
     * 
     * @return
     */
    public double getNumTuplesAffectedByLowestRisk() {
        int[] classes = getHistogram().getHistogram();
        int index = classes.length - 2;
        return (double) classes[index] * (double) classes[index + 1];
    }
}
