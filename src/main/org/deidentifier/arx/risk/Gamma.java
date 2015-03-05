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

/**
 * Helper class containing approximations for the digamma and trigamma functions.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public class Gamma {

    /** The Constant LARGE. */
    private final static double LARGE = 9.5;

    /** The Constant D1. */
    private final static double D1    = -0.5772156649015328606065121; // digamma(1)

    /** The Constant D2. */
    private final static double D2    = Math.pow(Math.PI, 2.0) / 6.0;

    /** The Constant SMALL. */
    private final static double SMALL = 1e-6;

    /** The Constant S3. */
    private final static double S3    = 1.0 / 12.0;

    /** The Constant S4. */
    private final static double S4    = 1.0 / 120.0;

    /** The Constant S5. */
    private final static double S5    = 1.0 / 252.0;

    /** The Constant S6. */
    private final static double S6    = 1.0 / 240.0;

    /** The Constant S7. */
    private final static double S7    = 1.0 / 132.0;

    /** The Constant S8. */
    private final static double S8    = 1.0 / 30.0;

    /** The Constant S9. */
    private final static double S9    = 1.0 / 6.0;

    /** The Constant S10. */
    private final static double S10   = 1.0 / 42.0;

    /** The Constant S11. */
    private final static double S11   = 1.0 / 13.2;

    /** The Constant S12. */
    private final static double S12   = 1.0 / 2.0;
    /**
     * Approximates the digamma function.
     * Adapted from: https://github.com/georgg/georg/blob/master/src/old%20code/SpecialMathFn/Digamma.java
     * which in turn is adapted from Tony Minka's Lightspeed 2.0 Matlab library
     * 
     * DIGAMMA(X) returns digamma(x) = d log(gamma(x)) / dx
     * If X is a matrix, returns the digamma function evaluated at each element.
     * 
     * Reference:
     * 
     * J Bernardo,
     * Psi ( Digamma ) Function,
     * Algorithm AS 103,
     * Applied Statistics,
     * Volume 25, Number 3, pages 315-317, 1976.
     * From http://www.psc.edu/~burkardt/src/dirichlet/dirichlet.f
     *
     * @param x input value
     * @return approximation of digamma for x
     */
    public static double digamma(double x) {
        
        double y = 0.0d;
        double r = 0.0d;

        if (Double.isInfinite(x) || Double.isNaN(x)) {
            return 0.0d / 0.0d;
        }

        if (x == 0.0d) {
            return -1.0d / 0.0d;
        }

        if (x < 0.0d) {
            // Use the reflection formula (Jeffrey 11.1.6):
            // digamma(-x) = digamma(x+1) + pi*cot(pi*x)
            y = digamma(-x + 1.0d) + (Math.PI * (1.0d / Math.tan(-Math.PI * x)));
            return y;
            // This is related to the identity
            // digamma(-x) = digamma(x+1) - digamma(z) + digamma(1-z)
            // where z is the fractional part of x
            // For example:
            // digamma(-3.1) = 1/3.1 + 1/2.1 + 1/1.1 + 1/0.1 + digamma(1-0.1)
            // = digamma(4.1) - digamma(0.1) + digamma(1-0.1)
            // Then we use
            // digamma(1-z) - digamma(z) = pi*cot(pi*z)
        }

        // Use approximation if argument <= small.
        if (x <= SMALL) {
            y = ((y + D1) - (1.0d / x)) + (D2 * x);
            return y;
        }

        // Reduce to digamma(X + N) where (X + N) >= large.
        while (true) {
            if ((x > SMALL) && (x < LARGE)) {
                y = y - (1.0d / x);
                x = x + 1.0d;
            } else {
                break;
            }
        }

        // Use de Moivre's expansion if argument >= large.
        // In maple: asympt(Psi(x), x);
        if (x >= LARGE) {
            r = 1.0d / x;
            y = (y + Math.log(x)) - (0.5d * r);
            r = r * r;
            y = y - (r * (S3 - (r * (S4 - (r * (S5 - (r * (S6 - (r * S7)))))))));
        }

        return y;
    }

    /**
     * Approximates the trigamma function.
     * Adapted from: https://github.com/lintool/Cloud9/blob/master/src/main/java/edu/umd/cloud9/math/Gamma.java
     * 
     * Author Mihai Preda, 2006.
     * The author disclaims copyright to this source code.
     *
     * @param x input value
     * @return approximation of trigamma for x
     */
    public static double trigamma(double x) {
        double p = 0d;

        x = x + 6.0d;
        p = 1.0d / (x * x);
        p = (((((((((((S11 * p) - S8) * p) + S10) * p) - S8) * p) + S9) * p) + 1d) / x) + (S12 * p);
        for (int i = 0; i < 6; i++) {
            x = x - 1.0d;
            p = (1.0d / (x * x)) + p;
        }

        return p;
    }
}
