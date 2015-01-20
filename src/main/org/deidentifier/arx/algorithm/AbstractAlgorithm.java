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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.InformationLossWithBound;

/**
 * Abstract class for an algorithm, which provides some generic methods.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractAlgorithm {

    /** The optimal transformation. */
    private Node               globalOptimum          = null;

    /** The optimal information loss. */
    private InformationLoss<?> optimalInformationLoss = null;

    /** A node checker. */
    protected INodeChecker     checker                = null;

    /** The lattice. */
    protected Lattice          lattice                = null;

    /**
     * Walks the lattice.
     * 
     * @param lattice
     *            The lattice
     * @param checker
     *            The checker
     */
    protected AbstractAlgorithm(final Lattice lattice,
                                final INodeChecker checker) {
        this.checker = checker;
        this.lattice = lattice;
    }

    /**
     * Returns a list of all anonymous nodes in the lattice.
     * 
     * @return the all anonymous nodes
     */
    public List<Node> getAllAnonymousNodes() {
        final ArrayList<Node> results = new ArrayList<Node>();
        for (final Node[] level : lattice.getLevels()) {
            for (final Node n : level) {
                if (n.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    results.add(n);
                }
            }
        }
        return results;
    }

    /**
     * Returns the global optimum.
     *
     * @return
     */
    public Node getGlobalOptimum() {
        return globalOptimum;
    }

    /**
     * Implement this method in order to provide a new algorithm.
     */
    public abstract void traverse();

    /**
     * Determine information loss of the given node if it can be
     * used for estimating minimum and maximum information
     * loss for tagged nodes.
     *
     * @param node
     */
    protected void computeUtilityForMonotonicMetrics(Node node) {
        if ((checker.getMetric().isMonotonic() ||
            (checker.getConfiguration().getMaxOutliers() == 0d)) &&
            (node.getInformationLoss() == null)) {

            // Independent evaluation or check
            if (checker.getMetric().isIndependent()) {
                InformationLossWithBound<?> loss = checker.getMetric().getInformationLoss(node, null);
                lattice.setInformationLoss(node, loss.getInformationLoss());
                lattice.setLowerBound(node, loss.getLowerBound());
            } else {
                lattice.setChecked(node, checker.check(node, true));
            }
        }
    }

    /**
     * Keeps track of the global optimum.
     *
     * @param node
     */
    protected void trackOptimum(Node node) {
        if (node.hasProperty(Node.PROPERTY_ANONYMOUS) &&
            ((globalOptimum == null) ||
             (node.getInformationLoss().compareTo(optimalInformationLoss) < 0) ||
            ((node.getInformationLoss().compareTo(optimalInformationLoss) == 0) && (node.getLevel() < globalOptimum.getLevel())))) {
            globalOptimum = node;
            optimalInformationLoss = node.getInformationLoss();
        }
    }
}
