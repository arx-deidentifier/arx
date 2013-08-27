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

package org.deidentifier.arx;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements a representation of the generalization lattice that is
 * exposed to users of the API
 * 
 * @author Prasser, Kohlmayer
 */
public class ARXLattice implements Serializable {

    /**
     * The internal accessor class
     * 
     * @author Prasser, Kohlmayer
     */
    public class Access implements Serializable {

        private static final long  serialVersionUID = 6654627605797832468L;

        private final ARXLattice lattice;

        public Access(final ARXLattice lattice) {
            this.lattice = lattice;
        }

        public Map<String, Integer> getAttributeMap() {
            return bottom.headermap;
        }

        public void setBottom(final ARXNode bottom) {
            lattice.bottom = bottom;
        }

        public void setLevels(final ARXNode[][] levels) {
            lattice.levels = levels;
        }

        public void setMaxAbsoluteOutliers(final int maxAbsoluteOutliers) {
            lattice.maxAbsoluteOutliers = maxAbsoluteOutliers;
        }

        public void setMetric(final Metric<?> metric) {
            lattice.metric = metric;
        }

        public void setOptimum(final ARXNode node) {
            lattice.optimum = node;
        }

        public void
                setUncertainty(final boolean uncertainty) {
            lattice.uncertainty = uncertainty;
        }

        public void
                setMonotonicSubcriterion(final boolean containsMonotonicSubcriterion) {
            lattice.containsMonotonicSubcriterion = containsMonotonicSubcriterion;
        }

        public void setSize(final int size) {
            lattice.size = size;
        }

        public void setTop(final ARXNode top) {
            lattice.top = top;
        }
    }

    public static enum Anonymity {
        ANONYMOUS,
        NOT_ANONYMOUS,
        PROBABLY_ANONYMOUS,
        PROBABLY_NOT_ANONYMOUS
    }

    /**
     * A node in the lattice
     * 
     * @author Prasser, Kohlmayer
     */
    public class ARXNode {

        /**
         * Internal access class
         * 
         * @author Prasser, Kohlmayer
         */
        public class Access {

            private final ARXNode node;

            public Access(final ARXNode node) {
                this.node = node;
            }

            /**
             * Sets the anonymity
             * 
             * @param anonymity
             */
            public void setAnonymity(final Anonymity anonymity) {
                node.anonymity = anonymity;
            }

            /**
             * Set anonymous
             */
            public void setAnonymous() {
                node.anonymity = Anonymity.ANONYMOUS;
            }

            /**
             * Sets the attributes
             * 
             * @param attributes
             */
            public void setAttributes(final Map<Integer, Object> attributes) {
                node.attributes = attributes;
            }

            /**
             * Set checked
             * 
             * @param checked
             */
            public void setChecked(final boolean checked) {
                node.checked = checked;
            }

            /**
             * Sets the headermap
             * 
             * @param headermap
             */
            public void setHeadermap(final Map<String, Integer> headermap) {
                node.headermap = headermap;
            }

            /**
             * Sets the maximal information loss
             * 
             * @return
             */
            public void setMaximumInformationLoss(final InformationLoss a) {
                node.maxInformationLoss = a;
            }

            /**
             * Sets the minimal information loss
             * 
             * @return
             */
            public void setMinimumInformationLoss(final InformationLoss a) {
                node.minInformationLoss = a;
            }

            /**
             * Set not anonymous
             */
            public void setNotAnonymous() {
                node.anonymity = Anonymity.NOT_ANONYMOUS;
            }

            /**
             * Sets the predecessors
             * 
             * @param predecessors
             */
            public void setPredecessors(final ARXNode[] predecessors) {
                node.predecessors = predecessors;
            }

            /**
             * Sets the successors
             * 
             * @param successors
             */
            public void setSuccessors(final ARXNode[] successors) {
                node.successors = successors;
            }

            /**
             * Sets the transformation
             * 
             * @param transformation
             */
            public void setTransformation(final int[] transformation) {
                node.transformation = transformation;
            }
        }

        /** The transformation */
        private int[]                transformation;
        
        /** Is it anonymous */
        private Anonymity            anonymity;

        /** The min information loss */
        private InformationLoss      minInformationLoss;

        /** The max information loss */
        private InformationLoss      maxInformationLoss;

        /** Attributes */
        private Map<Integer, Object> attributes = new HashMap<Integer, Object>();

        /** The sucessors */
        private ARXNode[]            successors;

        /** The predecessors */
        private ARXNode[]            predecessors;

        /** The headermap */
        private Map<String, Integer> headermap;

        /** Has the node been checked */
        private boolean              checked;

        /** The access */
        private final Access         access     = new Access(this);

