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
 * This class provides an implementation of the (normalized) average equivalence class size metric.
 * We dont normailze the metric as proposed in the original publication [1], as this would only be possible for k-anonymity.
 * [1] LeFevre K, DeWitt DJ, Ramakrishnan R. Mondrian Multidimensional K-Anonymity. IEEE; 2006:25-25.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricAECS extends MetricDefault {

    private static final long serialVersionUID = -532478849890959974L;

    /** Number of tuples*/
    private int rowCount = 0;
    
    protected MetricAECS() {
        super(false, false);
    }
    
    @Override
    public String toString() {
        return "Average Equivalence Class Size";
    }
    
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount);
        }
    }
    
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(1);
    }
    
    @Override
    protected InformationLossDefault evaluateInternal(final Node node, final IHashGroupify g) {

        // The total number of groups with suppression
        int groups = 0;
        // The total number of tuples
        int tuples = 0;
        // Are there suppressed tuples
        boolean suppressed = false;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                tuples += m.count;
                groups += m.isNotOutlier ? 1 : 0;
                suppressed |= !m.isNotOutlier;
            }
            m = m.nextOrdered;
        }
        
        // If there are suppressed tuples, they form one additional group
        groups += suppressed ? 1 : 0;
        
        // Compute AECS
        return new InformationLossDefault((double)tuples / (double)groups,
                                          (double)tuples / (double)g.size());
    }

    @Override
    protected void
            initializeInternal(DataDefinition definition, Data input, GeneralizationHierarchy[] hierarchies, ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> crits = config.getCriteria(DPresence.class);
            if (crits.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported!"); }
            for (DPresence dPresence : crits) {
                rowCount = dPresence.getSubset().getArray().length;
            }
        } else {
            rowCount = input.getDataLength();
        }
    }
}
