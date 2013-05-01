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

package org.deidentifier.flash.gui.view.impl.explore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deidentifier.flash.FLASHLattice.Anonymity;
import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.FLASHResult;

public class NodeFilter implements Serializable {

    private static final long    serialVersionUID   = 5451641489562102719L;

    private Set<Integer>[]       generalizations    = null;
    private final Set<Anonymity> anonymity          = new HashSet<Anonymity>();
    private double               minInformationLoss = 0;
    private double               maxInformationLoss = Double.MAX_VALUE;
    private int[]                maxLevels          = null;
    private int                  maxNumNodesInitial = 0;

    @SuppressWarnings("unchecked")
    public NodeFilter(final int[] maxLevels, final int maxNumNodesInitial) {
        this.maxNumNodesInitial = maxNumNodesInitial;
        generalizations = new Set[maxLevels.length];
        for (int i = 0; i < generalizations.length; i++) {
            generalizations[i] = new HashSet<Integer>();
        }
        this.maxLevels = maxLevels;
    }

    public void allowAll() {
        // TODO: Handle unknown
        anonymity.add(Anonymity.ANONYMOUS);
        anonymity.add(Anonymity.NOT_ANONYMOUS);
        anonymity.add(Anonymity.UNKNOWN);
        minInformationLoss = Double.NEGATIVE_INFINITY;
        maxInformationLoss = Double.MAX_VALUE;
        for (int i = 0; i < maxLevels.length; i++) {
            for (int j = 0; j < maxLevels[i]; j++) {
                generalizations[i].add(j);
            }
        }
    }

    public void allowAllInformationLoss() {
        minInformationLoss = 0;
        maxInformationLoss = Double.MAX_VALUE;
    }

    public void allowAnonymous() {
        anonymity.add(Anonymity.ANONYMOUS);
    }

    public void allowGeneralization(final int dimension, final int level) {
        generalizations[dimension].add(level);
    }

    public void allowInformationLoss(final double min, final double max) {
        minInformationLoss = min;
        maxInformationLoss = max;
    }

    public void allowNonAnonymous() {
        anonymity.add(Anonymity.NOT_ANONYMOUS);
    }

    public void allowUnknown() {
        anonymity.add(Anonymity.UNKNOWN);
    }

