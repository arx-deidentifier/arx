/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import org.deidentifier.arx.dp.ExponentialMechanism;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.TransformationChecker.ScoreType;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.framework.lattice.TransformationList;
import org.deidentifier.arx.metric.v2.ILScore;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

import de.linearbits.jhpl.PredictiveProperty;

/**
 * This class implements the search algorithm used with data-dependent differential privacy as proposed in:
 * Bild R, Kuhn KA, Prasser F. SafePub: A Truthful Data Anonymization Algorithm With Strong Privacy Guarantees.
 * Proceedings on Privacy Enhancing Technologies. 2018(1):67-87.
 * 
 * @author Raffael Bild
 */
public class DataDependentEDDPAlgorithm extends AbstractAlgorithm {

    /** Property */
    private final PredictiveProperty           propertyChecked;

    /** Number of expansions to be performed */
    private final int                          expansionLimit;

    /** The exponential mechanism */
    private final ExponentialMechanism<Object> exponentialMechanism;

    /**
     * Creates a new instance
     * @param solutionSpace
     * @param checker
     * @param deterministic
     * @param expansionLimit
     * @param epsilonSearch
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace<?> solutionSpace, TransformationChecker checker,
                                           boolean deterministic, int expansionLimit, double epsilonSearch) {
        return new DataDependentEDDPAlgorithm(solutionSpace, checker, deterministic, expansionLimit, epsilonSearch);
    }

    /**
     * Constructor
     * @param space
     * @param checker
     * @param deterministic
     * @param expansionLimit
     * @param epsilonSearch
     */
    private DataDependentEDDPAlgorithm(SolutionSpace<?> space, TransformationChecker checker,
                                       boolean deterministic, int expansionLimit, double epsilonSearch) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        this.propertyChecked = space.getPropertyChecked();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.expansionLimit = expansionLimit;
        
        // Calculate the privacy budget to use for each step
        double epsilonPerStep = 0d;
        if (this.expansionLimit != 0) {
            IntervalArithmeticDouble arithmetic = new IntervalArithmeticDouble();
            try {
                epsilonPerStep = arithmetic.div(arithmetic.createInterval(epsilonSearch), arithmetic.createInterval(this.expansionLimit)).lower;
            } catch (IntervalArithmeticException e) {
                throw new RuntimeException(e);
            }
        }
        
        // Initialize the exponential mechanism
        this.exponentialMechanism = new ExponentialMechanism<Object>(epsilonPerStep, deterministic);
    }
    
    @Override
    public boolean traverse() {
        
        // Set the top-transformation to be the initial pivot element
        Transformation<?> pivot = solutionSpace.getTop();
        assureChecked(pivot);
        ILScore score = (ILScore)pivot.getInformationLoss();
        
        // Initialize variables tracking the best of all pivot elements
        Transformation<?> bestTransformation = pivot;
        ILScore bestScore = score;
        
        progress(0d);

        // Initialize the set of candidates, each mapped to its respective score
        Map<Object, ILScore> transformationIDToScore = new HashMap<>();
        transformationIDToScore.put(pivot.getIdentifier(), score);
        
        // For each step
        for (int step = 1; step <= expansionLimit; ++step) {
            
            // Add predecessors of the current pivot element to the set of candidates
            TransformationList<?> list = pivot.getPredecessors();
            for (int i = 0; i < list.size(); i++) {
                Object id = list.getQuick(i);
                if (transformationIDToScore.containsKey(id)) continue;
                Transformation<?> predecessor = solutionSpace.getTransformation(id);
                assureChecked(predecessor);
                transformationIDToScore.put(id, (ILScore)predecessor.getInformationLoss());
            }
            
            // Remove the current pivot element from the set of candidates
            transformationIDToScore.remove(pivot.getIdentifier());
            
            // Select the next pivot element from the set of candidates using the exponential mechanism
            Object id = executeExponentialMechanism(transformationIDToScore, exponentialMechanism);
            pivot = solutionSpace.getTransformation(id);
            score = transformationIDToScore.get(id);
            
            // Keep track of the best pivot element
            if (score.compareTo(bestScore) < 0) {
                bestTransformation = pivot;
                bestScore = score;
            }
            
            progress((double)step / (double)expansionLimit);
        }
        
        // Track optimum
        trackOptimum(bestTransformation);
        return false;
    }
    
    /**
    * Makes sure that the given Transformation<?> has been checked
    * @param transformation
    */
    private void assureChecked(final Transformation<?> transformation) {
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation, true, ScoreType.DP_SCORE));
        }
    }

    /**
     * Executes the exponential mechanism
     * @param transformationIDToScore
     * @param exponentialMechanism 
     * @return
     */
    private Object executeExponentialMechanism(Map<Object, ILScore> transformationIDToScore, ExponentialMechanism<Object> exponentialMechanism) {
        
        // Convert the map into arrays of the types required by the exponential mechanism
        Object[] values = new Object[transformationIDToScore.size()];
        double[] scores = new double[values.length];

        int i = 0;
        for (Entry<Object, ILScore> entry : transformationIDToScore.entrySet()) {
            values[i] = entry.getKey();
            scores[i] = toDouble(entry.getValue().getValue());
            i++;
        }

        // Set the probability distribution
        exponentialMechanism.setDistribution(values, scores);
        
        // Select and return a value
        return exponentialMechanism.sample();
    }
    
    /**
     * Tries converting fraction into a double which is within one ulp of the exact result.
     * If this is not possible, an exception is thrown.
     * @param fraction
     * @return
     */
    private double toDouble(BigFraction fraction) {
        double d = fraction.doubleValue();
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new RuntimeException("Encountered a value which can not be represented as a double");
        }
        if (fraction.subtract(new BigFraction(d)).abs().compareTo(new BigFraction(Math.ulp(d))) > 0) {
            throw new RuntimeException("Encountered a value with insufficient precision");
        }
        return d;
    }
}
