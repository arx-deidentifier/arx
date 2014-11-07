/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

    /** SVUID*/
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
     * Returns the configuration of this metric
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
    public String toString() {
        return "Non-monotonic non-uniform entropy";
    }

    /**
     * For backwards compatibility
     * @param cache
     * @param cardinalities
     * @param hierarchies
     */
    protected void initialize(double[][] cache, int[][][] cardinalities, int[][][] hierarchies) {
        ((MetricMDNUNMEntropy)this.getDefaultMetric()).initialize(cache, cardinalities, hierarchies);
        ((MetricMDNUNMEntropyPrecomputed)this.getPrecomputedMetric()).initialize(cache, cardinalities, hierarchies);
    }
}
