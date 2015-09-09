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
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
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
public class MetricMDNUNMEntropyOriginal extends AbstractMetricMultiDimensional {

    /** SVUID */
    private static final long   serialVersionUID = 2492674912704409350L;

    /** Cardinalities. */
    private CardinalitiesGeneric              cardinalities;

    /** Tuple matcher */
    private TupleMatcher        matcher;

    /** Input */
    private int[][]             input;

    /** Log 2. */
    private static final double LOG2             = Math.log(2);

    /** Subset */
    private RowSet              subset;
    

    /**
     * Computes log 2.
     *
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / LOG2;
    }

    /**
     * Creates a new instance.
     */
    protected MetricMDNUNMEntropyOriginal() {
        super(false, false, AggregateFunction.SUM);
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNUNMEntropyOriginal(AggregateFunction function){
        super(false, false, function);
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
        return "Non-uniform entropy (original)";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        double[] result = new double[getDimensions()];
        Arrays.fill(result, entry.count);
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        
        // Prepare
        CardinalitiesGeneric outputCounts = new CardinalitiesGeneric(g);
        double[] result = new double[input[0].length];

        // Foreach row
        for (int row = 0; row < input.length; row++) {
            
            // If contained
            if (subset == null || subset.contains(row)) {
                    
                // For each column
                for (int column = 0; column < input[0].length; column++) {
    
                    int[] generalization = node.getGeneralization();
                    HashGroupifyEntry entry = this.matcher.getEntry(row, generalization, g);
                    double inputFrequency = cardinalities.getCardinality(column, input[row][column]);
                    double outputFrequency = outputCounts.getCardinality(column, entry.isNotOutlier ? entry.key[column] : -1);
                    result[column] += log2(inputFrequency / outputFrequency);
                }
            }
        }
        
        // Switch sign bit and round
        for (int column = 0; column < input[0].length; column++) {
            result[column] = round(result[column] == 0.0d ? result[column] : -result[column]);
        }

        // Return
        return new ILMultiDimensionalWithBound(createInformationLoss(result));
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node,
                                                           HashGroupify groupify) {
       return null;
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {

        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Obtain subset
        this.subset = null;
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criterion = config.getCriteria(DPresence.class);
            if (criterion.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported"); }
            this.subset = criterion.iterator().next().getSubset().getSet();
        } 
        
        // Prepare
        this.cardinalities = new CardinalitiesGeneric(input, subset);
        this.matcher = new TupleMatcher(hierarchies, input.getArray());
        this.input = input.getArray();
        
        // Set min & max
        super.setMin(new double[hierarchies.length]);
        super.setMax(getMax(input.getArray()));
    }
    

    /**
     * Returns the upper bound of the entropy value per column
     * @return
     */
    protected double[] getMax(int[][] input) {

        // Prepare
        double[] result = new double[input[0].length];

        // For each column
        for (int column = 0; column < input[0].length; column++) {
            
            // For each row
            double value = 0d;
            for (int row = 0; row < input.length; row++) {
                
                if (this.subset == null || this.subset.contains(row)) {   
                    double inputFrequency = cardinalities.getCardinality(column, input[row][column]);
                    double outputFrequency = this.subset == null ? input.length : this.subset.size();
                    value += log2(inputFrequency / outputFrequency);
                }
            }
            result[column] = value;
        }
        
        // Switch sign bit and round
        for (int column = 0; column < input[0].length; column++) {
            result[column] = round(result[column] == 0.0d ? result[column] : -result[column]);
        }

        return result;
    }
}
