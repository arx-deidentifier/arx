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

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * This class implements the Newton Raphson Algorithm for the SNB Model
 * 
 * @author Michael Schneider
 * @version 1.0
 */
class AlgorithmNewtonSNB extends AbstractAlgorithmNewtonRaphson {

    /**
     * number of equivalence classes of size one in the sample
     */
    private final int c1;

    /**
     * number of equivalence classes of size two in the sample
     */
    private final int c2;

    /**
     * number of non-empty classes equivalence classes in the population
     * (estimated)
     */
    protected double  estimatedNumberOfNonEmptyClasses;

    /**
     * sampling fraction
     */
    protected double  samplingFraction;

    /**
     * Implements procedures of Newton Raphson algorithm for SNB Model
     * 
     * @param pi
     *            sampling fraction,
     * @param eqClasses
     *            Map containing the equivalence class sizes (as keys) of the
     *            data set and the corresponding frequency (as values) e.g. if
     *            the key 2 has value 3 then there are 3 equivalence classes of
     *            size two.
     * @param k
     *            number of non zero classes equivalence classes in the
     *            population (estimated)
     */
    protected AlgorithmNewtonSNB(final double k,
                     final double pi,
                     final IntIntOpenHashMap eqClasses) {
        this.estimatedNumberOfNonEmptyClasses = k;
        this.samplingFraction = pi;
        c1 = eqClasses.get(1);
        c2 = eqClasses.get(2);
    }

    /**
     * Helper function to compute the value of the first argument raised to the
     * power of the second argument.
     * 
     * @param base
     *            (first argument)
     * @param power
     *            (second argument)
     * @return value of the first argument raised to the power of the second
     *         argument
     */
    private double power(final double base, final double power) {
        double result;
        if (base < 0) {
            if (power < 0) {
                result = -1.0 / Math.pow(Math.abs(base), Math.abs(power));
            } else {
                result = -Math.pow(Math.abs(base), power);
            }
        } else {
            if (power < 0) {
                result = 1.0 / Math.pow(Math.abs(base), Math.abs(power));
            } else {
                result = Math.pow(Math.abs(base), power);
            }
        }

        return result;
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
        final double iBeta = iteratedSolution[1] - 1;
        // The derivation of the following formulas has been obtained using
        // Mathematica

        // Formula 1, d alpha
        result[0][0] = (-(samplingFraction *
                          estimatedNumberOfNonEmptyClasses *
                          power((iteratedSolution[1] / ((iteratedSolution[1] * (-samplingFraction)) +
                                                             iteratedSolution[1] + samplingFraction)),
                                     iteratedSolution[0]) * ((((iteratedSolution[0] *
                                                                iBeta * (samplingFraction - 1)) +
                                                               (iteratedSolution[1] * (-samplingFraction)) +
                                                               iteratedSolution[1] + samplingFraction) * Math.log(((iteratedSolution[1] * (-samplingFraction)) +
                                                                                                                   iteratedSolution[1] + samplingFraction))) + (iBeta * (samplingFraction - 1)))) / ((iteratedSolution[1] * (samplingFraction - 1)) - samplingFraction));

        // Formula 1, d beta
        result[0][1] = ((iteratedSolution[0] *
                         samplingFraction *
                         Math.pow((iteratedSolution[1] / ((iteratedSolution[1] + samplingFraction) - (iteratedSolution[1] * samplingFraction))),
                                  iteratedSolution[0] - 1) *
                         ((iteratedSolution[1] * (samplingFraction - 1) * (1 + ((iteratedSolution[0] - 1) * samplingFraction))) + (samplingFraction * ((iteratedSolution[0] + samplingFraction) - (iteratedSolution[0] * samplingFraction)))) * estimatedNumberOfNonEmptyClasses) / (power(((iteratedSolution[1] + samplingFraction) - (iteratedSolution[1] * samplingFraction)),
                                                                                                                                                                                                                                                                                                3)));

        // Formula 2, d alpha
        result[1][0] = (power(2, -iteratedSolution[0] - 2) *
                        (1 - iteratedSolution[1]) *
                        power(iteratedSolution[1], iteratedSolution[0]) *
                        (samplingFraction * samplingFraction) *
                        power(((iteratedSolution[1] + samplingFraction) - (iteratedSolution[1] * samplingFraction)),
                                   -iteratedSolution[0] - 2) *
                        estimatedNumberOfNonEmptyClasses * (((1 +
                                                              iteratedSolution[1] +
                                                              (2 *
                                                               iteratedSolution[0] *
                                                               (iteratedSolution[1] - 1) * (samplingFraction - 1)) + samplingFraction) - (samplingFraction * iteratedSolution[1])) + ((iteratedSolution[0] * ((1 +
                                                                                                                                                                                                               iteratedSolution[1] +
                                                                                                                                                                                                               (iteratedSolution[0] *
                                                                                                                                                                                                                (iteratedSolution[1] - 1) * (samplingFraction - 1)) + samplingFraction) - (iteratedSolution[1] * samplingFraction))) * (Math.log(iteratedSolution[1]) - Math.log(2 * ((iteratedSolution[1] + samplingFraction) - (iteratedSolution[1] * samplingFraction)))))));

        // Formula 2, d beta
        result[1][1] = (power(-2, -iteratedSolution[0] - 2) *
                        iteratedSolution[0] *
                        power(iteratedSolution[1], iteratedSolution[0] - 1) *
                        (samplingFraction * samplingFraction) *
                        power((iteratedSolution[1] + samplingFraction) -
                                           (iteratedSolution[1] * samplingFraction),
                                   -iteratedSolution[0] - 3) *
                        (((2 * iteratedSolution[1] * ((1 + iteratedSolution[0]) - (iteratedSolution[0] * iteratedSolution[1]))) - ((iteratedSolution[0] * ((-1 + (iteratedSolution[0] * (iteratedSolution[1] - 1))) - (3 * iteratedSolution[1]))) * ((iteratedSolution[1] - 1) * samplingFraction))) + ((iteratedSolution[0] - 1) *
                                                                                                                                                                                                                                                                                                        iteratedSolution[0] *
                                                                                                                                                                                                                                                                                                        power((iteratedSolution[1] - 1),
                                                                                                                                                                                                                                                                                                                   2) * (samplingFraction * samplingFraction))) * estimatedNumberOfNonEmptyClasses);
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
        final double[] result = new double[iteratedSolution.length];
        final double dividend = ((1 - samplingFraction) * (1 - iteratedSolution[1]));

        // original equations to determine the value of the parameters alpha and
        // beta in the SNB Model:
        result[0] = (estimatedNumberOfNonEmptyClasses *
                     samplingFraction *
                     power((iteratedSolution[1] / (1 - dividend)),
                                iteratedSolution[0]) * (((iteratedSolution[0] * dividend) / (1 - dividend)) + 1)) -
                    c1;

        result[1] = (estimatedNumberOfNonEmptyClasses *
                     ((iteratedSolution[0] *
                       power(iteratedSolution[1], iteratedSolution[0]) *
                       (samplingFraction * samplingFraction) * (1 - iteratedSolution[1])) / (2 * power((1 - dividend),
                                                                                                            iteratedSolution[0] + 2))) * (2 - ((1 - iteratedSolution[0]) * dividend))) -
                    c2;

        return result;
    }

}
