/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
 * This class provides an implementation of the Height metric.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDHeight extends AbstractMetricMultiDimensional {

    /** SVUID. */
    private static final long serialVersionUID = -4720395539299677086L;

    /**
     * Creates a new instance.
     */
    protected MetricMDHeight() {
        super(true, true, true, AggregateFunction.SUM);
    }

    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDHeight(AggregateFunction function) {
        super(true, true, true, function);
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
                                       0d,                         // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }
  
    /**
     * For backwards compatibility only.
     *
     * @param minHeight
     * @param maxHeight
     */
    public void initialize(int minHeight, int maxHeight) {
        super.initialize(1);
        setMin(new double[]{minHeight});
        setMax(new double[]{maxHeight});
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Height");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }
    
    @Override
    public String toString() {
        return "Height";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation<?> node, final HashGroupify g) {
        AbstractILMultiDimensional loss = getLowerBoundInternal(node);
        return new ILMultiDimensionalWithBound(loss, (AbstractILMultiDimensional)loss.clone());
    }
    
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation<?> node, HashGroupifyEntry entry) {
        double[] result = new double[getDimensions()];
        Arrays.fill(result, entry.count);
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation<?> node) {
        double[] result = new double[getDimensions()];
        int[] transformation = node.getGeneralization();
        for (int i=0; i<result.length; i++) {
            result[i] = transformation[i];
        }
        return super.createInformationLoss(result);
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation<?> node,
                                                       HashGroupify groupify) {
        return getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(manager, definition, input, hierarchies, config);
        
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
}
