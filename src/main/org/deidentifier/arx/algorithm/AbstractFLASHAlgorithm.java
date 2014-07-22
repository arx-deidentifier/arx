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
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.framework.lattice.NodeTrigger;

/**
 * This abstract class provides basic functionalities for implementing the FLASH algorithm
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractFLASHAlgorithm extends AbstractAlgorithm {

    /** Are the pointers for a node with id 'index' already sorted?. */
    protected final boolean[]           sorted;

    /** The strategy. */
    protected final FLASHStrategy       strategy;

    /** The history */
    protected History                   history;

    /**
     * Instantiate
     * @param lattice
     * @param checker
     * @param strategy
     */
    protected AbstractFLASHAlgorithm(final Lattice lattice, 
                                     final INodeChecker checker, 
                                     final FLASHStrategy strategy) {

        super(lattice, checker);
        this.strategy = strategy;
        this.sorted = new boolean[lattice.getSize()];
        this.history = checker.getHistory();
    }
    
    /**
     * Returns all nodes that do not have the given property and sorts the resulting array
     * according to the strategy
     * 
     * @param level The level which is to be sorted
     * @param triggerSkip The trigger to be used for limiting the number of nodes to be sorted
     * @return A sorted array of nodes remaining on this level
     */

    protected final Node[] getUnsetNodesAndSort(int level, NodeTrigger triggerSkip) {
        
        // Create
        List<Node> result = new ArrayList<Node>();
        Node[] nlevel = lattice.getLevels()[level];
        for (Node n : nlevel) {
            if (!triggerSkip.appliesTo(n)) {
                result.add(n);
            }
        }
        
        // Sort
        Node[] resultArray = result.toArray(new Node[result.size()]);
        this.sort(resultArray);
        return resultArray;
    }

    /**
     * Sorts pointers to successor nodes according to the strategy
     * 
     * @param node The node
     */
    protected final void sortSuccessors(final Node node) {
        if (!sorted[node.id]) {
            this.sort(node.getSuccessors());
            sorted[node.id] = true;
        }
    }

    /**
     * Sorts a node array.
     * 
     * @param array The array
     */
    private final void sort(final Node[] array) {
        Arrays.sort(array, strategy);
    }
}
