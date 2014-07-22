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

import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.framework.lattice.NodeTrigger;

/**
 * This class provides a reference implementation of the Two-Phase FLASH algorithm.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHAlgorithmTwoPhases extends AbstractFLASHAlgorithm {

    /** The heap. */
    protected final PriorityQueue<Node> pqueue;

    /** The stack. */
    protected final Stack<Node>         stack;

    /**
     * Creates a new instance of the FLASH algorithm.
     * 
     * @param lattice
     *            The lattice
     * @param history
     *            The history
     * @param checker
     *            The checker
     * @param strategy
     *            The strategy
     */
    public FLASHAlgorithmTwoPhases(final Lattice lattice, final INodeChecker checker, final FLASHStrategy strategy) {

        super(lattice, checker, strategy);
        this.stack = new Stack<Node>();
        this.pqueue = new PriorityQueue<Node>(11, strategy);
        this.history.setEvictionTrigger(History.EVICTION_TRIGGER_K_ANONYMOUS);
        this.history.setStorageTrigger(History.STORAGE_TRIGGER_NON_ANONYMOUS);
    }

    /**
     * Check a node during the first phase
     * 
     * @param node
     */
    protected void checkNode1(final Node node) {

        checker.check(node);
        lattice.tagKAnonymous(node, node.isKAnonymous());
        lattice.triggerTagged();
    }

    /**
     * Checks a path binary.
     * 
     * @param path
     *            The path
     */
    private final Node checkPathBinary(final List<Node> path) {
        int low = 0;
        int high = path.size() - 1;
        Node lastAnonymousNode = null;

        while (low <= high) {

            final int mid = (low + high) >>> 1;
            final Node node = path.get(mid);

            if (!node.isTagged()) {
                checkNode1(node);
                if (!node.isKAnonymous()) {
                    for (final Node up : node.getSuccessors()) {
                        if (!up.isTagged()) {
                            pqueue.add(up);
                        }
                    }
                }
            }

            if (node.isKAnonymous()) {
                lastAnonymousNode = node;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return lastAnonymousNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.algorithm.AbstractAlgorithm#traverse()
     */
    @Override
    public void traverse() {

        pqueue.clear();
        stack.clear();
        if (!lattice.getBottom().isChecked()) {
            checker.check(lattice.getBottom(), true);
        }
        
        // For each node
        final int length = lattice.getLevels().length;
        for (int i = 0; i < length; i++) {
            Node[] level;
            level = this.sortSuccessors(i);
            for (final Node node : level) {
                if (!node.isTagged()) {
                    pqueue.add(node);
                    while (!pqueue.isEmpty()) {
                        Node head = pqueue.poll();
                        // if anonymity is unknown
                        if (!head.isTagged()) {

                            // First phase
                            findPath(head);
                            head = checkPathBinary(path);

                            // Second phase
                            if (head != null) {

                                // Change strategies
                                NodeTrigger pruningStrategy = history.getEvictionTrigger();
                                NodeTrigger storageStrategy = history.getStorageTrigger();
                                history.setEvictionTrigger(History.EVICTION_TRIGGER_CHECKED);
                                history.setStorageTrigger(History.STORAGE_TRIGGER_ALL);

                                // Untag all nodes above first anonymous node if
                                // they have already been tagged in first phase.
                                // They will all be tagged again in the second phase
                                lattice.doUnTagUpwards(head);

                                stack.push(head);
                                while (!stack.isEmpty()) {
                                    final Node start = stack.pop();
                                    if (!start.isTagged()) {
                                        findPath(start);
                                        checkPathLinear(path, stack);
                                    }
                                }

                                // Switch back to previous strategies
                                history.setEvictionTrigger(pruningStrategy);
                                history.setStorageTrigger(storageStrategy);
                            }
                        }
                    }
                }
            }
        }
        
        if (lattice.getTop().getInformationLoss() == null) {
            if (!lattice.getTop().isChecked()) {
                checker.check(lattice.getTop(), true);
            }
        }
    }
}
