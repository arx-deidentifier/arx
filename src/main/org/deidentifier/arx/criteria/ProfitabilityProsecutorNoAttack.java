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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXCostBenefitConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * Privacy model for the "no-attack" variant of the game theoretic approach proposed in:
 * A Game Theoretic Framework for Analyzing Re-Identification Risk.
 * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton,
 * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
 * PLOS|ONE. 2015.
 * 
 * @author Fabian Prasser
 */
public class ProfitabilityProsecutorNoAttack extends ImplicitPrivacyCriterion {

    /** SVUID */
    private static final long           serialVersionUID = -1283022087083117810L;

    /** The underlying k-anonymity privacy model */
    private int                         k                = -1;

    /** The underlying cost/benefit configuration */
    private ARXCostBenefitConfiguration config           = null;

    /**
     * Creates a new instance
     * @param config
     */
    public ProfitabilityProsecutorNoAttack() {
        // This model is monotonic
        super(true, true);
    }
    
    @Override
    public PrivacyCriterion clone() {
        return new ProfitabilityProsecutorNoAttack();
    }
    
    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        return clone();
    }
    
    @Override
    public DataSubset getDataSubset() {
        return null;
    }
    
    /**
     * Returns the parameter k
     * @return
     */
    public int getK() {
        return this.k;
    }
    
    @Override
    public int getMinimalClassSize() {
        return this.k;
    }
    
    @Override
    public int getRequirements() {
        return ARXConfiguration.REQUIREMENT_COUNTER;
    }
    
    @Override
    public double getRiskThresholdJournalist() {
        return getRiskThresholdProsecutor();
    }
    
    @Override
    public double getRiskThresholdMarketer() {
        return getRiskThresholdProsecutor();
    }

    @Override
    public double getRiskThresholdProsecutor() {
        return 1d / (double)k;
    }
    
    @Override
    public void initialize(DataManager manager, ARXConfiguration config) {

        // Compute domain shares
        this.config = config.getCostBenefitConfiguration();
        
        // We reduce this model to k-map or k-anonymity:
        // adversaryPayoff = adversaryGain * successProbability - adversaryCost
        // To give the adversary no incentives to attack we need to ensure that the following holds:
        // adversaryGain * successProbability < adversaryCost
        // -> successProbability < adversaryCost / adversaryGain
        // With successProbability = 1 / [size of (population) group of r], we have:
        // -> 1 / [size of (population) group of r] < adversaryCost / adversaryGain
        // -> [size of (population) group of r] > adversaryGain / adversaryCost
        double threshold = this.config.getAdversaryGain() / this.config.getAdversaryCost();
        if (this.config.getAdversaryGain() == 0) {
            this.k = 1;
        } else if (Double.isInfinite(threshold)) {
            this.k = Integer.MAX_VALUE;
        } else if ((threshold == Math.floor(threshold))) {
            this.k = (int)threshold + 1;
        } else {
            this.k = (int)Math.ceil(threshold);
        }
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        return entry.count >= k;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public boolean isMinimalClassSizeAvailable() {
        return k != -1;
    }

    @Override
    public boolean isSubsetAvailable() {
        return false;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("No-attack profitability");
        result.addProperty("Attacker model", "Prosecutor");
        if (config != null) {
            result.addProperty("Threshold", getK());
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
     * Returns the current configuration
     * @return
     */
    protected ARXCostBenefitConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Returns a string representation
     */
    protected String toString(String attackerModel) {
        return "no-attack-profitability (" + attackerModel + ")" + (config != null ? config.toString() : "");
    }
}
