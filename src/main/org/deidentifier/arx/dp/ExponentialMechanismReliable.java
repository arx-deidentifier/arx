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
import java.util.Random;

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
    private BigInteger[] cumulativeDistribution;
    
    /** The random generator */
    private Random random;
    
    /** The values to sample from */
    private T[] values;
    
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
        
    	// Initialize
    	BigFraction[] cumulativeDistributionFractions = new BigFraction[scores.length];
    	this.cumulativeDistribution = new BigInteger[scores.length];
        this.values = values.clone();
        
        int index = 0;
        BigFraction denominatorProduct = BigFraction.ONE;
        BigFraction lastAccumulated = new BigFraction(0);
        for (index = 0; index < values.length; index++) {
        	
        	// Extract
        	BigFraction score = scores[index];
        	
        	// Calculate the next element of the cumulative distribution
        	BigFraction nextAccumulated = lastAccumulated.add(base.pow(floorToInt(score)));
        	
        	// Update the product of denominators
        	denominatorProduct = denominatorProduct.multiply(score.getDenominator());
        	
        	// Store
        	cumulativeDistributionFractions[index] = nextAccumulated;
        	lastAccumulated = nextAccumulated;
        }
        
        // Scale the cumulative distribution so that it contains only natural numbers
        for (index = 0; index < cumulativeDistributionFractions.length; index++) {
        	this.cumulativeDistribution[index] = (cumulativeDistributionFractions[index].multiply(denominatorProduct)).getNumerator();
        }
        
        // Initialize the random generator
        random = deterministic ? new Random(0xDEADBEEF) : new SecureRandom();
    }
    
    /**
     * Returns a random value sampled from this distribution
     * @return the random value
     */
    public T sample() {
    	
    	// Draw a number within the range of the cumulative distribution
    	BigInteger drawn = getRandomBigInteger(cumulativeDistribution[cumulativeDistribution.length-1]);
    	
    	// Determine the according index
    	int index;
    	for (index = 0; index < cumulativeDistribution.length; index++) {
    		if (drawn.compareTo(cumulativeDistribution[index]) == -1) {
    			break;
    		}
    	}
    	
    	// Return the according value
        return values[index];
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
     * @param rand
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
