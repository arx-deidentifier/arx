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

import org.deidentifier.arx.dp.ExponentialMechanism;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;

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
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace solutionSpace, TransformationChecker checker,
                                           boolean deterministic, int steps, double epsilonSearch) {
        return new DataDependentEDDPAlgorithm(solutionSpace, checker, deterministic, steps, epsilonSearch);
    }
    /** Property */
    private final PredictiveProperty propertyChecked;
    /** Number of steps to be performed */
    private final int                steps;
    /** Parameter */
    private final double             epsilonSearch;
    /** Parameter */
    private final boolean            deterministic;
    
    /**
    * Constructor
    * @param space
    * @param checker
    * @param metric
    * @param deterministic 
    * @param steps
    * @param epsilonSearch
    */
    private DataDependentEDDPAlgorithm(SolutionSpace space, TransformationChecker checker,
                                       boolean deterministic, int steps, double epsilonSearch) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        this.propertyChecked = space.getPropertyChecked();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.epsilonSearch = epsilonSearch;
        this.deterministic = deterministic;
        this.steps = steps;
    }
    
    @Override
    public boolean traverse() {
        
        // Set the top-transformation to be the initial pivot element
        Transformation pivot = solutionSpace.getTop();
        assureChecked(pivot);
        double score = (Double)pivot.getInformationLoss().getValue();
        
        // Initialize variables tracking the best of all pivot elements
        Transformation bestTransformation = pivot;
        double bestScore = score;
        
        progress(0d);

        // Initialize the set of candidates, each mapped to its respective score
        Map<Long, Double> transformationIDToScore = new HashMap<Long, Double>();
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
                transformationIDToScore.put(id, (Double)predecessor.getInformationLoss().getValue());
            }
            
            // Remove the current pivot element from the set of candidates
            transformationIDToScore.remove(pivot.getIdentifier());
            
            // Abort if there are no candidates left
            if (transformationIDToScore.isEmpty()) {
            	break;
            }
            
            // Select the next pivot element from the set of candidates using the exponential mechanism

            ExponentialMechanism<Long> expMechanism = new ExponentialMechanism<Long>(transformationIDToScore,
                    epsilonSearch / ((double) steps), ExponentialMechanism.defaultPrecision, deterministic);

            long id = expMechanism.sample();
            pivot = solutionSpace.getTransformation(id);
            score = transformationIDToScore.get(id);
            
            // Keep track of the best pivot element
            if (score > bestScore) {
                bestTransformation = pivot;
                bestScore = score;
            }
            
            progress((double)step / (double)steps);
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
            transformation.setChecked(checker.check(transformation, true, true));
        }
    }
}
