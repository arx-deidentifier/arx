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

package org.deidentifier.arx.metric;

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an abstract skeleton for the implementation of weighted metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @param <T>
 */
public abstract class MetricWeighted<T extends InformationLoss<?>> extends Metric<T> {

    /** SSUID. */
    private static final long         serialVersionUID = 6508220940790010968L;
    
    /** The weights. */
    protected double[]                weights;

    /**
     * Constructor.
     *
     * @param monotonic
     * @param independent
     */
    public MetricWeighted(final boolean monotonic, final boolean independent) {
        super(monotonic, independent);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected T getLowerBoundInternal(final Node node) {
        return (T)node.getLowerBound();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected T getLowerBoundInternal(final Node node, final IHashGroupify groupify) {
        return (T)node.getLowerBound();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {

        // Initialize weights
        weights = new double[hierarchies.length];
        double total = 0d;
        for (int i = 0; i < hierarchies.length; i++) {
            String attribute = hierarchies[i].getName();
            double weight = config.getAttributeWeight(attribute);
            weights[i] = weight;
            total += weight;
        }
        
        // Normalize: default case
        if (total == 0d) {
            Arrays.fill(weights, 1d);
        // Weighted case
        } else {
            for (int i=0; i<weights.length; i++){
                weights[i] /= total;
            }
        }
    }
}
