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
 * This class provides an implementation of the non-uniform entropy
 * metric. See:<br>
 * A. De Waal and L. Willenborg: 
 * "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17–20, 1999.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUNMEntropyPotentiallyPrecomputed extends AbstractMetricMultiDimensionalPotentiallyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -3213516677340712914L;

    /**
     * Creates a new instance. The pre-computed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     * @param gsFactor
     * @param function
     */
    protected MetricMDNUNMEntropyPotentiallyPrecomputed(double threshold,
                                                        double gsFactor,
                                                        AggregateFunction function) {
        super(new MetricMDNUNMEntropy(gsFactor, function),
              new MetricMDNUNMEntropyPrecomputed(gsFactor, function),
              threshold);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false, // monotonic
                                       super.getDefaultMetric().getGeneralizationSuppressionFactor(), // gs-factor
                                       super.isPrecomputed(), // precomputed
                                       super.getThreshold(), // precomputation threshold
                                       super.getDefaultMetric().getAggregateFunction() // aggregate function
        );
    }
    
    @Override
    public boolean isGSFactorSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        return super.getDefaultMetric().render(config);
    }

    @Override
    public String toString() {
        return "Non-monotonic non-uniform entropy";
    }
}
