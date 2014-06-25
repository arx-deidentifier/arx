/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
    private double            total  = 0d;
    private boolean           dPresence        = false;

    protected MetricAECS() {
        super(true, false);
    }

    @Override
    protected InformationLossDefault evaluateInternal(final Node node, final IHashGroupify g) {

        int size = 0;
        // TODO: This is buggy. All EQs that are suppressed should result in one large equivalence class
        // When enforcing d-presence, use only ECs which contain at least one tuple from the research subset
        if (dPresence) { 
            HashGroupifyEntry m = g.getFirstEntry();
            while (m != null) {
                if (m.count > 0) {
                    size++;
                }
                m = m.nextOrdered;
            }
        } else {
            size = g.size();
        }

        final double value = total / size;
        return new InformationLossDefault(value);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] ahierarchies, 
                                      final ARXConfiguration config) {

        if (config.containsCriterion(DPresence.class)) {
            dPresence = true;
            Set<DPresence> crits = config.getCriteria(DPresence.class);
            if (crits.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported!"); }
            for (DPresence dPresence : crits) {
                total = dPresence.getSubset().getArray().length;
            }
        } else {
            total = input.getDataLength();
        }
    }
}
