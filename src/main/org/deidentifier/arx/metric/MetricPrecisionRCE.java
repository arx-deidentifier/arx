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

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of a weighted variant of the monotonic precision metric. Individual
 * losses for each column will be compared with recursive conservative estimation.
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricPrecisionRCE extends MetricWeighted<InformationLossRCE> {

    /** SVUID*/
    private static final long serialVersionUID = 8669348894042436213L;

    /** Height */
    private int[]             height;

    /** Min */
    private double[]          min;

    /** Max */
    private double[]          max;

    /**
     * Creates a new instance
     */
    protected MetricPrecisionRCE() {
        super(true, true);
    }

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (max == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossRCE(max);
        }
    }
    
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (min == null) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossRCE(min);
        }
    }

    @Override
    public String toString() {
        return "Monotonic Precision with Conservative Estimation";
    }

    @Override
    protected InformationLossWithBound<InformationLossRCE> getInformationLossInternal(final Node node, final IHashGroupify g) {

        final int[] transformation = node.getTransformation();
        final double[] result = new double[transformation.length];
        for (int i = 0; i < transformation.length; i++) {
            result[i] = height[i] != 0 ? ((double) transformation[i] / (double) height[i]) : 0;
        }
        return new InformationLossWithBound<InformationLossRCE>(new InformationLossRCE(result, weights),
                                                            new InformationLossRCE(result, weights));
    }

    @Override
    protected InformationLossRCE getLowerBoundInternal(Node node) {
        return this.getInformationLossInternal(node, null).getLowerBound();
    }
    
    @Override
    protected InformationLossRCE getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        return getLowerBoundInternal(node);
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

        // Min and max
        this.min = new double[this.height.length];
        Arrays.fill(min, 0d);
        this.max = new double[this.height.length];
        Arrays.fill(max, 1d);
    }
}
