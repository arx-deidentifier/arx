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
 * This class implements a variant of the Loss metric.
 * TODO: Add reference.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNMLossPotentiallyPrecomputed extends AbstractMetricMultiDimensionalPotentiallyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -409964525491865637L;

    /**
     * Creates a new instance. The precomputed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     */
    protected MetricMDNMLossPotentiallyPrecomputed(double threshold) {
        super(new MetricMDNMLoss(),
              new MetricMDNMLossPrecomputed(),
              threshold);
    }

    /**
     * Creates a new instance. The precomputed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     * @param function
     */
    protected MetricMDNMLossPotentiallyPrecomputed(double threshold,
                                                   AggregateFunction function) {
        super(new MetricMDNMLoss(function),
              new MetricMDNMLossPrecomputed(function),
              threshold);
    }
    
    /**
     * Creates a new instance. The precomputed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * 
     * @param threshold
     * @param gsFactor
     * @param function
     */
    protected MetricMDNMLossPotentiallyPrecomputed(double threshold,
                                                   double gsFactor,
                                                   AggregateFunction function) {
        super(new MetricMDNMLoss(gsFactor, function),
              new MetricMDNMLossPrecomputed(gsFactor, function),
              threshold);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       ((MetricMDNMLoss)super.getDefaultMetric()).getGeneralizationSuppressionFactor(), // gs-factor
                                       super.isPrecomputed(),      // precomputed
                                       super.getThreshold(),       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }

    @Override
    public double getGeneralizationFactor() {
        return super.getPrecomputedMetric().getGeneralizationFactor();
    }

    @Override
    public double getGeneralizationSuppressionFactor() {
        return super.getPrecomputedMetric().getGeneralizationSuppressionFactor();
    }
    
    @Override
    public double getSuppressionFactor() {
        return super.getPrecomputedMetric().getSuppressionFactor();
    }

    @Override
    public boolean isAbleToHandleMicroaggregation() {
        return true;
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
        MetricMDNMLoss loss = ((MetricMDNMLoss)super.getDefaultMetric());
        return "Loss ("+loss.getGeneralizationSuppressionFactor()+"/"+
                        loss.getGeneralizationFactor()+"/"+
                        loss.getSuppressionFactor()+")";
    }
}
