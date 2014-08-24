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

import java.util.Arrays;
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
public class MetricMDNMPrecision extends AbstractMetricMultiDimensional {

    /** SVUID*/
    private static final long serialVersionUID = 7972929684708525849L;

    /** Row count */
    private double            rowCount;

    /** Hierarchy heights */
    private int[]             heights;

    /**
     * Creates a new instance
     */
    protected MetricMDNMPrecision() {
        super(false, false, AggregateFunction.ARITHMETIC_MEAN);
    }
    
    /**
     * Creates a new instance
     * @param function
     */
    protected MetricMDNMPrecision(AggregateFunction function){
        super(false, false, function);
    }

    /**
     * For subclasses
     * @param monotonic
     * @param independent
     * @param function
     */
    protected MetricMDNMPrecision(boolean monotonic, boolean independent, AggregateFunction function){
        super(monotonic, independent, function);
    }

    @Override
    public String toString() {
        return "Non-monotonic precision";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        
        int suppressedTuples = 0;
        int unsuppressedTuples = 0;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // if (m.count > 0) is given implicitly
            unsuppressedTuples += m.isNotOutlier ? m.count : 0;
            suppressedTuples += m.isNotOutlier ? 0 : m.count;
            m = m.nextOrdered;
        }
        
        double[] result = new double[getDimensions()];
        for (int i = 0; i<heights.length; i++) {
            double value = heights[i] == 0 ? 0 : (double) node.getTransformation()[i] / (double) heights[i];
            result[i] += (double)unsuppressedTuples * value + (double)suppressedTuples;
            result[i] /= rowCount;
        }
        
        // Return
        return new ILMultiDimensionalWithBound(createInformationLoss(result), 
                                               (AbstractILMultiDimensional)getLowerBoundInternal(node).clone());
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        double[] result = new double[getDimensions()];
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            double level = (double) transformation[i];
            result[i] += heights[i] == 0 ? 0 : (level / (double) heights[i]);
        }
        return createInformationLoss(result);
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
       return getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);

        // Min and max
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        double[] max = new double[min.length];
        Arrays.fill(max, 1d);
        setMin(min);
        setMax(max);
        
        // Store row count
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criterion = config.getCriteria(DPresence.class);
            if (criterion.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported"); }
            rowCount = (double)criterion.iterator().next().getSubset().getArray().length;
        } else {
            rowCount = (double)input.getDataLength();
        }

        // Store heights
        this.heights = new int[hierarchies.length];
        for (int j = 0; j < heights.length; j++) {
            heights[j] = hierarchies[j].getArray()[0].length - 1;
        }
    }
}
