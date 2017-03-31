/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.ARXResult.ScoreType;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;

import cern.colt.list.LongArrayList;
import de.linearbits.jhpl.PredictiveProperty;

/**
 * 
 * @author Raffael Bild
 */
public class EDDPAlgorithm extends AbstractAlgorithm{

    /**
     * Creates a new instance
     * @param solutionSpace
     * @param checker
     * @param classIndex
     * @param definition
     * @param steps
     * @param epsilonSearch
     * @param scoreType 
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace solutionSpace, NodeChecker checker, int classIndex,
                                           DataDefinition definition, int steps, double epsilonSearch, ScoreType scoreType) {
        return new EDDPAlgorithm(solutionSpace, checker, classIndex, definition, steps, epsilonSearch, scoreType);
    }
    /** Property */
    private final PredictiveProperty propertyChecked;
    /** Number of steps to be performed */
    private final int                steps;
    /** Parameter */
    private final double             epsilonSearch;
    /** Data definition */
    private final DataDefinition     definition;
    /** Score type */
    private final ScoreType          scoreType;
    /** Parameter */
    private final int                classIndex;
    
    /**
    * Constructor
    * @param space
    * @param checker
    * @param classIndex
    * @param definition
    * @param steps
    * @param epsilonSearch
    * @param scoreType 
    */
    private EDDPAlgorithm(SolutionSpace space, NodeChecker checker, int classIndex,
                          DataDefinition definition, int steps, double epsilonSearch, ScoreType scoreType) {
        super(space, checker);
        this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
        this.propertyChecked = space.getPropertyChecked();
        this.solutionSpace.setAnonymityPropertyPredictable(false);
        this.epsilonSearch = epsilonSearch;
        this.definition = definition;
        this.scoreType = scoreType;
        this.classIndex = classIndex;
        this.steps = steps;
        if (steps < 0) { 
            throw new IllegalArgumentException("Invalid step number. Must not be negative."); 
        }
    }
    
    @Override
    public void traverse() {
        
        Transformation transformation = solutionSpace.getTop();
        assureChecked(transformation, 0);
        
        Map<Long, Double> transformationIDToScore = new HashMap<Long, Double>();
        transformationIDToScore.put(transformation.getIdentifier(), getScore(transformation));
        
        for (int step = 1; step <= steps; ++step) {
            
            LongArrayList list = transformation.getPredecessors();
            for (int i = 0; i < list.size(); i++) {
                long id = list.getQuick(i);
                if (transformationIDToScore.containsKey(id)) continue;
                Transformation predecessor = solutionSpace.getTransformation(id);
                transformationIDToScore.put(predecessor.getIdentifier(), getScore(predecessor));
            }
            
            transformationIDToScore.remove(transformation.getIdentifier());
            
            ExponentialMechanism<Long> expMechanism = new ExponentialMechanism<Long>(transformationIDToScore,
                    epsilonSearch / ((double) steps));
            
            transformation = solutionSpace.getTransformation(expMechanism.sample());
            assureChecked(transformation, step);
        }
    }
    
    /**
     * Returns the score for the given transformation
     * @param transformation
     * @return
     */
    private Double getScore(Transformation transformation) {
        return checker.getScore(definition, transformation, scoreType, classIndex);
    }

    /**
    * Makes sure that the given Transformation has been checked
    * @param transformation
    * @param step
    */
    private void assureChecked(final Transformation transformation, int step) {
        if (!transformation.hasProperty(propertyChecked)) {
            transformation.setChecked(checker.check(transformation, true));
            trackOptimum(transformation);
            progress((double)(steps) / (double)step);
        }
    }
}
