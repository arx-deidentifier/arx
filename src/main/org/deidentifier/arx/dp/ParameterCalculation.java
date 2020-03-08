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

import java.util.LinkedHashMap;
import java.util.Map;

import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;
import org.deidentifier.arx.reliability.IntervalDouble;

/**
 * Implements the parameter calculation for differential privacy as proposed in:
 * 
 * Bild R, Kuhn KA, Prasser F. SafePub: A Truthful Data Anonymization Algorithm With Strong Privacy Guarantees.
 * Proceedings on Privacy Enhancing Technologies. 2018(1):67-87.
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 */
public class ParameterCalculation {

    /** Interval arithmetic system*/
    private final IntervalArithmeticDouble                    arithmetic;

    /** Result */
    private double                                            beta;

    /** Result */
    private int                                               k;

    /**
     * Constructor
     * @param epsilon
     * @param delta
     * @throws IntervalArithmeticException 
     */
    public ParameterCalculation(double epsilon, double delta) throws IntervalArithmeticException {
        
        this.arithmetic = new IntervalArithmeticDouble();
        
        IntervalDouble epsilonInterval = this.arithmetic.createInterval(epsilon);
        IntervalDouble deltaInterval = this.arithmetic.createInterval(delta);
        
        IntervalDouble beta = calculateBeta(epsilonInterval);
        this.beta = beta.lower;
        this.k = calculateK(deltaInterval, epsilonInterval, beta);
    }
    
    /**
     * Returns k
     * @return
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Returns beta
     * @return
     */
    public int getK() {
        return k;
    }

