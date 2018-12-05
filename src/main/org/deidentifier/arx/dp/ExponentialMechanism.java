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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

/**
 * An implementation of the reliable variant of the exponential mechanism.
 * This implementation assumes that all score values have been divided by the sensitivity of the respective score function.
 * 
 * Note: This implementations uses internal caches which may grow up to the size of distinct values
 * provided as scores to the method setDistribution during the lifetime of an instance.
 * 
 * @author Raffael Bild
 */
public class ExponentialMechanism<T> {
    
    /** The base having the form of a fraction n/d */
    private BigFraction                             base;

    /** The cumulative distribution scaled so that it consists of natural numbers */
    private BigInteger[]                            cumulativeDistribution;
    
    /** The values to sample from */
    private T[]                                     values;

    /** The random generator */
    private Random                                  random;

    /** A cache mapping an exponent e to n^e used to increase performance */
    private Map<Integer, BigInteger>                numeratorCache;

    /** A cache mapping an exponent e to d^e used to increase performance */
    private Map<Integer, BigInteger>                denominatorCache;

    /** A cache mapping a pair of exponents (e_1,e_2) to n^{e_1} / d^{e_2} used to increase performance */
    private Map<Pair<Integer, Integer>, BigInteger> productCache;

    /**
     * Creates a new instance
     * @param epsilon
     * @throws IntervalArithmeticException 
     */
    public ExponentialMechanism(double epsilon) throws IntervalArithmeticException {
        this(epsilon, false);
    }

    /**
     * Creates a new instance which may be configured to produce deterministic output.
     * Note: *never* set deterministic to true in production. This parameterization is for testing purposes, only.
     * 
     * @param epsilon
     * @param deterministic
     * @throws IntervalArithmeticException 
     */
    public ExponentialMechanism(double epsilon, boolean deterministic) throws IntervalArithmeticException {

        // Calculate the base, depending on epsilon
        IntervalArithmeticDouble arithmetic = new IntervalArithmeticDouble();
        double bound = arithmetic.exp(arithmetic.div(arithmetic.createInterval(epsilon), arithmetic.createInterval(3d))).lower;
        this.base = new BigFraction(bound);
        
        // Initialize the random generator
        this.random = deterministic ? new Random(0xDEADBEEF) : new SecureRandom();
        
        // Initialize caches
        this.numeratorCache = new HashMap<Integer,BigInteger>();
        this.denominatorCache = new HashMap<Integer,BigInteger>();
        this.productCache = new HashMap<Pair<Integer,Integer>, BigInteger>();
    }
    
    /**
     * Returns a value drawn from the probability distribution
     * @return
     */
    public T sample() {
        
        // Draw a number within the range of the cumulative distribution
        BigInteger drawn = getRandomBigInteger(cumulativeDistribution[cumulativeDistribution.length-1]);

        // Determine the according index
        for (int i = 0; i < cumulativeDistribution.length; i++) {
            if (drawn.compareTo(cumulativeDistribution[i]) == -1) {
                return values[i];
            }
        }

        // Must not happen
        throw new IllegalStateException("Must not happen");
    }
    
