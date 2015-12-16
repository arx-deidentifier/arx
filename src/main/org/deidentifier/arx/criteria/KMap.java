/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The k-map criterion as proposed by Latanya Sweeney.
 * Additionally to providing a population it can be estimated.
 * Therefore this class also implmentes the D3 (Poisson) and D4 (zero-truncated Poisson) criteria published in:
 * K. El Emam and F. Dankar, "Protecting privacy using k-anonymity" JAMIA, vol. 15, no. 5, pp. 627–637, 2008.
 * Criterion D3 was first published in:
 * J. Pannekoek, "Statistical methods for some simple disclosure limitation rules," Statistica Neerlandica, vol. 53, no. 1, pp. 55–67, 1999.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class KMap extends ImplicitPrivacyCriterion {
    
    /**
     * The estimators.
     * 
     */
    public enum Estimator {
                           NONE,
                           POISSON,
                           ZERO_TRUNCATED_POISSON
    }
    
    /** SVUID */
    private static final long        serialVersionUID = -6966985761538810077L;
                                                      
    /** K */
    private final int                k;
                                     
    /** A compressed representation of the research subset. */
    private DataSubset               subset;
                                     
    /** The parameter k'. */
    private int                      derivedK;
                                     
    /** The maximal allowed error */
    protected final double           error;
                                     
    /** The population model */
    private final ARXPopulationModel populationModel;
                                     
    /** The selected estimator */
    private final Estimator          estimator;
                                     
    /**
     * Creates a new instance of the k-map criterion as proposed by Latanya Sweeney
     * @param k
     * @param subset Research subset
     */
    public KMap(int k, DataSubset subset) {
        this(k, 0d, null, Estimator.NONE, null);
    }
    
    /**
     * Creates a new instance of the criterion. Using the Poisson estimator.
     */
    public KMap(int k, double error, ARXPopulationModel populationModel) {
        this(k, error, populationModel, Estimator.POISSON, null);
    }
    
    /**
     * Creates a new instance of the criterion.
     */
    public KMap(int k, double error, ARXPopulationModel populationModel, Estimator estimator) {
        this(k, error, populationModel, estimator, null);
    }
    
    /**
     * Internal constructor.
     * @param k
     * @param error
     * @param populationModel
     * @param estimator
     */
    private KMap(int k, double error, ARXPopulationModel populationModel, Estimator estimator, DataSubset subset) {
        super(true, true);
        this.k = k;
        this.populationModel = populationModel;
        this.subset = subset;
        if ((error < 0) || (error > 1d)) {
            throw new IllegalArgumentException("Error has to be between 0 and 1.");
        }
        this.error = error;
        this.estimator = estimator;
        if ((estimator == Estimator.NONE) && (this.subset == null)) {
            throw new IllegalArgumentException("If no estimator is defined a subset has to be provided!");
        }
    }
    
    @Override
    public KMap clone() {
        return new KMap(getK(), getError(), ((getPopulationModel() == null) ? null : getPopulationModel().clone()), getEstimator(), ((getSubset() == null) ? null : getSubset().clone()));
    }
    
    /**
     * Returns the derived parameter k.
     *
     * @return
     */
    public int getDerivedK() {
        return this.derivedK;
    }
    
    /**
     * Returns the specified error.
     * @return
     */
    public double getError() {
        return this.error;
    }
    
    /**
     * Returns the specified estimator.
     * @return
     */
    public Estimator getEstimator() {
        return this.estimator;
    }
    
    /**
     * Returns k.
     *
     * @return
     */
    public int getK() {
        return this.k;
    }
    
    /**
     * Returns the population model.
     * @return
     */
    public ARXPopulationModel getPopulationModel() {
        return this.populationModel;
    }
    
    @Override
    public int getRequirements() {
        switch (this.estimator) {
        case NONE:
            // Requires two counters
            return ARXConfiguration.REQUIREMENT_COUNTER |
                   ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
        case POISSON:
        case ZERO_TRUNCATED_POISSON:
            // Requires only one counter
            return ARXConfiguration.REQUIREMENT_COUNTER;
        default:
            throw new IllegalArgumentException("Should not happen!");
        }
        
    }
    
    /**
     * Returns the research subset.
     *
     * @return
     */
    public DataSubset getSubset() {
        return this.subset;
    }
    
    /**
     * Return true if this criterion has a subset.
     * @return
     */
    public boolean hasSubset() {
        return this.subset != null;
    }
    
    @Override
    public void initialize(DataManager manager) {
        super.initialize(manager);
        switch (this.estimator) {
        case POISSON:
            double samplingFraction = this.populationModel.getSamplingFraction(manager.getDataGeneralized().getDataLength());
            this.derivedK = calculateKPoisson(samplingFraction * this.k);
            break;
        case ZERO_TRUNCATED_POISSON:
            samplingFraction = this.populationModel.getSamplingFraction(manager.getDataGeneralized().getDataLength());
            this.derivedK = calculateKZeroPoisson(samplingFraction * this.k);
            break;
        case NONE:
            // Do nothing
            break;
        default:
            throw new IllegalArgumentException("Should not happen!");
        }
        
        // Check boundary
        if (this.derivedK > manager.getDataGeneralized().getDataLength()) {
            this.derivedK = manager.getDataGeneralized().getDataLength();
        }
    }
    
    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        switch (this.estimator) {
        case NONE:
            return entry.pcount >= this.k;
        case POISSON:
        case ZERO_TRUNCATED_POISSON:
            return entry.count >= this.derivedK;
        default:
            throw new IllegalArgumentException("Should not happen!");
        }
    }
    
    @Override
    public String toString() {
        String value = "(" + this.k + ")-map";
        if (this.estimator != Estimator.NONE) {
            value += " (" + this.derivedK + "-anonymity used " + this.estimator + ")";
        }
        return value;
    }
    
    /**
     * Calculates k, based on Poisson distribution.
     * @param lambda
     * @return
     */
    private int calculateKPoisson(double lambda) {
        
        final double threshold = 1d - this.error;
        final PoissonDistribution distribution = new PoissonDistribution(lambda);
        int counter = 0;
        double value = 0;
        while (value < threshold) {
            // value += (Math.pow(lambda, counter) * Math.exp(-lambda)) / ArithmeticUtils.factorial(counter);
            value = distribution.cumulativeProbability(counter);
            counter++;
        }
        return counter + 1;
    }
    
    /**
     * Calculates k, based on Zero-truncated Poisson distribution.
     * https://en.wikipedia.org/wiki/Zero-truncated_Poisson_distribution
     * 
     * @param lambda
     * @return
     */
    private int calculateKZeroPoisson(double lambda) {
        
        final double threshold = 1d - this.error;
        final PoissonDistribution distribution = new PoissonDistribution(lambda);
        final double v2 = 1d - distribution.cumulativeProbability(0);
        int counter = 0;
        double value = 0;
        while (value < threshold) {
            // value += ((Math.pow(lambda, counter)) / (Math.exp(lambda) - 1)) * ArithmeticUtils.factorial(counter);
            value = distribution.cumulativeProbability(counter) / v2;
            counter++;
        }
        return counter + 1;
    }
}
