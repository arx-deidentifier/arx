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

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.data.Data;
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

    /** SVUID*/
    private static final long serialVersionUID = -1082954137578580790L;

    /** Row count*/
    private Double            tuples         = null;

    /**
     * Creates a new instance
     * @param monotonic
     * @param independent
     */
    protected AbstractMetricSingleDimensional(final boolean monotonic, final boolean independent) {
        super(monotonic, independent);
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
     * Returns the number of rows in the dataset or subset
     * @return
     */
    protected Double getNumTuples() {
        return tuples;
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Store row count
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criterion = config.getCriteria(DPresence.class);
            if (criterion.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported"); }
            tuples = (double)criterion.iterator().next().getSubset().getArray().length;
        } else {
            tuples = (double)input.getDataLength();
        }
    }

    /**
     * Returns the number of rows in the dataset or subset
     * @return
     */
    protected void setNumTuples(Double tuples) {
        this.tuples = tuples;
    }
}
