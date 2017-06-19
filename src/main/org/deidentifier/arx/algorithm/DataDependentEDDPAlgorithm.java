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

import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.Metric;

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
     * @param scoreType 
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace solutionSpace, NodeChecker checker,
                                           Metric<?> metric, boolean deterministic, int steps, double epsilonSearch) {
        return new DataDependentEDDPAlgorithm(solutionSpace, checker, metric, deterministic, steps, epsilonSearch);
    }
    /** Property */
    private final PredictiveProperty propertyChecked;
    /** Number of steps to be performed */
    private final int                steps;
    /** Parameter */
    private final double             epsilonSearch;
    /** The metric */
    private final Metric<?>          metric;
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
    private DataDependentEDDPAlgorithm(SolutionSpace space, NodeChecker checker,
                          Metric<?> metric, boolean deterministic, int steps, double epsilonSearch) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        this.propertyChecked = space.getPropertyChecked();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.epsilonSearch = epsilonSearch;
        this.metric = metric;
        this.deterministic = deterministic;
        this.steps = steps;
        if (steps < 0) { 
            throw new IllegalArgumentException("Invalid step number. Must not be negative."); 
        }
    }
    
    @Override
    public void traverse() {
        
        Transformation transformation = solutionSpace.getTop();
        double score = getScore(transformation);
        
        Transformation bestTransformation = transformation;
        double bestScore = score;
        progress(0d);

        Map<Long, Double> transformationIDToScore = new HashMap<Long, Double>();
        transformationIDToScore.put(transformation.getIdentifier(), score);
        
        for (int step = 1; step <= steps; ++step) {
            
            LongArrayList list = transformation.getPredecessors();
            for (int i = 0; i < list.size(); i++) {
                long id = list.getQuick(i);
                if (transformationIDToScore.containsKey(id)) continue;
                Transformation predecessor = solutionSpace.getTransformation(id);
                transformationIDToScore.put(id, getScore(predecessor));
            }
            
            transformationIDToScore.remove(transformation.getIdentifier());

            ExponentialMechanism<Long> expMechanism = new ExponentialMechanism<Long>(transformationIDToScore,
                    epsilonSearch / ((double) steps), ExponentialMechanism.defaultPrecision, deterministic);
            
            long id = expMechanism.sample();
            transformation = solutionSpace.getTransformation(id);
            score = transformationIDToScore.get(id);
            
            if (score > bestScore) {
                bestTransformation = transformation;
                bestScore = score;
            }
            
            progress((double)step / (double)steps);
        }
        
        assureChecked(bestTransformation);
    }
    
    /**
     * Returns the score for the given transformation
     * @param transformation
     * @return
     */
    private Double getScore(Transformation transformation) {
        return checker.getScore(transformation, metric);
    }

    /**
    * Makes sure that the given Transformation has been checked
    * @param transformation
    */
    private void assureChecked(final Transformation transformation) {
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation, true));
            trackOptimum(transformation);
        }
    }
}
