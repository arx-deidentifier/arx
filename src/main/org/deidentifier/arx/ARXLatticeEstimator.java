/*
 * ARX: Powerful Data Anonymization
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
package org.deidentifier.arx;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * A class that estimates information loss within a generalization lattice
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class ARXLatticeEstimator {

    /** The lattice */
    private ARXLattice           lattice;
    /** The metric */
    private Metric<?>            metric;

    /** Additional fields */
    private InformationLoss<?>[] minimumAnonymous;
    /** Additional fields */
    private InformationLoss<?>[] minimumNonAnonymous;
    /** Additional fields */
    private InformationLoss<?>[] maximumAnonymous;
    /** Additional fields */
    private InformationLoss<?>[] maximumNonAnonymous;
    /** Additional fields */
    private InformationLoss<?>[] lowerBound;

    /** Monotonicity */
    private final boolean        monotonicAnonymous;
    /** Monotonicity */
    private final boolean        monotonicNonAnonymous;

    /** Maximum/minimum*/
    private InformationLoss<?>   globalMinimum;
    /** Maximum/minimum*/
    private InformationLoss<?>   globalMaximum;
    
    /**
     * Creates a new estimation process for a lattice
     * 
     * @param lattice
     * @param metric
     * @param monotonicAnonymous
     * @param monotonicNonAnonymous
     */
    ARXLatticeEstimator(ARXLattice lattice, Metric<?> metric, boolean monotonicAnonymous, boolean monotonicNonAnonymous) {
        
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
        if (this.lattice.getBottom().getId() == null) {
            int id = 0;
            for (ARXNode[] level : this.lattice.getLevels()) {
                for (ARXNode node : level) {
                    node.setId(id++);
                }
            }
        }
    }
    
    /**
     * Estimate maximum information loss
     */
    private void estimateMax() {

        // Prepare
        initializeTopDown(lattice.getTop());
        setMaximum(lattice.getTop());
        this.globalMaximum = null;
        
        // Pull
        ARXNode[][] levels = lattice.getLevels();
        for (int i = levels.length-2; i >= 0; i--) {
            final ARXNode[] level = levels[i];
            for (final ARXNode node : level) {
                pullTopDown(node);
                setMaximum(node);
                this.globalMaximum = max(this.globalMaximum, node.getMaximumInformationLoss());
            }
        }
    }

    /**
     * Estimate minimum information loss
     */
    private void estimateMin() {

        // Prepare
        initializeBottomUp(lattice.getBottom());
        setMinimum(lattice.getBottom());
        this.globalMinimum = null;
        
        // Pull
        ARXNode[][] levels = lattice.getLevels();
        for (int i = 1; i < levels.length; i++) {
            final ARXNode[] level = levels[i];
            for (final ARXNode node : level) {
                pullBottomUp(node);
                setMinimum(node);
                this.globalMinimum = min(this.globalMinimum, node.getMinimumInformationLoss());
            }
        }
    }

    /**
     * Returns the max of both, handles null values
     * @param first
     * @param second
     * @return
     */
    private InformationLoss<?> max(InformationLoss<?> first, InformationLoss<?> second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        } else if (first.compareTo(second) < 0) {
            return second;
        } else {
            return first;
        }
    }

    /**
     * Returns the min of both, handles null values
     * @param first
     * @param second
     * @return
     */
    private InformationLoss<?> min(InformationLoss<?> first, InformationLoss<?> second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        } else if (first.compareTo(second) < 0) {
            return first;
        } else {
            return second;
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
     * Initializes the bottom node
     * @param node
     */
    private void initializeBottomUp(ARXNode node) {

        lowerBound[node.getId()] = getValueOrDefault(node.getLowerBound(), metric.createMinInformationLoss());
        
        if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
            minimumAnonymous[node.getId()] = getValueOrDefault(node.getMinimumInformationLoss(), metric.createMinInformationLoss());
            minimumNonAnonymous[node.getId()] = metric.createMinInformationLoss();
        } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            minimumNonAnonymous[node.getId()] = getValueOrDefault(node.getMinimumInformationLoss(), metric.createMinInformationLoss());
            minimumAnonymous[node.getId()] = metric.createMinInformationLoss();
        } else {
            minimumAnonymous[node.getId()] = metric.createMinInformationLoss();
            minimumNonAnonymous[node.getId()] = metric.createMinInformationLoss();
        }
    }

    /**
     * Initializes the top node
     * @param node
     */
    private void initializeTopDown(ARXNode node) {

        if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
            maximumAnonymous[node.getId()] = getValueOrDefault(node.getMaximumInformationLoss(), metric.createMaxInformationLoss());
            maximumNonAnonymous[node.getId()] = metric.createMaxInformationLoss();
        } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            maximumNonAnonymous[node.getId()] = getValueOrDefault(node.getMaximumInformationLoss(), metric.createMaxInformationLoss());
            maximumAnonymous[node.getId()] = metric.createMaxInformationLoss();
        } else {
            maximumAnonymous[node.getId()] = metric.createMaxInformationLoss();
            maximumNonAnonymous[node.getId()] = metric.createMaxInformationLoss();
        }
    }

    /**
     * Propagate bottom up
     * @param node
     */
    private void pullBottomUp(ARXNode node) {
        
        // Pull all values
        for (ARXNode pre : node.getPredecessors()) {
            pullMax(minimumAnonymous, node.getId(), pre.getId());
            pullMax(minimumNonAnonymous, node.getId(), pre.getId());
            pullMax(lowerBound, node.getId(), pre.getId());
        }
        
        // Lower bound can always be replaced
        if (node.getLowerBound() != null) {
            lowerBound[node.getId()] = max(lowerBound[node.getId()], node.getLowerBound());
        }
        
        // Check if values can be replaced
        if (node.getMinimumInformationLoss() != null) {
            if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
                minimumAnonymous[node.getId()] = max(minimumAnonymous[node.getId()], node.getMinimumInformationLoss());
            } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
                minimumNonAnonymous[node.getId()] = max(minimumNonAnonymous[node.getId()], node.getMinimumInformationLoss());
            }
        }
    }

    /**
     * Pulls a specific property and retains the value using a maximum aggregate function
     * @param array
     * @param target
     * @param source
     */
    private void pullMax(InformationLoss<?>[] array, int target, int source) {
        if (array[target] == null) {
            array[target] = array[source];
        } else {
            array[target] = max(array[target], array[source]);
        }
    }

    /**
     * Pulls a specific property and retains the value using a minimum aggregate function
     * @param array
     * @param target
     * @param source
     */
    private void pullMin(InformationLoss<?>[] array, int target, int source) {
        if (array[target] == null) {
            array[target] = array[source];
        } else {
            array[target] = min(array[target], array[source]);
        }
    }
    
    
    /**
     * Propagate top down
     * @param node
     */
    private void pullTopDown(ARXNode node) {
        
        // Pull all values
        for (ARXNode succ : node.getSuccessors()) {
            pullMin(maximumAnonymous, node.getId(), succ.getId());
            pullMin(maximumNonAnonymous, node.getId(), succ.getId());
        }
        
        // Check if values can be replaced
        if (node.getMaximumInformationLoss() != null) {
            if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
                maximumAnonymous[node.getId()] = min(maximumAnonymous[node.getId()], node.getMaximumInformationLoss());
            } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
                maximumNonAnonymous[node.getId()] = min(maximumNonAnonymous[node.getId()], node.getMaximumInformationLoss());
            }
        }
    }

    /**
     * Selects a maximum for the given node
     * @param node
     */
    private void setMaximum(ARXNode node) {
    
        // If we already know everything, abort
        if (node.getMinimumInformationLoss() != null && 
            node.getMaximumInformationLoss() != null && 
            node.getMinimumInformationLoss().compareTo(node.getMaximumInformationLoss())==0){
            return;
        }
        
        // Check if values can be replaced
        InformationLoss<?> minimalMaximum = null;
        if (node.getAnonymity() == Anonymity.ANONYMOUS && monotonicAnonymous) {
            minimalMaximum = min(node.getMaximumInformationLoss(), maximumAnonymous[node.getId()]);
        } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            minimalMaximum = min(node.getMaximumInformationLoss(), maximumNonAnonymous[node.getId()]);
        } else {
            minimalMaximum = min(node.getMaximumInformationLoss(), metric.createMaxInformationLoss());
        }
        
        // Set
        node.access().setMaximumInformationLoss(minimalMaximum);
    }

    /**
     * Selects a minimum for the given node
     * @param node
     */
    private void setMinimum(ARXNode node) {
    
        // If we already know everything, abort
        if (node.getMinimumInformationLoss() != null && 
            node.getMaximumInformationLoss() != null && 
            node.getMinimumInformationLoss().compareTo(node.getMaximumInformationLoss())==0){
            return;
        }
        
        // We can always use the lower bound
        InformationLoss<?> maximalMinimum = max(node.getMinimumInformationLoss(), lowerBound[node.getId()]);
        
        // Check if values can be replaced
        if (node.getAnonymity() == Anonymity.ANONYMOUS) {
            
            // We can always use the optimum as a minimum for anonymous nodes
            maximalMinimum = max(maximalMinimum, lattice.getOptimum().getMinimumInformationLoss());
            
            if (monotonicAnonymous) {
                maximalMinimum = max(maximalMinimum, minimumAnonymous[node.getId()]);
            }
        } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS && monotonicNonAnonymous) {
            maximalMinimum = max(maximalMinimum, minimumNonAnonymous[node.getId()]);
        }
        
        // Set
        node.access().setMinimumInformationLoss(maximalMinimum);
    }

    /**
     * Implements the estimation process
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
