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

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.distribution.BinomialDistribution;

/**
 * Implements parameter calculation for differential privacy
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 */
public class ParameterCalculationDouble implements ParameterCalculation {

    /** Cache for a */
    private ParameterCalculationSequenceCache<Double> aCache = null;

    /** Cache for c */
    private ParameterCalculationSequenceCache<Double> cCache = null;

    /** Result */
    private double                                    beta;

    /** Result */
    private int                                       k;
    
    /**
     * Constructor
     * @param epsilon
     * @param delta
     */
    public ParameterCalculationDouble(double epsilon, double delta) {
        
        this.aCache = new ParameterCalculationSequenceCache<Double>();
        this.cCache = new ParameterCalculationSequenceCache<Double>();
        
        this.beta = calculateBeta(epsilon);
        this.k = calculateK(delta, epsilon, this.beta);
    }
    
    @Override
    public double getBeta() {
        return beta;
    }

    @Override
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
     */
    private double calculateA(int n, double epsilon, double beta, double gamma) {
        return calculateBinomialSum((int) Math.floor(n * gamma) + 1, n, beta);
    }
    
    /**
     * Calculates beta_max
     * @param epsilon
     * @return
     */
    private double calculateBeta(double epsilon) {
        return 1.0d - (new Exp()).value(-1.0d * epsilon);
    }

    /**
     * Adds summands of the binomial distribution with probability beta
     * @param from
     * @param to
     * @param beta
     * @return
     */
    private double calculateBinomialSum(int from, int to, double beta) {
        BinomialDistribution binomialDistribution = new BinomialDistribution(to, beta);
        double sum = 0.0d;

        for (int j = from; j <= to; ++j) {
            sum += binomialDistribution.probability(j);
        }

        return sum;
    }

    /**
     * Calculates c_n
     * @param n
     * @param epsilon
     * @param beta
     * @param gamma 
     * @return
     */
    private double calculateC(int n, double epsilon, double beta, double gamma) {
        return (new Exp()).value(-1.0d * n * (gamma * (new Log()).value(gamma / beta) - (gamma - beta)));
    }

    /**
     * Calculates delta
     * @param k
     * @param epsilon
     * @param beta
     * @param gamma 
     * @return
     */
    private double calculateDelta(int k, double epsilon, double beta, double gamma) {
        int n_m = (int) Math.ceil((double) k / gamma - 1.0d);
        
        double delta = -Double.MAX_VALUE;
        double bound = Double.MAX_VALUE;

        for (int n = n_m; delta < bound; ++n) {
            if (!aCache.containsKey(n)) {
                aCache.put(n, calculateA(n, epsilon, beta, gamma));
                cCache.put(n, calculateC(n, epsilon, beta, gamma));
            }
            delta = Math.max(delta, aCache.get(n));
            bound = cCache.get(n);
        }
        return delta;
    }

    /**
     * Calculates gamma
     * @param epsilon
     * @param beta
     * @return
     */
    private double calculateGamma(double epsilon, double beta) {
        double power = (new Exp()).value(epsilon);
        return (power - 1.0d + beta) / power;
    }
    
    /**
     * Calculates k
     * @param delta
     * @param epsilon
     * @param beta
     * @return
     */
    private int calculateK(double delta, double epsilon, double beta) {
        
        double gamma = calculateGamma(epsilon, beta);
        
        int k = 1;
        double delta_k = Double.MAX_VALUE;
        for (; delta_k > delta; ++k) {
            delta_k = calculateDelta(k, epsilon, beta, gamma);
        }
        return k;
    }
}
