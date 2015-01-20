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

