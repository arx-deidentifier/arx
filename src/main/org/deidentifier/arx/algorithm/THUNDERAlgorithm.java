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

import java.util.Comparator;
import java.util.Iterator;

import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;

import de.linearbits.jhpl.PredictiveProperty;

/**
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 * @author Johanna Eicher
 * @author Helmut Spengler
 */
public class THUNDERAlgorithm extends AbstractAlgorithm{

    /** The maximal size of the priority queue */
    private static final int         MAX_QUEUE_SIZE = 50000;
    /** Property */
    private final PredictiveProperty propertyChecked;
    /** Property */
    private final PredictiveProperty propertyExpanded;
    /** The number indicating how often a depth-first-search will be performed */
    private final int                stepping;
    /** Time limit */
    private final int                timeLimit;
    /** The start time */
    private long                     timeStart;

    /**
    * Constructor
    * @param space
    * @param checker
    * @param timeLimit
    */
    protected THUNDERAlgorithm(SolutionSpace space, NodeChecker checker, int timeLimit) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        int stepping = space.getTop().getLevel();
        this.stepping = stepping > 0 ? stepping : 1;
        this.propertyChecked = space.getPropertyChecked();
        this.propertyExpanded = space.getPropertyExpanded();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.timeLimit = timeLimit;
    }
    
    /**
    * Makes sure that the given Transformation has been checked
    * @param transformation
    */
    private void assureChecked(final Transformation transformation) {
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation));
            trackOptimum(transformation);
        }
    }

    @Override
    public void traverse() {
        timeStart = System.currentTimeMillis();
        MinMaxPriorityQueue<Long> queue = new MinMaxPriorityQueue<Long>(MAX_QUEUE_SIZE, new Comparator<Long>() {
            @Override
            public int compare(Long arg0, Long arg1) {
                return solutionSpace.getUtility(arg0).compareTo(solutionSpace.getUtility(arg1));
            }
        });
        Transformation bottom = solutionSpace.getBottom();
        assureChecked(bottom);
        queue.add(bottom.getIdentifier());
        Transformation next;
        int step = 0;
        while ((next = solutionSpace.getTransformation(queue.poll())) != null) {
            if (!prune(next)) {
                step++;
                if (step % stepping == 0) {
                    dfs(queue, next);
                } else {
                    expand(queue, next);
                }
                if (getTime() > timeLimit) {
                    return;
                }
            }
        }
    }
    
    /**
     * Returns the current execution time
     * @return
     */
    private int getTime() {
        return (int)(System.currentTimeMillis() - timeStart);
    }

    /**
    * Performs a depth first search (without backtracking) starting from the the given transformation
    * @param queue
    * @param transformation
    */
    private void dfs(MinMaxPriorityQueue<Long> queue, Transformation transformation) {
        if (getTime() > timeLimit) {
            return;
        }
        Transformation next = expand(queue, transformation);
        if (next != null) {
            queue.remove(next);
            dfs(queue, next);
        }
    }
    /**
    * Returns the successor with minimal information loss, if any, null otherwise.
    * @param queue
    * @param transformation
    * @return
    */
    private Transformation expand(MinMaxPriorityQueue<Long> queue, Transformation transformation) {
        Transformation result = null;
        for (Iterator<Long> iter = solutionSpace.getSuccessors(transformation.getIdentifier()); iter.hasNext();) {
            Transformation successor = solutionSpace.getTransformation(iter.next());
            if (!successor.hasProperty(propertyExpanded)) {
                assureChecked(successor);
                queue.add(successor.getIdentifier());
                if (result == null || successor.getInformationLoss().compareTo(result.getInformationLoss()) < 0) {
                    result = successor;
                }
            }
            if (getTime() > timeLimit) {
                return null;
            }
            while (queue.size() > MAX_QUEUE_SIZE) {
                queue.removeTail();
            }
        }
        transformation.setProperty(propertyExpanded);
        return result;
    }
    
    /**
    * Returns whether we can prune this Transformation
    * @param transformation
    * @return
    */
    private boolean prune(Transformation transformation) {
        // A Transformation (and it's direct and indirect successors, respectively) can be pruned if
        // the information loss is monotonic and the nodes's IL is greater or equal than the IL of the
        // global maximum (regardless of the anonymity criterion's monotonicity)
        boolean metricMonotonic = checker.getMetric().isMonotonic() || checker.getConfiguration().getAbsoluteMaxOutliers() == 0;
        // Depending on monotony of metric we choose to compare either IL or monotonic subset with the global optimum
        boolean prune = false;
        if (getGlobalOptimum() != null) {
            if (metricMonotonic) prune = transformation.getInformationLoss().compareTo(getGlobalOptimum().getInformationLoss()) >= 0;
        }
        return (prune || transformation.hasProperty(propertyExpanded));
    }
}
