/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * This class provides an abstract skeleton for the implementation of single-dimensional metrics.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractMetricSingleDimensional extends Metric<ILSingleDimensional> {

    /** SVUID. */
    private static final long               serialVersionUID = -1082954137578580790L;

    /** Row count. */
    private Double                          tuples           = null;

    /** The microaggregation functions. */
    private DistributionAggregateFunction[] microaggregationFunctions;

    /** The start index of the attributes with microaggregation in the data array (dataAnalyzed) */
    private int                             microaggregationStartIndex;

    /** Domain size for each microaggregated attribute */
    private int[]                           microaggregationDomainSizes;

    /**
     * Creates a new instance.
     *
     * @param monotonicWithGeneralization
     * @param monotonicWithSuppression
     * @param independent
     */
    protected AbstractMetricSingleDimensional(final boolean monotonicWithGeneralization, final boolean monotonicWithSuppression, final boolean independent) {
        super(monotonicWithGeneralization, monotonicWithSuppression, independent, 0.5d);
    }

    /**
     * Creates a new instance.
     *
     * @param monotonicWithGeneralization
     * @param monotonicWithSuppression
     * @param independent
     * @param gsFactor
     */
    protected AbstractMetricSingleDimensional(final boolean monotonicWithGeneralization, final boolean monotonicWithSuppression, final boolean independent, final double gsFactor) {
        super(monotonicWithGeneralization, monotonicWithSuppression, independent, gsFactor);
    }
    
    /**
     * Create a loss object
     * @param loss
     * @param bound
     * @return
     */
    public ILSingleDimensionalWithBound createInformationLoss(double loss, double bound) {
        return new ILSingleDimensionalWithBound(loss, bound);
    }
    
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new ILSingleDimensional(Double.MAX_VALUE);
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new ILSingleDimensional(0d);
    }

    /**
     * Needed for microaggregation
     * @return
     */
    protected int[] getMicroaggregationDomainSizes() {
        return microaggregationDomainSizes;
    }
    
    /**
     * Needed for microaggregation
     * @return
     */
    protected DistributionAggregateFunction[] getMicroaggregationFunctions() {
        return microaggregationFunctions;
    }
    
    /**
     * Needed for microaggregation
     * @return
     */
    protected int getMicroaggregationStartIndex() {
        return microaggregationStartIndex;
    }

    /**
     * Returns the number of rows in the dataset or subset.
     *
     * @return
     */
    protected Double getNumTuples() {
        return tuples;
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        this.tuples = (double) getNumRecords(config, input);

        // Handle microaggregation
        this.microaggregationFunctions = manager.getMicroaggregationFunctions();
        this.microaggregationStartIndex = manager.getMicroaggregationStartIndex();
        this.microaggregationDomainSizes = manager.getMicroaggregationDomainSizes();
        if (!config.isUtilityBasedMicroaggregation() || !isAbleToHandleMicroaggregation()) {
            this.microaggregationFunctions = new DistributionAggregateFunction[0];
            this.microaggregationDomainSizes = new int[0];
        }
    }

    /**
     * Returns the number of rows in the dataset or subset.
     *
     * @param tuples
     */
    protected void setNumTuples(Double tuples) {
        this.tuples = tuples;
    }
}
