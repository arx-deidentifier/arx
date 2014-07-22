/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.algorithm;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.framework.lattice.NodeTrigger;
import org.deidentifier.arx.framework.lattice.NodeTriggerConstant;
import org.deidentifier.arx.framework.lattice.NodeTriggerInverse;
import org.deidentifier.arx.metric.Metric;

/**
 * This class provides a static method for instantiating the FLASH algorithm
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHAlgorithm {
    
    /**
     * Monotonicity
     */
    private static enum Monotonicity {
        FULL,
        PARTIAL,
        NONE
    }

    /**
     * Creates a new instance of the FLASH algorithm
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    public static AbstractAlgorithm create(final Lattice lattice,
                                           final INodeChecker checker,
                                           final FLASHStrategy strategy){

        // Init
        ARXConfiguration config = checker.getConfiguration();
        Metric<?> metric = checker.getMetric();
        
        // NOTE: 
        // - If we assume practical monotonicity then we assume
        //   monotonicity for both criterion AND metric
        // - Without suppression we assume monotonicity for all criteria
        // - Without suppression we assume monotonicity for all metrics
        
        // Determine monotonicity of metric
        Monotonicity monotonicityMetric;
        if (metric.isMonotonic() || config.getMaxOutliers() == 0d || config.isPracticalMonotonicity()) {
            monotonicityMetric = Monotonicity.FULL;
        } else {
            monotonicityMetric = Monotonicity.NONE;
        }
        
        // Determine monotonicity of criteria
        Monotonicity monotonicityCriteria;
        if (config.getMaxOutliers() == 0d || config.isPracticalMonotonicity()) {
            monotonicityCriteria = Monotonicity.FULL;
        } else {
            if (config.getMinimalGroupSize() != Integer.MAX_VALUE) {
                if (config.getCriteria().size() == 1 && config.containsCriterion(KAnonymity.class)) {
                    monotonicityCriteria = Monotonicity.FULL;
                } else {
                    monotonicityCriteria = Monotonicity.PARTIAL;
                }
            } else {
                monotonicityCriteria = Monotonicity.NONE;
            }
        }
        
        // ******************************
        // CASE 1
        // ******************************
        if (monotonicityCriteria == Monotonicity.FULL && monotonicityMetric == Monotonicity.FULL) {
            return createFullFull(lattice, checker, strategy);
        }
        
        
        // ******************************
        // CASE 2
        // ******************************
        if (monotonicityCriteria == Monotonicity.FULL && monotonicityMetric == Monotonicity.NONE) {
            return createFullNone(lattice, checker, strategy);
        }
        
        // ******************************
        // CASE 3
        // ******************************
        if (monotonicityCriteria == Monotonicity.PARTIAL && monotonicityMetric == Monotonicity.FULL) {
            return createPartialFull(lattice, checker, strategy);
        }
        
        // ******************************
        // CASE 4
        // ******************************
        if (monotonicityCriteria == Monotonicity.PARTIAL && monotonicityMetric == Monotonicity.NONE) {
            return createPartialNone(lattice, checker, strategy);
        }
        
        // ******************************
        // CASE 5
        // ******************************
        if (monotonicityCriteria == Monotonicity.NONE && monotonicityMetric == Monotonicity.FULL) {
            return createNoneFull(lattice, checker, strategy);
        }
        
        // ******************************
        // CASE 6
        // ******************************
        if (monotonicityCriteria == Monotonicity.NONE && monotonicityMetric == Monotonicity.NONE) {
            return createNoneNone(lattice, checker, strategy);
        }
        
        throw new IllegalStateException("Oops");
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createNoneNone(Lattice lattice,
                                                    INodeChecker checker,
                                                    FLASHStrategy strategy) {

        // We focus on the anonymity property
        int anonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // Skip nodes for which the anonymity property is known
        NodeTrigger triggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // No evaluation, no tagging
        NodeTrigger triggerSnapshotStore = History.STORAGE_TRIGGER_ALL;
        NodeTrigger triggerSnapshotEvict = History.EVICTION_TRIGGER_CHECKED;
        NodeTrigger triggerEvaluate = new NodeTriggerConstant(false);
        NodeTrigger triggerCheck = new NodeTriggerInverse(triggerSkip);
        NodeTrigger triggerTag = new NodeTriggerConstant(false);

        // Only one linear phase
        FLASHConfiguration binaryConfiguration = new FLASHConfiguration();
        FLASHConfiguration linearConfiguration = new FLASHConfiguration(anonymityProperty,
                                                                        triggerTag,
                                                                        triggerCheck,
                                                                        triggerEvaluate,
                                                                        triggerSkip,
                                                                        triggerSnapshotStore,
                                                                        triggerSnapshotEvict);
        
        return new FLASHAlgorithmImpl(lattice, checker,strategy, binaryConfiguration, linearConfiguration);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createNoneFull(Lattice lattice,
                                                    INodeChecker checker,
                                                    FLASHStrategy strategy) {

        // We focus on the anonymity property
        int anonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // We skip nodes for which the anonymity property is known or which have insufficient utility
        NodeTrigger triggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };

        // No evaluation
        NodeTrigger triggerSnapshotStore = History.STORAGE_TRIGGER_ALL;
        NodeTrigger triggerSnapshotEvict = History.EVICTION_TRIGGER_CHECKED;
        NodeTrigger triggerEvaluate = new NodeTriggerConstant(false);
        NodeTrigger triggerCheck = new NodeTriggerInverse(triggerSkip);
        
        // We predictively tag nodes with insufficient utility because of the monotonic metric 
        NodeTrigger triggerTag = new FLASHNodeTrigger(lattice) {
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS);
            }
            public void action(Lattice lattice, Node node) {
                lattice.setPropertyUpwards(node, false, Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };
        
        // Only one linear search
        FLASHConfiguration binaryConfiguration = new FLASHConfiguration();
        FLASHConfiguration linearConfiguration = new FLASHConfiguration(anonymityProperty,
                                                                        triggerTag,
                                                                        triggerCheck,
                                                                        triggerEvaluate,
                                                                        triggerSkip,
                                                                        triggerSnapshotStore,
                                                                        triggerSnapshotEvict);
        
        return new FLASHAlgorithmImpl(lattice, checker,strategy, binaryConfiguration, linearConfiguration);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createPartialNone(Lattice lattice,
                                                       INodeChecker checker,
                                                       FLASHStrategy strategy) {
        /* *******************************
         *  BINARY PHASE
         *********************************/

        // We focus on the k-anonymity property
        int binaryAnonymityProperty = Node.PROPERTY_K_ANONYMOUS;
        
        // Skip nodes for which the k-anonymity property is known
        NodeTrigger binaryTriggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
            }
        };

        // We predictively tag the k-anonymity property
        NodeTrigger binaryTriggerTag = new FLASHNodeTrigger(binaryTriggerSkip, lattice){
            public void action(Lattice lattice, Node node) {
                if (node.hasProperty(Node.PROPERTY_K_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_K_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_K_ANONYMOUS);
                }
            }
        };
        
        // No evaluation
        NodeTrigger binaryTriggerSnapshotStore = History.STORAGE_TRIGGER_NON_ANONYMOUS;
        NodeTrigger binaryTriggerSnapshotEvict = History.EVICTION_TRIGGER_K_ANONYMOUS;
        NodeTrigger binaryTriggerCheck = new NodeTriggerInverse(binaryTriggerSkip);
        NodeTrigger binaryTriggerEvaluate = new NodeTriggerConstant(false);
        
        /* *******************************
         *  LINEAR PHASE
         *********************************/

        // We focus on the anonymity property
        int linearAnonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // We skip nodes for which the anonymity property is known, or which are not k-anonymous
        NodeTrigger linearTriggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
            }
        };

        // No evaluation
        NodeTrigger linearTriggerSnapshotStore = History.STORAGE_TRIGGER_ALL;
        NodeTrigger linearTriggerSnapshotEvict = History.EVICTION_TRIGGER_CHECKED;
        NodeTrigger linearTriggerEvaluate = new NodeTriggerConstant(false);

        // We check nodes which have not been skipped
        NodeTrigger linearTriggerCheck = new NodeTriggerInverse(linearTriggerSkip);

        // We do not predictively tag anything 
        NodeTrigger linearTriggerTag = new NodeTriggerConstant(false);

        // Binary configuration
        FLASHConfiguration binaryConfiguration = new FLASHConfiguration(binaryAnonymityProperty,
                                                                        binaryTriggerTag,
                                                                        binaryTriggerCheck,
                                                                        binaryTriggerEvaluate,
                                                                        binaryTriggerSkip,
                                                                        binaryTriggerSnapshotStore,
                                                                        binaryTriggerSnapshotEvict);
        
        // Linear configuration
        FLASHConfiguration linearConfiguration = new FLASHConfiguration(linearAnonymityProperty,
                                                                        linearTriggerTag,
                                                                        linearTriggerCheck,
                                                                        linearTriggerEvaluate,
                                                                        linearTriggerSkip,
                                                                        linearTriggerSnapshotStore,
                                                                        linearTriggerSnapshotEvict);

        return new FLASHAlgorithmImpl(lattice, checker,strategy, binaryConfiguration, linearConfiguration);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createPartialFull(Lattice lattice,
                                                       INodeChecker checker,
                                                       FLASHStrategy strategy) {
        /* *******************************
         *  BINARY PHASE
         *********************************/

        // We focus on the k-anonymity property
        int binaryAnonymityProperty = Node.PROPERTY_K_ANONYMOUS;
        
        // Skip nodes for which the k-anonymity property is known or which have insufficient utility
        NodeTrigger binaryTriggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };

        // We predictively tag the k-anonymity property and the insufficient utility property
        NodeTrigger binaryTriggerTag = new FLASHNodeTrigger(lattice){
            public boolean appliesTo(Node node) {
                
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_ANONYMOUS);
            }
            
            public void action(Lattice lattice, Node node) {

                // Tag k-anonymity
                if (node.hasProperty(Node.PROPERTY_K_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_K_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_K_ANONYMOUS);
                }
                
                // Tag insufficient utility
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_INSUFFICIENT_UTILITY);
                }
            }
        };
        
        // No evaluation
        NodeTrigger binaryTriggerSnapshotStore = History.STORAGE_TRIGGER_NON_ANONYMOUS;
        NodeTrigger binaryTriggerSnapshotEvict = History.EVICTION_TRIGGER_K_ANONYMOUS;
        NodeTrigger binaryTriggerCheck = new NodeTriggerInverse(binaryTriggerSkip);
        NodeTrigger binaryTriggerEvaluate = new NodeTriggerConstant(false);
        
        /* *******************************
         *  LINEAR PHASE
         *********************************/

        // We focus on the anonymity property
        int linearAnonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // We skip nodes for which the anonymity property is known, which are not k-anonymous,
        // or which have insufficient utility
        NodeTrigger linearTriggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };

        // No evaluation
        NodeTrigger linearTriggerSnapshotStore = History.STORAGE_TRIGGER_ALL;
        NodeTrigger linearTriggerSnapshotEvict = History.EVICTION_TRIGGER_CHECKED;
        NodeTrigger linearTriggerEvaluate = new NodeTriggerConstant(false);

        // We check nodes which have not been skipped
        NodeTrigger linearTriggerCheck = new NodeTriggerInverse(linearTriggerSkip);

        // We predictively tag the insufficient utility property
        NodeTrigger linearTriggerTag = new FLASHNodeTrigger(lattice){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS);
            }
            public void action(Lattice lattice, Node node) {
                lattice.setPropertyUpwards(node, false, Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };
        

        // Binary configuration
        FLASHConfiguration binaryConfiguration = new FLASHConfiguration(binaryAnonymityProperty,
                                                                        binaryTriggerTag,
                                                                        binaryTriggerCheck,
                                                                        binaryTriggerEvaluate,
                                                                        binaryTriggerSkip,
                                                                        binaryTriggerSnapshotStore,
                                                                        binaryTriggerSnapshotEvict);
        
        // Linear configuration
        FLASHConfiguration linearConfiguration = new FLASHConfiguration(linearAnonymityProperty,
                                                                        linearTriggerTag,
                                                                        linearTriggerCheck,
                                                                        linearTriggerEvaluate,
                                                                        linearTriggerSkip,
                                                                        linearTriggerSnapshotStore,
                                                                        linearTriggerSnapshotEvict);

        return new FLASHAlgorithmImpl(lattice, checker,strategy, binaryConfiguration, linearConfiguration);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullNone(final Lattice lattice,
                                                    final INodeChecker checker,
                                                    final FLASHStrategy strategy) {
        
        /* *******************************
         *  BINARY PHASE
         *********************************/

        // We focus on the anonymity property
        int binaryAnonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // Skip nodes for which the anonymity property is known
        NodeTrigger binaryTriggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We predictively tag the anonymity property
        NodeTrigger binaryTriggerTag = new FLASHNodeTrigger(binaryTriggerSkip, lattice){
            public void action(Lattice lattice, Node node) {
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_ANONYMOUS);
                }
            }
        };
        
        // No evaluation
        NodeTrigger binaryTriggerSnapshotStore = History.STORAGE_TRIGGER_NON_ANONYMOUS;
        NodeTrigger binaryTriggerSnapshotEvict = History.EVICTION_TRIGGER_ANONYMOUS;
        NodeTrigger binaryTriggerCheck = new NodeTriggerInverse(binaryTriggerSkip);
        NodeTrigger binaryTriggerEvaluate = new NodeTriggerConstant(false);
        
        /* *******************************
         *  LINEAR PHASE
         *********************************/

        // We focus on the anonymity property
        int linearAnonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // We skip nodes which have already been checked or which are not anonymous
        NodeTrigger linearTriggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_CHECKED) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        NodeTrigger linearTriggerSnapshotStore = History.STORAGE_TRIGGER_ALL;
        NodeTrigger linearTriggerSnapshotEvict = History.EVICTION_TRIGGER_CHECKED;
        
        // We evaluate nodes which have not been skipped, if the metric is independent
        NodeTrigger linearTriggerEvaluate = new NodeTrigger() {
            public boolean appliesTo(Node node) {
                return checker.getMetric().isIndependent() &&
                       !node.hasProperty(Node.PROPERTY_CHECKED) && 
                       !node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We check nodes which have not been skipped, if the metric is dependent
        NodeTrigger linearTriggerCheck = new NodeTrigger() {
            public boolean appliesTo(Node node) {
                return !checker.getMetric().isIndependent() &&
                       !node.hasProperty(Node.PROPERTY_CHECKED) && 
                       !node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We do not predictively tag anything 
        NodeTrigger linearTriggerTag = new NodeTriggerConstant(false);

        // Binary configuration
        FLASHConfiguration binaryConfiguration = new FLASHConfiguration(binaryAnonymityProperty,
                                                                        binaryTriggerTag,
                                                                        binaryTriggerCheck,
                                                                        binaryTriggerEvaluate,
                                                                        binaryTriggerSkip,
                                                                        binaryTriggerSnapshotStore,
                                                                        binaryTriggerSnapshotEvict);
        
        // Linear configuration
        FLASHConfiguration linearConfiguration = new FLASHConfiguration(linearAnonymityProperty,
                                                                        linearTriggerTag,
                                                                        linearTriggerCheck,
                                                                        linearTriggerEvaluate,
                                                                        linearTriggerSkip,
                                                                        linearTriggerSnapshotStore,
                                                                        linearTriggerSnapshotEvict);

        return new FLASHAlgorithmImpl(lattice, checker,strategy, binaryConfiguration, linearConfiguration);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullFull(Lattice lattice,
                                                    INodeChecker checker,
                                                    FLASHStrategy strategy) {
        
        // We focus on the anonymity property
        int anonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        // Skip nodes for which the anonymity property is known
        NodeTrigger triggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We predictively tag the anonymity property
        NodeTrigger triggerTag = new FLASHNodeTrigger(triggerSkip, lattice){
            public void action(Lattice lattice, Node node) {
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_ANONYMOUS);
                }
            }
        };
        
        // No evaluation
        NodeTrigger triggerSnapshotStore = History.STORAGE_TRIGGER_NON_ANONYMOUS;
        NodeTrigger triggerSnapshotEvict = History.EVICTION_TRIGGER_ANONYMOUS;
        NodeTrigger triggerEvaluate = new NodeTriggerConstant(false);
        NodeTrigger triggerCheck = new NodeTriggerInverse(triggerSkip);
        
        // Only one binary phase
        FLASHConfiguration binaryConfiguration = new FLASHConfiguration(anonymityProperty,
                                                                        triggerTag,
                                                                        triggerCheck,
                                                                        triggerEvaluate,
                                                                        triggerSkip,
                                                                        triggerSnapshotStore,
                                                                        triggerSnapshotEvict);
        
        FLASHConfiguration linearConfiguration = new FLASHConfiguration();
        
        return new FLASHAlgorithmImpl(lattice, checker,strategy, binaryConfiguration, linearConfiguration);
    }
}
