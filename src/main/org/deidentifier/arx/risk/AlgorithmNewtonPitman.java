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

        double t = iteratedSolution[0]; // Theta
        double a = iteratedSolution[1]; // Alpha
        double v = 0;
        double w = 0;
        double x = 0;
        double y = 0;
        double z = 0;
        
        // For each...
        // TODO: Find closed form
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
                // TODO: Find closed form
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
     * The method for computing the object functions evaluated at the iterated
     * solutions.
     * 
     * @param iteratedSolution
     *            the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */
    @Override
    protected double[] objectFunctionVector(final double[] iteratedSolution) {
        
        double t = iteratedSolution[0]; // Theta
        double a = iteratedSolution[1]; // Alpha

        double w = 0;
        double x = 0;
        double y = 0;
        double z = 0;
        
        // TODO: Find closed form
        for (int i = 1; i < numberOfEquivalenceClasses; i++) {
            double val0 = 1d / (t + (i * a));
            double val1 = i * val0;
            w += val0;
            y += val1;
        }
        checkInterrupt();
        
        // TODO: Find closed form
        for (int i = 1; i < numberOfEntries; i++) {
            x += 1d / (t + i);
        }
        checkInterrupt();

        // For each class..
        for (int i = 0; i < classes.length; i += 2) {
            int key = classes[i];
            int value = classes[i + 1];
            
            if (key != 1) {
                // TODO: Find closed form
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
}
