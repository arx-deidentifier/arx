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

    /** SVUID. */
    private static final long serialVersionUID = -218192738838711533L;
    
    /** Height. */
    private int[]             height;
    
    /** Number of cells. */
    private double            cells;

    /**
     * Creates a new instance.
     */
    protected MetricNMPrecision() {
        super(false, false);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new InformationLossDefault(1);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(0);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Non-Monotonic Precision";
    }

    /**
     * @return the heights
     */
    protected int[] getHeights() {
        return height;
    }
    
    /**
     * Returns the number of cells.
     *
     * @return
     */
    protected double getCells() {
        return cells;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {
        
        int suppressedTuples = 0;
        int unsuppressedTuples = 0;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // if (m.count > 0) is given implicitly
            unsuppressedTuples += m.isNotOutlier ? m.count : 0;
            suppressedTuples += m.isNotOutlier ? 0 : m.count;
            m = m.nextOrdered;
        }
        
        double precision = 0;
        for (int i = 0; i<height.length; i++) {
            double weight = weights != null ? weights[i] : 1d;
            double value = height[i] == 0 ? 0 : (double) node.getTransformation()[i] / (double) height[i];
            precision += (double)unsuppressedTuples * value * weight;
            precision += (double)suppressedTuples * 1d * weight;
        }
        
        precision /= cells;
        
        // Return
        return new InformationLossDefaultWithBound(precision, getLowerBound(node).getValue());
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricWeighted#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        double result = 0;
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            double weight = weights != null ? weights[i] : 1d;
            double level = (double) transformation[i];
            result += height[i] == 0 ? 0 : (level / (double) height[i]) * weight;
        }
        result /= (double) transformation.length;
        
        // Return
        return new InformationLossDefault(result);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricWeighted#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
       return getLowerBoundInternal(node);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricWeighted#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
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
