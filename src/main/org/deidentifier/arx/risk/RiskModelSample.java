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

/**
 * Abstract base class for sample-based models
 * 
 * @author Fabian Prasser
 */
abstract class RiskModelSample {

    /** The classes */
    private final RiskModelHistogram histogram;

    /**
     * Creates a new instance
     * 
     * @param histogram
     */
    RiskModelSample(RiskModelHistogram histogram) {
        this.histogram = histogram;
    }

    /**
     * @return the classes
     */
    protected RiskModelHistogram getHistogram() {
        return histogram;
    }
}
