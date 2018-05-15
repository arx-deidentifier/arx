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

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * A class that estimates information loss within a generalization lattice. Method:<br>
 * <br>
 * Minimum:<br>
 *    -   Anonymous &  monotonic: max(push(min), optimum)<br>
 *    -   Anonymous & !monotonic: push(lower)<br>
 *    -  !Anonymous &  monotonic: max(push(min), optimum)<br>
 *    -  !Anonymous & !monotonic: push(lower)<br>
 * <br>
 * Maximum:<br>
 *    -   Anonymous &  monotonic: push(max)<br>
 *    -   Anonymous & !monotonic: metric.max<br>
 *    -  !Anonymous &  monotonic: push(max)<br>
 *    -  !Anonymous & !monotonic: metric.max<br>
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class UtilityEstimator {

    /** The lattice. */
    private ARXLattice           lattice;
    
    /** The metric. */
    private Metric<?>            metric;

    /** Additional fields. */
    private InformationLoss<?>[] minimumAnonymous;
    
    /** Additional fields. */
    private InformationLoss<?>[] minimumNonAnonymous;
    
    /** Additional fields. */
    private InformationLoss<?>[] maximumAnonymous;
    
    /** Additional fields. */
    private InformationLoss<?>[] maximumNonAnonymous;
    
    /** Additional fields. */
    private InformationLoss<?>[] lowerBound;

    /** Monotonicity. */
    private final boolean        monotonicAnonymous;
    
    /** Monotonicity. */
    private final boolean        monotonicNonAnonymous;

    /** Maximum/minimum. */
    private InformationLoss<?>   globalMinimum;
    
    /** Maximum/minimum. */
    private InformationLoss<?>   globalMaximum;
    
    /**
     * Creates a new estimation process for a lattice.
     *
     * @param lattice
     * @param metric
     * @param monotonicAnonymous
     * @param monotonicNonAnonymous
     */
    UtilityEstimator(ARXLattice lattice, Metric<?> metric, boolean monotonicAnonymous, boolean monotonicNonAnonymous) {
        
        // Init
        this.lattice = lattice;
        this.metric = metric;
        this.minimumAnonymous = new InformationLoss<?>[this.lattice.getSize()];
        this.minimumNonAnonymous = new InformationLoss<?>[this.lattice.getSize()];
        this.maximumAnonymous = new InformationLoss<?>[this.lattice.getSize()];
        this.maximumNonAnonymous = new InformationLoss<?>[this.lattice.getSize()];
        this.lowerBound = new InformationLoss<?>[this.lattice.getSize()];
        this.monotonicAnonymous = monotonicAnonymous;
        this.monotonicNonAnonymous = monotonicNonAnonymous;
        
        // Make sure that all nodes have an identifier
        int id = 0;
        for (ARXNode[] level : this.lattice.getLevels()) {
            for (ARXNode node : level) {
                node.setId(id++);
            }
        }
    }
    
    /**
     * Estimate maximum information loss.
     */
    private void estimateMax() {

        // Prepare
        initializeTopDown(lattice.getTop());
        setMaximum(lattice.getTop());
        this.globalMaximum = lattice.getTop().getHighestScore();
        
        // Pull
        ARXNode[][] levels = lattice.getLevels();
        for (int i = levels.length-2; i >= 0; i--) {
            final ARXNode[] level = levels[i];
            for (final ARXNode node : level) {
                pullTopDown(node);
                setMaximum(node);
                this.globalMaximum = max(this.globalMaximum, node.getHighestScore());
            }
        }
    }

    /**
     * Estimate minimum information loss.
     */
    private void estimateMin() {

        // Prepare
        initializeBottomUp(lattice.getBottom());
        setMinimum(lattice.getBottom());
        this.globalMinimum = lattice.getBottom().getLowestScore();
        
        // Pull
        ARXNode[][] levels = lattice.getLevels();
        for (int i = 1; i < levels.length; i++) {
            final ARXNode[] level = levels[i];
            for (final ARXNode node : level) {
                pullBottomUp(node);
                setMinimum(node);
                this.globalMinimum = min(this.globalMinimum, node.getLowestScore());
            }
        }
    }

    /**
     * Returns the value if != null, the default otherwise.
     * @param value
     * @param _default
     * @return
     */
    private InformationLoss<?> getValueOrDefault(InformationLoss<?> value, InformationLoss<?> _default) {
        return value != null ? value : _default;
    }

    /**
     * Initializes the bottom node.
     *
     * @param node
     */
    private void initializeBottomUp(ARXNode node) {

        int id = node.getId();
        Anonymity nodeAnonymity = node.getAnonymity();
        InformationLoss<?> nodeMin = node.getLowestScore();
        InformationLoss<?> metricMin = metric.createInstanceOfLowestScore();

        lowerBound[id] = getValueOrDefault(node.getLowerBound(), metricMin);
        
        if (nodeAnonymity == Anonymity.ANONYMOUS && monotonicAnonymous) {
            minimumAnonymous[id] = getValueOrDefault(nodeMin, metricMin);
            minimumNonAnonymous[id] = metricMin;
        } else if (nodeAnonymity == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            minimumNonAnonymous[id] = getValueOrDefault(nodeMin, metricMin);
            minimumAnonymous[id] = metricMin;
        } else {
            minimumAnonymous[id] = metricMin;
            minimumNonAnonymous[id] = metricMin;
        }
    }

    /**
     * Initializes the top node.
     *
     * @param node
     */
    private void initializeTopDown(ARXNode node) {
        
        int id = node.getId();
        Anonymity nodeAnonymity = node.getAnonymity();
        InformationLoss<?> nodeMax = node.getHighestScore();
        InformationLoss<?> metricMax = metric.createInstanceOfHighestScore();

        if (nodeAnonymity == Anonymity.ANONYMOUS && monotonicAnonymous) {
            maximumAnonymous[id] = getValueOrDefault(nodeMax, metricMax);
            maximumNonAnonymous[id] = metricMax;
        } else if (nodeAnonymity == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            maximumNonAnonymous[id] = getValueOrDefault(nodeMax, metricMax);
            maximumAnonymous[id] = metricMax;
        } else {
            maximumAnonymous[id] = metricMax;
            maximumNonAnonymous[id] = metricMax;
        }
    }

    /**
     * Returns the max of both, handles null values.
     *
     * @param first
     * @param second
     * @return
     */
    private InformationLoss<?> max(InformationLoss<?> first, InformationLoss<?> second) {
        return (first == null) ? second :
               (second == null) ? first :
               (first.compareTo(second) < 0) ? second : first;    
    }

    /**
     * Returns the min of both, handles null values.
     *
     * @param first
     * @param second
     * @return
     */
    private InformationLoss<?> min(InformationLoss<?> first, InformationLoss<?> second) {
        return (first == null) ? second :
               (second == null) ? first :
               (first.compareTo(second) < 0) ? first : second;    
    }

    /**
     * Propagate bottom up.
     *
     * @param node
     */
    private void pullBottomUp(ARXNode node) {
        
        int id = node.getId();
        
        // Pull all values
        for (ARXNode pre : node.getPredecessors()) {
            int preId = pre.getId();
            pullMax(minimumAnonymous, id, preId);
            pullMax(minimumNonAnonymous, id, preId);
            pullMax(lowerBound, id, preId);
        }
        
        // Lower bound can always be replaced
        if (node.getLowerBound() != null) {
            lowerBound[id] = max(lowerBound[id], node.getLowerBound());
        }
        
        // Check if values can be replaced
        if (node.getLowestScore() != null) {
            if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
                minimumAnonymous[id] = max(minimumAnonymous[id], node.getLowestScore());
            } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
                minimumNonAnonymous[id] = max(minimumNonAnonymous[id], node.getLowestScore());
            }
        }
    }

    /**
     * Pulls a specific property and retains the value using a maximum aggregate function.
     *
     * @param array
     * @param target
     * @param source
     */
    private void pullMax(InformationLoss<?>[] array, int target, int source) {
        array[target] = array[target] == null ? array[source] : max(array[target], array[source]);
    }

    /**
     * Pulls a specific property and retains the value using a minimum aggregate function.
     *
     * @param array
     * @param target
     * @param source
     */
    private void pullMin(InformationLoss<?>[] array, int target, int source) {
        array[target] = array[target] == null ? array[source] : min(array[target], array[source]);
    }
    
    
    /**
     * Propagate top down.
     *
     * @param node
     */
    private void pullTopDown(ARXNode node) {
        
        int id = node.getId();
        
        // Pull all values
        for (ARXNode succ : node.getSuccessors()) {
            int succId = succ.getId();
            pullMin(maximumAnonymous, id, succId);
            pullMin(maximumNonAnonymous, id, succId);
        }
        
        // Check if values can be replaced
        if (node.getHighestScore() != null) {
            if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
                maximumAnonymous[id] = min(maximumAnonymous[id], node.getHighestScore());
            } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
                maximumNonAnonymous[id] = min(maximumNonAnonymous[id], node.getHighestScore());
            }
        }
    }

    /**
     * Selects a maximum for the given node.
     *
     * @param node
     */
    private void setMaximum(ARXNode node) {
    
        // If we already know everything, abort
        if (node.getLowestScore() != null && 
            node.getHighestScore() != null && 
            node.getLowestScore().compareTo(node.getHighestScore())==0){
            return;
        }
        
        // Check if values can be replaced
        InformationLoss<?> minimalMaximum = null;
        if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
            minimalMaximum = min(node.getHighestScore(), maximumAnonymous[node.getId()]);
        } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            minimalMaximum = min(node.getHighestScore(), maximumNonAnonymous[node.getId()]);
        } else {
            minimalMaximum = min(node.getHighestScore(), metric.createInstanceOfHighestScore());
        }
        
        // Set
        node.access().setHighestScore(minimalMaximum);
    }

    /**
     * Selects a minimum for the given node.
     *
     * @param node
     */
    private void setMinimum(ARXNode node) {
    
        // If we already know everything, abort
        if (node.getLowestScore() != null && 
            node.getHighestScore() != null && 
            node.getLowestScore().compareTo(node.getHighestScore())==0){
            return;
        }
        
        // We can always use the lower bound
        InformationLoss<?> maximalMinimum = max(node.getLowestScore(), lowerBound[node.getId()]);
        
        // Check if values can be replaced
        if (node.getAnonymity() == Anonymity.ANONYMOUS) {
            
            // We can always use the optimum as a minimum for anonymous nodes
            maximalMinimum = max(maximalMinimum, lattice.getOptimum().getLowestScore());
            
            if (monotonicAnonymous) {
                maximalMinimum = max(maximalMinimum, minimumAnonymous[node.getId()]);
            }
        } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            maximalMinimum = max(maximalMinimum, minimumNonAnonymous[node.getId()]);
        }
        
        // Set
        node.access().setLowestScore(maximalMinimum);
    }

    /**
     * Implements the estimation process.
     */
    void estimate() {
        estimateMin();
        estimateMax();    
    }

    /**
     * @return the globalMaximum
     */
    InformationLoss<?> getGlobalMaximum() {
        return globalMaximum;
    }

    /**
     * @return the globalMinimum
     */
    InformationLoss<?> getGlobalMinimum() {
        return globalMinimum;
    }
}
