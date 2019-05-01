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

import org.deidentifier.arx.ARXConfiguration;

/**
 * Class for analyzing re-identification risks of the current sample and mixed
 * risks which have been derived from privacy models
 * 
 * @author Fabian Prasser
 */
public class RiskModelSampleRisks extends RiskModelSample {

    /** Configuration */
    private final ARXConfiguration config;
    /** Is the transformation anonymous */
    private final boolean          anonymous;
    
    /**
     * Creates a new instance
     * 
     * @param histogram
     * @param config
     * @param anonymous
     */
    public RiskModelSampleRisks(RiskModelHistogram histogram,
                                ARXConfiguration config,
                                boolean anonymous) {
        super(histogram);
        this.config = config;
        this.anonymous = anonymous;
    }

    /**
     * Returns the average re-identification risk
     * 
     * @return
     */
    public double getAverageRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        return 1.0d / getHistogram().getAvgClassSize();
    }

    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getEstimatedJournalistRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        return Math.min(1.0d / (double)getHistogram().getHistogram()[0], config != null && anonymous ? config.getRiskThresholdJournalist() : 1d);
    }

    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getEstimatedMarketerRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        return Math.min(1.0d / getHistogram().getAvgClassSize(), config != null && anonymous ? config.getRiskThresholdMarketer() : 1d);
    }

    /**
     * Return prosecutor risk threshold, 1 if there is none
     * @return
     */
    public double getEstimatedProsecutorRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        return Math.min(1.0d / (double)getHistogram().getHistogram()[0], config != null && anonymous ? config.getRiskThresholdProsecutor() : 1d);
    }

    /**
     * Returns the fraction of records affected by the highest re-identification
     * risk
     * 
     * @return
     */
    public double getFractionOfRecordsAffectedByHighestRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        return getNumRecordsAffectedByHighestRisk() /
               getHistogram().getNumRecords();
    }

    /**
     * Returns the fraction of records affected by the lowest re-identification
     * risk
     * 
     * @return
     */
    public double getFractionOfRecordsAffectedByLowestRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        return getNumRecordsAffectedByLowestRisk() /
               getHistogram().getNumRecords();
    }

    /**
     * Returns the highest re-identification risk of any records in the data set
     * 
     * @return
     */
    public double getHighestRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        int[] classes = getHistogram().getHistogram();
        return 1d / (double) classes[0];
    }

    /**
     * Returns the lowest re-identification risk of any records in the data set
     * 
     * @return
     */
    public double getLowestRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        int[] classes = getHistogram().getHistogram();
        int index = classes.length - 2;
        return 1d / (double) classes[index];
    }
    
    /**
     * Returns the number of records affected by the highest re-identification
     * risk
     * 
     * @return
     */
    public double getNumRecordsAffectedByHighestRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        int[] classes = getHistogram().getHistogram();
        return (double) classes[0] * (double) classes[1];
    }
    
    /**
     * Returns the number of records affected by the lowest re-identification
     * risk
     * 
     * @return
     */
    public double getNumRecordsAffectedByLowestRisk() {
        if (getHistogram().isEmpty()) {
            return 0d;
        }
        int[] classes = getHistogram().getHistogram();
        int index = classes.length - 2;
        return (double) classes[index] * (double) classes[index + 1];
    }
}
