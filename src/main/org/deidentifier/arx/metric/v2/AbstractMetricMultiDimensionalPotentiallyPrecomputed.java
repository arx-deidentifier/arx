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
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.InformationLossWithBound;

/**
 * This class provides an abstract skeleton for the implementation of metrics
 * that can either be precomputed or not. The decision is made at runtime depending
 * on data properties.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractMetricMultiDimensionalPotentiallyPrecomputed extends AbstractMetricMultiDimensional {

    /** SVUID. */
    private static final long              serialVersionUID = 7278544218893194559L;

    /** Is this instance precomputed. */
    private boolean                        precomputed      = false;

    /** The threshold. */
    private final double                   threshold;

    /** The default metric. */
    private AbstractMetricMultiDimensional defaultMetric;

    /** The precomputed variant. */
    private AbstractMetricMultiDimensional precomputedMetric;

    /**
     * Creates a new instance. The precomputed variant will be used if 
     * #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param defaultMetric
     * @param precomputedMetric
     * @param threshold
     */
    AbstractMetricMultiDimensionalPotentiallyPrecomputed(AbstractMetricMultiDimensional defaultMetric,
                                                         AbstractMetricMultiDimensional precomputedMetric,
                                                         double threshold) {

        super(defaultMetric.isMonotonicWithGeneralization(), 
              defaultMetric.isMonotonicWithSuppression(),
              false, defaultMetric.getAggregateFunction());
        
        // Sanity checks
        if (defaultMetric.getAggregateFunction() != precomputedMetric.getAggregateFunction()) {
            throw new IllegalArgumentException("Aggregate function does not match");
        }
        if (defaultMetric.isMonotonicWithSuppression() != precomputedMetric.isMonotonicWithSuppression()) {
            throw new IllegalArgumentException("Monotonicity does not match");
        }
        if (defaultMetric.isMonotonicWithGeneralization() != precomputedMetric.isMonotonicWithGeneralization()) {
            throw new IllegalArgumentException("Monotonicity does not match");
        }

        // Default is non-precomputed
        this.threshold = threshold;
        this.precomputed = false;
        this.defaultMetric = defaultMetric;
        this.precomputedMetric = precomputedMetric;
    }
    
    

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (precomputed) {
            return precomputedMetric.createMaxInformationLoss();
        } else {
            return defaultMetric.createMaxInformationLoss();
        }
    }



    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (precomputed) {
            return precomputedMetric.createMinInformationLoss();
        } else {
            return defaultMetric.createMinInformationLoss();
        }
    }



    @Override
    public AggregateFunction getAggregateFunction() {
        if (precomputed) {
            return precomputedMetric.getAggregateFunction();
        } else {
            return defaultMetric.getAggregateFunction();
        }
    }

    @Override
    public double getGeneralizationFactor() {
        return defaultMetric.getGeneralizationFactor();
    }

    @Override
    public double getGeneralizationSuppressionFactor() {
        return defaultMetric.getGeneralizationSuppressionFactor();
    }
    
    @Override
    public ILScore getScore(final Transformation node, final HashGroupify groupify) {
        return precomputed ?
               precomputedMetric.getScore(node, groupify) :
               defaultMetric.getScore(node, groupify);
    }

    @Override
    public double getSuppressionFactor() {
        return defaultMetric.getSuppressionFactor();
    }
    
    @Override
    public boolean isIndependent() {
        return precomputed ? precomputedMetric.isIndependent() : defaultMetric.isIndependent();
    }
  
    
    @Override
    public boolean isPrecomputed() {
        return this.precomputed;
    }
    
    @Override
    public boolean isScoreFunctionSupported() {
        return isPrecomputed() ? precomputedMetric.isScoreFunctionSupported() : defaultMetric.isScoreFunctionSupported();
    }

    /**
     * Returns the default variant.
     *
     * @return
     */
    protected AbstractMetricMultiDimensional getDefaultMetric(){
        return this.defaultMetric;
    }

    @Override
    protected InformationLossWithBound<AbstractILMultiDimensional>
            getInformationLossInternal(Transformation node, HashGroupify groupify) {
        return precomputed ? precomputedMetric.getInformationLoss(node, groupify) : 
                             defaultMetric.getInformationLoss(node, groupify);
    }

    @Override
    protected InformationLossWithBound<AbstractILMultiDimensional> getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        if (precomputed) {
            return precomputedMetric.getInformationLoss(node, entry);
        } else {
            return defaultMetric.getInformationLoss(node, entry);
        }
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return precomputed ? precomputedMetric.getLowerBound(node) : 
                             defaultMetric.getLowerBound(node);
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node, HashGroupify groupify) {
        return precomputed ? precomputedMetric.getLowerBound(node, groupify) : 
                             defaultMetric.getLowerBound(node, groupify);
    }

    /**
     * Returns the precomputed variant.
     *
     * @return
     */
    protected AbstractMetricMultiDimensional getPrecomputedMetric(){
        return this.precomputedMetric;
    }
    
    /**
     * Returns the threshold.
     *
     * @return
     */
    protected double getThreshold() {
        return this.threshold;
    }
    
    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] ahierarchies, 
                                      final ARXConfiguration config) {
        
        this.precomputed = true;
        double rows = input.getDataLength();
        for (GeneralizationHierarchy hierarchy : ahierarchies) {
            double share = (double)hierarchy.getDistinctValues()[0] / rows;
            if (share > threshold) {
                this.precomputed = false;
                break;
            }
        }
        
        if (precomputed) {
            precomputedMetric.initializeInternal(manager, definition, input, ahierarchies, config);
        } else {
            defaultMetric.initializeInternal(manager, definition, input, ahierarchies, config);
        }
    }
}
