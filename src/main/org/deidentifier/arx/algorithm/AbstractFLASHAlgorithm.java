package org.deidentifier.arx.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;

public abstract class AbstractFLASHAlgorithm extends AbstractAlgorithm {

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
        this.pqueue = new PriorityQueue<Node>(11, strategy);
        this.sorted = new boolean[lattice.getSize()];
        this.path = new ArrayList<Node>();
        this.stack = new Stack<Node>();
        this.history = checker.getHistory();
    }
    
    /**
     * Instantiate
     * @param lattice
     * @param checker
     * @param strategy
     * @param sorted
     */
    protected AbstractFLASHAlgorithm(final Lattice lattice, 
                                     final INodeChecker checker, 
                                     final FLASHStrategy strategy,
                                     final boolean[] sorted) {

        super(lattice, checker);
        this.strategy = strategy;
        this.pqueue = new PriorityQueue<Node>(11, strategy);
        this.sorted = sorted;
        this.path = new ArrayList<Node>();
        this.stack = new Stack<Node>();
        this.history = checker.getHistory();
    }

    /** The stack. */
    protected final Stack<Node>         stack;

    /** The heap. */
    protected final PriorityQueue<Node> pqueue;

    /** The current path. */
    protected final ArrayList<Node>     path;

    /** Are the pointers for a node with id 'index' already sorted?. */
    protected final boolean[]           sorted;

    /** The strategy. */
    protected final FLASHStrategy       strategy;

    /** The history */
    protected History                   history;

    /**
     * Greedily find a path.
     * 
     * @param current
     *            The current
     * @return the list
     */
    protected final List<Node> findPath(Node current) {
        path.clear();
        path.add(current);
        boolean found = true;
        while (found) {
            found = false;
            this.sort(current);
            for (final Node candidate : current.getSuccessors()) {
                if (!candidate.isTagged()) {
                    current = candidate;
                    path.add(candidate);
                    found = true;
                    break;
                }
            }
        }
        return path;
    }

    /**
     * Sorts a level.
     * 
     * @param level
     *            The level
     * @return the node[]
     */

    protected final Node[] sort(final int level) {
        final Node[] result = new Node[lattice.getUntaggedCount(level)];
        if (result.length == 0) { return result; }
        int index = 0;
        final Node[] nlevel = lattice.getLevels()[level];
        for (final Node n : nlevel) {
            if (!n.isTagged()) {
                result[index++] = n;
            }
        }
        this.sort(result);
        return result;
    }

    /**
     * Sorts upwards pointers of a node.
     * 
     * @param current
     *            The current
     */
    protected final void sort(final Node current) {
        if (!sorted[current.id]) {
            this.sort(current.getSuccessors());
            sorted[current.id] = true;
        }
    }

    /**
     * Sorts a node array.
     * 
     * @param array
     *            The array
     */
    protected final void sort(final Node[] array) {
        Arrays.sort(array, strategy);
    }

    /**
     * Checks the bottom node
     */
    protected void checkBottom() {
        for (final Node[] level : lattice.getLevels()) {
            if (level.length != 0) {
                if (level.length == 1) {
                    checker.check(level[0]);
                    break;
                } else {
                    throw new RuntimeException("Multiple bottom nodes!");
                }
            }
        }
    }
    

    /**
     * Checks a path sequentially.
     * 
     * @param path
     *            The path
     */
    protected final void checkPathLinear(final List<Node> path) {

        for (final Node node : path) {
            if (!node.isTagged()) { 
                checkNodeLinear(node);
                // Put all untagged nodes on the stack
                for (final Node up : node.getSuccessors()) {
                    if (!up.isTagged()) {
                        stack.push(up);
                    }
                }
            }
        }
    }
    
    /**
     * Check a node during the second phase
     * 
     * @param node
     */
    protected void checkNodeLinear(final Node node) {
        if (!node.isChecked()) {

            // TODO: Rethink var1 & var2
            final boolean var1 = !checker.getMetric().isMonotonic() &&
                                 checker.getConfiguration()
                                        .isCriterionMonotonic();

            final boolean var2 = !checker.getMetric().isMonotonic() &&
                                 !checker.getConfiguration()
                                         .isCriterionMonotonic() &&
                                 checker.getConfiguration()
                                        .isPracticalMonotonicity();

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
            lattice.untaggedCount[node.getLevel()]--;
            lattice.triggerTagged();
        }
    }
    
}
