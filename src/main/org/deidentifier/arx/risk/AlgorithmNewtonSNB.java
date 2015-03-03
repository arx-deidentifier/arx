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
 * This class implements the Newton Raphson Algorithm for the SNB Model. The results of this class (regarding the derivates as well
 * as the solutions for the system of non-linear equations) have been validated by comparison with results from Matlab and Mathematica.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Michael Schneider
 * @version 1.0
 */
class AlgorithmNewtonSNB extends AlgorithmNewtonRaphson {

    /** Number of equivalence classes of size one in the sample */
    private final int    c1;

    /** number of equivalence classes of size two in the sample */
    private final int    c2;

    /** Number of non-empty classes equivalence classes in the population (estimated) */
    private final double k;

    /** Sampling fraction */
    private final double f;

    /**
     * Implements procedures of the Newton Raphson algorithm for the SNB Model
     * 
     * @param k Number of non zero classes equivalence classes in the population (estimated)
     * @param pi Sampling fraction
     * @param numClassesOfSize1
     * @param numClassesOfSize2
     */
    protected AlgorithmNewtonSNB(final double k,
                                 final double pi,
                                 final int numClassesOfSize1,
                                 final int numClassesOfSize2,
                                 final int maxIterations,
                                 final double accuracy,
                                 final WrappedBoolean stop) {
        super(accuracy, maxIterations, stop);
        this.k = k;
        this.f = pi;
        this.c1 = numClassesOfSize1;
        this.c2 = numClassesOfSize2;
    }

    /**
     * The method for computing the first derivatives of the object functions
     * evaluated at the iterated solutions.
     * 
     * @param iteratedSolution The iterated vector of solutions.
     * @return The first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */
    @Override
    protected double[][] firstDerivativeMatrix(final double[] iteratedSolution) {

        // The derivation of the following formulas has been obtained using Matlab
        final double a = iteratedSolution[0];
        final double b = iteratedSolution[1];

        final double[][] result = new double[iteratedSolution.length][iteratedSolution.length];
        final double     val0 = (b - 1d) * (f - 1d);
        final double     val1 = val0 - 1d;
        final double     val2 = 1d - val0;
        final double     val3 = a * val0 / val1 - 1d;
        final double     val4 = Math.pow(-b / val1, a);
        final double     val5 = val0 + 2d;
        final double     val6 = Math.pow(f, 2d);
        final double     val7 = Math.pow(b, a);
        final double     val8 = val7 * val6 * k;
        final double     val9 = a * val8;
        final double     val10 = 2d * Math.pow(val2, a + 2d);
        final double     val11 = Math.pow((val1), 2d);
        final double     val12 = (a - 1d) * val5;
        final double     val13 = f * k;
        final double     val14 = f - 1d;
        final double     val15 = a - 1d;
        final double     val16 = b - 1d;
        final double     val17 = val12 * val16;
        
        // Formula 1d, d alpha
        result[0][0] = -val13 * Math.log(-b / val1) * val3 * val4 - (val13 * val4 * val0) / val1;
        
        // Formula 1d, d beta
        result[0][1] = a * val13 * (1d / val1 - (b * val14) / val11) * val3 * Math.pow((-b / val1), val15) - val13 * val4 * 
                       ((a * val14) / val1 - (a * val16 * Math.pow(val14, 2d)) / val11);
        // Formula 2d, d alpha
        result[1][0] = (val9 * Math.log(val2) * val17) / val10 - (val9 * Math.pow(val16, 2d) * 
                       val14) / val10 - (val8 * val17) / val10 - (val9 * Math.log(b) * val17) / val10;
        // Formula 2d, d beta
        result[1][1] = -(val9 * val12) / val10 - (Math.pow(a, 2d) * Math.pow(b, val15) * val6 * k * val17) / val10 - (val9 * val15 * val0) / 
                       val10 - (val9 * val12 * (a + 2d) * val0) / (2d * Math.pow(val2, (a + 3d)));

        return result;
    }

    /**
     * The method for computing the object functions evaluated at the iterated
     * solutions.
     * 
     * @param iteratedSolution the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */
    @Override
    protected double[] objectFunctionVector(final double[] iteratedSolution) {
        
        final double[] result = new double[iteratedSolution.length];
        
        final double a = iteratedSolution[0];
        final double b = iteratedSolution[1];
        
        final double dividend = (1 - f) * (1 - b);

        // Original equations to determine the value of the parameters alpha and beta in the SNB Model
        result[0] = k * f * Math.pow(b / (1 - dividend), a) * (((a * dividend) / (1 - dividend)) + 1) - c1;
        result[1] = k * a * Math.pow(b, a) * (f * f) * (1 - b) / 2 * Math.pow(1 - dividend, a + 2) * (2 - (1 - a) * dividend) - c2;
        return result;
    }
}
