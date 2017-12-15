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
package org.deidentifier.arx.criteria;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * (e,d)-Differential Privacy implemented with (k,b)-SDGS as proposed in:
 * 
 * Ninghui Li, Wahbeh H. Qardaji, Dong Su:
 * On sampling, anonymization, and differential privacy or, k-anonymization meets differential privacy. 
 * Proceedings of ASIACCS 2012. pp. 32-33
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class EDDifferentialPrivacy extends ImplicitPrivacyCriterion {
    
    /** SVUID */
    private static final long        serialVersionUID = 242579895476272606L;

    /** Parameter */
    private final double             epsilon;
    /** Parameter */
    private final double             delta;
    /** Parameter */
    private final int                k;
    /** Parameter */
    private final double             beta;
    /** Parameter */
    private DataSubset               subset;
    /** Parameter */
    private transient boolean        deterministic    = false;
    /** Parameter */
    private DataGeneralizationScheme generalization;

    /**
     * Creates a new instance
     * @param epsilon
     * @param delta
     * @param generalization
     */
    public EDDifferentialPrivacy(double epsilon, double delta, 
                                 DataGeneralizationScheme generalization) {
        super(false, false);
        this.epsilon = epsilon;
        this.delta = delta;
        this.generalization = generalization;
        this.beta = calculateBeta(epsilon);
        this.k = calculateK(delta, epsilon, this.beta);
        this.deterministic = false;
    }
    
    /**
     * Creates a new instance which may be configured to produce deterministic output.
     * Note: *never* use this in production. It is implemented for testing purposes, only.
     * 
     * @param epsilon
     * @param delta
     * @param generalization
     * @param deterministic
     */
    public EDDifferentialPrivacy(double epsilon, double delta, 
                                 DataGeneralizationScheme generalization,
                                 boolean deterministic) {
        super(false, false);
        this.epsilon = epsilon;
        this.delta = delta;
        this.generalization = generalization;
        this.beta = calculateBeta(epsilon);
        this.k = calculateK(delta, epsilon, this.beta);
        this.deterministic = true;
    }
    

    @Override
    public EDDifferentialPrivacy clone() {
        return new EDDifferentialPrivacy(this.getEpsilon(), this.getDelta(), this.getGeneralizationScheme());
    }

    /**
     * Returns the k parameter of (k,b)-SDGS
     * @return
     */
    public double getBeta() {
        return beta;
    }
    
    @Override
    public DataSubset getDataSubset() {
        return subset;
    }

    /**
     * Returns the delta parameter of (e,d)-DP
     * @return
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Returns the epsilon parameter of (e,d)-DP
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Returns the defined generalization scheme
     * @return
     */
    public DataGeneralizationScheme getGeneralizationScheme() {
        return this.generalization;
    }

    /**
     * Returns the k parameter of (k,b)-SDGS
     * @return
     */
    public int getK() {
        return k;
    }

    @Override
    public int getMinimalClassSize() {
        return k;
    }
    
    @Override
    public int getRequirements(){
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER |
               ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }

    /**
     * Creates a random sample based on beta
     *
     * @param manager
     */
    public void initialize(DataManager manager, ARXConfiguration config){
        
        // If the subset has already been created
        if (subset != null) {
            return;
        }

        // Create RNG
        Random random;
        if (deterministic) {
            random = new Random(0xDEADBEEF);
        } else {
            random = new SecureRandom();
        }

        // Create a data subset via sampling based on beta
        Set<Integer> subsetIndices = new HashSet<Integer>();
        int records = manager.getDataGeneralized().getDataLength();
        for (int i = 0; i < records; ++i) {
            if (random.nextDouble() < beta) {
                subsetIndices.add(i);
            }
        }
        this.subset = DataSubset.create(records, subsetIndices);
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        return entry.count >= k;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return false;
    }
    
    @Override
    public boolean isMinimalClassSizeAvailable() {
        return true;
    }

    @Override
    public boolean isSubsetAvailable() {
        return subset != null;
    }
    
    @Override
    public ElementData render() {
        ElementData result = new ElementData("Differential privacy");
        result.addProperty("Epsilon", epsilon);
        result.addProperty("Delta", delta);
        result.addProperty("Uniqueness threshold (k)", k);
        result.addProperty("Sampling probability (beta)", beta);
        return result;
    }

    @Override
    public String toString() {
        return "(" + epsilon + "," + delta + ")-DP";
    }

    /**
     * Calculates a_n
     * @param n
     * @param epsilon
     * @param beta
     * @return
     */
    private double calculateA(int n, double epsilon, double beta) {
        double gamma = calculateGamma(epsilon, beta);
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
     * @return
     */
    private double calculateC(int n, double epsilon, double beta) {
        double gamma = calculateGamma(epsilon, beta);
        return (new Exp()).value(-1.0d * n * (gamma * (new Log()).value(gamma / beta) - (gamma - beta)));
    }

    /**
     * Calculates delta
     * @param k
     * @param epsilon
     * @param beta
     * @return
     */
    private double calculateDelta(int k, double epsilon, double beta) {
        double gamma = calculateGamma(epsilon, beta);
        int n_m = (int) Math.ceil((double) k / gamma - 1.0d);

        double delta = Double.MIN_VALUE;
        double bound = Double.MAX_VALUE;

        for (int n = n_m; delta < bound; ++n) {
            delta = Math.max(delta, calculateA(n, epsilon, beta));
            bound = calculateC(n, epsilon, beta);
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
        int k = 1;

        for (double delta_k = Double.MAX_VALUE; delta_k > delta; ++k) {
            delta_k = calculateDelta(k, epsilon, beta);
        }

        return k;
    }
}
