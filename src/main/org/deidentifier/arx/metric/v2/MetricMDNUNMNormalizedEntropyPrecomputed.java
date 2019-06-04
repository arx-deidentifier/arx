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
 * This class provides an efficient implementation of normalized non-uniform entropy. See:<br>
 * A. De Waal and L. Willenborg: 
 * "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17–20, 1999.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUNMNormalizedEntropyPrecomputed extends MetricMDNUNMEntropyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -2384411534214262365L;

    /** Upper bounds */
    private double[]          upper;

    /**
     * Creates a new instance.
     *
     * @param function
     */
    public MetricMDNUNMNormalizedEntropyPrecomputed(AggregateFunction function) {
        super(0.5d, function); // TODO: REPLACE WITH GS_FACTOR WHEN APPLICABLE
    }
    
    /**
     * Creates a new instance.
     */
    protected MetricMDNUNMNormalizedEntropyPrecomputed() {
        super();
    }


    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       0.5d,                       // gs-factor
                                       true,                       // precomputed
                                       1.0d,                       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }

    @Override
    public String getName() {
        return "Normalized non-uniform entropy";
    }
    
    

    @Override
    public boolean isPrecomputed() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Normalized non-uniform entropy");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Normalized non-uniform entropy";
    }
    
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation<?> node, final HashGroupify g) {
        
        ILMultiDimensionalWithBound result = super.getInformationLossInternal(node, g);
        double[] loss = result.getInformationLoss() != null ? result.getInformationLoss().getValues() : null;
        double[] bound = result.getLowerBound() != null ?result.getLowerBound().getValues() : null;
        
        // Switch sign bit and round
        for (int column = 0; column < loss.length; column++) {
            if (loss != null) loss[column] /= upper[column];
            if (bound != null) bound[column] /= upper[column];
        }

        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(loss),
                                               super.createInformationLoss(bound));
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation<?> node, HashGroupifyEntry entry) {
        return super.getInformationLossInternal(node, entry);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation<?> node) {

        AbstractILMultiDimensional result = super.getLowerBoundInternal(node);
        if (result == null) return null;
        double[] loss = result.getValues();
        
        // Switch sign bit and round
        for (int column = 0; column < loss.length; column++) {
            loss[column] /= upper[column];
        }

        // Return
        return super.createInformationLoss(loss);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation<?> node,
                                                               HashGroupify groupify) {
        
        AbstractILMultiDimensional result = super.getLowerBoundInternal(node, groupify);
        if (result == null) return null;
        double[] loss = result.getValues();
        
        // Switch sign bit and round
        for (int column = 0; column < loss.length; column++) {
            loss[column] /= upper[column];
        }

        // Return
        return super.createInformationLoss(loss);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(manager, definition, input, hierarchies, config);

        this.upper = super.getUpperBounds();
        
        // Compute a reasonable min & max
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        
        double[] max = new double[hierarchies.length];
        Arrays.fill(max, 1d);
        
        super.setMax(max);
        super.setMin(min);
    }
}
