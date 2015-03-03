/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.risk;

import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;

import Jama.Matrix;

/**
 * The class defines the required methods for the sub-classes using Newton-Raphson algorithm
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Michael Schneider
 * @version 1.0
 */

abstract class AlgorithmNewtonRaphson {

    /** Convergence threshold for the Newton-Raphson algorithm. */
    public double          accuracy      = 1.0e-9;

    /** Maximum number of iterations. */
    public int             maxIterations = 300;

    /** The solutions. */
    public double[]        solution;

    /** The vector of the differences between the iterated vectors of solutions. */
    private Matrix         differenceVector;

    /** The vector of first derivatives of the object functions. */
    private Matrix         firstDerivativeMatrix;

    /** The vector of solutions. */
    private Matrix         solutionVector;

    /** Stop flag */
    private WrappedBoolean stop;
    
    /**
     * Creates a new instance
     * @param accuracy
     * @param maxIterations
     * @param stop
     */
    AlgorithmNewtonRaphson(double accuracy, int maxIterations, WrappedBoolean stop) {
        this.accuracy = accuracy;
        this.maxIterations = maxIterations;
        this.stop = stop;
    }

    /**
     * The abstract method (need to be implemented in sub-classes) for computing
     * the first derivatives of the object functions evaluated at the iterated
     * solutions.
     * 
     * @param iteratedSolution The iterated vector of solutions.
     * @return the first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */

    protected abstract double[][] firstDerivativeMatrix(double[] iteratedSolution);

    /**
     * Returns the vector of solutions obtained by the Newton-Raphson algorithm.
     * 
     * @param initialValue The vector of initial values.
     * @return the vector of solutions.
     * @exception IllegalArgumentException
     *                the first derivative matrix of the object functions is
     *                singular.
     */

    protected double[] getSolution(final double[] initialValue) {

        solutionVector = new Matrix(initialValue, initialValue.length);
        differenceVector = new Matrix(initialValue.length, 1, 100);
        firstDerivativeMatrix = new Matrix(initialValue.length, initialValue.length);
        
        // Solve
        int iterations = 0;
        while (true) {
            
            // Check
            checkInterrupt();
            
            // Iterate
            firstDerivativeMatrix = new Matrix(firstDerivativeMatrix(solutionVector.getColumnPackedCopy()));
            double det = firstDerivativeMatrix.det();
            if (!Double.isNaN(det) && det != 0d) {
                differenceVector = firstDerivativeMatrix.inverse().times(new Matrix(objectFunctionVector(solutionVector.getColumnPackedCopy()), 
                                                                                                         initialValue.length));
                solutionVector = solutionVector.minus(differenceVector);
            
                // Error
            } else {
                return new double[] { Double.NaN, Double.NaN };
            }

            // Break, if conditions met
            double normF = differenceVector.normF();
            if (Double.isNaN(normF) || normF <= accuracy || iterations++ > maxIterations) {
                break;
            }
        }
        return solutionVector.getColumnPackedCopy();
    }

    /**
     * The abstract method (need to be implemented in sub-classes) for computing
     * the object functions evaluated at the iterated solutions.
     * 
     * @param iteratedSolution The iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */

    protected abstract double[] objectFunctionVector(double[] iteratedSolution);

    /** Checks for interrupts*/
    void checkInterrupt() {
        if (stop.value) {
            throw new ComputationInterruptedException();
        }
    }
}
