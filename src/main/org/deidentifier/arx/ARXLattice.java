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

package org.deidentifier.arx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;

/**
 * This class implements a representation of the generalization lattice that is
 * exposed to users of the API.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ARXLattice implements Serializable {

    /**
     * The internal accessor class.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public class Access implements Serializable {

        /**  SVUID */
        private static final long serialVersionUID = 6654627605797832468L;

        /**  Lattice */
        private final ARXLattice  lattice;

        /**
         * Constructor
         *
         * @param lattice
         */
        public Access(final ARXLattice lattice) {
            this.lattice = lattice;
        }

        /**
         * 
         *
         * @return
         */
        public Map<String, Integer> getAttributeMap() {
            return bottom.headermap;
        }

        /**
         * 
         *
         * @param bottom
         */
        public void setBottom(final ARXNode bottom) {
            lattice.bottom = bottom;
        }

        /**
         * 
         *
         * @param levels
         */
        public void setLevels(final ARXNode[][] levels) {
            lattice.levels = levels;
        }

        /**
         * 
         *
         * @param metric
         */
        public void setMetric(final Metric<?> metric) {
            lattice.metric = metric;
        }

        /**
         * 
         *
         * @param config
         */
        public void setMonotonicity(ARXConfiguration config) {
            lattice.monotonicNonAnonymous = lattice.metric.isMonotonic() || !config.isSuppressionAlwaysEnabled();
            lattice.monotonicAnonymous = lattice.metric.isMonotonic() || config.getAbsoluteMaxOutliers() == 0;
        }

        /**
         * 
         *
         * @param node
         */
        public void setOptimum(final ARXNode node) {
            lattice.optimum = node;
        }

        /**
         * 
         *
         * @param size
         */
        public void setSize(final int size) {
            lattice.size = size;
        }

        /**
         * 
         *
         * @param top
         */
        public void setTop(final ARXNode top) {
            lattice.top = top;
        }
        
        /**
         * 
         *
         * @param uncertainty
         */
        public void setUncertainty(final boolean uncertainty) {
            lattice.uncertainty = uncertainty;
        }
    }

    /**
     * 
     */
    public static enum Anonymity {
        
        /**  ANONYMOUS */
        ANONYMOUS,
        
        /**  NOT_ANONYMOUS */
        NOT_ANONYMOUS,
        
        /**  UNKNOWN */
        UNKNOWN,
        
        /**  PROBABLY_ANONYMOUS */
        PROBABLY_ANONYMOUS,
        
        /**  PROBABLY_NOT_ANONYMOUS */
        PROBABLY_NOT_ANONYMOUS
    }

    /**
     * A node in the lattice.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public class ARXNode {
        
        /**
         * Internal access class.
         *
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        public class Access {

            /**  Node */
            private final ARXNode node;

            /**
             * 
             *
             * @param node
             */
            public Access(final ARXNode node) {
                this.node = node;
            }

            /**
             * Sets the anonymity.
             *
             * @param anonymity
             */
            public void setAnonymity(final Anonymity anonymity) {
                node.anonymity = anonymity;
            }

            /**
             * Set anonymous.
             */
            public void setAnonymous() {
                node.anonymity = Anonymity.ANONYMOUS;
            }

            /**
             * Sets the attributes.
             *
             * @param attributes
             */
            public void setAttributes(final Map<Integer, Object> attributes) {
                node.attributes = attributes;
            }

            /**
             * Set checked.
             *
             * @param checked
             */
            public void setChecked(final boolean checked) {
                node.checked = checked;
            }

            /**
             * Sets the headermap.
             *
             * @param headermap
             */
            public void setHeadermap(final Map<String, Integer> headermap) {
                node.headermap = headermap;
            }

            /**
             * Sets the lower bound.
             *
             * @param a
             */
            public void setLowerBound(final InformationLoss<?> a) {
                node.lowerBound = InformationLoss.createInformationLoss(a, metric, getDeserializationContext().minLevel, getDeserializationContext().maxLevel);
            }

            /**
             * Sets the maximal information loss.
             *
             * @param a
             */
            public void setMaximumInformationLoss(final InformationLoss<?> a) {
                node.maxInformationLoss = InformationLoss.createInformationLoss(a, metric, getDeserializationContext().minLevel, getDeserializationContext().maxLevel);
            }

            /**
             * Sets the minimal information loss.
             *
             * @param a
             */
            public void setMinimumInformationLoss(final InformationLoss<?> a) {
                node.minInformationLoss = InformationLoss.createInformationLoss(a, metric, getDeserializationContext().minLevel, getDeserializationContext().maxLevel);
            }

            /**
             * Set not anonymous.
             */
            public void setNotAnonymous() {
                node.anonymity = Anonymity.NOT_ANONYMOUS;
            }

            /**
             * Sets the predecessors.
             *
             * @param predecessors
             */
            public void setPredecessors(final ARXNode[] predecessors) {
                node.predecessors = predecessors;
            }

            /**
             * Sets the successors.
             *
             * @param successors
             */
            public void setSuccessors(final ARXNode[] successors) {
                node.successors = successors;
            }

            /**
             * Sets the transformation.
             *
             * @param transformation
             */
            public void setTransformation(final int[] transformation) {
                node.transformation = transformation;
            }
        }

        /** Id. */
        private Integer id = null;

        /** The access. */
        private final Access         access     = new Access(this);

        /** Is it anonymous. */
        private Anonymity            anonymity;

        /** Attributes. */
        private Map<Integer, Object> attributes = new HashMap<Integer, Object>();

        /** Has the node been checked. */
        private boolean              checked;

        /** The header map. */
        private Map<String, Integer> headermap;

        /** The lower bound. */
        private InformationLoss<?>   lowerBound;
        
        /** The max information loss. */
        private InformationLoss<?>   maxInformationLoss;

        /** The min information loss. */
        private InformationLoss<?>   minInformationLoss;

        /** The predecessors. */
        private ARXNode[]            predecessors;

        /** The successors. */
        private ARXNode[]            successors;

        /** The transformation. */
        private int[]                transformation;

        /**
         * Internal constructor for deserialization.
         */
        public ARXNode() {
            // Empty by design
        }

        /**
         * Constructor.
         *
         * @param solutions
         * @param node
         * @param headermap
         */
        private ARXNode(final SolutionSpace solutions, 
                        final Transformation node, 
                        final Map<String, Integer> headermap) {
            
            // Set properties
            this.headermap = headermap;
            this.transformation = node.getGeneralization();
            this.minInformationLoss = node.getInformationLoss();
            this.maxInformationLoss = node.getInformationLoss();
            this.lowerBound = node.getLowerBound();
            this.checked = node.hasProperty(solutions.getPropertyChecked());
            
            // Transfer anonymity property without uncertainty
            if (node.hasProperty(solutions.getPropertyChecked())){
                if (node.hasProperty(solutions.getPropertyAnonymous())) {
                    this.anonymity = Anonymity.ANONYMOUS;
                } else if(node.hasProperty(solutions.getPropertyNotAnonymous())) {
                    this.anonymity = Anonymity.NOT_ANONYMOUS;
                } else {
                    throw new IllegalStateException("Missing node information");
                }
            // This is a node for which the property is unknown
            } else {
                if (node.hasProperty(solutions.getPropertyAnonymous())) {
                    this.anonymity = uncertainty ? Anonymity.PROBABLY_ANONYMOUS : Anonymity.ANONYMOUS;
                } else if (node.hasProperty(solutions.getPropertyNotAnonymous())) {
                    this.anonymity = uncertainty ? Anonymity.PROBABLY_NOT_ANONYMOUS : Anonymity.NOT_ANONYMOUS;
                } else if (node.hasProperty(solutions.getPropertyNotKAnonymous())) {
                    this.anonymity = Anonymity.NOT_ANONYMOUS;
                } else if (node.hasProperty(solutions.getPropertyInsufficientUtility())) {
                    this.anonymity = Anonymity.UNKNOWN;
                } else {
                    throw new IllegalStateException("Missing node information");
                }
            }
        }

        /**
         * Alter associated fields.
         *
         * @return
         */
        public Access access() {
            return access;
        }

        /**
         * Returns the anonymity property.
         *
         * @return
         */
        public Anonymity getAnonymity() {
            return anonymity;
        }
        
        /**
         * Returns the attributes.
         *
         * @return
         */
        public Map<Integer, Object> getAttributes() {
            return attributes;
        }
        
        /**
         * Returns the index of an attribute.
         *
         * @param attr
         * @return
         */
        public int getDimension(final String attr) {
            return headermap.get(attr);
        }

        /**
         * Returns the generalization for the attribute.
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
         * Returns the maximal information loss.
         *
         * @return
         */
        public InformationLoss<?> getMaximumInformationLoss() {
            return maxInformationLoss;
        }

        /**
         * Returns the minimal information loss.
         *
         * @return
         */
        public InformationLoss<?> getMinimumInformationLoss() {
            return minInformationLoss;
        }

        /**
         * The predecessors.
         *
         * @return
         */
        public ARXNode[] getPredecessors() {
            return predecessors;
        }

        /**
         * Returns the quasi identifiers.
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
         * The successors.
         *
         * @return
         */
        public ARXNode[] getSuccessors() {
            return successors;
        }

        /**
         * Returns the sum of all generalization levels.
         *
         * @return
         */
        public int getTotalGeneralizationLevel() {
            int level = 0;
            for (int i : transformation) {
                level += i;
            }
            return level;
        }

        /**
         * Returns the transformation as an array.
         *
         * @return
         */
        public int[] getTransformation() {
            return transformation;
        }

        /**
         * Returns the anonymity property.
         *
         * @return
         */
        @Deprecated
        public Anonymity isAnonymous() {
            return anonymity;
        }
        
        /**
         * Returns if the node has been checked explicitly.
         *
         * @return
         */
        public boolean isChecked() {
            return checked;
        }

        /**
         * De-serialization.
         *
         * @param aInputStream
         * @throws ClassNotFoundException
         * @throws IOException
         */
        private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

            // Default de-serialization
            aInputStream.defaultReadObject();
            
            // Translate information loss, if necessary
            this.lowerBound = InformationLoss.createInformationLoss(this.lowerBound, 
                                                                    metric, 
                                                                    getDeserializationContext().minLevel, 
                                                                    getDeserializationContext().maxLevel);
            
            this.maxInformationLoss = InformationLoss.createInformationLoss(this.maxInformationLoss, 
                                                                            metric, 
                                                                            getDeserializationContext().minLevel, 
                                                                            getDeserializationContext().maxLevel);
            
            this.minInformationLoss = InformationLoss.createInformationLoss(this.minInformationLoss,
                                                                            metric, 
                                                                            getDeserializationContext().minLevel, 
                                                                            getDeserializationContext().maxLevel);
        }

        /**
         * Returns a node's internal id.
         *
         * @return
         */
        protected Integer getId(){
            return this.id;
        }

        /**
         * Returns a node's lower bound, if any.
         *
         * @return
         */
        protected InformationLoss<?> getLowerBound(){
            return this.lowerBound;
        }

        /**
         * Internal method that sets the id.
         *
         * @param id
         */
        protected void setId(int id) {
            this.id = id;
        }
    }

    /**
     * Context for deserialization.
     *
     * @author Florian Kohlmayer
     */
    public static class LatticeDeserializationContext {
        
        /**  Min level */
        public int minLevel = 0;
        
        /**  Max level */
        public int maxLevel = 0;
    }
    
    /**
     * For using int arrays as keys in maps
     */
    class IntArrayWrapper {

        /**  Value */
        private final int[] array;
        
        /**  Hash code */
        private final int   hashCode;

        /**
         * Constructor
         *
         * @param array
         */
        public IntArrayWrapper(final int[] array) {
            this.array = array;
            this.hashCode = Arrays.hashCode(array);
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) return false;
            return Arrays.equals(array, ((IntArrayWrapper) obj).array);
        }

        /**
         * 
         *
         * @return
         */
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
    
    /** Deserialization context. */
    private static LatticeDeserializationContext deserializationContext = new LatticeDeserializationContext();

    /**  SVUID */
    private static final long     serialVersionUID                  = -8790104959905019184L;

    /**
     * Returns the deserialization context.
     *
     * @return
     */
    public static LatticeDeserializationContext getDeserializationContext() {
        return deserializationContext;
    }

    /** The accessor. */
    private final Access          access                            = new Access(this);

    /** The bottom node. */
    private transient ARXNode     bottom;

    /** The levels in the lattice. */
    private transient ARXNode[][] levels;

    /** Metric. */
    private Metric<?>             metric;

    /** The optimum. */
    private transient ARXNode     optimum;

    /** The number of nodes. */
    private int                   size;

    /** The top node. */
    private transient ARXNode     top;

    /** Is practical monotonicity being assumed. */
    private boolean               uncertainty;

    /** Monotonicity of information loss. */
    private boolean               monotonicAnonymous;
    
    /** Monotonicity of information loss. */
    private boolean               monotonicNonAnonymous;

    /** Minimum loss in the lattice. */
    private InformationLoss<?>    minimumInformationLoss            = null;

    /** Maximum loss in the lattice. */
    private InformationLoss<?>    maximumInformationLoss            = null;

    /**
     * Constructor.
     *
     * @param solutions The solution space
     * @param optimum The optimum
     * @param header The header
     * @param config The config
     */
    ARXLattice(final SolutionSpace solutions,
               final Transformation optimum,
               final String[] header,
               final ARXConfigurationInternal config) {

        this.metric = config.getMetric();
        this.monotonicNonAnonymous = metric.isMonotonic() || !config.isSuppressionAlwaysEnabled();
        this.monotonicAnonymous = metric.isMonotonic() || config.getAbsoluteMaxOutliers() == 0;
 
        // Set this flag to true, if practical monotonicity is being assumed
        this.uncertainty = config.isPracticalMonotonicity() && config.getMaxOutliers()!=0d &&
                           (!config.isCriterionMonotonic() || !config.getMetric().isMonotonic());
        
        // Build header map
        final Map<String, Integer> headermap = new HashMap<String, Integer>();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            headermap.put(header[i], index++);
        }

        // Create nodes
        final LongObjectOpenHashMap<ARXNode> map = new LongObjectOpenHashMap<ARXNode>();
        final IntObjectOpenHashMap<List<ARXNode>> levels = new IntObjectOpenHashMap<List<ARXNode>>(); 
        int size = 0;
        int maxlevel = 0;
        for (Iterator<Long> iterator = solutions.getMaterializedTransformations(); iterator.hasNext();) {
            Transformation transformation = solutions.getTransformation(iterator.next());
            if (!levels.containsKey(transformation.getLevel())) {
                levels.put(transformation.getLevel(), new ArrayList<ARXNode>());
            }
            ARXNode node = new ARXNode(solutions, transformation, headermap);
            size++;
            map.put(transformation.getId(), node);
            levels.get(transformation.getLevel()).add(node);
            if (transformation.getId() == optimum.getId()) {
                this.optimum = node;
            }
            maxlevel = Math.max(maxlevel, transformation.getLevel());
        }
        this.size = size;
        this.levels = new ARXNode[maxlevel+1][];
        for (int i = 0; i < this.levels.length - 1; i++) {
            if (levels.containsKey(i)) {
                this.levels[i] = levels.get(i).toArray(new ARXNode[levels.get(i).size()]);
            } else {
                this.levels[i] = new ARXNode[0];
            }
        }
        
        // Create relationships
        for (Iterator<Long> iterator = solutions.getMaterializedTransformations(); iterator.hasNext();) {
            final long id = iterator.next();
            final ARXNode fnode = map.get(id);
            
            List<ARXNode> successors = new ArrayList<ARXNode>();
            List<ARXNode> predecessors = new ArrayList<ARXNode>();
            for (Iterator<Long> iter1 = solutions.getSuccessors(id); iter1.hasNext();) {
                successors.add(map.get(iter1.next()));
            }
            for (Iterator<Long> iter2 = solutions.getPredecessors(id); iter2.hasNext();) {
                predecessors.add(map.get(iter2.next()));
            }
            
            fnode.successors = successors.toArray(new ARXNode[successors.size()]);
            fnode.predecessors = predecessors.toArray(new ARXNode[predecessors.size()]);
        }
        
        // find bottom node
        outer: for (int i = 0; i < this.levels.length; i++) {
            final ARXNode[] level = this.levels[i];
            for (int j = 0; j < level.length; j++) {
                final ARXNode node = level[j];
                if (node != null) {
                    bottom = node;
                    break outer;
                }
            }
        }

        // find top node
        outer: for (int i = this.levels.length - 1; i >= 0; i--) {
            final ARXNode[] level = this.levels[i];
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
     * Access fields of this class.
     *
     * @return
     */
    public Access access() {
        return access;
    }

    /**
     * Returns the bottom node.
     *
     * @return
     */
    public ARXNode getBottom() {
        return bottom;
    }

    /**
     * Returns the levels of the generalization lattice.
     *
     * @return
     */
    public ARXNode[][] getLevels() {
        return levels;
    }

    /**
     * Returns the maximal information loss.
     *
     * @return
     */
    public InformationLoss<?> getMaximumInformationLoss(){
        if (this.maximumInformationLoss == null) {
            this.estimateInformationLoss();
        }
        return this.maximumInformationLoss;
    }

    /**
     * Returns the minimal information loss.
     *
     * @return
     */
    public InformationLoss<?> getMinimumInformationLoss(){
        if (this.minimumInformationLoss == null) {
            this.estimateInformationLoss();
        }
        return this.minimumInformationLoss;
    }
    
    /**
     * Returns the number of nodes.
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the top node.
     *
     * @return
     */
    public ARXNode getTop() {
        return top;
    }
    
    /**
     * De-serialization.
     *
     * @param aInputStream
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

        // Default de-serialization
        aInputStream.defaultReadObject();
        
        // Translate minimum and maximum
        this.maximumInformationLoss = InformationLoss.createInformationLoss(this.maximumInformationLoss,
                                                                            metric,
                                                                            getDeserializationContext().minLevel,
                                                                            getDeserializationContext().maxLevel);
        
        this.minimumInformationLoss = InformationLoss.createInformationLoss(this.minimumInformationLoss,
                                                                            metric,
                                                                            getDeserializationContext().minLevel,
                                                                            getDeserializationContext().maxLevel);
        
        // Translate metric, if necessary
        this.metric = Metric.createMetric(this.metric, 
                                          getDeserializationContext().minLevel, 
                                          getDeserializationContext().maxLevel);
    }


    /**
     * This method triggers the estimation of the information loss of all nodes
     * in the lattice regardless of whether they have been checked for anonymity
     * or not.
     */
    protected void estimateInformationLoss() {
        UtilityEstimator estimator = new UtilityEstimator(this, metric, monotonicAnonymous, monotonicNonAnonymous);
        estimator.estimate();
        this.minimumInformationLoss = estimator.getGlobalMinimum();
        this.maximumInformationLoss = estimator.getGlobalMaximum();
    }
    

    /**
     * Returns the optimum, if any.
     *
     * @return
     */
    protected ARXNode getOptimum() {
        return optimum;
    }
}
