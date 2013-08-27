/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History.PruningStrategy;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides a reference implementation of the Two-Phase FLASH algorithm.
 * 
 * @author Prasser, Kohlmayer
 */
public class FLASHAlgorithmTwoPhases extends AbstractFLASHAlgorithm {

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
        history.setPruningStrategy(PruningStrategy.K_ANONYMOUS);
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
     * Check a node during the second phase
     * 
     * @param node
     */
    protected void checkNode2(final Node node) {
        if (!node.isChecked()) {

            // TODO: Rethink var1 & var2
            final boolean var1 = !checker.getMetric().isMonotonic() && checker.getConfiguration().isCriterionMonotonic();

            final boolean var2 = !checker.getMetric().isMonotonic() && !checker.getConfiguration().isCriterionMonotonic() && checker.getConfiguration().isPracticalMonotonicity();

            // NOTE: Might return non-anonymous result as optimum, when
            // 1. the criterion is not monotonic, and
            // 2. practical monotonicity is assumed, and
            // 3. the metric is non-monotonic BUT independent.
            // -> Such a metric does currently not exist
            if (checker.getMetric().isIndependent() && (var1 || var2)) {
                checker.getMetric().evaluate(node, null);
            } else {
                checker.check(node);
            }

        }

        // In case metric is monotone it can be tagged if the node is anonymous
        if (checker.getMetric().isMonotonic() && node.isAnonymous()) {
            lattice.tagAnonymous(node, node.isAnonymous());
        } else {
            node.setTagged();
            lattice.decUntaggedCount(node.getLevel());
            lattice.triggerTagged();
        }
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
        checkBottom();

        // For each node
        final int length = lattice.getLevels().length;
        for (int i = 0; i < length; i++) {
            Node[] level;
            level = this.sort(i);
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

                                final PruningStrategy pruning = history.getPruningStrategy();
                                history.setPruningStrategy(PruningStrategy.CHECKED);

                                // Untag all nodes above first anonymous node if
                                // they have already been tagged in first phase.
                                // They will all be tagged again in the second phase
                                lattice.doUnTagUpwards(head);

                                stack.push(head);
                                while (!stack.isEmpty()) {
                                    final Node start = stack.pop();
                                    if (!start.isTagged()) {
                                        findPath(start);
                                        checkPathLinear(path);
                                    }
                                }

                                // Switch back to previous strategy
                                history.setPruningStrategy(pruning);
                            }
                        }
                    }
                }
            }
        }
    }
}
