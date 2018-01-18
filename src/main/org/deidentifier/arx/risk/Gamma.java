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

/**
 * Helper class containing approximations for the digamma and trigamma
 * functions.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
class Gamma {

    /** The Constant B10. */
    private final static double B10            = 5.0 / 66.0;

    /** The Constant B2. */
    private final static double B2             = 1.0 / 6.0;

    /** The Constant B4. */
    private final static double B4             = -1.0 / 30.0;

    /** The Constant B6. */
    private final static double B6             = 1.0 / 42.0;

    /** The Constant B8. */
    private final static double B8             = -1.0 / 30.0;

    /** The Constant DIGAMMA_1. <br>
     * -digamma(1) = Euler Mascheroni constant
     */
    private final static double DIGAMMA_1      = -0.57721566490153286060651209008240243104215933593992d;

    /** The Constant LARGE_DIGAMMA. */
    private final static double LARGE_DIGAMMA  = 12.0;

    /** The Constant LARGE_TRIGAMMA. */
    private final static double LARGE_TRIGAMMA = 8.0;

    /** The Constant S3. */
    private final static double S3             = 1.0 / 12.0;

    /** The Constant S4. */
    private final static double S4             = 1.0 / 120.0;

    /** The Constant S5. */
    private final static double S5             = 1.0 / 252.0;

    /** The Constant S6. */
    private final static double S6             = 1.0 / 240.0;

    /** The Constant S7. */
    private final static double S7             = 1.0 / 132.0;

    /** The Constant SMALL_DIGAMMA. */
    private final static double SMALL_DIGAMMA  = 1e-6;

    /** The Constant SMALL_TRIGAMMA. */
    private final static double SMALL_TRIGAMMA = 1e-4;

    /** The Constant TETRAGAMMA_1. <br>
     * -2 * Zeta(3) = -2 * Apry constant  
     */
    private final static double TETRAGAMMA_1   = -2.0d * 1.202056903159594285399738161511449990764986292d;

    /** The Constant TRIGAMMA_1. <br>
     * trigamma(1) = pi^2/6 = Zeta(2)
     */
    private final static double TRIGAMMA_1     = (StrictMath.PI * StrictMath.PI) / 6.0;                  

    /**
     * Approximates the digamma function. Java port of the
     * "The Lightspeed Matlab toolbox" version 2.7 by Tom Minka see:
     * http://research.microsoft.com/en-us/um/people/minka/software/lightspeed/
     * 
     * @param x
     *            input value
     * @return approximation of digamma for x
     */
    static double digamma(double x) {

        /* Illegal arguments */
        if (Double.isInfinite(x) || Double.isNaN(x)) { return Double.NaN; }

        /* Singularities */
        if (x == 0.0d) { return Double.NEGATIVE_INFINITY; }

        /* Negative values */
        /*
         * Use the reflection formula (Jeffrey 11.1.6): digamma(-x) =
         * digamma(x+1) + pi*cot(pi*x)
         * 
         * This is related to the identity digamma(-x) = digamma(x+1) -
         * digamma(z) + digamma(1-z) where z is the fractional part of x For
         * example: digamma(-3.1) = 1/3.1 + 1/2.1 + 1/1.1 + 1/0.1 +
         * digamma(1-0.1) = digamma(4.1) - digamma(0.1) + digamma(1-0.1) Then we
         * use digamma(1-z) - digamma(z) = pi*cot(pi*z)
         */
        if (x < 0.0d) { return digamma(1.0d - x) +
                               (StrictMath.PI / StrictMath.tan(-StrictMath.PI *
                                                               x)); }

        /* Use Taylor series if argument <= small */
        if (x <= SMALL_DIGAMMA) { return (DIGAMMA_1 - (1.0d / x)) +
                                         (TRIGAMMA_1 * x); }

        double result = 0.0d;
        /* Reduce to digamma(X + N) where (X + N) >= large */
        while (x < LARGE_DIGAMMA) {
            result -= 1.0d / x;
            x++;
        }

        /* Use de Moivre's expansion if argument >= C */
        /* This expansion can be computed in Maple via asympt(Psi(x),x) */
        if (x >= LARGE_DIGAMMA) {
            double r = 1.0d / x;
            result += StrictMath.log(x) - (0.5d * r);
            r *= r;
            result -= r *
                      (S3 - (r * (S4 - (r * (S5 - (r * (S6 - (r * S7))))))));
        }

        return result;
    }

    /**
     * TODO: Implement efficiently
     * 
     * @param x
     * @return
     */
    static double gamma(double x) {
        return org.apache.commons.math3.special.Gamma.gamma(x);
    }

    /**
     * TODO: Implement efficiently
     * 
     * @param x
     * @return
     */
    static double logGamma(double x) {
        return org.apache.commons.math3.special.Gamma.logGamma(x);
    }

    /**
     * Approximates the trigamma function. Java port of the
     * "The Lightspeed Matlab toolbox" version 2.7 by Tom Minka see:
     * http://research.microsoft.com/en-us/um/people/minka/software/lightspeed/
     * 
     * @param x
     *            input value
     * @return approximation of trigamma for x
     */
    static double trigamma(double x) {
        /* Illegal arguments */
        if (Double.isInfinite(x) || Double.isNaN(x)) { return Double.NaN; }

        /* Singularities */
        if (x == 0.0d) { return Double.NEGATIVE_INFINITY; }

        /* Negative values */
        /*
         * Use the derivative of the digamma reflection formula: -trigamma(-x) =
         * trigamma(x+1) - (pi*csc(pi*x))^2
         */
        if (x < 0.0d) {
            double r = StrictMath.PI / StrictMath.sin(-StrictMath.PI * x);
            return -trigamma(1.0d - x) + (r * r);
        }

        /* Use Taylor series if argument <= small */
        if (x <= SMALL_TRIGAMMA) { return (1.0d / (x * x)) + TRIGAMMA_1 +
                                          (TETRAGAMMA_1 * x); }

        double result = 0.0d;
        /* Reduce to trigamma(x+n) where ( X + N ) >= B */
        while (x < LARGE_TRIGAMMA) {
            result += 1.0d / (x * x);
            x++;
        }

        /* Apply asymptotic formula when X >= B */
        /* This expansion can be computed in Maple via asympt(Psi(1,x),x) */
        if (x >= LARGE_DIGAMMA) {
            double r = 1.0d / (x * x);
            result += (0.5d * r) +
                      ((1.0d + (r * (B2 + (r * (B4 + (r * (B6 + (r * (B8 + (r * B10)))))))))) / x);
        }
        return result;
    }

}
