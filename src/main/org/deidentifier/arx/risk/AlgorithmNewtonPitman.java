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

import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;

/**
 * This class implements Newton Raphson algorithm for the Pitman Model to obtain
 * results for the Maximum Likelihood estimation. For further details see
 * Hoshino, 2001
 * 
 * @author Fabian Prasser
 * @author Michael Schneider
 * @version 1.0
 */
class AlgorithmNewtonPitman extends AlgorithmNewtonRaphson {

    /**
     * The number of equivalence class sizes (keys) and corresponding frequency (values)
     */
    private final int[]  classes;

    /** The total number of entries in our sample data set */
    private final double numberOfEntries;

    /** The total number of equivalence classes in the sample data set */
    private final double numberOfEquivalenceClasses;

    /**
     * Creates an instance of the Newton-Raphson Algorithm to determine the
     * Maximum Likelihood Estimator for the Pitman Model
     * 
     * @param u total number of entries in the sample data set
     * @param n size of sample
     * @param classes
     */
    AlgorithmNewtonPitman(final double u,
                          final double n,
                          final int[] classes,
                          final int maxIterations,
                          final double accuracy,
                          final WrappedBoolean stop) {
        super(accuracy, maxIterations, stop);
        this.numberOfEquivalenceClasses = u;
        this.numberOfEntries = n;
        this.classes = classes;
    }

    /**
     * Iterated version of the firstDerivativeMatrix function
     * @param solution
     * @return
     */
    @SuppressWarnings("unused")
    private double[][] firstDerivativeMatrixIterative(final double[] solution) {

        double t = solution[0]; // Theta
        double a = solution[1]; // Alpha
        double v = 0;
        double w = 0;
        double x = 0;
        double y = 0;
        double z = 0;

        // For each...
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            double val0 = (t + (i * a));
            double val1 = 1d / (val0 * val0);
            double val2 = i * val1;
            double val3 = i * val2;
            v += val1; // Compute d^2L/(dtheta)^2
            z += val2; // Compute d^2L/(d theta d alpha)
            x += val3; // Compute d^2L/(d alpha)^2

        }
        checkInterrupt();

        // For each class...
        for (int i = 0; i < classes.length; i += 2) {
            int key = classes[i];
            int value = classes[i + 1];
            double val0 = t + key;
            w += 1d / (val0 * val0);

            if (key != 1) {
                double val1 = 0;
                for (int j = 1; j < key; j++) {
                    double val2 = j - a;
                    val1 += 1d / (val2 * val2);
                }
                y += value * val1;
            }
            checkInterrupt();
        }

        // Pack
        double[][] result = new double[2][2];
        result[0][0] = w - v;
        result[0][1] = 0 - z;
        result[1][0] = 0 - z;
        result[1][1] = 0 - x - y;
        return result;
    }

    /**
     * Iterative version of the object function
     * @param solution
     * @return
     */
    @SuppressWarnings("unused")
    private double[] objectFunctionVectorIterative(final double[] solution) {

        double t = solution[0]; // Theta
        double a = solution[1]; // Alpha

        double w = 0;
        double x = 0;
        double y = 0;
        double z = 0;

        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            double val0 = 1d / (t + (i * a));
            double val1 = i * val0;
            w += val0;
            y += val1;
        }
        checkInterrupt();

        for (int i = 1; i < numberOfEntries; i++) {
            x += 1d / (t + i);
        }
        checkInterrupt();

        // For each class..
        for (int i = 0; i < classes.length; i += 2) {
            int key = classes[i];
            int value = classes[i + 1];

            if (key != 1) {
                double val0 = 0;
                for (int j = 1; j < key; j++) {
                    val0 += 1d / (j - a);
                }
                z += value * val0;
            }
            checkInterrupt();

        }

        // Return
        double[] result = new double[2];
        result[0] = w - x;
        result[1] = y - z;
        return result;
    }

    /**
     * The method for computing the first derivatives of the object functions
     * evaluated at the iterated solutions.
     * 
     * @param solution
     *            the iterated vector of solutions.
     * @return the first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */
    @Override
    protected double[][] firstDerivativeMatrix(final double[] solution) {
        
        double t = solution[0]; // Theta
        double a = solution[1]; // Alpha

        // These closed forms have been verified with Matlab/Mupad and Mathematica!
        double n = numberOfEquivalenceClasses - 1d;
        double val0 = Gamma.trigamma((t / a) + 1d);
        double val1 = Gamma.digamma(n + (t / a) + 1d);
        double val2 = Gamma.trigamma((a + t + (a * n)) / a);
        double val3 = Gamma.trigamma((a + t) / a);
        double val4 = Gamma.digamma((a + t) / a);
        double val5 = Gamma.digamma((t / a) + 1);
        double val6 = a * a;

        double v = (val3 - val2) / (val6);
        double z = (((a * val1) + (t * val2)) - (a * val4) - (t * val3)) / (val6 * a);
        double x = (((((val6 * n) - (t * t * val2)) + (t * t * val0)) - (2 * a * t * val1)) + (2 * a * t * val5)) / (val6 * val6);
        checkInterrupt();

        // For each class...
        double w = 0;
        double y = 0;
        double val7 = Gamma.trigamma(1d - a);
        for (int i = 0; i < classes.length; i += 2) {
            int key = classes[i];
            int value = classes[i + 1];
            double val8 = t + key;
            w += 1d / (val8 * val8);
            y += key != 1 ? value * (val7 - Gamma.trigamma(key - a)) : 0;
            checkInterrupt();
        }

        // Pack
        double[][] result = new double[2][2];
        result[0][0] = w - v;
        result[0][1] = 0 - z;
        result[1][0] = 0 - z;
        result[1][1] = 0 - x - y;

        // Return
        return result;
    }

    /**
     * The method for computing the object functions evaluated at the iterated
     * solutions.
     * 
     * @param solution
     *            the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */
    @Override
    protected double[] objectFunctionVector(final double[] solution) {
        double t = solution[0]; // Theta
        double a = solution[1]; // Alpha

        // Compute z
        double z = 0;
        double val0 = Gamma.digamma(1d - a);
        for (int i = 0; i < classes.length; i += 2) {
            int key = classes[i];
            int value = classes[i + 1];
            if (key != 1) {
                z += value * (Gamma.digamma(key - a) - val0);
            }
            checkInterrupt();
        }

        // Compute w,y
        double n = numberOfEquivalenceClasses - 1d;
        double dVal0 = Gamma.digamma(n + (t / a) + 1d);
        double dVal1 = Gamma.digamma((a + t) / a);
        double w = (dVal0 - dVal1) / a;
        double y = ((-t * dVal0) + (a * n) + (t * dVal1)) / (a * a);

        // Compute x
        double x = Gamma.digamma(numberOfEntries + t) - Gamma.digamma(t + 1d);

        // Return
        double[] result = new double[2];
        result[0] = w - x;
        result[1] = y - z;
        return result;
    }
}
