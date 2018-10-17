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
import org.deidentifier.arx.metric.v2.ILScore;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

import cern.colt.list.LongArrayList;
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
    private final PredictiveProperty         propertyChecked;

    /** Number of expansions to be performed */
    private final int                        expansionLimit;

    /** Privacy budget to use for each execution of the exponential mechanism */
    private final double                     epsilonPerStep;

    /** True iff this algorithm should be executed in a deterministic manner */
    private final boolean                    deterministic;

    /**
     * Creates a new instance
     * @param solutionSpace
     * @param checker
     * @param deterministic
     * @param expansionLimit
     * @param epsilonSearch
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace solutionSpace, TransformationChecker checker,
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
    private DataDependentEDDPAlgorithm(SolutionSpace space, TransformationChecker checker,
                                       boolean deterministic, int expansionLimit, double epsilonSearch) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        this.propertyChecked = space.getPropertyChecked();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.deterministic = deterministic;
        this.expansionLimit = expansionLimit;
        
        if (this.expansionLimit == 0) {
            // Avoid division by zero
            this.epsilonPerStep = 0d;
        } else {
            IntervalArithmeticDouble arithmetic = new IntervalArithmeticDouble();
            try {
                this.epsilonPerStep = arithmetic.div(arithmetic.createInterval(epsilonSearch), arithmetic.createInterval(this.expansionLimit)).lower;
            } catch (IntervalArithmeticException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public boolean traverse() {
        
        // Create a new local ExponentialMechanism instance in order to reduce memory consumption caused
        // by internal caches used by this class
        ExponentialMechanism<Long> exponentialMechanism;
        try {
            exponentialMechanism = new ExponentialMechanism<Long>(epsilonPerStep, deterministic);
        } catch (IntervalArithmeticException e) {
            throw new RuntimeException(e);
        }
        
        // Set the top-transformation to be the initial pivot element
        Transformation pivot = solutionSpace.getTop();
        assureChecked(pivot);
        ILScore score = (ILScore)pivot.getInformationLoss();
        
        // Initialize variables tracking the best of all pivot elements
        Transformation bestTransformation = pivot;
        ILScore bestScore = score;
        
        progress(0d);

        // Initialize the set of candidates, each mapped to its respective score
        Map<Long, ILScore> transformationIDToScore = new HashMap<Long, ILScore>();
        transformationIDToScore.put(pivot.getIdentifier(), score);
        
        // For each step
        for (int step = 1; step <= expansionLimit; ++step) {
            
            // Add predecessors of the current pivot element to the set of candidates
            LongArrayList list = pivot.getPredecessors();
            for (int i = 0; i < list.size(); i++) {
                long id = list.getQuick(i);
                if (transformationIDToScore.containsKey(id)) continue;
                Transformation predecessor = solutionSpace.getTransformation(id);
                assureChecked(predecessor);
                transformationIDToScore.put(id, (ILScore)predecessor.getInformationLoss());
            }
            
            // Remove the current pivot element from the set of candidates
            transformationIDToScore.remove(pivot.getIdentifier());
            
            // Select the next pivot element from the set of candidates using the exponential mechanism
            long id = executeExponentialMechanism(transformationIDToScore, exponentialMechanism);
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
    * Makes sure that the given Transformation has been checked
    * @param transformation
    */
    private void assureChecked(final Transformation transformation) {
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
    private long executeExponentialMechanism(Map<Long, ILScore> transformationIDToScore, ExponentialMechanism<Long> exponentialMechanism) {
        
        // Convert the map into arrays of the types required by the exponential mechanism

        Long[] values = new Long[transformationIDToScore.size()];
        BigFraction[] scores = new BigFraction[values.length];
        
        int i = 0;
        for (Entry<Long, ILScore> entry : transformationIDToScore.entrySet()) {
            values[i] = entry.getKey();
            scores[i] = entry.getValue().getValue();
            i++;
        }

        // Set the probability distribution
        exponentialMechanism.setDistribution(values, scores);
        
        // Select and return a value
        return exponentialMechanism.sample();
    }
}
