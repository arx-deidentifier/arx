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

import java.util.Map;

/**
 * This class implements Newton Raphson algorithm for the Pitman Model to obtain
 * results for the Maximum Likelihood estimation. For further details see
 * Hoshino, 2001
 * 
 * @author Michael Schneider
 * @version 1.0
 */
class AlgorithmNewtonPitman extends AbstractAlgorithmNewtonRaphson {

    /**
     * The number of equivalence class sizes (keys) and corresponding frequency
     * (values)
     */
    private final Map<Integer, Integer> eqClasses;

    /** The total number of entries in our sample data set */
    private final double                numberOfEntries;

    /** The total number of equivalence classes in the sample data set */
    private final double                numberOfEquivalenceClasses;

    /**
     * Creates an instance of the Newton-Raphson Algorithm to determine the
     * Maximum Likelihood Estimator for the Pitman Model
     * 
     * @param u
     *            total number of entries in the sample data set
     * @param n
     *            size of sample
     * @param eqClasses
     *            The number of equivalence class sizes (keys) and corresponding
     *            frequency (values)
     */
    AlgorithmNewtonPitman(final double u,
                        final double n,
                        final Map<Integer, Integer> eqClasses) {
        this.numberOfEquivalenceClasses = u;
        this.numberOfEntries = n;
        this.eqClasses = eqClasses;
    }

    /**
     * The method for computing the first derivatives of the object functions
     * evaluated at the iterated solutions.
     * 
     * @param iteratedSolution
     *            the iterated vector of solutions.
     * @return the first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */
    @Override
    protected double[][] firstDerivativeMatrix(final double[] iteratedSolution) {

        final double[][] result = new double[iteratedSolution.length][iteratedSolution.length];
        double temp1 = 0, temp2 = 0, temp3 = 0;

        // compute d^2L/(dtheta)^2
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            temp1 += (1 / ((iteratedSolution[0] + (i * iteratedSolution[1])) * (iteratedSolution[0] + (i * iteratedSolution[1]))));
        }

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            temp2 += (1 / ((iteratedSolution[0] + entry.getKey()) * (iteratedSolution[0] + entry.getKey())));
        }
        result[0][0] = temp2 - temp1;

        // compute d^2L/(d alpha)^2
        temp1 = 0;
        temp2 = 0;
        temp3 = 0;
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            temp1 += ((i * i) / ((iteratedSolution[0] + (i * iteratedSolution[1])) * (iteratedSolution[0] + (i * iteratedSolution[1]))));
        }

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            temp3 = 0;
            if (entry.getKey() != 1) {
                for (int j = 1; j < entry.getKey(); j++) {
                    temp3 += (1 / ((j - iteratedSolution[1]) * (j - iteratedSolution[1])));
                }
                temp2 += entry.getValue() * temp3;
            }
        }
        result[1][1] = 0 - temp1 - temp2;

        // compute d^2L/(d theta d alpha)
        temp1 = 0;
        temp2 = 0;
        temp3 = 0;
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            temp1 += (i / (((i * iteratedSolution[1]) + iteratedSolution[0]) * ((i * iteratedSolution[1]) + iteratedSolution[0])));
        }
        result[0][1] = 0 - temp1;
        result[1][0] = 0 - temp1;

        return result;
    }

    /**
     * The method for computing the object functions evaluated at the iterated
     * solutions.
     * 
     * @param iteratedSolution
     *            the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */
    @Override
    protected double[] objectFunctionVector(final double[] iteratedSolution) {
        // theta is at iteratedSolution[0], alpha at [1]
        final double[] result = new double[iteratedSolution.length];
        double temp1 = 0, temp2 = 0, temp3 = 0;

        // compute theta
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            temp1 += (1 / (iteratedSolution[0] + (i * iteratedSolution[1])));
        }
        for (int i = 1; i < numberOfEntries; i++) {
            temp2 += (1 / (iteratedSolution[0] + i));
        }
        result[0] = temp1 - temp2;

        // compute alpha
        temp1 = 0;
        temp2 = 0;
        temp3 = 0;
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            temp1 += (i / (iteratedSolution[0] + (i * iteratedSolution[1])));
        }
        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            temp3 = 0;
            if (entry.getKey() != 1) {
                for (int j = 1; j < entry.getKey(); j++) {
                    temp3 += (1 / (j - iteratedSolution[1]));
                }
                temp2 += entry.getValue() * temp3;
            }
        }
        result[1] = temp1 - temp2;

        return result;
    }

}
