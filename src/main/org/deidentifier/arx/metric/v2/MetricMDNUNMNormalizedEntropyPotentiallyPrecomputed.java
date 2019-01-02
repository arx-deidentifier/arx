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

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.metric.MetricConfiguration;


/**
 * This class provides an implementation of normalized non-uniform entropy. See:<br>
 * A. De Waal and L. Willenborg: 
 * "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17–20, 1999.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUNMNormalizedEntropyPotentiallyPrecomputed extends AbstractMetricMultiDimensionalPotentiallyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -3297238195567701353L;

    /**
     * Creates a new instance. The precomputed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     */
    protected MetricMDNUNMNormalizedEntropyPotentiallyPrecomputed(double threshold) {
        super(new MetricMDNUNMNormalizedEntropy(),
              new MetricMDNUNMNormalizedEntropyPrecomputed(),
              threshold);
    }

    /**
     * Creates a new instance. The pre-computed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     * @param function
     */
    protected MetricMDNUNMNormalizedEntropyPotentiallyPrecomputed(double threshold,
                                                        AggregateFunction function) {
        super(new MetricMDNUNMNormalizedEntropy(function),
              new MetricMDNUNMNormalizedEntropyPrecomputed(function),
              threshold);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                       // monotonic
                                       0.5d,                       // gs-factor
                                       super.isPrecomputed(),      // precomputed
                                       super.getThreshold(),       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }
    
    @Override
    public String getName() {
        return "Normalized non-uniform entropy";
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        return super.getDefaultMetric().render(config);
    }

    @Override
    public String toString() {
        return "Normalized non-uniform entropy";
    }
}
