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

package org.deidentifier.arx.metric;

import java.util.Arrays;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an efficient implementation of the non-uniform entropy
 * metric. It avoids a cell-by-cell process by utilizing a three-dimensional
 * array that maps identifiers to their frequency for all quasi-identifiers and
 * generalization levels. It further reduces the overhead induced by subsequent
 * calls by caching the results for previous columns and generalization levels.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricEntropy extends MetricDefault {

    /** Value unknown. */
    private static final double NA               = Double.POSITIVE_INFINITY;
    
    /** SVUID. */
    private static final long   serialVersionUID = -8618697919821588987L;
    
    /** Log 2. */
    static final double         log2             = Math.log(2);
    
    /**
     * Computes log 2.
     *
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / log2;
    }

    /** Column -> Level -> Value. */
    private double[][] cache;

    /** Column -> Id -> Level -> Count. */
    private int[][][]  cardinalities;

    /** Column -> Id -> Level -> Output. */
    private int[][][]  hierarchies;

    /**
     * 
     */
    protected MetricEntropy() {
        super(true, true);
    }

    /**
     * 
     *
     * @param monotonic
     * @param independent
     */
    protected MetricEntropy(final boolean monotonic, final boolean independent) {
        super(monotonic, independent);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Monotonic Non-Uniform Entropy";
    }

    /**
     * @return the cache
     */
    protected double[][] getCache() {
        return cache;
    }
    
    /**
     * @return the cardinalities
     */
    protected int[][][] getCardinalities() {
        return cardinalities;
    }

    /**
     * @return the hierarchies
     */
    protected int[][][] getHierarchies() {
        return hierarchies;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {

        if (node.getLowerBound() != null) { 
            return new InformationLossWithBound<InformationLossDefault>((InformationLossDefault)node.getLowerBound(),
                                                                    (InformationLossDefault)node.getLowerBound()); 
        }
        
        // Init
        double result = 0;

        // For each column
        for (int column = 0; column < hierarchies.length; column++) {

            // Check for cached value
            final int state = node.getTransformation()[column];
            double value = cache[column][state];
            if (value == NA) {
                value = 0d;
                final int[][] cardinality = cardinalities[column];
                final int[][] hierarchy = hierarchies[column];
                for (int in = 0; in < hierarchy.length; in++) {
                    final int out = hierarchy[in][state];
                    final double a = cardinality[in][0];
                    final double b = cardinality[out][state];
                    if (a != 0d) {
                        value += a * log2(a / b);
                    }
                }
                cache[column][state] = value;
            }
            result += value;
        }
        result = round(result == 0.0d ? result : -result);
        return new InformationLossDefaultWithBound(result, result);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return getInformationLossInternal(node, null).getLowerBound();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        return getLowerBoundInternal(node);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] ahierarchies, 
                                      final ARXConfiguration config) {
        
        // Obtain dictionary
        final Dictionary dictionary = input.getDictionary();

        // Obtain research subset
        RowSet rSubset = null;
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> crits = config.getCriteria(DPresence.class);
            if (crits.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported!"); }
            for (DPresence dPresence : crits) {
                rSubset = dPresence.getSubset().getSet();
            }
        }

        // Create reference to the hierarchies
        final int[][] data = input.getArray();
        hierarchies = new int[data[0].length][][];
        for (int i = 0; i < ahierarchies.length; i++) {
            hierarchies[i] = ahierarchies[i].getArray();
            // Column -> Id -> Level -> Output
        }

        // Initialize counts
        cardinalities = new int[data[0].length][][];
        for (int i = 0; i < cardinalities.length; i++) {
            cardinalities[i] = new int[dictionary.getMapping()[i].length][ahierarchies[i].getArray()[0].length];
            // Column -> Id -> Level -> Count
        }

		for (int i = 0; i < data.length; i++) { 
			// only use the rows contained in the research subset
			if (rSubset == null || rSubset.contains(i)) {
				final int[] row = data[i];
				for (int column = 0; column < row.length; column++) {
					cardinalities[column][row[column]][0]++;
				}
			}
		}

        // Create counts for other levels
        for (int column = 0; column < hierarchies.length; column++) {
            final int[][] hierarchy = hierarchies[column];
            for (int in = 0; in < hierarchy.length; in++) {
                final int cardinality = cardinalities[column][in][0];
                for (int level = 1; level < hierarchy[in].length; level++) {
                    final int out = hierarchy[in][level];
                    cardinalities[column][out][level] += cardinality;
                }
            }
        }

        // Create a cache for the results
        cache = new double[hierarchies.length][];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new double[ahierarchies[i].getArray()[0].length];
            Arrays.fill(cache[i], NA);
        }
    }
}
