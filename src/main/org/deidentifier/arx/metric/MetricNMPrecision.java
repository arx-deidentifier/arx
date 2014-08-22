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
 * This class provides an implementation of a weighted precision metric as 
 * proposed in: <br>
 * Sweeney, L. (2002). Achieving k-anonymity privacy protection using generalization and suppression.<br> 
 * International Journal of Uncertainty Fuzziness and, 10(5), 2002.<br>
 * <br>
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricNMPrecision extends MetricWeighted<InformationLossDefault> {

    /** SVUID */
    private static final long serialVersionUID = -218192738838711533L;
    /** Height */
    private int[]             height;
    /** Number of cells*/
    private double            cells;

    /**
     * Creates a new instance
     */
    protected MetricNMPrecision() {
        super(false, false);
    }

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new InformationLossDefault(1);
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(0);
    }

    @Override
    public String toString() {
        return "Non-Monotonic Precision";
    }

    @Override
    public InformationLossDefault getLowerBound(Node node) {
        return (InformationLossDefault)node.getLowerBound();
    }

    @Override
    public InformationLossDefault getLowerBound(Node node,
                                                IHashGroupify groupify) {
        if (node.getLowerBound() != null) {
            return (InformationLossDefault)node.getLowerBound();
        }
        
        double lowerBound = 0d;
        
        HashGroupifyEntry m = groupify.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                for (int i = 0; i < height.length; i++) {
                    lowerBound += m.count * (height[i] == 0 ? 0 : (double) m.key[i] / (double) height[i]);
                }
            }
            m = m.nextOrdered;
        }
        
        lowerBound /= cells;
        
        // Return
        return new InformationLossDefault(lowerBound);
    }
    
    @Override
    protected BoundInformationLoss<InformationLossDefault> evaluateInternal(final Node node, final IHashGroupify g) {
        
        double total = 0d;
        double lowerBound = 0d;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                for (int i = 0; i < height.length; i++) {
                    total += m.count * (m.isNotOutlier ? (height[i] == 0 ? 0 : (double) m.key[i] / (double) height[i]) : 1d);
                    lowerBound += m.count * (height[i] == 0 ? 0 : (double) m.key[i] / (double) height[i]);
                }
            }
            m = m.nextOrdered;
        }
        
        total /= cells;
        lowerBound /= cells;
        
        // Return
        return new BoundInformationLossDefault(total, lowerBound);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);

        // Initialize maximum levels
        height = new int[hierarchies.length];
        for (int j = 0; j < height.length; j++) {
            height[j] = hierarchies[j].getArray()[0].length - 1;
        }
        
        int rowCount = 0;
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> crits = config.getCriteria(DPresence.class);
            if (crits.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported!"); }
            for (DPresence dPresence : crits) {
                rowCount = dPresence.getSubset().getArray().length;
            }
        } else {
            rowCount = input.getDataLength();
        }
        this.cells = (double)rowCount * (double)input.getHeader().length;
    }
}
