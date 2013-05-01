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

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.flash.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * This class provides an efficient implementation of a non-monotonic and
 * non-uniform entropy metric. It avoids a cell-by-cell process by utilizing a
 * three-dimensional array that maps identifiers to their frequency for all
 * quasi-identifiers and generalization levels. It further reduces the overhead
 * induced by subsequent calls by caching the results for previous columns and
 * generalization levels. It takes supressed tuples into account by adding the
 * information loss induced by suppressing the transformed representation of the
 * outliers.
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricNMEntropy extends MetricEntropy {

    /**
     * 
     */
    private static final long serialVersionUID = 5789738609326541247L;

    public MetricNMEntropy() {
        super(false, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public InformationLossDefault evaluateInternal(final Node node,
                                                   final IHashGroupify g) {

        // Obtain "standard" value
        final double originalInfoLoss = super.evaluateInternal(node, g)
                                             .getValue();

        // Compute loss induced by suppression
        // Init TODO: Use lightweight alternative to Map<Integer, Integer>();
        double suppressedTuples = 0;
        double additionalInfoLoss = 0;
        int key;
        Integer val;
        final Map<Integer, Integer>[] original = new Map[node.getTransformation().length];
        for (int i = 0; i < original.length; i++) {
            original[i] = new HashMap<Integer, Integer>();
        }

        // Compute counts for suppressed values in each column
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (!m.isNotOutlier) {
                suppressedTuples += m.count;
                for (int i = 0; i < original.length; i++) {
                    key = m.key[i];
                    val = original[i].get(key);
                    if (val == null) {
                        original[i].put(key, m.count);
                    } else {
                        original[i].put(key, m.count + val);
                    }
                }
            }
            m = m.nextOrdered;
        }

        // Evaluate entropy for suppressed tuples
        for (int i = 0; i < original.length; i++) {
            for (final double count : original[i].values()) {
                additionalInfoLoss += count *
                                      MetricEntropy.log2(count /
                                                         suppressedTuples);
            }
        }

        // Return sum of both values
        return new InformationLossDefault(originalInfoLoss - additionalInfoLoss);
    }

    @Override
    public void
            initializeInternal(final Data input,
                               final GeneralizationHierarchy[] ahierarchies) {
        super.initializeInternal(input, ahierarchies);
    }
}
