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
package org.deidentifier.arx.algorithm;

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
        private SecureRandom random;

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
        private Random random;

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

        mc = new MathContext(precision, RoundingMode.HALF_UP);

        double shift = Double.MAX_VALUE;
        for (double score : valueToScore.values()) {
            shift = Math.min(shift, 0.5d * epsilon * score);
        }
        shift *= -1d;

        BigDecimal divisor = new BigDecimal(0d, mc);
        Map<T, BigDecimal> transformationToEnumerator = new HashMap<T, BigDecimal>();
        for (Entry<T, Double> entry : valueToScore.entrySet()) {
            T transformation = entry.getKey();
            Double score = entry.getValue();

            Double exponent = 0.5d * epsilon * score + shift;
            BigDecimal enumerator = exp(exponent);
            transformationToEnumerator.put(transformation, enumerator);

            divisor = divisor.add(enumerator, mc);
        }

        List<Pair<T, Double>> pmf = new ArrayList<>();
        for (Entry<T, BigDecimal> entry : transformationToEnumerator.entrySet()) {
            T transformation = entry.getKey();
            BigDecimal enumerator = entry.getValue();
            BigDecimal probability = enumerator.divide(divisor, mc);
            pmf.add(new Pair<T, Double>(transformation, probability.doubleValue()));
        }

        AbstractRandomGenerator random = deterministic ? new DeterministicRandomGenerator() : new SecureRandomGenerator();
        
        distribution = new EnumeratedDistribution<T>(random, pmf);
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
     * Returns the probability mass function
     * @return
     */
    public List<Pair<T, Double>> getPmf() {
        return distribution.getPmf();
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
