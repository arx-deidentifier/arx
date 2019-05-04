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
package org.deidentifier.arx.dp;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.util.Pair;

/**
 * An implementation of the exponential mechanism for discrete domains as proposed in:
 * 
 * McSherry, Frank, and Kunal Talwar:
 * Mechanism design via differential privacy.
 * Foundations of Computer Science 2007. pp. 94-103
 * 
 * This implementation assumes that all score values have been divided by the sensitivity of the respective score function.
 * 
 * We point out that this implementation draws from a probability distribution which approximates the mathematically precise
 * distribution of the exponential mechanism as a consequence of floating-point arithmetic, which could potentially affect
 * the privacy guarantees provided. However, it can be shown that the resulting absolute exceedance of the privacy parameter
 * epsilon is at most in the order of E = log( (1 + n * 2^{-51})^2 ) where n is the size of the domain.
 * For various values of n, these absolute errors are as follows:
 * 
 *  ------------------------------------------------------------------------------------------------------------------------
 * | n |    10^1    |    10^2    |    10^3    |    10^4    |    10^5    |    10^6    |    10^7    |    10^8    |    10^9    |
 *  ------------------------------------------------------------------------------------------------------------------------
 * | E | 9*10^{-15} | 9*10^{-14} | 9*10^{-13} | 9*10^{-12} | 9*10^{-11} | 9*10^{-10} | 9*10^{-9}  | 9*10^{-8}  | 9*10^{-7}  |
 *  ------------------------------------------------------------------------------------------------------------------------
 * 
 * @author Raffael Bild
 */
public class ExponentialMechanism<T> {

    /** 
     * Random number generator to use here
     * @author Fabian Prasser
     */
    private static class RandomNumberGenerator extends AbstractRandomGenerator {
        
        /** The random generator */
        private final Random random;

        /** 
         * Constructor
         * @param deterministic
         */
        public RandomNumberGenerator(boolean deterministic) {
            super();
            if (deterministic) {
                this.random = new Random(0xDEADBEEF);
            } else {
                this.random = new SecureRandom();
            }
        }

        @Override
        public double nextDouble() {
            return this.random.nextDouble();
        }

        @Override
        public void setSeed(long seed) {
            if (this.random instanceof SecureRandom) {
                throw new IllegalStateException("May not set seed on non-deterministic RNG");
            }
            this.random.setSeed(seed);
        }
    }

    /** The probability distribution */
    private EnumeratedDistribution<T> distribution;

    /** The privacy parameter epsilon */
    private double                    epsilon;

    /** The random generator */
    private AbstractRandomGenerator   random;
    
    /**
     * Constructs a new instance
     * @param epsilon
     */
    public ExponentialMechanism(double epsilon) {
        this(epsilon, false);
    }

    /**
     * Constructs a new instance
     * Note: *never* set deterministic to true in production. It is implemented for testing purposes, only.
     * 
     * @param epsilon
     * @param deterministic
     */
    public ExponentialMechanism(double epsilon, boolean deterministic) {
        this.epsilon = epsilon;
        this.random = new RandomNumberGenerator(deterministic);
    }
    
    /**
     * Returns a random sampled value
     * @return
     */
    public T sample() {
        T solution = distribution.sample();
        return solution;
    }
    
    /**
     * Sets the distribution to sample from.
     * The arrays values and scores have to have the same length.
     * @param values
     * @param scores
     */
    public void setDistribution(T[] values, double[] scores) {
        
        // Check arguments
        if (values.length == 0) {
            throw new IllegalStateException("No values supplied");
        }
        if (values.length != scores.length) {
            throw new IllegalStateException("Number of scores and values must be identical");
        }
        
        // The following code calculates the probability distribution which assigns every value
        // a probability proportional to exp(0,5 * epsilon * score)
        
        // Calculate all exponents having the form 0,5 * epsilon * score and remember the maximal value.
        // This value is used during the following calculations in a manner which reduces the magnitude of numbers involved
        // (which can get very large due to the application of the exponential function) while it does not change the result;
        // it is a trick to make the following computations feasible.
        double[] exponents = new double[scores.length];
        double shift = -Double.MAX_VALUE;
        for (int i = 0; i < scores.length; ++i) {
            exponents[i] = 0.5d * epsilon * scores[i];
            shift = Math.max(shift, exponents[i]);
        }

        // Create a probability mass function containing numbers of the form exp(0,5 * epsilon * score - shift)
        // which correspond to non-normalized probabilities multiplied with exp(-shift).
        List<Pair<T, Double>> pmf = new ArrayList<>();
        for (int i = 0; i < scores.length; ++i) {
            double prob = Math.exp(exponents[i] - shift);
            pmf.add(new Pair<T, Double>(values[i], prob));
        }

        // Create the distribution. Note that all probabilities are being normalized by the class
        // EnumeratedDistribution by effectively dividing them through their sum, which cancels
        // the factor exp(-shift)
        this.distribution = new EnumeratedDistribution<T>(this.random, pmf);
    }
}