    /**
     * Calculates a_n
     * @param n
     * @param epsilon
     * @param beta
     * @param gamma
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble calculateA(int n, IntervalDouble epsilon, IntervalDouble beta, IntervalDouble gamma) throws IntervalArithmeticException {
        
        // This method performs the following calculation using interval arithmetic:
        //
        // double gamma = calculateGamma(epsilon, beta);
        // return calculateBinomialSum((int) Math.floor(n * gamma) + 1, n, beta);
        
        // Using the lower bound can only increase the degree of privacy protection provided
        // and allows to process intervals for which floor to int is not decidable
        return calculateBinomialSum(arithmetic.floorLowerBoundToInt(arithmetic.mult(arithmetic.createInterval(n), gamma)) + 1, n, beta);
    }

    /**
     * Calculates beta_max
     * 1.0d - (new Exp()).value(-1.0d * epsilon);
     * @param epsilon
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble calculateBeta(IntervalDouble epsilon) throws IntervalArithmeticException {
        
        // This method performs the following calculation using interval arithmetic:
        //
        // return 1.0d - (new Exp()).value(-1.0d * epsilon);
        
        return arithmetic.sub(arithmetic.ONE, arithmetic.exp(arithmetic.mult(arithmetic.MINUS_ONE, epsilon)));
    }
    
    /**
     * Adds summands of the binomial distribution with probability beta
     * @param from
     * @param to
     * @param beta
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble calculateBinomialSum(int from, int to, IntervalDouble beta) throws IntervalArithmeticException {
        
        // This method performs the following calculation using interval arithmetic:
        //
        // BinomialDistribution binomialDistribution = new BinomialDistribution(to, beta);
        // double sum = 0.0d;
        // for (int j = from; j <= to; ++j) {
        //     sum += binomialDistribution.probability(j);
        // }
        // return sum;
        
        IntervalDouble sum = arithmetic.ZERO;

        for (int j = from; j <= to; ++j) {
            IntervalDouble probability = arithmetic.binomialProbability(to, beta, j);
            sum = arithmetic.add(sum, probability);
        }

        return sum;
    }

    /**
     * Calculates c_n
     * (new Exp()).value(-1.0d * n * (gamma * (new Log()).value(gamma / beta) - (gamma - beta)));
     * @param n
     * @param epsilon
     * @param beta
     * @param gamma
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble calculateC(int n, IntervalDouble epsilon, IntervalDouble beta, IntervalDouble gamma) throws IntervalArithmeticException {
        
        // This method performs the following calculation using interval arithmetic:
        //
        // double gamma = calculateGamma(epsilon, beta);
        // return (new Exp()).value(-1.0d * n * (gamma * (new Log()).value(gamma / beta) - (gamma - beta)));
        
        IntervalDouble term1 = arithmetic.mult(gamma, arithmetic.log(arithmetic.div(gamma, beta)));
        IntervalDouble term2 = arithmetic.sub(term1, arithmetic.sub(gamma, beta));
        return arithmetic.exp(arithmetic.mult(arithmetic.MINUS_ONE, 
                                              arithmetic.mult(arithmetic.createInterval(n), term2)));   
    }

    /**
     * Calculates delta
     * @param k
     * @param epsilon
     * @param beta
     * @param gamma
     * @param aCache 
     * @param cCache 
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble calculateDelta(int k, IntervalDouble epsilon, IntervalDouble beta, IntervalDouble gamma,
                                          LinkedHashMap<Integer, IntervalDouble> aCache, LinkedHashMap<Integer, IntervalDouble> cCache) throws IntervalArithmeticException {
        
        // This method performs the following calculation using interval arithmetic:
        //
        // double gamma = calculateGamma(epsilon, beta);
        // int n_m = (int) Math.ceil((double) k / gamma - 1.0d);

        // double delta = -Double.MAX_VALUE;
        // double bound = Double.MAX_VALUE;

        // for (int n = n_m; delta < bound; ++n) {
        //  delta = Math.max(delta, calculateA(n, epsilon, beta));
        //  bound = calculateC(n, epsilon, beta);
        // }
        // return delta;
        
        // Using the lower bound can only increase the degree of privacy protection provided
        // and allows to process intervals for which ceil to int is not decidable
        int n_m = arithmetic.ceilLowerBoundToInt(arithmetic.sub(arithmetic.div(arithmetic.createInterval(k), gamma), arithmetic.ONE));

        IntervalDouble delta = arithmetic.createInterval(-Double.MAX_VALUE);
        IntervalDouble bound = arithmetic.createInterval(Double.MAX_VALUE);

        // Assure that delta is greater than bound to guarantee the desired degree of privacy protection
        for (int n = n_m; arithmetic.lessThanOrOverlap(delta, bound); ++n) {
            if (!aCache.containsKey(n)) {
                aCache.put(n, calculateA(n, epsilon, beta, gamma));
                cCache.put(n, calculateC(n, epsilon, beta, gamma));
            }
            delta = arithmetic.max(delta, aCache.get(n));
            bound = cCache.get(n);
        }

        return delta;
    }

    /**
     * Calculates gamma
     * @param epsilon
     * @param beta
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble calculateGamma(IntervalDouble epsilon, IntervalDouble beta) throws IntervalArithmeticException {
        
         // This method performs the following calculation using interval arithmetic:
        //
        // double power = (new Exp()).value(epsilon);
        // return (power - 1.0d + beta) / power;
        
        IntervalDouble power = arithmetic.exp(epsilon);
        return arithmetic.div(arithmetic.add(arithmetic.sub(power, arithmetic.ONE), beta), power);
        
    }
    
    /**
     * Calculates k
     * @param delta
     * @param epsilon
     * @param beta
     * @return
     * @throws IntervalArithmeticException 
     */
    private int calculateK(IntervalDouble delta, IntervalDouble epsilon, IntervalDouble beta) throws IntervalArithmeticException {
        
        // This method performs the following calculation using interval arithmetic:
        //
        // int k = 1;
        // for (double delta_k = Double.MAX_VALUE; delta_k > delta; ++k) {
        //     delta_k = calculateDelta(k, epsilon, beta);
        // }
        // return k;
        
        IntervalDouble gamma = calculateGamma(epsilon, beta);
        
        // Prepare caches for values of the sequences a and c
        
        final int CACHE_SIZE = 1000;
        
        @SuppressWarnings({ "serial" })
        LinkedHashMap<Integer, IntervalDouble> aCache = new LinkedHashMap<Integer, IntervalDouble>(CACHE_SIZE) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, IntervalDouble> eldest) {
                return size() > CACHE_SIZE;
            }
        };
        
        @SuppressWarnings({ "serial" })
        LinkedHashMap<Integer, IntervalDouble> cCache = new LinkedHashMap<Integer, IntervalDouble>(CACHE_SIZE) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, IntervalDouble> eldest) {
                return size() > CACHE_SIZE;
            }
        };
        
        int k = 1;
        IntervalDouble delta_k = arithmetic.createInterval(Double.MAX_VALUE);
        // Assure that delta_k is smaller than the desired value delta to guarantee privacy protection
        for (; !arithmetic.lessThan(delta_k, delta); ++k) {
            delta_k = calculateDelta(k, epsilon, beta, gamma, aCache, cCache);
        }
        return k;
    }
}
