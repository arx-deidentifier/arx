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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * Abstract class for an algorithm, which provides some generic methods.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractAlgorithm {

    /** The optimal transformation*/
    private Node globalOptimum = null;

    /** The optimal information loss*/
    private InformationLoss optimalInformationLoss = null;

    /** A node checker. */
    protected INodeChecker  checker  = null;

    /** The lattice. */
    protected Lattice       lattice  = null;
    
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
     * Returns the global optimum
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
     * loss for tagged nodes
     * @param node
     */
    protected void computeUtilityForMonotonicMetrics(Node node){
        if ((checker.getMetric().isMonotonic() ||
             checker.getConfiguration().getMaxOutliers() == 0d) &&
             node.getInformationLoss() == null) {
               
               // Independent evaluation or check
               if (checker.getMetric().isIndependent()) {
                   node.setInformationLoss(checker.getMetric().evaluate(node, null));
               } else {
                   node.setChecked(checker.check(node, true));
               }
           }
    }
    

    /**
     * Keeps track of the global optimum
     * @param node
     */
    protected void trackOptimum(Node node){
        if (node.hasProperty(Node.PROPERTY_ANONYMOUS) && 
           ((globalOptimum == null) || 
            (node.getInformationLoss().compareTo(optimalInformationLoss) < 0) || 
            (node.getInformationLoss().compareTo(optimalInformationLoss) == 0 && node.getLevel() < globalOptimum.getLevel()))) {
            this.globalOptimum = node;
            this.optimalInformationLoss = node.getInformationLoss();
        }
    }
}
