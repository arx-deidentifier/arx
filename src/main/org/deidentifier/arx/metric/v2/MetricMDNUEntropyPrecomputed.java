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

    /** SVUID. */
    private static final long   serialVersionUID = 8053878428909814308L;

    /** Not available in the cache. */
    private static final double NOT_AVAILABLE    = Double.POSITIVE_INFINITY;

    /** Log 2. */
    private static final double LOG2             = Math.log(2);

    /**
     * Computes log 2.
     *
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / LOG2;
    }

    /** Cardinalities. */
    private Cardinalities cardinalities;

    /** Column -> Level -> Value. */
    private double[][]    cache;

    /** Column -> Id -> Level -> Output. */
    private int[][][]     hierarchies;

    /**
     * Precomputed.
     *
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
     * Creates a new instance.
     */
    protected MetricMDNUEntropyPrecomputed() {
        super(true, true, AggregateFunction.SUM);
    }

    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNUEntropyPrecomputed(AggregateFunction function){
        super(true, true, function);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(true,                       // monotonic
                                       0.5d,                       // gs-factor
                                       true,                       // precomputed
                                       1.0d,                       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Non-uniform entropy";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        
        double[] result = getInformationLossInternalRaw(node, g);
        
        // Switch sign bit and round
        for (int column = 0; column < hierarchies.length; column++) {
            result[column] = round(result[column] == 0.0d ? result[column] : -result[column]);
        }

        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result),
                                               super.createInformationLoss(result));
    }
    
    /**
     * 
     *
     * @param node
     * @param g
     * @return
     */
    protected double[] getInformationLossInternalRaw(final Node node, final IHashGroupify g) {

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

        return result;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return this.getInformationLossInternal(node, null).getLowerBound();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                               IHashGroupify groupify) {
        return this.getLowerBoundInternal(node);
    }

    /**
     * For backwards compatibility.
     *
     * @param cache
     * @param cardinalities
     * @param hierarchies
     */
    protected void initialize(double[][] cache, int[][][] cardinalities, int[][][] hierarchies) {
        
        // Initialize data structures
        this.cache = cache;
        this.hierarchies = hierarchies;
        this.cardinalities = new Cardinalities(cardinalities);

        // Initialize weights
        super.initialize(hierarchies.length);

        // Compute a reasonable maximum
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        
        // Its difficult to compute a reasonale maximum in this case
        double[] max = new double[hierarchies.length];
        Arrays.fill(max, Double.MAX_VALUE / hierarchies.length);
        
        super.setMax(max);
        super.setMin(min);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
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

        // Compute a reasonable min & max
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        
        double[] max = new double[hierarchies.length];
        for (int i=0; i<max.length; i++) {
            max[i] = input.getDataLength() * log2(input.getDataLength());
        }
        
        super.setMax(max);
        super.setMin(min);
    }
}
