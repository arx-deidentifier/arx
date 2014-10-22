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

    /** SVUID*/
    private static final long serialVersionUID = 8076459507565472479L;

    /**
     * Creates a new instance
     */
    protected MetricSDAECS() {
        super(false, false);
    }
    
    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(rows);
        }
    }
    
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(1d);
    }
    
    @Override
    public String toString() {
        return "Average equivalence class size";
    }
    
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

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Node node) {
        return null;
    }

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

    /**
     * Returns the configuration of this metric
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       0.5d,                       // gs-factor
                                       false,                      // precomputed
                                       0.0d,                       // precomputation threshold
                                       AggregateFunction.SUM       // aggregate function
                                       );
    }
}
