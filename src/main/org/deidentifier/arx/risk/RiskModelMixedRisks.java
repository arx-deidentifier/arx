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

import org.deidentifier.arx.ARXConfiguration;

/**
 * A mixed risk model considering thresholds from privacy models as well as thresholds
 * derived from data properties
 * 
 * @author Fabian Prasser
 */
public class RiskModelMixedRisks {

    /** The classes */
    private final RiskModelHistogram histogram;

    /** Configuration */
    private final ARXConfiguration   config;

    /**
     * Creates a new instance
     * 
     * @param histogram
     */
    RiskModelMixedRisks(RiskModelHistogram histogram,
                        ARXConfiguration config) {
        this.histogram = histogram;
        this.config = config;
    }

    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getJournalistRisk() {
        return Math.min(1.0d / (double)histogram.getHistogram()[0], config != null ? config.getRiskThresholdJournalist() : 1d);
    }
    
    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getMarketerRisk() {
        return Math.min(1.0d / histogram.getAvgClassSize(), config != null ? config.getRiskThresholdMarketer() : 1d);
    }
    
    /**
     * Return prosecutor risk threshold, 1 if there is none
     * @return
     */
    public double getProsecutorRisk() {
        return Math.min(1.0d / (double)histogram.getHistogram()[0], config != null ? config.getRiskThresholdProsecutor() : 1d);
    }
}
