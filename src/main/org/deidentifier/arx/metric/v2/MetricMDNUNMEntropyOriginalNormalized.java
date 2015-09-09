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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the non-uniform entropy
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUNMEntropyOriginalNormalized extends MetricMDNUNMEntropyOriginal {

    /** SVUID */
    private static final long serialVersionUID = 831847901920892464L;

    /** Field */
    private double[]          normalization;

    /**
     * Creates a new instance.
     */
    protected MetricMDNUNMEntropyOriginalNormalized() {
        super();
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNUNMEntropyOriginalNormalized(AggregateFunction function){
        super(function);
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

    @Override
    public String toString() {
        return "Normalized non-uniform entropy (original)";
    }


    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        
        // Call and reuse
        double[] result = super.getInformationLossInternal(node, g).getInformationLoss().getValues();
        double[] array = new double[result.length];
        for (int i=0; i<array.length; i++) {
            array[i] = result[i] / normalization[i];
        }

        // Return
        return new ILMultiDimensionalWithBound(createInformationLoss(array));
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {

        super.initializeInternal(manager, definition, input, hierarchies, config);

        this.normalization = ((AbstractILMultiDimensional)super.createMaxInformationLoss()).getValues();
        double[] max = new double[hierarchies.length];
        Arrays.fill(max, 1d);
        super.setMax(max);
    }
}
