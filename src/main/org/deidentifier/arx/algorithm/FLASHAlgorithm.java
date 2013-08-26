/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.algorithm;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;

/**
 * This class provides a static method for instantiating the FLASH algorithm
 * 
 * @author Prasser, Kohlmayer
 */
public class FLASHAlgorithm {

    /**
     * Creates a new instance of the FLASH algorithm
     * @param lattice
     * @param checker
     * @param strategy
     * @return
     */
    public static AbstractAlgorithm create(final Lattice lattice,
                                           final INodeChecker checker,
                                           final FLASHStrategy strategy){
        
        // NOTE: 
        // - If we assume practical monotonicity then we assume
        //   monotonicity for both criterion AND metric
        // - Without suppression we assume monotonicity for all criteria
        
        if ((checker.getConfiguration().getAbsoluteMaxOutliers() == 0) ||
            (checker.getConfiguration().isCriterionMonotonic() && checker.getMetric().isMonotonic()) ||
            (checker.getConfiguration().isPracticalMonotonicity())) {
            
            /* Binary FLASH*/
            return new FLASHAlgorithmBinary(lattice, checker, strategy);
            
        } else {
            if (checker.getConfiguration().getMinimalGroupSize() != Integer.MAX_VALUE) {
                
               /* Two-Phase FLASH*/
               return new FLASHAlgorithmTwoPhases(lattice, checker, strategy);
               
            } else {
                
                /* Linear FLASH*/
                return new FLASHAlgorithmLinear(lattice, checker, strategy);
                
            }
        }
    }
    
    /**
     * Creates a new instance based on a previous one for repeated executions
     * @param previous
     * @param checker
     * @return
     */
    public static AbstractAlgorithm create(final AbstractFLASHAlgorithm previous,
                                           final INodeChecker checker) {
        
        /* Iterative FLASH*/
        return new FLASHAlgorithmIterative(previous.lattice, checker, previous.strategy, previous.sorted);
    }
}
