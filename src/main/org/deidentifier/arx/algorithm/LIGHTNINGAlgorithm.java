/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2020 Fabian Prasser and contributors
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.TransformationChecker.ScoreType;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.framework.lattice.TransformationList;
import org.deidentifier.arx.metric.InformationLoss;

import de.linearbits.jhpl.PredictiveProperty;

/**
 * Lightning algorithm as described in:<br>
 * <br>
 * Prasser, F., Bild, R., Eicher, J., Spengler, H., Kohlmayer, F., & Kuhn, K. A. (2016). 
 * Lightning: Utility-Driven Anonymization of High-Dimensional Data.
 * Transactions on Data Privacy, 9(2), 161-185.
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
    public static AbstractAlgorithm create(SolutionSpace<?> solutionSpace, TransformationChecker checker, int timeLimit, int checkLimit) {
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
    
    /**
    * Constructor
    * @param space
    * @param checker
    * @param timeLimit
    * @param checkLimit
    */
    protected LIGHTNINGAlgorithm(SolutionSpace<?> space, TransformationChecker checker, int timeLimit, int checkLimit) {
        super(space, checker, timeLimit, checkLimit);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        int stepping = space.getTop().getLevel();
        this.stepping = stepping > 0 ? stepping : 1;
        this.propertyChecked = space.getPropertyChecked();
        this.propertyExpanded = space.getPropertyExpanded();
        this.propertyInsufficientUtility = space.getPropertyInsufficientUtility();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
    }

    @Override
    public boolean traverse() {

        // Prepare
        super.startTraverse();
        PriorityQueue<Object> queue = new PriorityQueue<>(stepping, new Comparator<Object>() {
            @Override
            public int compare(Object arg0, Object arg1) {
                return solutionSpace.getUtility(arg0).compareTo(solutionSpace.getUtility(arg1));
            }
        });
        Transformation<?> bottom = solutionSpace.getBottom();
        assureChecked(bottom);
        queue.add(bottom.getIdentifier());
        
        // Start bottom-up best-first search combined with depth-first search
        int step = 0;
        Object nextId;
        while ((nextId = queue.poll()) != null) {
            Transformation<?> next = solutionSpace.getTransformation(nextId);
            if (!prune(next, true)) {
                step++;
                if (step % stepping == 0) {
                    dfs(queue, next);
                } else {
                    expand(queue, next, true);
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
    * Makes sure that the given Transformation<?> has been checked
    * @param transformation
    */
    protected void assureChecked(final Transformation<?> transformation) {
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation, true, ScoreType.INFORMATION_LOSS));
            trackOptimum(transformation);
            trackProgressFromLimits();
        }
    }

    /**
    * Performs a depth first search (without backtracking) starting from the the given transformation
    * @param queue
    * @param transformation
    */
    private void dfs(PriorityQueue<Object> queue, Transformation<?> transformation) {
        if (mustStop()) {
            return;
        }
        Transformation<?> next = expand(queue, transformation, true);
        if (next != null) {
            queue.remove(next.getIdentifier());
            dfs(queue, next);
        }
    }
    
    /**
    * Returns the successor with minimal information loss, if any, null otherwise.
    * @param queue
    * @param transformation
    * @param up
    * @return
    */
    protected Transformation<?> expand(PriorityQueue<Object> queue, Transformation<?> transformation, boolean up) {
        
        Transformation<?> result = null;
        TransformationList<?> list = up ? transformation.getSuccessors() : transformation.getPredecessors();
        for (int i = 0; i < list.size(); i++) {
            Object id = list.getQuick(i);
            Transformation<?> successor = solutionSpace.getTransformation(id);
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
    * Returns whether we can prune this Transformation
    * @param transformation
    * @param up whether this is a bottom-up search
    * @return
    */
    protected boolean prune(Transformation<?> transformation, boolean up) {
        
        // Already expanded
        if (transformation.hasProperty(propertyExpanded) || (up && transformation.hasProperty(propertyInsufficientUtility))){
            return true;
        }

        if (up) {
            // If a current optimum has been discovered
            Transformation<?> optimum = getGlobalOptimum();
            if (optimum != null && !Arrays.equals(optimum.getGeneralization(), transformation.getGeneralization())) {
                
                // We can compare lower bounds on quality
                InformationLoss<?> bound = transformation.getLowerBound();
                if (bound != null && bound.compareTo(optimum.getInformationLoss()) >= 0) {
                    transformation.setProperty(propertyInsufficientUtility);
                    return true;
                }
            }
        }
        
        // We have to process this transformation
        return false;
    }
}