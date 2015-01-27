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
import org.deidentifier.arx.metric.MetricConfiguration;

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

    /** SVUID. */
    private static final long serialVersionUID = 7972929684708525849L;

    /** Row count. */
    private double            rowCount;

    /** Hierarchy heights. */
    private int[]             heights;

    /**
     * Creates a new instance.
     */
    protected MetricMDNMPrecision() {
        super(false, false, AggregateFunction.ARITHMETIC_MEAN);
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNMPrecision(AggregateFunction function){
        super(false, false, function);
    }

    /**
     * For subclasses.
     *
     * @param monotonic
     * @param independent
     * @param function
     */
    protected MetricMDNMPrecision(boolean monotonic, boolean independent, AggregateFunction function){
        super(monotonic, independent, function);
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
                                       this.getAggregateFunction() // aggregate function
                                       );
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Non-monotonic precision";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
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
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
       return getLowerBoundInternal(node);
    }

    /**
     * For backwards compatibility only.
     *
     * @param heights
     * @param cells
     */
    protected void initialize(int[] heights, double cells){

        super.initialize(heights.length);
        this.heights = heights;
        this.rowCount = cells / heights.length;

        // Min and max
        double[] min = new double[heights.length];
        Arrays.fill(min, 0d);
        double[] max = new double[min.length];
        Arrays.fill(max, 1d);
        setMin(min);
        setMax(max);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
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
