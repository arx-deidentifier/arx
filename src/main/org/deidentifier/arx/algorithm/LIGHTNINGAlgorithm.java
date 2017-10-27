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

import java.util.Comparator;
import java.util.PriorityQueue;

import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;

import cern.colt.list.LongArrayList;
import de.linearbits.jhpl.PredictiveProperty;

/**
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 * @author Johanna Eicher
 * @author Helmut Spengler
 */
public class LIGHTNINGAlgorithm extends AbstractAlgorithm{

    /**
     * Creates a new instance
     * @param solutionSpace
     * @param checker
     * @param timeLimit
     * @param checkLimit 
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace solutionSpace, TransformationChecker checker, int timeLimit, int checkLimit) {
        return new LIGHTNINGAlgorithm(solutionSpace, checker, timeLimit, checkLimit);
    }

    /** Property */
    private final PredictiveProperty propertyChecked;
    /** Property */
    private final PredictiveProperty propertyExpanded;
    /** Property */
    private final PredictiveProperty propertyInsufficientUtility;

    /** The number indicating how often a depth-first-search will be performed */
    private final int                stepping;
    /** Time limit */
    private final int                timeLimit;
    /** The start time */
    private long                     timeStart;
    /** The number of checks */
    private int                      checkCount;
    /** The number of checks */
    private final int                checkLimit;
    
    /**
    * Constructor
    * @param space
    * @param checker
    * @param timeLimit
    * @param checkLimit
    */
    private LIGHTNINGAlgorithm(SolutionSpace space, TransformationChecker checker, int timeLimit, int checkLimit) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        int stepping = space.getTop().getLevel();
        this.stepping = stepping > 0 ? stepping : 1;
        this.propertyChecked = space.getPropertyChecked();
        this.propertyExpanded = space.getPropertyExpanded();
        this.propertyInsufficientUtility = space.getPropertyInsufficientUtility();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.timeLimit = timeLimit;
        this.checkLimit = checkLimit;
        if (timeLimit <= 0) { 
            throw new IllegalArgumentException("Invalid time limit. Must be greater than zero."); 
        }
        if (checkLimit <= 0) { 
            throw new IllegalArgumentException("Invalid step limit. Must be greater than zero."); 
        }
    }

    @Override
    public boolean traverse() {
        timeStart = System.currentTimeMillis();
        checkCount = 0;
        PriorityQueue<Long> queue = new PriorityQueue<Long>(stepping, new Comparator<Long>() {
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
        Long nextId;
        while ((nextId = queue.poll()) != null) {
            next = solutionSpace.getTransformation(nextId);
            if (!prune(next)) {
                step++;
                if (step % stepping == 0) {
                    dfs(queue, next);
                } else {
                    expand(queue, next);
                }
                if (mustStop()) {
                    break;
                }
            }
        }
        

        // Return whether the optimum has been found
        return !this.mustStop() && (this.getGlobalOptimum() != null);
    }
    
    /**
    * Makes sure that the given Transformation has been checked
    * @param transformation
    */
    private void assureChecked(final Transformation transformation) {
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation, true, false));
            trackOptimum(transformation);
            checkCount++;
            double progressSteps = (double)checkCount / (double)checkLimit;
            double progressTime = (double)(System.currentTimeMillis() - timeStart) / (double)timeLimit;
            progress(Math.max(progressSteps, progressTime));
        }
    }

    /**
    * Performs a depth first search (without backtracking) starting from the the given transformation
    * @param queue
    * @param transformation
    */
    private void dfs(PriorityQueue<Long> queue, Transformation transformation) {
        if (mustStop()) {
            return;
        }
        Transformation next = expand(queue, transformation);
        if (next != null) {
            queue.remove(next.getIdentifier());
            dfs(queue, next);
        }
    }
    
    /**
    * Returns the successor with minimal information loss, if any, null otherwise.
    * @param queue
    * @param transformation
    * @return
    */
    private Transformation expand(PriorityQueue<Long> queue, Transformation transformation) {
        
        Transformation result = null;
        LongArrayList list = transformation.getSuccessors();
        for (int i = 0; i < list.size(); i++) {
            long id = list.getQuick(i);
            Transformation successor = solutionSpace.getTransformation(id);
            if (!successor.hasProperty(propertyExpanded) && !successor.hasProperty(propertyInsufficientUtility)) {
                assureChecked(successor);
                queue.add(successor.getIdentifier());
                if (result == null || successor.getInformationLoss().compareTo(result.getInformationLoss()) < 0) {
                    result = successor;
                }
            }
            if (mustStop()) {
                return null;
            }
        }
        transformation.setProperty(propertyExpanded);
        return result;
    }
    
    /**
     * Returns whether we have exceeded the allowed number of steps or time.
     * @return
     */
    private boolean mustStop() {
        return ((int)(System.currentTimeMillis() - timeStart) > timeLimit) ||
               (checkCount >= checkLimit);
    }

    /**
    * Returns whether we can prune this Transformation
    * @param transformation
    * @return
    */
    private boolean prune(Transformation transformation) {
        
        // Already expanded
        if (transformation.hasProperty(propertyExpanded) ||
            transformation.hasProperty(propertyInsufficientUtility)){
            return true;
        }
        
        // If a current optimum has been discovered
        Transformation optimum = getGlobalOptimum();
        if (optimum != null) {
            
            // We can compare lower bounds on quality
            InformationLoss<?> bound = transformation.getLowerBound();
            if (bound.compareTo(optimum.getInformationLoss()) >= 0) {
                transformation.setProperty(propertyInsufficientUtility);
                return true;
            }
        }
        
        // We have to process this transformation
        return false;
    }
}