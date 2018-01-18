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
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

/**
 * An implementation of the reliable exponential mechanism.
 * This implementation assumes a sensitivity of one (which can always be achieved by scaling score functions appropriately).
 * 
 * @author Raffael Bild
 */
public class ExponentialMechanismReliable<T> extends AbstractExponentialMechanism<T, BigFraction> {

    /** The distribution scaled so that it consists of natural numbers */
    private BigInteger[] distribution;

    /** The random generator */
    private Random random;

    /** The values to sample from */
    private T[] values;
    
    /** The base of the mechanism */
    BigFraction base;
    
    private Map<Integer,BigInteger> numeratorCache;
    
    private Map<Integer,BigInteger> denominatorCache;
    
    private Map<Pair<Integer,Integer>, BigInteger> productCache;
    
    public static long distributionTime = 0;
    
    public static long drawTime = 0;
    
    public static long floorTime = 0;
    
    public static long sumTime = 0;
    
    public static long multTime = 0;
    
    public static long powTime = 0;
    
    public static long usedMemory = 0;
    
    public static long cacheHits = 0;
    
    public static long cacheMisses = 0;
    
    public static long largestDistLength = 0;
    
    public static BigInteger largestCumulated = BigInteger.ZERO;

    /**
     * Creates a new instance
     * @param epsilon
     * @throws IntervalArithmeticException 
     */
    public ExponentialMechanismReliable(double epsilon) throws IntervalArithmeticException {
        this(epsilon, false);
    }

    /**
     * Creates a new instance which may be configured to produce deterministic output.
     * Note: *never* set deterministic to true in production. It is implemented for testing purposes, only.
     * 
     * @param epsilon
     * @param deterministic
     * @throws IntervalArithmeticException 
     */
    public ExponentialMechanismReliable(double epsilon, boolean deterministic) throws IntervalArithmeticException {

        // Calculate the base to use, depending on epsilon
        IntervalArithmeticDouble arithmetic = new IntervalArithmeticDouble();
        double bound = arithmetic.exp(arithmetic.div(arithmetic.createInterval(epsilon), arithmetic.createInterval(3d))).getLowerBound();
        this.base = new BigFraction(bound);
        
        // Initialize the random generator
        this.random = deterministic ? new Random(0xDEADBEEF) : new SecureRandom();
        
        // Initialize caches
        this.numeratorCache = new HashMap<Integer,BigInteger>();
        this.denominatorCache = new HashMap<Integer,BigInteger>();
        this.productCache = new HashMap<Pair<Integer,Integer>, BigInteger>();
    }
    
    @Override
    public T sample() {
        
        long drawTime = System.nanoTime();

        // Draw a number within the range of the cumulative distribution
        BigInteger drawn = getRandomBigInteger(distribution[distribution.length-1]);

        // Determine the according index
        int index;
        for (index = 0; index < distribution.length; index++) {
            if (drawn.compareTo(distribution[index]) == -1) {
                break;
            }
        }
        
        this.drawTime += System.nanoTime() - drawTime;
        
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.usedMemory = Math.max(this.usedMemory, usedMemory);

        // Return the according value
        return values[index];
    }
    
    @Override
    public void setDistribution(T[] values, BigFraction[]scores) {
        
        long initTime = System.nanoTime();
        
        // Check arguments
        super.setDistribution(values, scores);

        // Determine the smallest of all exponents.
        // This value is used during the following calculations in a manner which reduces the magnitude of numbers involved
        // (which can get very large due to the application of the exponential function) while it does not change the result;
        // it is a trick to make the following computations feasible.
        int shift = 0;
        int[] exponents = new int[values.length];
        boolean first = true;
        for (int i=0; i<values.length; ++i) {
            long floorTime = System.nanoTime();
            int nextShift = floorToInt(scores[i]);
            this.floorTime += System.nanoTime() - floorTime;
            if (first) {
                shift = nextShift;
                first = false;
            } else if (nextShift < shift) {
                shift = nextShift;
            }
            exponents[i] = nextShift;
        }
        int maxExponent = Integer.MIN_VALUE;
        for (int i=0; i<values.length; ++i) {
            exponents[i] -= shift;
            maxExponent = Math.max(exponents[i], maxExponent);
        }
        BigInteger maxDenominator = base.getDenominator().pow(maxExponent);
        if (!denominatorCache.containsKey(maxExponent)) {
            denominatorCache.put(maxExponent, maxDenominator);
        }

        // Initialize
        this.distribution = new BigInteger[scores.length];
        this.values = values;

        int index = 0;
        for (index = 0; index < values.length; index++) {

            long powTime = System.nanoTime();
            
            // Calculate the next element of the cumulative distribution
            int exponent = exponents[index];
            
            if (!numeratorCache.containsKey(exponent)) {
                numeratorCache.put(exponent, base.getNumerator().pow(exponent));
            }
            
            int denominatorExponent = maxExponent - exponent;
            if (!denominatorCache.containsKey(denominatorExponent)) {
                denominatorCache.put(denominatorExponent, base.getDenominator().pow(denominatorExponent));
            }
            
            this.powTime += System.nanoTime() - powTime;
            
            
            long multTime = System.nanoTime();
            
            Pair<Integer,Integer> exponentPair = new Pair<Integer,Integer>(exponent, denominatorExponent);
            if (!productCache.containsKey(exponentPair)) {
                cacheMisses++;
                BigInteger product = numeratorCache.get(exponent).multiply(denominatorCache.get(denominatorExponent));
                productCache.put(exponentPair, product);
            } else {
                cacheHits++;
            }
            
            this.multTime += System.nanoTime() - multTime;
            
            
            BigInteger next = productCache.get(exponentPair);
            
            
            long sumTime = System.nanoTime();
            distribution[index] = index == 0 ? next : next.add(distribution[index-1]);
            this.sumTime += System.nanoTime() - sumTime;
        }

        this.distributionTime += System.nanoTime() - initTime;
        
        this.largestDistLength = Math.max(this.largestDistLength, distribution.length);
        this.largestCumulated = this.largestCumulated.max(distribution[distribution.length-1]);
        
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.usedMemory = Math.max(this.usedMemory, usedMemory);
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

    /**
     * Returns a random BigInteger in the interval [0, limit).
     * The parameter limit has to be >= 1, otherwise an illegal argument exception is thrown.
     * @param limit
     * @return
     */
    private BigInteger getRandomBigInteger(BigInteger limit) {

        // Check the argument
        if (limit.compareTo(BigInteger.ONE) == -1) {
            throw new IllegalArgumentException("limit has to be greater than or equal to one");
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
