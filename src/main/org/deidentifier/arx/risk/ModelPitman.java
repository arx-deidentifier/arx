/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

import de.linearbits.newtonraphson.Constraint2D;
import de.linearbits.newtonraphson.Function;
import de.linearbits.newtonraphson.NewtonRaphson2D;
import de.linearbits.newtonraphson.NewtonRaphsonConfiguration;
import de.linearbits.newtonraphson.Pair;
import de.linearbits.newtonraphson.SquareMatrix2D;
import de.linearbits.newtonraphson.Vector2D;

/**
 * This class implements the PitmanModel, for details see Hoshino, 2001
 * 
 * @author Fabian Prasser
 * @author Michael Schneider
 * @author Florian Kohlmayer
 * @version 1.0
 */
class ModelPitman extends RiskModelPopulation {

    /** The result */
    private final double numUniques;

    /**
     * Creates a new instance
     * 
     * @param model
     * @param histogram
     * @param config
     * @param stop
     */
    ModelPitman(final ARXPopulationModel model,
                final RiskModelHistogram histogram,
                final NewtonRaphsonConfiguration<?> config,
                final WrappedBoolean stop) {

        super(histogram, model, stop, new WrappedInteger());

        // Init
        double c1 = getNumClassesOfSize(1);
        double c2 = getNumClassesOfSize(2);
        double u = getNumClasses();
        double p = getPopulationSize();
        double n = super.getSampleSize();

        // Initial guess
        c2 = c2 != 0 ? c2 : 1; // Overestimate
        double c = (c1 * (c1 - 1)) / c2;
        double t = ((n * u * c) - (c1 * (n - 1) * ((2 * u) + c))) /
                   (((2 * c1 * u) + (c1 * c)) - (n * c));
        double a = ((t * (c1 - n)) + ((n - 1) * c1)) / (n * u);

        // Solve the Maximum Likelihood Estimates with Polygamma functions
        NewtonRaphson2D solver = new NewtonRaphson2D(getMasterFunctionClosed(histogram.getHistogram(), u, n),
                                                     getConstraint()).configure(config);
        Vector2D result = solver.solve(new Vector2D(t, a));

        // If no result found, use iterative implementation
        if (Double.isNaN(result.x) || Double.isNaN(result.y)) {

            solver = new NewtonRaphson2D(getMasterFunctionIterative(histogram.getHistogram(), u, n),
                                         getConstraint()).configure(config);
            result = solver.solve(new Vector2D(t, a));

            // Else check the result against the iterative implementation
        } else {

            // Run test
            Vector2D test = getObjectFunctionsIterative(histogram.getHistogram(), u, n).evaluate(result);

            // Check result of test
            if (Double.isNaN(test.x) || Double.isNaN(test.y) ||
                Math.abs(test.x) > config.getAccuracy() ||
                Math.abs(test.y) > config.getAccuracy()) {

                // Use iterative implementation
                solver = new NewtonRaphson2D(getMasterFunctionIterative(histogram.getHistogram(), u, n),
                                                                        getConstraint()).configure(config);
                result = solver.solve(new Vector2D(t, a));
            }
        }

        // Compile the result
        this.numUniques = getResult(result, p);
    }

    /**
     * Returns the number of uniques
     * 
     * @return
     */
    public double getNumUniques() {
        return this.numUniques;
    }

    /**
     * Returns a constraint on theta
     * @return
     */
    private Constraint2D getConstraint() {
        return new Constraint2D() {
            @Override
            public Boolean evaluate(Vector2D arg0) {
                return arg0.x >= 0;
            }
        };
    }

    /**
     * Returns the master function including the object function and the
     * derivative functions
     * 
     * @return
     */
    private Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>
            getMasterFunctionClosed(final int[] classes,
                                    final double u,
                                    final double n) {

        return new Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>() {

            // Init
            private final SquareMatrix2D                 derivatives = new SquareMatrix2D();
            private final Vector2D                       object      = new Vector2D();
            private final Pair<Vector2D, SquareMatrix2D> result      = new Pair<Vector2D, SquareMatrix2D>(object,
                                                                                                          derivatives);

            @Override
            public Pair<Vector2D, SquareMatrix2D> evaluate(Vector2D input) {

                // Prepare
                double t = input.x; // Theta
                double a = input.y; // Alpha

                // These closed forms have been verified with Matlab and Mathematica
                double val0 = u - 1d;
                double val1 = Gamma.digamma(val0 + (t / a) + 1d);
                double val2 = Gamma.trigamma((a + t + (a * val0)) / a);
                double val3 = Gamma.trigamma((t / a) + 1d);
                double val4 = Gamma.digamma((t / a) + 1d);
                double val5 = a * a;

                double d1 = (val3 - val2) / (val5);
                double d5 = (((a * val1) + (t * val2)) - (a * val4) - (t * val3)) / (val5 * a);
                double d3 = (((((val5 * val0) - (t * t * val2)) + (t * t * val3)) - 
                            (2d * a * t * val1)) + (2d * a * t * val4)) / (val5 * val5);
                double o1 = (val1 - val4) / a;
                double o3 = ((-t * val1) + (a * val0) + (t * val4)) / (a * a);
                double o2 = Gamma.digamma(n + t) - Gamma.digamma(t + 1d);
                
                checkInterrupt();

                double d2 = Gamma.trigamma(t + 1d) - Gamma.trigamma(n + t);

                // For each class...
                double d4 = 0;
                double o4 = 0;
                double val6 = Gamma.digamma(1d - a);
                double val7 = Gamma.trigamma(1d - a);
                for (int i = 0; i < classes.length; i += 2) {
                    int key = classes[i];
                    int value = classes[i + 1];

                    if (key != 1) {
                        d4 += value * (val7 - Gamma.trigamma(key - a));
                        o4 += value * (Gamma.digamma(key - a) - val6);
                    }
                    checkInterrupt();
                }

                // Store
                derivatives.x1 = d2 - d1;
                derivatives.x2 = 0d - d5;
                derivatives.y1 = 0d - d5;
                derivatives.y2 = 0d - d3 - d4;
                object.x = o1 - o2;
                object.y = o3 - o4;

                // Return
                return result;
            }
        };
    }

