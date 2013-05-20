/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The t-closeness criterion with hierarchical-distance EMD
 * @author Prasser, Kohlmayer
 */
public class HierarchicalDistanceTCloseness extends TCloseness {

    private static final long serialVersionUID = -2142590190479670706L;
    
    /** The hierarchy used for the EMD*/
    private final Hierarchy hierarchy;
    /** Internal tree*/
    private int[]        tree;
    /** Internal offset*/
    private int start;
    /** Internal empty tree*/
    private int[] empty;

    /**
     * Creates a new instance
     * @param t
     * @param h
     */
    public HierarchicalDistanceTCloseness(double t, Hierarchy h) {
        super(t);
        this.hierarchy = h;
    }

    @Override
    public void initialize(DataManager manager) {
        this.tree = manager.getTree();
        this.start = this.tree[1] + 3;
        this.empty = new int[this.tree[1]];
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        
        // Empty data in tree
        System.arraycopy(empty, 0, tree, start, empty.length);

        // init parameters
        final int totalElementsP = tree[0];
        final int numLeafs = tree[1];
        final double height = tree[2]; // cast to double as it is used in double
                                       // calculations
        final int extraStartPos = numLeafs + 3;
        final int extraEndPos = extraStartPos + numLeafs;

        // Copy and count
        int totalElementsQ = 0;
        int[] buckets = entry.distribution.getBuckets();
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                final int value = buckets[i];
                final int frequency = buckets[i + 1];
                tree[value + extraStartPos] = frequency;
                totalElementsQ += frequency;
            }
        }
        // Tree data format: #p_count, #leafs, height, freqLeaf_1, ...,
        // freqLeaf_n, extra_1,..., extra_n, [#childs, level, child_1, ...
        // child_x, pos_e, neg_e], ...
        double cost = 0;

        // leafs
        for (int i = extraStartPos; i < extraEndPos; i++) {
            tree[i] = (tree[i - numLeafs] * totalElementsQ) -
                      (tree[i] * totalElementsP); // p_i - q_i
        }

        // innerNodes
        for (int i = extraEndPos; i < tree.length; i++) {
            int pos_e = 0;
            int neg_e = 0;

            final int numChilds = tree[i++];
            final int level = tree[i++];

            // iterate over all children
            for (int j = 0; j < numChilds; j++) {
                // differentiate between first level and rest

                int extra = 0;
                if (level == 1) {
                    extra = tree[tree[i + j]];
                } else {
                    final int extra_child_index = tree[i + j] +
                                                  tree[tree[i + j]] + 2; // pointer
                                                                         // to
                                                                         // the
                                                                         // pos_e
                                                                         // of
                                                                         // node
                    final int pos_child = tree[extra_child_index];
                    final int neg_child = tree[extra_child_index + 1];
                    extra = pos_child - neg_child;
                }

                if (extra > 0) { // positive
                    pos_e += extra;
                } else { // negative
                    neg_e += (-extra);
                }
            }

            // save extras
            i += numChilds; // increment pointer to extra
            tree[i++] = pos_e;
            tree[i] = neg_e;

            // sum
            final double cost_n = (level / height) * Math.min(pos_e, neg_e);
            cost += cost_n;
        }

        cost /= ((double) totalElementsP * (double) totalElementsQ);

        // check
        return cost <= t;
    }

    /**
     * Returns the hierarchy backing the EMD calculations
     * @return
     */
    public Hierarchy getHierarchy() {
        return hierarchy;
    }
}
