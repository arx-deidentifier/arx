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
 * This class provides an implementation of the non-monotonic DM metric.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricSDNMDiscernability extends AbstractMetricSingleDimensional {
    
    /** SVUID. */
    private static final long serialVersionUID = -8573084860566655278L;

    /**
     * Creates a new instance.
     */
    protected MetricSDNMDiscernability() {
        super(false, false);
    }

    /**
     * For subclasses.
     *
     * @param monotonic
     */
    MetricSDNMDiscernability(boolean monotonic) {
        super(monotonic, false);
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
            return new ILSingleDimensional(rows * rows);
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricSingleDimensional#createMinInformationLoss()
     */
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(rows);
        }
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
        return "Non-monotonic discernability";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        
        double rows = getNumTuples();
        double dm = 0;
        double dmStar = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count>0){
                double count = (double)m.count;
                double current = count * count;
                dmStar += current;
                dm += m.isNotOutlier ? current : rows * count;
            }
            m = m.nextOrdered;
        }
        return new ILSingleDimensionalWithBound(dm, dmStar);
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
        double lowerBound = 0;
        HashGroupifyEntry m = groupify.getFirstEntry();
        while (m != null) {
            lowerBound += (m.count>0) ? ((double) m.count * (double) m.count) : 0;
            m = m.nextOrdered;
        }
        return new ILSingleDimensional(lowerBound);
    }
}

