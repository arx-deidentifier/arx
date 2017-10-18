/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.deidentifier.arx.algorithm.FLASHPhaseConfiguration.PhaseAnonymityProperty;
import org.deidentifier.arx.framework.check.TransformationResult;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.lattice.DependentAction;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.InformationLossWithBound;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.list.LongArrayList;

import com.carrotsearch.hppc.IntArrayList;

import de.linearbits.jhpl.JHPLIterator.LongIterator;
import de.linearbits.jhpl.PredictiveProperty;

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
    private final int[][]              sortedSuccessors;

    /** The strategy. */
    private final FLASHStrategy        strategy;

    /** List of nodes that may be used for pruning transformations with insufficient utility. */
    private final List<Integer>        potentiallyInsufficientUtility;

    /** The number of checked transformations */
    private int                        checked = 0;

    /**
     * Creates a new instance.
     *
     * @param solutionSpace
     * @param checker
     * @param strategy
     * @param config
     */
    public FLASHAlgorithmImpl(SolutionSpace solutionSpace,
                              TransformationChecker checker,
                              FLASHStrategy strategy,
                              FLASHConfiguration config) {

        super(solutionSpace, checker);
        if (solutionSpace.getSize() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        this.checked = 0;
        this.solutionSpace.setAnonymityPropertyPredictable(config.isAnonymityPropertyPredicable());
        this.strategy = strategy;
        this.sortedSuccessors = new int[(int)solutionSpace.getSize()][];
        this.config = config;
        this.potentiallyInsufficientUtility = this.config.isPruneInsufficientUtility() ? 
                                              new LinkedList<Integer>() : null;
    }

    @Override
    public boolean traverse() {
        
        // Determine configuration for the outer loop
        FLASHPhaseConfiguration outerLoopConfiguration;
        if (config.isBinaryPhaseRequired()) {
            outerLoopConfiguration = config.getBinaryPhaseConfiguration();
        } else {
            outerLoopConfiguration = config.getLinearPhaseConfiguration();
        }

        // Set some triggers
        checker.getHistory().setStorageStrategy(config.getSnapshotStorageStrategy());

        // Initialize
        PriorityQueue<Integer> queue = new PriorityQueue<Integer>(solutionSpace.getTop().getLevel() + 1, strategy);
        Transformation bottom = solutionSpace.getBottom();
        Transformation top = solutionSpace.getTop();

        // Check bottom for speed and remember the result to prevent repeated checks
        TransformationResult result = checker.check(bottom);
        bottom.setProperty(solutionSpace.getPropertyForceSnapshot());
        bottom.setData(result);

        // For each node in the lattice
        for (int level = bottom.getLevel(); level <= top.getLevel(); level++) {
            for (int id : getSortedUnprocessedNodes(level, outerLoopConfiguration.getTriggerSkip())) {

                // Run the correct phase
                Transformation transformation = solutionSpace.getTransformation(id);
                if (config.isBinaryPhaseRequired()) {
                    binarySearch(transformation, queue);
                } else {
                    linearSearch(transformation);
                }
            }
        }

        // Potentially allows to better estimate utility in the lattice
        computeUtilityForMonotonicMetrics(bottom);
        computeUtilityForMonotonicMetrics(top);

        // Remove the associated result information to leave the lattice in a consistent state
        bottom.setData(null);

        // Clear list of pruning candidates
        if (potentiallyInsufficientUtility != null) {
        	potentiallyInsufficientUtility.clear();
        }
        
        // Return whether the optimum has been found
        return this.getGlobalOptimum() != null;
    }

    /**
     * Implements the FLASH algorithm (without outer loop).
     *
     * @param transformation
     * @param queue
     */
    private void binarySearch(Transformation transformation, PriorityQueue<Integer> queue) {

        // Obtain node action
        DependentAction triggerSkip = config.getBinaryPhaseConfiguration().getTriggerSkip();

        // Add to queue
        queue.add((int)transformation.getIdentifier());

        // While queue is not empty
        while (!queue.isEmpty()) {

            // Remove head and process
            transformation = solutionSpace.getTransformation(queue.poll());
            if (!skip(triggerSkip, transformation)) {

                // First phase
                List<Transformation> path = findPath(transformation, triggerSkip);
                transformation = checkPath(path, triggerSkip, queue);

                // Second phase
                if (config.isLinearPhaseRequired() && (transformation != null)) {

                    // Run linear search on head
                    linearSearch(transformation);
                }
            }
        }
    }

    /**
     * Checks and tags the given transformation.
     *
     * @param transformation
     * @param configuration
     */
    private void checkAndTag(Transformation transformation, FLASHPhaseConfiguration configuration) {

        // Check or evaluate
        if (configuration.getTriggerEvaluate().appliesTo(transformation)) {
            InformationLossWithBound<?> loss = checker.getMetric().getInformationLoss(transformation, (HashGroupify)null);
            transformation.setInformationLoss(loss.getInformationLoss());
            transformation.setLowerBound(loss.getLowerBound());
            if (loss.getLowerBound() == null) {
                transformation.setLowerBound(checker.getMetric().getLowerBound(transformation));
            }
        } else if (configuration.getTriggerCheck().appliesTo(transformation)) {
            transformation.setChecked(checker.check(transformation));
            progress((double)++checked / (double)solutionSpace.getSize());
        }

        // Store optimum
        trackOptimum(transformation);

        // Tag
        configuration.getTriggerTag().apply(transformation);

        // Potentially prune some parts of the search space
        prune(transformation);
    }

    /**
     * Checks a path binary.
     *
     * @param path The path
     * @param triggerSkip
     * @param queue
     * @return
     */
    private Transformation checkPath(List<Transformation> path, DependentAction triggerSkip, PriorityQueue<Integer> queue) {

        // Obtain anonymity property
        PredictiveProperty anonymityProperty = config.getBinaryPhaseConfiguration().getAnonymityProperty() == PhaseAnonymityProperty.ANONYMITY ?
                                               solutionSpace.getPropertyAnonymous() : solutionSpace.getPropertyKAnonymous();

        // Init
        int low = 0;
        int high = path.size() - 1;
        Transformation lastAnonymousTransformation = null;

        // While not done
        while (low <= high) {

            // Init
            final int mid = (low + high) / 2;
            final Transformation transformation = path.get(mid);

            // Skip
            if (!skip(triggerSkip, transformation)) {

                // Check and tag
                checkAndTag(transformation, config.getBinaryPhaseConfiguration());

                // Add nodes to queue
                if (!transformation.hasProperty(anonymityProperty)) {
                    for (final int up : getSortedSuccessors(transformation)) {
                        if (!skip(triggerSkip, solutionSpace.getTransformation(up))) {
                            queue.add(up);
                        }
                    }
                }

                // Binary search
                if (transformation.hasProperty(anonymityProperty)) {
                    lastAnonymousTransformation = transformation;
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            } else {
                high = mid - 1;
            }
        }
        
        return lastAnonymousTransformation;
    }

    /**
     * Greedily finds a path to the top node.
     *
     * @param current The node to start the path with. Will be included
     * @param triggerSkip All nodes to which this trigger applies will be skipped
     * @return The path as a list
     */
    private List<Transformation> findPath(Transformation current, DependentAction triggerSkip) {
        List<Transformation> path = new ArrayList<Transformation>();
        path.add(current);
        boolean found = true;
        while (found) {
            found = false;
            for (final int id : getSortedSuccessors(current)) {
                Transformation next = solutionSpace.getTransformation(id);
                if (!skip(triggerSkip, next)) {
                    current = next;
                    path.add(next);
                    found = true;
                    break;
                }
            }
        }
        return path;
    }
    
    /**
     * Sorts pointers to successor nodes according to the strategy.
     *
     * @param transformation
     */
    private int[] getSortedSuccessors(final Transformation transformation) {
        
        int identifier = (int)transformation.getIdentifier();
        if (sortedSuccessors[identifier] == null) {
            LongArrayList list = transformation.getSuccessors();
            int[] result = new int[list.size()];
            for (int i=0; i<list.size(); i++) {
                result[i] = (int)list.getQuick(i);
            }
            sort(result);
            sortedSuccessors[identifier] = result;
        }
        return sortedSuccessors[identifier];
    }

    /**
     * Returns all transformations that do not have the given property and sorts the resulting array
     * according to the strategy.
     *
     * @param level The level which is to be sorted
     * @param triggerSkip The trigger to be used for limiting the number of nodes to be sorted
     * @return A sorted array of nodes remaining on this level
     */
    private int[] getSortedUnprocessedNodes(int level, DependentAction triggerSkip) {

        // Create
        IntArrayList list = new IntArrayList();
        for (LongIterator iter = solutionSpace.unsafeGetLevel(level); iter.hasNext();) {
            long id = iter.next();
            if (!skip(triggerSkip, solutionSpace.getTransformation(id))) {
                list.add((int)id);
            }            
        }

        // Copy & sort
        int[] array = new int[list.size()];
        System.arraycopy(list.buffer, 0, array, 0, list.elementsCount);
        sort(array);
        return array;
    }

    /**
     * Implements a depth-first search with predictive tagging.
     *
     * @param transformation
     */
    private void linearSearch(Transformation transformation) {

        // Obtain node action
        DependentAction triggerSkip = config.getLinearPhaseConfiguration().getTriggerSkip();

        // Skip this node
        if (!skip(triggerSkip, transformation)) {

            // Check and tag
            checkAndTag(transformation, config.getLinearPhaseConfiguration());

            // DFS
            for (final int child : getSortedSuccessors(transformation)) {
                Transformation childTransformation = solutionSpace.getTransformation(child);
                if (!skip(triggerSkip, childTransformation)) {
                    linearSearch(childTransformation);
                }
            }
        }

        // Mark as successors pruned
        transformation.setProperty(solutionSpace.getPropertySuccessorsPruned());
    }

    /**
     * We may be able to prune some transformations based on weak lower bounds on
     * the monotonic share of a node's information loss.
     *
     * @param node
     */
    private void prune(Transformation node) {

        // Check if pruning is enabled
        if (potentiallyInsufficientUtility == null) {
            return;
        }

        // There is no need to do anything, if we do not have a lower bound
        if (node.getLowerBound() == null) {
            return;
        }

        // Extract some data
        Transformation optimalTransformation = getGlobalOptimum();

        // There is no need to do anything, if the transformation that was just checked was already pruned
        if ((node != optimalTransformation) && node.hasProperty(solutionSpace.getPropertySuccessorsPruned())) {
            return;
        }

        // If we haven't yet found an optimum, we simply add the node to the list of pruning candidates
        if (optimalTransformation == null) {
            potentiallyInsufficientUtility.add((int)node.getIdentifier());
            return;
        }

        // Extract some data
        InformationLoss<?> optimalInfoLoss = optimalTransformation.getInformationLoss();

        // If the current node is not the new optimum, we simply check it
        if (node != optimalTransformation) {

            // Prune it
            if (optimalInfoLoss.compareTo(node.getLowerBound()) <= 0) {
                node.setProperty(solutionSpace.getPropertyInsufficientUtility());
                node.setProperty(solutionSpace.getPropertySuccessorsPruned());
                // Else, we store it as a future pruning candidate
            } else {
                potentiallyInsufficientUtility.add((int)node.getIdentifier());
            }

            // If the current node is our new optimum, we check all candidates
        } else {

            // For each candidate
            Iterator<Integer> iterator = potentiallyInsufficientUtility.iterator();
            while (iterator.hasNext()) {
                Integer current = iterator.next();

                // Remove the candidate, if it was already pruned in the meantime
                Transformation currentTransformation = solutionSpace.getTransformation(current);
                if (currentTransformation.hasProperty(solutionSpace.getPropertySuccessorsPruned())) {
                    iterator.remove();

                    // Else, check if we can prune it
                } else if (optimalInfoLoss.compareTo(currentTransformation.getLowerBound()) <= 0) {
                    currentTransformation.setProperty(solutionSpace.getPropertyInsufficientUtility());
                    currentTransformation.setProperty(solutionSpace.getPropertySuccessorsPruned());
                    iterator.remove();
                }
            }

            // The current optimum is a future pruning candidate
            if (!node.hasProperty(solutionSpace.getPropertySuccessorsPruned())) {
                potentiallyInsufficientUtility.add((int)node.getIdentifier());
            }
        }
    }

    /**
     * Returns whether a node should be skipped.
     *
     * @param transformation
     * @param identifier
     * @return
     */
    private boolean skip(DependentAction trigger, Transformation transformation) {

        // If the trigger applies, skip
        if (trigger.appliesTo(transformation)) {
            return true;
        }

        // Check if pruning is enabled
        if (potentiallyInsufficientUtility == null) {
            return false;
        }

        // Check, if we can prune based on a monotonic sub-metric
        if (!checker.getConfiguration().isPracticalMonotonicity() && (getGlobalOptimum() != null)) {

            // We skip, if we already know that this node has insufficient utility
            if (transformation.hasProperty(solutionSpace.getPropertyInsufficientUtility())) {
                return true;
            }

            // Check whether a lower bound exists
            InformationLoss<?> lowerBound = transformation.getLowerBound();
            if (lowerBound == null) {
                lowerBound = checker.getMetric().getLowerBound(transformation);
                if (lowerBound != null) {
                    transformation.setLowerBound(lowerBound);
                }
            }

            // Check whether this node has insufficient utility, if a lower bound exists
            if (lowerBound != null) {
                if (getGlobalOptimum().getInformationLoss().compareTo(lowerBound) <= 0) {
                    transformation.setProperty(solutionSpace.getPropertyInsufficientUtility());
                    transformation.setProperty(solutionSpace.getPropertySuccessorsPruned());
                    return true;
                }
            }
        }

        // We need to process this node
        return false;
    }

    /**
     * Sorts a given array of transformation identifiers.
     * 
     * @param array
     */
    private void sort(final int[] array) {
        GenericSorting.mergeSort(0, array.length, new IntComparator(){
            @Override
            public int compare(int arg0, int arg1) {
                return strategy.compare(array[arg0], array[arg1]);
            }
        }, new Swapper(){
            @Override
            public void swap(int arg0, int arg1) {
                int temp = array[arg0];
                array[arg0] = array[arg1];
                array[arg1] = temp;
            }
            
        });
    }
}