    /**
     * Cleans up the settings
     * 
     * @param lattice
     * @return
     */
    private void clean(final Set<FLASHNode> visible, final int[] optimum) {

        // Remove hidden from visible
        final Iterator<FLASHNode> i = visible.iterator();
        while (i.hasNext()) {
            final FLASHNode node = i.next();
            if (!isAllowed(node)) {
                i.remove();
            }
        }

        // Build sets
        final Set[] required = new HashSet[optimum.length];
        for (int j = 0; j < optimum.length; j++) {
            required[j] = new HashSet<Integer>();
        }
        for (final FLASHNode node : visible) {
            for (int j = 0; j < optimum.length; j++) {
                required[j].add(node.getTransformation()[j]);
            }
        }

        // Clean the settings
        for (int j = 0; j < optimum.length; j++) {
            final Iterator<Integer> it = generalizations[j].iterator();
            while (it.hasNext()) {
                final int l = it.next();
                if (!required[j].contains(l)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Counts the number of visible nodes
     * 
     * @param lattice
     * @return
     */
    private int
            count(final Set<FLASHNode> visible, final Set<FLASHNode> hidden) {
        final Iterator<FLASHNode> i = hidden.iterator();
        while (i.hasNext()) {
            final FLASHNode node = i.next();
            if (isAllowed(node)) {
                i.remove();
                visible.add(node);
            }
        }
        return visible.size();
    }

    public void disallowAll() {
        anonymity.clear();
        minInformationLoss = Double.MAX_VALUE;
        maxInformationLoss = 0;
        for (int i = 0; i < maxLevels.length; i++) {
            generalizations[i].clear();
        }
    }

    public void disallowAnonymous() {
        anonymity.remove(Anonymity.ANONYMOUS);
    }

    public void disallowGeneralization(final int dimension, final int level) {
        generalizations[dimension].remove(level);
    }

    public void disallowNonAnonymous() {
        anonymity.remove(Anonymity.NOT_ANONYMOUS);
    }

    public void disallowUnknown() {
        anonymity.remove(Anonymity.UNKNOWN);
    }

    /**
     * @return the anonymity
     */
    public Set<Anonymity> getAllowedAnonymity() {
        return anonymity;
    }

    /**
     * @return the generalizations
     */
    public Set<Integer> getAllowedGeneralizations(final int dimension) {
        return generalizations[dimension];
    }

    /**
     * @return the maxInformationLoss
     */
    public double getAllowedMaxInformationLoss() {
        return maxInformationLoss;
    }

    /**
     * @return the minInformationLoss
     */
    public double getAllowedMinInformationLoss() {
        return minInformationLoss;
    }

    /**
     * Creates a node filter for the given result
     * 
     * @param result
     */
    public void initialize(final FLASHResult result) {
        disallowAll();
        if (result.isResultAvailable()) {

            // Allow specializations and generalizations of optimum
            allowAnonymous();
            final double max = result.getLattice()
                                     .getTop()
                                     .getMaximumInformationLoss()
                                     .getValue();
            final double min = result.getLattice()
                                     .getBottom()
                                     .getMinimumInformationLoss()
                                     .getValue();
            allowInformationLoss(min, max);
            final int[] optimum = result.getGlobalOptimum().getTransformation();
            for (int i = 0; i < optimum.length; i++) {
                allowGeneralization(i, optimum[i]);
            }

            // Build sets of visible and hidden nodes
            final Set<FLASHNode> visible = new HashSet<FLASHNode>();
            final Set<FLASHNode> hidden = new HashSet<FLASHNode>();
            visible.add(result.getGlobalOptimum());
            for (final FLASHNode[] level : result.getLattice().getLevels()) {
                for (final FLASHNode node : level) {
                    if (node.isAnonymous() == Anonymity.ANONYMOUS) {
                        if (!node.equals(result.getGlobalOptimum())) {
                            hidden.add(node);
                        }
                    }
                }
            }

            // Determine max generalization
            int maxgen = 0;
            for (int i = 0; i < optimum.length; i++) {
                maxgen = Math.max(result.getLattice()
                                        .getTop()
                                        .getTransformation()[i], maxgen);
            }

            // Show less generalized nodes
            for (int j = 1; j <= maxgen; j++) {
                for (int i = 0; i < optimum.length; i++) {
                    final int gen = optimum[i] - j;
                    if (gen >= 0) {
                        allowGeneralization(i, gen);
                        final int current = count(visible, hidden);
                        if (current > maxNumNodesInitial) {
                            disallowGeneralization(i, gen);
                            return;
                        }
                    }
                }
            }

            // Show more generalized nodes
            for (int j = 1; j <= maxgen; j++) {
                for (int i = 0; i < optimum.length; i++) {
                    final int gen = optimum[i] + j;
                    if (gen <= result.getLattice().getTop().getTransformation()[i]) {
                        allowGeneralization(i, gen);
                        final int current = count(visible, hidden);
                        if (current > maxNumNodesInitial) {
                            disallowGeneralization(i, gen);
                            return;
                        }
                    }
                }
            }

            // Clean up
            clean(visible, optimum);
        } else {

            // Allow generalizations of bottom
            allowNonAnonymous();
            final double max = result.getLattice()
                                     .getTop()
                                     .getMaximumInformationLoss()
                                     .getValue();
            final double min = result.getLattice()
                                     .getBottom()
                                     .getMinimumInformationLoss()
                                     .getValue();
            allowInformationLoss(min, max);
            final int[] base = result.getLattice()
                                     .getBottom()
                                     .getTransformation();
            for (int i = 0; i < base.length; i++) {
                allowGeneralization(i, base[i]);
            }

            // Build sets of visible and hidden nodes
            final Set<FLASHNode> visible = new HashSet<FLASHNode>();
            final Set<FLASHNode> hidden = new HashSet<FLASHNode>();
            visible.add(result.getLattice().getBottom());
            for (final FLASHNode[] level : result.getLattice().getLevels()) {
                for (final FLASHNode node : level) {
                    if (node.isAnonymous() == Anonymity.NOT_ANONYMOUS) {
                        if (!node.equals(result.getLattice().getBottom())) {
                            hidden.add(node);
                        }
                    }
                }
            }

            // Determine max generalization
            int maxgen = 0;
            for (int i = 0; i < base.length; i++) {
                maxgen = Math.max(result.getLattice()
                                        .getTop()
                                        .getTransformation()[i], maxgen);
            }

            // Show more generalized nodes
            for (int j = 1; j <= maxgen; j++) {
                for (int i = 0; i < base.length; i++) {
                    final int gen = base[i] + j;
                    if (gen <= result.getLattice().getTop().getTransformation()[i]) {
                        allowGeneralization(i, gen);
                        final int current = count(visible, hidden);
                        if (current > maxNumNodesInitial) {
                            disallowGeneralization(i, gen);
                            return;
                        }
                    }
                }
            }

            // Clean up
            clean(visible, base);
        }
    }

    public boolean isAllowed(final FLASHNode node) {
        if (node.getMaximumInformationLoss().getValue() < minInformationLoss) {
            return false;
        } else if (node.getMinimumInformationLoss().getValue() > maxInformationLoss) {
            return false;
        } else if (!anonymity.contains(node.isAnonymous())) { return false; }
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            if (!generalizations[i].contains(transformation[i])) { return false; }
        }
        return true;
    }

    public boolean isAllowedAnonymous() {
        return anonymity.contains(Anonymity.ANONYMOUS);
    }

    /**
     * Returns whether the given generalization is allowed
     * 
     * @param dimension
     * @param level
     * @return
     */
    public boolean
            isAllowedGeneralization(final int dimension, final int level) {
        return generalizations[dimension].contains(level);
    }

    public boolean isAllowedNonAnonymous() {
        return anonymity.contains(Anonymity.NOT_ANONYMOUS);
    }

    public boolean isAllowedUnknown() {
        return anonymity.contains(Anonymity.UNKNOWN);
    }
}
