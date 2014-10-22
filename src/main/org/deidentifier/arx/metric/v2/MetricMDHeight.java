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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the Height metric.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDHeight extends AbstractMetricMultiDimensional {

    /** SVUID */
    private static final long serialVersionUID = -4720395539299677086L;

    /**
     * Creates a new instance
     */
    protected MetricMDHeight() {
        super(true, true, AggregateFunction.SUM);
    }

    /**
     * Creates a new instance
     */
    protected MetricMDHeight(AggregateFunction function) {
        super(true, true, function);
    }

    @Override
    public String toString() {
        return "Height";
    }
    
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        AbstractILMultiDimensional loss = getLowerBoundInternal(node);
        return new ILMultiDimensionalWithBound(loss, (AbstractILMultiDimensional)loss.clone());
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        double[] result = new double[getDimensions()];
        int[] transformation = node.getTransformation();
        for (int i=0; i<result.length; i++) {
            result[i] = transformation[i];
        }
        return super.createInformationLoss(result);
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                       IHashGroupify groupify) {
        return getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(DataDefinition definition,
                                      Data input,
                                      GeneralizationHierarchy[] hierarchies,
                                      ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);
        
        // Min and max
        double[] min = new double[hierarchies.length];
        double[] max = new double[min.length];

        for (int i=0; i<hierarchies.length; i++){
            String attribute = hierarchies[i].getName();
            min[i] = definition.getMinimumGeneralization(attribute);
            max[i] = definition.getMaximumGeneralization(attribute);
        }
        
        setMin(min);
        setMax(max);
    }
    
    /**
     * Returns the configuration of this metric
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(true,                       // monotonic
                                       0.5d,                       // gs-factor
                                       false,                      // precomputed
                                       0d,                         // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }
}
