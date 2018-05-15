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

import de.linearbits.newtonraphson.Function;
import de.linearbits.newtonraphson.NewtonRaphson2D;
import de.linearbits.newtonraphson.NewtonRaphsonConfiguration;
import de.linearbits.newtonraphson.SquareMatrix2D;
import de.linearbits.newtonraphson.Vector2D;

/**
 * This class implements the SNB model for population uniqueness, for details see Chen, 1998
 * 
 * @author Fabian Prasser
 * @author Michael Schneider
 * 
 * @version 1.0
 */
class ModelSNB extends RiskModelPopulation {

    /** The result */
    private final double uniques;

    /**
     * Creates a new instance
     * 
     * @param model
     * @param histogram
     * @param config
     * @param stop
     */
    ModelSNB(final ARXPopulationModel model,
             final RiskModelHistogram histogram,
             final NewtonRaphsonConfiguration<?> config,
             final WrappedBoolean stop) {

        super(histogram, model, stop, new WrappedInteger());

        // Prepare
        int[] _histogram = super.getHistogram().getHistogram();
        double c1 = super.getNumClassesOfSize(1);
        double c2 = super.getNumClassesOfSize(2);
        double k = estimateNonEmptyEquivalenceClasses(_histogram, super.getNumClasses(),
                                                      c1, super.getSamplingFraction());
        double f = getSamplingFraction();

        // Solve the maximum likelihood estimates
        Vector2D result = new NewtonRaphson2D(getObjectFunction(k, f, c1, c2),
                                              getDerivatives(k, f, c1, c2))
                                             .configure(config)
                                             .solve();

        // Compile and store
        this.uniques = k * Math.pow(result.y, result.x);
    }

    /**
     * Returns the number of uniques
     * 
     * @return
     */
    public double getNumUniques() {
        return this.uniques;
    }

    /**
     * @return Shlosser estimator for variable K, giving number of non zero
     *         classes in the population estimated according to Haas, 1998 and
     *         Shlosser
     */
    private double estimateNonEmptyEquivalenceClasses(int[] histogram,
                                                      double n,
                                                      double n1,
                                                      double f) {

        double var1 = 0, var2 = 0, var3 = 0, var4 = 0;
        double var5 = f * f;
        for (int i = 0; i < histogram.length; i += 2) {
            double val0 = histogram[i];
            double val1 = histogram[i + 1];
            double val2 = Math.pow(1 - f, val0) * val1;
            var1 += val0 * var5 * Math.pow(1 - var5, val0 - 1) * val1;
            var2 += val2 * Math.pow(1 + f, val0) - 1;
            var3 += val2;
            var4 += val0 * f * Math.pow(1 - f, val0 - 1) * val1;
            checkInterrupt();
        }
        return n + n1 * (var1 / var2) * (var3 / var4) * (var3 / var4);
    }

    /**
     * Returns the derivatives
     * 
     * @param k
     * @param f
     * @param c1
     * @param c2
     * @return
     */
    private Function<Vector2D, SquareMatrix2D> getDerivatives(final double k,
                                                              final double f,
                                                              final double c1,
                                                              final double c2) {

        return new Function<Vector2D, SquareMatrix2D>() {
            public SquareMatrix2D evaluate(Vector2D input) {

                // The derivation of the following formulas has been obtained using Matlab
                final double a = input.x;
                final double b = input.y;

                final double val0 = (b - 1d) * (f - 1d);
                final double val1 = val0 - 1d;
                final double val2 = 1d - val0;
                final double val3 = a * val0 / val1 - 1d;
                final double val4 = Math.pow(-b / val1, a);
                final double val6 = Math.pow(f, 2d);
                final double val7 = Math.pow(b, a);
                final double val8 = val7 * val6 * k;
                final double val9 = a * val8;
                final double val10 = 2d * Math.pow(val2, a + 2d);
                final double val11 = Math.pow((val1), 2d);
                final double val13 = f * k;
                final double val14 = f - 1d;
                final double val15 = a - 1d;
                final double val16 = b - 1d;
                final double val17 = val15 * val0;
                final double val18 = val6 * k;
                final double val19 = (val17 + 2d);
                final double val20 = val18 * val19;

                SquareMatrix2D result = new SquareMatrix2D();

                // Formula 1d, d alpha
                result.x1 = -val13 * Math.log(-b / val1) * val3 * val4 -
                            (val13 * val4 * val0) / val1;

                // Formula 1d, d beta
                result.x2 = a * val13 * (1d / val1 - (b * val14) / val11) *
                            val3 * Math.pow((-b / val1), val15) - val13 *
                            val4 * ((a * val14) / val1 - (a * val16 * Math.pow(val14, 2d)) / val11);

                // Formula 2d, d alpha
                result.y1 = (val9 * Math.log(val2) * val19 * val16) / (val10) -
                            (val9 * Math.pow(val16, 2d) * val14) / (val10) -
                            (val7 * val20 * val16) / (val10) -
                            (a * val7 * val18 * Math.log(b) * val19 * val16) /
                            (val10);
                // Formula 2d, d beta
                result.y2 = -(val9 * val19) / (val10) - (Math.pow(a, 2d) * Math.pow(b, val15) * val20 * val16) /
                            val10 - (a * val7 * val18 * val17) / val10 -
                            (a * val7 * val20 * (a + 2d) * val0) /
                            (2d * Math.pow((val2), (a + 3d)));

                return result;
            }
        };
    }

    /**
     * Returns the object function
     * 
     * @param k
     * @param f
     * @param c1
     * @param c2
     * @return
     */
    private Function<Vector2D, Vector2D> getObjectFunction(final double k,
                                                           final double f,
                                                           final double c1,
                                                           final double c2) {

        return new Function<Vector2D, Vector2D>() {
            public Vector2D evaluate(Vector2D input) {
                // The derivatives of the following formulas have been obtained using Matlab
                final double a = input.x;
                final double b = input.y;
                final double dividend = (1 - f) * (1 - b);

                // Original equations to determine the value of the parameters alpha and beta in the SNB Model
                Vector2D result = new Vector2D();
                result.x = k * f * Math.pow(b / (1 - dividend), a) *
                           (((a * dividend) / (1 - dividend)) + 1) - c1;
                result.y = k * a * Math.pow(b, a) * (f * f) * (1 - b) / 2 *
                           Math.pow(1 - dividend, a + 2) *
                           (2 - (1 - a) * dividend) - c2;
                return result;
            }
        };
    }
}
