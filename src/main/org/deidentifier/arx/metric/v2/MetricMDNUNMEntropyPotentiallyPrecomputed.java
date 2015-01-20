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

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.MetricConfiguration;


/**
 * This class provides an implementation of the non-uniform entropy
 * metric. TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUNMEntropyPotentiallyPrecomputed extends AbstractMetricMultiDimensionalPotentiallyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -3213516677340712914L;

    /**
     * Creates a new instance. The precomputed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     */
    protected MetricMDNUNMEntropyPotentiallyPrecomputed(double threshold) {
        super(new MetricMDNUNMEntropy(),
              new MetricMDNUNMEntropyPrecomputed(),
              threshold);
    }

    /**
     * Creates a new instance. The pre-computed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     * @param function
     */
    protected MetricMDNUNMEntropyPotentiallyPrecomputed(double threshold,
                                                        AggregateFunction function) {
        super(new MetricMDNUNMEntropy(function),
              new MetricMDNUNMEntropyPrecomputed(function),
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
    

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Non-monotonic non-uniform entropy";
    }

    /**
     * For backwards compatibility.
     *
     * @param cache
     * @param cardinalities
     * @param hierarchies
     */
    protected void initialize(double[][] cache, int[][][] cardinalities, int[][][] hierarchies) {
        ((MetricMDNUNMEntropy)this.getDefaultMetric()).initialize(cache, cardinalities, hierarchies);
        ((MetricMDNUNMEntropyPrecomputed)this.getPrecomputedMetric()).initialize(cache, cardinalities, hierarchies);
    }
}
