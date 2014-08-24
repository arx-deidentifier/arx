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

    /** SVUID*/
    private static final long serialVersionUID = 7278544218893194559L;

    /** Is this instance precomputed*/
    private boolean precomputed = false;
    
    /** The threshold*/
    private final double threshold;
    
    /** The default metric*/
    private AbstractMetricMultiDimensional defaultMetric;
    
    /** The precomputed variant*/
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

    @Override
    public boolean isIndependent() {
        return precomputed ? precomputedMetric.isIndependent() : defaultMetric.isIndependent();
    }

    @Override
    protected InformationLossWithBound<AbstractILMultiDimensional>
            getInformationLossInternal(Node node, IHashGroupify groupify) {
        return precomputed ? precomputedMetric.getInformationLoss(node, groupify) : 
                             defaultMetric.getInformationLoss(node, groupify);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return precomputed ? precomputedMetric.getLowerBound(node) : 
                             defaultMetric.getLowerBound(node);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node, IHashGroupify groupify) {
        return precomputed ? precomputedMetric.getLowerBound(node, groupify) : 
                             defaultMetric.getLowerBound(node, groupify);
    }

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
}
