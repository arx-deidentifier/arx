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

import java.util.Arrays;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an efficient implementation of the non-uniform entropy
 * metric. It avoids a cell-by-cell process by utilizing a three-dimensional
 * array that maps identifiers to their frequency for all quasi-identifiers and
 * generalization levels. It further reduces the overhead induced by subsequent
 * calls by caching the results for previous columns and generalization levels.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUEntropyPrecomputed extends AbstractMetricMultiDimensional {

    /** SVUID */
    private static final long   serialVersionUID = 8053878428909814308L;

    /** Not available in the cache */
    private static final double NOT_AVAILABLE    = Double.POSITIVE_INFINITY;

    /** Log 2 */
    private static final double LOG2             = Math.log(2);

    /**
     * Computes log 2
     * 
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / LOG2;
    }
    
    /** Cardinalities*/
    private Cardinalities cardinalities;

    /** Column -> Level -> Value */
    private double[][]            cache;

    /** Column -> Id -> Level -> Output */
    private int[][][]             hierarchies;

    /**
     * Precomputed
     * @param monotonic
     * @param independent
     * @param function
     */
    public MetricMDNUEntropyPrecomputed(boolean monotonic, 
                                        boolean independent, 
                                        AggregateFunction function) {
        super(monotonic, independent, function);
    }
    
    /**
     * Creates a new instance
     */
    protected MetricMDNUEntropyPrecomputed() {
        super(true, true, AggregateFunction.SUM);
    }

    /**
     * Creates a new instance
     * @param function
     */
    protected MetricMDNUEntropyPrecomputed(AggregateFunction function){
        super(true, true, function);
    }

    @Override
    public String toString() {
        return "Non-uniform entropy";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        

        // Prepare
        int[][][] cardinalities = this.cardinalities.getCardinalities();
        double[] result = new double[hierarchies.length];

        // For each column
        for (int column = 0; column < hierarchies.length; column++) {

            // Check for cached value
            final int transformation = node.getTransformation()[column];
            double value = cache[column][transformation];
            if (value == NOT_AVAILABLE) {
                value = 0d;
                final int[][] cardinality = cardinalities[column];
                final int[][] hierarchy = hierarchies[column];
                for (int in = 0; in < hierarchy.length; in++) {
                    final int out = hierarchy[in][transformation];
                    final double a = cardinality[in][0];
                    final double b = cardinality[out][transformation];
                    if (a != 0d) {
                        value += a * log2(a / b);
                    }
                }
                cache[column][transformation] = value;
            }
            result[column] = value;
        }

        // Switch sign bit
        for (int column = 0; column < hierarchies.length; column++) {
            result[column] = result[column] == 0.0d ? result[column] : -result[column];
        }

        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result),
                                               super.createInformationLoss(result));
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return this.getInformationLossInternal(node, null).getLowerBound();
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                               IHashGroupify groupify) {
        return this.getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(DataDefinition definition,
                                      Data input,
                                      GeneralizationHierarchy[] hierarchies,
                                      ARXConfiguration config) {
        
        super.initializeInternal(definition, input, hierarchies, config);

        // Obtain subset
        RowSet subset = null;
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criterion = config.getCriteria(DPresence.class);
            if (criterion.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported"); }
            subset = criterion.iterator().next().getSubset().getSet();
        } 
        
        // Cardinalities
        this.cardinalities = new Cardinalities(input, subset, hierarchies);
        
        // Create a cache for the results
        cache = new double[hierarchies.length][];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new double[hierarchies[i].getArray()[0].length];
            Arrays.fill(cache[i], NOT_AVAILABLE);
        }
        
        // Create reference to the hierarchies
        final int[][] data = input.getArray();
        this.hierarchies = new int[data[0].length][][];
        for (int i = 0; i < hierarchies.length; i++) {
            this.hierarchies[i] = hierarchies[i].getArray();
        }

        // TODO: Compute a reasonable maximum
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        double[] max = new double[hierarchies.length];
        Arrays.fill(max, Double.MAX_VALUE / hierarchies.length);
        super.setMax(max);
        super.setMin(min);
    }

    /**
     * Returns the configuration of this metric
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(true,                       // monotonic
                                       0.5d,                       // gs-factor
                                       true,                       // precomputed
                                       1.0d,                       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }
}
