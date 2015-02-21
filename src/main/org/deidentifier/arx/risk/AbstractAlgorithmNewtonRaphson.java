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

import Jama.Matrix;

/**
 * The class defines the required methods for the sub-classes using
 * Newton-Raphson algorithm
 */

abstract class AbstractAlgorithmNewtonRaphson {

    /**
     * Convergence threshold for the Newton-Raphson algorithm.
     */

    public double   accuracy      = 1.0e-9;

    /**
     * Maximum number of iterations.
     */

    public int      maxIterations = 300;

    /**
     * The solutions.
     */

    public double[] solution;

    /**
     * The vector of the differences between the iterated vectors of solutions.
     */

    private Matrix  differenceVector;

    /**
     * The vector of first derivatives of the object functions.
     */

    private Matrix  firstDerivativeMatrix;

    /**
     * The vector of solutions.
     */

    private Matrix  solutionVector;

    /**
     * The iterated vector of solutions.
     */

    private Matrix  updatedSolutionVector;

    /**
     * Default NewtonRaphsonAlgorithm constructor.
     */

    AbstractAlgorithmNewtonRaphson() {
    }

    /**
     * The abstract method (need to be implemented in sub-classes) for computing
     * the first derivatives of the object functions evaluated at the iterated
     * solutions.
     * 
     * @param iteratedSolution
     *            the iterated vector of solutions.
     * @return the first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */

    protected abstract double[][] firstDerivativeMatrix(double[] iteratedSolution);

    /**
     * Returns the vector of solutions obtained by the Newton-Raphson algorithm.
     * 
     * @param initialValue
     *            the vector of initial values.
     * @return the vector of solutions.
     * @exception IllegalArgumentException
     *                the first derivative matrix of the object functions is
     *                singular.
     */

    protected double[] getSolution(final double[] initialValue) {

        solutionVector = new Matrix(initialValue, initialValue.length);
        updatedSolutionVector = new Matrix(initialValue, initialValue.length);
        differenceVector = new Matrix(initialValue.length, 1, 100);
        firstDerivativeMatrix = new Matrix(initialValue.length,
                                           initialValue.length);
        int i = 0;
        while ((differenceVector.normF() > accuracy) && (i++ < maxIterations)) {

            firstDerivativeMatrix = new Matrix(firstDerivativeMatrix(solutionVector.getColumnPackedCopy()));
            if (firstDerivativeMatrix.det() != 0.0) {
                differenceVector = firstDerivativeMatrix.inverse()
                                                        .times(new Matrix(objectFunctionVector(solutionVector.getColumnPackedCopy()),
                                                                          initialValue.length));
                updatedSolutionVector = solutionVector.minus(differenceVector);
            } else {
                final double[] result = solutionVector.getColumnPackedCopy();
                for (int j = 0; j < result.length; j++) {
                    result[j] = Double.NaN;
                }
                return result;
            }
            solutionVector = updatedSolutionVector;
        }

        if (i == maxIterations) {
            System.out.println("Maximum number of iterations have been performed.");
        }

        return solutionVector.getColumnPackedCopy();
    }

    /**
     * The abstract method (need to be implemented in sub-classes) for computing
     * the object functions evaluated at the iterated solutions.
     * 
     * @param iteratedSolution
     *            the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */

    protected abstract double[] objectFunctionVector(double[] iteratedSolution);

}