        /**
         * Constructor
         * 
         * @param node
         * @param headermap
         */
        private ARXNode(final Node node, final Map<String, Integer> headermap) {
            this.headermap = headermap;
            transformation = node.getTransformation();
            if (node.isAnonymous()) {
                if (uncertainty && !node.isChecked()) {
                    anonymity = Anonymity.PROBABLY_ANONYMOUS;
                } else  {
                    anonymity = Anonymity.ANONYMOUS;
                }
            } else {
                if (uncertainty && !node.isChecked()) {
                    if (containsMonotonicSubcriterion && !node.isKAnonymous()){
                        anonymity = Anonymity.NOT_ANONYMOUS;
                    } else {
                        anonymity = Anonymity.PROBABLY_NOT_ANONYMOUS;
                    }
                } else  {
                    anonymity = Anonymity.NOT_ANONYMOUS;
                }
            }
            minInformationLoss = node.getInformationLoss();
            maxInformationLoss = node.getInformationLoss();
            checked = node.isChecked();
        }

        /**
         * Alter associated fields
         * 
         * @return
         */
        public Access access() {
            return access;
        }

        /**
         * Returns the attributes
         * 
         * @return
         */
        public Map<Integer, Object> getAttributes() {
            return attributes;
        }

        /**
         * Returns the index of an attribute
         * 
         * @param attr
         * @return
         */
        public int getDimension(final String attr) {
            return headermap.get(attr);
        }

        /**
         * Returns the generalization for the attribute
         * 
         * @param attribute
         * @return
         */
        public int getGeneralization(final String attribute) {
            final Integer index = headermap.get(attribute);
            if (index == null) { return 0; }
            return transformation[index];
        }

        /**
         * Returns the maximal information loss
         * 
         * @return
         */
        public InformationLoss getMaximumInformationLoss() {
            return maxInformationLoss;
        }

        /**
         * Returns the minimal information loss
         * 
         * @return
         */
        public InformationLoss getMinimumInformationLoss() {
            return minInformationLoss;
        }

        /**
         * The predecessors
         * 
         * @return
         */
        public ARXNode[] getPredecessors() {
            return predecessors;
        }

        /**
         * Returns the quasi identifiers
         * 
         * @return
         */
        public String[] getQuasiIdentifyingAttributes() {
            final String[] result = new String[headermap.size()];
            for (final String key : headermap.keySet()) {
                result[headermap.get(key)] = key;
            }
            return result;
        }

        /**
         * The successors
         * 
         * @return
         */
        public ARXNode[] getSuccessors() {
            return successors;
        }

        /**
         * Returns the transformation as an array
         * 
         * @return
         */
        public int[] getTransformation() {
            return transformation;
        }

        /**
         * Is it anonymous?
         * 
         * @return
         */
        public Anonymity isAnonymous() {
            return anonymity;
        }

        /**
         * Returns if the node has been checked explicitly
         * 
         * @return
         */
        public boolean isChecked() {
            return checked;
        }
    }

    class IntArrayWrapper {

        private final int[] array;
        private final int   hashCode;

        public IntArrayWrapper(final int[] array) {
            this.array = array;
            hashCode = Arrays.hashCode(array);
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) { return true; }
            return Arrays.equals(array, ((IntArrayWrapper) obj).array);
        }

        public final int[] getArray() {
            return array;
        }

        @Override
        public final int hashCode() {
            return hashCode;
        }

