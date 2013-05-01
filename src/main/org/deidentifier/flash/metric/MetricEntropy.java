/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.metric;

import java.util.Arrays;

import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.Dictionary;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * This class provides an efficient implementation of the non-uniform entropy
 * metric. It avoids a cell-by-cell process by utilizing a three-dimensional
 * array that maps identifiers to their frequency for all quasi-identifiers and
 * generalization levels. It further reduces the overhead induced by subsequent
 * calls by caching the results for previous columns and generalization levels.
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricEntropy extends MetricDefault {

    /**
     * 
     */
    private static final long   serialVersionUID = -8618697919821588987L;

    /** Log 2 */
    static final double         log2             = Math.log(2);

    /** Column -> Id -> Level -> Count */
    private int[][][]           cardinalities;

    /** Column -> Id -> Level -> Output */
    private int[][][]           hierarchies;

    /** Column -> Level -> Value */
    private double[][]          cache;

    /** Value unknown */
    private static final double NA               = Double.POSITIVE_INFINITY;

    /**
     * Computes log 2
     * 
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / log2;
    }

    public MetricEntropy() {
        super(true, true);
    }

    public MetricEntropy(final boolean monotonic, final boolean independent) {
        super(monotonic, independent);
    }

    @Override
    public InformationLossDefault evaluateInternal(final Node node,
                                                   final IHashGroupify g) {

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
                    value += a * log2(a / b);
                }
                cache[column][state] = value;
            }
            result += value;
        }
        return new InformationLossDefault(-result);
    }

    @Override
    public void
            initializeInternal(final Data input,
                               final GeneralizationHierarchy[] ahierarchies) {

        // Obtain dictionary
        final Dictionary dictionary = input.getDictionary();

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
            final int[] row = data[i];
            for (int column = 0; column < row.length; column++) {
                cardinalities[column][row[column]][0]++;
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
