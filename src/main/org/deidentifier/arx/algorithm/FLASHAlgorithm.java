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

package org.deidentifier.arx.algorithm;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.framework.lattice.NodeAction;
import org.deidentifier.arx.framework.lattice.NodeAction.NodeActionConstant;
import org.deidentifier.arx.framework.lattice.NodeAction.NodeActionInverse;
import org.deidentifier.arx.framework.lattice.NodeAction.NodeActionOR;
import org.deidentifier.arx.metric.Metric;

/**
 * This class provides a static method for instantiating the FLASH algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHAlgorithm {

    /**
     * Monotonicity.
     */
    private static enum Monotonicity {
        
        /**  TODO */
        FULL,
        
        /**  TODO */
        PARTIAL,
        
        /**  TODO */
        NONE
    }

    /**
     * Creates a new instance of the FLASH algorithm.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    public static AbstractAlgorithm create(final Lattice lattice,
                                           final INodeChecker checker,
                                           final FLASHStrategy strategy) {

        // Init
        ARXConfigurationInternal config = checker.getConfiguration();
        Metric<?> metric = checker.getMetric();

        // NOTE:
        // - If we assume practical monotonicity then we assume
        // monotonicity for both criterion AND metric
        // - Without suppression we assume monotonicity for all criteria
        // - Without suppression we assume monotonicity for all metrics

        // Determine monotonicity of metric
        Monotonicity monotonicityMetric;
        if (metric.isMonotonic() || (config.getMaxOutliers() == 0d) || config.isPracticalMonotonicity()) {
            monotonicityMetric = Monotonicity.FULL;
        } else {
            monotonicityMetric = Monotonicity.NONE;
        }

        // First, determine whether the overall set of criteria is monotonic
        Monotonicity monotonicityCriteria = Monotonicity.FULL;
        for (PrivacyCriterion criterion : config.getCriteria()) {
            if (!(criterion.isMonotonic() || (config.getMaxOutliers() == 0d) || config.isPracticalMonotonicity())) {
                if (config.getMinimalGroupSize() != Integer.MAX_VALUE) {
                    monotonicityCriteria = Monotonicity.PARTIAL;
                } else {
                    monotonicityCriteria = Monotonicity.NONE;
                }
                break;
            }
        }

        // ******************************
        // CASE 1
        // ******************************
        if ((monotonicityCriteria == Monotonicity.FULL) && (monotonicityMetric == Monotonicity.FULL)) {
            return createFullFull(lattice, checker, strategy);
        }

        // ******************************
        // CASE 2
        // ******************************
        if ((monotonicityCriteria == Monotonicity.FULL) && (monotonicityMetric == Monotonicity.NONE)) {
            return createFullNone(lattice, checker, strategy);
        }

        // ******************************
        // CASE 3
        // ******************************
        if ((monotonicityCriteria == Monotonicity.PARTIAL) && (monotonicityMetric == Monotonicity.FULL)) {
            return createPartialFull(lattice, checker, strategy);
        }

        // ******************************
        // CASE 4
        // ******************************
        if ((monotonicityCriteria == Monotonicity.PARTIAL) && (monotonicityMetric == Monotonicity.NONE)) {
            return createPartialNone(lattice, checker, strategy);
        }

        // ******************************
        // CASE 5
        // ******************************
        if ((monotonicityCriteria == Monotonicity.NONE) && (monotonicityMetric == Monotonicity.FULL)) {
            return createNoneFull(lattice, checker, strategy);
        }

        // ******************************
        // CASE 6
        // ******************************
        if ((monotonicityCriteria == Monotonicity.NONE) && (monotonicityMetric == Monotonicity.NONE)) {
            return createNoneNone(lattice, checker, strategy);
        }

        throw new IllegalStateException("Oops");
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullFull(final Lattice lattice,
                                                    final INodeChecker checker,
                                                    final FLASHStrategy strategy) {

        // We focus on the anonymity property
        int anonymityProperty = Node.PROPERTY_ANONYMOUS;

        // Skip nodes for which the anonymity property is known
        NodeAction triggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We predictively tag the anonymity property
        NodeAction triggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_ANONYMOUS | Node.PROPERTY_SUCCESSORS_PRUNED);
                    lattice.setProperty(node, Node.PROPERTY_SUCCESSORS_PRUNED);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_ANONYMOUS);
                }
            }

            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // No evaluation
        NodeAction triggerEvaluate = new NodeActionConstant(false);
        NodeAction triggerCheck = new NodeActionInverse(triggerSkip);
        NodeAction triggerFireEvent = new NodeActionOR(triggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED);
            }
        };

        // Only one binary phase
        // Deactivate pruning due to lower bound as it increases number of checks needed
        FLASHConfiguration config = FLASHConfiguration.createBinaryPhaseConfiguration(new FLASHPhaseConfiguration(anonymityProperty,
                                                                                                                  triggerTag,
                                                                                                                  triggerCheck,
                                                                                                                  triggerEvaluate,
                                                                                                                  triggerSkip),
                                                                                      History.STORAGE_TRIGGER_NON_ANONYMOUS,
                                                                                      triggerFireEvent,
                                                                                      false);

        return new FLASHAlgorithmImpl(lattice, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullNone(final Lattice lattice,
                                                    final INodeChecker checker,
                                                    final FLASHStrategy strategy) {

        /* *******************************
         * BINARY PHASE
         * *******************************
         */

        // We focus on the anonymity property
        int binaryAnonymityProperty = Node.PROPERTY_ANONYMOUS;

        // Skip nodes for which the anonymity property is known
        NodeAction binaryTriggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We predictively tag the anonymity property
        NodeAction binaryTriggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_ANONYMOUS);
                }
            }

            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // No evaluation
        NodeAction binaryTriggerCheck = new NodeActionInverse(binaryTriggerSkip);
        NodeAction binaryTriggerEvaluate = new NodeActionConstant(false);

        /* *******************************
         * LINEAR PHASE
         * *******************************
         */

        // We focus on the anonymity property
        int linearAnonymityProperty = Node.PROPERTY_ANONYMOUS;

        // We skip nodes which are not anonymous or which have already been visited during the second phase
        NodeAction linearTriggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_VISITED);
            }
        };

        // We evaluate nodes which have not been skipped, if the metric is independent
        NodeAction linearTriggerEvaluate = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return checker.getMetric().isIndependent() &&
                       !node.hasProperty(Node.PROPERTY_CHECKED) &&
                       !node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // We check nodes which have not been skipped, if the metric is dependent
        NodeAction linearTriggerCheck = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return !checker.getMetric().isIndependent() &&
                       !node.hasProperty(Node.PROPERTY_CHECKED) &&
                       !node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // Mark nodes as already visited during the second phase
        NodeAction linearTriggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                lattice.setProperty(node, Node.PROPERTY_VISITED);
            }

            @Override
            public boolean appliesTo(Node node) {
                return true;
            }
        };

        // Fire event
        NodeAction triggerFireEvent = new NodeActionOR(linearTriggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED);
            }
        };

        // Two interwoven phases
        FLASHConfiguration config = FLASHConfiguration.createTwoPhaseConfiguration(new FLASHPhaseConfiguration(binaryAnonymityProperty,
                                                                                                               binaryTriggerTag,
                                                                                                               binaryTriggerCheck,
                                                                                                               binaryTriggerEvaluate,
                                                                                                               binaryTriggerSkip),
                                                                                   new FLASHPhaseConfiguration(linearAnonymityProperty,
                                                                                                               linearTriggerTag,
                                                                                                               linearTriggerCheck,
                                                                                                               linearTriggerEvaluate,
                                                                                                               linearTriggerSkip),
                                                                                   History.STORAGE_TRIGGER_ALL,
                                                                                   triggerFireEvent,
                                                                                   true);

        return new FLASHAlgorithmImpl(lattice, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createNoneFull(final Lattice lattice,
                                                    final INodeChecker checker,
                                                    final FLASHStrategy strategy) {

        // We focus on the anonymity property
        int anonymityProperty = Node.PROPERTY_ANONYMOUS;

        // We skip nodes for which the anonymity property is known or which have insufficient utility
        NodeAction triggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };

        // No evaluation
        NodeAction triggerEvaluate = new NodeActionConstant(false);
        NodeAction triggerCheck = new NodeActionInverse(triggerSkip);
        NodeAction triggerFireEvent = new NodeActionOR(triggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED);
            }
        };

        // We predictively tag nodes with insufficient utility because of the monotonic metric
        NodeAction triggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                lattice.setPropertyUpwards(node, false, Node.PROPERTY_INSUFFICIENT_UTILITY | Node.PROPERTY_SUCCESSORS_PRUNED);
                lattice.setProperty(node, Node.PROPERTY_SUCCESSORS_PRUNED);
            }

            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS);
            }
        };

        // Only one linear phase
        FLASHConfiguration config = FLASHConfiguration.createLinearPhaseConfiguration(new FLASHPhaseConfiguration(anonymityProperty,
                                                                                                                  triggerTag,
                                                                                                                  triggerCheck,
                                                                                                                  triggerEvaluate,
                                                                                                                  triggerSkip),
                                                                                      History.STORAGE_TRIGGER_ALL,
                                                                                      triggerFireEvent,
                                                                                      true);

        return new FLASHAlgorithmImpl(lattice, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
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
        NodeAction triggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };

        // No evaluation, no tagging
        NodeAction triggerEvaluate = new NodeActionConstant(false);
        NodeAction triggerCheck = new NodeActionInverse(triggerSkip);
        NodeAction triggerTag = new NodeActionConstant(false);
        NodeAction triggerFireEvent = new NodeActionOR(triggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED);
            }
        };

        // Only one linear phase
        FLASHConfiguration config = FLASHConfiguration.createLinearPhaseConfiguration(new FLASHPhaseConfiguration(anonymityProperty,
                                                                                                                  triggerTag,
                                                                                                                  triggerCheck,
                                                                                                                  triggerEvaluate,
                                                                                                                  triggerSkip),
                                                                                      History.STORAGE_TRIGGER_ALL,
                                                                                      triggerFireEvent,
                                                                                      true);

        return new FLASHAlgorithmImpl(lattice, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createPartialFull(final Lattice lattice,
                                                       final INodeChecker checker,
                                                       final FLASHStrategy strategy) {
        /* *******************************
         * BINARY PHASE
         * *******************************
         */

        // We focus on the k-anonymity property
        int binaryAnonymityProperty = Node.PROPERTY_K_ANONYMOUS;

        // Skip nodes for which the k-anonymity property is known or which have insufficient utility
        NodeAction binaryTriggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };

        // We predictively tag the k-anonymity property and the insufficient utility property
        NodeAction binaryTriggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                // Tag k-anonymity
                if (node.hasProperty(Node.PROPERTY_K_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_K_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_K_ANONYMOUS);
                }

                // Tag insufficient utility
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_INSUFFICIENT_UTILITY | Node.PROPERTY_SUCCESSORS_PRUNED);
                    lattice.setProperty(node, Node.PROPERTY_SUCCESSORS_PRUNED);
                }
            }

            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_ANONYMOUS);
            }
        };

        // No evaluation
        NodeAction binaryTriggerCheck = new NodeActionInverse(binaryTriggerSkip);
        NodeAction binaryTriggerEvaluate = new NodeActionConstant(false);

        /* *******************************
         * LINEAR PHASE
         * *******************************
         */

        // We focus on the anonymity property
        int linearAnonymityProperty = Node.PROPERTY_ANONYMOUS;

        // We skip nodes for which the anonymity property is known, which are not k-anonymous,
        // or which have insufficient utility
        NodeAction linearTriggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_VISITED) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY);
            }
        };

        // No evaluation
        NodeAction linearTriggerEvaluate = new NodeActionConstant(false);

        // We check nodes which have not been skipped
        NodeAction linearTriggerCheck = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return !node.hasProperty(Node.PROPERTY_VISITED) &&
                       !node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS) &&
                       !node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY) &&
                       !node.hasProperty(Node.PROPERTY_CHECKED);
            }
        };

        // We predictively tag the insufficient utility property
        NodeAction linearTriggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                lattice.setProperty(node, Node.PROPERTY_VISITED);
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_INSUFFICIENT_UTILITY | Node.PROPERTY_SUCCESSORS_PRUNED);
                    lattice.setProperty(node, Node.PROPERTY_SUCCESSORS_PRUNED);
                }
            }

            @Override
            public boolean appliesTo(Node node) {
                return true;
            }
        };

        // Fire event
        NodeAction triggerFireEvent = new NodeActionOR(linearTriggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED);
            }
        };

        // Two interwoven phases
        FLASHConfiguration config = FLASHConfiguration.createTwoPhaseConfiguration(new FLASHPhaseConfiguration(binaryAnonymityProperty,
                                                                                                               binaryTriggerTag,
                                                                                                               binaryTriggerCheck,
                                                                                                               binaryTriggerEvaluate,
                                                                                                               binaryTriggerSkip),
                                                                                   new FLASHPhaseConfiguration(linearAnonymityProperty,
                                                                                                               linearTriggerTag,
                                                                                                               linearTriggerCheck,
                                                                                                               linearTriggerEvaluate,
                                                                                                               linearTriggerSkip),
                                                                                   History.STORAGE_TRIGGER_ALL,
                                                                                   triggerFireEvent,
                                                                                   true);

        return new FLASHAlgorithmImpl(lattice, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createPartialNone(final Lattice lattice,
                                                       final INodeChecker checker,
                                                       final FLASHStrategy strategy) {
        /* *******************************
         * BINARY PHASE
         * *******************************
         */

        // We focus on the k-anonymity property
        int binaryAnonymityProperty = Node.PROPERTY_K_ANONYMOUS;

        // Skip nodes for which the k-anonymity property is known
        NodeAction binaryTriggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
            }
        };

        // We predictively tag the k-anonymity property
        NodeAction binaryTriggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                if (node.hasProperty(Node.PROPERTY_K_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_K_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_K_ANONYMOUS);
                }
            }

            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_K_ANONYMOUS) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
            }
        };

        // No evaluation
        NodeAction binaryTriggerCheck = new NodeActionInverse(binaryTriggerSkip);
        NodeAction binaryTriggerEvaluate = new NodeActionConstant(false);

        /* *******************************
         * LINEAR PHASE
         * *******************************
         */

        // We focus on the anonymity property
        int linearAnonymityProperty = Node.PROPERTY_ANONYMOUS;

        // We skip nodes for which are not k-anonymous and which have not been visited yet in the 2nd phase
        NodeAction linearTriggerSkip = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_VISITED) ||
                       node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
            }
        };

        // No evaluation
        NodeAction linearTriggerEvaluate = new NodeActionConstant(false);

        // We check nodes which are k-anonymous and have not been checked already
        NodeAction linearTriggerCheck = new NodeAction() {
            @Override
            public boolean appliesTo(Node node) {
                return !node.hasProperty(Node.PROPERTY_CHECKED) &&
                       !node.hasProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
            }
        };

        // Mark nodes as already visited during the second phase
        NodeAction linearTriggerTag = new NodeAction() {
            @Override
            public void action(Node node) {
                lattice.setProperty(node, Node.PROPERTY_VISITED);
            }

            @Override
            public boolean appliesTo(Node node) {
                return true;
            }
        };

        // Fire event
        NodeAction triggerFireEvent = new NodeActionOR(linearTriggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED);
            }
        };

        // Two interwoven phases
        FLASHConfiguration config = FLASHConfiguration.createTwoPhaseConfiguration(new FLASHPhaseConfiguration(binaryAnonymityProperty,
                                                                                                               binaryTriggerTag,
                                                                                                               binaryTriggerCheck,
                                                                                                               binaryTriggerEvaluate,
                                                                                                               binaryTriggerSkip),
                                                                                   new FLASHPhaseConfiguration(linearAnonymityProperty,
                                                                                                               linearTriggerTag,
                                                                                                               linearTriggerCheck,
                                                                                                               linearTriggerEvaluate,
                                                                                                               linearTriggerSkip),
                                                                                   History.STORAGE_TRIGGER_ALL,
                                                                                   triggerFireEvent,
                                                                                   true);

        return new FLASHAlgorithmImpl(lattice, checker, strategy, config);
    }
}
