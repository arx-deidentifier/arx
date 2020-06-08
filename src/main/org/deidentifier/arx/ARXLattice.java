/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.ARXProcessStatistics.Step;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.framework.lattice.ObjectIterator;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.framework.lattice.TransformationList;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

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
         * Accessor method
         * 
         * @return
         */
        public Map<String, Integer> getAttributeMap() {
            return bottom.headermap;
        }

        /**
         * Accessor method
         *
         * @param bottom
         */
        public void setBottom(final ARXNode bottom) {
            lattice.bottom = bottom;
        }

        /**
         * Accessor method
         *
         * @param levels
         */
        public void setLevels(final ARXNode[][] levels) {
            lattice.levels = levels;
        }

        /**
         * Accessor method
         *
         * @param config
         */
        public void setMonotonicity(ARXConfiguration config) {
            lattice.setMonotonicity(config.isSuppressionAlwaysEnabled(), config.getAbsoluteSuppressionLimit());
        }

        /**
         * Accessor method
         *
         * @param node
         */
        public void setOptimum(final ARXNode node) {
            lattice.optimum = node;
        }

        /**
         * Accessor method
         *
         * @param model
         */
        public void setQualityModel(final Metric<?> model) {
            lattice.metric = model;
        }

        /**
         * Accessor method
         *
         * @param size
         */
        public void setSize(final int size) {
            lattice.size = size;
        }

        /**
         * Updates the solution space
         * @param solutions
         */
        public void setSolutionSpace(SolutionSpace<?> solutions) {
            lattice.solutions = solutions;
            
            // For backwards compatibility
            lattice.map = new ObjectObjectOpenHashMap<Object, ARXNode>();
            for (ARXNode[] level : lattice.levels) {
                for (ARXNode node : level) {
                    int[] levels = node.getTransformation();
                    Transformation<?> transformation = lattice.solutions.getTransformation(levels);
                    Object id = transformation.getIdentifier();
                    lattice.map.put(id, node);
                }
            }
        }
        
        /**
         * Accessor method
         *
         * @param top
         */
        public void setTop(final ARXNode top) {
            lattice.top = top;
        }

        /**
         * Accessor method
         *
         * @param uncertainty
         */
        public void setUncertainty(final boolean uncertainty) {
            lattice.uncertainty = uncertainty;
        }
    }

    /**
     * Reflects different anonymity properties.
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
             * Accessor class
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
             * Sets the maximal information loss.
             *
             * @param a
             */
            public void setHighestScore(final InformationLoss<?> a) {
                node.maxInformationLoss = InformationLoss.createInformationLoss(a, metric, getDeserializationContext().minLevel, getDeserializationContext().maxLevel);
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
             * Sets the minimal information loss.
             *
             * @param a
             */
            public void setLowestScore(final InformationLoss<?> a) {
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
        private Integer              id         = null;

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

        /** The underlying lattice */
        private final ARXLattice     lattice;

        /**
         * Internal constructor for deserialization.
         * 
         * @param lattice
         */
        public ARXNode(ARXLattice lattice) {
            this.lattice = lattice;
        }
        
        /**
         * Constructor.
         *
         * @param lattice
         * @param solutions
         * @param transformation
         * @param headermap
         */
        private ARXNode(final ARXLattice lattice,
                        final SolutionSpace<?> solutions, 
                        final Transformation<?> transformation, 
                        final Map<String, Integer> headermap) {
            
            // Set properties
            this.lattice = lattice;
            this.headermap = headermap;
            this.transformation = transformation.getGeneralization();
            this.minInformationLoss = transformation.getInformationLoss();
            this.maxInformationLoss = transformation.getInformationLoss();
            this.lowerBound = transformation.getLowerBound();
            this.checked = transformation.hasProperty(solutions.getPropertyChecked());
            
            // Transfer anonymity property without uncertainty
            if (transformation.hasProperty(solutions.getPropertyChecked())){
                if (transformation.hasProperty(solutions.getPropertyAnonymous())) {
                    this.anonymity = Anonymity.ANONYMOUS;
                } else if(transformation.hasProperty(solutions.getPropertyNotAnonymous())) {
                    this.anonymity = Anonymity.NOT_ANONYMOUS;
                } else {                  
                    this.anonymity = Anonymity.UNKNOWN;
                }
            // This is a node for which the property is unknown
            } else {
                if (transformation.hasProperty(solutions.getPropertyAnonymous())) {
                    this.anonymity = uncertainty ? Anonymity.PROBABLY_ANONYMOUS : Anonymity.ANONYMOUS;
                } else if (transformation.hasProperty(solutions.getPropertyNotAnonymous())) {
                    this.anonymity = uncertainty ? Anonymity.PROBABLY_NOT_ANONYMOUS : Anonymity.NOT_ANONYMOUS;
                } else if (transformation.hasProperty(solutions.getPropertyNotKAnonymous())) {
                    this.anonymity = Anonymity.NOT_ANONYMOUS;
                } else if (transformation.hasProperty(solutions.getPropertyInsufficientUtility())) {
                    this.anonymity = Anonymity.UNKNOWN;
                } else {
                    this.anonymity = Anonymity.UNKNOWN;
                }
            }

            // Make sure that we have information loss available
            // Important for expand operations
            if (this.maxInformationLoss == null) {
                this.maxInformationLoss = metric.createInstanceOfHighestScore();
            }
            if (this.minInformationLoss == null) {
                this.minInformationLoss = metric.createInstanceOfLowestScore();
            }
        }

        /**
         * Creates a new node from a step
         * @param lattice
         * @param step
         */
        private ARXNode(final ARXLattice lattice, 
                        final Step step) {
            this.lattice = lattice;
            this.headermap = step.getHeader();
            this.transformation = step.getTransformation();
            this.minInformationLoss = step.getScore();
            this.maxInformationLoss = step.getScore();
            this.lowerBound = null;
            this.checked = true;
            this.anonymity = Anonymity.ANONYMOUS;
            this.getAttributes().put(Integer.MAX_VALUE, step);
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
         * Materializes any non-materialized predecessors and successors
         */
        public void expand() {
            this.lattice.expand(this);
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
         * Returns the highest score. Lower is better.
         * @return
         */
        public InformationLoss<?> getHighestScore() {
            return maxInformationLoss;
        }

        /**
         * Returns the highest score. Lower is better.
         * @return
         */
        public InformationLoss<?> getLowestScore() {
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
         * Returns if the node has been checked explicitly.
         *
         * @return
         */
        public boolean isChecked() {
            return checked;
        }
        
        /**
         * Renders this object
         * @return
         */
        public ElementData render() {
            ElementData result = new ElementData("Transformation");
            result.addProperty("Anonymity", this.anonymity);
            result.addProperty("Minimum information loss", this.minInformationLoss.toString());
            result.addProperty("Maximum information loss", this.maxInformationLoss.toString());
            result.addProperty(null, renderGeneralizationScheme());
            return result;
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
         * Renders this object
         * @return
         */
        private ElementData renderGeneralizationScheme() {
            ElementData result = new ElementData("Generalization scheme");
            for (String qi : this.getQuasiIdentifyingAttributes()) {
                result.addProperty(qi, this.getGeneralization(qi) + "/" +
                                       this.lattice.getTop().getGeneralization(qi));    
            }
            return result;
        }

        /**
         * Returns the headermap
         * @return
         */
        protected Map<String, Integer> getHeaderMap() {
            return this.headermap;
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

    /** Deserialization context. */
    private static LatticeDeserializationContext deserializationContext = new LatticeDeserializationContext();

    /** SVUID */
    private static final long                    serialVersionUID       = -8790104959905019184L;

    /**
     * Returns the deserialization context.
     * 
     * @return
     */
    public static LatticeDeserializationContext getDeserializationContext() {
        return deserializationContext;
    }

    /** The accessor. */
    private final Access                                       access                 = new Access(this);

    /** The bottom node. */
    private transient ARXNode                                  bottom;

    /** The levels in the lattice. */
    private transient ARXNode[][]                              levels;

    /** Metric. */
    private Metric<?>                                          metric;

    /** The optimum. */
    private transient ARXNode                                  optimum;

    /** The number of nodes. */
    private int                                                size;

    /** The virtual size: TODO: Legacy field. Remove later. */
    private Long                                               virtualSize;

    /** The virtual size */
    private BigInteger                                         virtualSizeLargeLattice;

    /** The top node. */
    private transient ARXNode                                  top;

    /** Is practical monotonicity being assumed. */
    private boolean                                            uncertainty;

    /** Kept only for backwards compatibility */
    private Boolean                                            complete;

    /** Monotonicity of information loss. */
    private boolean                                            monotonicAnonymous;

    /** Monotonicity of information loss. */
    private boolean                                            monotonicNonAnonymous;

    /** Minimum loss in the lattice. */
    private InformationLoss<?>                                 minimumInformationLoss = null;

    /** Maximum loss in the lattice. */
    private InformationLoss<?>                                 maximumInformationLoss = null;

    /** The solution space */
    private transient SolutionSpace<?>                            solutions;

    /** Map from ids to nodes */
    private transient ObjectObjectOpenHashMap<Object, ARXNode> map;

    /**
     * Constructor.
     *
     * @param statistics
     */
    ARXLattice(final ARXProcessStatistics statistics) {
        
        // Lots of null values
        solutions = null;
        map = null;
        monotonicNonAnonymous = false;
        monotonicAnonymous = false;
        complete = null;
        uncertainty = false;
        optimum = null;
        top = null;
        bottom = null;

        // Basic data
        virtualSizeLargeLattice = BigInteger.valueOf(statistics.getNumberOfSteps());
        size = statistics.getNumberOfSteps();
        
        // Build lattice
        int minLevel = Integer.MAX_VALUE;
        int maxLevel = Integer.MIN_VALUE;
        minimumInformationLoss = null;
        maximumInformationLoss = null;
        for (Step s : statistics.getSteps()) {
            if (minimumInformationLoss == null) {
                minimumInformationLoss = s.getScore().clone();
            } else {
                minimumInformationLoss.min(s.getScore());
            }
            if (maximumInformationLoss == null) {
                maximumInformationLoss = s.getScore().clone();
            } else {
                maximumInformationLoss.max(s.getScore());
            }
            minLevel = Math.min(minLevel, s.getTotalGeneralizationLevel());
            maxLevel = Math.max(maxLevel, s.getTotalGeneralizationLevel());
        }
        List<List<ARXNode>> lattice = new ArrayList<List<ARXNode>>();
        for (int level = 0; level <= maxLevel; level++) {
            lattice.add(new ArrayList<ARXNode>());
        }
        for (Step s : statistics.getSteps()) {
            ARXNode node = new ARXNode(this, s);
            node.predecessors = new ARXNode[0];
            node.successors = new ARXNode[0];
            lattice.get(s.getTotalGeneralizationLevel()).add(node);
        }
        this.levels = new ARXNode[maxLevel + 1][];
        for (int level = 0; level < levels.length; level++) {
            levels[level] = lattice.get(level).toArray(new ARXNode[lattice.get(level).size()]);
        }
        
        // Create relationships
        for (int level = 0; level < levels.length - 1; level++) {

            // Outer loop
            for (ARXNode lower : levels[level]) {
                
                // Inner loop
                for (ARXNode upper : levels[level + 1]) {
                    
                    // Check
                    boolean relationship = true;
                    loop: for (int i=0; i<lower.getTransformation().length; i++) {
                        if (lower.getTransformation()[i] > upper.getTransformation()[i]) {
                            relationship = false;
                            break loop;
                        }
                    }
                    
                    // Connect
                    if (relationship) {
                        List<ARXNode> predecessors = new ArrayList<>();
                        predecessors.addAll(Arrays.asList(upper.getPredecessors()));
                        List<ARXNode> successors = new ArrayList<>();
                        successors.addAll(Arrays.asList(lower.getSuccessors()));
                        predecessors.add(lower);
                        successors.add(upper);
                        lower.predecessors = predecessors.toArray(new ARXNode[predecessors.size()]);
                        lower.successors = successors.toArray(new ARXNode[successors.size()]);
                    }
                }
            }
        }
    }

    /**
     * Constructor.
     *
     * @param solutions The solution space
     * @param optimum The optimum
     * @param header The header
     * @param config The config
     */
    ARXLattice(final SolutionSpace<?> solutions,
               final Transformation<?> optimum,
               final String[] header,
               final ARXConfigurationInternal config) {

        // Init
        this.solutions = solutions;
        this.metric = config.getQualityModel();
        this.setMonotonicity(config.isSuppressionAlwaysEnabled(), config.getAbsoluteSuppressionLimit());
        this.virtualSizeLargeLattice = solutions.getSize();
 
        // Set this flag to true, if practical monotonicity is being assumed
        this.uncertainty = config.isPracticalMonotonicity();
        
        // Build header map
        final Map<String, Integer> headermap = new HashMap<String, Integer>();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            headermap.put(header[i], index++);
        }
        
        // Build lattice
        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            buildSingle(solutions, optimum, headermap);
            this.solutions = null;
        } else {
            build(optimum, headermap);
        }

        // find bottom node
        outer: for (int i = 0; i < this.levels.length; i++) {
            final ARXNode[] level = this.levels[i];
            for (int j = 0; j < level.length; j++) {
                final ARXNode node = level[j];
                if (node != null) {
                    this.bottom = node;
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
                    this.top = node;
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
     * Materializes any non-materialized predecessors and successors
     */
    public void expand(ARXNode center) {
        
        // For results of local recoding
        if (solutions == null) {
            return;
        }
        
        // Initialize
        int[] indices = center.getTransformation();
        Transformation<?> transformation = solutions.getTransformation(indices);
        
        // Collect neighbors
        TransformationList<?> neighbors = transformation.getPredecessors();
        TransformationList<?> successors = transformation.getSuccessors();
        neighbors.addAllOfFromTo(successors, 0, successors.size() - 1);
        
        // Collect affected levels
        int lowerLevel = transformation.getLevel() - 1;
        int higherLevel = transformation.getLevel() + 1;
        if (transformation.getLevel() == solutions.getTop().getLevel()) {
            lowerLevel = solutions.getTop().getLevel() - 1;
            higherLevel = solutions.getTop().getLevel() - 1;
        } else if (transformation.getLevel() == solutions.getBottom().getLevel()) {
            lowerLevel = solutions.getBottom().getLevel() + 1;
            higherLevel = solutions.getBottom().getLevel() + 1;
        }

        // Find missing neighbors and initialize variables
        Map<String, Integer> headermap = this.getBottom().getHeaderMap();
        Set<Object> missing = new HashSet<>();
        for (int i = 0; i < neighbors.size(); i++) {
            missing.add(neighbors.getQuick(i));
        }
        outer: for (int level = lowerLevel; level <= higherLevel; level ++) {
            if (level != transformation.getLevel()) {
                for (ARXNode node : this.levels[level]) {
                    Object id = solutions.getTransformation(node.getTransformation()).getIdentifier();
                    missing.remove(id);
                    if (missing.isEmpty()) {
                        break outer;
                    }
                }
            }
        }
        
        // Materialize missing nodes
        Map<Integer, List<ARXNode>> levels = new HashMap<Integer, List<ARXNode>>();
        for (Object id : missing) {
            
            // Materialize
            transformation = solutions.getTransformation(id);
            ARXNode node = new ARXNode(this, solutions, transformation, headermap);
            
            // Store in global map
            map.put(id, node);
            
            // Store in map of levels
            if (!levels.containsKey(transformation.getLevel())) {
                levels.put(transformation.getLevel(), new ArrayList<ARXNode>());
            }
            levels.get(transformation.getLevel()).add(node);
        }
        
        // Insert missing nodes into level Arrays
        for (int level : levels.keySet()) {
            
            // Sort nodes to insert, lexicographically
            List<ARXNode> nodes = levels.get(level);
            Collections.sort(nodes, new Comparator<ARXNode>(){
                public int compare(ARXNode o1, ARXNode o2) {
                    return compareLexicographically(o1, o2);
                }
            });

            // Initialize new level
            List<ARXNode> list = new ArrayList<ARXNode>();
            
            // Now add all nodes in one pass
            int index = 0;
            for (ARXNode node : this.levels[level]) {
                while (index < nodes.size() && compareLexicographically(nodes.get(index), node) < 0) {
                    list.add(nodes.get(index++));
                }
                list.add(node);
            }
            
            // Add remaining
            while (index < nodes.size()) {
                list.add(nodes.get(index++));
            }
            
            // Convert
            this.levels[level] = list.toArray(new ARXNode[list.size()]);
        }
        
        // Build relationships from/to missing nodes
        for (Object id : missing) {
            this.createExpandedRelationships(solutions, id);
        }
        
        // Update size
        this.size += missing.size();
        
        // Update information loss
        for (Object id : missing) {
            
            // Pull lower bound from predecessors
            transformation = solutions.getTransformation(id);
            ARXNode node = map.get(id);
            InformationLoss<?> lowerBound = null;
            InformationLoss<?> min = metric.createInstanceOfLowestScore();
            InformationLoss<?> max = metric.createInstanceOfHighestScore();
            TransformationList<?> list = transformation.getPredecessors();
            for (int i = 0; i < list.size(); i++) {
                ARXNode predecessor = map.get(list.getQuick(i));
                if (predecessor != null && predecessor.getLowerBound() != null) {
                    min.max(predecessor.getLowerBound().clone());
                    if (lowerBound == null) {
                        lowerBound = predecessor.getLowerBound().clone();
                    } else {
                        lowerBound.max(predecessor.getLowerBound().clone());
                    }
                }
            }
            node.access.setLowestScore(min);
            node.access.setLowerBound(lowerBound);
            node.access.setHighestScore(max);
        }
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
     * Returns the highest score. Lower is better.
     * @return
     */
    public InformationLoss<?> getHighestScore(){
        // For backwards compatibility
        if (this.maximumInformationLoss == null) {
            this.estimateInformationLoss();
        }
        return this.maximumInformationLoss;
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
     * Returns the lowest score. Lower is better.
     * @return
     */
    public InformationLoss<?> getLowestScore(){
        // For backwards compatibility
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
     * Returns the virtual size of the solution space
     * @return
     */
    public BigInteger getVirtualSize() {
        return virtualSizeLargeLattice != null ? virtualSizeLargeLattice : 
               (virtualSize != null ? BigInteger.valueOf(virtualSize) : 
               BigInteger.valueOf(size));
    }

    /**
     * Renders this object
     * @return
     */
    public ElementData render() {
        ElementData result = new ElementData("Search space");
        result.addProperty("Size", this.getVirtualSize().toString());
        result.addProperty("Materialized", this.size);
        return result;
    }
    
    /**
     * Build an ARX lattice for an incompletely classified solution space
     * @param optimum
     * @param headermap
     */
    private void build(final Transformation<?> optimum, Map<String, Integer> headermap) {

        // Create nodes
        this.map = new ObjectObjectOpenHashMap<Object, ARXNode>();
        final IntObjectOpenHashMap<List<ARXNode>> levels = new IntObjectOpenHashMap<List<ARXNode>>(); 
        int size = 0;
        int maxlevel = 0;
        for (ObjectIterator<?> iterator = solutions.getMaterializedTransformations(); iterator.hasNext();) {
            
            Transformation<?> transformation = solutions.getTransformation(iterator.next());
            if (!levels.containsKey(transformation.getLevel())) {
                levels.put(transformation.getLevel(), new ArrayList<ARXNode>());
            }
            ARXNode node = new ARXNode(this, solutions, transformation, headermap);
            map.put(transformation.getIdentifier(), node);
            levels.get(transformation.getLevel()).add(node);
            if (optimum != null && transformation.getIdentifier().equals(optimum.getIdentifier())) {
                this.optimum = node;
            }
            maxlevel = Math.max(maxlevel, transformation.getLevel());
            size++;
        }
        
        // Make sure that bottom and top are in the resulting solution space
        Transformation<?> top = solutions.getTop();
        Transformation<?> bottom = solutions.getBottom();
        if (!map.containsKey(top.getIdentifier())) {
            if (!levels.containsKey(top.getLevel())) {
                levels.put(top.getLevel(), new ArrayList<ARXNode>());
            }
            ARXNode node = new ARXNode(this, solutions, top, headermap);
            map.put(top.getIdentifier(), node);
            levels.get(top.getLevel()).add(node);
            maxlevel = top.getLevel();
            size++;
        }
        if (!map.containsKey(bottom.getIdentifier())) {
            if (!levels.containsKey(bottom.getLevel())) {
                levels.put(bottom.getLevel(), new ArrayList<ARXNode>());
            }
            ARXNode node = new ARXNode(this, solutions, bottom, headermap);
            map.put(bottom.getIdentifier(), node);
            levels.get(bottom.getLevel()).add(node);
            size++;
        }

        // Create levels array
        this.size = size;
        this.levels = new ARXNode[maxlevel+1][];
        for (int i = 0; i < this.levels.length; i++) {
            if (levels.containsKey(i)) {
                this.levels[i] = levels.get(i).toArray(new ARXNode[levels.get(i).size()]);
            } else {
                this.levels[i] = new ARXNode[0];
            }
        }
        
        // Create relationships
        for (ObjectIterator<?> iterator = solutions.getMaterializedTransformations(); iterator.hasNext();) {
            createRelationships(solutions, iterator.next());
        }
        createRelationships(solutions, solutions.getTop().getIdentifier());
        createRelationships(solutions, solutions.getBottom().getIdentifier());
    }
    
    /**
     * Build an ARX lattice for a solution space with only one element
     * @param solutions 
     * @param optimum
     * @param headermap
     */
    private void buildSingle(SolutionSpace<?> solutions, final Transformation<?> optimum, Map<String, Integer> headermap) {

        // Init
        this.size = 1;
        
        // Generate node
        ARXNode node = new ARXNode(this,
                                   solutions,
                                   optimum,
                                   headermap);
        node.successors = new ARXNode[0];
        node.predecessors = new ARXNode[0];
        
        // Store optimum
        this.optimum = node;
        
        // Generate level arrays
        this.levels = new ARXNode[optimum.getLevel() + 1][];
        for (int i = 0; i < levels.length - 1; i++) {
            this.levels[i] = new ARXNode[0];
        }
        this.levels[optimum.getLevel()] = new ARXNode[]{node};
    }

    /**
     * Compares the transformations of two nodes lexicographically
     * @param first
     * @param second
     * @return
     */
    private int compareLexicographically(ARXNode first, ARXNode second) {
        int[] firstArray = first.getTransformation();
        int[] secondArray = second.getTransformation();
        for (int i = 0; i < firstArray.length; i++) {
            if (firstArray[i] < secondArray[i]) {
                return -1;
            } else if (firstArray[i] > secondArray[i]) {
                return +1;
            }
        }
        return 0;
    }
    
    /**
     * Creates all relationships 
     * @param solutions
     * @param map
     * @param id
     */
    private void createExpandedRelationships(final SolutionSpace<?> solutions,
                                             final Object id) {
        
        // Obtain given node
        final ARXNode center = map.get(id);
        final Transformation<?> transformation = solutions.getTransformation(id);
        
        // Collect materialized successors and predecessors
        List<ARXNode> successors = new ArrayList<ARXNode>();
        List<ARXNode> predecessors = new ArrayList<ARXNode>();
        
        TransformationList<?> list1 = transformation.getSuccessors();
        for (int i = 0; i < list1.size(); i++) {
            ARXNode node = map.get(list1.getQuick(i));
            if (node != null) {
                successors.add(node);
            }
        }

        TransformationList<?> list2 = transformation.getPredecessors();
        for (int i = 0; i < list2.size(); i++) {
            ARXNode node = map.get(list2.getQuick(i));
            if (node != null) {
                predecessors.add(node);
            }
        }
        
        // Add successors and predecessors to given node
        center.successors = successors.toArray(new ARXNode[successors.size()]);
        center.predecessors = predecessors.toArray(new ARXNode[predecessors.size()]);
        
        // Update predecessors
        for (ARXNode node : predecessors) {
            
           List<ARXNode> nodeSuccessors = new ArrayList<ARXNode>();
           nodeSuccessors.addAll(Arrays.asList(node.successors));
           int index = 0;
           while (index < nodeSuccessors.size() && compareLexicographically(nodeSuccessors.get(index), center) < 0 ) {
               index++;
           }
           // Subtract
           index = index == 0 ? 0 : index - 1;
           nodeSuccessors.add(index, center);
           
           // Add and update
           node.successors = nodeSuccessors.toArray(new ARXNode[nodeSuccessors.size()]); 
        }
        

        // Update successors
        for (ARXNode node : successors) {
            
           List<ARXNode> nodePredecessors = new ArrayList<ARXNode>();
           nodePredecessors.addAll(Arrays.asList(node.predecessors));
           int index = 0;
           while (index < nodePredecessors.size() && compareLexicographically(nodePredecessors.get(index), center) < 0 ) {
               index++;
           }
           // Subtract
           index = index == 0 ? 0 : index - 1;
           
           // Add and update
           nodePredecessors.add(index, center);
           node.predecessors = nodePredecessors.toArray(new ARXNode[nodePredecessors.size()]); 
        }
    }


    /**
     * Creates all relationships
     * @param solutions
     * @param map
     * @param id
     */
    private void createRelationships(final SolutionSpace<?> solutions,
                                     final Object id) {
        
        final ARXNode fnode = map.get(id);
        final Transformation<?> transformation = solutions.getTransformation(id);
        
        List<ARXNode> successors = new ArrayList<ARXNode>();
        List<ARXNode> predecessors = new ArrayList<ARXNode>();
        
        TransformationList<?> list1 = transformation.getSuccessors();
        for (int i = 0; i < list1.size(); i++) {
            ARXNode node = map.get(list1.getQuick(i));
            if (node != null) {
                successors.add(node);
            }
        }

        TransformationList<?> list2 = transformation.getPredecessors();
        for (int i = 0; i < list2.size(); i++) {
            ARXNode node = map.get(list2.getQuick(i));
            if (node != null) {
                predecessors.add(node);
            }
        }
        
        fnode.successors = successors.toArray(new ARXNode[successors.size()]);
        fnode.predecessors = predecessors.toArray(new ARXNode[predecessors.size()]);
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
     * Sets the monotonicity based on the current configuration
     * @param isSuppressionAlwaysEnabled
     * @param absoluteSuppressionLimit
     */
    private void setMonotonicity(boolean isSuppressionAlwaysEnabled, int absoluteSuppressionLimit) {
        

        this.monotonicNonAnonymous = (this.metric.isMonotonicWithSuppression() && isSuppressionAlwaysEnabled) ||
                                     (this.metric.isMonotonicWithGeneralization() && !isSuppressionAlwaysEnabled);
        
        this.monotonicAnonymous = this.metric.isMonotonic(absoluteSuppressionLimit);
    }
    
    /**
     * This method triggers the estimation of the information loss of all nodes
     * in the lattice regardless of whether they have been checked for anonymity
     * or not. Additionally, it computes global upper and lower bounds on utility
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

    /**
     * Kept only for backwards compatibility
     * @return
     */
    boolean _legacySearchedWithFlash() {
        return complete == null ? true : complete;
    }
}
