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

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the (normalized) average equivalence class size metric.
 * We dont normailze the metric as proposed in the original publication [1], as this would only be possible for k-anonymity.
 * [1] LeFevre K, DeWitt DJ, Ramakrishnan R. Mondrian Multidimensional K-Anonymity. IEEE; 2006:25-25.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricSDAECS extends AbstractMetricSingleDimensional {

    /** SVUID. */
    private static final long serialVersionUID = 8076459507565472479L;

    /**
     * Creates a new instance.
     */
    protected MetricSDAECS() {
        super(false, false);
    }

    /**
     * Creates a new instance. Preinitialized
     *
     * @param rowCount
     */
    protected MetricSDAECS(double rowCount) {
        super(false, false);
        super.setNumTuples(rowCount);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricSingleDimensional#createMaxInformationLoss()
     */
    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(rows);
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricSingleDimensional#createMinInformationLoss()
     */
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(1d);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       0.5d,                       // gs-factor
                                       false,                      // precomputed
                                       0.0d,                       // precomputation threshold
                                       AggregateFunction.SUM       // aggregate function
                                       );
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Average equivalence class size";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {

        // The total number of groups with suppression
        int groupsWithSuppression = 0;
        // The total number of groups without suppression
        int groupsWithoutSuppression = 0;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                groupsWithSuppression += m.isNotOutlier ? 1 : 0;
                groupsWithoutSuppression++;
            }
            m = m.nextOrdered;
        }
        
        // If there are suppressed tuples, they form one additional group
        groupsWithSuppression += (groupsWithSuppression != groupsWithoutSuppression) ? 1 : 0;
        
        // Compute AECS
        return new ILSingleDimensionalWithBound(getNumTuples() / (double)groupsWithSuppression,
                                                getNumTuples() / (double)groupsWithoutSuppression);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected ILSingleDimensional getLowerBoundInternal(Node node) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILSingleDimensional getLowerBoundInternal(Node node,
                                                        IHashGroupify groupify) {
        // Ignore suppression for the lower bound
        int groups = 0;
        HashGroupifyEntry m = groupify.getFirstEntry();
        while (m != null) {
            groups += (m.count > 0) ? 1 : 0;
            m = m.nextOrdered;
        }
        
        // Compute AECS
        return new ILSingleDimensional(getNumTuples() / (double)groups);
    }
}