    /**
     * Builds a probability distribution
     * @param values
     * @param scores
     */
    public void setDistribution(T[] values, BigFraction[] scores) {
        
        // Check arguments
        if (values.length == 0) {
            throw new RuntimeException("No values supplied");
        }
        if (values.length != scores.length) {
            throw new RuntimeException("Number of scores and values must be identical");
        }
        
        // Initialize
        this.cumulativeDistribution = new BigInteger[scores.length];
        this.values = values;
        
        // The following code calculates a distribution consisting of natural numbers which is directly proportional to
        // b^{e_1} = n^{e_1} / d^{e_1} ... b^{e_m} = n^{e_m} / d^{e_m} with e_i = floorToInt(scores[i])
        // and accumulates the resulting distribution

        // Calculate the exponents e_i and determine the smallest exponent
        int[] exponents = new int[values.length];
        int minExponent = Integer.MAX_VALUE;
        for (int i=0; i<values.length; ++i) {
            int exponent = floorToInt(scores[i]);
            minExponent = Math.min(minExponent, exponent);
            exponents[i] = exponent;
        }
        
        // Subtract the smallest exponent from all exponents.
        // This modification reduces the magnitude of powers, increases the chances for cache hits,
        // and hence, it reduces the execution times of the following computations.
        // Since b^{e_i - minExponent} = b^e_i * b^{-minExponent} holds, this transformation corresponds to a multiplication
        // of the distribution with a constant factor and hence it retains proportionality.
        // Moreover, the maximum of the resulting exponents is determined.
        int maxExponent = Integer.MIN_VALUE;
        for (int i=0; i<values.length; ++i) {
            exponents[i] -= minExponent;
            maxExponent = Math.max(exponents[i], maxExponent);
        }
        
        // Calculate the scaled cumulative distribution by computing n^{exponents[i]} * d^{maxExponent - exponents[i]}
        // and accumulating the results. Since
        // n^{exponents[i]} * d^{maxExponent - exponents[i]} = (n^{exponents[i]} / d^{exponents[i]}) * d^{maxExponent}
        // = b^{exponents[i]} * d^{maxExponent} holds, this transformation corresponds to a multiplication
        // of the distribution with a constant factor and hence it retains proportionality.
        for (int i = 0; i < values.length; i++) {

            // Determine exponents
            int numeratorExponent = exponents[i];
            int denominatorExponent = maxExponent - numeratorExponent;
            
            // Assure that productCache contains n^{exponents[i]} * d^{maxExponent - exponents[i]}
            Pair<Integer,Integer> exponentPair = new Pair<Integer,Integer>(numeratorExponent, denominatorExponent);
            if (!productCache.containsKey(exponentPair)) {
                
                // Assure that numeratorCache contains n^{exponents[i]}
                if (!numeratorCache.containsKey(numeratorExponent)) {
                    numeratorCache.put(numeratorExponent, base.getNumerator().pow(numeratorExponent));
                }
                
                // Assure that denominatorCache contains d^{maxExponent - exponents[i]}
                if (!denominatorCache.containsKey(denominatorExponent)) {
                    denominatorCache.put(denominatorExponent, base.getDenominator().pow(denominatorExponent));
                }
                
                // Calculate n^{exponents[i]} * d^{maxExponent - exponents[i]} and insert into productCache
                BigInteger product = numeratorCache.get(numeratorExponent).multiply(denominatorCache.get(denominatorExponent));
                productCache.put(exponentPair, product);
            }
            
            // Retrieve n^{exponents[i]} * d^{maxExponent - exponents[i]}
            BigInteger nextElement = productCache.get(exponentPair);
            
            // Accumulate
            cumulativeDistribution[i] = i == 0 ? nextElement : nextElement.add(cumulativeDistribution[i-1]);
        }
    }

    /**
     * Returns floor() to int if possible.
     * If the absolute value of fraction is too big, an IllegalArgumentException is thrown.
     * @param fraction
     * @return
     */
    private int floorToInt(BigFraction fraction) {

        // Assure that score is within the range of numbers which can be processed
        if (fraction.compareTo(new BigFraction(Integer.MAX_VALUE)) == 1 || (fraction.subtract(1)).compareTo(new BigFraction(Integer.MIN_VALUE)) == -1) {
            throw new IllegalArgumentException("The absolute value of " + fraction + " is too big to be processed");
        }

        // Extract the whole number part of the fraction 
        int result = fraction.intValue();

        // Extracting the whole number part effectively rounds towards zero, and not downwards.
        // Hence, when fraction is negative and not an integer number, it has been rounded upwards.
        // This is corrected by subtracting one.
        if (fraction.compareTo(BigFraction.ZERO) < 0 && !fraction.equals(new BigFraction(result))) {
            result -= 1;
        }

        // Return
        return result;
    }

    /**
     * Returns a random BigInteger in the interval [0, limit).
     * The parameter limit has to be >= 1, otherwise an illegal argument exception is thrown.
     * @param limit
     * @return
     */
    private BigInteger getRandomBigInteger(BigInteger limit) {

        // Check the argument
        if (limit.compareTo(BigInteger.ONE) == -1) {
            throw new IllegalArgumentException("Limit has to be greater than or equal to one");
        }

        // Compute a random value
        BigInteger result;
        do {
            result = new BigInteger(limit.bitLength(), random);
        } while(result.compareTo(limit) >= 0);

        // Return
        return result;
    }
}
