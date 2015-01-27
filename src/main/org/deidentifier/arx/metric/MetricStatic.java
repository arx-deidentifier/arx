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

import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of a static metric in
 * which information loss is user-defined per generalization level.
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricStatic extends MetricWeighted<InformationLossDefault> {

    /**  TODO */
    private static final long               serialVersionUID = 3778891174824606177L;

    /** The user defined information loss per level, indexed by column name. */
    protected final Map<String, List<Double>> _infoloss;

    /** The pre-calculated information loss. */
    private double[][]                      infoloss;

    /**
     * Constructor.
     *
     * @param infoloss
     */
    protected MetricStatic(final Map<String, List<Double>> infoloss) {
        super(true, true);
        _infoloss = infoloss;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new InformationLossDefault(Double.MAX_VALUE);
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
        return "Static";
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {

        double value = 0;
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            value += infoloss[i][transformation[i]];
        }
        return new InformationLossDefaultWithBound(value, value);
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
         return this.getLowerBoundInternal(node);
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

        // Initialize
        infoloss = new double[hierarchies.length][];
        for (int i = 0; i < hierarchies.length; i++) {
            final String attribute = hierarchies[i].getName();

            final List<Double> basicInfoloss = _infoloss.get(attribute);
            if (basicInfoloss == null) {
                throw new RuntimeException("No information loss defined for attribute [" + attribute + "]");
            }
            if (basicInfoloss.size() != hierarchies[i].getHeight()) {
                throw new RuntimeException("Information loss for attribute [" + attribute + "] is not defined on all levels.");
            }

            for (int j = 1; j < basicInfoloss.size(); j++) {
                if (basicInfoloss.get(j) < basicInfoloss.get(j - 1)) {
                    throw new RuntimeException("Information loss is not monotonic for attribute [" + attribute + "]");
                }
            }

            infoloss[i] = new double[basicInfoloss.size()];
            for (int j = 0; j < infoloss[i].length; j++) {
                double weight = weights != null ? weights[i] : 1d;
                infoloss[i][j] = basicInfoloss.get(j) * weight;
            }
        }
    }
}
