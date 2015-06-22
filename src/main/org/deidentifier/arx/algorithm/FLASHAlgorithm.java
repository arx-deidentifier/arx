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
import org.deidentifier.arx.algorithm.FLASHPhaseConfiguration.PhaseAnonymityProperty;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.DependentAction;
import org.deidentifier.arx.framework.lattice.DependentAction.NodeActionConstant;
import org.deidentifier.arx.framework.lattice.DependentAction.NodeActionInverse;
import org.deidentifier.arx.framework.lattice.DependentAction.NodeActionOR;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
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
        
        /**  Fully monotonic */
        FULL,
        
        /**  Partially monotonic */
        PARTIAL,
        
        /**  Non-monotonic */
        NONE
    }

    /**
     * Creates a new instance of the FLASH algorithm.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    public static AbstractAlgorithm create(final SolutionSpace solutionSpace,
                                           final NodeChecker checker,
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
            return createFullFull(solutionSpace, checker, strategy);
        }

        // ******************************
        // CASE 2
        // ******************************
        if ((monotonicityCriteria == Monotonicity.FULL) && (monotonicityMetric == Monotonicity.NONE)) {
            return createFullNone(solutionSpace, checker, strategy);
        }

        // ******************************
        // CASE 3
        // ******************************
        if ((monotonicityCriteria == Monotonicity.PARTIAL) && (monotonicityMetric == Monotonicity.FULL)) {
            return createPartialFull(solutionSpace, checker, strategy);
        }

        // ******************************
        // CASE 4
        // ******************************
        if ((monotonicityCriteria == Monotonicity.PARTIAL) && (monotonicityMetric == Monotonicity.NONE)) {
            return createPartialNone(solutionSpace, checker, strategy);
        }

        // ******************************
        // CASE 5
        // ******************************
        if ((monotonicityCriteria == Monotonicity.NONE) && (monotonicityMetric == Monotonicity.FULL)) {
            return createNoneFull(solutionSpace, checker, strategy);
        }

        // ******************************
        // CASE 6
        // ******************************
        if ((monotonicityCriteria == Monotonicity.NONE) && (monotonicityMetric == Monotonicity.NONE)) {
            return createNoneNone(solutionSpace, checker, strategy);
        }

        throw new IllegalStateException("Oops");
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullFull(final SolutionSpace solutionSpace,
                                                    final NodeChecker checker,
                                                    final FLASHStrategy strategy) {

        // We focus on the anonymity property
        PhaseAnonymityProperty anonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // Skip nodes for which the anonymity property is known
        DependentAction triggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // We predictively tag the anonymity property
        DependentAction triggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                if (node.hasProperty(solutionSpace.getPropertyAnonymous())) {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyAnonymous());
                    node.setPropertyToNeighbours(solutionSpace.getPropertySuccessorsPruned());
                    node.setProperty( solutionSpace.getPropertySuccessorsPruned());
                } else {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyNotAnonymous());
                }
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // No evaluation
        DependentAction triggerEvaluate = new NodeActionConstant(false);
        DependentAction triggerCheck = new NodeActionInverse(triggerSkip);
        DependentAction triggerFireEvent = new NodeActionOR(triggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertySuccessorsPruned());
            }
        };

        // Only one binary phase
        // Deactivate pruning due to lower bound as it increases number of checks needed
        FLASHConfiguration config = FLASHConfiguration.createBinaryPhaseConfiguration(new FLASHPhaseConfiguration(anonymityProperty,
                                                                                                                  triggerTag,
                                                                                                                  triggerCheck,
                                                                                                                  triggerEvaluate,
                                                                                                                  triggerSkip),
                                                                                      triggerFireEvent,
                                                                                      StorageStrategy.NON_ANONYMOUS,
                                                                                      false,
                                                                                      true);

        return new FLASHAlgorithmImpl(solutionSpace, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullNone(final SolutionSpace solutionSpace,
                                                    final NodeChecker checker,
                                                    final FLASHStrategy strategy) {

        /* *******************************
         * BINARY PHASE
         * *******************************
         */

        // We focus on the anonymity property
        PhaseAnonymityProperty binaryAnonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // Skip nodes for which the anonymity property is known
        DependentAction binaryTriggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // We predictively tag the anonymity property
        DependentAction binaryTriggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                if (node.hasProperty(solutionSpace.getPropertyAnonymous())) {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyAnonymous());
                } else {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyNotAnonymous());
                }
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // No evaluation
        DependentAction binaryTriggerCheck = new NodeActionInverse(binaryTriggerSkip);
        DependentAction binaryTriggerEvaluate = new NodeActionConstant(false);

        /* *******************************
         * LINEAR PHASE
         * *******************************
         */

        // We focus on the anonymity property
        PhaseAnonymityProperty linearAnonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // We skip nodes which are not anonymous or which have already been visited during the second phase
        DependentAction linearTriggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyNotAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyVisited());
            }
        };

        // We evaluate nodes which have not been skipped, if the metric is independent
        DependentAction linearTriggerEvaluate = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return checker.getMetric().isIndependent() &&
                       !node.hasProperty(solutionSpace.getPropertyChecked()) &&
                       !node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // We check nodes which have not been skipped, if the metric is dependent
        DependentAction linearTriggerCheck = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return !checker.getMetric().isIndependent() &&
                       !node.hasProperty(solutionSpace.getPropertyChecked()) &&
                       !node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // Mark nodes as already visited during the second phase
        DependentAction linearTriggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                node.setProperty(solutionSpace.getPropertyVisited());
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return true;
            }
        };

        // Fire event
        DependentAction triggerFireEvent = new NodeActionOR(linearTriggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertySuccessorsPruned());
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
                                                                                   triggerFireEvent,
                                                                                   StorageStrategy.ALL,
                                                                                   true,
                                                                                   true);

        return new FLASHAlgorithmImpl(solutionSpace, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createNoneFull(final SolutionSpace solutionSpace,
                                                    final NodeChecker checker,
                                                    final FLASHStrategy strategy) {

        // We focus on the anonymity property
        PhaseAnonymityProperty anonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // We skip nodes for which the anonymity property is known or which have insufficient utility
        DependentAction triggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyInsufficientUtility());
            }
        };

        // No evaluation
        DependentAction triggerEvaluate = new NodeActionConstant(false);
        DependentAction triggerCheck = new NodeActionInverse(triggerSkip);
        DependentAction triggerFireEvent = new NodeActionOR(triggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertySuccessorsPruned());
            }
        };

        // We predictively tag nodes with insufficient utility because of the monotonic metric
        DependentAction triggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                node.setPropertyToNeighbours(solutionSpace.getPropertyInsufficientUtility());
                node.setPropertyToNeighbours(solutionSpace.getPropertySuccessorsPruned());
                node.setProperty(solutionSpace.getPropertySuccessorsPruned());
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous());
            }
        };

        // Only one linear phase
        FLASHConfiguration config = FLASHConfiguration.createLinearPhaseConfiguration(new FLASHPhaseConfiguration(anonymityProperty,
                                                                                                                  triggerTag,
                                                                                                                  triggerCheck,
                                                                                                                  triggerEvaluate,
                                                                                                                  triggerSkip),
                                                                                      triggerFireEvent,
                                                                                      StorageStrategy.ALL,
                                                                                      true,
                                                                                      false);

        return new FLASHAlgorithmImpl(solutionSpace, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createNoneNone(final SolutionSpace solutionSpace,
                                                    NodeChecker checker,
                                                    FLASHStrategy strategy) {

        // We focus on the anonymity property
        PhaseAnonymityProperty anonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // Skip nodes for which the anonymity property is known
        DependentAction triggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotAnonymous());
            }
        };

        // No evaluation, no tagging
        DependentAction triggerEvaluate = new NodeActionConstant(false);
        DependentAction triggerCheck = new NodeActionInverse(triggerSkip);
        DependentAction triggerTag = new NodeActionConstant(false);
        DependentAction triggerFireEvent = new NodeActionOR(triggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertySuccessorsPruned());
            }
        };

        // Only one linear phase
        FLASHConfiguration config = FLASHConfiguration.createLinearPhaseConfiguration(new FLASHPhaseConfiguration(anonymityProperty,
                                                                                                                  triggerTag,
                                                                                                                  triggerCheck,
                                                                                                                  triggerEvaluate,
                                                                                                                  triggerSkip),
                                                                                      triggerFireEvent,
                                                                                      StorageStrategy.ALL,
                                                                                      true,
                                                                                      false);

        return new FLASHAlgorithmImpl(solutionSpace, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createPartialFull(final SolutionSpace solutionSpace,
                                                       final NodeChecker checker,
                                                       final FLASHStrategy strategy) {
        /* *******************************
         * BINARY PHASE
         * *******************************
         */

        // We focus on the k-anonymity property
        PhaseAnonymityProperty binaryAnonymityProperty = PhaseAnonymityProperty.K_ANONYMITY;

        // Skip nodes for which the k-anonymity property is known or which have insufficient utility
        DependentAction binaryTriggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyInsufficientUtility());
            }
        };

        // We predictively tag the k-anonymity property and the insufficient utility property
        DependentAction binaryTriggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                // Tag k-anonymity
                if (node.hasProperty(solutionSpace.getPropertyKAnonymous())) {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyKAnonymous());
                } else {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyNotKAnonymous());
                }

                // Tag insufficient utility
                if (node.hasProperty(solutionSpace.getPropertyAnonymous())) {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyInsufficientUtility());
                    node.setPropertyToNeighbours(solutionSpace.getPropertySuccessorsPruned());
                    node.setProperty(solutionSpace.getPropertySuccessorsPruned());
                }
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyAnonymous());
            }
        };

        // No evaluation
        DependentAction binaryTriggerCheck = new NodeActionInverse(binaryTriggerSkip);
        DependentAction binaryTriggerEvaluate = new NodeActionConstant(false);

        /* *******************************
         * LINEAR PHASE
         * *******************************
         */

        // We focus on the anonymity property
        PhaseAnonymityProperty linearAnonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // We skip nodes for which the anonymity property is known, which are not k-anonymous,
        // or which have insufficient utility
        DependentAction linearTriggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyVisited()) ||
                       node.hasProperty(solutionSpace.getPropertyNotKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyInsufficientUtility());
            }
        };

        // No evaluation
        DependentAction linearTriggerEvaluate = new NodeActionConstant(false);

        // We check nodes which have not been skipped
        DependentAction linearTriggerCheck = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return !node.hasProperty(solutionSpace.getPropertyVisited()) &&
                       !node.hasProperty(solutionSpace.getPropertyNotKAnonymous()) &&
                       !node.hasProperty(solutionSpace.getPropertyInsufficientUtility()) &&
                       !node.hasProperty(solutionSpace.getPropertyChecked());
            }
        };

        // We predictively tag the insufficient utility property
        DependentAction linearTriggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                node.setProperty(solutionSpace.getPropertyVisited());
                if (node.hasProperty(solutionSpace.getPropertyAnonymous())) {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyInsufficientUtility());
                    node.setPropertyToNeighbours(solutionSpace.getPropertySuccessorsPruned());
                    node.setProperty(solutionSpace.getPropertySuccessorsPruned());
                }
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return true;
            }
        };

        // Fire event
        DependentAction triggerFireEvent = new NodeActionOR(linearTriggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertySuccessorsPruned());
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
                                                                                   triggerFireEvent,
                                                                                   StorageStrategy.ALL,
                                                                                   true,
                                                                                   false);

        return new FLASHAlgorithmImpl(solutionSpace, checker, strategy, config);
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createPartialNone(final SolutionSpace solutionSpace,
                                                       final NodeChecker checker,
                                                       final FLASHStrategy strategy) {
        /* *******************************
         * BINARY PHASE
         * *******************************
         */

        // We focus on the k-anonymity property
        PhaseAnonymityProperty binaryAnonymityProperty = PhaseAnonymityProperty.K_ANONYMITY;

        // Skip nodes for which the k-anonymity property is known
        DependentAction binaryTriggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotKAnonymous());
            }
        };

        // We predictively tag the k-anonymity property
        DependentAction binaryTriggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                if (node.hasProperty(solutionSpace.getPropertyKAnonymous())) {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyKAnonymous());
                } else {
                    node.setPropertyToNeighbours(solutionSpace.getPropertyNotKAnonymous());
                }
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyKAnonymous()) ||
                       node.hasProperty(solutionSpace.getPropertyNotKAnonymous());
            }
        };

        // No evaluation
        DependentAction binaryTriggerCheck = new NodeActionInverse(binaryTriggerSkip);
        DependentAction binaryTriggerEvaluate = new NodeActionConstant(false);

        /* *******************************
         * LINEAR PHASE
         * *******************************
         */

        // We focus on the anonymity property
        PhaseAnonymityProperty linearAnonymityProperty = PhaseAnonymityProperty.ANONYMITY;

        // We skip nodes for which are not k-anonymous and which have not been visited yet in the 2nd phase
        DependentAction linearTriggerSkip = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertyVisited()) ||
                       node.hasProperty(solutionSpace.getPropertyNotKAnonymous());
            }
        };

        // No evaluation
        DependentAction linearTriggerEvaluate = new NodeActionConstant(false);

        // We check nodes which are k-anonymous and have not been checked already
        DependentAction linearTriggerCheck = new DependentAction() {
            @Override
            public boolean appliesTo(Transformation node) {
                return !node.hasProperty(solutionSpace.getPropertyChecked()) &&
                       !node.hasProperty(solutionSpace.getPropertyNotKAnonymous());
            }
        };

        // Mark nodes as already visited during the second phase
        DependentAction linearTriggerTag = new DependentAction() {
            @Override
            public void action(Transformation node) {
                node.setProperty(solutionSpace.getPropertyVisited());
            }

            @Override
            public boolean appliesTo(Transformation node) {
                return true;
            }
        };

        // Fire event
        DependentAction triggerFireEvent = new NodeActionOR(linearTriggerSkip) {
            @Override
            protected boolean additionalConditionAppliesTo(Transformation node) {
                return node.hasProperty(solutionSpace.getPropertySuccessorsPruned());
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
                                                                                   triggerFireEvent,
                                                                                   StorageStrategy.ALL,
                                                                                   true,
                                                                                   false);

        return new FLASHAlgorithmImpl(solutionSpace, checker, strategy, config);
    }
}
