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

package org.deidentifier.arx.criteria;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * This class implements the k-map privacy model as proposed by Latanya Sweeney.<br>
 * <br>
 * As an alternative to explicitly providing data about the underlying population, cell sizes can be can be estimated with
 * the D3 (Poisson) and D4 (zero-truncated Poisson) estimators proposed in:<br>
 * K. El Emam and F. Dankar, "Protecting privacy using k-anonymity" JAMIA, vol. 15, no. 5, pp. 627-637, 2008.<br>
 * <br>
 * The estimator D3 was first published in:<br>
 * J. Pannekoek, "Statistical methods for some simple disclosure limitation rules," Statistica Neerlandica, vol. 53, no. 1, pp. 55-67, 1999.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class KMap extends ImplicitPrivacyCriterion {
    
    /**
     * Estimators for cell sizes in the population.
     * 
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     */
    public enum CellSizeEstimator {
                                   
        /** Poisson distribution */
        POISSON("Poisson"),
        /** Truncate-at-zero Poisson distribution */
        ZERO_TRUNCATED_POISSON("Zero-truncated Poisson");
        
        /** Label */
        private String label;
        
        /** Creates a new instance */
        CellSizeEstimator(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return this.label;
        }
    }
    
    /** SVUID */
    private static final long        serialVersionUID = -6966985761538810077L;
                                                      
    /** K */
    private final int                k;
                                     
    /** A compressed representation of the research subset. */
    private DataSubset               subset;
                                     
    /** The parameter k'. */
    private int                      derivedK = -1;
                                     
    /** The significance level */
    private final double             significanceLevel;
                                     
    /** The population model */
    private final ARXPopulationModel populationModel;
                                     
    /** The selected estimator */
    private final CellSizeEstimator  estimator;
                                     
    /** The actual type I error. */
    private double                   type1Error;
                                     
    /**
     * Creates a new instance of the k-map criterion as proposed by Latanya Sweeney
     * @param k
     * @param subset Research subset
     */
    public KMap(int k, DataSubset subset) {
        this(k, 0d, null, null, subset);
    }
    
    /**
     * Creates a new instance of the criterion using thr Poisson estimator proposed by Pannekoek.
     */
    public KMap(int k, double significanceLevel, ARXPopulationModel populationModel) {
        this(k, significanceLevel, populationModel, CellSizeEstimator.POISSON, null);
    }
    
    /**
     * Creates a new instance of the criterion using the Poisson estimator proposed by Pannekoek or by El Emam.
     */
    public KMap(int k, double significanceLevel, ARXPopulationModel populationModel, CellSizeEstimator estimator) {
        this(k, significanceLevel, populationModel, estimator, null);
    }
    
    /**
     * Internal constructor.
     * @param k
     * @param significanceLevel
     * @param populationModel
     * @param estimator
     */
    private KMap(int k, double significanceLevel, ARXPopulationModel populationModel, CellSizeEstimator estimator, DataSubset subset) {
        super(true, true);
        this.k = k;
        this.populationModel = populationModel;
        this.subset = subset;
        if ((significanceLevel < 0) || (significanceLevel > 1d)) {
            throw new IllegalArgumentException("Significance level has to be between 0 and 1.");
        }
        this.significanceLevel = significanceLevel;
        this.estimator = estimator;
        if ((estimator == null) && (this.subset == null)) {
            throw new IllegalArgumentException("If no estimator is defined a subset has to be provided.");
        }
    }
    
    @Override
    public KMap clone() {
        return new KMap(getK(), getSignificanceLevel(), ((getPopulationModel() == null) ? null : getPopulationModel().clone()), getEstimator(), ((getDataSubset() == null) ? null : getDataSubset().clone()));
    }
    
    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        if (!isLocalRecodingSupported()) {
            throw new UnsupportedOperationException("Local recoding is not supported by this model");
        }
        // We replace estimated k-map with an according instance of k-anonymity.
        // This avoids the re-calculation of k' 
        return new KAnonymity(this.getDerivedK());
    }
    
    @Override
    public DataSubset getDataSubset() {
        return this.subset;
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
     * Returns the specified estimator.
     * @return
     */
    public CellSizeEstimator getEstimator() {
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
    
    @Override
    public int getMinimalClassSize() {
        if (!isAccurate()) {
            return this.derivedK;
        } else {
            return 0;
        }
    }
    
    @Override
    public ARXPopulationModel getPopulationModel() {
        return this.populationModel;
    }
    
    @Override
    public int getRequirements() {
        if (this.estimator == null) {
            // Requires two counters
            return ARXConfiguration.REQUIREMENT_COUNTER |
                   ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
        } else {
            // Requires only one counter
            return ARXConfiguration.REQUIREMENT_COUNTER;
        }
    }
    
    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdJournalist() {
        return 1d / (double)k;
    }
    
    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdMarketer() {
        return getRiskThresholdJournalist();
    }
    
    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdProsecutor() {
        if (isAccurate() || derivedK == -1) {
            return 1d;
        } else {
            return 1d / (double)derivedK;
        }
    }
    
    /**
     * Returns the specified significance level.
     * @return
     */
    public double getSignificanceLevel() {
        return this.significanceLevel;
    }
    
    /**
     * Returns the calculated type I error.
     * @return
     */
    public double getType1Error() {
        return this.type1Error;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void initialize(DataManager manager, ARXConfiguration config) {
        super.initialize(manager, config);
        
        // TODO: Needed for backwards compatibility of ARX 3.4.0 with previous versions
        if (this.populationModel != null) {
            this.populationModel.makeBackwardsCompatible(manager.getDataGeneralized().getDataLength());
        }
        
        if (this.estimator != null) {
            
            // TODO: consider subset/inclusion
            double samplingFraction =
                    (double)manager.getDataGeneralized().getDataLength() /
                    (double)this.populationModel.getPopulationSize();
            
            // Derive k
            switch (this.estimator) {
            case POISSON:
                this.derivedK = calculateKPoisson(samplingFraction * (double)this.k);
                break;
            case ZERO_TRUNCATED_POISSON:
                this.derivedK = calculateKZeroPoisson(samplingFraction * (double)this.k);
                break;
            default:
                throw new IllegalArgumentException("Unknown estimator: " + this.estimator);
            }
        }
        
        // Check bounds
        if (this.derivedK > manager.getDataGeneralized().getDataLength()) {
            this.derivedK = manager.getDataGeneralized().getDataLength();
        }
        this.derivedK = Math.min(this.k, this.derivedK);
    }

    /**
     * Return true if the population has been modeled explicitly.
     * This implies that no approximation is performed.
     * @return
     */
    public boolean isAccurate() {
        return this.subset != null;
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        if (this.estimator == null) {
            return entry.pcount >= this.k;
        } else {
            return entry.count >= this.derivedK;
        }
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return !isAccurate();
    }

    @Override
    public boolean isMinimalClassSizeAvailable() {
        return this.estimator != null && this.derivedK != -1;
    }

    @Override
    public boolean isSubsetAvailable() {
        return this.subset != null;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("k-Map");
        result.addProperty("Threshold (k)", k);
        if (this.estimator != null) {
            result.addProperty("Estimator", this.estimator.toString());
            if (this.derivedK != -1) {
                result.addProperty("Derived threshold", this.derivedK);
            }
            if (this.populationModel != null) {
                result.addProperty("Population", this.populationModel.getPopulationSize());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String value = "(" + this.k + ")-map";
        if (this.estimator != null) {
            if (derivedK == -1){
                value += " estimated as (unknown)-anonymity (" + this.estimator + ")";
            } else {
                value += " estimated as (" + this.derivedK + ")-anonymity (" + this.estimator + ")";
            }
        }
        return value;
    }

    /**
     * Calculates k, based on Poisson distribution.
     * @param lambda
     * @return
     */
    private int calculateKPoisson(double lambda) {
        
        final double threshold = 1d - this.significanceLevel;
        final PoissonDistribution distribution = new PoissonDistribution(lambda);
        int counter = 0;
        double value = 0;
        while (value < threshold) {
            // value += (Math.pow(lambda, counter) * Math.exp(-lambda)) / ArithmeticUtils.factorial(counter);
            value = distribution.cumulativeProbability(counter);
            counter++;
            // Break if the estimated k is equal or greater than the given k, as this makes no sense.
            if (counter >= this.k) {
                // We are 100% sure that the dataset fulfills k-map
                value = 1d;
                break;
            }
        }
        this.type1Error = 1d - value;
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
        
        final double threshold = 1d - this.significanceLevel;
        final PoissonDistribution distribution = new PoissonDistribution(lambda);
        final double v2 = 1d - distribution.probability(0);
        int counter = 1;
        double value = 0d;
        while (value < threshold) {
            // value2 += ((Math.pow(lambda, counter)) / (Math.exp(lambda) - 1)) * ArithmeticUtils.factorial(counter);
            value += distribution.probability(counter) / v2;
            counter++;
            // Break if the estimated k is equal or greater than the given k, as this makes no sense.
            if (counter >= this.k) {
                // We are 100% sure that the dataset fulfills k-map
                value = 1d;
                break;
            }
        }
        this.type1Error = 1d - value;
        return counter;
    }
}
