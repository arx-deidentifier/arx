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

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
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

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount * rowCount);
        }
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount);
        }
    }

    @Override
    public String toString() {
        return "Non-Monotonic Discernability";
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(Node node, HashGroupifyEntry entry) {
        return new InformationLossDefaultWithBound(entry.count, entry.count);
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final HashGroupify g) {

        double value = 0;
        double lowerBound = 0; // DM*
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
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

    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return null;
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           HashGroupify groupify) {
        double lowerBound = 0; // DM*
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
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
