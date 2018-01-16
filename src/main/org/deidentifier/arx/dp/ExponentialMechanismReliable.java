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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

/**
 * An implementation of the reliable exponential mechanism.
 * This implementation assumes a sensitivity of one (which can always be achieved by scaling score functions appropriately).
 * 
 * @author Raffael Bild
 */
public class ExponentialMechanismReliable<T> {

    /** The cumulative distribution scaled so that it consists of natural numbers */
    private BigFraction[] cumulativeDistribution;

    /** The random generator */
    private Random random;

    /** The values to sample from */
    private T[] values;
    
    public static Map<Integer,BigFraction> cache = new HashMap<Integer,BigFraction>();
    
    public static long cacheHits = 0;
    
    public static long cacheMisses = 0;
    
    public static long initTime = 0;
    
    public static long powTime = 0;
    
    public static long transformTime = 0;
    
    public static long usedMemory = 0;

    /**
     * Creates a new instance
     * @param values
     * @param scores
     * @param epsilon
     * @throws IntervalArithmeticException 
     */
    public ExponentialMechanismReliable(T[] values, BigFraction[] scores, double epsilon) throws IntervalArithmeticException {
        this(values, scores, epsilon, false);
    }

    /**
     * Creates a new instance which may be configured to produce deterministic output.
     * Note: *never* set deterministic to true in production. It is implemented for testing purposes, only.
     * 
     * @param values
     * @param scores
     * @param epsilon
     * @param deterministic
     * @throws IntervalArithmeticException 
     */
    public ExponentialMechanismReliable(T[] values, BigFraction[] scores, double epsilon, boolean deterministic) throws IntervalArithmeticException {

        long initTime = System.nanoTime();
        
        // Check arguments
        if (values.length == 0) {
            throw new RuntimeException("No values supplied");
        }
        if (values.length != scores.length) {
            throw new RuntimeException("Number of scores and values must be identical");
        }

        // Calculate the base to use, depending on epsilon
        IntervalArithmeticDouble arithmetic = new IntervalArithmeticDouble();
        double bound = arithmetic.exp(arithmetic.div(arithmetic.createInterval(epsilon), arithmetic.createInterval(3d))).getLowerBound();
        BigFraction base = new BigFraction(bound);

        // Verify that a useful base could be calculated
        if (base.compareTo(new BigFraction(0)) != 1) {
            throw new IllegalArgumentException("No appropriate base could be derived for the given value epsilon = " + epsilon);
        }

        // Determine the smallest of all exponents.
        // This value is used during the following calculations in a manner which reduces the magnitude of numbers involved
        // (which can get very large due to the application of the exponential function) while it does not change the result;
        // it is a trick to make the following computations feasible.
        int shift = 0;
        int[] exponents = new int[values.length];
        boolean first = true;
        for (int i=0; i<values.length; ++i) {
            int nextShift = floorToInt(scores[i]);
            if (first) {
                shift = nextShift;
                first = false;
            } else if (nextShift < shift) {
                shift = nextShift;
            }
            exponents[i] = nextShift;
        }
        for (int i=0; i<values.length; ++i) {
            exponents[i] -= shift;
        }
//        BigFraction correction = base.pow(shift);

        // Initialize
        this.cumulativeDistribution = new BigFraction[scores.length];
        this.values = values.clone();

        int index = 0;
        BigFraction sum = new BigFraction(0);
        for (index = 0; index < values.length; index++) {

            // Calculate the next element of the cumulative distribution
            int exponent = exponents[index] - shift;
            if (cache.containsKey(exponent)) {
                this.cumulativeDistribution[index] = cache.get(exponent);
                this.cacheHits++;
            } else {
                long powTime = System.nanoTime();
                this.cumulativeDistribution[index] = base.pow(exponent);
                this.powTime += System.nanoTime() - powTime;
                cache.put(exponent, this.cumulativeDistribution[index]);
                this.cacheMisses++;
            }
            sum = sum.add(this.cumulativeDistribution[index]);
        }

        long transformTime = System.nanoTime();
        // Transform to probabilities and accumulate
        for (index = 0; index < this.cumulativeDistribution.length; index++) {
            this.cumulativeDistribution[index] = this.cumulativeDistribution[index].divide(sum);
//            this.cumulativeDistribution[index] = this.cumulativeDistribution[index].multiply(correction);
            if (index > 0) {
                this.cumulativeDistribution[index] = this.cumulativeDistribution[index].add(this.cumulativeDistribution[index-1]);
            }
        }
        this.transformTime += System.nanoTime() - transformTime;

        // Initialize the random generator
        random = deterministic ? new Random(0xDEADBEEF) : new SecureRandom();
        
        this.initTime += System.nanoTime() - initTime;
        
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.usedMemory = Math.max(this.usedMemory, usedMemory);
    }

    /**
     * Returns a random value sampled from this distribution
     * @return the random value
     */
    public T sample() {

        // Draw a number within the range of the cumulative distribution
        //      BigInteger drawn = getRandomBigInteger(cumulativeDistribution[cumulativeDistribution.length-1]);
        BigFraction drawn = new BigFraction(random.nextDouble());

        // Determine the according index
        int index;
        for (index = 0; index < cumulativeDistribution.length; index++) {
            if (drawn.compareTo(cumulativeDistribution[index]) == -1) {
                //          if (drawn.compareTo(cumulativeDistribution[index]) < 0) {
                break;
            }
        }

        // Return the according value
        return values[index];
    }
    
    public static void main(String[] args) {
        
        BigInteger enumerator = new BigInteger("1164062411989977");
        BigInteger denominator = new BigInteger("1125899906842624");
        
        BigFraction base = new BigFraction(enumerator, denominator);
        int exponent = 300;
        
        long time = System.nanoTime();
        BigFraction resultOne = base.pow(exponent);
        System.out.println((double)(System.nanoTime() - time) / 1e9);
        
        time = System.nanoTime();
        for (int i=0; i<exponent; ++i) {
            enumerator = enumerator.multiply(enumerator);
            denominator = denominator.multiply(denominator);
        }
        BigFraction resultTwo = new BigFraction(enumerator, denominator);
        System.out.println((double)(System.nanoTime() - time) / 1e9);
        
        System.out.println(resultOne + " " + resultTwo);
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

        // Correct direction of rounding if necessary
        if (result < 0 && result != fraction.intValue()) {
            result -= 1;
        }

        // Return
        return result;
    }

//    /**
//     * Returns a random BigInteger in the interval [0, limit).
//     * The parameter limit has to be >= 1, otherwise an illegal argument exception is thrown.
//     * @param rand
//     * @param limit
//     * @return
//     */
//    private BigInteger getRandomBigInteger(BigInteger limit) {
//
//        // Check the argument
//        if (limit.compareTo(BigInteger.ONE) == -1) {
//            throw new IllegalArgumentException("limit has to be greater than or equal to one");
//        }
//
//        // Compute a random value
//        BigInteger result;
//        do {
//            result = new BigInteger(limit.bitLength(), random);
//        } while(result.compareTo(limit) >= 0);
//
//        // Return
//        return result;
//    }
}
