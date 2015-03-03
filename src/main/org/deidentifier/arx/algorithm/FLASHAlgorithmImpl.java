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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.framework.lattice.NodeAction;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.InformationLossWithBound;

/**
 * This class implements the FLASH algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHAlgorithmImpl extends AbstractAlgorithm {

    /** Configuration for the algorithm's phases. */
    protected final FLASHConfiguration config;

    /** Are the pointers for a node with id 'index' already sorted?. */
    private final boolean[]            sorted;

    /** The strategy. */
    private final FLASHStrategy        strategy;

    /** List of nodes that may be used for pruning transformations with insufficient utility. */
    private final List<Node>           potentiallyInsufficientUtility;

    /**
     * Creates a new instance.
     *
     * @param lattice
     * @param checker
     * @param strategy
     * @param config
     */
    public FLASHAlgorithmImpl(Lattice lattice,
                              INodeChecker checker,
                              FLASHStrategy strategy,
                              FLASHConfiguration config) {

        super(lattice, checker);
        this.strategy = strategy;
        this.sorted = new boolean[lattice.getSize()];
        this.config = config;
        this.potentiallyInsufficientUtility = this.config.isPruneInsufficientUtility() ? 
                                              new LinkedList<Node>() : null;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.algorithm.AbstractAlgorithm#traverse()
     */
    @Override
    public void traverse() {

        // Determine configuration for the outer loop
        FLASHPhaseConfiguration outerLoopConfiguration;
        if (config.isBinaryPhaseRequired()) {
            outerLoopConfiguration = config.getBinaryPhaseConfiguration();
        } else {
            outerLoopConfiguration = config.getLinearPhaseConfiguration();
        }

        // Set some triggers
        lattice.setTagTrigger(config.getTriggerTagEvent());
        checker.getHistory().setStorageTrigger(config.getTriggerSnapshotStore());

        // Initialize
        PriorityQueue<Node> queue = new PriorityQueue<Node>(lattice.getLevels().length, strategy);

        // Check bottom for speed and remember the result to prevent repeated checks
        Node bottom = lattice.getBottom();
        INodeChecker.Result result = checker.check(bottom);
        lattice.setProperty(bottom, Node.PROPERTY_FORCE_SNAPSHOT);
        bottom.setData(result);

        // For each node in the lattice
        int length = lattice.getLevels().length;
        for (int i = 0; i < length; i++) {
            for (Node node : getUnsetNodesAndSort(i, outerLoopConfiguration.getTriggerSkip())) {

                // Run the correct phase
                if (config.isBinaryPhaseRequired()) {
                    binarySearch(node, queue);
                } else {
                    linearSearch(node);
                }
            }
        }

        // Potentially allows to better estimate utility in the lattice
        computeUtilityForMonotonicMetrics(lattice.getBottom());
        computeUtilityForMonotonicMetrics(lattice.getTop());

        // Remove the associated result information to leave the lattice in a consistent state
        lattice.getBottom().setData(null);

        // Clear list of pruning candidates
        if (potentiallyInsufficientUtility != null) {
        	potentiallyInsufficientUtility.clear();
        }
    }

    /**
     * Implements the FLASH algorithm (without outer loop).
     *
     * @param start
     * @param queue
     */
    private void binarySearch(Node start, PriorityQueue<Node> queue) {

        // Obtain node action
        NodeAction triggerSkip = config.getBinaryPhaseConfiguration().getTriggerSkip();

        // Add to queue
        queue.add(start);

        // While queue is not empty
        while (!queue.isEmpty()) {

            // Remove head and process
            Node head = queue.poll();
            if (!skip(triggerSkip, head)) {

                // First phase
                List<Node> path = findPath(head, triggerSkip);
                head = checkPath(path, triggerSkip, queue);

                // Second phase
                if (config.isLinearPhaseRequired() && (head != null)) {

                    // Run linear search on head
                    linearSearch(head);
                }
            }
        }
    }

    /**
     * Checks and tags the given transformation.
     *
     * @param node
     * @param configuration
     */
    private void checkAndTag(Node node, FLASHPhaseConfiguration configuration) {

        // Check or evaluate
        if (configuration.getTriggerEvaluate().appliesTo(node)) {
            InformationLossWithBound<?> loss = checker.getMetric().getInformationLoss(node, null);
            lattice.setInformationLoss(node, loss.getInformationLoss());
            lattice.setLowerBound(node, loss.getLowerBound());
            if (loss.getLowerBound() == null) {
                lattice.setLowerBound(node, checker.getMetric().getLowerBound(node));
            }
        } else if (configuration.getTriggerCheck().appliesTo(node)) {
            lattice.setChecked(node, checker.check(node));
        }

        // Store optimum
        trackOptimum(node);

        // Tag
        configuration.getTriggerTag().apply(node);

        // Potentially prune some parts of the search space
        prune(node);
    }

    /**
     * Checks a path binary.
     *
     * @param path The path
     * @param triggerSkip
     * @param queue
     * @return
     */
    private Node checkPath(List<Node> path, NodeAction triggerSkip, PriorityQueue<Node> queue) {

        // Obtain anonymity property
        int anonymityProperty = config.getBinaryPhaseConfiguration().getAnonymityProperty();

        // Init
        int low = 0;
        int high = path.size() - 1;
        Node lastAnonymousNode = null;

        // While not done
        while (low <= high) {

            // Init
            final int mid = (low + high) / 2;
            final Node node = path.get(mid);

            // Skip
            if (!skip(triggerSkip, node)) {

                // Check and tag
                checkAndTag(node, config.getBinaryPhaseConfiguration());

                // Add nodes to queue
                if (!node.hasProperty(anonymityProperty)) {
                    for (final Node up : node.getSuccessors()) {
                        if (!skip(triggerSkip, up)) {
                            queue.add(up);
                        }
                    }
                }

                // Binary search
                if (node.hasProperty(anonymityProperty)) {
                    lastAnonymousNode = node;
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            } else {
                high = mid - 1;
            }
        }
        return lastAnonymousNode;
    }

    /**
     * Greedily finds a path to the top node.
     *
     * @param current The node to start the path with. Will be included
     * @param triggerSkip All nodes to which this trigger applies will be skipped
     * @return The path as a list
     */
    private List<Node> findPath(Node current, NodeAction triggerSkip) {
        List<Node> path = new ArrayList<Node>();
        path.add(current);
        boolean found = true;
        while (found) {
            found = false;
            sortSuccessors(current);
            for (final Node candidate : current.getSuccessors()) {
                if (!skip(triggerSkip, candidate)) {
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
     * Returns all nodes that do not have the given property and sorts the resulting array
     * according to the strategy.
     *
     * @param level The level which is to be sorted
     * @param triggerSkip The trigger to be used for limiting the number of nodes to be sorted
     * @return A sorted array of nodes remaining on this level
     */

    private Node[] getUnsetNodesAndSort(int level, NodeAction triggerSkip) {

        // Create
        List<Node> result = new ArrayList<Node>();
        Node[] nlevel = lattice.getLevels()[level];
        for (Node n : nlevel) {
            if (!skip(triggerSkip, n)) {
                result.add(n);
            }
        }

        // Sort
        Node[] resultArray = result.toArray(new Node[result.size()]);
        sort(resultArray);
        return resultArray;
    }

    /**
     * Implements a depth-first search with predictive tagging.
     *
     * @param start
     */
    private void linearSearch(Node start) {

        // Obtain node action
        NodeAction triggerSkip = config.getLinearPhaseConfiguration().getTriggerSkip();

        // Skip this node
        if (!skip(triggerSkip, start)) {

            // Sort successors
            sortSuccessors(start);

            // Check and tag
            checkAndTag(start, config.getLinearPhaseConfiguration());

            // DFS
            for (final Node child : start.getSuccessors()) {
                if (!skip(triggerSkip, child)) {
                    linearSearch(child);
                }
            }
        }

        // Mark as successors pruned
        lattice.setProperty(start, Node.PROPERTY_SUCCESSORS_PRUNED);
    }

    /**
     * We may be able to prune some transformations based on weak lower bounds on
     * the monotonic share of a node's information loss.
     *
     * @param node
     */
    private void prune(Node node) {

        // Check if pruning is enabled
        if (potentiallyInsufficientUtility == null) {
            return;
        }

        // There is no need to do anything, if we do not have a lower bound
        if (node.getLowerBound() == null) {
            return;
        }

        // Extract some data
        Node optimalTransformation = getGlobalOptimum();

        // There is no need to do anything, if the transformation that was just checked was already pruned
        if ((node != optimalTransformation) && node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED)) {
            return;
        }

        // If we haven't yet found an optimum, we simply add the node to the list of pruning candidates
        if (optimalTransformation == null) {
            potentiallyInsufficientUtility.add(node);
            return;
        }

        // Extract some data
        InformationLoss<?> optimalInfoLoss = optimalTransformation.getInformationLoss();

        // If the current node is not the new optimum, we simply check it
        if (node != optimalTransformation) {

            // Prune it
            if (optimalInfoLoss.compareTo(node.getLowerBound()) <= 0) {
                lattice.setPropertyUpwards(node, true, Node.PROPERTY_INSUFFICIENT_UTILITY |
                                                       Node.PROPERTY_SUCCESSORS_PRUNED);
                // Else, we store it as a future pruning candidate
            } else {
                potentiallyInsufficientUtility.add(node);
            }

            // If the current node is our new optimum, we check all candidates
        } else {

            // For each candidate
            Iterator<Node> iterator = potentiallyInsufficientUtility.iterator();
            while (iterator.hasNext()) {
                Node current = iterator.next();

                // Remove the candidate, if it was already pruned in the meantime
                if (current.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED)) {
                    iterator.remove();

                    // Else, check if we can prune it
                } else if (optimalInfoLoss.compareTo(current.getLowerBound()) <= 0) {
                    lattice.setPropertyUpwards(current, true, Node.PROPERTY_INSUFFICIENT_UTILITY |
                                                              Node.PROPERTY_SUCCESSORS_PRUNED);
                    iterator.remove();
                }
            }

            // The current optimum is a future pruning candidate
            if (!node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED)) {
                potentiallyInsufficientUtility.add(node);
            }
        }
    }

    /**
     * Returns whether a node should be skipped.
     *
     * @param trigger
     * @param node
     * @return
     */
    private boolean skip(NodeAction trigger, Node node) {

        // If the trigger applies, skip
        if (trigger.appliesTo(node)) {
            return true;
        }

        // Check if pruning is enabled
        if (potentiallyInsufficientUtility == null) {
            return false;
        }

        // Check, if we can prune based on a monotonic sub-metric
        if (!checker.getConfiguration().isPracticalMonotonicity() && (getGlobalOptimum() != null)) {

            // We skip, if we already know that this node has insufficient utility
            if (node.hasProperty(Node.PROPERTY_INSUFFICIENT_UTILITY)) {
                return true;
            }

            // Check whether a lower bound exists
            InformationLoss<?> lowerBound = node.getLowerBound();
            if (lowerBound == null) {
                lowerBound = checker.getMetric().getLowerBound(node);
                if (lowerBound != null) {
                    lattice.setLowerBound(node, lowerBound);
                }
            }

            // Check whether this node has insufficient utility, if a lower bound exists
            if (lowerBound != null) {
                if (getGlobalOptimum().getInformationLoss().compareTo(lowerBound) <= 0) {
                    lattice.setPropertyUpwards(node, true, Node.PROPERTY_INSUFFICIENT_UTILITY |
                                                           Node.PROPERTY_SUCCESSORS_PRUNED);
                    return true;
                }
            }
        }

        // We need to process this node
        return false;
    }

    /**
     * Sorts a node array.
     * 
     * @param array The array
     */
    private void sort(final Node[] array) {
        Arrays.sort(array, strategy);
    }

    /**
     * Sorts pointers to successor nodes according to the strategy.
     *
     * @param node The node
     */
    private void sortSuccessors(final Node node) {
        if (!sorted[node.id]) {
            sort(node.getSuccessors());
            sorted[node.id] = true;
        }
    }
}
