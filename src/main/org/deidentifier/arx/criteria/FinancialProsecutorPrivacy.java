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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXFinancialConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.v2.DomainShare;
import org.deidentifier.arx.risk.RiskModelFinancial;

/**
 * Privacy model for the game theoretic approach proposed in:
 * A Game Theoretic Framework for Analyzing Re-Identification Risk.
 * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton,
 * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
 * PLOS|ONE. 2015.
 * 
 * @author Fabian Prasser
 */
public class FinancialProsecutorPrivacy extends ImplicitPrivacyCriterion {

    /** SVUID */
    private static final long         serialVersionUID = -1698534839214708559L;

    /** Configuration */
    private ARXFinancialConfiguration config;

    /** Domain shares for each dimension. */
    private DomainShare[]             shares;

    /** MaxIL */
    private double                    maxIL;

    /** Risk model */
    private RiskModelFinancial        riskModel;

    /**
     * Creates a new instance of game theoretic approach proposed in:
     * A Game Theoretic Framework for Analyzing Re-Identification Risk.
     * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton, 
     * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
     * PLOS|ONE. 2015. 
     */
    public FinancialProsecutorPrivacy(){
        // This model is not monotonic:
        // Often, generalization only marginally reduces the adversary's success
        // probability but at the same time it significantly reduces the
        // publisher's payout - up to the point where it does not make sense to
        // keep a record anymore.
        super(false, false);
    }

    @Override
    public FinancialProsecutorPrivacy clone() {
        return new FinancialProsecutorPrivacy();
    }

    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        return clone();
    }
    
    /**
     * Returns the config
     * @return
     */
    public ARXFinancialConfiguration getConfiguration() {
        return this.config;
    }
    
    @Override
    public DataSubset getDataSubset() {
        return null;
    }
    
    @Override
    public int getRequirements(){
        return ARXConfiguration.REQUIREMENT_COUNTER;
    }

    @Override
    public void initialize(DataManager manager, ARXConfiguration config) {

        // Compute domain shares
        this.shares =  manager.getDomainShares();
        this.config = config.getFinancialConfiguration();
        this.riskModel = new RiskModelFinancial(this.config);
                
        // Calculate MaxIL
        this.maxIL = 1d;
        for (DomainShare share : shares) {
            maxIL *= share.getDomainSize();
        }
        maxIL = Math.log10(maxIL);
    }

    @Override
    public boolean isAnonymous(Transformation transformation, HashGroupifyEntry entry) {
        
        // This is a class containing only records from the population
        if (entry.count == 0) {
            return false;
        }
        
        // Calculate information loss and success probability
        double informationLoss = getEntropyBasedInformationLoss(transformation, entry);
        double successProbability = getSuccessProbability(entry);
        double publisherPayoff = riskModel.getExpectedPublisherPayout(informationLoss, successProbability);
                
        // We keep the set of records if the payoff is > 0
        return publisherPayoff > 0;
    }
    
    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public boolean isSubsetAvailable() {
        return false;
    }
    
    @Override
    public ElementData render() {
        ElementData result = new ElementData("Financial privacy");
        result.addProperty("Attacker model", "Prosecutor");
        if (config != null) {
            result.addProperty("Adversary cost", config.getAdversaryCost());
            result.addProperty("Adversary gain", config.getAdversaryGain());
            result.addProperty("Publisher loss", config.getPublisherLoss());
            result.addProperty("Publisher benefit", config.getPublisherBenefit());
        }
        return result;
    }

    @Override
    public String toString() {
        return toString("prosecutor");
    }

    /**
     * Returns the information loss for the according class. This is an exact copy of: 
     * @see MetricSDNMEntropyBasedInformationLoss.getEntropyBasedInformationLoss(Transformation, HashGroupifyEntry)
     */
    private double getEntropyBasedInformationLoss(Transformation transformation, HashGroupifyEntry entry) {
        int[] generalization = transformation.getGeneralization();
        double infoLoss = 1d;
        for (int dimension = 0; dimension < shares.length; dimension++) {
            int value = entry.key[dimension];
            int level = generalization[dimension];
            infoLoss *= shares[dimension].getShare(value, level);
        }
        return Math.log10(infoLoss) / maxIL + 1d;
    }

    /**
     * Returns the success probability. If the game is configured to use the journalist risk, 
     * but no population table is available, we silently default to the prosecutor model.
     * @param entry
     * @return
     */
    protected double getSuccessProbability(HashGroupifyEntry entry) {
        return 1d / entry.count;
    }

    /**
     * Returns a string representation
     */
    protected String toString(String attackerModel) {
        return "financial-privacy (" + attackerModel + ")" + (config != null ? config.toString() : "");
    }
}