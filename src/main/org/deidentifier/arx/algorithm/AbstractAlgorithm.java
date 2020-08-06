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

import org.deidentifier.arx.ARXConfiguration.Monotonicity;
import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.TransformationChecker.ScoreType;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.InformationLossWithBound;

/**
 * Abstract class for an algorithm, which provides some generic methods.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractAlgorithm {

    /** The optimal transformation. */
    private Transformation<?>       globalOptimum          = null;

    /** The optimal information loss. */
    private InformationLoss<?>      optimalInformationLoss = null;

    /** The listener */
    private ARXListener             listener               = null;

    /** A node checker. */
    protected TransformationChecker checker                = null;

    /** The lattice. */
    protected SolutionSpace<?>      solutionSpace          = null;

    /** Time limit */
    private final int               timeLimit;

    /** The start time */
    private long                    timeStart;

    /** The number of checks */
    private final int               checkLimit;

    /**
     * Initializes the algorithm
     * 
     * @param solutionSpace The solution space
     * @param checker The checker
     */
    protected AbstractAlgorithm(final SolutionSpace<?>  solutionSpace,
                                final TransformationChecker checker, 
                                final int timeLimit, 
                                final int checkLimit) {
        this.checker = checker;
        this.solutionSpace = solutionSpace;
        this.timeLimit = timeLimit;
        this.checkLimit = checkLimit;
        
        if (timeLimit <= 0) { 
            throw new IllegalArgumentException("Invalid time limit. Must be greater than zero."); 
        }
        if (checkLimit <= 0) { 
            throw new IllegalArgumentException("Invalid step limit. Must be greater than zero."); 
        }
    }

    /**
     * Return stuff for progress monitoring
     * @return
     */
    public int getCheckCount() {
        return checker.getNumChecksPerformed();
    }

    /**
     * Return stuff for progress monitoring
     * @return
     */
    public int getCheckLimit() {
        return checkLimit;
    }
    
    /**
     * Returns the global optimum.
     *
     * @return
     */
    public Transformation<?> getGlobalOptimum() {
        return globalOptimum;
    }

    /**
     * Return stuff for progress monitoring
     * @return
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * Return stuff for progress monitoring
     * @return
     */
    public long getTimeStart() {
        return timeStart;
    }

    /**
     * Sets a listener
     * @param listener
     */
    public void setListener(ARXListener listener) {
        this.listener = listener;
    }

    /**
     * Implement this method in order to provide a new algorithm.
     * 
     * @return Whether the result is optimal
     */
    public abstract boolean traverse();

    /**
     * Determine information loss implied by the given transformation if it can be
     * used for estimating minimum and maximum information loss for tagged nodes.
     *
     * @param transformation
     */
    protected void computeUtilityForMonotonicMetrics(Transformation<?> transformation) {
        if (checker.getConfiguration().getMonotonicityOfUtility() == Monotonicity.FULL &&
            transformation.getInformationLoss() == null) {

            // Independent evaluation or check
            if (checker.getMetric().isIndependent()) {
                InformationLossWithBound<?> loss = checker.getMetric().getInformationLoss(transformation, (HashGroupify)null);
                transformation.setInformationLoss(loss.getInformationLoss());
                transformation.setLowerBound(loss.getLowerBound());
            } else {
                transformation.setChecked(checker.check(transformation, true, ScoreType.INFORMATION_LOSS));
            }
        }
    }

    /**
     * Returns whether we have exceeded the allowed number of steps or time.
     * @return
     */
    protected boolean mustStop() {
        return ((int)(System.currentTimeMillis() - timeStart) > timeLimit) ||
               (checker.getNumChecksPerformed() >= checkLimit);
    }

    /**
     * Propagate progress to listeners
     * @param progress
     */
    protected void progress(double progress) {
        if (this.listener != null) {
            this.listener.progress(progress);
        }
    }

    /**
     * Call before traversal
     */
    protected void startTraverse() {
        timeStart = System.currentTimeMillis();
    }

    /**
     * Keeps track of the global optimum.
     *
     * @param transformation
     */
    protected void trackOptimum(Transformation<?> transformation) {
        if (transformation.hasProperty(solutionSpace.getPropertyAnonymous()) &&
            ((globalOptimum == null) ||
             (transformation.getInformationLoss().compareTo(optimalInformationLoss) < 0) ||
            ((transformation.getInformationLoss().compareTo(optimalInformationLoss) == 0) && (transformation.getLevel() < globalOptimum.getLevel())))) {
            globalOptimum = transformation;
            optimalInformationLoss = transformation.getInformationLoss();
        }
    }

    /**
     * Track progress from limits
     */
    protected void trackProgressFromLimits() {
        trackProgressFromLimits(0d);
    }
    
    /**
     * Track progress from limits
     * @param algorithmProgress 
     */
    protected void trackProgressFromLimits(double algorithmProgress) {
        double progressSteps = (double)getCheckCount() / (double)getCheckLimit();
        double progressTime = (double)(System.currentTimeMillis() - getTimeStart()) / (double)getTimeLimit();
        progress(Math.min(1.0d, Math.max(algorithmProgress, Math.max(progressSteps, progressTime))));
    }
}
