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

package org.deidentifier.arx.metric;

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of the DM metric (non-monotonic).
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricDM extends MetricDefault {
    
    /** SVUID. */
    private static final long serialVersionUID = 4886262855672670521L;
    
    /** Number of tuples. */
    private int            rowCount         = 0;

    /**
     * Creates a new instance.
     */
    protected MetricDM() {
        super(false, false);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount * rowCount);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Non-Monotonic Discernability";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {

        double value = 0;
        double lowerBound = 0; // DM*
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                if (m.isNotOutlier) {
                    double current = ((double) m.count * (double) m.count);
                    value += current;
                    lowerBound += current;
                } else {
                    value += ((double) rowCount * (double) m.count);
                    lowerBound += ((double) m.count * (double) m.count);
                }
            }
            m = m.nextOrdered;
        }
        return new InformationLossDefaultWithBound(value, lowerBound);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        double lowerBound = 0; // DM*
        HashGroupifyEntry m = groupify.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                lowerBound += ((double) m.count * (double) m.count);
            }
            m = m.nextOrdered;
        }
        return new InformationLossDefault(lowerBound);
    }

    /**
     * Returns the current row count.
     *
     * @return
     */
    protected double getRowCount() {
        return this.rowCount;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input,
                                      final GeneralizationHierarchy[] hierarchies,
                                      final ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> crits = config.getCriteria(DPresence.class);
            if (crits.size() > 1) {
                throw new IllegalArgumentException("Only one d-presence criterion supported!");
            }
            for (DPresence dPresence : crits) {
                rowCount = dPresence.getSubset().getArray().length;
            }
        } else {
            rowCount = input.getDataLength();
        }
    }
}
