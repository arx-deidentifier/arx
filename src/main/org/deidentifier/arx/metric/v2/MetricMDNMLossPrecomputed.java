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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class implements a variant of the Loss metric.
 * TODO: Add reference.
 *
 * @author Fabian Prasser
 */
public class MetricMDNMLossPrecomputed extends MetricMDNMLoss {

    /** SUID. */
    private static final long serialVersionUID = -7505441444551612996L;

    /** Cardinalities. */
    private Cardinalities     cardinalities;
    
    /** Distinct values: attribute -> level -> values. */
    private int[][][]         values;
    
    /**
     * Creates a new instance.
     */
    protected MetricMDNMLossPrecomputed() {
        super();
    }

    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNMLossPrecomputed(AggregateFunction function) {
        super(function);
    }

    /**
     * Creates a new instance.
     *
     * @param gsFactor
     * @param function
     */
    protected MetricMDNMLossPrecomputed(double gsFactor,
                                        AggregateFunction function) {
        super(gsFactor, function);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                                       // monotonic
                                       super.getGeneralizationSuppressionFactor(),  // gs-factor
                                       true,                                        // precomputed
                                       1.0d,                                        // precomputation threshold
                                       this.getAggregateFunction()                  // aggregate function
                                       );
    }

    @Override
    public boolean isAbleToHandleMicroaggregation() {
        return true;
    }

    @Override
    public boolean isClassBasedInformationLossAvailable() {
        return true;
    }

    @Override
    public boolean isGSFactorSupported() {
        return true;
    }

    @Override
    public boolean isPrecomputed() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Loss");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation<?> node) {

        // Prepare
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        int[] transformation = node.getGeneralization();
        double[] bound = new double[dimensions];
        DomainShare[] shares = super.getShares();
        double gFactor = super.getGeneralizationFactor();
        // Column -> Id -> Level -> Count
        int[][][] cardinalities = this.cardinalities.getCardinalities();


        // For each column
        for (int column = 0; column < dimensionsGeneralized; column++) {

            // Check for cached value
            int level = transformation[column];
            int[][] cardinality = cardinalities[column];
            int[] values = this.values[column][level];
            
            for (int value : values) {
                double count = cardinality[value][level];
                double share = count * shares[column].getShare(value, level);
                bound[column] += share * gFactor;
            }
        }
        // Note: we ignore microaggregation, as we cannot compute a bound for it
        // this means that the according entries in the resulting array are not changed and remain 0d
        // This is not a problem, as it is OK to underestimate information loss when computing lower bounds
                
        // Normalize
        for (int column=0; column<dimensionsGeneralized; column++){
            bound[column] = normalizeGeneralized(bound[column], column);
        }
        
        // Return
        return super.createInformationLoss(bound);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation<?> node, HashGroupify g) {
        return this.getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare super
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Compute cardinalities
        RowSet subset = super.getSubset(config);
        
        // Cardinalities
        this.cardinalities = new Cardinalities(input, subset, hierarchies);
        
        // Distinct values
        this.values = new int[hierarchies.length][][];
        for (int i=0; i<values.length; i++) {
            values[i] = new int[hierarchies[i].getHeight()][];
            for (int j=0; j<values[i].length; j++){
                values[i][j] = hierarchies[i].getDistinctValues(j);
            }
        }
    }
}
