/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXStackelbergConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLossWithBound;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class implements a prototype metric which maximizes publisher benefit in the Stackelberg game.
 * 
 * @author Fabian Prasser
 */
public class MetricSDNMPublisherBenefit extends AbstractMetricSingleDimensional {

    /** SUID. */
    private static final long                 serialVersionUID = 5729454129866471107L;

    /** Domain shares for each dimension. */
    private DomainShare[]                     shares;

    /** Max-IL */
    private double                            maxIL;

    /** Config for the Stackelberg game */
    private final ARXStackelbergConfiguration config;

    /**
     * Clone constructor
     * @param config
     * @param shares
     * @param maxIL
     */
    private MetricSDNMPublisherBenefit(ARXStackelbergConfiguration config,
                                       DomainShare[] shares,
                                       double maxIL) {
        super(false, false);
        this.config = config;
        this.shares = shares;
        this.maxIL = maxIL;
    }

    /**
     * Creates a new instance
     * @param config
     */
    public MetricSDNMPublisherBenefit(ARXStackelbergConfiguration config) {
        super(false, false);
        this.config = config;
    }

    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(rows * this.config.getPublisherBenefit());
        }
    }

    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(0d);
    }

    /**
     * Returns the configuration of this metric.
     * 
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false, // monotonic
                                       0.5d, // gs-factor
                                       false, // precomputed
                                       0.0d, // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
        );
    }

    @Override
    public String getName() {
        return "PublisherBenefit";
    }

    /**
     * Returns the publisher payoff for one record of the equivalence class. We return  three variants
     * of the publisher's payoff to be able to construct (1) a pruning strategy, (2) a privacy model, (3) a quality model:<br>
     * <br>
     * (1) Considering only attribute generalization (ignoring record suppression) - the adversary never attacks a record<br>
     * (2) Considering only attribute generalization (ignoring record suppression) - the adversary attacks if her payoff is positive<br>
     * (3) Considering attribute generalization and record suppression - the adversary attacks if her payoff is positive<br>
     * 
     * @param transformation
     * @param entry
     * @return
     */
    public double[] getPublisherPayoff(Transformation transformation, HashGroupifyEntry entry) {

        // Compute success probability: if the metric is configured to use the journalist risk, 
        // but no population table is available, we silently default to the prosecutor model.
        double adversarySuccessProbability = config.isProsecutorAttackerModel() || entry.pcount == 0 ? 1d / entry.count : 1d / entry.pcount;
        
        // Determine adversary's payoff
        double adversaryPayoff = config.getAdversaryGain() * adversarySuccessProbability - config.getAdversaryCost();

        // Determine the benefit of the publisher
        double benefit = getBenefit(transformation, entry);
        
        // Prepare result
        double[] result = new double[3];
        result[0] = benefit;
        result[1] = benefit - (adversaryPayoff > 0 ? config.getPublisherLoss() * adversarySuccessProbability : 0d);
        result[2] = entry.isNotOutlier ? result[1] : 0d;
        
        // Return
        return result;
    }

    /**
     * Returns the configuration for the Stackelberg game
     * @return
     */
    public ARXStackelbergConfiguration getStackelbergConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "PublisherBenefit (" + config.getPublisherBenefit() + ")";
    }

    /**
     * Returns the publisher's benefit when sharing a generalized record
     * @param entry
     * @return
     */
    private double getBenefit(Transformation transformation, HashGroupifyEntry entry) {

        // Although the equivalence class likely contains multiple records, we can think of
        // it as containing exactly one record when reasoning about payoffs. 
        int[] generalization = transformation.getGeneralization();
        double benefit = 0d;
        
        // Calculate
        for (int dimension = 0; dimension < shares.length; dimension++) {
            
            int value = entry.key[dimension];
            int level = generalization[dimension];
            double share = 1d / (shares[dimension].getShare(value, level) * shares[dimension].getDomainSize());
            benefit -= Math.log10(share);
        }
        
        // Normalize
        benefit /= maxIL;
        benefit = config.getPublisherBenefit() * (1d - benefit);
        
        // Return
        return benefit;
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation transformation, HashGroupify g) {
        
        // Compute
        double real = 0;
        double bound = 0;
        HashGroupifyEntry entry = g.getFirstEquivalenceClass();
        while (entry != null) {
            if (entry.count > 0) {
                double[] payoff = getPublisherPayoff(transformation, entry);
                real += entry.count * payoff[2];
                bound += entry.count * payoff[0];
            }
            entry = entry.nextOrdered;
        }
        
        // Invert
        real = this.getNumTuples() * this.config.getPublisherBenefit() - real;
        bound = this.getNumTuples() * this.config.getPublisherBenefit() - bound;
        
        // Return
        return new ILSingleDimensionalWithBound(real, bound);
    }

    @Override
    protected InformationLossWithBound<ILSingleDimensional> getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation transformation,
                                                        HashGroupify g) {

        // Compute
        double bound = 0;
        HashGroupifyEntry entry = g.getFirstEquivalenceClass();
        while (entry != null) {
            if (entry.count > 0) {
                bound += entry.count * getPublisherPayoff(transformation, entry)[0];
            }
            entry = entry.nextOrdered;
        }
        
        // Invert
        bound = this.getNumTuples() * this.config.getPublisherBenefit() - bound;
        
        // Return
        return new ILSingleDimensional(bound);
    }

    /**
     * For subclasses.
     * 
     * @return
     */
    protected DomainShare[] getShares() {
        return this.shares;
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition,
                                      final Data input,
                                      final GeneralizationHierarchy[] hierarchies,
                                      final ARXConfiguration config) {

        // Prepare weights
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Compute domain shares
        this.shares =  manager.getDomainShares();
                
        // Precompute MaxIL
        this.maxIL = 0d;
        for (DomainShare share : shares) {
            maxIL -= Math.log10(1d / share.getDomainSize());
        }
    }
    
    @Override
    public MetricSDNMPublisherBenefit clone() {
        
        DomainShare[] shares = new DomainShare[this.shares.length];
        for (int i = 0; i < this.shares.length; i++) {
            shares[i] = this.shares[i].clone();
        }
        return new MetricSDNMPublisherBenefit(config.clone(), shares, maxIL);
    }
}
