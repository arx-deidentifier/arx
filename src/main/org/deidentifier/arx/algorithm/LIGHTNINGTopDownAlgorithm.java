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

import java.util.Comparator;
import java.util.PriorityQueue;

import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * Top-down version of the lightning algorithm.
 * 
 * @author Fabian Prasser
 */
public class LIGHTNINGTopDownAlgorithm extends LIGHTNINGAlgorithm {

    /**
     * Creates a new instance
     * @param solutionSpace
     * @param checker
     * @param timeLimit
     * @param checkLimit 
     * @return
     */
    public static AbstractAlgorithm create(SolutionSpace<?> solutionSpace, TransformationChecker checker, int timeLimit, int checkLimit) {
        return new LIGHTNINGTopDownAlgorithm(solutionSpace, checker, timeLimit, checkLimit);
    }

    /**
    * Constructor
    * @param space
    * @param checker
    * @param timeLimit
    * @param checkLimit
    */
    private LIGHTNINGTopDownAlgorithm(SolutionSpace<?> space, TransformationChecker checker, int timeLimit, int checkLimit) {
        super(space, checker, timeLimit, checkLimit);
    }

    @Override
    public boolean traverse() {

        // Prepare
        super.startTraverse();
        PriorityQueue<Object> queue = new PriorityQueue<>(new Comparator<Object>() {
            @Override
            public int compare(Object arg0, Object arg1) {
                return solutionSpace.getUtility(arg0).compareTo(solutionSpace.getUtility(arg1));
            }
        });
        Transformation<?> top = solutionSpace.getTop();
        assureChecked(top);
        queue.add(top.getIdentifier());
        
        // Start top-down best-first search
        Object nextId;
        while ((nextId = queue.poll()) != null) {
            Transformation<?> next = solutionSpace.getTransformation(nextId);
            if (!prune(next, false)) {
                expand(queue, next, false);
                if (mustStop()) {
                    break;
                }
            }
        }
        
        // Return whether the optimum has been found
        return !this.mustStop() && (this.getGlobalOptimum() != null);
    }
}