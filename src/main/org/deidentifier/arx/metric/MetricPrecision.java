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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of a monotonic weighted precision metric.
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricPrecision extends MetricWeighted<InformationLossDefault> {

    /** SVUID. */
    private static final long serialVersionUID = -7612335677779934529L;

    /** Height. */
    private int[]             maxLevels;

    /**
     * Creates a new instance.
     */
    protected MetricPrecision() {
        super(true, true);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new InformationLossDefault(1d);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(0d);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Monotonic Precision";
    }

    /**
     * @return the heights
     */
    protected int[] getHeights() {
        return maxLevels;
    }

    /**
     * Returns the number of cells.
     *
     * @return
     */
    protected double getCells() {
        return 0d;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {

        double result = 0;
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            double weight = weights != null ? weights[i] : 1d;
            double level = (double) transformation[i];
            result += maxLevels[i] == 0 ? 0 : (level / (double) maxLevels[i]) * weight;
        }
        result /= (double) transformation.length;
        return new InformationLossDefaultWithBound(result, result);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricWeighted#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return this.getInformationLossInternal(node, null).getLowerBound();
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
        maxLevels = new int[hierarchies.length];
        for (int j = 0; j < maxLevels.length; j++) {
            maxLevels[j] = hierarchies[j].getArray()[0].length - 1;
        }
    }
}
