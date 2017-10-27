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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * This class provides an efficient implementation of the non-uniform entropy
 * metric. It avoids a cell-by-cell process by utilizing a three-dimensional
 * array that maps identifiers to their frequency for all quasi-identifiers and
 * generalization levels. It further reduces the overhead induced by subsequent
 * calls by caching the results for previous columns and generalization levels.
 * See:<br>
 * A. De Waal and L. Willenborg: 
 * "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17–20, 1999.
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

    /** Num rows */
    private double        rows;

    /** Minimal size of equivalence classes enforced by the differential privacy model */
    private double        k;

    /** The root values of all generalization hierarchies or -1 if no single root value exists */
    private int[]         rootValues;


    /**
     * Precomputed.
     *
     * @param monotonicWithGeneralization
     * @param monotonicWithSuppression
     * @param independent
     * @param gsFactor
     * @param function
     */
    public MetricMDNUEntropyPrecomputed(boolean monotonicWithGeneralization,
                                        boolean monotonicWithSuppression, 
                                        boolean independent, 
                                        double gsFactor, 
                                        AggregateFunction function) {
        super(monotonicWithGeneralization, monotonicWithSuppression, independent, gsFactor, function);
    }
    
    /**
     * Creates a new instance.
     */
    protected MetricMDNUEntropyPrecomputed() {
        super(true, true, true, 0.5d, AggregateFunction.SUM);
    }
    
    /**
     * Creates a new instance.
     *
     * @param gsFactor
     * @param function
     */
    protected MetricMDNUEntropyPrecomputed(double gsFactor, AggregateFunction function){
        super(true, true, true, gsFactor, function);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(true, // monotonic
                                       super.getGeneralizationSuppressionFactor(), // gs-factor
                                       true, // precomputed
                                       1.0d, // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
        );
    }
    
    @Override
    public ILScore getScore(final Transformation node, final HashGroupify groupify) {
        
        // Prepare
        int dimensionsGeneralized = getDimensionsGeneralized();
        IntIntOpenHashMap[] nonSuppressedValueToCount = new IntIntOpenHashMap[dimensionsGeneralized];
        for (int dimension=0; dimension<dimensionsGeneralized; dimension++) {
            nonSuppressedValueToCount[dimension] = new IntIntOpenHashMap();
        }

        // Compute score
        double score = 0d;
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            m.read();
            for (int dimension=0; dimension<dimensionsGeneralized; dimension++) {
                int value = m.next();
                // Process values of records which have not been suppressed by sampling
                if (m.isNotOutlier && (rootValues[dimension] == -1 || value != rootValues[dimension])) {
                    // The attribute value has neither been suppressed because of record suppression nor because of generalization
                    nonSuppressedValueToCount[dimension].putOrAdd(value, m.count, m.count);
                } else {
                    // The attribute value has been suppressed because of record suppression or because of generalization
                    score += (double)m.count * (double)rows;
                }
                // Add values for records which have been suppressed by sampling
                score += (double)(m.pcount - m.count) * (double)rows;
            }
            m = m.nextOrdered;
        }
        // Add values for all attribute values which were not suppressed
        for (int dimension=0; dimension<dimensionsGeneralized; dimension++) {
            final boolean [] states = nonSuppressedValueToCount[dimension].allocated;
            final int [] counts = nonSuppressedValueToCount[dimension].values;
            for (int i=0; i<states.length; i++) {
                if (states[i]) {
                    score += (double)counts[i] * (double)counts[i];
                }
            }
        }

        // Adjust sensitivity and multiply with -1 so that higher values are better
        score *= -1d / ((double)rows * (double)dimensionsGeneralized);
        return new ILScore((k==1) ? score / 5d : score / (double)(k * k / (k - 1d) + 1d));
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
    public boolean isScoreFunctionSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Non-uniform entropy");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }
    
    @Override
    public String toString() {
        return "Non-uniform entropy";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        
        double[] result = getInformationLossInternalRaw(node, g);
        
        // Switch sign bit and round
        for (int column = 0; column < hierarchies.length; column++) {
            result[column] = round(result[column] == 0.0d ? result[column] : -result[column]);
        }

        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result),
                                               super.createInformationLoss(result));
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        double[] result = new double[getDimensions()];
        Arrays.fill(result, entry.count);
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }

    /**
     * 
     *
     * @param node
     * @param g
     * @return
     */
    protected double[] getInformationLossInternalRaw(final Transformation node, final HashGroupify g) {

        // Prepare
        int[][][] cardinalities = this.cardinalities.getCardinalities();
        double[] result = new double[hierarchies.length];
        double gFactor = super.getGeneralizationFactor();

        // For each column
        for (int column = 0; column < hierarchies.length; column++) {

            // Check for cached value
            final int transformation = node.getGeneralization()[column];
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
            result[column] = value * gFactor;
        }

        return result;
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return this.getInformationLossInternal(node, (HashGroupify)null).getLowerBound();
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node,
                                                               HashGroupify groupify) {
        return this.getLowerBoundInternal(node);
    }
    
    /**
     * Returns the upper bound of the entropy value per column
     * @return
     */
    protected double[] getUpperBounds() {

        // Prepare
        int[][][] cardinalities = this.cardinalities.getCardinalities();
        double[] result = new double[hierarchies.length];
        double gFactor = super.getGeneralizationFactor();

        // For each column
        for (int column = 0; column < hierarchies.length; column++) {

            // Compute entropy
            double value = 0d;
            final int[][] cardinality = cardinalities[column];
            final int[][] hierarchy = hierarchies[column];
            for (int in = 0; in < hierarchy.length; in++) {
                final double a = cardinality[in][0];
                if (a != 0d) {
                    value += a * log2(a / rows);
                }
            }
            result[column] = value * gFactor;
        }
        
        // Switch sign bit and round
        for (int column = 0; column < hierarchies.length; column++) {
            result[column] = round(result[column] == 0.0d ? result[column] : -result[column]);
        }

        return result;
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
        
        // Its difficult to compute a reasonable maximum in this case
        double[] max = new double[hierarchies.length];
        Arrays.fill(max, Double.MAX_VALUE / hierarchies.length);
        
        super.setMax(max);
        super.setMin(min);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Obtain subset
        RowSet subset = super.getSubset(config);
        
        // Cardinalities
        this.cardinalities = new Cardinalities(input, subset, hierarchies);
        this.rows = input.getDataLength();
        double gFactor = super.getGeneralizationFactor();
        double sFactor = super.getSuppressionFactor();
        
        // Create a cache for the results
        this.cache = new double[hierarchies.length][];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new double[hierarchies[i].getArray()[0].length];
            Arrays.fill(cache[i], NOT_AVAILABLE);
        }
        
        // Create reference to the hierarchies
        final DataMatrix data = input.getArray();
        this.hierarchies = new int[data.getNumColumns()][][];
        for (int i = 0; i < hierarchies.length; i++) {
            this.hierarchies[i] = hierarchies[i].getArray();
        }

        // Compute a reasonable min & max
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        
        double[] max = new double[hierarchies.length];
        for (int i=0; i<max.length; i++) {
            max[i] = (input.getDataLength() * log2(input.getDataLength())) * Math.max(gFactor, sFactor);
        }
        
        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            
            // Store minimal size of equivalence classes
            EDDifferentialPrivacy dpCriterion = config.getPrivacyModel(EDDifferentialPrivacy.class);
            k = (double)dpCriterion.getMinimalClassSize();
            
            // Store root values of generalization hierarchies or null if no single root value exists
            rootValues = new int[hierarchies.length];
            for (int i = 0; i < hierarchies.length; i++) {
                int rootValue = -1;
                for (int[] row : hierarchies[i].getArray()) {
                    if (rootValue == -1) {
                        rootValue = row[row.length-1];
                    } else if (row[row.length-1] != rootValue) {
                        rootValue = -1;
                        break;
                    }
                }
                rootValues[i] = rootValue;
            }
        }
        
        super.setMax(max);
        super.setMin(min);
    }
}
