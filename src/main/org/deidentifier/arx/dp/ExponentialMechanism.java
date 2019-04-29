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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.analysis.function.Exp;
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
 * the privacy guarantees provided. However, it can be shown that the resulting exceedance of the privacy parameter epsilon
 * is at most in the order of log((1 + n * 10^{1-p}) / (1 - n * 10^{1-p})) where n is the size of the domain and p is
 * the floating-point precision which can be specified by the user.
 * 
 * @author Raffael Bild
 */
public class ExponentialMechanism<T> {

    /** The precision to use for BigDecimal arithmetic */
    public static final int           DEFAULT_PRECISION = 100;

    /** The probability distribution */
    private EnumeratedDistribution<T> distribution;

    /** The privacy parameter epsilon */
    private double                    epsilon;

    /** The math context to use for BigDecimal arithmetic */
    private MathContext               mc;

    /** The random generator */
    private AbstractRandomGenerator   random;

    /** A cryptographically strong random generator */
    private static class SecureRandomGenerator extends AbstractRandomGenerator {
        
        /** The random generator */
        private SecureRandom random;

        /** Constructor */
        public SecureRandomGenerator() {
            super();
            random = new SecureRandom();
        }

        @Override
        public double nextDouble() {
            return random.nextDouble();
        }

        @Override
        public void setSeed(long seed) {
            random.setSeed(seed);
        }
    }
    
    /** A deterministic random generator */
    private static class DeterministicRandomGenerator extends AbstractRandomGenerator {
        
        /** The random generator */
        private Random random;

        /** Constructor */
        public DeterministicRandomGenerator() {
            super();
            random = new Random(0xDEADBEEF);
        }

        @Override
        public double nextDouble() {
            return random.nextDouble();
        }

        @Override
        public void setSeed(long seed) {
            random.setSeed(seed);
        }
    }

    /**
     * Constructs a new instance
     * @param epsilon
     */
    public ExponentialMechanism(double epsilon) {
        this(epsilon, DEFAULT_PRECISION, false);
    }
    
    /**
     * Constructs a new instance
     * @param epsilon
     * @param deterministic
     */
    public ExponentialMechanism(double epsilon, boolean deterministic) {
        this(epsilon, DEFAULT_PRECISION, deterministic);
    }

    /**
     * Constructs a new instance
     * Note: *never* set deterministic to true in production. It is implemented for testing purposes, only.
     * 
     * @param epsilon
     * @param precision
     * @param deterministic
     */
    public ExponentialMechanism(double epsilon, int precision, boolean deterministic) {
        this.mc = new MathContext(precision, RoundingMode.HALF_UP);
        this.epsilon = epsilon;
        this.random = deterministic ? new DeterministicRandomGenerator() : new SecureRandomGenerator();
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
            throw new RuntimeException("No values supplied");
        }
        if (values.length != scores.length) {
            throw new RuntimeException("Number of scores and values must be identical");
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

        // For every value, calculate exp(0,5 * epsilon * score - shift) (enumerator), and calculate the sum of all these numbers (divisor).
        // Note that all numbers are effectively being multiplied with exp(-shift).
        BigDecimal divisor = new BigDecimal(0d, mc);
        BigDecimal[] enumerators = new BigDecimal[scores.length];
        for (int i = 0; i < scores.length; ++i) {
            enumerators[i] = exp(exponents[i] - shift);
            divisor = divisor.add(enumerators[i], mc);
        }

        // Compute the probability for every value by calculating enumerator / divisor.
        // Note that during this computation, the factor exp(-shift) is effectively being cancelled.
        List<Pair<T, Double>> pmf = new ArrayList<>();
        for (int i = 0; i < scores.length; ++i) {
            BigDecimal enumerator = enumerators[i];
            BigDecimal probability = enumerator.divide(divisor, mc);
            // Add entry to the PMF.
            // Note that BigDecimal.doubleValue() may yield infinity.
            // In such a case, the constructor of the class EnumeratedDistribution will throw an exception.
            pmf.add(new Pair<T, Double>(values[i], probability.doubleValue()));
        }

        // Store the distribution
        this.distribution = new EnumeratedDistribution<T>(this.random, pmf);
    }

    /**
     * An implementation of the exponential function for the BigDecimal type
     * @param exponent
     * @return
     */
    private BigDecimal exp(double exponent) {
        int exponentFloor = (int) (Math.floor(exponent));
        double exponentRemainder = exponent - exponentFloor;

        BigDecimal floorPart = (new BigDecimal(new Exp().value(1), mc)).pow(exponentFloor, mc);
        double remPart = (new Exp()).value(exponentRemainder);

        return floorPart.multiply(new BigDecimal(remPart, mc), mc);
    }
}
