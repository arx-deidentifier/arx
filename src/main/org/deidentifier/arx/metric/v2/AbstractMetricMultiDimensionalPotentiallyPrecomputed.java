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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
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
    private static final long serialVersionUID = 7278544218893194559L;

    /** Is this instance precomputed. */
    private boolean precomputed = false;
    
    /** The threshold. */
    private final double threshold;
    
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

        super(defaultMetric.isMonotonic(), false, defaultMetric.getAggregateFunction());
        if (defaultMetric.getAggregateFunction() != precomputedMetric.getAggregateFunction()) { throw new IllegalArgumentException("Aggregate function does not match"); }
        if (defaultMetric.isMonotonic() != precomputedMetric.isMonotonic()) { throw new IllegalArgumentException("Monotonicity does not match"); }

        // Default is non-precomputed
        this.threshold = threshold;
        this.precomputed = false;
        this.defaultMetric = defaultMetric;
        this.precomputedMetric = precomputedMetric;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (precomputed) {
            return precomputedMetric.createMaxInformationLoss();
        } else {
            return defaultMetric.createMaxInformationLoss();
        }
    }


    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (precomputed) {
            return precomputedMetric.createMinInformationLoss();
        } else {
            return defaultMetric.createMinInformationLoss();
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#getAggregateFunction()
     */
    @Override
    public AggregateFunction getAggregateFunction() {
        if (precomputed) {
            return precomputedMetric.getAggregateFunction();
        } else {
            return defaultMetric.getAggregateFunction();
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#isIndependent()
     */
    @Override
    public boolean isIndependent() {
        return precomputed ? precomputedMetric.isIndependent() : defaultMetric.isIndependent();
    }
  
    
    /**
     * Returns the default variant.
     *
     * @return
     */
    protected AbstractMetricMultiDimensional getDefaultMetric(){
        return this.defaultMetric;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<AbstractILMultiDimensional>
            getInformationLossInternal(Node node, IHashGroupify groupify) {
        return precomputed ? precomputedMetric.getInformationLoss(node, groupify) : 
                             defaultMetric.getInformationLoss(node, groupify);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return precomputed ? precomputedMetric.getLowerBound(node) : 
                             defaultMetric.getLowerBound(node);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node, IHashGroupify groupify) {
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(final DataDefinition definition,
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
            precomputedMetric.initializeInternal(definition, input, ahierarchies, config);
        } else {
            defaultMetric.initializeInternal(definition, input, ahierarchies, config);
        }
    }
    
    /**
     * Returns whether the metric is precomputed.
     *
     * @return
     */
    protected boolean isPrecomputed() {
        return this.precomputed;
    }
}