    /**
     * Returns the master function including the object function and the
     * derivative functions
     * 
     * @return
     */
    private Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>
            getMasterFunctionIterative(final int[] classes,
                                       final double u,
                                       final double n) {

        return new Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>() {

            // Init
            private final SquareMatrix2D                 derivatives = new SquareMatrix2D();
            private final Vector2D                       object      = new Vector2D();
            private final Pair<Vector2D, SquareMatrix2D> result      = new Pair<Vector2D, SquareMatrix2D>(object,
                                                                                                          derivatives);

            @Override
            public Pair<Vector2D, SquareMatrix2D> evaluate(Vector2D input) {

                // Prepare
                double t = input.x; // Theta
                double a = input.y; // Alpha

                // Init
                double d1 = 0;
                double d2 = 0;
                double d3 = 0;
                double d4 = 0;
                double d5 = 0;
                double o1 = 0;
                double o2 = 0;
                double o3 = 0;
                double o4 = 0;

                // For each...
                for (int i = 1; i < u; i++) {

                    double val0 = (t + (i * a));
                    double val1 = 1d / val0;
                    double val2 = i * val1;
                    double val3 = 1d / (val0 * val0);
                    double val4 = i * val3;
                    double val5 = i * val4;
                    d1 += val3; // Compute d^2L/(dtheta)^2
                    d5 += val4; // Compute d^2L/(d theta d alpha)
                    d3 += val5; // Compute d^2L/(d alpha)^2
                    o1 += val1;
                    o3 += val2;

                }
                checkInterrupt();

                // For each class...
                for (int i = 0; i < classes.length; i += 2) {
                    int key = classes[i];
                    int value = classes[i + 1];

                    if (key != 1) {
                        double val1 = 0;
                        double val2 = 0;
                        for (int j = 1; j < key; j++) {
                            double val3 = j - a;
                            val1 += 1d / (val3 * val3);
                            val2 += 1d / val3;
                        }
                        d4 += value * val1;
                        o4 += value * val2;
                    }
                    checkInterrupt();
                }

                checkInterrupt();

                for (int i = 1; i < n; i++) {
                    double val0 = (t + i);
                    d2 += 1d / (val0 * val0);
                    o2 += 1d / val0;
                }

                // Store
                object.x = o1 - o2;
                object.y = o3 - o4;
                derivatives.x1 = d2 - d1;
                derivatives.x2 = 0d - d5;
                derivatives.y1 = 0d - d5;
                derivatives.y2 = 0d - d3 - d4;

                // Return
                return result;
            }
        };
    }

    /**
     * Returns the object functions as an iterative implementation
     * 
     * @return
     */
    private Function<Vector2D, Vector2D>
            getObjectFunctionsIterative(final int[] classes,
                                        final double u,
                                        final double n) {

        return new Function<Vector2D, Vector2D>() {

            // Init
            private final Vector2D object = new Vector2D();

            @Override
            public Vector2D evaluate(Vector2D input) {

                // Prepare
                double t = input.x; // Theta
                double a = input.y; // Alpha

                // Init
                double o1 = 0;
                double o2 = 0;
                double o3 = 0;
                double o4 = 0;

                // For each...
                for (int i = 1; i < u; i++) {

                    double val0 = (t + (i * a));
                    double val1 = 1d / val0;
                    double val2 = i * val1;
                    o1 += val1;
                    o3 += val2;

                }
                checkInterrupt();

                // For each class...
                for (int i = 0; i < classes.length; i += 2) {
                    int key = classes[i];
                    int value = classes[i + 1];

                    if (key != 1) {
                        double val2 = 0;
                        for (int j = 1; j < key; j++) {
                            double val3 = j - a;
                            val2 += 1d / val3;
                        }
                        o4 += value * val2;
                    }
                    checkInterrupt();
                }

                checkInterrupt();

                for (int i = 1; i < n; i++) {
                    double val0 = (t + i);
                    o2 += 1d / val0;
                }

                // Store
                object.x = o1 - o2;
                object.y = o3 - o4;

                // Return
                return object;
            }
        };
    }

    /**
     * Compiles the result of running the solver
     * 
     * @return
     */
    private double getResult(Vector2D result, double p) {
        double t = result.x;
        double a = result.y;
        if (Double.isNaN(a) || Double.isNaN(t) || a == 0) { return Double.NaN; }
        double val1 = Double.NaN;
        try { val1 = Math.exp(Gamma.logGamma(t + 1d) - Gamma.logGamma(t + a)) * Math.pow(p, a); } catch (Exception e) {}
        val1 = val1 >= 0d && val1 <= p ? val1 : Double.NaN;
        double val2 = Double.NaN;
        try { val2 = (Gamma.gamma(t + 1d) / Gamma.gamma(t + a)) * Math.pow(p, a); } catch (Exception e) {}
        val2 = val2 >= 0d && val2 <= p ? val2 : Double.NaN;
        if (Double.isNaN(val1) && Double.isNaN(val2)) {
            return Double.NaN;
        } else if (!Double.isNaN(val1) && !Double.isNaN(val2)) {
            return Math.max(val1, val2);
        } else if (Double.isNaN(val1)) {
            return val2;
        } else {
            return val1;
        }
    }
}
