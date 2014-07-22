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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Semantics of method name: monotonicity of criteria + monotonicity of metric
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    private static AbstractAlgorithm createFullNone(Lattice lattice,
                                                    INodeChecker checker,
                                                    FLASHStrategy strategy) {
        // TODO Auto-generated method stub
        return null;
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
        
        int anonymityProperty = Node.PROPERTY_ANONYMOUS;
        
        NodeTrigger triggerSnapshotStore = History.STORAGE_TRIGGER_NON_ANONYMOUS;
        
        NodeTrigger triggerSnapshotEvict = History.EVICTION_TRIGGER_ANONYMOUS;
        
        NodeTrigger triggerSkip = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };
        
        NodeTrigger triggerEvaluate = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return false;
            }
        };
        
        NodeTrigger triggerCheck = new NodeTrigger(){
            public boolean appliesTo(Node node) {
                return !node.hasProperty(Node.PROPERTY_ANONYMOUS) && 
                       !node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
        };
        
        NodeTrigger triggerTag = new FLASHNodeTrigger(lattice){
            public boolean appliesTo(Node node) {
                return node.hasProperty(Node.PROPERTY_ANONYMOUS) || 
                       node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
            }
            public void action(Lattice lattice, Node node) {
                if (node.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    lattice.setPropertyUpwards(node, false, Node.PROPERTY_ANONYMOUS);
                } else {
                    lattice.setPropertyDownwards(node, false, Node.PROPERTY_NOT_ANONYMOUS);
                }
            }
        };
        
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
