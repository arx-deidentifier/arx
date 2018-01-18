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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.dp.AbstractExponentialMechanism;
import org.deidentifier.arx.dp.ExponentialMechanism;
import org.deidentifier.arx.dp.ExponentialMechanismReliable;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.TransformationChecker.InformationLossSource;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.v2.ILScore;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

import cern.colt.list.LongArrayList;
import de.linearbits.jhpl.PredictiveProperty;

/**
 * This class implements the search algorithm used with data-dependent differential privacy
 * 
 * @author Raffael Bild
 */
public class DataDependentEDDPAlgorithm extends AbstractAlgorithm{

    /**
     * Creates a new instance
     * @param solutionSpace
     * @param checker
     * @param metric 
     * @param deterministic 
     * @param steps
     * @param epsilonSearch
     * @param reliable 
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace solutionSpace, TransformationChecker checker,
                                           boolean deterministic, int steps, double epsilonSearch, boolean reliable) {
        return new DataDependentEDDPAlgorithm(solutionSpace, checker, deterministic, steps, epsilonSearch, reliable);
    }

    /** True iff this instance is deterministic */
    private final boolean            deterministic;

    /** The privacy budget to use for each step */
    private final double             epsilonStep;

    /** Property */
    private final PredictiveProperty propertyChecked;

    /** True iff this instance is reliable */
    private final boolean            reliable;

    /** Number of steps to be performed */
    private final int                steps;
    
    /** The expopnential mechanism */
    private final AbstractExponentialMechanism<Long,?> exponentialMechanism;

    /**
     * Constructor
     * @param space
     * @param checker
     * @param metric
     * @param deterministic 
     * @param steps
     * @param epsilonSearch
     * @param reliable 
     */
    private DataDependentEDDPAlgorithm(SolutionSpace space, TransformationChecker checker,
                                       boolean deterministic, int steps, double epsilonSearch, boolean reliable) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        this.propertyChecked = space.getPropertyChecked();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.deterministic = deterministic;
        this.steps = steps;
        this.reliable = reliable;

        if (reliable) {
            IntervalArithmeticDouble arithmetic = new IntervalArithmeticDouble();
            try {
                epsilonStep = arithmetic.div(arithmetic.createInterval(epsilonSearch), arithmetic.createInterval(steps)).getLowerBound();
            } catch (IntervalArithmeticException e) {
                throw new RuntimeException(e);
            }
        } else {
            epsilonStep = epsilonSearch / ((double)steps); 
        }
        

        try {
            this.exponentialMechanism = reliable ?
                    new ExponentialMechanismReliable<Long>(epsilonStep, deterministic) :
                    new ExponentialMechanism<Long>(epsilonStep, ExponentialMechanism.defaultPrecision, deterministic);
        } catch(IntervalArithmeticException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean traverse() {

        // Set the top-transformation to be the initial pivot element
        Transformation pivot = solutionSpace.getTop();
        assureChecked(pivot);
        ILScore<?> score = (ILScore<?>)pivot.getInformationLoss();

        // Initialize variables tracking the best of all pivot elements
        Transformation bestTransformation = pivot;
        ILScore<?> bestScore = score;

        progress(0d);

        // Initialize the set of candidates, each mapped to its respective score
        Map<Long, ILScore<?>> transformationIDToScore = new HashMap<Long, ILScore<?>>();
        transformationIDToScore.put(pivot.getIdentifier(), score);

        // For each step
        for (int step = 1; step <= steps; ++step) {

            // Add predecessors of the current pivot element to the set of candidates
            LongArrayList list = pivot.getPredecessors();
            for (int i = 0; i < list.size(); i++) {
                long id = list.getQuick(i);
                if (transformationIDToScore.containsKey(id)) continue;
                Transformation predecessor = solutionSpace.getTransformation(id);
                assureChecked(predecessor);
                transformationIDToScore.put(id, (ILScore<?>)predecessor.getInformationLoss());
            }

            // Remove the current pivot element from the set of candidates
            transformationIDToScore.remove(pivot.getIdentifier());

            // Select the next pivot element from the set of candidates using the exponential mechanism
            long id = executeExponentialMechanism(transformationIDToScore);
            pivot = solutionSpace.getTransformation(id);
            score = transformationIDToScore.get(id);

            // Keep track of the best pivot element
            if (score.compareTo(bestScore) < 0) {
                bestTransformation = pivot;
                bestScore = score;
            }

            progress((double)step / (double)steps);
        }

        // Track optimum
        trackOptimum(bestTransformation);
        return false;
    }

    @SuppressWarnings("unchecked")
    private long executeExponentialMechanism(Map<Long, ILScore<?>> transformationIDToScore) {

        if(reliable) {
            Long[] values = new Long[transformationIDToScore.size()];
            BigFraction[] scores = new BigFraction[transformationIDToScore.size()];

            int index = 0;
            for (Entry<Long, ILScore<?>> element : transformationIDToScore.entrySet()) {
                values[index] = element.getKey();
                scores[index] = ((ILScore<BigFraction>)element.getValue()).getValue();
                index++;
            }

            ((ExponentialMechanismReliable<Long>)exponentialMechanism).setDistribution(values, scores);
        } else {
            Long[] values = new Long[transformationIDToScore.size()];
            Double[] scores = new Double[transformationIDToScore.size()];

            int index = 0;
            for (Entry<Long, ILScore<?>> element : transformationIDToScore.entrySet()) {
                values[index] = element.getKey();
                scores[index] = ((ILScore<Double>)element.getValue()).getValue();
                index++;
            }

            ((ExponentialMechanism<Long>)exponentialMechanism).setDistribution(values, scores);
        }
        
        return exponentialMechanism.sample();
    }

    /**
     * Makes sure that the given Transformation has been checked
     * @param transformation
     */
    private void assureChecked(final Transformation transformation) {
        InformationLossSource ilSource = reliable ? InformationLossSource.SCORE_RELIABLE : InformationLossSource.SCORE;
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation, true, ilSource));
        }
    }
}