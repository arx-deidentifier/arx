/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of a static metric in
 * which information loss is user-defined per generalization level.
 * This metric will respect attribute weights defined in the configuration.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDStatic extends AbstractMetricMultiDimensional {

    /** SVUID. */
    private static final long serialVersionUID = -1436611621616365335L;

    /** The user defined information loss per level, indexed by column name. */
    private final Map<String, List<Double>> _infoloss;

    /** The pre-calculated information loss. */
    private double[][]                      infoloss;

    /**
     * Constructor.
     *
     * @param function
     * @param infoloss
     */
    protected MetricMDStatic(final AggregateFunction function, final Map<String, List<Double>> infoloss) {
        super(true, true, true, function);
        _infoloss = infoloss;
    }

    /**
     * Constructor.
     *
     * @param infoloss
     */
    protected MetricMDStatic(final Map<String, List<Double>> infoloss) {
        super(true, true, true, AggregateFunction.SUM);
        _infoloss = infoloss;
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(true,                       // monotonic
                                       0.5d,                       // gs-factor
                                       false,                      // precomputed
                                       0.0d,                       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }
    
    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Static");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }
    
    @Override
    public String toString() {
        return "Static";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        AbstractILMultiDimensional loss = this.getLowerBoundInternal(node);
        return new ILMultiDimensionalWithBound(loss, (AbstractILMultiDimensional)loss.clone());
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        double[] result = new double[getDimensions()];
        Arrays.fill(result, entry.count);
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        double[] values = new double[getDimensions()];
        int[] transformation = node.getGeneralization();
        for (int i = 0; i < transformation.length; i++) {
            values[i] = infoloss[i][transformation[i]];
        }
        return super.createInformationLoss(values);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node,
                                                       HashGroupify groupify) {
        return this.getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(manager, definition, input, hierarchies, config);

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
                infoloss[i][j] = basicInfoloss.get(j);
            }
        }
        
        // Min and max
        double[] min = new double[hierarchies.length];
        double[] max = new double[min.length];

        for (int i=0; i<hierarchies.length; i++){
            
            String attribute = hierarchies[i].getName();
            min[i] = infoloss[i][definition.getMinimumGeneralization(attribute)];
            max[i] = infoloss[i][definition.getMaximumGeneralization(attribute)];
        }
        
        setMin(min);
        setMax(max);
    }
}