        @Override
        public final String toString() {
            return Arrays.toString(array);
        }

    }

    private static final long     serialVersionUID = -8790104959905019184L;

    /** The levels in the lattice */
    private transient ARXNode[][] levels;

    /** The number of nodes */
    private int                   size;

    /** The top node */
    private transient ARXNode     top;

    /** The bottom node */
    private transient ARXNode     bottom;

    /** Are anonymity properties potentially uncertain */
    private boolean               uncertainty;
    
    /** Is there a monotonic sub-criterion*/
    private boolean               containsMonotonicSubcriterion;

    /** Metric */
    private Metric<?>             metric;

    /** The current max absolute outliers */
    private int                   maxAbsoluteOutliers;

    /** The accessor */
    private final Access          access           = new Access(this);

    /** The optimum */
    private transient ARXNode     optimum;

    /**
     * Constructor
     * @param lattice The lattice to represent
     * @param header The header
     * @param config The config
     */
    ARXLattice(final Lattice lattice,
                 final String[] header,
                 final ARXConfiguration config) {

        this.maxAbsoluteOutliers = config.getAbsoluteMaxOutliers();
        this.metric = config.getMetric();
        this.uncertainty = (config.isPracticalMonotonicity()) || 
                           (config.getMetric().isMonotonic() && !config.isCriterionMonotonic());
        this.containsMonotonicSubcriterion = config.getMinimalGroupSize() != Integer.MAX_VALUE;
        
        // Build header map
        final Map<String, Integer> headermap = new HashMap<String, Integer>();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            headermap.put(header[i], index++);
        }

        // Create nodes
        final Map<Node, ARXNode> map = new HashMap<Node, ARXNode>();
        size = lattice.getSize();
        levels = new ARXNode[lattice.getLevels().length][];
        for (int i = 0; i < lattice.getLevels().length; i++) {
            final Node[] level = lattice.getLevels()[i];
            levels[i] = new ARXNode[level.length];
            for (int j = 0; j < level.length; j++) {
                final ARXNode node = new ARXNode(level[j], headermap);
                if (level[j] == metric.getGlobalOptimum()) {
                    optimum = node;
                }
                levels[i][j] = node;
                map.put(level[j], node);
            }
        }
        
        // Create relationships
        for (final Node[] level : lattice.getLevels()) {
            for (final Node node : level) {
                final ARXNode fnode = map.get(node);
                fnode.successors = new ARXNode[node.getSuccessors().length];
                fnode.predecessors = new ARXNode[node.getPredecessors().length];
                for (int i = 0; i < node.getSuccessors().length; i++) {
                    fnode.successors[i] = map.get(node.getSuccessors()[i]);
                }
                for (int i = 0; i < node.getPredecessors().length; i++) {
                    fnode.predecessors[i] = map.get(node.getPredecessors()[i]);
                }
            }
        }

        // find bottom node
        outer: for (int i = 0; i < levels.length; i++) {
            final ARXNode[] level = levels[i];
            for (int j = 0; j < level.length; j++) {
                final ARXNode node = level[j];
                if (node != null) {
                    bottom = node;
                    break outer;
                }
            }
        }

        // find top node
        outer: for (int i = levels.length - 1; i >= 0; i--) {
            final ARXNode[] level = levels[i];
            for (int j = 0; j < level.length; j++) {
                final ARXNode node = level[j];
                if (node != null) {
                    top = node;
                    break outer;
                }
            }
        }

        // Estimate information loss of all nodes
        estimateInformationLoss();
    }

    /**
     * Access fields of this class
     * 
     * @return
     */
    public Access access() {
        return access;
    }

    /**
     * This method triggers the estimation of the information loss of all nodes
     * in the lattice regardless of whether they have been checked for anonymity
     * or not
     */
    protected void estimateInformationLoss() {

        // only estimate info loss in case of monotonic metrics
        // or no suppression
        if (metric.isMonotonic() || (maxAbsoluteOutliers == 0)) {
            estimateMinLoss();
            estimateMaxLoss();
        } else {

            // TODO: Fix this! We currently assume monotonicity for all metrics!
            System.out.println("[WARNING] Assuming monotonicity for non-monotonic metric!");
            estimateMinLoss();
            estimateMaxLoss();
        }
    }

    /**
     * Estimates maximal information loss
     */
    private void estimateMaxLoss() {

        // Estimate with max if not known
        if (top.getMaximumInformationLoss() == null) {
            top.access().setMaximumInformationLoss(metric.max());
        }

        // Push
        for (int i = levels.length - 1; i >= 0; i--) {
            final ARXNode[] level = levels[i];
            for (final ARXNode node : level) {
                final InformationLoss a = node.getMaximumInformationLoss();
                for (final ARXNode n : node.getPredecessors()) {
                    if (n.getMaximumInformationLoss() == null) {
                        n.access().setMaximumInformationLoss(metric.max());
                    }
                    // If we dont know the value for sure
                    if (n.getMinimumInformationLoss() != n.getMaximumInformationLoss()) {
                        n.getMaximumInformationLoss().min(a);
                    }
                }
            }
        }
    }

    /**
     * Estimates minimal information loss
     */
    private void estimateMinLoss() {

        // Estimate with zero if not known
        // TODO: Not correct for DM*
        if (bottom.getMinimumInformationLoss() == null) {
            bottom.access().setMinimumInformationLoss(metric.min());
        }

        // Push
        for (int i = 0; i < levels.length; i++) {
            final ARXNode[] level = levels[i];
            for (final ARXNode node : level) {
                final InformationLoss a = node.getMinimumInformationLoss();
                for (final ARXNode n : node.getSuccessors()) {
                    if (n.getMinimumInformationLoss() == null) {
                        n.access().setMinimumInformationLoss(metric.min());
                    }
                    // If we dont know the value for sure
                    if (n.getMinimumInformationLoss() != n.getMaximumInformationLoss()) {
                        n.getMinimumInformationLoss().max(a);
                    }
                }
            }
        }
    }

    /**
     * Returns the bottom node
     * 
     * @return
     */
    public ARXNode getBottom() {
        return bottom;
    }

    /**
     * Returns the levels of the generalization lattice
     * 
     * @return
     */
    public ARXNode[][] getLevels() {
        return levels;
    }

    protected ARXNode getOptimum() {
        return optimum;
    }

    /**
     * Returns the number of nodes
     * 
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the top node
     * 
     * @return
     */
    public ARXNode getTop() {
        return top;
    }
}
