/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * @author Raffael Bild
 */
public class ExponentialMechanism<T> {

    /** The precision to use for BigDecimal arithmetic */
    public static final int defaultPrecision = 100;

    /** The math context to use for BigDecimal arithmetic */
    private MathContext     mc;

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

    /** The probability distribution */
    private EnumeratedDistribution<T> distribution;
    
    /**
     * Constructs a new instance
     * @param valueToScore
     * @param epsilon
     */
    public ExponentialMechanism(Map<T, Double> valueToScore, double epsilon) {
        this(valueToScore, epsilon, defaultPrecision, false);
    }

    /**
     * Constructs a new instance
     * Note: *never* set deterministic to true in production. It is implemented for testing purposes, only.
     * 
     * @param valueToScore
     * @param epsilon
     * @param precision
     * @param deterministic
     */
    public ExponentialMechanism(Map<T, Double> valueToScore, double epsilon, int precision, boolean deterministic) {
        
        // The following code calculates the probability distribution which assigns every value
        // a probability proportional to exp(0,5 * epsilon * score)

        mc = new MathContext(precision, RoundingMode.HALF_UP);

        // Determine the smallest of all exponents having the form 0,5 * epsilon * score.
        // This value is used during the following calculations in a manner which reduces the magnitude of numbers involved
        // (which can get very large due to the application of the exponential function) while it does not change the result;
        // it is a trick to make the following computations feasible.
        double shift = Double.MAX_VALUE;
        for (double score : valueToScore.values()) {
            shift = Math.min(shift, 0.5d * epsilon * score);
        }

        // For every value, calculate exp(0,5 * epsilon * score) (enumerator), and calculate the sum of all these numbers (divisor).
        // Note that all numbers are effectively being multiplied with exp(-shift).
        BigDecimal divisor = new BigDecimal(0d, mc);
        Map<T, BigDecimal> valueToEnumerator = new HashMap<T, BigDecimal>();
        for (Entry<T, Double> entry : valueToScore.entrySet()) {
            T value = entry.getKey();
            Double score = entry.getValue();

            BigDecimal enumerator = exp(0.5d * epsilon * score - shift);
            valueToEnumerator.put(value, enumerator);

            divisor = divisor.add(enumerator, mc);
        }

        // Compute the probability for every value by calculating enumerator / divisor.
        // Note that during this computation, the factor exp(-shift) is effectively being cancelled.
        List<Pair<T, Double>> pmf = new ArrayList<>();
        for (Entry<T, BigDecimal> entry : valueToEnumerator.entrySet()) {
            T value = entry.getKey();
            BigDecimal enumerator = entry.getValue();
            BigDecimal probability = enumerator.divide(divisor, mc);
            pmf.add(new Pair<T, Double>(value, probability.doubleValue()));
        }

        AbstractRandomGenerator random = deterministic ? new DeterministicRandomGenerator() : new SecureRandomGenerator();
        
        distribution = new EnumeratedDistribution<T>(random, pmf);
    }

    /**
     * Returns the probability mass function
     * @return
     */
    public List<Pair<T, Double>> getPmf() {
        return distribution.getPmf();
    }
    
    /**
     * Returns a random value sampled from this distribution
     * @return
     */
    public T sample() {
        T solution = distribution.sample();
        return solution;
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
