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

 * Title: javastat
 * </p>
 * <p>
 * Description: JAVA programs for statistical computations
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Tung Hai University
 * </p>
 * 
 * @author Wen Hsiang Wei
 * @version 1.4
 *          The Java code in all Javastat packages is currently licensed under the terms of the
 *          GNU General Public License.
 */
import Jama.Matrix;

/**
 * The class defines the required methods for the sub-classes using
 * Newton-Raphson algorithm
 */

public abstract class NewtonRaphsonAlgorithm {

    /**
     * Convergence threshold for the Newton-Raphson algorithm.
     */

    public double   accuracy     = 1.0e-9;

    /**
     * Maximum number of iterations.
     */

    public int      maxIteration = 300;

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

    public NewtonRaphsonAlgorithm() {
    }

    /**
     * The abstract method (need to be implemented in sub-classes) for computing
     * the first derivatives of the object functions evaluated at the iterated
     * solutions.
     * @param iteratedSolution the iterated vector of solutions.
     * @return the first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */

    public abstract double[][] firstDerivativeMatrix(double[] iteratedSolution);

    /**
     * Returns the vector of solutions obtained by the Newton-Raphson algorithm.
     * @param initialValue the vector of initial values.
     * @return the vector of solutions.
     * @exception IllegalArgumentException the first derivative matrix of
     *                                     the object functions is singular.
     */

    public double[] getSolution(final double[] initialValue) {

        solutionVector = new Matrix(initialValue, initialValue.length);
        updatedSolutionVector = new Matrix(initialValue, initialValue.length);
        differenceVector = new Matrix(initialValue.length, 1, 100);
        firstDerivativeMatrix = new Matrix(initialValue.length, initialValue.length);
        int i = 0;
        while ((differenceVector.normF() > accuracy) && (i++ < maxIteration)) {

            firstDerivativeMatrix = new Matrix(firstDerivativeMatrix(solutionVector.getColumnPackedCopy()));
            if (firstDerivativeMatrix.det() != 0.0) {
                differenceVector = firstDerivativeMatrix.inverse().times(new Matrix(objectFunctionVector(solutionVector.getColumnPackedCopy()), initialValue.length));
                updatedSolutionVector = solutionVector.minus(differenceVector);
            } else {
                System.out.println("Newton Raphson Algorithm:");
                System.out.println("The first derivative matrix of the object functions is " + "singular. Using different method to estimate Population Uniqueness!");
                final double[] result = solutionVector.getColumnPackedCopy();
                for (int j = 0; j < result.length; j++) {
                    result[j] = Double.NaN;
                }
                return result;
                /*
                 * throw new IllegalArgumentException(
                 * "The first derivative matrix of the object functions is "
                 * + "singular.");
                 */
            }
            solutionVector = updatedSolutionVector;
        }

        if (i == maxIteration) {
            System.out.println("Maximum number of iterations have been acheived.");
        }

        return solutionVector.getColumnPackedCopy();
    }

    /**
     * The abstract method (need to be implemented in sub-classes) for computing
     * the object functions evaluated at the iterated solutions.
     * @param iteratedSolution the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */

    public abstract double[] objectFunctionVector(double[] iteratedSolution);

}
